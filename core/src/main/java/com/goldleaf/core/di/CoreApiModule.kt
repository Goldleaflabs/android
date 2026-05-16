package com.goldleaf.core.di

import android.content.Context
import com.goldleaf.core.auth.UserSessionManager
import com.goldleaf.core.data.api.*
import com.goldleaf.core.data.network.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreApiModule {


    @Provides
    @MainApiUrl
    fun provideMainApiUrl(): String = "https://api.goldleaflabs.co.ke/v1/"

    @Provides
    @BatchApiUrl
    fun provideBatchApiUrl(): String = "https://batch.goldleaflabs.co.ke/v1/"

    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        val cacheSize = 10L * 1024 * 1024
        return Cache(cacheDir, cacheSize)
    }

    @Provides
    fun provideLoggingInterceptor(@IsDebug isDebug: Boolean): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (isDebug) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.BASIC
        }
    }

    @Provides
    @Singleton
    fun provideNetworkInterceptor(@ApplicationContext context: Context): NetworkInterceptor {
        return NetworkInterceptor(context)
    }

    @Provides
    @Singleton
    fun provideRetryInterceptor(): RetryInterceptor {
        return RetryInterceptor(maxRetries = 2, initialDelayMs = 1000, maxDelayMs = 10000, backoffMultiplier = 2.0)
    }


    @Provides
    @Singleton
    fun provideConnectionPool(): ConnectionPool {
        return ConnectionPool(maxIdleConnections = 5, keepAliveDuration = 5, TimeUnit.MINUTES)
    }


    // --- SECTION A: NO-AUTH PATH (Breaks the cycle) ---
    @Provides
    @Singleton
    @NoAuthRetrofit
    fun provideNoAuthRetrofit(
        @MainApiUrl baseUrl: String,
        @NoAuthRetrofit okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }



    @Provides
    @Singleton
    @NoAuthRetrofit
    fun provideNoAuthOkHttpClient(
        @IsDebug isDebug: Boolean,
        loggingInterceptor: HttpLoggingInterceptor,
        networkInterceptor: NetworkInterceptor
    ): OkHttpClient {

        loggingInterceptor.level =
            if (isDebug)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.BASIC

        return OkHttpClient.Builder()
            .connectTimeout(30L, TimeUnit.SECONDS)
            .addInterceptor(networkInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }



    @Provides
    @Singleton
    fun provideAuthApiService(@NoAuthRetrofit retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    // --- SECTION B: AUTH PATH (Main App Usage) ---

    @Provides
    @Singleton
    fun provideAuthInterceptor(userSessionManager: UserSessionManager): AuthInterceptor {
        return AuthInterceptor(userSessionManager)
    }


    @Provides
    @Singleton
    fun provideOkHttpClient(
        @IsDebug isDebug: Boolean,
        connectionPool: ConnectionPool,
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: AuthInterceptor,
        networkInterceptor: NetworkInterceptor,
        retryInterceptor: RetryInterceptor
    ): OkHttpClient {

        // 🔒 Ensure logging level is safe at runtime
        loggingInterceptor.level =
            if (isDebug)
                HttpLoggingInterceptor.Level.BODY
            else
                HttpLoggingInterceptor.Level.BASIC

        return OkHttpClient.Builder()
            .cache(null) // explicitly disabled
            .connectionPool(connectionPool)
            .connectTimeout(30L, TimeUnit.SECONDS)
            .readTimeout(30L, TimeUnit.SECONDS)
            .writeTimeout(30L, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)

            // ✅ Mutating / retry interceptors FIRST
            .addInterceptor(networkInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(retryInterceptor)

            // ✅ Logging interceptor LAST (critical)
            .addInterceptor(loggingInterceptor)

            .build()
    }


    @Provides
    @Singleton
    @MainRetrofit
    fun provideMainRetrofit(
        @MainApiUrl mainApiUrl: String,
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(mainApiUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // --- SECTION C: SERVICES ---

    @Provides
    @Singleton
    fun provideApiService(@MainRetrofit retrofit: Retrofit): ApiService {
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun providePlantIdentificationApi(@MainRetrofit retrofit: Retrofit): PlantIdentificationApi {
        return retrofit.create(PlantIdentificationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideAIBackendService(@MainRetrofit retrofit: Retrofit): AIBackendService {
        return retrofit.create(AIBackendService::class.java)
    }

    @Provides
    @Singleton
    fun provideVideoApiService(@MainRetrofit retrofit: Retrofit): VideoApiService {
        return retrofit.create(VideoApiService::class.java)
    }

    @Provides
    @Singleton
    @BatchRetrofit
    fun provideBatchRetrofit(@BatchApiUrl batchApiUrl: String, okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(batchApiUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(@ApplicationContext context: Context): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideDownloadManager(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient,
        @IoDispatcher ioDispatcher: CoroutineDispatcher
    ): DownloadManager = DownloadManager(context, okHttpClient, ioDispatcher)

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}

@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainApiUrl
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BatchApiUrl
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class MainRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class BatchRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IoDispatcher
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class NoAuthRetrofit
@Qualifier @Retention(AnnotationRetention.BINARY) annotation class IsDebug
