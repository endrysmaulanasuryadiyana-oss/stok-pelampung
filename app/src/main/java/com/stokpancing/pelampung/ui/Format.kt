package com.stokpancing.pelampung.ui

import android.content.Context
import android.content.Intent
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val localeIndonesia: Locale = Locale.forLanguageTag("id-ID")

fun formatWaktu(waktu: Long): String =
    SimpleDateFormat("dd MMM yyyy, HH:mm", localeIndonesia).format(Date(waktu))

/** 12500 -> "12.500" */
fun formatAngka(angka: Int): String = NumberFormat.getIntegerInstance(localeIndonesia).format(angka.toLong())

fun bagikanTeks(context: Context, teks: String) {
    val kirim = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, teks)
    }
    context.startActivity(Intent.createChooser(kirim, "Bagikan laporan stok"))
}
