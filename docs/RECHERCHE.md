# Travaux de recherche (synthèse)

**Relations JPA (§4)** — `Incident` → `User` : `@ManyToOne(fetch = LAZY)` pour `createdBy` (obligatoire) et `assignedTo` (nullable). LAZY car on n'a pas besoin de charger l'utilisateur à chaque lecture d'incident ; EAGER provoquerait des jointures inutiles (problème N+1). Le côté `@OneToMany` n'est pas modélisé sur `User` : inutile, et il alourdirait la sérialisation ; on interroge par `findByCreatedById...`. Les DTO sont construits dans des méthodes `@Transactional` (`open-in-view=false`) pour éviter les `LazyInitializationException`.

**JWT (§6)** — Un *claim* est une paire clé/valeur du payload. Standards : `sub`, `iat`, `exp`. Personnalisé : `role` (`.claim("role", ...)`), relu avec `claims.get("role", String.class)`. L'expiration est vérifiée automatiquement par `Jwts.parser()...parseSignedClaims()` (lève `ExpiredJwtException`). Les autorités réellement appliquées sont relues en base, le claim `role` sert surtout au client.

**Rôles (§9)** — `hasRole('ADMIN')` ajoute le préfixe `ROLE_`. Règles d'URL dans `SecurityConfig` (`/api/admin/**`) + `@PreAuthorize` activé par `@EnableMethodSecurity` : défense en profondeur.

**Propriétaire (§12)** — `Principal`/`Authentication` (issu de `SecurityContextHolder`) donne l'email ; le service compare l'id du créateur avec celui de l'utilisateur connecté et lève 403 sinon.

**Validation / exceptions (§13-14)** — `@Valid` + `@NotBlank/@Email/@Size` → `MethodArgumentNotValidException` → 400. `@RestControllerAdvice` + `@ExceptionHandler` centralisent les réponses `{status, message, ...}`. 400 = requête invalide ; 401 = non authentifié (token absent/expiré) ; 403 = authentifié mais interdit ; 404 = ressource inexistante.

**Refresh token (§23)** — L'access token est court (5 min) : s'il est volé, la fenêtre d'abus est courte. Le refresh token est long (7 jours), stocké en base, donc révocable. `/auth/refresh` émet un nouvel access token ; `/auth/logout` supprime le refresh token. Axios : `interceptors.response.use` rejoue la requête après refresh, sinon retour au login.
