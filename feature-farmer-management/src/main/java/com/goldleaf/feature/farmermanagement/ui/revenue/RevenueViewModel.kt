package com.goldleaf.feature.farmermanagement.ui.revenue

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.core.data.api.ApiService
import com.goldleaf.core.data.local.FarmerPayoutInfoEntity
import com.goldleaf.core.data.local.dao.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RevenueUiState(
    val totalPaid: Double = 0.0,
    val totalDeclaredKg: Double = 0.0,
    val totalDeductions: Double = 0.0,
    val mpesaPhone: String = "",
    val isLoading: Boolean = true
)

@HiltViewModel
class RevenueViewModel @Inject constructor(
    private val apiService: ApiService,
    private val paymentDao: PaymentDao,
    private val deductionDao: DeductionDao,
    private val payoutInfoDao: FarmerPayoutInfoDao,
    private val harvestDeliveryDao: HarvestDeliveryDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(RevenueUiState())
    val uiState: StateFlow<RevenueUiState> = _uiState.asStateFlow()

    fun loadRevenue(farmerId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val response = apiService.getFarmerRevenue(farmerId)
                if (response.isSuccessful) {
                    val data = response.body()?.data
                    if (data != null) {
                        _uiState.value = _uiState.value.copy(
                            totalPaid = data.successfullyPaid,
                            totalDeclaredKg = data.totalDeclaredKg,
                            totalDeductions = data.totalDeductions
                        )
                    }
                }
                // Also load payout info
                val payoutResponse = apiService.getPayoutInfo(farmerId)
                if (payoutResponse.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        mpesaPhone = payoutResponse.body()?.data?.mpesaPhone ?: ""
                    )
                }
            } catch (e: Exception) {
                // Load from local DB as fallback
                val paid = paymentDao.getTotalPaid(farmerId) ?: 0.0
                val deductions = deductionDao.getTotalDeductions(farmerId) ?: 0.0
                val payout = payoutInfoDao.getByFarmer(farmerId)
                _uiState.value = _uiState.value.copy(
                    totalPaid = paid,
                    totalDeductions = deductions,
                    mpesaPhone = payout?.mpesaPhone ?: ""
                )
            }
            _uiState.value = _uiState.value.copy(isLoading = false)
        }
    }

    fun updatePayoutPhone(farmerId: String, phone: String) {
        viewModelScope.launch {
            try {
                apiService.updatePayoutInfo(farmerId, FarmerPayoutInfoEntity(farmerId = farmerId, mpesaPhone = phone))
                payoutInfoDao.insert(FarmerPayoutInfoEntity(farmerId = farmerId, mpesaPhone = phone))
                _uiState.value = _uiState.value.copy(mpesaPhone = phone)
            } catch (e: Exception) { }
        }
    }
}
