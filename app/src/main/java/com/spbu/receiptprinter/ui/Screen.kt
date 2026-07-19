package com.spbu.receiptprinter.ui

/**
 * Definisi semua route navigasi dalam aplikasi.
 */
sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object FormTransaksi : Screen("form_transaksi?transaksiId={transaksiId}") {
        fun createRoute(transaksiId: Long? = null) =
            if (transaksiId != null) "form_transaksi?transaksiId=$transaksiId"
            else "form_transaksi"
    }
    object Preview : Screen("preview/{transaksiId}") {
        fun createRoute(transaksiId: Long) = "preview/$transaksiId"
    }
    object Riwayat : Screen("riwayat")
    object Pengaturan : Screen("pengaturan")
    object PengaturanHargaBbm : Screen("pengaturan_harga_bbm")
    object Printer : Screen("printer")
}
