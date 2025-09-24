package com.autoever.mocar.viewmodel

import com.autoever.mocar.domain.model.Car
import com.autoever.mocar.ui.common.component.atoms.BrandUi

sealed class HomeUiState {
    data object Loading : HomeUiState()
    data class Partial(val cars: List<Car>) : HomeUiState()
    data class Success(
        val cars: List<Car>,
        val favorites: Set<String>,
        val brands: List<BrandUi>
    ) : HomeUiState()
    data class Error(val message: String?) : HomeUiState()
}
