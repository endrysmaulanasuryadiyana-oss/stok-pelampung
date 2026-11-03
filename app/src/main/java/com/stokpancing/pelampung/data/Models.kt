package com.stokpancing.pelampung.data

/** Satu jenis barang, misalnya "Pelampung Bulu 3 gram". */
data class Barang(
    val id: Long,
    val nama: String,
    val keterangan: String,
    val stokMinimum: Int,
    val totalMasuk: Int,
    val totalKeluar: Int,
) {
    /** Jumlah barang yang ada di gudang sekarang. */
    val sisa: Int get() = totalMasuk - totalKeluar

    /** True jika stok sudah mencapai atau di bawah batas minimum. */
    val menipis: Boolean get() = sisa <= stokMinimum
}

enum class JenisTransaksi { MASUK, KELUAR }

/** Catatan satu kali barang masuk atau keluar. */
data class Transaksi(
    val id: Long,
    val barangId: Long,
    val namaBarang: String,
    val jenis: JenisTransaksi,
    val jumlah: Int,
    val catatan: String,
    val waktu: Long,
)
