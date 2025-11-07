# Guide Frontend Kotlin (Android) – Intégration de l’API Spotify Clone

Ce guide explique comment connecter une app Android (Kotlin) à l’API backend Express/TypeScript.

## Pré-requis

- Android Studio Hedgehog+ (ou plus récent)
- Min SDK conseillé: 24+
- Kotlin 1.9+
- API backend accessible (localhost, ngrok ou déploiement)

## Base URL
- Json openapi docs 'https://1cfa5e41c4c9.ngrok-free.app/api-docs.json'
- Dev local: `http://10.0.2.2:3000/api/v1` (émulateur Android)
- Via ngrok: `'https://1cfa5e41c4c9.ngrok-free.app/api/v1`
- Prod: `https://votre-domaine.com/api/v1`

Astuce: exposez l’API locale via `ngrok http 3000` et ajoutez le domaine ngrok à `ALLOWED_ORIGINS` côté backend (ou `*` en dev).

## Authentification

- Type: JWT Bearer (géré par Supabase)
- Header: `Authorization: Bearer <access_token>`
- Flux standard:
  1) `POST /auth/login` → récupère `{ session.access_token, session.refresh_token }`
  2) Utiliser `access_token` dans les appels protégés
  3) Sur 401 (token expiré) → `POST /auth/refresh` avec `refresh_token`, puis rejouer la requête

Endpoints clés:
- `POST /auth/register`
- `POST /auth/login`
- `POST /auth/refresh`
- `GET /auth/me`

## Dépendances Gradle (Module)

```gradle
dependencies {
  implementation platform("com.squareup.okhttp3:okhttp-bom:4.12.0")
  implementation "com.squareup.okhttp3:okhttp"
  implementation "com.squareup.okhttp3:logging-interceptor"

  implementation "com.squareup.retrofit2:retrofit:2.11.0"
  implementation "com.squareup.retrofit2:converter-moshi:2.11.0"
  implementation "com.squareup.moshi:moshi-kotlin:1.15.1"
  // Optional: KotlinX Serialization si vous préférez
}
```

## Configuration Retrofit/OkHttp

```kotlin
object ApiConfig {
  private const val BASE_URL = "https://<VOTRE_NGROK>.ngrok.io/api/v1/" // ou 10.0.2.2:3000

  @Volatile
  private var accessToken: String? = null
  @Volatile
  private var refreshToken: String? = null

  fun setTokens(access: String?, refresh: String?) {
    accessToken = access
    refreshToken = refresh
  }

  private val authInterceptor = Interceptor { chain ->
    val original = chain.request()
    val token = accessToken
    val req = if (!token.isNullOrBlank()) {
      original.newBuilder()
        .addHeader("Authorization", "Bearer $token")
        .build()
    } else original
    chain.proceed(req)
  }

  private val logging = HttpLoggingInterceptor().apply {
    level = HttpLoggingInterceptor.Level.BODY
  }

  // Rafraîchit automatiquement le token sur 401, puis rejoue 1 fois
  private val tokenAuthenticator = Authenticator { route, response ->
    // Evite boucle infinie
    if (responseCount(response) >= 2) return@Authenticator null

    val currentRefresh = refreshToken ?: return@Authenticator null
    val newSession = RefreshService.create().refresh(RefreshBody(currentRefresh)).execute()
    if (!newSession.isSuccessful) return@Authenticator null

    val session = newSession.body()?.data?.session
    val newAccess = session?.access_token
    val newRefresh = session?.refresh_token
    if (newAccess.isNullOrBlank()) return@Authenticator null

    setTokens(newAccess, newRefresh)

    response.request.newBuilder()
      .header("Authorization", "Bearer $newAccess")
      .build()
  }

  private fun responseCount(response: Response): Int {
    var result = 1
    var r: Response? = response.priorResponse
    while (r != null) { result++; r = r.priorResponse }
    return result
  }

  private val okHttp = OkHttpClient.Builder()
    .addInterceptor(authInterceptor)
    .addInterceptor(logging)
    .authenticator(tokenAuthenticator)
    .build()

  val retrofit: Retrofit = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .client(okHttp)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
}
```

Services utilisés par l’authenticator (refresh):

```kotlin
data class RefreshBody(val refresh_token: String)

data class SessionDTO(val access_token: String?, val refresh_token: String?)

data class RefreshResponse(val success: Boolean, val data: Data?) {
  data class Data(val session: SessionDTO?)
}

interface RefreshApi {
  @POST("auth/refresh")
  fun refresh(@Body body: RefreshBody): Call<RefreshResponse>
}

object RefreshService {
  fun create(): RefreshApi = Retrofit.Builder()
    .baseUrl(ApiConfig.BASE_URL)
    .addConverterFactory(MoshiConverterFactory.create())
    .build()
    .create(RefreshApi::class.java)
}
```

## Définir l’API principale

Exemple minimal pour quelques endpoints:

```kotlin
data class LoginBody(val email: String, val password: String)

data class AuthUser(val id: String?, val email: String?)

data class Profile(val id: String?, val username: String?, val display_name: String?)

data class LoginSession(val access_token: String?, val refresh_token: String?)

data class LoginData(val user: AuthUser?, val session: LoginSession?, val profile: Profile?)

data class ApiResponse<T>(val success: Boolean, val data: T?, val message: String?, val error: String?)

interface ApiService {
  @POST("auth/login")
  fun login(@Body body: LoginBody): Call<ApiResponse<LoginData>>

  @GET("auth/me")
  fun me(): Call<ApiResponse<Profile>>

  @GET("tracks")
  fun tracks(@Query("limit") limit: Int = 20, @Query("offset") offset: Int = 0): Call<ApiResponse<Paged<Track>>>
}

// Exemples de modèles simplifiés

data class Track(val id: String, val title: String?, val duration_ms: Long?)

data class Paged<T>(val items: List<T>?, val total: Int?, val limit: Int?, val offset: Int?, val hasMore: Boolean?)
```

Factory Retrofit principal:

```kotlin
object ApiServiceFactory {
  val api: ApiService = ApiConfig.retrofit.create(ApiService::class.java)
}
```

## Flux de connexion (exemple)

```kotlin
val res = ApiServiceFactory.api.login(LoginBody(email, password)).execute()
if (res.isSuccessful) {
  val session = res.body()?.data?.session
  ApiConfig.setTokens(session?.access_token, session?.refresh_token)
}
```

## Gestion des erreurs

- Réponses d’erreur backend: `{ success: false, error: "message" }`
- Sur 401, l’`Authenticator` tente un refresh automatique puis rejoue la requête.
- Journalisez les erreurs réseau (OkHttp logging) en dev.

## Pagination

- Les listes renvoient une structure paginée `{ items, total, limit, offset, hasMore }`.
- Utiliser `limit` et `offset` dans les requêtes (`/tracks`, `/albums`, etc.).

## CORS et Origine

- En dev, ajoutez votre domaine ngrok à `ALLOWED_ORIGINS` côté backend.
- Pour l’émulateur: utilisez `http://10.0.2.2:3000`.

## Checklist d’intégration

- [ ] Configurer la `BASE_URL` (local/ngrok/prod)
- [ ] Implémenter la persistance sécurisée des tokens (EncryptedSharedPreferences/Datastore)
- [ ] Relancer les requêtes échouées après refresh
- [ ] Gérer la déconnexion (vider tokens, navigation)
- [ ] Surveiller les 401/403/429 et informer l’utilisateur

## Ressources

- Swagger UI: `/api-docs`
- Spec JSON: `/api-docs.json`
- Backend README: `backend/README.md`
