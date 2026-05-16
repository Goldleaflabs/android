package com.goldleaf.core.data.api


import com.goldleaf.core.data.models.VideoDto
import com.goldleaf.core.data.network.ApiResponse
import retrofit2.http.*

interface VideoApiService {

    @GET("training/videos")
    suspend fun getAllVideos(): ApiResponse<List<VideoDto>>

    @GET("training/videos/{id}")
    suspend fun getVideoById(@Path("id") id: String): ApiResponse<VideoDto>

    @GET("training/videos/featured")
    suspend fun getFeaturedVideos(): ApiResponse<List<VideoDto>>

    @GET("training/videos/category/{category}")
    suspend fun getVideosByCategory(   @Path("category") category: String  ): ApiResponse<List<VideoDto>>

    @GET("training/videos/search")
    suspend fun searchVideos(@Query("q") query: String): ApiResponse<List<VideoDto>>

    @POST("training/videos/{id}/view")
    suspend fun trackVideoView( @Path("id") videoId: String,  @Body userId: String): ApiResponse<Unit>
}
