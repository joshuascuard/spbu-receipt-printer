package com.spbu.receiptprinter.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.data.model.ProdukBbm
import com.spbu.receiptprinter.data.model.Transaksi
import com.spbu.receiptprinter.data.repository.ProdukBbmRepository
import com.spbu.receiptprinter.data.repository.TransaksiRepository
import com.spbu.receiptprinter.util.FormatUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FormTransaksiUiState(
    // Data transaksi
    val nomorTransaksi: String = "",
    val tanggal: String = FormatUtil.tanggalHariIni(),
    val jam: String = FormatUtil.jamSekarang(),
    val shift: String = "1",
    val operator: String = "",
    val pembayaran: String = "CASH",
    val nomorPlat: String = "",

    // Data BBM
    val produk: String = "",
    val hargaLiter: String = "",
    val volume: String = "",
    val total: String = "",

    // State
    val daftarProduk: List<ProdukBbm> = emptyList(),
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val editId: Long? = null,
    val errorPesan: String = "",
    val isSimpanBerhasil: Boolean = false,
    val savedTransaksiId: Long = -1L,

    // Validasi
    val errorNomor: String = "",
    val errorTanggal: String = "",
    val errorOperator: String = "",
    val errorPlat: String = "",
    val errorProduk: String = "",
    val errorHarga: String = "",
    val errorVolume: String = "",
    val errorTotal: String = ""
)

/** Trigger kalkulasi otomatis */
enum class TriggerHitung { NONE, HITUNG_TOTAL, HITUNG_VOLUME }

