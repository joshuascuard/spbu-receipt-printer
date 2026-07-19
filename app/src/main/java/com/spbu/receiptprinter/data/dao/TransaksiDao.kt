package com.spbu.receiptprinter.data.dao

import androidx.room.*
import com.spbu.receiptprinter.data.model.Transaksi
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk tabel transaksi.
 * Semua query database transaksi ada di sini.
 */
@Dao
interface TransaksiDao {

    /** Insert transaksi baru, kembalikan id yang digenerate */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaksi: Transaksi): Long

    /** Update transaksi yang sudah ada */
    @Update
    suspend fun update(transaksi: Transaksi)

    /** Hapus transaksi */
    @Delete
    suspend fun delete(transaksi: Transaksi)

    /** Hapus transaksi berdasarkan id */
    @Query("DELETE FROM transaksi WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Ambil semua transaksi, diurutkan terbaru dulu */
    @Query("SELECT * FROM transaksi ORDER BY createdAt DESC")
    fun getAllFlow(): Flow<List<Transaksi>>

    /** Ambil transaksi berdasarkan id */
    @Query("SELECT * FROM transaksi WHERE id = :id")
    suspend fun getById(id: Long): Transaksi?

    /** Cari transaksi berdasarkan nomor transaksi */
    @Query("SELECT * FROM transaksi WHERE nomorTransaksi LIKE '%' || :query || '%' OR nomorPlat LIKE '%' || :query || '%' OR operator LIKE '%' || :query || '%' OR produk LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchFlow(query: String): Flow<List<Transaksi>>

    /** Filter transaksi berdasarkan rentang tanggal */
    @Query("SELECT * FROM transaksi WHERE tanggal BETWEEN :dari AND :sampai ORDER BY createdAt DESC")
    fun filterByDateFlow(dari: String, sampai: String): Flow<List<Transaksi>>

    /** Hitung jumlah transaksi hari ini */
    @Query("SELECT COUNT(*) FROM transaksi WHERE tanggal = :hari")
    fun countHariIni(hari: String): Flow<Int>

    /** Total liter hari ini */
    @Query("SELECT SUM(volume) FROM transaksi WHERE tanggal = :hari")
    fun totalLiterHariIni(hari: String): Flow<Double?>

    /** Total penjualan hari ini */
    @Query("SELECT SUM(total) FROM transaksi WHERE tanggal = :hari")
    fun totalPenjualanHariIni(hari: String): Flow<Double?>

    /** Ambil semua transaksi untuk export (tanpa Flow, sekali ambil) */
    @Query("SELECT * FROM transaksi ORDER BY createdAt DESC")
    suspend fun getAll(): List<Transaksi>

    /** Cek apakah nomor transaksi sudah digunakan */
    @Query("SELECT COUNT(*) FROM transaksi WHERE nomorTransaksi = :nomor")
    suspend fun cekNomorTransaksi(nomor: String): Int
}
