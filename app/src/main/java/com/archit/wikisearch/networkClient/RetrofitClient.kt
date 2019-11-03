package com.archit.wikisearch.networkClient

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import okhttp3.Cache
import okhttp3.Interceptor
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

    /**
     * returns cache enables Retrofit client
     * maintains 10 seconds cache while online
     * maintains 7 days cache when offline
     */
    fun getCacheEnabledRetrofit(context: Context): Retrofit {

        val onlineCacheInterceptor = Interceptor { chain ->
            val originalResponse = chain.proceed(chain.request())
            val cacheControl = originalResponse.header("Cache-Control")
            if (cacheControl == null || cacheControl!!.contains("no-store") || cacheControl!!.contains("no-cache") ||
                cacheControl!!.contains("must-revalidate") || cacheControl!!.contains("max-age=0")
            ) {
                originalResponse.newBuilder()
                    .header("Cache-Control", "public, max-age=" + 10)
                    .build()
            } else {
                originalResponse
            }
        }

        val offlineCacheInterceptor = Interceptor { chain ->
            var request = chain.request()
            if (!hasNetwork(context)!!) {
                val maxStale = 60 * 60 * 24 * 7
                request = request.newBuilder()
                    .header("Cache-Control", "public, only-if-cached, max-stale=$maxStale")
                    .build()
            }
            chain.proceed(request)
        }

        val okHttpClient = OkHttpClient.Builder()
            .addNetworkInterceptor(onlineCacheInterceptor)
            .addInterceptor(offlineCacheInterceptor)
            .cache(Cache(File(context.cacheDir, "http-cache"), 100 * 1024 * 1024))
            .build()

        return Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .baseUrl("https://en.wikipedia.org//")
            .build()
    }
}

