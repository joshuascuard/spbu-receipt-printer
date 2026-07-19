package com.spbu.receiptprinter.ui.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.spbu.receiptprinter.R
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.StatCard
import com.spbu.receiptprinter.util.FormatUtil

@Composable
fun DashboardScreen(
    onBuatTransaksi: () -> Unit,
    onRiwayat: () -> Unit,
    onPengaturan: () -> Unit,
    onPrinter: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto-reconnect printer saat masuk dashboard
    LaunchedEffect(Unit) {
        viewModel.tryAutoReconnect()
    }

    Scaffold(
        topBar = {
            SPBUTopBar(
                title = if (uiState.namaSpbu.isNotEmpty()) uiState.namaSpbu
                        else stringResource(R.string.app_name)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ============ STATUS PRINTER ============
            PrinterStatusCard(
                status = uiState.printerStatus,
                onKonfigurasi = onPrinter
            )

            // ============ STATISTIK HARI INI ============
            Text(
                text = "Statistik Hari Ini",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatCard(
                    title = "Transaksi",
                    value = uiState.jumlahTransaksi.toString(),
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.Receipt, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                )
                StatCard(
                    title = "Total Liter",
                    value = "${FormatUtil.formatVolume(uiState.totalLiter)} L",
                    modifier = Modifier.weight(1f),
                    icon = { Icon(Icons.Default.LocalGasStation, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
                )
            }

            StatCard(
                title = "Total Penjualan Hari Ini",
                value = FormatUtil.formatRupiah(uiState.totalPenjualan),
                modifier = Modifier.fillMaxWidth(),
                icon = { Icon(Icons.Default.Payments, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer) }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ============ MENU UTAMA ============
            Text(
                text = "Menu",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Tombol Buat Transaksi - paling menonjol
            Button(
                onClick = onBuatTransaksi,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Buat Transaksi",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // Grid menu 2x2
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuCard(
                    title = "Riwayat",
                    icon = Icons.Default.History,
                    onClick = onRiwayat,
                    modifier = Modifier.weight(1f)
                )
                MenuCard(
                    title = "Printer",
                    icon = Icons.Default.Print,
                    onClick = onPrinter,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MenuCard(
                    title = "Pengaturan",
                    icon = Icons.Default.Settings,
                    onClick = onPengaturan,
                    modifier = Modifier.weight(1f)
                )
                // Placeholder agar simetris
                Spacer(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Card status printer Bluetooth di dashboard.
 */
@Composable
fun PrinterStatusCard(
    status: PrinterStatus,
    onKonfigurasi: () -> Unit
) {
    val (warna, ikon, teks) = when (status) {
        is PrinterStatus.Connected -> Triple(
            MaterialTheme.colorScheme.tertiaryContainer,
            Icons.Default.BluetoothConnected,
            "Terhubung: ${status.deviceName}"
        )
        is PrinterStatus.Connecting -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            Icons.Default.Bluetooth,
            "Menghubungkan..."
        )
        is PrinterStatus.Error -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            Icons.Default.BluetoothDisabled,
            "Error: ${status.message}"
        )
        PrinterStatus.Printing -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            Icons.Default.Print,
            "Sedang mencetak..."
        )
        PrinterStatus.Disconnected -> Triple(
            MaterialTheme.colorScheme.surfaceVariant,
            Icons.Default.BluetoothDisabled,
            "Printer tidak terhubung"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = warna)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(imageVector = ikon, contentDescription = null)
                Text(
                    text = teks,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1
                )
            }
            TextButton(onClick = onKonfigurasi) {
                Text("Atur")
            }
        }
    }
}

/**
 * Card menu navigasi.
 */
@Composable
fun MenuCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                modifier = Modifier.size(32.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
