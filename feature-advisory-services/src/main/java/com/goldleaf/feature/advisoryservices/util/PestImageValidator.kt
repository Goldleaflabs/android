package com.goldleaf.feature.advisoryservices.util

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class PestImageValidator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun isValidImage(uri: Uri?, maxSizeMB: Int = 10): Boolean {
        return ImageUtils.isValidImage(context, uri, maxSizeMB)
    }
}
