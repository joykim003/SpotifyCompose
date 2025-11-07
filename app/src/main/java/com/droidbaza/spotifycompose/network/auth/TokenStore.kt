package com.droidbaza.spotifycompose.network.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class TokenStore private constructor(private val appContext: Context) {

    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")

    @Volatile
    private var cachedAccess: String? = null
    @Volatile
    private var cachedRefresh: String? = null

    val accessTokenFlow: Flow<String?> = appContext.dataStore.data.map { it[KEY_ACCESS] }
    val refreshTokenFlow: Flow<String?> = appContext.dataStore.data.map { it[KEY_REFRESH] }

    suspend fun setTokens(access: String?, refresh: String?) {
        cachedAccess = access
        cachedRefresh = refresh
        appContext.dataStore.edit { prefs ->
            if (access == null) prefs.remove(KEY_ACCESS) else prefs[KEY_ACCESS] = access
            if (refresh == null) prefs.remove(KEY_REFRESH) else prefs[KEY_REFRESH] = refresh
        }
    }

    suspend fun clear() {
        cachedAccess = null
        cachedRefresh = null
        appContext.dataStore.edit { it.clear() }
    }

    fun getAccessTokenSync(): String? {
        // Use cache first, fall back to blocking read (used by OkHttp Authenticator)
        cachedAccess?.let { return it }
        return runBlocking { accessTokenFlow.first() }.also { cachedAccess = it }
    }

    fun getRefreshTokenSync(): String? {
        cachedRefresh?.let { return it }
        return runBlocking { refreshTokenFlow.first() }.also { cachedRefresh = it }
    }

    companion object {
        @Volatile private var INSTANCE: TokenStore? = null
        fun getInstance(context: Context): TokenStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: TokenStore(context.applicationContext).also { INSTANCE = it }
            }
    }
}
