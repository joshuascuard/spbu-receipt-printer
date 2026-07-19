package com.spbu.receiptprinter.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.spbu.receiptprinter.data.model.ProdukBbm
import com.spbu.receiptprinter.data.repository.ProdukBbmRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HargaBbmUiState(
    val daftarProduk: List<ProdukBbm> = emptyList(),
    val showDialog: Boolean = false,
    val editProduk: ProdukBbm? = null,
    val inputNama: String = "",
    val inputHarga: String = "",
    val pesanInfo: String = "",
    val pesanError: String = "",
    val errorNama: String = "",
    val errorHarga: String = ""
)

@HiltViewModel
class PengaturanHargaBbmViewModel @Inject constructor(
    private val produkBbmRepository: ProdukBbmRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HargaBbmUiState())
    val uiState: StateFlow<HargaBbmUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            produkBbmRepository.semuaProduk.collect { list ->
                _uiState.update { it.copy(daftarProduk = list) }
            }
        }
    }

    fun bukaTambah() {
        _uiState.update { it.copy(
            showDialog = true,
            editProduk = null,
            inputNama = "",
            inputHarga = "",
            errorNama = "",
            errorHarga = ""
        )}
    }

    fun bukaEdit(produk: ProdukBbm) {
        _uiState.update { it.copy(
            showDialog = true,
            editProduk = produk,
            inputNama = produk.namaProduk,
            inputHarga = produk.harga.toLong().toString(),
            errorNama = "",
            errorHarga = ""
        )}
    }

    fun tutupDialog() = _uiState.update { it.copy(showDialog = false) }

    fun setInputNama(v: String) = _uiState.update { it.copy(inputNama = v, errorNama = "") }
    fun setInputHarga(v: String) = _uiState.update { it.copy(inputHarga = v, errorHarga = "") }

    fun simpan() {
        val s = _uiState.value
        var valid = true
        var errNama = ""
        var errHarga = ""

        if (s.inputNama.isBlank()) { errNama = "Nama produk wajib diisi"; valid = false }
        val harga = s.inputHarga.toDoubleOrNull()
        if (harga == null || harga < 0) { errHarga = "Harga harus angka positif"; valid = false }

        if (!valid) {
            _uiState.update { it.copy(errorNama = errNama, errorHarga = errHarga) }
            return
        }

        viewModelScope.launch {
            try {
                if (s.editProduk != null) {
                    produkBbmRepository.update(s.editProduk.copy(
                        namaProduk = s.inputNama.trim(),
                        harga = harga!!
                    ))
                    _uiState.update { it.copy(pesanInfo = "Produk diperbarui") }
                } else {
                    val urutan = (_uiState.value.daftarProduk.maxOfOrNull { it.urutan } ?: 0) + 1
                    produkBbmRepository.insert(ProdukBbm(
                        namaProduk = s.inputNama.trim(),
                        harga = harga!!,
                        urutan = urutan
                    ))
                    _uiState.update { it.copy(pesanInfo = "Produk ditambahkan") }
                }
                tutupDialog()
            } catch (e: Exception) {
                _uiState.update { it.copy(pesanError = "Gagal: ${e.message}") }
            }
        }
    }

    fun hapus(produk: ProdukBbm) {
        viewModelScope.launch {
            produkBbmRepository.delete(produk)
            _uiState.update { it.copy(pesanInfo = "${produk.namaProduk} dihapus") }
        }
    }

    fun clearPesanInfo() = _uiState.update { it.copy(pesanInfo = "") }
    fun clearPesanError() = _uiState.update { it.copy(pesanError = "") }
}
