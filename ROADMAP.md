# ROADMAP — SpotifyCompose (Intégration API et fonctionnalités)

## Objectifs
- Connecter proprement le frontend Kotlin (Compose) à l’API Express `/api/v1`.
- Stabiliser l’authentification (JWT + refresh) et la pagination.
- Préparer le socle pour lecture audio (ExoPlayer + URL signées) et fonctionnalités sociales (playlists, likes, follow).

## Périmètre court terme (Semaine 1)
- Intégration OpenAPI (FAIT): spec localisée et client Kotlin généré.
- Config `BuildConfig.BASE_URL` (debug: ngrok, release: prod) et branchement dans Retrofit.
- Interceptor Bearer + Authenticator (401 -> POST `/auth/refresh`) + stockage sécurisé des tokens (DataStore / EncryptedSharedPreferences).
- Endpoints prioritaires: `POST /auth/login`, `GET /auth/me`, `GET /tracks` (pagination), `GET /search`, `GET /albums`, `GET /artists`.
- Démo UI minimale: login -> affichage feed de tracks paginés.

## Périmètre moyen terme (Semaine 2)
- Repositories & UseCases pour domaines: Auth, Tracks, Playlists, Artists, Search.
- Actions utilisateur: like/unlike track, follow/unfollow artist, CRUD playlists (create, update, delete, add/remove track).
- Playback: intégration ExoPlayer + récupération `GET /upload/tracks/{id}/url` avant lecture.
- Gestion d’erreurs: standardisation (toast/snackbar), 401/403/429.
- Tests: MockWebServer (login, refresh, tracks paginés), instrumentation légère.

## Périmètre plus long terme (Semaine 3+)
- Mise en cache (Room + réconciliation) pour listes et détails.
- Offline first basique (affichage dernier état connu, retry réseau).
- Optimisations UI (lazy lists, placeholders, shimmer, palette couleurs via Coil/Palette).
- Feature discovery: featured playlists, catégories browse.
- Observabilité: logs réseau, métriques clé (cold start, ANR, crashes).

## Détails techniques
- OpenAPI: utiliser le client généré (`com.droidbaza.spotifycompose.network.generated.*`).
- Retrofit/OkHttp: 
  - Interceptor: ajout du header `Authorization` si token présent.
  - Authenticator: 1 seul retry après `POST /auth/refresh`.
- Pagination: utiliser `limit`/`offset`; mapper la réponse `{ success, data: { items, total, ... } }` vers modèles UI.
- Sécurité: ne jamais logguer les tokens, privilégier EncryptedSharedPreferences si possible.

## Tâches détaillées
- BuildConfig
  - Ajouter `BASE_URL` pour `debug` et `release`.
  - Remplacer la constante hardcodée dans `Network.kt` par `BuildConfig.BASE_URL`.
- Auth
  - Créer `TokenStore` (DataStore prefs) + `AuthInterceptor` + `TokenAuthenticator`.
  - Écran Login: appel `login`, persister tokens, naviguer vers Home.
- Data
  - Brancher `TracksApi`, `AlbumsApi`, `ArtistsApi`, `PlaylistsApi`, `SearchApi` générés dans des repositories.
  - Mapper les DTOs vers modèles UI (simples data classes côté présentation).
- Playback
  - Service `PlaybackRepository`: `getSignedUrl(trackId)` -> ExoPlayer.
- UI
  - Home feed (tracks paginés), Track details, Profile minimal.

## Tests & Qualité
- Unitaires: repositories (mapping + erreurs), auth refresh flow.
- Réseau: MockWebServer scénarios succès/401->refresh/erreur.
- Lint: ktlint déjà configuré.
- CI (plus tard): Gradle build + tests unitaires.

## Risques & Mitigation
- URL ngrok instable: documenter procédure de mise à jour rapide de `BASE_URL`.
- Incompatibilités spec -> client: préférer endpoints critiques en handwritten si besoin.
- Gestion refresh: éviter boucles; limiter à 1 retry.

## Suivi
- Créer issues GitHub par tâche ci-dessus.
- Utiliser cette ROADMAP comme référence et la mettre à jour à chaque jalon.

---

## Checklist d'avancement

- [x] OpenAPI intégré: spec téléchargée et client Kotlin généré (`openApiGenerate`).
- [x] BASE_URL configurée (debug/release) et utilisée dans `Network.kt`.
- [x] Auth branchée: `TokenStore` (DataStore), `AuthInterceptor`, `TokenAuthenticator` (1 retry).
- [x] Fabrique Retrofit pour APIs générées (`ApiFactory`).
- [x] Repositories créés: `AuthRepository`, `TracksRepository` (mapper `{ success, data }`).
- [x] UseCases: `LoginUseCase`, `GetPagedTracksUseCase`.
- [x] UI demo: écran Login → persistance tokens → navigation Home.
- [ ] UI demo: feed de tracks paginés (limit/offset, hasMore).
- [~] Tests MockWebServer: header Bearer (OK) et 401→refresh→retry (ébauche). À compléter.
- [ ] Tests MockWebServer: login OK/KO (AuthenticationApi).
- [ ] Tests MockWebServer: pagination tracks OK/KO (TracksApi).
- [ ] Playback: `GET /upload/tracks/{id}/url` → ExoPlayer.
- [ ] Gestion d’erreurs: standardisation messages (401/403/429), UX toasts.

Notes:
- Marquer chaque item dès qu’il est livré (cocher la case et ajouter une courte note si nécessaire).
- 2025-11-07: Repos & UseCases livrés (auth login + tracks paginés).
- 2025-11-07: Navigation RootScreen (Login → Home) livrée; tests MockWebServer ajoutés (certains ignorés en attendant TokenStore injectable en test).
