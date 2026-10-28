# Gestion des incidents — Spring Security + JWT + React

Application de déclaration et de suivi d'incidents informatiques / cybersécurité.
Rôles : **EMPLOYEE**, **TECHNICIAN**, **ADMIN**.

```
incident-app/
├── backend/    Spring Boot 3.3 (Java 17), Spring Security, JWT (jjwt 0.12), JPA, H2 / MySQL
├── frontend/   React 18 + Vite, React Router, Axios
├── docs/       diagrammes (entités, architecture) + réponses aux "travaux de recherche"
├── scripts/    scénario de test + tests de sécurité (curl)
└── _previous_backend_attempt/   ancienne version du backend (non utilisée, conservée par prudence)
```

## Lancer le backend (port 8080)
```bash
cd backend
mvn spring-boot:run
```
Base H2 en mémoire par défaut (console : http://localhost:8080/h2-console, JDBC URL `jdbc:h2:mem:incidentdb`, user `sa`, mot de passe vide).
Pour MySQL : décommenter le bloc MySQL dans `backend/src/main/resources/application.properties`.

Paramètres JWT (`application.properties`) : `app.jwt.access-expiration-ms=300000` (5 min, pratique pour tester l'expiration), `app.jwt.refresh-expiration-ms` (7 jours).

## Lancer React (port 5173)
```bash
cd frontend
npm install
npm run dev
```
Ouvrir http://localhost:5173 (CORS autorise uniquement cette origine, voir `app.cors.allowed-origin`).

## Ports
| Service | Port |
|---|---|
| Spring Boot | 8080 |
| React (Vite) | 5173 |

## Comptes de test (créés au démarrage)
| Rôle | Email | Mot de passe |
|---|---|---|
| ADMIN | admin@test.com | Admin123! |
| TECHNICIAN | tech@test.com | Tech1234! |
| TECHNICIAN | tech2@test.com | Tech1234! |
| EMPLOYEE | emp1@test.com | Employee1! |
| EMPLOYEE | emp2@test.com | Employee1! |

`/auth/register` crée toujours un EMPLOYEE (le rôle n'est jamais lu dans la requête).

## Endpoints principaux
| Méthode | URL | Accès |
|---|---|---|
| POST | /auth/register, /auth/login, /auth/refresh, /auth/logout | public |
| GET / PUT | /api/profile | authentifié |
| PUT | /api/profile/password | authentifié (ancien + nouveau + confirmation) |
| POST | /api/incidents | EMPLOYEE |
| GET | /api/incidents/my | EMPLOYEE |
| GET | /api/incidents/assigned | TECHNICIAN |
| GET | /api/incidents | ADMIN |
| GET | /api/incidents/{id} | propriétaire, technicien affecté ou ADMIN (détail + historique + notes) |
| PUT | /api/incidents/{id}/status | TECHNICIAN (ses incidents) ou ADMIN |
| PUT | /api/incidents/{id}/assign/{technicianId} | ADMIN |
| POST | /api/incidents/{id}/comments | TECHNICIAN (ses incidents) ou ADMIN |
| GET | /api/dashboard | selon le rôle |
| GET | /api/admin/users[?role=TECHNICIAN] | ADMIN |
| PUT | /api/admin/users/{id}/enable \| disable | ADMIN |
| GET | /api/admin/statistics | ADMIN |

## Sécurité mise en place
- **JWT** (HS256) : `sub`=email, `role`, `iat`, `exp`. Filtre `JwtFilter` → `SecurityContext`. Les droits sont relus en base à chaque requête : un compte désactivé est refusé immédiatement.
- **Stateless** (`SessionCreationPolicy.STATELESS`), CSRF désactivé (pas de cookie de session), **CORS** limité à `http://localhost:5173`.
- Routes publiques : `/auth/**`, `/h2-console/**` ; `/api/**` protégé ; `/api/admin/**` = ADMIN.
- Autorisation par rôle : règles d'URL (`hasRole`) **et** `@PreAuthorize` + `@EnableMethodSecurity`.
- **Contrôle du propriétaire** (anti-IDOR) dans `IncidentService.findViewable` : `GET /api/incidents/12` → 403 si l'incident n'est pas à l'utilisateur connecté.
- Mots de passe **BCrypt** ; validation Bean Validation (`@NotBlank`, `@Email`, `@Size`, `@Valid`).
- Erreurs JSON uniformes (`@RestControllerAdvice`) : 400 données invalides, 401 non authentifié / token expiré, 403 accès interdit / compte désactivé, 404 introuvable, 409 email déjà utilisé.
- Règle métier : incident `CLOSED` non modifiable sauf par un ADMIN.
- **Refresh token** : `/auth/login` renvoie access + refresh ; `/auth/refresh` renouvelle l'access token ; `/auth/logout` supprime le refresh token.
- Côté React : intercepteur Axios (ajout du Bearer, refresh automatique sur 401, sinon suppression du JWT + redirection vers `/login` avec message « session expirée »), `ProtectedRoute`, `RoleRoute`.

## Fonctionnalités réalisées
Inscription / connexion, profil + changement de mot de passe, déclaration et suivi d'incidents, affectation, changement de statut, notes de traitement (`IncidentComment`), historique (`IncidentHistory`), dashboards par rôle, administration des utilisateurs (activer / désactiver), statistiques globales, refresh token.

## Tests
- `scripts/scenario.sh` : scénario du §24 (2 employés, admin, technicien…).
- `scripts/security-tests.sh` : les 9 tests de sécurité du §25 (à rejouer aussi dans Postman pour les captures).
```bash
cd backend && mvn spring-boot:run     # terminal 1
bash scripts/security-tests.sh        # terminal 2 (pour le JWT expiré : attendre 5 min, ou mettre une durée de 5000 ms)
```
