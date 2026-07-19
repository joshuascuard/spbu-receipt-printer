package com.spbu.receiptprinter.ui.transaction

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.ui.common.*
import java.util.Calendar

/** Opsi jenis pembayaran */
val OPSI_PEMBAYARAN = listOf("CASH", "QRIS", "Debit", "Kredit", "Lainnya")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormTransaksiScreen(
    transaksiId: Long?,
    onBack: () -> Unit,
    onPreview: (Long) -> Unit,
    viewModel: FormTransaksiViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = rememberSnackbarState()

    // Inisialisasi mode edit
    LaunchedEffect(transaksiId) {
        if (transaksiId != null) {
            viewModel.loadTransaksi(transaksiId)
        }
    }

    // Navigasi ke preview setelah simpan berhasil
    LaunchedEffect(uiState.isSimpanBerhasil) {
        if (uiState.isSimpanBerhasil && uiState.savedTransaksiId != -1L) {
            viewModel.resetSimpanBerhasil()
            onPreview(uiState.savedTransaksiId)
        }
    }

    // Tampilkan error di snackbar
    LaunchedEffect(uiState.errorPesan) {
        if (uiState.errorPesan.isNotBlank()) {
            snackbarState.showSnackbar(uiState.errorPesan)
            viewModel.resetError()
        }
    }

    // DatePicker
    val kalender = Calendar.getInstance()
    val datePicker = DatePickerDialog(
        context,
        { _, tahun, bulan, hari ->
            viewModel.setTanggal("${hari.toString().padStart(2, '0')}/${(bulan + 1).toString().padStart(2, '0')}/$tahun")
        },
        kalender.get(Calendar.YEAR),
        kalender.get(Calendar.MONTH),
        kalender.get(Calendar.DAY_OF_MONTH)
    )

    // TimePicker
    val timePicker = TimePickerDialog(
        context,
        { _, jam, menit ->
            viewModel.setJam("${jam.toString().padStart(2, '0')}:${menit.toString().padStart(2, '0')}:00")
        },
        kalender.get(Calendar.HOUR_OF_DAY),
        kalender.get(Calendar.MINUTE),
        true
    )

    Scaffold(
        topBar = {
            SPBUTopBar(
                title = if (uiState.isEditMode) "Edit Transaksi" else "Buat Transaksi",
                onBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) },
        bottomBar = {
            // Tombol simpan di bagian bawah layar
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { viewModel.simpan() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(52.dp),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Visibility, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Preview & Cetak",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ============ DATA TRANSAKSI ============
            SectionHeader("Data Transaksi")

            // Nomor Transaksi
            SPBUTextField(
                value = uiState.nomorTransaksi,
                onValueChange = viewModel::setNomorTransaksi,
                label = "Nomor Transaksi *",
                isError = uiState.errorNomor.isNotBlank(),
                errorMessage = uiState.errorNomor,
                placeholder = "Contoh: TRX-00001 atau 2745859"
            )

            // Tanggal
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = uiState.tanggal,
                    onValueChange = {},
                    label = { Text("Tanggal *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { datePicker.show() }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih tanggal")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )

                OutlinedTextField(
                    value = uiState.jam,
                    onValueChange = {},
                    label = { Text("Jam *") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { timePicker.show() }) {
                            Icon(Icons.Default.AccessTime, contentDescription = "Pilih jam")
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Tombol waktu sekarang
            OutlinedButton(
                onClick = viewModel::gunakanWaktuSekarang,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Gunakan Waktu Sekarang")
            }

            // Shift
            SPBUTextField(
                value = uiState.shift,
                onValueChange = viewModel::setShift,
                label = "Shift",
                keyboardType = KeyboardType.Number,
                placeholder = "1, 2, 3, ..."
            )

            // Operator
            SPBUTextField(
                value = uiState.operator,
                onValueChange = viewModel::setOperator,
                label = "Operator *",
                isError = uiState.errorOperator.isNotBlank(),
                errorMessage = uiState.errorOperator,
                placeholder = "Nama petugas"
            )

            // Jenis Pembayaran - Dropdown
            DropdownField(
                label = "Jenis Pembayaran *",
                selected = uiState.pembayaran,
                options = OPSI_PEMBAYARAN,
                onSelect = viewModel::setPembayaran
            )

            // Nomor Plat
            SPBUTextField(
                value = uiState.nomorPlat,
                onValueChange = viewModel::setNomorPlat,
                label = "Nomor Plat *",
                isError = uiState.errorPlat.isNotBlank(),
                errorMessage = uiState.errorPlat,
                placeholder = "Contoh: AD8629AV"
            )

            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader("Data BBM")

            // Jenis Produk BBM - Dropdown dari database
            DropdownField(
                label = "Jenis Produk BBM *",
                selected = uiState.produk,
                options = uiState.daftarProduk.map { it.namaProduk },
                onSelect = viewModel::setProduk,
                isError = uiState.errorProduk.isNotBlank(),
                errorMessage = uiState.errorProduk
            )

            // Harga per Liter
            SPBUTextField(
                value = uiState.hargaLiter,
                onValueChange = viewModel::setHargaLiter,
                label = "Harga per Liter (Rp) *",
                isError = uiState.errorHarga.isNotBlank(),
                errorMessage = uiState.errorHarga,
                keyboardType = KeyboardType.Number,
                placeholder = "Contoh: 12300"
            )

            // Volume
            SPBUTextField(
                value = uiState.volume,
                onValueChange = viewModel::setVolume,
                label = "Volume (Liter) *",
                isError = uiState.errorVolume.isNotBlank(),
                errorMessage = uiState.errorVolume,
                keyboardType = KeyboardType.Decimal,
                placeholder = "Contoh: 12.195"
            )

            // Total Harga
            SPBUTextField(
                value = uiState.total,
                onValueChange = viewModel::setTotal,
                label = "Total Harga (Rp) *",
                isError = uiState.errorTotal.isNotBlank(),
                errorMessage = uiState.errorTotal,
                keyboardType = KeyboardType.Number,
                placeholder = "Contoh: 150000"
            )

            // Info perhitungan otomatis
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = "💡 Isi Harga dan Volume → Total otomatis terhitung\n" +
                           "   Isi Harga dan Total → Volume otomatis terhitung",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Dropdown field reusable untuk pilihan terbatas.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = isError,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
    if (isError && errorMessage.isNotBlank()) {
        Text(
            text = errorMessage,
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(start = 16.dp, top = 4.dp)
        )
    }
}
