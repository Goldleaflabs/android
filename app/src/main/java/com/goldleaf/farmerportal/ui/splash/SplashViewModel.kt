package com.goldleaf.farmerportal.ui.splash


import androidx.lifecycle.ViewModel
import com.goldleaf.core.auth.UserSessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val userSession: UserSessionManager
) : ViewModel() {

    val isLoggedIn: Flow<Boolean> = userSession.isLoggedIn
}