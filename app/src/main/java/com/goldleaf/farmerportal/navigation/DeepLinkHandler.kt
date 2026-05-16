package com.goldleaf.farmerportal.navigation

import android.content.Intent
import android.net.Uri
import androidx.navigation.NavController
import com.goldleaf.certification.navigation.CertificationRoutes

object DeepLinkHandler {

    fun handleDeepLink(
        intent: Intent?,
        navController: NavController
    ) {
        val uri: Uri? = intent?.data

        uri?.let {
            val scheme = it.scheme // "goldleaf" or "https"
            val host = it.host // "verify", "batch", "certification" OR "goldleaflabs.co.ke"
            val path = it.path // "/verify/BATCH123" or "/BATCH123"
            val lastSegment = it.lastPathSegment // "BATCH123"

            when {
                // Custom scheme: goldleaf://verify/BATCH123
                scheme == "goldleaf" && host == "verify" -> {
                    lastSegment?.let { batchId ->
                        navController.navigate(
                            CertificationRoutes.ConsumerVerification.createRoute(batchId)
                        )
                    }
                }

                // Custom scheme: goldleaf://batch/BATCH123
                scheme == "goldleaf" && host == "batch" -> {
                    lastSegment?.let { batchId ->
                        navController.navigate(
                            CertificationRoutes.BatchDetails.createRoute(batchId)
                        )
                    }
                }

                // Custom scheme: goldleaf://certification/CERT123
                scheme == "goldleaf" && host == "certification" -> {
                    lastSegment?.let { certId ->
                        navController.navigate(
                            CertificationRoutes.CertificationDetails.createRoute(certId)
                        )
                    }
                }

                // HTTPS: https://goldleaflabs.co.ke/verify/BATCH123
                scheme == "https" && host == "goldleaflabs.co.ke" && path?.startsWith("/verify") == true -> {
                    lastSegment?.let { batchId ->
                        navController.navigate(
                            CertificationRoutes.ConsumerVerification.createRoute(batchId)
                        )
                    }
                }

                // HTTPS: https://goldleaflabs.co.ke/batch/BATCH123
                scheme == "https" && host == "goldleaflabs.co.ke" && path?.startsWith("/batch") == true -> {
                    lastSegment?.let { batchId ->
                        navController.navigate(
                            CertificationRoutes.BatchDetails.createRoute(batchId)
                        )
                    }
                }

                // HTTPS: https://goldleaflabs.co.ke/certification/CERT123
                scheme == "https" && host == "goldleaflabs.co.ke" && path?.startsWith("/certification") == true -> {
                    lastSegment?.let { certId ->
                        navController.navigate(
                            CertificationRoutes.CertificationDetails.createRoute(certId)
                        )
                    }
                }
                // Handle unrecognized deep links
                else -> {
                    // Optional: Log or show error
                    android.util.Log.w("DeepLink", "Unrecognized deep link: $uri")
                }
            }
        }
    }
}