### Guides

- [Lecture privée via URLs signées (tracks)](./docs/signed-urls-guide.md)

# 🎵 Spotify Clone Backend API

API backend personnalisée pour le clone Spotify, utilisant Supabase pour l'authentification, la base de données PostgreSQL et le stockage de fichiers.

## 🚀 Stack technique

- **Runtime**: Node.js 20+
- **Framework**: Express.js + TypeScript
- **Base de données**: PostgreSQL (Supabase)
- **Authentification**: Supabase Auth (JWT)
- **Stockage**: Supabase Storage
- **ORM**: Supabase Client

## 📋 Prérequis

- Node.js >= 20
- npm ou yarn
- Compte Supabase (gratuit)

## 🔧 Installation

### 1. Installer les dépendances

```bash
cd backend
npm install
```

### 2. Configuration Supabase

1. Créez un compte sur [supabase.com](https://supabase.com)
2. Créez un nouveau projet
3. Notez votre **URL du projet** et vos **clés API**

### 3. Configuration de la base de données

1. Allez dans l'éditeur SQL de Supabase
2. Copiez et exécutez le contenu de `database/schema.sql`
3. Vérifiez que toutes les tables sont créées

### 4. Configuration des buckets de stockage

Dans Supabase Storage, créez les buckets suivants:

- `tracks` (pour les fichiers audio)
- `covers` (pour les images d'albums/playlists)
- `avatars` (pour les photos de profil)

Configurez les buckets en **public** pour les covers et avatars.

### 5. Variables d'environnement

Créez un fichier `.env` à partir de `.env.example`:

```bash
cp .env.example .env
```

Remplissez les variables:

```env
PORT=3000
NODE_ENV=development

SUPABASE_URL=https://votre-projet.supabase.co
SUPABASE_ANON_KEY=votre-anon-key
SUPABASE_SERVICE_ROLE_KEY=votre-service-role-key

ALLOWED_ORIGINS=http://localhost:8081,exp://localhost:8081
```

## 🏃 Démarrage

### Mode développement

```bash
npm run dev
```

Le serveur démarre sur `http://localhost:3000`

### Mode production

```bash
npm run build
npm start
```

## 📚 Documentation API

### Base URL

```
http://localhost:3000/api/v1
```

### Endpoints principaux

#### Authentification

```
POST   /auth/register          # Inscription
POST   /auth/login             # Connexion
POST   /auth/logout            # Déconnexion
POST   /auth/refresh           # Rafraîchir le token
GET    /auth/me                # Profil actuel
```

#### Morceaux (Tracks)

```
GET    /tracks                 # Liste des morceaux
GET    /tracks/:id             # Détails d'un morceau
GET    /tracks/liked           # Morceaux likés
GET    /tracks/check?ids=...   # Vérifier si likés
PUT    /tracks/:id/like        # Liker un morceau
DELETE /tracks/:id/like        # Unliker un morceau
```

#### Albums

```
GET    /albums                 # Liste des albums
GET    /albums/:id             # Détails d'un album
GET    /albums/:id/tracks      # Morceaux d'un album
GET    /albums/saved           # Albums sauvegardés
GET    /albums/check?ids=...   # Vérifier si sauvegardés
PUT    /albums/:id/save        # Sauvegarder un album
DELETE /albums/:id/save        # Retirer un album
GET    /albums/recently-played # Albums récemment écoutés
```

#### Artistes

```
GET    /artists                # Liste des artistes
GET    /artists/:id            # Détails d'un artiste
GET    /artists/:id/tracks     # Morceaux d'un artiste
GET    /artists/:id/albums     # Albums d'un artiste
GET    /artists/followed       # Artistes suivis
PUT    /artists/:id/follow     # Suivre un artiste
DELETE /artists/:id/follow     # Ne plus suivre
```

#### Playlists

```
GET    /playlists              # Playlists publiques
GET    /playlists/me           # Mes playlists
POST   /playlists              # Créer une playlist
GET    /playlists/:id          # Détails d'une playlist
PUT    /playlists/:id          # Modifier une playlist
DELETE /playlists/:id          # Supprimer une playlist
GET    /playlists/:id/tracks   # Morceaux d'une playlist
POST   /playlists/:id/tracks   # Ajouter un morceau
DELETE /playlists/:id/tracks/:trackId  # Retirer un morceau
```

#### Utilisateurs

```
GET    /users/:id              # Profil utilisateur
PUT    /users/me/profile       # Modifier son profil
GET    /users/me/history       # Historique d'écoute
POST   /users/me/history       # Ajouter à l'historique
GET    /users/me/top/tracks    # Top morceaux
GET    /users/me/top/artists   # Top artistes
GET    /users/me/top/albums    # Top albums
```

#### Recherche

```
GET    /search?q=query&type=track,artist,album,playlist
GET    /search/categories
GET    /search/featured-playlists
```

#### Upload

```
POST   /upload/audio             # Uploader un fichier audio (auth + rôle artist)
POST   /upload/cover             # Uploader une image de cover (auth + rôle artist)
POST   /upload/avatar            # Uploader un avatar utilisateur (auth + rôle artist)
GET    /upload/tracks/:id/url    # Récupérer une URL signée pour un morceau
```

#### Health

```
GET    /health                   # Vérifier l'état de l'API
```

#### Admin

```
POST   /admin/users/:id/promote  # Promouvoir un utilisateur (nécessite un token admin)
```

### Authentification

Toutes les routes protégées nécessitent un header `Authorization`:

```
Authorization: Bearer <votre_token_jwt>
```

### Réponses API

Format de réponse standard:

```json
{
  "success": true,
  "data": { ... },
  "message": "Optional message"
}
```

Format d'erreur:

```json
{
  "success": false,
  "error": "Error message"
}
```

### Pagination

Les endpoints de liste supportent la pagination:

```
?limit=20&offset=0
```

Réponse paginée:

```json
{
  "success": true,
  "data": {
    "items": [...],
    "total": 100,
    "limit": 20,
    "offset": 0,
    "hasMore": true
  }
}
```

## 🧪 Tests

```bash
npm test
```

## 📦 Structure du projet

```
backend/
├── src/
│   ├── config/          # Configuration (Supabase, etc.)
│   ├── controllers/     # Logique métier
│   ├── middleware/      # Middleware Express
│   ├── routes/          # Définition des routes
│   ├── services/        # Services (upload, streaming)
│   ├── types/           # Types TypeScript
│   ├── utils/           # Utilitaires
│   └── index.ts         # Point d'entrée
├── database/
│   └── schema.sql       # Schéma de base de données
├── .env.example         # Template variables d'environnement
├── package.json
├── tsconfig.json
└── README.md
```

## 🔒 Sécurité

- **Helmet**: Protection contre les vulnérabilités web courantes
- **CORS**: Configuration stricte des origines autorisées
- **Rate Limiting**: Limitation du nombre de requêtes par IP
- **JWT**: Authentification sécurisée via Supabase
- **RLS**: Row Level Security sur PostgreSQL

## 🚀 Déploiement

### Railway / Render / DigitalOcean

1. Créez un nouveau projet
2. Connectez votre repo GitHub
3. Configurez les variables d'environnement
4. Déployez

### Variables d'environnement en production

```env
NODE_ENV=production
PORT=3000
SUPABASE_URL=...
SUPABASE_ANON_KEY=...
SUPABASE_SERVICE_ROLE_KEY=...
ALLOWED_ORIGINS=https://votre-app.com
```

## 📝 TODO

- [ ] Ajouter le streaming audio
- [ ] Implémenter l'upload de fichiers
- [ ] Ajouter les recommandations
- [ ] Ajouter les tests unitaires
- [ ] Documenter avec Swagger
- [ ] Ajouter le cache Redis
- [ ] Implémenter les WebSockets pour le temps réel

## 🤝 Contribution

1. Fork le projet
2. Créez une branche (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 Licence

MIT

## 👥 Auteurs

Équipe Spotify Clone
