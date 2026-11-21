# UniqueVerse — Sprint 5 Notes
## Part 2: Authorization, Roles, Ownership & Favorites

> **Sprint:** 5  
> **Focus:** Authorization, roles, protected resources, Favorites ownership, security boundaries  
> **Project:** UniqueVerse  
> **Status:** Completed

---

# 1. Sprint 5 Part 2 Objective

Part 1 established:

```text
Authentication
JWT
UserDetails
SecurityContext
```

Part 2 answers the next question:

> **Now that we know who the user is, what is that user allowed to do?**

The final security model became:

```text
                    UniqueVerse API
                         |
             +-----------+-----------+
             |                       |
          PUBLIC                 PROTECTED
             |                       |
        Planet reads            Auth required
        APOD                    Favorites
                                /auth/me
             |
        ADMIN ONLY
             |
    Planet create/update/delete
```

---

# 2. Authentication vs Authorization

The distinction is fundamental.

### Authentication

```text
"Who are you?"
```

Handled using:

```text
JWT
SecurityContext
UserDetails
```

### Authorization

```text
"What are you allowed to do?"
```

Handled using:

```text
SecurityConfig
roles
authorities
endpoint rules
resource ownership
```

Example:

```text
USER
  |
  +--> GET /planets              allowed
  +--> GET /favorites            allowed
  +--> POST /planets             forbidden

ADMIN
  |
  +--> GET /planets              allowed
  +--> GET /favorites            allowed
  +--> POST /planets             allowed
```

---

# 3. UniqueVerse Authorization Matrix

The final intended access model is:

| Endpoint | Guest | USER | ADMIN |
|---|---:|---:|---:|
| `POST /auth/register` | Yes | Yes | Yes |
| `POST /auth/login` | Yes | Yes | Yes |
| `GET /auth/me` | No | Yes | Yes |
| `GET /planets/**` | Yes | Yes | Yes |
| `POST /planets/**` | No | No | Yes |
| `PUT /planets/**` | No | No | Yes |
| `DELETE /planets/**` | No | No | Yes |
| `/favorites/**` | No | Yes | Yes |

This became the security contract for Sprint 5.

---

# 4. Spring Security Endpoint Rules

The central authorization configuration is inside:

```java
SecurityConfig
```

Public authentication endpoints:

```java
.requestMatchers(
        "/api/v1/auth/register",
        "/api/v1/auth/login"
).permitAll()
```

Authenticated `/auth/me`:

```java
.requestMatchers("/api/v1/auth/me")
.authenticated()
```

Public planet reads:

```java
.requestMatchers(
        HttpMethod.GET,
        "/api/v1/planets/**"
).permitAll()
```

ADMIN-only planet writes:

```java
.requestMatchers(
        HttpMethod.POST,
        "/api/v1/planets/**"
).hasRole("ADMIN")

.requestMatchers(
        HttpMethod.PUT,
        "/api/v1/planets/**"
).hasRole("ADMIN")

.requestMatchers(
        HttpMethod.DELETE,
        "/api/v1/planets/**"
).hasRole("ADMIN")
```

Favorites require authentication:

```java
.requestMatchers("/api/v1/favorites/**")
.authenticated()
```

Everything else is denied:

```java
.anyRequest().denyAll()
```

---

# 5. Why `anyRequest().denyAll()` Matters

A dangerous approach is:

```java
.anyRequest().permitAll()
```

That effectively creates:

> "If I forgot to secure a new endpoint, everyone gets access."

Instead:

```java
.anyRequest().denyAll()
```

creates a safer default:

```text
New endpoint
     |
     v
No explicit security rule?
     |
     v
DENIED
```

This is a **fail-closed** approach.

---

# 6. `permitAll()` vs `authenticated()` vs `hasRole()`

These three are worth memorizing.

## `permitAll()`

Anyone can access the endpoint.

```java
.permitAll()
```

Example:

```text
POST /auth/login
GET /planets
```

---

## `authenticated()`

A valid authenticated identity is required.

```java
.authenticated()
```

