package com.stokpancing.pelampung.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.stokpancing.pelampung.data.Barang
import com.stokpancing.pelampung.data.JenisTransaksi
import com.stokpancing.pelampung.data.Transaksi

/** Label kecil + angka besar, dipakai untuk Masuk / Keluar / Di Gudang. */
@Composable
fun KolomAngka(
    label: String,
    nilai: Int,
    warna: Color,
    modifier: Modifier = Modifier,
    tebal: Boolean = false,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = Color.Gray)
        Text(
            formatAngka(nilai),
            style = if (tebal) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.titleLarge,
            fontWeight = if (tebal) FontWeight.Bold else FontWeight.SemiBold,
            color = warna,
        )
    }
}

@Composable
fun LabelMenipis() {
    Surface(color = WarnaMenipis, shape = RoundedCornerShape(50)) {
        Text(
            "Stok menipis",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
        )
    }
}

@Composable
fun KartuBarang(barang: Barang, onKlik: () -> Unit) {
    Card(
        onClick = onKlik,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        barang.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (barang.keterangan.isNotBlank()) {
                        Text(
                            barang.keterangan,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                if (barang.menipis) {
                    Spacer(Modifier.width(8.dp))
                    LabelMenipis()
                }
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                KolomAngka("Stok Masuk", barang.totalMasuk, WarnaMasuk, Modifier.weight(1f))
                KolomAngka("Barang Keluar", barang.totalKeluar, WarnaKeluar, Modifier.weight(1f))
                KolomAngka(
                    "Di Gudang",
                    barang.sisa,
                    if (barang.menipis) WarnaMenipis else MaterialTheme.colorScheme.primary,
                    Modifier.weight(1f),
                    tebal = true,
                )
            }
        }
    }
}

@Composable
fun BarisTransaksi(
    transaksi: Transaksi,
    tampilkanNama: Boolean,
    onHapus: (() -> Unit)? = null,
) {
    val masuk = transaksi.jenis == JenisTransaksi.MASUK
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(
            Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                if (tampilkanNama) {
                    Text(
                        transaksi.namaBarang,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    (if (masuk) "Barang masuk" else "Barang keluar") + " • " + formatWaktu(transaksi.waktu),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                )
                if (transaksi.catatan.isNotBlank()) {
                    Text(transaksi.catatan, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text(
                (if (masuk) "+" else "−") + formatAngka(transaksi.jumlah),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (masuk) WarnaMasuk else WarnaKeluar,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            if (onHapus != null) {
                IconButton(onClick = onHapus) {
                    Icon(Icons.Default.Delete, contentDescription = "Hapus catatan", tint = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun TeksKosong(teks: String) {
    Text(
        teks,
        style = MaterialTheme.typography.bodyLarge,
        color = Color.Gray,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 24.dp),
        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
    )
}
