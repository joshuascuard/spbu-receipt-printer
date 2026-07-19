package com.spbu.receiptprinter.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.spbu.receiptprinter.ui.dashboard.DashboardScreen
import com.spbu.receiptprinter.ui.history.RiwayatScreen
import com.spbu.receiptprinter.ui.preview.PreviewScreen
import com.spbu.receiptprinter.ui.printer.PrinterScreen
import com.spbu.receiptprinter.ui.settings.PengaturanHargaBbmScreen
import com.spbu.receiptprinter.ui.settings.PengaturanScreen
import com.spbu.receiptprinter.ui.transaction.FormTransaksiScreen

/**
 * Navigation graph utama aplikasi.
 */
@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        // Dashboard
        composable(Screen.Dashboard.route) {
            DashboardScreen(
                onBuatTransaksi = { navController.navigate(Screen.FormTransaksi.createRoute()) },
                onRiwayat = { navController.navigate(Screen.Riwayat.route) },
                onPengaturan = { navController.navigate(Screen.Pengaturan.route) },
                onPrinter = { navController.navigate(Screen.Printer.route) }
            )
        }

        // Form Transaksi (buat baru atau edit)
        composable(
            route = Screen.FormTransaksi.route,
            arguments = listOf(
                navArgument("transaksiId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val transaksiId = backStackEntry.arguments?.getLong("transaksiId") ?: -1L
            FormTransaksiScreen(
                transaksiId = if (transaksiId == -1L) null else transaksiId,
                onBack = { navController.popBackStack() },
                onPreview = { id -> navController.navigate(Screen.Preview.createRoute(id)) }
            )
        }

        // Preview struk sebelum cetak
        composable(
            route = Screen.Preview.route,
            arguments = listOf(
                navArgument("transaksiId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val transaksiId = backStackEntry.arguments?.getLong("transaksiId") ?: return@composable
            PreviewScreen(
                transaksiId = transaksiId,
                onBack = { navController.popBackStack() },
                onBackToForm = {
                    navController.popBackStack()
                    navController.popBackStack()
                },
                onSelesai = {
                    // Kembali ke dashboard setelah cetak
                    navController.popBackStack(Screen.Dashboard.route, inclusive = false)
                }
            )
        }

        // Riwayat transaksi
        composable(Screen.Riwayat.route) {
            RiwayatScreen(
                onBack = { navController.popBackStack() },
                onEditTransaksi = { id ->
                    navController.navigate(Screen.FormTransaksi.createRoute(id))
                },
                onPreview = { id ->
                    navController.navigate(Screen.Preview.createRoute(id))
                }
            )
        }

        // Pengaturan
        composable(Screen.Pengaturan.route) {
            PengaturanScreen(
                onBack = { navController.popBackStack() },
                onHargaBbm = { navController.navigate(Screen.PengaturanHargaBbm.route) },
                onPrinter = { navController.navigate(Screen.Printer.route) }
            )
        }

        // Pengaturan Harga BBM
        composable(Screen.PengaturanHargaBbm.route) {
            PengaturanHargaBbmScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Pengaturan Printer
        composable(Screen.Printer.route) {
            PrinterScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
