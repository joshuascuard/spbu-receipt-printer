package com.spbu.receiptprinter.ui.printer

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.spbu.receiptprinter.bluetooth.PrinterStatus
import com.spbu.receiptprinter.ui.common.SPBUTopBar
import com.spbu.receiptprinter.ui.common.rememberSnackbarState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PrinterScreen(
    onBack: () -> Unit,
    viewModel: PrinterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarState = rememberSnackbarState()

    // ============ PERMISSION HANDLING ============
    // Android 12+ butuh BLUETOOTH_CONNECT + BLUETOOTH_SCAN
    // Android < 12 butuh BLUETOOTH + ACCESS_FINE_LOCATION
    val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        listOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN
        )
    } else {
        listOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
    }

    val permissionState = rememberMultiplePermissionsState(permissions) { results ->
        if (results.values.all { it }) {
            viewModel.loadPairedDevices()
        }
    }

    LaunchedEffect(Unit) {
        if (permissionState.allPermissionsGranted) {
            viewModel.loadPairedDevices()
        }
    }

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

    Scaffold(
        topBar = { SPBUTopBar(title = "Printer Bluetooth", onBack = onBack) },
        snackbarHost = { SnackbarHost(snackbarState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ============ PERMISSION WARNING ============
            if (!permissionState.allPermissionsGranted) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Izin Bluetooth Diperlukan",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Aplikasi memerlukan izin Bluetooth untuk mencari dan menghubungkan printer.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { permissionState.launchMultiplePermissionRequest() }
                        ) {
                            Text("Berikan Izin")
                        }
                    }
                }
            }

            // ============ STATUS KONEKSI SAAT INI ============
            StatusKoneksiCard(
                status = uiState.printerStatus,
                namaPrinterDefault = uiState.namaPrinterDefault,
                isLoading = uiState.isLoading,
                onDisconnect = viewModel::disconnect,
                onTestPrint = viewModel::testPrint
            )

            // ============ DAFTAR PERANGKAT PAIRED ============
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Perangkat Tersimpan (Paired)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = {
                        if (permissionState.allPermissionsGranted) {
                            viewModel.loadPairedDevices()
                        } else {
                            permissionState.launchMultiplePermissionRequest()
                        }
                    }
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
            }

            if (uiState.pairedDevices.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.BluetoothSearching,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Tidak ada perangkat Bluetooth yang dipasangkan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Pasangkan printer di Pengaturan Bluetooth sistem Android terlebih dahulu.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.pairedDevices) { device ->
                        DeviceCard(
                            device = device,
                            isDefault = device.address == uiState.printerDefault,
                            isConnected = uiState.printerStatus is PrinterStatus.Connected &&
                                    (uiState.printerStatus as? PrinterStatus.Connected)?.macAddress == device.address,
                            isLoading = uiState.isLoading,
                            onConnect = { viewModel.connect(device) }
                        )
                    }
                }
            }

            // ============ PETUNJUK ============
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        "💡 Petunjuk",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "1. Pastikan printer sudah dinyalakan\n" +
                        "2. Pasangkan (pair) printer di Pengaturan Bluetooth Android\n" +
                        "3. Kembali ke aplikasi dan pilih printer dari daftar\n" +
                        "4. Tekan Hubungkan, lalu Test Print untuk verifikasi",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Card status koneksi printer aktif saat ini.
 */
@Composable
fun StatusKoneksiCard(
    status: PrinterStatus,
    namaPrinterDefault: String,
    isLoading: Boolean,
    onDisconnect: () -> Unit,
    onTestPrint: () -> Unit
) {
    val isConnected = status is PrinterStatus.Connected

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected)
                MaterialTheme.colorScheme.tertiaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (isConnected) Icons.Default.BluetoothConnected
                                  else Icons.Default.BluetoothDisabled,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = if (isConnected) MaterialTheme.colorScheme.tertiary
                           else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (status) {
                            is PrinterStatus.Connected -> "Terhubung"
                            PrinterStatus.Connecting -> "Menghubungkan..."
                            PrinterStatus.Disconnected -> "Tidak Terhubung"
                            is PrinterStatus.Error -> "Error"
                            PrinterStatus.Printing -> "Mencetak..."
                        },
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = when (status) {
                            is PrinterStatus.Connected -> status.deviceName
                            is PrinterStatus.Error -> status.message
                            PrinterStatus.Disconnected -> if (namaPrinterDefault.isNotBlank())
                                "Default: $namaPrinterDefault" else "Belum ada printer default"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isConnected || isLoading) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onTestPrint,
                        enabled = isConnected && !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Print, null, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Test Print")
                    }
                    OutlinedButton(
                        onClick = onDisconnect,
                        enabled = !isLoading,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.BluetoothDisabled, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Putuskan")
                    }
                }
            }
        }
    }
}

/**
 * Card untuk setiap perangkat Bluetooth yang sudah di-pair.
 */
@SuppressLint("MissingPermission")
@Composable
fun DeviceCard(
    device: BluetoothDevice,
    isDefault: Boolean,
    isConnected: Boolean,
    isLoading: Boolean,
    onConnect: () -> Unit
) {
    val nama = try { device.name ?: "Unknown Device" } catch (e: SecurityException) { device.address }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isConnected) MaterialTheme.colorScheme.secondaryContainer
                             else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Print,
                contentDescription = null,
                tint = if (isConnected) MaterialTheme.colorScheme.secondary
                       else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = nama,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (isDefault) {
                        Spacer(modifier = Modifier.width(4.dp))
                        SuggestionChip(
                            onClick = {},
                            label = { Text("Default", style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
                Text(
                    text = device.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isConnected) {
                    Text(
                        text = "● Terhubung",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
            if (!isConnected) {
                Button(
                    onClick = onConnect,
                    enabled = !isLoading,
                    modifier = Modifier.defaultMinSize(minWidth = 90.dp)
                ) {
                    Text("Hubungkan", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
