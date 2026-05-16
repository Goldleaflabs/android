package com.goldleaf.farmerportal.di


import android.content.Context
import com.goldleaf.core.di.IsDebug // Import the label from your core module
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @IsDebug
    fun provideIsDebug(@ApplicationContext context: Context): Boolean {
        // This checks the system flags directly.
        // No BuildConfig required = NO RED TEXT.
        return (context.applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}