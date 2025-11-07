package com.droidbaza.spotifycompose.network.auth

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

interface TokenStoreContract {
    val accessTokenFlow: Flow<String?>
    val refreshTokenFlow: Flow<String?>
    suspend fun setTokens(access: String?, refresh: String?)
    suspend fun clear()
    fun getAccessTokenSync(): String?
    fun getRefreshTokenSync(): String?
}

private val Context.dataStore by preferencesDataStore(name = "auth_prefs")

class DataStoreTokenStore(private val appContext: Context) : TokenStoreContract {
    private val KEY_ACCESS = stringPreferencesKey("access_token")
    private val KEY_REFRESH = stringPreferencesKey("refresh_token")

    @Volatile private var cachedAccess: String? = null
    @Volatile private var cachedRefresh: String? = null

    override val accessTokenFlow: Flow<String?> = appContext.dataStore.data.map { it[KEY_ACCESS] }
    override val refreshTokenFlow: Flow<String?> = appContext.dataStore.data.map { it[KEY_REFRESH] }

    override suspend fun setTokens(access: String?, refresh: String?) {
        cachedAccess = access
        cachedRefresh = refresh
        appContext.dataStore.edit { prefs ->
            if (access == null) prefs.remove(KEY_ACCESS) else prefs[KEY_ACCESS] = access
            if (refresh == null) prefs.remove(KEY_REFRESH) else prefs[KEY_REFRESH] = refresh
        }
    }

    override suspend fun clear() {
        cachedAccess = null
        cachedRefresh = null
        appContext.dataStore.edit { it.clear() }
    }

    override fun getAccessTokenSync(): String? {
        cachedAccess?.let { return it }
        return runBlocking { accessTokenFlow.first() }.also { cachedAccess = it }
    }

    override fun getRefreshTokenSync(): String? {
        cachedRefresh?.let { return it }
        return runBlocking { refreshTokenFlow.first() }.also { cachedRefresh = it }
    }
}

object TokenStores {
    @Volatile private var INSTANCE: TokenStoreContract? = null
    fun get(context: Context): TokenStoreContract =
        INSTANCE ?: synchronized(this) {
            INSTANCE ?: DataStoreTokenStore(context.applicationContext).also { INSTANCE = it }
        }
    fun setForTests(fake: TokenStoreContract) { INSTANCE = fake }
}
