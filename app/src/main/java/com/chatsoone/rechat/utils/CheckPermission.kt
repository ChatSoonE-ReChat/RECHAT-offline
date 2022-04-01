package com.chatsoone.rechat.utils

import android.content.Context
import androidx.core.app.NotificationManagerCompat

fun permissionGrantred(context: Context): Boolean {
    return NotificationManagerCompat.getEnabledListenerPackages(context).any { enabledPackageName ->
        enabledPackageName == context.packageName
    }
}