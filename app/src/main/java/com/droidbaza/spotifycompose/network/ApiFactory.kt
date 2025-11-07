package com.droidbaza.spotifycompose.network

import android.content.Context
import com.droidbaza.spotifycompose.network.auth.AuthInterceptor
import com.droidbaza.spotifycompose.network.auth.TokenAuthenticator
import com.droidbaza.spotifycompose.network.auth.TokenStores
import com.droidbaza.spotifycompose.network.generated.api.AuthenticationApi
import com.droidbaza.spotifycompose.network.generated.api.TracksApi
import com.droidbaza.spotifycompose.network.generated.api.AlbumsApi
import com.droidbaza.spotifycompose.network.generated.api.ArtistsApi
import com.droidbaza.spotifycompose.network.generated.api.PlaylistsApi
import com.droidbaza.spotifycompose.network.generated.api.UsersApi
import com.droidbaza.spotifycompose.network.generated.api.SearchApi
import com.droidbaza.spotifycompose.network.generated.api.UploadApi

object ApiFactory {
    @Volatile private var retrofitHolder: retrofit2.Retrofit? = null

    private fun buildRetrofit(context: Context): retrofit2.Retrofit {
        val store = TokenStores.get(context)
        val authInterceptor = AuthInterceptor(store)
        val tokenAuthenticator = TokenAuthenticator(store)
        return Network.retrofit(authInterceptor, tokenAuthenticator)
    }

    private fun retrofit(context: Context): retrofit2.Retrofit =
        retrofitHolder ?: synchronized(this) {
            retrofitHolder ?: buildRetrofit(context).also { retrofitHolder = it }
        }

    fun authentication(context: Context): AuthenticationApi = retrofit(context).create(AuthenticationApi::class.java)
    fun tracks(context: Context): TracksApi = retrofit(context).create(TracksApi::class.java)
    fun albums(context: Context): AlbumsApi = retrofit(context).create(AlbumsApi::class.java)
    fun artists(context: Context): ArtistsApi = retrofit(context).create(ArtistsApi::class.java)
    fun playlists(context: Context): PlaylistsApi = retrofit(context).create(PlaylistsApi::class.java)
    fun users(context: Context): UsersApi = retrofit(context).create(UsersApi::class.java)
    fun search(context: Context): SearchApi = retrofit(context).create(SearchApi::class.java)
    fun upload(context: Context): UploadApi = retrofit(context).create(UploadApi::class.java)
}
