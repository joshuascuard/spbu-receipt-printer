package com.spbu.receiptprinter.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.data.model.Setting
import com.spbu.receiptprinter.ui.common.SPBUTextField
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.SectionHeader
import com.spbu.receiptprinter.ui.common.rememberSnackbarState
import com.spbu.receiptprinter.ui.transaction.DropdownField

@Composable
fun PengaturanScreen(
    onBack: () -> Unit,
    onHargaBbm: () -> Unit,
    onPrinter: () -> Unit,
    viewModel: PengaturanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val pesanInfo by viewModel.pesanInfo.collectAsStateWithLifecycle()
    val pesanError by viewModel.pesanError.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = rememberSnackbarState()

    // State lokal form (sinkronisasi dari uiState)
    var namaSpbu by remember { mutableStateOf("") }
    var nomorSpbu by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var footer by remember { mutableStateOf("") }
    var ukuranFont by remember { mutableStateOf("MEDIUM") }
    var jumlahCopy by remember { mutableStateOf("1") }
    var lebarKertas by remember { mutableStateOf("58") }
    var marginKiri by remember { mutableStateOf("0") }
    var darkMode by remember { mutableStateOf(false) }

    // Sinkronisasi dari database ke state lokal
    LaunchedEffect(uiState.setting) {
        val s = uiState.setting
        namaSpbu = s.namaSpbu
        nomorSpbu = s.nomorSpbu
        alamat = s.alamat
        footer = s.footer
        ukuranFont = s.ukuranFont
        jumlahCopy = s.jumlahCopy.toString()
        lebarKertas = s.lebarKertas.toString()
        marginKiri = s.marginKiri.toString()
        darkMode = s.darkMode
    }

    LaunchedEffect(pesanInfo) {
        if (pesanInfo.isNotBlank()) {
            snackbarState.showSnackbar(pesanInfo)
            viewModel.clearPesanInfo()
        }
    }

    LaunchedEffect(pesanError) {
        if (pesanError.isNotBlank()) {
            snackbarState.showSnackbar(pesanError)
            viewModel.clearPesanError()
        }
    }

    // Launcher untuk pilih logo
    val logoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.updateLogo(it) } }

    // Launcher untuk restore backup
    val restoreLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> uri?.let { viewModel.restoreDatabase(context, it) } }

    Scaffold(
        topBar = { SPBUTopBar(title = "Pengaturan", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarState) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = {
                        viewModel.simpan(
                            uiState.setting.copy(
                                namaSpbu = namaSpbu,
                                nomorSpbu = nomorSpbu,
                                alamat = alamat,
                                footer = footer,
                                ukuranFont = ukuranFont,
                                jumlahCopy = jumlahCopy.toIntOrNull() ?: 1,
                                lebarKertas = lebarKertas.toIntOrNull() ?: 58,
                                marginKiri = marginKiri.toIntOrNull() ?: 0,
                                darkMode = darkMode
                            )
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Simpan Pengaturan", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // ============ DATA SPBU ============
            SectionHeader("Data SPBU")
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SPBUTextField(
                    value = namaSpbu,
                    onValueChange = { namaSpbu = it },
                    label = "Nama SPBU"
                )
                SPBUTextField(
                    value = nomorSpbu,
                    onValueChange = { nomorSpbu = it },
                    label = "Nomor SPBU"
                )
                SPBUTextField(
                    value = alamat,
                    onValueChange = { alamat = it },
                    label = "Alamat SPBU",
                    singleLine = false,
                    maxLines = 3
                )
                SPBUTextField(
                    value = footer,
                    onValueChange = { footer = it },
                    label = "Footer Struk",
                    singleLine = false,
                    maxLines = 3,
                    placeholder = "Terima Kasih\nSelamat Jalan"
                )
            }

            // ============ LOGO ============
            SectionHeader("Logo SPBU")
            ListItem(
                headlineContent = { Text("Upload Logo") },
                supportingContent = {
                    Text(
                        if (uiState.setting.logoPath.isNotBlank()) "Logo sudah dipasang"
                        else "Belum ada logo",
                        style = MaterialTheme.typography.bodySmall
                    )
                },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = null) },
                trailingContent = {
                    TextButton(onClick = { logoLauncher.launch("image/*") }) {
                        Text("Pilih")
                    }
                }
            )

            // ============ PRINTER ============
            SectionHeader("Pengaturan Printer")
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DropdownField(
                    label = "Lebar Kertas",
                    selected = "$lebarKertas mm",
                    options = listOf("58 mm", "80 mm"),
                    onSelect = { lebarKertas = it.replace(" mm", "") }
                )
                DropdownField(
                    label = "Ukuran Font",
                    selected = ukuranFont,
                    options = listOf("SMALL", "MEDIUM", "LARGE"),
                    onSelect = { ukuranFont = it }
                )
                SPBUTextField(
                    value = jumlahCopy,
                    onValueChange = {
                        val n = it.toIntOrNull() ?: 1
                        jumlahCopy = n.coerceIn(1, 10).toString()
                    },
                    label = "Jumlah Copy (1-10)"
                )
                SPBUTextField(
                    value = marginKiri,
                    onValueChange = { marginKiri = it },
                    label = "Margin Kiri (karakter)"
                )
            }

            // Tombol ke halaman printer
            ListItem(
                headlineContent = { Text("Konfigurasi Printer Bluetooth") },
                supportingContent = { Text(if (uiState.setting.namaPrinter.isNotBlank()) "Default: ${uiState.setting.namaPrinter}" else "Belum ada printer default") },
                leadingContent = { Icon(Icons.Default.Print, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                modifier = Modifier.padding(horizontal = 0.dp)
            ).also {
                // Wrapper card clickable
            }
            TextButton(
                onClick = onPrinter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.Bluetooth, null)
                Spacer(Modifier.width(8.dp))
                Text("Buka Pengaturan Printer")
            }

            // ============ TAMPILAN ============
            SectionHeader("Tampilan")
            ListItem(
                headlineContent = { Text("Dark Mode") },
                trailingContent = {
                    Switch(
                        checked = darkMode,
                        onCheckedChange = {
                            darkMode = it
                            viewModel.toggleDarkMode(it)
                        }
                    )
                },
                leadingContent = { Icon(Icons.Default.DarkMode, null) }
            )

            // ============ HARGA BBM ============
            SectionHeader("Harga BBM")
            ListItem(
                headlineContent = { Text("Pengaturan Harga BBM") },
                supportingContent = { Text("Tambah, edit, atau hapus produk BBM") },
                leadingContent = { Icon(Icons.Default.LocalGasStation, null) },
                trailingContent = { Icon(Icons.Default.ChevronRight, null) }
            )
            TextButton(
                onClick = onHargaBbm,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(Icons.Default.EditNote, null)
                Spacer(Modifier.width(8.dp))
                Text("Kelola Harga BBM")
            }

            // ============ BACKUP & RESTORE ============
            SectionHeader("Backup & Restore")
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.backupDatabase(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Upload, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Backup")
                }
                OutlinedButton(
                    onClick = { restoreLauncher.launch("application/octet-stream") },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Download, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Restore")
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
