package com.goldleaf.feature.cropmanagement.ui.selection

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLEncoder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsWhatsappNotifier @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun sendRequest(phone: String, message: String) {
        Log.d("SmsWhatsappNotifier", "📤 sendRequest() called with phone=$phone, message length=${message.length}")
        
        val cleanPhone = phone.replace(Regex("[^\\d]"), "")
        Log.d("SmsWhatsappNotifier", "📱 Cleaned phone: $cleanPhone")
        
        val fullPhone = if (cleanPhone.startsWith("254")) cleanPhone else "254$cleanPhone"
        Log.d("SmsWhatsappNotifier", "📱 Full phone with country code: $fullPhone")

        withContext(Dispatchers.Main) {
            // Modern Android (API 23+) requires SMS permission and doesn't have reliable SMS sending
            // Go directly to WhatsApp as the primary method
            Log.d("SmsWhatsappNotifier", "🚀 Attempting to open WhatsApp...")
            openWhatsApp(fullPhone, message)
        }
    }

    private fun openWhatsApp(phone: String, message: String) {
        try {
            Log.d("SmsWhatsappNotifier", "🔗 Attempting WhatsApp app intent (com.whatsapp)...")
            val encodedMessage = URLEncoder.encode(message, "UTF-8")
            val url = "https://wa.me/$phone?text=$encodedMessage"
            Log.d("SmsWhatsappNotifier", "🔗 URL: $url")
            
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                setPackage("com.whatsapp") // Ensures WhatsApp opens if installed
            }
            
            Log.d("SmsWhatsappNotifier", "✅ Starting WhatsApp activity...")
            context.startActivity(intent)
            Log.d("SmsWhatsappNotifier", "✅ WhatsApp activity started successfully")
        } catch (e: Exception) {
            // If WhatsApp package not found, try web version
            Log.w("SmsWhatsappNotifier", "⚠️ WhatsApp app not found or error: ${e.message}")
            try {
                Log.d("SmsWhatsappNotifier", "🔗 Attempting WhatsApp web fallback...")
                val encodedMessage = URLEncoder.encode(message, "UTF-8")
                val url = "https://wa.me/$phone?text=$encodedMessage"
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                Log.d("SmsWhatsappNotifier", "✅ Starting fallback browser intent...")
                context.startActivity(intent)
                Log.d("SmsWhatsappNotifier", "✅ Fallback browser started successfully")
            } catch (webException: Exception) {
                Log.e("SmsWhatsappNotifier", "❌ Failed to open WhatsApp: ${webException.message}", webException)
                Toast.makeText(
                    context,
                    "Unable to send message. Please install WhatsApp",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}

// Composable wrapper for easy usage in Compose UI
@Composable
fun rememberSmsWhatsappNotifier(notifier: SmsWhatsappNotifier): SmsWhatsappNotifierCompose {
    val scope = rememberCoroutineScope()

    return SmsWhatsappNotifierCompose(
        notifier = notifier,
        scope = scope
    )
}

class SmsWhatsappNotifierCompose(
    private val notifier: SmsWhatsappNotifier,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    fun sendRequest(phone: String, message: String) {
        scope.launch {
            notifier.sendRequest(phone, message)
        }
    }
}