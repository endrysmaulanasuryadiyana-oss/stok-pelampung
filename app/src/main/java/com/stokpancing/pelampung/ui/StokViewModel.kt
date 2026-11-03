package com.stokpancing.pelampung.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.JenisTransaksi
import com.stokpancing.pelampung.data.StokRepository
import com.stokpancing.pelampung.data.Transaksi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class UiState(
    val barang: List<Barang> = emptyList(),
    val riwayat: List<Transaksi> = emptyList(),
    val sudahDimuat: Boolean = false,
)

class StokViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StokRepository(app)

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    private val _pesan = Channel<String>(Channel.BUFFERED)
    /** Pesan singkat yang ditampilkan di bagian bawah layar. */
    val pesan: Flow<String> = _pesan.receiveAsFlow()

    init {
        viewModelScope.launch { muatUlang() }
    }

    private suspend fun muatUlang() {
        val barang = repo.semuaBarang()
        val riwayat = repo.semuaTransaksi()
        _state.update { it.copy(barang = barang, riwayat = riwayat, sudahDimuat = true) }
    }

    private fun jalankan(pesanSelesai: String, aksi: suspend () -> Unit) {
        viewModelScope.launch {
            try {
                aksi()
                muatUlang()
                _pesan.send(pesanSelesai)
            } catch (e: Exception) {
                _pesan.send("Gagal menyimpan: ${e.message}")
            }
        }
    }

    fun tambahBarang(nama: String, keterangan: String, stokMinimum: Int, stokAwal: Int) =
        jalankan("Barang \"$nama\" ditambahkan") {
            repo.tambahBarang(nama, keterangan, stokMinimum, stokAwal)
        }

    fun ubahBarang(id: Long, nama: String, keterangan: String, stokMinimum: Int) =
        jalankan("Data barang diperbarui") {
            repo.ubahBarang(id, nama, keterangan, stokMinimum)
        }

    fun hapusBarang(barang: Barang) =
        jalankan("Barang \"${barang.nama}\" dihapus") { repo.hapusBarang(barang.id) }

    fun tambahTransaksi(barang: Barang, jenis: JenisTransaksi, jumlah: Int, catatan: String) {
        if (jumlah <= 0) return
        if (jenis == JenisTransaksi.KELUAR && jumlah > barang.sisa) return
        val teks = if (jenis == JenisTransaksi.MASUK) {
            "$jumlah barang masuk dicatat"
        } else {
            "$jumlah barang keluar dicatat"
        }
        jalankan(teks) { repo.tambahTransaksi(barang.id, jenis, jumlah, catatan) }
    }

    fun hapusTransaksi(transaksi: Transaksi) {
        val barang = _state.value.barang.find { it.id == transaksi.barangId } ?: return
        if (transaksi.jenis == JenisTransaksi.MASUK && barang.sisa - transaksi.jumlah < 0) {
            viewModelScope.launch {
                _pesan.send("Tidak bisa dihapus: stok di gudang akan menjadi minus")
            }
            return
        }
        jalankan("Catatan dihapus") { repo.hapusTransaksi(transaksi.id) }
    }

    /** Teks laporan stok untuk dibagikan lewat WhatsApp, dll. */
    fun laporanTeks(): String {
        val daftar = _state.value.barang
        if (daftar.isEmpty()) return "Belum ada data barang."
        return buildString {
            appendLine("LAPORAN STOK PELAMPUNG PANCING")
            appendLine(formatWaktu(System.currentTimeMillis()))
            appendLine()
            daftar.forEachIndexed { i, b ->
                appendLine("${i + 1}. ${b.nama}")
                appendLine(
                    "   Masuk: ${formatAngka(b.totalMasuk)} | Keluar: ${formatAngka(b.totalKeluar)}" +
                        " | Di gudang: ${formatAngka(b.sisa)}" +
                        if (b.menipis) "  (STOK MENIPIS)" else ""
                )
            }
            appendLine()
            append("Total di gudang: ${formatAngka(daftar.sumOf { it.sisa })} pcs")
        }
    }
}
