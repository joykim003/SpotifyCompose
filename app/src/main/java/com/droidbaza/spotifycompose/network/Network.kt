package com.droidbaza.spotifycompose.network

import com.droidbaza.spotifycompose.BuildConfig
import com.droidbaza.spotifycompose.network.auth.AuthInterceptor
import com.droidbaza.spotifycompose.network.auth.TokenAuthenticator
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object Network {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    fun retrofit(
        authInterceptor: AuthInterceptor? = null,
        tokenAuthenticator: TokenAuthenticator? = null
    ): Retrofit {
        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(logging)
        authInterceptor?.let { clientBuilder.addInterceptor(it) }
        tokenAuthenticator?.let { clientBuilder.authenticator(it) }

        val client = clientBuilder.build()

        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
