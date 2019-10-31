package com.archit.wikisearch.networkClient

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import okhttp3.Cache
import okhttp3.CacheControl
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

object RetrofitClient {
    /**
     * checks for internet connection available
     */
    private fun hasNetwork(context: Context): Boolean? {
        var isConnected: Boolean? = false // Initial Value
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = connectivityManager.activeNetworkInfo
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting)
            isConnected = true
        return isConnected
    }

    /*
     * As of now, cache is getting created but unexpectedly,
     * saved cache files are not being read when offline
     * API Response gives "HTTP 504 Unsatisfiable Request (only-if-cached)" when offline
     * RCA is in WIP for this behaviour
     */
    fun getCacheEnabledRetrofit(context: Context): Retrofit {
        val okHttpClient = OkHttpClient.Builder()
            .cache(Cache(context.cacheDir, (5 * 1024 * 1024).toLong()))
            .addInterceptor { chain ->
                var request = chain.request()
                request = if (hasNetwork(context)!!)
                    request.newBuilder()
                        .removeHeader("Cache-Control")
                        .header(
                            "Cache-Control",
                            "public, max-age=" + 5)
                        .removeHeader("Pragma")
                        .build()
                else
                    request.newBuilder()
                        .header(
                            "Cache-Control",
                            "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7
                        )
                        .removeHeader("Pragma")
                        .build()
                chain.proceed(request)
            }
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("https://en.wikipedia.org//")
            .build()
    }
}

