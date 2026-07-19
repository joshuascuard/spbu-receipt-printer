package com.spbu.receiptprinter.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.spbu.receiptprinter.data.database.AppDatabase
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

/**
 * Utility untuk backup dan restore database Room.
 * Backup mengkopi file .db ke external storage.
 * Restore mengkopi kembali file backup ke direktori database.
 */
object BackupUtil {

    private const val BACKUP_FOLDER = "SPBUBackup"

    /**
     * Backup database ke folder Documents di external storage.
     * @return Uri file backup jika berhasil, null jika gagal
     */
    fun backupDatabase(context: Context): Uri? {
        return try {
            // Path database Room
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            if (!dbFile.exists()) return null

            // Folder tujuan backup
            val backupDir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                BACKUP_FOLDER
            )
            if (!backupDir.exists()) backupDir.mkdirs()

            val fileName = "backup_spbu_${FormatUtil.namaFileExport("")}.db"
            val backupFile = File(backupDir, fileName)

            // Copy file database
            FileInputStream(dbFile).use { input ->
                FileOutputStream(backupFile).use { output ->
                    input.copyTo(output)
                }
            }

            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                backupFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Restore database dari file backup.
     * PERHATIAN: Ini akan mengganti semua data yang ada!
     *
     * @param context Context
     * @param backupUri Uri file backup yang dipilih user
     * @return true jika berhasil, false jika gagal
     */
    fun restoreDatabase(context: Context, backupUri: Uri): Boolean {
        return try {
            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)

            // Tutup semua koneksi database sebelum restore
            // (idealnya panggil ini dari luar setelah close database)

            context.contentResolver.openInputStream(backupUri)?.use { input ->
                FileOutputStream(dbFile).use { output ->
                    input.copyTo(output)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Mendapatkan daftar file backup yang tersedia.
     */
    fun getBackupFiles(context: Context): List<File> {
        val backupDir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            BACKUP_FOLDER
        )
        return if (backupDir.exists()) {
            backupDir.listFiles { file -> file.name.endsWith(".db") }
                ?.sortedByDescending { it.lastModified() }
                ?: emptyList()
        } else {
            emptyList()
        }
    }
}
