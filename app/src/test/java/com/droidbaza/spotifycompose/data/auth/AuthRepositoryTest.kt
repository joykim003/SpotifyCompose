package com.droidbaza.spotifycompose.data.auth

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.droidbaza.spotifycompose.network.auth.TokenStore
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.Ignore
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class AuthRepositoryTest {

    private fun retrofitFor(server: MockWebServer): Retrofit = Retrofit.Builder()
        .baseUrl(server.url("/").toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Ignore("DataStore I/O under unit tests; to be re-enabled with injectable TokenStore")
    @Test
    fun login_success_persists_tokens() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val store = TokenStore.getInstance(context)
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"data":{"session":{"access_token":"ACCESS","refresh_token":"REFRESH"}}}
                """.trimIndent()
            )
        )
        server.start()
        try {
            val api = retrofitFor(server).create(AuthApi::class.java)
            val repo = AuthRepository(context, authApi = api)
            val res = kotlin.runCatching {
                kotlinx.coroutines.runBlocking { repo.login("a@b.c", "pwd") }
            }
            assertTrue(res.isSuccess)
            // TokenStore should now contain the tokens
            val access = store.getAccessTokenSync()
            val refresh = store.getRefreshTokenSync()
            assertEquals("ACCESS", access)
            assertEquals("REFRESH", refresh)
        } finally {
            server.shutdown()
        }
    }

    @Ignore("DataStore I/O under unit tests; to be re-enabled with injectable TokenStore")
    @Test
    fun login_failure_returns_failure() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(401).setBody("{}"))
        server.start()
        try {
            val api = retrofitFor(server).create(AuthApi::class.java)
            val repo = AuthRepository(context, authApi = api)
            val res = kotlinx.coroutines.runBlocking { repo.login("a@b.c", "pwd") }
            assertTrue(res.isFailure)
        } finally {
            server.shutdown()
        }
    }
}
