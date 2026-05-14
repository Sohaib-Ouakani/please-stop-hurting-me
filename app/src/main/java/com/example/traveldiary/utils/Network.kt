package com.example.traveldiary.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

fun isOnline(ctx: Context): Boolean {
    val connectivityManager = ctx
        .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val capabilities =
        connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
    return capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true ||
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
}
