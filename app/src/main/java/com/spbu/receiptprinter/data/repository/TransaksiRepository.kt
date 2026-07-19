package com.spbu.receiptprinter.data.repository

import com.spbu.receiptprinter.data.dao.TransaksiDao
import com.spbu.receiptprinter.data.model.Transaksi
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository pattern untuk data transaksi.
 * ViewModel berinteraksi dengan Repository, bukan langsung ke DAO.
 */
@Singleton
class TransaksiRepository @Inject constructor(
    private val transaksiDao: TransaksiDao
) {
    /** Stream semua transaksi */
    val semuaTransaksi: Flow<List<Transaksi>> = transaksiDao.getAllFlow()

    suspend fun insert(transaksi: Transaksi): Long = transaksiDao.insert(transaksi)

    suspend fun update(transaksi: Transaksi) = transaksiDao.update(transaksi)

    suspend fun delete(transaksi: Transaksi) = transaksiDao.delete(transaksi)

    suspend fun deleteById(id: Long) = transaksiDao.deleteById(id)

    suspend fun getById(id: Long): Transaksi? = transaksiDao.getById(id)

    fun search(query: String): Flow<List<Transaksi>> = transaksiDao.searchFlow(query)

    fun filterByDate(dari: String, sampai: String): Flow<List<Transaksi>> =
        transaksiDao.filterByDateFlow(dari, sampai)

    fun countHariIni(hari: String): Flow<Int> = transaksiDao.countHariIni(hari)

    fun totalLiterHariIni(hari: String): Flow<Double?> = transaksiDao.totalLiterHariIni(hari)

    fun totalPenjualanHariIni(hari: String): Flow<Double?> = transaksiDao.totalPenjualanHariIni(hari)

    suspend fun getAll(): List<Transaksi> = transaksiDao.getAll()

    suspend fun cekNomorTransaksi(nomor: String): Boolean =
        transaksiDao.cekNomorTransaksi(nomor) > 0
}
