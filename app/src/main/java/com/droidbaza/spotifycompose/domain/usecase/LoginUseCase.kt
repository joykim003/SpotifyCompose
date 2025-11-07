package com.droidbaza.spotifycompose.domain.usecase

import android.content.Context
import com.droidbaza.spotifycompose.data.auth.AuthRepository

class LoginUseCase(private val context: Context) {
    private val repo = AuthRepository(context)
    suspend operator fun invoke(email: String, password: String): Result<Unit> =
        repo.login(email, password)
}
