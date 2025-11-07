![](art/header.png)

# Spotify Compose (:construction:work in progres :construction:)
 Spotify UI built with Jetpack Compose. Warning navigation extension functions used in this project is deprecated. More powerfull and valid way descripted in navigationBooster project: (https://github.com/droidbaza/NavigationBooster) 

# Try now

[![SpotifyCompose](https://github.com/droidbaza/SpotifyCompose/blob/master/app/src/main/res/mipmap-hdpi/ic_launcher.png)](https://github.com/droidbaza/SpotifyCompose/raw/master/app/release/app-release.apk)
👈 click to download :rocket::rocket::rocket:

# Preview
![SpotifyCompose](art/preview.gif)

## :fire::fire::fire: Useful extensions


```kotlin
/**
 * rounding.
 */
fun Modifier.round(
        radius: Dp? = null,
        percent: Int = 0
    ): Modifier {
        return if (radius != null) {
            this.clip(RoundedCornerShape(radius))
        } else {
            this.clip(RoundedCornerShape(percent))
        }
    }
    
fun Modifier.round(
        percent: Int = 0
    ): Modifier {
        return this.clip(RoundedCornerShape(percent = percent))
    }
```

## Intégration backend Express (API v1)

- Base URL (debug): `https://1cfa5e41c4c9.ngrok-free.app/api/v1/`
- Spec OpenAPI: `https://1cfa5e41c4c9.ngrok-free.app/api-docs.json`
- Auth: JWT Bearer (Supabase). Header `Authorization: Bearer <access_token>`.

### Génération du client OpenAPI (Kotlin + Retrofit)

1) Mettre à jour la spec locale (Windows / PowerShell):
```
Invoke-WebRequest -Uri "https://1cfa5e41c4c9.ngrok-free.app/api-docs.json" -Headers @{ "ngrok-skip-browser-warning"="true" } -OutFile "app/src/main/openapi/api-docs.json"
```
2) Générer le client:
```
./gradlew openApiGenerate
```
3) Les sources générées: `app/build/generated/openapi/src/main/kotlin/...`

### Configuration BASE_URL (BuildConfig)

- Définir `BuildConfig.BASE_URL` par buildType (exemple):
  - debug: `https://1cfa5e41c4c9.ngrok-free.app/api/v1/`
  - release: `https://api.spotify-clone.com/api/v1/`
- Utiliser `BuildConfig.BASE_URL` dans la fabrique Retrofit.

### Authentification & refresh

- Stocker `access_token` et `refresh_token` (DataStore ou EncryptedSharedPreferences).
- Interceptor: ajoute le header `Authorization` si token présent.
- Authenticator: sur 401 → `POST /auth/refresh` avec `refresh_token`, met à jour les tokens et rejoue une fois la requête.

### Lancer le projet

1) Ouvrir dans Android Studio (JDK 17).
2) Générer le client OpenAPI (voir ci-dessus).
3) Exécuter l’appli (émulateur ou device).

### Dépannage (ngrok)

- Si la spec renvoie une page d’avertissement ngrok, ajouter l’en-tête `ngrok-skip-browser-warning: true` lors du téléchargement.
- En émulateur Android, pour un backend local, utiliser `http://10.0.2.2:3000/api/v1`.

### Structure du client généré

- APIs: `com.droidbaza.spotifycompose.network.generated.api`
- Modèles: `com.droidbaza.spotifycompose.network.generated.model`
- Infra (Retrofit/Auth/Serializer): `com.droidbaza.spotifycompose.network.generated.infrastructure`

### Liens utiles

- Swagger UI: `/api-docs`
- OpenAPI JSON: `/api-docs.json`

```kotlin
/**
 * change color.
 */
fun Modifier.color(
        colorHex: Int = 0
    ): Modifier {
        return this.background(Color(colorHex))
    }

fun Modifier.color(
        color: Color = Color.Transparent
    ): Modifier {
        return this.background(color)
    }
```

```kotlin
/**
 * gradient color.
 */
 enum class GradientType {
        HORIZONTAL, VERTICAL, LINEAR, RADIAL, SWEEP
    }
    
 fun Modifier.gradient(
        colors: List<Color>,
        gradientType: GradientType = GradientType.LINEAR
    ): Modifier {
        return this.background(
            brush = when (gradientType) {
                GradientType.HORIZONTAL -> {
                    Brush.horizontalGradient(colors = colors)
                }
                GradientType.VERTICAL -> {
                    Brush.verticalGradient(colors = colors)
                }
                GradientType.LINEAR -> {
                    Brush.linearGradient(colors = colors)
                }
                GradientType.RADIAL -> {
                    Brush.radialGradient(colors = colors)
                }
                GradientType.SWEEP -> {
                    Brush.sweepGradient(colors = colors)
                }
            }
        )
    }
```

