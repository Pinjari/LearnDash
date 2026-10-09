package com.intellipaat.learndash.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * One-line answer to "are we online?". Used to pick the right message —
 * cached-data-with-banner vs genuine first-load failure.
 */
@Singleton
class ConnectivityGate @Inject constructor(
    @ApplicationContext private val context: Context
) : Connectivity {
    override fun isOnline(): Boolean {
        val manager = context.getSystemService(ConnectivityManager::class.java)
            ?: return false
        val network = manager.activeNetwork ?: return false
        val caps = manager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