Example:

```text
GET /auth/me
GET /favorites
```

---

## `hasRole("ADMIN")`

The authenticated user must have the ADMIN role.

```java
.hasRole("ADMIN")
```

Example:

```text
POST /planets
```

---

# 7. `401 Unauthorized` vs `403 Forbidden`

This was one of the most important concepts in Sprint 5.

## 401 Unauthorized

The request has **no valid authentication**.

Example:

```text
POST /api/v1/favorites
```

without a token.

Result:

```text
401 Unauthorized
```

Meaning:

```text
"You have not authenticated."
```

---

## 403 Forbidden

The user **is authenticated**, but does not have permission.

Example:

```text
USER token
    |
    v
POST /api/v1/planets
    |
    v
Requires ADMIN
    |
    v
403 Forbidden
```

Meaning:

```text
"I know who you are.
You simply aren't allowed to do this."
```

---

# 8. The Security Decision Tree

A useful mental model:

```text
Incoming request
       |
       v
Is authentication required?
       |
       +---- NO ---> allow
       |
       YES
       |
       v
Is user authenticated?
       |
       +---- NO ---> 401
       |
       YES
       |
       v
Does user have required authority?
       |
       +---- NO ---> 403
       |
       YES
       |
       v
Allow request
```

This distinction showed up repeatedly during manual Postman testing.

---

# 9. Roles

UniqueVerse currently has two application roles:

```text
USER
ADMIN
```

The database stores:

```text
USER
ADMIN
```

Spring Security receives:

```text
ROLE_USER
ROLE_ADMIN
```

because the application creates authorities using:

```java
new SimpleGrantedAuthority(
    "ROLE_" + user.getRole()
)
```

---

# 10. Why `hasRole("ADMIN")` Works

The configuration says:

```java
.hasRole("ADMIN")
```

Spring Security internally expects:

```text
ROLE_ADMIN
```

So:

```text
Database role
     |
     v
ADMIN
     |
     v
GrantedAuthority
     |
     v
ROLE_ADMIN
     |
     v
hasRole("ADMIN")
     |
     v
AUTHORIZED
```

This is a common Spring Security interview topic.

---

# 11. ADMIN Planet Operations

Planet writes are restricted:

```text
POST   /api/v1/planets
PUT    /api/v1/planets/{id}
DELETE /api/v1/planets/{id}
```

Only:

```text
ROLE_ADMIN
```

can perform them.

The security boundary exists before the controller/service business logic needs to execute.

Example:

```text
USER
 |
 +--> POST /planets
          |
          v
     SecurityFilterChain
          |
          v
      ROLE_ADMIN?
          |
          NO
          |
          v
         403
```

The request never needs to reach the planet creation service.

---

# 12. Why Authorization Should Be Enforced at the Security Layer

It would be dangerous to rely entirely on controller code such as:

```java
if (!user.isAdmin()) {
    ...
}
```

for every endpoint.

Spring Security provides a centralized security boundary.

```text
HTTP request
     |
     v
Security layer
     |
     +---- unauthorized/forbidden
     |
     v
Controller
     |
     v
Service
```

This keeps endpoint-level authorization explicit and centralized.

---

# 13. Favorites Introduce Resource Ownership

Roles alone were not enough.

Suppose:

```text
User A
Favorite #17
```

and:

```text
User B
```

User B is authenticated.

Does that mean User B can access:

```http
GET /api/v1/favorites/17
```

No.

Being authenticated only proves:

```text
"You are logged in."
```

It does **not** prove:

```text
"This resource belongs to you."
```

This introduced the concept of **resource ownership**.

---

# 14. Ownership Security

The Favorites model became:

```text
Favorite
   |
   +---- User
   |
   +---- Planet
```

So every favorite belongs to a specific user.

Conceptually:

```text
User A
  |
  +---- Favorite 1 ---- Planet Mars
  +---- Favorite 2 ---- Planet Earth

User B
  |
  +---- Favorite 3 ---- Planet Jupiter
```

