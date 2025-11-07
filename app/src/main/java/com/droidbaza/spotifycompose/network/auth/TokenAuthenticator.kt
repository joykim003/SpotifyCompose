package com.droidbaza.spotifycompose.network.auth

import com.droidbaza.spotifycompose.BuildConfig
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

data class RefreshBody(val refresh_token: String)
data class SessionDTO(val access_token: String?, val refresh_token: String?)
data class RefreshData(val session: SessionDTO?)
data class RefreshResponse(val success: Boolean, val data: RefreshData?)

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshBody): RefreshResponse
}

class TokenAuthenticator(
    private val tokenStore: TokenStore,
    private val injectedRefreshApi: RefreshApi? = null
) : Authenticator {

    // Separate Retrofit instance without auth interceptor to avoid header loops
    private val refreshRetrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val refreshApi: RefreshApi by lazy { injectedRefreshApi ?: refreshRetrofit.create(RefreshApi::class.java) }

    override fun authenticate(route: Route?, response: Response): Request? {
        // Avoid infinite retry loops
        if (responseCount(response) >= 2) return null

        val currentRefresh = tokenStore.getRefreshTokenSync() ?: return null

        val newAccess = runBlocking {
            try {
                val res = refreshApi.refresh(RefreshBody(currentRefresh))
                val session = res.data?.session
                val access = session?.access_token
                val refresh = session?.refresh_token
                if (!access.isNullOrBlank()) {
                    tokenStore.setTokens(access, refresh)
                }
                access
            } catch (_: Exception) {
                null
            }
        } ?: return null

        // Replay original request with new token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $newAccess")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var result = 1
        var prior: Response? = response.priorResponse
        while (prior != null) {
            result++
            prior = prior.priorResponse
        }
        return result
    }
}
