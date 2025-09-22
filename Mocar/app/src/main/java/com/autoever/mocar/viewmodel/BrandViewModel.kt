package com.autoever.mocar.viewmodel

import androidx.lifecycle.ViewModel
import com.autoever.mocar.data.brands.BrandDto
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class BrandViewModel : ViewModel() {
    private val _brands = MutableStateFlow<List<BrandDto>>(emptyList())
    val brands: StateFlow<List<BrandDto>> = _brands

    init {
        fetchBrands()
    }

    private fun fetchBrands() {
        val db = FirebaseFirestore.getInstance()
        db.collection("car_brand").get()
            .addOnSuccessListener { result ->
                val list = result.mapNotNull { it.toObject(BrandDto::class.java) }
                _brands.value = list
            }
            .addOnFailureListener {
                // 에러 처리
            }
    }
}