@HiltViewModel
class FormTransaksiViewModel @Inject constructor(
    private val transaksiRepository: TransaksiRepository,
    private val produkBbmRepository: ProdukBbmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FormTransaksiUiState())
    val uiState: StateFlow<FormTransaksiUiState> = _uiState.asStateFlow()

    private var triggerHitung = TriggerHitung.NONE

    init {
        // Load daftar produk BBM
        viewModelScope.launch {
            produkBbmRepository.semuaProdukAktif.collect { produkList ->
                _uiState.update { it.copy(daftarProduk = produkList) }
            }
        }
    }

    /** Load data transaksi untuk mode edit */
    fun loadTransaksi(id: Long) {
        viewModelScope.launch {
            val t = transaksiRepository.getById(id) ?: return@launch
            _uiState.update { state ->
                state.copy(
                    isEditMode = true,
                    editId = id,
                    nomorTransaksi = t.nomorTransaksi,
                    tanggal = t.tanggal,
                    jam = t.jam,
                    shift = t.shift,
                    operator = t.operator,
                    pembayaran = t.pembayaran,
                    nomorPlat = t.nomorPlat,
                    produk = t.produk,
                    hargaLiter = t.hargaLiter.toLong().toString(),
                    volume = FormatUtil.formatVolume(t.volume),
                    total = t.total.toLong().toString()
                )
            }
        }
    }

    fun setNomorTransaksi(v: String) = _uiState.update { it.copy(nomorTransaksi = v, errorNomor = "") }
    fun setTanggal(v: String) = _uiState.update { it.copy(tanggal = v, errorTanggal = "") }
    fun setJam(v: String) = _uiState.update { it.copy(jam = v) }
    fun setShift(v: String) = _uiState.update { it.copy(shift = v) }
    fun setOperator(v: String) = _uiState.update { it.copy(operator = v, errorOperator = "") }
    fun setPembayaran(v: String) = _uiState.update { it.copy(pembayaran = v) }
    fun setNomorPlat(v: String) = _uiState.update { it.copy(nomorPlat = v, errorPlat = "") }

    /** Pilih produk BBM - harga otomatis diisi dari database */
    fun setProduk(namaProduk: String) {
        viewModelScope.launch {
            val produk = produkBbmRepository.getByNama(namaProduk)
            val harga = produk?.harga?.toLong()?.toString() ?: ""
            _uiState.update { state ->
                state.copy(
                    produk = namaProduk,
                    hargaLiter = harga,
                    errorProduk = ""
                )
            }
            hitungUlang(TriggerHitung.HITUNG_TOTAL)
        }
    }

    /** Set harga/liter - hitung total otomatis */
    fun setHargaLiter(v: String) {
        _uiState.update { it.copy(hargaLiter = v, errorHarga = "") }
        hitungUlang(TriggerHitung.HITUNG_TOTAL)
    }

    /** Set volume - hitung total otomatis */
    fun setVolume(v: String) {
        _uiState.update { it.copy(volume = v, errorVolume = "") }
        hitungUlang(TriggerHitung.HITUNG_TOTAL)
    }

    /** Set total - hitung volume otomatis */
    fun setTotal(v: String) {
        _uiState.update { it.copy(total = v, errorTotal = "") }
        hitungUlang(TriggerHitung.HITUNG_VOLUME)
    }

    /** Isi waktu sekarang */
    fun gunakanWaktuSekarang() {
        _uiState.update { state ->
            state.copy(
                tanggal = FormatUtil.tanggalHariIni(),
                jam = FormatUtil.jamSekarang()
            )
        }
    }

    /** Hitung otomatis total atau volume */
    private fun hitungUlang(trigger: TriggerHitung) {
        val state = _uiState.value
        val harga = FormatUtil.parseDouble(state.hargaLiter.replace(".", ""))
        val volume = FormatUtil.parseDouble(state.volume.replace(",", "."))
        val total = FormatUtil.parseDouble(state.total.replace(".", ""))

        when (trigger) {
            TriggerHitung.HITUNG_TOTAL -> {
                if (harga > 0 && volume > 0) {
                    val totalHitung = FormatUtil.hitungTotal(harga, volume)
                    _uiState.update { it.copy(total = totalHitung.toLong().toString()) }
                }
            }
            TriggerHitung.HITUNG_VOLUME -> {
                if (harga > 0 && total > 0) {
                    val volumeHitung = FormatUtil.hitungVolume(harga, total)
                    _uiState.update { it.copy(volume = FormatUtil.formatVolume(volumeHitung)) }
                }
            }
            TriggerHitung.NONE -> {}
        }
    }

    /** Validasi semua field sebelum simpan */
    private fun validasi(): Boolean {
        val s = _uiState.value
        var valid = true
        var errorNomor = ""
        var errorOperator = ""
        var errorPlat = ""
        var errorProduk = ""
        var errorHarga = ""
        var errorVolume = ""
        var errorTotal = ""

        if (s.nomorTransaksi.isBlank()) { errorNomor = "Nomor transaksi wajib diisi"; valid = false }
        if (s.operator.isBlank()) { errorOperator = "Nama operator wajib diisi"; valid = false }
        if (s.nomorPlat.isBlank()) { errorPlat = "Nomor plat wajib diisi"; valid = false }
        if (s.produk.isBlank()) { errorProduk = "Pilih jenis BBM"; valid = false }
        if (s.hargaLiter.isBlank() || FormatUtil.parseDouble(s.hargaLiter.replace(".", "")) <= 0) {
            errorHarga = "Harga harus lebih dari 0"; valid = false
        }
        if (s.volume.isBlank() || FormatUtil.parseDouble(s.volume.replace(",", ".")) <= 0) {
            errorVolume = "Volume harus lebih dari 0"; valid = false
        }
        if (s.total.isBlank() || FormatUtil.parseDouble(s.total.replace(".", "")) <= 0) {
            errorTotal = "Total harga harus lebih dari 0"; valid = false
        }

        _uiState.update { it.copy(
            errorNomor = errorNomor,
            errorOperator = errorOperator,
            errorPlat = errorPlat,
            errorProduk = errorProduk,
            errorHarga = errorHarga,
            errorVolume = errorVolume,
            errorTotal = errorTotal
        )}
        return valid
    }

    /** Simpan transaksi ke database */
    fun simpan() {
        if (!validasi()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val s = _uiState.value

            try {
                val transaksi = Transaksi(
                    id = s.editId ?: 0L,
                    nomorTransaksi = s.nomorTransaksi.trim(),
                    tanggal = s.tanggal,
                    jam = s.jam,
                    shift = s.shift,
                    operator = s.operator.trim(),
                    pembayaran = s.pembayaran,
                    nomorPlat = s.nomorPlat.trim().uppercase(),
                    produk = s.produk,
                    hargaLiter = FormatUtil.parseDouble(s.hargaLiter.replace(".", "")),
                    volume = FormatUtil.parseDouble(s.volume.replace(",", ".")),
                    total = FormatUtil.parseDouble(s.total.replace(".", ""))
                )

                val savedId = if (s.isEditMode && s.editId != null) {
                    transaksiRepository.update(transaksi)
                    s.editId
                } else {
                    transaksiRepository.insert(transaksi)
                }

                _uiState.update { it.copy(
                    isLoading = false,
                    isSimpanBerhasil = true,
                    savedTransaksiId = savedId
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    errorPesan = "Gagal menyimpan: ${e.message}"
                )}
            }
        }
    }

    fun resetError() = _uiState.update { it.copy(errorPesan = "") }
    fun resetSimpanBerhasil() = _uiState.update { it.copy(isSimpanBerhasil = false) }
}
