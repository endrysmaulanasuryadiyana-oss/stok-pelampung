package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.JenisTransaksi

private fun hanyaAngka(teks: String): String = teks.filter { it.isDigit() }.take(7)

@Composable
private fun KolomAngkaInput(
    nilai: String,
    onUbah: (String) -> Unit,
    label: String,
    keterangan: String? = null,
) {
    OutlinedTextField(
        value = nilai,
        onValueChange = { onUbah(hanyaAngka(it)) },
        label = { Text(label) },
        supportingText = if (keterangan != null) {
            { Text(keterangan) }
        } else {
            null
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
    )
}

/**
 * Dialog tambah barang baru (awal == null) atau ubah barang yang sudah ada.
 * onSimpan(nama, keterangan, stokMinimum, stokAwal)
 */
@Composable
fun DialogBarang(
    awal: Barang?,
    namaTerpakai: List<String>,
    onTutup: () -> Unit,
    onSimpan: (String, String, Int, Int) -> Unit,
) {
    var nama by rememberSaveable { mutableStateOf(awal?.nama ?: "") }
    var keterangan by rememberSaveable { mutableStateOf(awal?.keterangan ?: "") }
    var minimum by rememberSaveable { mutableStateOf(awal?.stokMinimum?.toString() ?: "5") }
    var stokAwal by rememberSaveable { mutableStateOf("") }
    var galat by rememberSaveable { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text(if (awal == null) "Tambah Barang" else "Ubah Barang") },
        text = {
            Column(
                Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it; galat = null },
                    label = { Text("Nama barang") },
                    placeholder = { Text("cth. Pelampung Bulu 3 gr") },
                    singleLine = true,
                    isError = galat != null,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    label = { Text("Keterangan (opsional)") },
                    placeholder = { Text("cth. warna, ukuran, merek") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (awal == null) {
                    KolomAngkaInput(
                        stokAwal, { stokAwal = it },
                        "Stok awal (opsional)",
                        "Jumlah yang sudah ada di gudang sekarang",
                    )
                }
                KolomAngkaInput(
                    minimum, { minimum = it },
                    "Batas stok menipis",
                    "Diberi tanda jika sisa ≤ angka ini",
                )
                galat?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val n = nama.trim()
                val dipakai = namaTerpakai.any {
                    it.equals(n, ignoreCase = true) && !it.equals(awal?.nama, ignoreCase = true)
                }
                when {
                    n.isEmpty() -> galat = "Nama barang wajib diisi"
                    dipakai -> galat = "Barang dengan nama ini sudah ada"
                    else -> onSimpan(
                        n,
                        keterangan.trim(),
                        minimum.toIntOrNull() ?: 0,
                        stokAwal.toIntOrNull() ?: 0,
                    )
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}

/** Dialog mencatat barang masuk atau barang keluar. onSimpan(jumlah, catatan) */
@Composable
fun DialogTransaksi(
    barang: Barang,
    jenis: JenisTransaksi,
    onTutup: () -> Unit,
    onSimpan: (Int, String) -> Unit,
) {
    var jumlah by rememberSaveable { mutableStateOf("") }
    var catatan by rememberSaveable { mutableStateOf("") }
    var galat by rememberSaveable { mutableStateOf<String?>(null) }

    val masuk = jenis == JenisTransaksi.MASUK
    val angka = jumlah.toIntOrNull() ?: 0
    val sisaBaru = if (masuk) barang.sisa + angka else barang.sisa - angka

    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text(if (masuk) "Barang Masuk" else "Barang Keluar") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(barang.nama, fontWeight = FontWeight.SemiBold)
                Text("Di gudang sekarang: ${formatAngka(barang.sisa)}", color = Color.Gray)
                KolomAngkaInput(jumlah, { jumlah = it; galat = null }, "Jumlah")
                OutlinedTextField(
                    value = catatan,
                    onValueChange = { catatan = it },
                    label = { Text("Catatan (opsional)") },
                    placeholder = {
                        Text(if (masuk) "cth. kiriman dari supplier" else "cth. dijual ke Toko Jaya")
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                if (angka > 0) {
                    Text(
                        "Sisa setelah ini: ${formatAngka(sisaBaru)}",
                        color = if (sisaBaru < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
                galat?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                when {
                    angka <= 0 -> galat = "Isi jumlah barang"
                    !masuk && angka > barang.sisa ->
                        galat = "Stok tidak cukup. Di gudang hanya ${formatAngka(barang.sisa)}"
                    else -> onSimpan(angka, catatan.trim())
                }
            }) { Text("Simpan") }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}

@Composable
fun DialogKonfirmasi(
    judul: String,
    isi: String,
    tombol: String,
    onYa: () -> Unit,
    onTutup: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onTutup,
        title = { Text(judul) },
        text = { Text(isi) },
        confirmButton = {
            TextButton(onClick = onYa) { Text(tombol, color = MaterialTheme.colorScheme.error) }
        },
        dismissButton = { TextButton(onClick = onTutup) { Text("Batal") } },
    )
}
