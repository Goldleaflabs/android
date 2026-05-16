package com.goldleaf.certification.presentation.verification

// feature-certification-quality/src/main/java/com/goldleaf/certification/presentation/verification/VerificationViewModel.kt

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.goldleaf.certification.domain.repository.BatchRepository
import com.goldleaf.certification.domain.repository.VerificationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VerificationViewModel @Inject constructor(
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _verificationResult = MutableStateFlow<VerificationResult?>(null)
    val verificationResult: StateFlow<VerificationResult?> = _verificationResult

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun verifyProduct(batchNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true

            val result = batchRepository.verifyProduct(batchNumber)

            result.onSuccess { verification ->
                _verificationResult.value = verification
            }.onFailure {
                _verificationResult.value = VerificationResult(
                    isValid = false,
                    message = "Unable to verify product. Please try again.",
                    batch = null,
                    blockchainRecord = null,
                    labTests = emptyList()
                )
            }

            _isLoading.value = false
        }
    }



}