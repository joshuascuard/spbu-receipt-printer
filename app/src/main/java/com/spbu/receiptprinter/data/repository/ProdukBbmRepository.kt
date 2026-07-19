package com.spbu.receiptprinter.data.repository

import com.spbu.receiptprinter.data.dao.ProdukBbmDao
import com.spbu.receiptprinter.data.model.ProdukBbm
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository untuk produk BBM.
 */
@Singleton
class ProdukBbmRepository @Inject constructor(
    private val produkBbmDao: ProdukBbmDao
) {
    val semuaProdukAktif: Flow<List<ProdukBbm>> = produkBbmDao.getAllAktifFlow()
    val semuaProduk: Flow<List<ProdukBbm>> = produkBbmDao.getAllFlow()

    suspend fun insert(produk: ProdukBbm): Long = produkBbmDao.insert(produk)

    suspend fun update(produk: ProdukBbm) = produkBbmDao.update(produk)

    suspend fun delete(produk: ProdukBbm) = produkBbmDao.delete(produk)

    suspend fun deleteById(id: Long) = produkBbmDao.deleteById(id)

    suspend fun getByNama(nama: String): ProdukBbm? = produkBbmDao.getByNama(nama)

    suspend fun getAll(): List<ProdukBbm> = produkBbmDao.getAll()
}