User A must never be able to manipulate User B's favorites.

---

# 15. Favorite Entity

The Favorite entity contains:

```java
private User user;
private Planet planet;
```

So the database relationship is:

```text
users
  |
  | 1
  |
  | *
favorites
  |
  | *
  |
  | 1
planets
```

A favorite therefore identifies:

```text
which user
+
which planet
```

---

# 16. Why Username Is Passed From Authentication

The controller receives:

```java
Authentication authentication
```

and obtains:

```java
authentication.getName()
```

Example:

```java
@PostMapping
public FavoriteResponse createFavorite(
        @RequestParam Long planetId,
        Authentication authentication) {

    return favoriteService.createFavorite(
            planetId,
            authentication.getName()
    );
}
```

The username comes from the authenticated security context.

It does **not** come from a request body such as:

```json
{
    "username": "someoneElse"
}
```

That distinction prevents clients from simply claiming another user's identity.

---

# 17. Creating a Favorite

The request:

```http
POST /api/v1/favorites?planetId=3
Authorization: Bearer <JWT>
```

flows through:

```text
JWT
 |
 v
Authentication
 |
 v
username
 |
 v
FavoriteService
 |
 +---- find User
 |
 +---- find Planet
 |
 +---- create Favorite
 |
 v
Database
```

The resulting favorite belongs to the authenticated user.

---

# 18. Duplicate Favorite Protection

A user should not be able to favorite the same planet repeatedly.

Repository method:

```java
boolean existsByUserUsernameAndPlanetId(
        String username,
        Long planetId
);
```

Before creating the favorite:

```java
if (favoriteRepository
        .existsByUserUsernameAndPlanetId(
                username,
                planetId)) {

    throw new DuplicateResourceException(
            "Planet is already in favorites"
    );
}
```

The service therefore rejects duplicate requests.

---

# 19. Why the Database Also Needs a Constraint

Application-level checking alone is not enough.

Consider:

```text
Request A ---> check ---> doesn't exist
Request B ---> check ---> doesn't exist
```

Both requests could potentially pass the check before either inserts the row.

Therefore the database also enforces:

```sql
UNIQUE (user_id, planet_id)
```

This gives two layers of protection:

```text
Application
    |
    v
DuplicateResourceException
    |
    v
Database
    |
    v
UNIQUE(user_id, planet_id)
```

The application gives a clean API response.

The database provides the final integrity guarantee.

---

# 20. Duplicate Favorite Response

If a user favorites the same planet again:

```text
POST /favorites?planetId=3
```

the service detects the existing relationship.

The application throws:

```java
DuplicateResourceException
```

The global exception handler maps it to:

```text
409 Conflict
```

Conceptually:

```text
Existing favorite
       |
       v
Duplicate request
       |
       v
DuplicateResourceException
       |
       v
409 Conflict
```

---

# 21. Why 409?

`409 Conflict` communicates:

> The request conflicts with the current state of the resource.

The request itself may be syntactically valid.

The problem is that the requested state already exists.

---

# 22. Favorite List Ownership

The list endpoint:

```http
GET /api/v1/favorites
```

must return only the current user's favorites.

Repository query:

```java
@Query("""
    SELECT f
    FROM Favorite f
    JOIN FETCH f.planet
    WHERE f.user.username = :username
    """)
List<Favorite> findAllWithPlanetByUsername(
        @Param("username") String username
);
```

The important part:

```text
WHERE f.user.username = :username
```

This prevents:

```text
User A
  |
  +---- sees User B's favorites
```

Instead:

```text
User A ---> only User A favorites
User B ---> only User B favorites
```

---

# 23. Ownership on Individual Favorite Access

The same principle applies to:

```http
GET /api/v1/favorites/{id}
```

The repository query is scoped by both:

```text
favorite ID
+
username
```

Conceptually:

```sql
WHERE f.id = :id
AND f.user.username = :username
```

Therefore:

```text
User A requests Favorite 17
       |
       v
Does Favorite 17 exist?
       |
       v
Does it belong to User A?
       |
       +---- NO ---> 404
       |
       YES
       |
       v
Return favorite
```

