package com.droidbaza.spotifycompose.network

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.droidbaza.spotifycompose.network.auth.AuthInterceptor
import com.droidbaza.spotifycompose.network.auth.RefreshApi
import com.droidbaza.spotifycompose.network.auth.TokenAuthenticator
import com.droidbaza.spotifycompose.testutil.FakeTokenStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertFalse
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
class TokenAuthenticatorTest {

    private fun clientWith(auth: AuthInterceptor? = null, authenticator: TokenAuthenticator? = null): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val builder = OkHttpClient.Builder().addInterceptor(logging)
        auth?.let { builder.addInterceptor(it) }
        authenticator?.let { builder.authenticator(it) }
        return builder.build()
    }

    @Test
    fun `refresh KO does not retry successfully`() {
        val store = FakeTokenStore(access = "EXPIRED", refresh = "REFRESH")
        kotlinx.coroutines.runBlocking { store.setTokens("EXPIRED", "REFRESH") }

        val server = MockWebServer()
        // First protected call returns 401
        server.enqueue(MockResponse().setResponseCode(401))
        // Refresh endpoint fails
        server.enqueue(MockResponse().setResponseCode(400).setBody("{}"))
        server.start()
        try {
            val retrofit = Retrofit.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val refreshApi = retrofit.create(RefreshApi::class.java)
            val authenticator = TokenAuthenticator(store, injectedRefreshApi = refreshApi)
            val client = clientWith(AuthInterceptor(store), authenticator)
            val req = Request.Builder().url(server.url("/protected").toString()).build()
            client.newCall(req).execute().use { resp ->
                // Should still be unauthorized after failed refresh
                assertFalse(resp.isSuccessful)
            }
        } finally {
            server.shutdown()
        }
    }
}
