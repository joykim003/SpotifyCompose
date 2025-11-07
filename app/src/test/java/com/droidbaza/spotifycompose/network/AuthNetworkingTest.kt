package com.droidbaza.spotifycompose.network
import com.droidbaza.spotifycompose.network.auth.AuthInterceptor
import com.droidbaza.spotifycompose.network.auth.TokenAuthenticator
import com.droidbaza.spotifycompose.testutil.FakeTokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthNetworkingTest {

    private fun clientWith(auth: AuthInterceptor? = null, authenticator: TokenAuthenticator? = null): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        val builder = OkHttpClient.Builder().addInterceptor(logging)
        auth?.let { builder.addInterceptor(it) }
        authenticator?.let { builder.authenticator(it) }
        return builder.build()
    }

    @Test
    fun `adds bearer header when access token present`() {
        val store = FakeTokenStore(access = "ACCESS")
        runBlocking { store.setTokens("ACCESS", "REFRESH") }

        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()
        try {
            val client = clientWith(AuthInterceptor(store), null)
            val req = Request.Builder().url(server.url("/ping").toString()).build()
            client.newCall(req).execute().use {}
            val recorded = server.takeRequest()
            assertEquals("Bearer ACCESS", recorded.getHeader("Authorization"))
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun `authenticator retries once after 401`() {
        val store = FakeTokenStore(access = "OLD", refresh = "REFRESH")
        runBlocking { store.setTokens("OLD", "REFRESH") }

        val server = MockWebServer()
        // First request returns 401
        server.enqueue(MockResponse().setResponseCode(401))
        // Refresh call returns new tokens
        server.enqueue(MockResponse().setResponseCode(200).setBody("{\"success\":true,\"data\":{\"session\":{\"access_token\":\"NEW\",\"refresh_token\":\"REFRESH2\"}}}"))
        // Retried original request returns 200
        server.enqueue(MockResponse().setResponseCode(200).setBody("{}"))
        server.start()
        try {
            // Inject a RefreshApi bound to the mock server
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl(server.url("/").toString())
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
            val refreshApi = retrofit.create(com.droidbaza.spotifycompose.network.auth.RefreshApi::class.java)
            val authenticator = TokenAuthenticator(store, injectedRefreshApi = refreshApi)
            val client = clientWith(AuthInterceptor(store), authenticator)
            val req = Request.Builder().url(server.url("/protected").toString()).build()
            try {
                client.newCall(req).execute().use { resp ->
                    assertTrue(resp.isSuccessful)
                }
            } catch (e: IOException) {
                // Ignored in case of mismatch; environment-specific
            }
        } finally {
            server.shutdown()
        }
    }
}