---

# 24. Why Return 404 for Someone Else's Favorite?

The implementation intentionally does not reveal that another user's favorite exists.

Instead of:

```text
403 Forbidden
```

the ownership query returns no result:

```text
Favorite not found
```

and the service throws:

```java
ResourceNotFoundException
```

Result:

```text
404 Not Found
```

This gives the API a cleaner ownership boundary and avoids unnecessarily revealing the existence of another user's resource.

---

# 25. Favorite Deletion Ownership

Deletion uses:

```java
findByIdAndUsername(id, username)
```

rather than:

```java
findById(id)
```

This is critical.

Bad:

```java
Favorite favorite =
        favoriteRepository.findById(id)
```

That would locate the resource without checking ownership.

Correct:

```java
Favorite favorite =
        favoriteRepository
            .findByIdAndUsername(id, username)
```

The query ensures:

```text
favorite ID matches
AND
authenticated user owns it
```

---

# 26. Delete Flow

```text
DELETE /favorites/17
        |
        v
JWT authentication
        |
        v
username = mac
        |
        v
findByIdAndUsername(17, "mac")
        |
        +---- missing ---> 404
        |
        +---- found -----> delete
                              |
                              v
                             204
```

This is resource-level authorization.

---

# 27. `204 No Content`

Successful deletion returns:

```text
204 No Content
```

There is no response body because the resource has been successfully removed.

So:

```text
DELETE /favorites/17
        |
        v
204
```

is preferable to returning an unnecessary JSON object such as:

```json
{
    "message": "deleted"
}
```

---

# 28. Manual Ownership Verification

Sprint 5 ownership was manually tested using multiple users.

### User A

Created a favorite.

```text
User A
  |
  +---- Favorite #X
```

User A could:

```text
GET favorite
GET all favorites
DELETE favorite
```

---

### User B

Logged in separately.

User B received:

```text
GET /favorites
```

with an empty list because User B had no favorites.

User B could not access User A's favorite.

Result:

```text
404 Not Found
```

User B could not delete User A's favorite.

Result:

```text
404 Not Found
```

This confirmed the ownership boundary.

---

# 29. No Token Behavior

Requests to:

```text
/api/v1/favorites/**
```

without authentication result in:

```text
401 Unauthorized
```

The security boundary therefore works before the service layer is reached.

---

# 30. USER vs ADMIN Testing

The authorization matrix was manually verified.

### Guest

```text
GET /planets
```

Result:

```text
200
```

because planet reads are public.

---

### USER

```text
POST /planets
```

Result:

```text
403 Forbidden
```

because the USER is authenticated but lacks `ROLE_ADMIN`.

---

### ADMIN

```text
POST /planets
```

Result:

```text
200/201
```

depending on the controller response.

The important part is that the ADMIN passes the authorization check.

---

# 31. The Difference Between Role Authorization and Ownership

These are two different security problems.

## Role authorization

Question:

> Does this user have enough privilege to perform this operation?

Example:

```text
Can USER create a planet?

No.
```

Handled by:

```java
.hasRole("ADMIN")
```

---

## Ownership authorization

Question:

> Does this particular resource belong to this user?

Example:

```text
Can User B delete User A's favorite?

No.
```

Handled by:

```text
WHERE favorite.user.username = :username
```

These mechanisms solve different problems.

---

# 32. Database Design for Favorites

The final favorites table needs:

```text
id
planet_id
user_id
```

Foreign keys:

```text
planet_id -> planets.id
user_id   -> users.id
```

The user relationship also has:

```sql
ON DELETE CASCADE
```

So deleting a user can remove their dependent favorites.

---

# 33. Unique Constraint

The database contains:

```sql
CONSTRAINT uk_favorites_user_planet
UNIQUE (user_id, planet_id)
```

This means:

```text
Same user + same planet
=
cannot appear twice
```

But:

```text
User A + Mars
User B + Mars
```

is valid.

Therefore uniqueness is **per user**, not globally per planet.

