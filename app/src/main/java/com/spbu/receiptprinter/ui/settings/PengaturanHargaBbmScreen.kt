package com.spbu.receiptprinter.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.data.model.ProdukBbm
import com.spbu.receiptprinter.ui.common.ConfirmDeleteDialog
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.rememberSnackbarState
import com.spbu.receiptprinter.util.FormatUtil

@Composable
fun PengaturanHargaBbmScreen(
    onBack: () -> Unit,
    viewModel: PengaturanHargaBbmViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberSnackbarState()
    var produkHapus by remember { mutableStateOf<ProdukBbm?>(null) }

    LaunchedEffect(uiState.pesanInfo) {
        if (uiState.pesanInfo.isNotBlank()) {
            snackbarState.showSnackbar(uiState.pesanInfo)
            viewModel.clearPesanInfo()
        }
    }

    // Dialog konfirmasi hapus
    produkHapus?.let { p ->
        ConfirmDeleteDialog(
            pesan = "Hapus produk ${p.namaProduk}?",
            onConfirm = { viewModel.hapus(p); produkHapus = null },
            onDismiss = { produkHapus = null }
        )
    }

    // Dialog tambah/edit
    if (uiState.showDialog) {
        DialogTambahEditProduk(
            isEdit = uiState.editProduk != null,
            nama = uiState.inputNama,
            harga = uiState.inputHarga,
            errorNama = uiState.errorNama,
            errorHarga = uiState.errorHarga,
            onNama = viewModel::setInputNama,
            onHarga = viewModel::setInputHarga,
            onSimpan = viewModel::simpan,
            onDismiss = viewModel::tutupDialog
        )
    }

    Scaffold(
        topBar = { SPBUTopBar(title = "Harga BBM", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = viewModel::bukaTambah,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Tambah BBM") }
            )
        }
    ) { padding ->
        if (uiState.daftarProduk.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada produk BBM", color = MaterialTheme.colorScheme.outline)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(
                    start = 16.dp, end = 16.dp,
                    top = padding.calculateTopPadding() + 8.dp,
                    bottom = padding.calculateBottomPadding() + 80.dp
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.daftarProduk, key = { it.id }) { produk ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = produk.namaProduk,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = FormatUtil.formatRupiah(produk.harga) + " / Liter",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Row {
                                IconButton(onClick = { viewModel.bukaEdit(produk) }) {
                                    Icon(Icons.Default.Edit, "Edit", tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { produkHapus = produk }) {
                                    Icon(Icons.Default.Delete, "Hapus", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DialogTambahEditProduk(
    isEdit: Boolean,
    nama: String,
    harga: String,
    errorNama: String,
    errorHarga: String,
    onNama: (String) -> Unit,
    onHarga: (String) -> Unit,
    onSimpan: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (isEdit) "Edit Produk BBM" else "Tambah Produk BBM",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nama,
                    onValueChange = onNama,
                    label = { Text("Nama Produk") },
                    isError = errorNama.isNotBlank(),
                    supportingText = if (errorNama.isNotBlank()) ({ Text(errorNama) }) else null,
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = harga,
                    onValueChange = onHarga,
                    label = { Text("Harga per Liter (Rp)") },
                    isError = errorHarga.isNotBlank(),
                    supportingText = if (errorHarga.isNotBlank()) ({ Text(errorHarga) }) else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = onSimpan) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
