package com.spbu.receiptprinter.bluetooth

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import com.spbu.receiptprinter.data.model.Setting
import com.spbu.receiptprinter.data.model.Transaksi
import com.spbu.receiptprinter.util.FormatUtil
import java.io.ByteArrayOutputStream
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Formatter untuk menghasilkan teks ESC/POS sesuai format struk SPBU.
 *
 * Format tag yang didukung library DantSu:
 * [L] = Left align
 * [C] = Center align
 * [R] = Right align
 * <b>text</b> = Bold
 * <u>text</u> = Underline
 * <font size='big'>text</font> = Double width
 * <font size='tall'>text</font> = Double height
 * <font size='big-tall'>text</font> = Double width & height
 * <img>base64</img> = Image
 * <qrcode size='20'>text</qrcode> = QR Code
 * <barcode type='128' height='10'>text</barcode> = Barcode
 */
@Singleton
class EscPosFormatter @Inject constructor() {

    /**
     * Menghasilkan teks ESC/POS lengkap untuk dicetak.
     *
     * @param transaksi Data transaksi
     * @param setting Pengaturan SPBU
     * @param lebarKertas Lebar kertas 58 atau 80mm
     */
    private fun logoKeBase64(context: Context, logoPath: String): String? {
    return try {
        // Coba dari assets dulu
        val stream = try {
            context.assets.open("Logo-Pertamina.png")
        } catch (e: Exception) {
            // Fallback ke URI dari storage
            if (logoPath.isNotBlank()) {
                val uri = Uri.parse(logoPath)
                context.contentResolver.openInputStream(uri)
            } else null
        } ?: return null

        val bitmap = BitmapFactory.decodeStream(stream)
        val scaled = Bitmap.createScaledBitmap(bitmap, 384, 100, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.PNG, 100, out)
        Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    } catch (e: Exception) {
        null
    }
    }

    fun formatStruk(
    transaksi: Transaksi,
    setting: Setting,
    lebarKertas: Int = 58,
    context: Context? = null
): String {
        // Karakter separator sesuai lebar kertas
        val separator = if (lebarKertas == 80) "-".repeat(48) else "-".repeat(32)

        return buildString {
            // ============ HEADER ============
            // Logo / Nama perusahaan besar di tengah
            if (context != null) {
    val base64 = logoKeBase64(context, setting.logoPath)
    if (base64 != null) {
        append("[C]<img>$base64</img>\n")
    } else {
        append("[C]<b>PERTAMINA</b>\n")
    }
} else {
    append("[C]<b>PERTAMINA</b>\n")
}
append("\n")

            // Nomor SPBU
            if (setting.nomorSpbu.isNotBlank()) {
                append("[C]<b>${setting.nomorSpbu}</b>\n")
            }

            // Nama SPBU
            append("[C]<b>${setting.namaSpbu}</b>\n")

            // Alamat SPBU
            if (setting.alamat.isNotBlank()) {
                setting.alamat.split("\n").forEach { baris ->
                    append("[C]$baris\n")
                }
            }

            append("[C]$separator\n")

            // ============ DATA TRANSAKSI ============
            append("[L]Shift      : ${transaksi.shift}\n")
            append("[L]No. Trans  : ${transaksi.nomorTransaksi}\n")
            append("[L]Waktu      : ${transaksi.tanggal} ${transaksi.jam}\n")

            append("[C]$separator\n")

            // ============ DATA BBM ============
            append("[L]Nama Produk : ${transaksi.produk}\n")
            append("[L]Harga/Liter : ${FormatUtil.formatRupiah(transaksi.hargaLiter)}\n")
            append("[L]Volume      : ${FormatUtil.formatVolume(transaksi.volume)} L\n")
            append("[L]Total Harga : ${FormatUtil.formatRupiah(transaksi.total)}\n")
            append("[L]Operator    : ${transaksi.operator}\n")

            append("[C]$separator\n")

            // ============ PEMBAYARAN ============
            append("[C]<b>${transaksi.pembayaran}</b>\n")
            append("\n")
            // Total besar di tengah
            append("[C]<b><font size='big-tall'>${FormatUtil.formatAngkaBesar(transaksi.total)}</font></b>\n")

            append("[C]$separator\n")

            // ============ NOMOR PLAT ============
            append("[L]No. Plat : ${transaksi.nomorPlat}\n")

            append("[C]$separator\n")

            // ============ FOOTER ============
            setting.footer.split("\n").forEach { baris ->
                if (baris.isNotBlank()) {
                    append("[C]<b>$baris</b>\n")
                }
            }

            // Feed paper dan cut (auto-cut jika didukung printer)
            append("\n\n\n")
        }
    }

    /**
     * Format struk untuk preview (teks biasa tanpa tag ESC/POS).
     * Digunakan untuk menampilkan preview di layar sebelum cetak.
     */
    fun formatPreview(
        transaksi: Transaksi,
        setting: Setting,
        lebarKertas: Int = 58
    ): String {
        val lebar = if (lebarKertas == 80) 48 else 32
        val separator = "-".repeat(lebar)

        fun center(text: String): String {
            val spasi = ((lebar - text.length) / 2).coerceAtLeast(0)
            return " ".repeat(spasi) + text
        }

        fun left(key: String, value: String): String {
            return "$key: $value"
        }

        return buildString {
            appendLine(center("PERTAMINA"))
            appendLine()
            if (setting.nomorSpbu.isNotBlank()) appendLine(center(setting.nomorSpbu))
            appendLine(center(setting.namaSpbu))
            if (setting.alamat.isNotBlank()) {
                setting.alamat.split("\n").forEach { appendLine(center(it)) }
            }
            appendLine(separator)

            appendLine(left("Shift     ", transaksi.shift))
            appendLine(left("No. Trans ", transaksi.nomorTransaksi))
            appendLine(left("Waktu     ", "${transaksi.tanggal} ${transaksi.jam}"))

            appendLine(separator)

            appendLine(left("Nama Produk", transaksi.produk))
            appendLine(left("Harga/Liter", FormatUtil.formatRupiah(transaksi.hargaLiter)))
            appendLine(left("Volume     ", "${FormatUtil.formatVolume(transaksi.volume)} L"))
            appendLine(left("Total Harga", FormatUtil.formatRupiah(transaksi.total)))
            appendLine(left("Operator   ", transaksi.operator))

            appendLine(separator)

            appendLine(center(transaksi.pembayaran))
            appendLine(center(FormatUtil.formatAngkaBesar(transaksi.total)))

            appendLine(separator)

            appendLine(left("No. Plat", transaksi.nomorPlat))

            appendLine(separator)

            setting.footer.split("\n").forEach { baris ->
                if (baris.isNotBlank()) appendLine(center(baris))
            }
        }
    }
}
