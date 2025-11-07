package com.droidbaza.spotifycompose.data.auth

import retrofit2.http.Body
import retrofit2.http.POST

data class LoginBody(val email: String, val password: String)

data class AuthUser(val id: String?, val email: String?)

data class Profile(val id: String?, val username: String?, val display_name: String?)

data class LoginSession(val access_token: String?, val refresh_token: String?)

data class LoginData(val user: AuthUser?, val session: LoginSession?, val profile: Profile?)

data class ApiResponse<T>(val success: Boolean, val data: T?)

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body body: LoginBody): ApiResponse<LoginData>
}
