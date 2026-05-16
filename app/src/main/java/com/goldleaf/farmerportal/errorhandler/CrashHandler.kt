package com.goldleaf.farmerportal.errorhandler

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.io.PrintWriter
import java.io.StringWriter

class CrashHandler(private val context: Context) : Thread.UncaughtExceptionHandler {

    private val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

    companion object {
        private const val TAG = "CrashHandler"
    }

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            // Log the crash
            Log.e(TAG, "Uncaught exception in thread ${thread.name}", throwable)

            // Save crash details
            saveCrashLog(throwable)

            // Attempt to send crash report
            sendCrashEmail(throwable)

        } catch (e: Exception) {
            Log.e(TAG, "Error handling crash", e)
        } finally {
            // Let the default handler finish the process
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun sendCrashEmail(throwable: Throwable) {
        try {
            val stackTrace = getStackTraceString(throwable)

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "message/rfc822"
                putExtra(Intent.EXTRA_EMAIL, arrayOf("info@goldleaf.co.ke"))
                putExtra(Intent.EXTRA_SUBJECT, "Farmer Portal Crash Report")
                putExtra(Intent.EXTRA_TEXT, buildCrashReport(throwable, stackTrace))

                // CRITICAL FIX: Add FLAG_ACTIVITY_NEW_TASK
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Use chooser to avoid direct startActivity issues
            val chooser = Intent.createChooser(intent, "Send Crash Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to send crash email", e)
        }
    }

    private fun saveCrashLog(throwable: Throwable) {
        try {
            val crashLog = buildCrashReport(throwable, getStackTraceString(throwable))

            // Save to app's internal storage
            context.openFileOutput("crash_log.txt", Context.MODE_PRIVATE).use { output ->
                output.write(crashLog.toByteArray())
            }

            Log.d(TAG, "Crash log saved successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save crash log", e)
        }
    }

    private fun buildCrashReport(throwable: Throwable, stackTrace: String): String {
        return buildString {
            appendLine("=== FARMER PORTAL CRASH REPORT ===")
            appendLine()
            appendLine("Time: ${System.currentTimeMillis()}")
            appendLine("App Version: ${getAppVersion()}")
            appendLine("Android Version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
            appendLine()
            appendLine("Exception: ${throwable.javaClass.simpleName}")
            appendLine("Message: ${throwable.message}")
            appendLine()
            appendLine("Stack Trace:")
            appendLine(stackTrace)

            // Include cause if present
            throwable.cause?.let { cause ->
                appendLine()
                appendLine("Caused by: ${cause.javaClass.simpleName}")
                appendLine("Message: ${cause.message}")
                appendLine(getStackTraceString(cause))
            }
        }
    }

    private fun getStackTraceString(throwable: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        throwable.printStackTrace(pw)
        return sw.toString()
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}