---

# 34. Index on `user_id`

The migration also creates:

```sql
CREATE INDEX idx_favorites_user_id
    ON favorites(user_id);
```

This supports queries that frequently filter favorites by user.

Which is exactly what UniqueVerse does.

---

# 35. Flyway Problem During Sprint 5

Sprint 5 also exposed an important real-world migration problem.

An earlier migration:

```text
V6__add_user_to_favorites.sql
```

had already been recorded by Flyway as successful but effectively contained no migration work.

Later, changing that migration produced:

```text
Migration checksum mismatch
```

The important lesson:

> Flyway migrations are versioned history, not disposable SQL files.

Once a migration has been applied, changing its contents can cause checksum validation failures.

---

# 36. Why We Did Not Simply Delete V6

The migration history already contained V6.

Therefore deleting it from the source directory would create another mismatch between:

```text
filesystem
```

and:

```text
flyway_schema_history
```

The correct resolution was to preserve V6 as the recorded no-op and create a new migration.

---

# 37. V7 Migration

The actual database change was moved into:

```text
V7__add_user_to_favorites.sql
```

It added:

```sql
ALTER TABLE favorites
    ADD COLUMN user_id BIGINT NOT NULL;
```

Then:

```sql
ALTER TABLE favorites
    ADD CONSTRAINT fk_favorites_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE;
```

Then:

```sql
ALTER TABLE favorites
    ADD CONSTRAINT uk_favorites_user_planet
        UNIQUE (user_id, planet_id);
```

And:

```sql
CREATE INDEX idx_favorites_user_id
    ON favorites(user_id);
```

---

# 38. Why V6 Remains Empty

V6 must remain as recorded.

The final migration history is conceptually:

```text
V1 -> planets table
V2 -> seed planets
V3 -> planet fields
V4 -> favorites
V5 -> users
V6 -> recorded no-op
V7 -> user ownership for favorites
```

Important:

```text
DO NOT edit V6
DO NOT delete V6
DO NOT randomly run repair
```

V7 represents the actual schema change.

---

# 39. Flyway Lesson

A useful production rule:

```text
Applied migration
       |
       v
Treat as immutable
       |
       v
Need another schema change?
       |
       v
Create a new migration
```

For example:

```text
V7
V8
V9
```

rather than modifying:

```text
V5
```

after it has already been applied.

---

# 40. Favorite Repository Design

The repository contains ownership-aware methods:

```java
findAllWithPlanetByUsername(username)
```

```java
findByIdWithPlanetAndUsername(id, username)
```

```java
findByIdAndUsername(id, username)
```

and duplicate detection:

```java
existsByUserUsernameAndPlanetId(
    username,
    planetId
)
```

This keeps important ownership checks inside the data access boundary.

---

# 41. `JOIN FETCH` and Favorites

The favorite response needs planet information:

```text
favorite.id
planet.id
planet.name
```

The repository uses:

```java
JOIN FETCH f.planet
```

for relevant reads.

This loads the associated planet together with the favorite.

It avoids relying on a lazy relationship after the persistence context has closed.

---

# 42. Service Layer Responsibility

The FavoriteService owns the business rules.

For creation:

```text
Check duplicate
      |
Find user
      |
Find planet
      |
Create favorite
      |
Save
```

For retrieval:

```text
Find favorite belonging to username
      |
Map to response
```

For deletion:

```text
Find favorite belonging to username
      |
Delete
```

The controller should not contain these business rules.

---

# 43. Controller Responsibility

The controller primarily handles:

```text
HTTP request
      |
      v
Extract parameters
      |
      v
Extract authenticated username
      |
      v
Call service
      |
      v
Return HTTP response
```

Example:

```java
authentication.getName()
```

is passed to the service.

The service decides whether the user is actually allowed to operate on the resource.

---

# 44. Defense in Depth

Sprint 5 ended up using multiple security layers.

