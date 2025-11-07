package com.droidbaza.spotifycompose.data.auth

import android.content.Context
import com.droidbaza.spotifycompose.network.Network
import com.droidbaza.spotifycompose.network.auth.TokenStoreContract
import com.droidbaza.spotifycompose.network.auth.TokenStores

class AuthRepository(
    private val context: Context,
    private val authApi: AuthApi = Network.retrofit().create(AuthApi::class.java),
    private val tokenStore: TokenStoreContract = TokenStores.get(context)
) {
    suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        val res = authApi.login(LoginBody(email, password))
        val access = res.data?.session?.access_token
        val refresh = res.data?.session?.refresh_token
        tokenStore.setTokens(access, refresh)
    }
}
