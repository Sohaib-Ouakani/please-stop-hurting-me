package com.example.traveldiary.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings

fun openAppSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", ctx.packageName, null)
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(ctx.packageManager) != null) {
        ctx.startActivity(intent)
    }
}

fun openWirelessSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_WIRELESS_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(ctx.packageManager) != null) {
        ctx.applicationContext.startActivity(intent)
    }
}

fun openLocationSettings(ctx: Context) {
    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
    if (intent.resolveActivity(ctx.packageManager) != null) {
        ctx.startActivity(intent)
    }
}