```text
Layer 1
Spring Security
      |
      v
Is the request authenticated?

Layer 2
Role authorization
      |
      v
Does the user have ADMIN?

Layer 3
Service ownership
      |
      v
Does this resource belong to the user?

Layer 4
Database constraints
      |
      v
Can invalid state physically exist?
```

This is much stronger than relying on a single `if` statement somewhere in a controller.

---

# 45. Security Testing Matrix

The important manual checks were:

| Scenario | Expected |
|---|---:|
| Public planet GET without token | 200 |
| Favorite request without token | 401 |
| `/auth/me` without token | 401 |
| USER creates planet | 403 |
| ADMIN creates planet | Allowed |
| User creates favorite | Allowed |
| Same user favorites same planet again | 409 |
| User sees own favorites | Allowed |
| User B sees User A favorites | Not allowed |
| User B reads User A favorite by ID | 404 |
| User B deletes User A favorite | 404 |
| Owner deletes own favorite | 204 |

This matrix effectively became the practical definition of Sprint 5's security behavior.

---

# 46. Common Security Mistakes Avoided

## Mistake 1: Trusting a username from the request

Bad:

```json
{
    "username": "admin"
}
```

Better:

```java
authentication.getName()
```

The identity comes from the authenticated security context.

---

## Mistake 2: Only checking authentication

Bad:

```text
User is logged in
     |
     v
Allow access to any favorite
```

Authentication is not ownership.

---

## Mistake 3: Only checking application-level duplicates

Bad:

```text
existsBy...
```

with no database constraint.

Race conditions can still produce duplicates.

UniqueVerse uses both:

```text
Application check
+
Database UNIQUE constraint
```

---

## Mistake 4: Returning 403 for every security failure

Not every failure means the same thing.

```text
No authentication
      -> 401

Authenticated but insufficient role
      -> 403

Resource not owned/found
      -> 404
```

---

## Mistake 5: Editing an applied Flyway migration

Once Flyway has recorded a migration, modifying it can cause checksum mismatches.

Create a new migration instead.

---

# 47. Interview Questions

## Q1. What is the difference between 401 and 403?

`401 Unauthorized` means the request lacks valid authentication.

`403 Forbidden` means the user is authenticated but does not have sufficient permissions.

---

## Q2. How does Spring Security know a user is an ADMIN?

The authenticated user's authorities contain:

```text
ROLE_ADMIN
```

The security configuration checks:

```java
.hasRole("ADMIN")
```

---

## Q3. Is authentication enough to access another user's resource?

No.

Authentication identifies the user. Resource ownership must additionally be checked.

---

## Q4. How did UniqueVerse prevent User B from deleting User A's favorite?

The repository query searches using both:

```text
favorite ID
+
authenticated username
```

If the favorite does not belong to that username, the query returns no result and the service returns `404`.

---

## Q5. Why use a database UNIQUE constraint if the service already checks duplicates?

Because application-level checks can race under concurrent requests.

The database constraint provides the final integrity guarantee.

---

## Q6. Why is `anyRequest().denyAll()` useful?

It creates a fail-closed security configuration. An endpoint that has not been explicitly permitted or protected does not accidentally become public.

---

## Q7. Why shouldn't V6 be modified after it was applied?

Flyway tracks migration checksums. Changing an already-applied migration can cause checksum mismatch errors and makes migration history unreliable.

---

## Q8. What is resource ownership authorization?

It verifies that the authenticated user owns the specific resource they are trying to access or modify.

Example:

```text
Favorite #17 belongs to Mac
```

Therefore another authenticated user cannot manipulate Favorite #17.

---

# 48. Sprint 5 Final Architecture

The complete security architecture after Sprint 5:

