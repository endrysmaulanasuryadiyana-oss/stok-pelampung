package com.stokpancing.pelampung

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.stokpancing.pelampung.ui.BerandaKonten
import com.stokpancing.pelampung.ui.DetailScreen
import com.stokpancing.pelampung.ui.DialogBarang
import com.stokpancing.pelampung.ui.RiwayatKonten
import com.stokpancing.pelampung.ui.StokTheme
import com.stokpancing.pelampung.ui.StokViewModel
import com.stokpancing.pelampung.ui.bagikanTeks

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StokTheme {
                AplikasiStok()
            }
        }
    }
}

private const val TANPA_DETAIL = -1L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AplikasiStok(vm: StokViewModel = viewModel()) {
    val state by vm.state.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val context = LocalContext.current

    var tab by rememberSaveable { mutableIntStateOf(0) }
    var detailId by rememberSaveable { mutableLongStateOf(TANPA_DETAIL) }
    var dialogTambah by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.pesan.collect { snackbar.showSnackbar(it) }
    }

    if (detailId != TANPA_DETAIL) {
        BackHandler { detailId = TANPA_DETAIL }
        DetailScreen(
            barang = state.barang.find { it.id == detailId },
            riwayat = state.riwayat.filter { it.barangId == detailId },
            semuaNama = state.barang.map { it.nama },
            sudahDimuat = state.sudahDimuat,
            vm = vm,
            snackbar = snackbar,
            onKembali = { detailId = TANPA_DETAIL },
        )
    } else {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(if (tab == 0) "Stok Pelampung Pancing" else "Riwayat Masuk & Keluar") },
                    actions = {
                        if (tab == 0) {
                            IconButton(onClick = { bagikanTeks(context, vm.laporanTeks()) }) {
                                Icon(Icons.Default.Share, contentDescription = "Bagikan laporan stok")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        titleContentColor = Color.White,
                        actionIconContentColor = Color.White,
                    ),
                )
            },
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = tab == 0,
                        onClick = { tab = 0 },
                        icon = { Icon(Icons.Default.Home, contentDescription = null) },
                        label = { Text("Stok Gudang") },
                    )
                    NavigationBarItem(
                        selected = tab == 1,
                        onClick = { tab = 1 },
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                        label = { Text("Riwayat") },
                    )
                }
            },
            floatingActionButton = {
                if (tab == 0) {
                    ExtendedFloatingActionButton(
                        onClick = { dialogTambah = true },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Barang") },
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = Color.White,
                    )
                }
            },
            snackbarHost = { SnackbarHost(snackbar) },
        ) { padding ->
            if (tab == 0) {
                BerandaKonten(state, padding) { id -> detailId = id }
            } else {
                RiwayatKonten(state, padding)
            }
        }
    }

    if (dialogTambah) {
        DialogBarang(
            awal = null,
            namaTerpakai = state.barang.map { it.nama },
            onTutup = { dialogTambah = false },
            onSimpan = { nama, ket, min, stokAwal ->
                vm.tambahBarang(nama, ket, min, stokAwal)
                dialogTambah = false
            },
        )
    }
}
