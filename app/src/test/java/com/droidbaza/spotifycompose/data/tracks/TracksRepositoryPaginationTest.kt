package com.droidbaza.spotifycompose.data.tracks

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.droidbaza.spotifycompose.domain.model.Paged
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@RunWith(RobolectricTestRunner::class)
class TracksRepositoryPaginationTest {

    private fun retrofitFor(server: MockWebServer): Retrofit = Retrofit.Builder()
        .baseUrl(server.url("/").toString())
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    @Test
    fun pagination_ok_maps_to_domain() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val server = MockWebServer()
        server.enqueue(
            MockResponse().setResponseCode(200).setBody(
                """
                {"success":true,"data":{"items":[{"id":"550e8400-e29b-41d4-a716-446655440000","title":"Song A","artist":{"name":"Artist A"},"durationMs":180000}],"total":1,"limit":20,"offset":0,"hasMore":false}}
                """.trimIndent()
            )
        )
        server.start()
        try {
            val api = retrofitFor(server).create(TracksApi::class.java)
            val repo = TracksRepository(context, injectedApi = api)
            val page: Paged<com.droidbaza.spotifycompose.domain.model.TrackItem> =
                kotlinx.coroutines.runBlocking { repo.getTracks(20, 0) }
            assertEquals(1, page.items.size)
            assertEquals("Song A", page.items.first().title)
            assertEquals(false, page.hasMore)
            assertEquals(0, page.offset)
        } finally {
            server.shutdown()
        }
    }

    @Test
    fun pagination_error_throws() {
        val context: Context = ApplicationProvider.getApplicationContext()
        val server = MockWebServer()
        server.enqueue(MockResponse().setResponseCode(500).setBody("{}"))
        server.start()
        try {
            val api = retrofitFor(server).create(TracksApi::class.java)
            val repo = TracksRepository(context, injectedApi = api)
            val res = kotlin.runCatching { kotlinx.coroutines.runBlocking { repo.getTracks(20, 0) } }
            assertTrue(res.isFailure)
        } finally {
            server.shutdown()
        }
    }
}
