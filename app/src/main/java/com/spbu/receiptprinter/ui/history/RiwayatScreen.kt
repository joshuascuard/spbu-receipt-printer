package com.spbu.receiptprinter.ui.history

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.data.model.Transaksi
import com.spbu.receiptprinter.ui.common.ConfirmDeleteDialog
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.rememberSnackbarState
import com.spbu.receiptprinter.util.FormatUtil
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RiwayatScreen(
    onBack: () -> Unit,
    onEditTransaksi: (Long) -> Unit,
    onPreview: (Long) -> Unit,
    viewModel: RiwayatViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = rememberSnackbarState()

    LaunchedEffect(uiState.pesanInfo) {
        if (uiState.pesanInfo.isNotBlank()) {
            snackbarState.showSnackbar(uiState.pesanInfo)
            viewModel.clearPesanInfo()
        }
    }

    LaunchedEffect(uiState.pesanError) {
        if (uiState.pesanError.isNotBlank()) {
            snackbarState.showSnackbar(uiState.pesanError)
            viewModel.clearPesanError()
        }
    }

    // Dialog hapus konfirmasi
    if (uiState.showHapusDialog && uiState.transaksiDipilih != null) {
        ConfirmDeleteDialog(
            pesan = "Hapus transaksi No. ${uiState.transaksiDipilih?.nomorTransaksi}?",
            onConfirm = viewModel::konfirmasiHapus,
            onDismiss = viewModel::batalHapus
        )
    }

    Scaffold(
        topBar = {
            SPBUTopBar(
                title = "Riwayat Transaksi",
                onBack = onBack,
                actions = {
                    IconButton(onClick = { viewModel.exportExcel(context) }) {
                        Icon(Icons.Default.GridOn, contentDescription = "Export Excel")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // ============ SEARCH BAR ============
            OutlinedTextField(
                value = uiState.queryPencarian,
                onValueChange = viewModel::setPencarian,
                placeholder = { Text("Cari nomor transaksi, plat, operator...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (uiState.queryPencarian.isNotBlank()) {
                        IconButton(onClick = { viewModel.setPencarian("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Hapus pencarian")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine = true
            )

            // ============ FILTER TANGGAL ============
            FilterTanggalRow(
                dari = uiState.filterDariTanggal,
                sampai = uiState.filterSampaiTanggal,
                onSetFilter = viewModel::setFilter,
                onReset = viewModel::resetFilter
            )

            // Jumlah hasil
            Text(
                text = "${uiState.daftarTransaksi.size} transaksi",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )

            // ============ LIST TRANSAKSI ============
            if (uiState.daftarTransaksi.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Belum ada transaksi",
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = uiState.daftarTransaksi,
                        key = { it.id }
                    ) { transaksi ->
                        TransaksiCard(
                            transaksi = transaksi,
                            onEdit = { onEditTransaksi(transaksi.id) },
                            onHapus = { viewModel.pilihHapus(transaksi) },
                            onCetak = { viewModel.cetakUlang(transaksi) },
                            onPreview = { onPreview(transaksi.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun FilterTanggalRow(
    dari: String,
    sampai: String,
    onSetFilter: (String, String) -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    val kal = Calendar.getInstance()

    var tempDari by remember { mutableStateOf(dari) }
    var tempSampai by remember { mutableStateOf(sampai) }

    val pickerDari = DatePickerDialog(context, { _, y, m, d ->
        tempDari = "${d.toString().padStart(2, '0')}/${(m + 1).toString().padStart(2, '0')}/$y"
        if (tempSampai.isNotBlank()) onSetFilter(tempDari, tempSampai)
    }, kal.get(Calendar.YEAR), kal.get(Calendar.MONTH), kal.get(Calendar.DAY_OF_MONTH))

    val pickerSampai = DatePickerDialog(context, { _, y, m, d ->
        tempSampai = "${d.toString().padStart(2, '0')}/${(m + 1).toString().padStart(2, '0')}/$y"
        if (tempDari.isNotBlank()) onSetFilter(tempDari, tempSampai)
    }, kal.get(Calendar.YEAR), kal.get(Calendar.MONTH), kal.get(Calendar.DAY_OF_MONTH))

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { pickerDari.show() },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                if (dari.isBlank()) "Dari Tanggal" else dari,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text("—")
        OutlinedButton(
            onClick = { pickerSampai.show() },
            modifier = Modifier.weight(1f)
        ) {
            Text(
                if (sampai.isBlank()) "Sampai Tanggal" else sampai,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (dari.isNotBlank() || sampai.isNotBlank()) {
            IconButton(onClick = {
                tempDari = ""
                tempSampai = ""
                onReset()
            }) {
                Icon(Icons.Default.FilterAltOff, contentDescription = "Reset filter")
            }
        }
    }
}

@Composable
fun TransaksiCard(
    transaksi: Transaksi,
    onEdit: () -> Unit,
    onHapus: () -> Unit,
    onCetak: () -> Unit,
    onPreview: () -> Unit
) {
    var expandedMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = transaksi.nomorTransaksi,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${transaksi.tanggal} ${transaksi.jam}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Chip pembayaran
                SuggestionChip(
                    onClick = {},
                    label = { Text(transaksi.pembayaran, style = MaterialTheme.typography.labelSmall) }
                )

                // Menu aksi
                Box {
                    IconButton(onClick = { expandedMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                    DropdownMenu(
                        expanded = expandedMenu,
                        onDismissRequest = { expandedMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Preview") },
                            leadingIcon = { Icon(Icons.Default.Visibility, null) },
                            onClick = { expandedMenu = false; onPreview() }
                        )
                        DropdownMenuItem(
                            text = { Text("Cetak Ulang") },
                            leadingIcon = { Icon(Icons.Default.Print, null) },
                            onClick = { expandedMenu = false; onCetak() }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { expandedMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Hapus", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
                            onClick = { expandedMenu = false; onHapus() }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = transaksi.produk,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "${FormatUtil.formatVolume(transaksi.volume)} L  •  Plat: ${transaksi.nomorPlat}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Shift ${transaksi.shift}  •  ${transaksi.operator}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = FormatUtil.formatRupiah(transaksi.total),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
