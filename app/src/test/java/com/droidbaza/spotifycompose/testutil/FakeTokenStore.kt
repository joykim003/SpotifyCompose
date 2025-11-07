package com.droidbaza.spotifycompose.testutil

import com.droidbaza.spotifycompose.network.auth.TokenStoreContract
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTokenStore(
    access: String? = null,
    refresh: String? = null
) : TokenStoreContract {
    private val accessFlow = MutableStateFlow(access)
    private val refreshFlow = MutableStateFlow(refresh)

    override val accessTokenFlow: Flow<String?> = accessFlow
    override val refreshTokenFlow: Flow<String?> = refreshFlow

    override suspend fun setTokens(access: String?, refresh: String?) {
        accessFlow.value = access
        refreshFlow.value = refresh
    }

    override suspend fun clear() {
        accessFlow.value = null
        refreshFlow.value = null
    }

    override fun getAccessTokenSync(): String? = accessFlow.value
    override fun getRefreshTokenSync(): String? = refreshFlow.value
}