```text
                         HTTP REQUEST
                              |
                              v
                   Spring Security Filters
                              |
                              v
                    JwtAuthenticationFilter
                              |
                    +---------+---------+
                    |                   |
                 No JWT              Valid JWT
                    |                   |
                    v                   v
              Anonymous          SecurityContext
                                        |
                                        v
                                  Authentication
                                        |
                                        v
                              Authorization Rules
                                        |
                    +-------------------+-------------------+
                    |                   |                   |
                  PUBLIC          AUTHENTICATED           ADMIN
                    |                   |                   |
                    v                   v                   v
                Planet GET          Favorites         Planet writes
                                        |
                                        v
                                Ownership check
                                        |
                                        v
                                  FavoriteService
                                        |
                                        v
                                  PostgreSQL
                                        |
                           +------------+------------+
                           |                         |
                      FK constraints          UNIQUE constraint
                           |                         |
                           +------------+------------+
                                        |
                                        v
                                  Data integrity
```

---

# 49. Sprint 5 Lessons

### Lesson 1

**Authentication and authorization are different problems.**

```text
Authentication = identity
Authorization = permission
```

---

### Lesson 2

**Roles are not enough for user-owned resources.**

A USER can be authenticated while still being forbidden from touching another user's favorite.

---

### Lesson 3

**Never trust client-provided identity for ownership decisions.**

Use:

```java
authentication.getName()
```

from the security context.

---

### Lesson 4

**Security should exist at multiple layers.**

```text
Spring Security
+
Role authorization
+
Service ownership checks
+
Database constraints
```

---

### Lesson 5

**Database constraints are part of application security.**

The application should prevent invalid operations, but the database should also prevent invalid state.

---

### Lesson 6

**Flyway migrations are history.**

Once applied:

```text
Don't rewrite history.
Add another migration.
```

---

# 50. Sprint 5 Completion

Sprint 5 successfully established the security foundation of UniqueVerse.

### Authentication

- [x] User registration
- [x] BCrypt password hashing
- [x] Login
- [x] AuthenticationManager
- [x] UserDetailsService
- [x] JWT generation
- [x] JWT validation
- [x] JWT expiration
- [x] JWT filter
- [x] SecurityContext
- [x] Stateless authentication

### Authorization

- [x] Public endpoints
- [x] Authenticated endpoints
- [x] USER role
- [x] ADMIN role
- [x] ADMIN-only planet writes
- [x] 401 handling
- [x] 403 handling
- [x] Fail-closed `anyRequest().denyAll()`

### Favorites security

- [x] User-owned favorites
- [x] Ownership-aware reads
- [x] Ownership-aware deletion
- [x] Duplicate favorite protection
- [x] `409 Conflict` for duplicates
- [x] `404` for inaccessible/unknown favorites
- [x] Database foreign key
- [x] Database unique constraint
- [x] User index
- [x] Manual multi-user verification

### Migration

- [x] Identified V6 checksum/history problem
- [x] Preserved V6 as recorded no-op
- [x] Created V7 for actual ownership migration
- [x] Added `user_id`
- [x] Added foreign key
- [x] Added unique constraint
- [x] Added user index

---

# 51. Final Sprint 5 Mental Model

If asked in an interview:

> **"Explain the security architecture you implemented in UniqueVerse."**

The short answer is:

```text
UniqueVerse uses stateless JWT authentication with Spring Security.

Users register with BCrypt-hashed passwords and authenticate through
AuthenticationManager and UserDetailsService. Successful login produces
a short-lived JWT. JwtAuthenticationFilter validates the token on
subsequent requests and places the authenticated identity into the
SecurityContext.

Authorization is handled through Spring Security endpoint rules.
Planet reads are public, Favorites require authentication, and planet
write operations require ROLE_ADMIN.

For user-owned Favorites, authentication alone is not sufficient.
Repository queries are scoped to the authenticated username so users
can only access their own resources. Duplicate favorites are protected
at both the service layer and database layer using a UNIQUE(user_id,
planet_id) constraint.

The API distinguishes 401 from 403, and inaccessible favorite resources
are returned as 404 rather than exposing another user's resources.

Flyway migrations are treated as immutable history, with the actual
Favorites ownership schema change moved to V7 after the earlier V6
no-op migration had already been recorded.
```

That is the **Sprint 5 story**: not just "I added JWT", but **authentication → authorization → ownership → database integrity**, which is the part that actually makes the security implementation worth discussing in an interview.