package com.spbu.receiptprinter.ui.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.rememberSnackbarState

@Composable
fun PreviewScreen(
    transaksiId: Long,
    onBack: () -> Unit,
    onBackToForm: () -> Unit,
    onSelesai: () -> Unit,
    viewModel: PreviewViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarState = rememberSnackbarState()

    LaunchedEffect(transaksiId) {
        viewModel.load(transaksiId)
    }

    LaunchedEffect(uiState.pesanError) {
        if (uiState.pesanError.isNotBlank()) {
            snackbarState.showSnackbar(uiState.pesanError)
            viewModel.clearPesanError()
        }
    }

    LaunchedEffect(uiState.pesanInfo) {
        if (uiState.pesanInfo.isNotBlank()) {
            snackbarState.showSnackbar(uiState.pesanInfo)
            viewModel.clearPesanInfo()
        }
    }

    Scaffold(
        topBar = {
            SPBUTopBar(
                title = "Preview Struk",
                onBack = onBack,
                actions = {
                    // Tombol share PDF
                    IconButton(onClick = { viewModel.exportPdf(context) }) {
                        Icon(Icons.Default.Share, contentDescription = "Bagikan PDF")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ============ STATUS PRINTER ============
                PrinterStatusBanner(status = uiState.printerStatus)

                // ============ PREVIEW STRUK ============
                // Simulasi kertas thermal dengan monospace font
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(12.dp)
                    ) {
                        Text(
                            text = uiState.previewText,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = Color.Black,
                            lineHeight = 16.sp
                        )
                    }
                }

                // ============ TOMBOL AKSI ============
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Kembali edit
                    OutlinedButton(
                        onClick = onBackToForm,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
                    }

                    // Tombol Cetak
                    Button(
                        onClick = { viewModel.cetak() },
                        enabled = !uiState.isCetak && uiState.printerStatus is PrinterStatus.Connected,
                        modifier = Modifier.weight(2f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isCetak) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (uiState.isCetak) "Mencetak..." else "Cetak Struk",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Tombol selesai (kembali ke dashboard)
                if (uiState.cetakBerhasil) {
                    Button(
                        onClick = onSelesai,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Selesai - Kembali ke Dashboard")
                    }
                }
            }
        }
    }
}

@Composable
fun PrinterStatusBanner(status: PrinterStatus) {
    val (warna, ikon, teks) = when (status) {
        is PrinterStatus.Connected -> Triple(
            Color(0xFF4CAF50),
            Icons.Default.BluetoothConnected,
            "Printer terhubung: ${status.deviceName}"
        )
        PrinterStatus.Disconnected -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.BluetoothDisabled,
            "Printer tidak terhubung - Buka menu Printer untuk menghubungkan"
        )
        is PrinterStatus.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.Error,
            "Error printer: ${status.message}"
        )
        PrinterStatus.Printing -> Triple(
            Color(0xFF2196F3),
            Icons.Default.Print,
            "Sedang mencetak..."
        )
        PrinterStatus.Connecting -> Triple(
            Color(0xFFFF9800),
            Icons.Default.Bluetooth,
            "Menghubungkan ke printer..."
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = warna,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(ikon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
            Text(teks, color = Color.White, style = MaterialTheme.typography.bodySmall)
        }
    }
}
