package com.example.wms.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wms.data.network.RetrofitInstance
import com.example.wms.model.Bin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class BinViewModel : ViewModel() {
    private val _bins = MutableStateFlow<List<Bin>>(emptyList())
    val bins: StateFlow<List<Bin>> = _bins

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    init {
        fetchBins()
    }

    fun fetchBins() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            fetchBinList { bins, error ->
                _isLoading.value = false
                if (bins != null) {
                    _bins.value = bins
                } else {
                    _error.value = error
                }
            }
        }
    }

    private fun fetchBinList(callback: (List<Bin>?, String?) -> Unit) {
        RetrofitInstance.api.getAllBins().enqueue(object : retrofit2.Callback<List<Bin>> {
            override fun onResponse(call: Call<List<Bin>>, response: Response<List<Bin>>) {
                if (response.isSuccessful) {
                    callback(response.body(), null)
                } else {
                    callback(null, "Request failed: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<List<Bin>>, t: Throwable) {
                callback(null, "Error: ${t.message}")
            }
        })
    }
}

