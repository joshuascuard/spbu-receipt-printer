package com.spbu.receiptprinter.data.dao

import androidx.room.*
import com.spbu.receiptprinter.data.model.ProdukBbm
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object untuk tabel produk_bbm.
 */
@Dao
interface ProdukBbmDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(produk: ProdukBbm): Long

    @Update
    suspend fun update(produk: ProdukBbm)

    @Delete
    suspend fun delete(produk: ProdukBbm)

    @Query("DELETE FROM produk_bbm WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Ambil semua produk aktif, diurutkan berdasarkan urutan */
    @Query("SELECT * FROM produk_bbm WHERE aktif = 1 ORDER BY urutan ASC, namaProduk ASC")
    fun getAllAktifFlow(): Flow<List<ProdukBbm>>

    /** Ambil semua produk (termasuk tidak aktif) */
    @Query("SELECT * FROM produk_bbm ORDER BY urutan ASC, namaProduk ASC")
    fun getAllFlow(): Flow<List<ProdukBbm>>

    @Query("SELECT * FROM produk_bbm ORDER BY urutan ASC, namaProduk ASC")
    suspend fun getAll(): List<ProdukBbm>

    /** Ambil produk berdasarkan nama */
    @Query("SELECT * FROM produk_bbm WHERE namaProduk = :nama LIMIT 1")
    suspend fun getByNama(nama: String): ProdukBbm?

    /** Cek apakah sudah ada data produk (untuk inisialisasi awal) */
    @Query("SELECT COUNT(*) FROM produk_bbm")
    suspend fun count(): Int
}
