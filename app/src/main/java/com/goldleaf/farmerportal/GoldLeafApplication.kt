package com.goldleaf.farmerportal

import android.app.Application
import com.goldleaf.farmerportal.errorhandler.CrashHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class GoldLeafApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.setDefaultUncaughtExceptionHandler(
            CrashHandler(applicationContext)
        )
    }
}