package com.stokpancing.pelampung.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Database SQLite yang tersimpan di HP (tidak perlu internet). */
private class StokDatabase(context: Context) :
    SQLiteOpenHelper(context, "stok_pelampung.db", null, 1) {

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE barang (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nama TEXT NOT NULL,
                keterangan TEXT NOT NULL DEFAULT '',
                stok_minimum INTEGER NOT NULL DEFAULT 0,
                dibuat INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL(
            """
            CREATE TABLE transaksi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                barang_id INTEGER NOT NULL REFERENCES barang(id) ON DELETE CASCADE,
                jenis TEXT NOT NULL,
                jumlah INTEGER NOT NULL,
                catatan TEXT NOT NULL DEFAULT '',
                waktu INTEGER NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX idx_transaksi_barang ON transaksi(barang_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Versi pertama, belum ada perubahan struktur.
    }
}

class StokRepository(context: Context) {

    private val helper = StokDatabase(context.applicationContext)

    suspend fun semuaBarang(): List<Barang> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT b.id, b.nama, b.keterangan, b.stok_minimum,
                   COALESCE(SUM(CASE WHEN t.jenis = 'MASUK'  THEN t.jumlah END), 0),
                   COALESCE(SUM(CASE WHEN t.jenis = 'KELUAR' THEN t.jumlah END), 0)
            FROM barang b
            LEFT JOIN transaksi t ON t.barang_id = b.id
            GROUP BY b.id
            ORDER BY b.nama COLLATE NOCASE
        """.trimIndent()
        helper.readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Barang(
                            id = c.getLong(0),
                            nama = c.getString(1),
                            keterangan = c.getString(2),
                            stokMinimum = c.getInt(3),
                            totalMasuk = c.getInt(4),
                            totalKeluar = c.getInt(5),
                        )
                    )
                }
            }
        }
    }

    suspend fun semuaTransaksi(): List<Transaksi> = withContext(Dispatchers.IO) {
        val sql = """
            SELECT t.id, t.barang_id, b.nama, t.jenis, t.jumlah, t.catatan, t.waktu
            FROM transaksi t
            JOIN barang b ON b.id = t.barang_id
            ORDER BY t.waktu DESC, t.id DESC
        """.trimIndent()
        helper.readableDatabase.rawQuery(sql, null).use { c ->
            buildList {
                while (c.moveToNext()) {
                    add(
                        Transaksi(
                            id = c.getLong(0),
                            barangId = c.getLong(1),
                            namaBarang = c.getString(2),
                            jenis = JenisTransaksi.valueOf(c.getString(3)),
                            jumlah = c.getInt(4),
                            catatan = c.getString(5),
                            waktu = c.getLong(6),
                        )
                    )
                }
            }
        }
    }

    suspend fun tambahBarang(
        nama: String,
        keterangan: String,
        stokMinimum: Int,
        stokAwal: Int,
    ): Long = withContext(Dispatchers.IO) {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val nilai = ContentValues().apply {
                put("nama", nama)
                put("keterangan", keterangan)
                put("stok_minimum", stokMinimum)
                put("dibuat", System.currentTimeMillis())
            }
            val id = db.insertOrThrow("barang", null, nilai)
            if (stokAwal > 0) {
                simpanTransaksi(db, id, JenisTransaksi.MASUK, stokAwal, "Stok awal")
            }
            db.setTransactionSuccessful()
            id
        } finally {
            db.endTransaction()
        }
    }

    suspend fun ubahBarang(id: Long, nama: String, keterangan: String, stokMinimum: Int) =
        withContext(Dispatchers.IO) {
            val nilai = ContentValues().apply {
                put("nama", nama)
                put("keterangan", keterangan)
                put("stok_minimum", stokMinimum)
            }
            helper.writableDatabase.update("barang", nilai, "id = ?", arrayOf(id.toString()))
        }

    /** Menghapus barang beserta seluruh riwayatnya. */
    suspend fun hapusBarang(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("barang", "id = ?", arrayOf(id.toString()))
    }

    suspend fun tambahTransaksi(
        barangId: Long,
        jenis: JenisTransaksi,
        jumlah: Int,
        catatan: String,
    ) = withContext(Dispatchers.IO) {
        simpanTransaksi(helper.writableDatabase, barangId, jenis, jumlah, catatan)
    }

    suspend fun hapusTransaksi(id: Long) = withContext(Dispatchers.IO) {
        helper.writableDatabase.delete("transaksi", "id = ?", arrayOf(id.toString()))
    }

    private fun simpanTransaksi(
        db: SQLiteDatabase,
        barangId: Long,
        jenis: JenisTransaksi,
        jumlah: Int,
        catatan: String,
    ): Long {
        val nilai = ContentValues().apply {
            put("barang_id", barangId)
            put("jenis", jenis.name)
            put("jumlah", jumlah)
            put("catatan", catatan)
            put("waktu", System.currentTimeMillis())
        }
        return db.insertOrThrow("transaksi", null, nilai)
    }
}
