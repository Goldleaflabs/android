package com.goldleaf.core.data.api

import com.goldleaf.core.data.dto.auth.DashboardFarmer
import com.goldleaf.core.data.dto.auth.LoginRequest
import com.goldleaf.core.data.dto.auth.LoginResponse
import com.goldleaf.core.data.dto.auth.RefreshTokenRequest
import com.goldleaf.core.data.dto.auth.TokenResponse
import com.goldleaf.core.data.dto.farm.Farmer
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<TokenResponse>

    @POST("auth/getFarmerById")
    suspend fun getFarmerById(@Body request: DashboardFarmer): Response<Farmer>

}