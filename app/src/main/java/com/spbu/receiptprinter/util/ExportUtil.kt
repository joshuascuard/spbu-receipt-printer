package com.spbu.receiptprinter.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.itextpdf.kernel.colors.ColorConstants
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.TextAlignment
import com.itextpdf.layout.properties.UnitValue
import com.spbu.receiptprinter.data.model.Transaksi
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Utility untuk export data transaksi ke PDF dan Excel.
 * Menggunakan iText 7 untuk PDF dan Apache POI untuk Excel.
 */
object ExportUtil {

    /**
     * Export satu transaksi ke PDF dan return Uri untuk di-share.
     */
    fun exportTransaksiToPdf(
        context: Context,
        transaksi: Transaksi,
        namaSpbu: String,
        nomorSpbu: String,
        alamat: String
    ): Uri? {
        return try {
            val fileName = "Struk_${transaksi.nomorTransaksi}_${FormatUtil.namaFileExport("")}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            val writer = PdfWriter(FileOutputStream(file))
            val pdfDoc = PdfDocument(writer)
            val document = Document(pdfDoc)

            // Header
            document.add(
                Paragraph("PERTAMINA")
                    .setFontSize(18f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(
                Paragraph(nomorSpbu)
                    .setFontSize(12f)
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(
                Paragraph(namaSpbu)
                    .setFontSize(12f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(
                Paragraph(alamat)
                    .setFontSize(10f)
                    .setTextAlignment(TextAlignment.CENTER)
            )

            document.add(Paragraph("─".repeat(50)).setTextAlignment(TextAlignment.CENTER))

            // Data transaksi - tabel 2 kolom
            val table = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                .setWidth(UnitValue.createPercentValue(100f))

            fun addRow(key: String, value: String) {
                table.addCell(Cell().add(Paragraph(key).setFontSize(10f)).setBorder(null))
                table.addCell(Cell().add(Paragraph(": $value").setFontSize(10f)).setBorder(null))
            }

            addRow("Shift", transaksi.shift)
            addRow("No. Transaksi", transaksi.nomorTransaksi)
            addRow("Waktu", "${transaksi.tanggal} ${transaksi.jam}")

            document.add(table)
            document.add(Paragraph("─".repeat(50)).setTextAlignment(TextAlignment.CENTER))

            val table2 = Table(UnitValue.createPercentArray(floatArrayOf(40f, 60f)))
                .setWidth(UnitValue.createPercentValue(100f))

            fun addRow2(key: String, value: String) {
                table2.addCell(Cell().add(Paragraph(key).setFontSize(10f)).setBorder(null))
                table2.addCell(Cell().add(Paragraph(": $value").setFontSize(10f)).setBorder(null))
            }

            addRow2("Nama Produk", transaksi.produk)
            addRow2("Harga/Liter", FormatUtil.formatRupiah(transaksi.hargaLiter))
            addRow2("Volume", "${FormatUtil.formatVolume(transaksi.volume)} L")
            addRow2("Total Harga", FormatUtil.formatRupiah(transaksi.total))
            addRow2("Operator", transaksi.operator)

            document.add(table2)
            document.add(Paragraph("─".repeat(50)).setTextAlignment(TextAlignment.CENTER))

            // Pembayaran
            document.add(
                Paragraph(transaksi.pembayaran)
                    .setFontSize(14f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )
            document.add(
                Paragraph(FormatUtil.formatAngkaBesar(transaksi.total))
                    .setFontSize(20f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )

            document.add(Paragraph("─".repeat(50)).setTextAlignment(TextAlignment.CENTER))

            // Nomor plat
            document.add(
                Paragraph("No. Plat : ${transaksi.nomorPlat}")
                    .setFontSize(10f)
            )

            document.add(Paragraph("─".repeat(50)).setTextAlignment(TextAlignment.CENTER))

            document.add(
                Paragraph("Terima Kasih\nSelamat Jalan")
                    .setFontSize(12f)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
            )

            document.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Export daftar transaksi ke Excel (.xlsx).
     */
    fun exportTransaksiToExcel(
        context: Context,
        transaksiList: List<Transaksi>
    ): Uri? {
        return try {
            val fileName = "Riwayat_Transaksi_${FormatUtil.namaFileExport("")}.xlsx"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)

            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transaksi SPBU")

            // Style header
            val headerStyle = workbook.createCellStyle().apply {
                val font = workbook.createFont().apply {
                    bold = true
                }
                setFont(font)
            }

            // Header row
            val headerRow = sheet.createRow(0)
            val headers = listOf(
                "No", "Nomor Transaksi", "Tanggal", "Jam", "Shift",
                "Operator", "Pembayaran", "No. Plat", "Produk",
                "Harga/Liter", "Volume (L)", "Total (Rp)"
            )
            headers.forEachIndexed { idx, header ->
                val cell = headerRow.createCell(idx)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }

            // Data rows
            transaksiList.forEachIndexed { idx, t ->
                val row = sheet.createRow(idx + 1)
                row.createCell(0).setCellValue((idx + 1).toDouble())
                row.createCell(1).setCellValue(t.nomorTransaksi)
                row.createCell(2).setCellValue(t.tanggal)
                row.createCell(3).setCellValue(t.jam)
                row.createCell(4).setCellValue(t.shift)
                row.createCell(5).setCellValue(t.operator)
                row.createCell(6).setCellValue(t.pembayaran)
                row.createCell(7).setCellValue(t.nomorPlat)
                row.createCell(8).setCellValue(t.produk)
                row.createCell(9).setCellValue(t.hargaLiter)
                row.createCell(10).setCellValue(t.volume)
                row.createCell(11).setCellValue(t.total)
            }

            // Auto-size columns
            headers.indices.forEach { sheet.autoSizeColumn(it) }

            val fos = FileOutputStream(file)
            workbook.write(fos)
            fos.close()
            workbook.close()

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Share file melalui intent Android.
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String, judulShare: String) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, judulShare))
    }
}
