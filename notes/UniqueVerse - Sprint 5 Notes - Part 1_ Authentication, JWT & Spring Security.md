# UniqueVerse — Sprint 5 Notes
## Part 1: Authentication, JWT & Spring Security

> **Sprint:** 5  
> **Focus:** Authentication, JWT, Spring Security foundation  
> **Project:** UniqueVerse  
> **Status:** Completed

---

# 1. Sprint Objective

Sprint 5 introduced **real authentication and security** into UniqueVerse.

Before Sprint 5, the API could expose planet data, but there was no meaningful concept of:

- registered users
- passwords
- authentication
- JWT access tokens
- authenticated requests
- user identity inside controllers/services
- roles
- protected resources

The goal was to build the authentication foundation required for the rest of the application.

The final authentication flow became:

```text
Client
   |
   | username + password
   v
POST /api/v1/auth/login
   |
   v
AuthenticationManager
   |
   v
UserDetailsService
   |
   v
UserRepository
   |
   v
BCrypt password verification
   |
   v
JWT generated
   |
   v
Client receives Bearer token
   |
   | Authorization: Bearer <JWT>
   v
JwtAuthenticationFilter
   |
   v
JWT validation
   |
   v
SecurityContext
   |
   v
Authenticated request
```

---

# 2. Authentication vs Authorization

This distinction became important during Sprint 5.

## Authentication

Authentication answers:

> **Who are you?**

Example:

```text
Username: mac
Password: ********
```

The application verifies the credentials and establishes the user's identity.

---

## Authorization

Authorization answers:

> **What are you allowed to do?**

For example:

```text
USER
  -> can access favorites

ADMIN
  -> can create/update/delete planets
```

Therefore:

```text
Authentication = Identity
Authorization  = Permissions
```

Sprint 5 implemented both, but this part focuses primarily on authentication.

---

# 3. User Entity

UniqueVerse stores users in PostgreSQL.

Conceptually:

```text
users
--------------------------------
id
username
email
password
role
```

The password stored in the database is **not the original password**.

Instead:

```text
Raw password
     |
     v
BCrypt
     |
     v
Password hash
     |
     v
Database
```

This means the application never needs to store the user's plaintext password.

---

# 4. Password Hashing with BCrypt

Spring Security provides a `PasswordEncoder`.

UniqueVerse uses:

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

During registration:

```java
user.setPassword(
    passwordEncoder.encode(request.getPassword())
);
```

So:

```text
"myPassword123"
       |
       v
BCrypt
       |
       v
"$2a$..."
```

The database stores the BCrypt hash.

---

## Why BCrypt?

Passwords should not be encrypted and stored with a reversible key.

They should be **hashed**.

BCrypt is designed specifically for password hashing and intentionally makes password guessing expensive.

The important distinction:

```text
Encryption:
plaintext <-> ciphertext

Hashing:
plaintext -> hash
```

Passwords should normally use the second model.

---

# 5. Registration Flow

Endpoint:

```http
POST /api/v1/auth/register
```

Example request:

```json
{
    "username": "mac",
    "email": "mac@example.com",
    "password": "password123"
}
```

The request reaches the authentication service.

---

## Registration logic

The service performs:

### Step 1: Check username

```java
if (userRepository.existsByUsername(request.getUsername())) {
    throw new IllegalArgumentException(
        "Username already exists"
    );
}
```

### Step 2: Check email

```java
if (userRepository.existsByEmail(request.getEmail())) {
    throw new IllegalArgumentException(
        "Email already exists"
    );
}
```

### Step 3: Create user

```java
User user = new User();

user.setUsername(request.getUsername());
user.setEmail(request.getEmail());
```

### Step 4: Hash password

```java
user.setPassword(
    passwordEncoder.encode(request.getPassword())
);
```

### Step 5: Assign role

Public registration always creates:

```java
user.setRole("USER");
```

The client does **not** get to submit:

```json
{
    "role": "ADMIN"
}
```

This is critical.

Otherwise a user could simply register themselves as:

```text
ADMIN
```

and congratulations, we have invented our own security vulnerability.

---

# 6. Why Role Must Not Come From the Client

Bad design:

```json
{
    "username": "attacker",
    "password": "123456",
    "role": "ADMIN"
}
```

Then:

```java
user.setRole(request.getRole());
```

This would allow privilege escalation.

Correct design:

```java
user.setRole("USER");
```

The server controls the privilege level.

ADMIN creation is handled separately through the startup seed.

---

# 7. ADMIN Bootstrap

UniqueVerse does not expose an endpoint such as:

```http
POST /api/v1/admin/create
```

for creating the first administrator.

Instead, the initial administrator is created from environment variables:

```text
ADMIN_USERNAME
ADMIN_EMAIL
ADMIN_PASSWORD
```

The startup seeder:

```java
@Component
public class AdminSeeder implements CommandLineRunner
```

checks whether the configured admin already exists.

If not:

```text
Application starts
       |
       v
AdminSeeder
       |
       v
Check username
       |
       +---- exists ---> do nothing
       |
       +---- missing --> create ADMIN
```

The password is still BCrypt-hashed before persistence.

---

# 8. AuthenticationManager

Spring Security's `AuthenticationManager` is responsible for performing authentication.

UniqueVerse exposes it as a bean:

```java
@Bean
public AuthenticationManager authenticationManager(
        AuthenticationConfiguration configuration)
        throws Exception {

    return configuration.getAuthenticationManager();
}
```

The application then uses it during login.

---

# 9. Login Flow

Endpoint:

```http
POST /api/v1/auth/login
```

Request:

```json
{
    "username": "mac",
    "password": "password123"
}
```

The service calls:

```java
authenticationManager.authenticate(
    new UsernamePasswordAuthenticationToken(
        request.getUsername(),
        request.getPassword()
    )
);
```

The important point is that the service does **not manually compare passwords**.

Spring Security handles the authentication process.

Conceptually:

```text
LoginRequest
     |
     v
AuthenticationManager
     |
     v
UserDetailsService
     |
     v
UserRepository
     |
     v
Stored BCrypt hash
     |
     v
PasswordEncoder
     |
     v
Authentication successful
```

If authentication fails, the request is rejected.

---

# 10. UserDetailsService

Spring Security needs a way to load a user.

UniqueVerse implements:

```java
UserDetailsService
```

The implementation:

```java
@Override
public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException {

    User user = userRepository.findByUsername(username)
            .orElseThrow(() ->
                    new UsernameNotFoundException(
                            "Invalid username or password"
                    ));

    return org.springframework.security.core.userdetails.User
            .withUsername(user.getUsername())
            .password(user.getPassword())
            .authorities(
                    new SimpleGrantedAuthority(
                            "ROLE_" + user.getRole()
                    )
            )
            .build();
}
```

This bridges the application's database user model with Spring Security's security model.

---

# 11. Why `UserDetails` Exists

Our application has:

```java
com.uniqueverse.auth.entity.User
```

Spring Security has its own concept:

```text
UserDetails
```

The `UserDetailsService` acts as the adapter:

```text
UniqueVerse User
       |
       v
UserDetailsService
       |
       v
Spring Security UserDetails
```

This separation allows Spring Security to work without knowing how UniqueVerse stores users.

---

# 12. Roles and `ROLE_` Prefix

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

This is because:

```java
new SimpleGrantedAuthority(
    "ROLE_" + user.getRole()
)
```

So:

```text
Database:
ADMIN

Spring Security:
ROLE_ADMIN
```

This becomes important when using:

```java
.hasRole("ADMIN")
```

Spring Security internally works with:

```text
ROLE_ADMIN
```

---

# 13. JWT

After successful authentication, UniqueVerse creates a JWT.

JWT means:

**JSON Web Token**

The token represents the authenticated identity.

Instead of sending the username and password with every request:

```text
username + password
```

the client sends:

```text
Bearer JWT
```

---

# 14. Why JWT?

UniqueVerse uses:

```text
STATELESS
```

authentication.

The server does not maintain a traditional HTTP session for each logged-in user.

Instead:

```text
Login
  |
  v
JWT issued
  |
  v
Client stores token
  |
  v
Every protected request includes token
```

This works particularly well for REST APIs.

---

# 15. JWT Structure

A JWT consists conceptually of:

```text
HEADER.PAYLOAD.SIGNATURE
```

For example:

```text
xxxxx.yyyyy.zzzzz
```

The payload contains claims such as:

```text
subject
issuedAt
expiration
```

UniqueVerse uses the username as the JWT subject:

```java
.subject(userDetails.getUsername())
```

---

# 16. JWT Expiration

The JWT is intentionally short-lived.

UniqueVerse currently configures:

```properties
jwt.expiration-ms=900000
```

That equals:

```text
15 minutes
```

So:

```text
Token issued
     |
     v
15 minutes
     |
     v
Token expires
```

Short-lived access tokens reduce the damage window if a token is stolen.

Refresh-token functionality is intentionally outside this sprint.

---

# 17. JWT Signing Key

The JWT is cryptographically signed.

UniqueVerse loads the secret from an environment variable:

```properties
jwt.secret=${JWT_SECRET}
```

The secret is not supposed to be committed to GitHub.

The service converts the Base64 secret into a signing key:

```java
this.signingKey = Keys.hmacShaKeyFor(
    Decoders.BASE64.decode(secret)
);
```

The same signing key is used to verify incoming tokens.

---

# 18. Generating the JWT

The core implementation:

```java
public String generateToken(UserDetails userDetails) {

    Date now = new Date();

    Date expiration =
            new Date(now.getTime() + expirationMs);

    return Jwts.builder()
            .subject(userDetails.getUsername())
            .issuedAt(now)
            .expiration(expiration)
            .signWith(signingKey)
            .compact();
}
```

The important claims are:

```text
subject     -> username
issuedAt    -> token creation time
expiration  -> token expiry time
```

Then:

```java
.signWith(signingKey)
```

protects the token from unauthorized modification.

---

# 19. Login Response

After successful login, the API returns:

```json
{
    "accessToken": "<JWT>",
    "tokenType": "Bearer",
    "expiresIn": 900000
}
```

The client then uses:

```http
Authorization: Bearer <JWT>
```

for protected endpoints.

---

# 20. Why `Bearer`?

The standard HTTP authorization format is:

```http
Authorization: Bearer <token>
```

`Bearer` essentially means:

> Whoever possesses this token can present it as the credential.

Therefore JWTs must be protected like credentials.

---

# 21. JwtAuthenticationFilter

The biggest piece connecting JWT to Spring Security is:

```java
OncePerRequestFilter
```

UniqueVerse implements:

```java
JwtAuthenticationFilter
```

This filter runs for incoming HTTP requests.

Conceptually:

```text
HTTP Request
     |
     v
JwtAuthenticationFilter
     |
     v
Read Authorization header
     |
     v
Extract JWT
     |
     v
Validate JWT
     |
     v
Load user
     |
     v
Create Authentication
     |
     v
SecurityContext
     |
     v
Controller
```

---

# 22. Reading the Authorization Header

The filter checks:

```java
String authHeader =
        request.getHeader("Authorization");
```

Then:

```java
if (authHeader == null
        || !authHeader.startsWith("Bearer ")) {

    filterChain.doFilter(request, response);
    return;
}
```

So a request without a Bearer token is simply passed along.

Spring Security later determines whether that endpoint requires authentication.

---

# 23. Extracting the JWT

The actual token is extracted with:

```java
String jwt = authHeader.substring(7);
```

Why `7`?

Because:

```text
Bearer
```

plus the following space contains seven characters:

```text
"Bearer "
0123456
```

The resulting value is the JWT itself.

---

# 24. Extracting the Username

The JWT service reads the subject:

```java
String username =
        jwtService.extractUsername(jwt);
```

Internally:

```java
Jwts.parser()
        .verifyWith(signingKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
```

The token signature is verified using the signing key.

---

# 25. Validating the Token

UniqueVerse checks:

```java
public boolean isTokenValid(
        String token,
        UserDetails userDetails) {

    String username =
            extractUsername(token);

    return username.equals(
                userDetails.getUsername()
           )
           && !isTokenExpired(token);
}
```

So the token must satisfy:

```text
Username matches
        AND
Token has not expired
```

---

# 26. Creating Authentication

Once the JWT is valid:

```java
UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );
```

Then:

```java
SecurityContextHolder
        .getContext()
        .setAuthentication(authentication);
```

This is the point where Spring Security considers the request authenticated.

---

# 27. SecurityContext

The `SecurityContext` holds the current authenticated identity.

After the JWT filter succeeds:

```text
SecurityContext
-------------------------
username = mac
authorities = ROLE_USER
```

Then downstream application code can access the authenticated user.

For example:

```java
authentication.getName()
```

returns:

```text
mac
```

This became especially important later when implementing user-owned favorites.

---

# 28. Stateless Session Configuration

UniqueVerse explicitly uses:

```java
.sessionManagement(session ->
    session.sessionCreationPolicy(
        SessionCreationPolicy.STATELESS
    )
)
```

This means the API does not rely on server-side HTTP sessions for authentication.

Every protected request must carry its authentication information.

```text
Request 1 -> JWT
Request 2 -> JWT
Request 3 -> JWT
```

rather than:

```text
Login
  |
  v
Server session
  |
  v
Session ID
```

---

# 29. Disabling Browser-Oriented Authentication

UniqueVerse is a REST API, not a traditional server-rendered website.

Therefore:

```java
.httpBasic(httpBasic -> httpBasic.disable())
.formLogin(formLogin -> formLogin.disable())
```

are disabled.

The application uses:

```text
JWT Bearer authentication
```

instead.

---

# 30. CSRF

UniqueVerse disables CSRF:

```java
.csrf(csrf -> csrf.disable())
```

The reason is the authentication architecture.

The API uses stateless Bearer tokens rather than browser-managed session cookies for authentication.

The important lesson is not:

> "Always disable CSRF."

The correct lesson is:

> CSRF requirements depend on how authentication credentials are transported and how the application is used.

---

# 31. Authentication Configuration

The security chain registers the JWT filter:

```java
.addFilterBefore(
        jwtAuthenticationFilter,
        UsernamePasswordAuthenticationFilter.class
)
```

This places the JWT authentication logic before Spring Security's username/password authentication filter.

The request therefore gets a chance to establish authentication from its Bearer token before authorization decisions are made.

---

# 32. Public vs Protected Endpoints

At this stage the security configuration establishes the basic boundary.

### Public

```text
POST /api/v1/auth/register
POST /api/v1/auth/login

GET /api/v1/planets/**
```

### Authenticated

```text
GET /api/v1/auth/me
/api/v1/favorites/**
```

### ADMIN

```text
POST   /api/v1/planets/**
PUT    /api/v1/planets/**
DELETE /api/v1/planets/**
```

The detailed authorization model is covered in **Sprint 5 Part 2**.

---

# 33. Important Security Principle

Authentication is not the same thing as trusting the client.

The client sends:

```http
Authorization: Bearer <JWT>
```

The server does **not** simply trust the username contained in the request body.

Instead:

```text
JWT
 |
 v
signature verification
 |
 v
username extraction
 |
 v
database-backed UserDetails
 |
 v
SecurityContext
```

The authenticated identity therefore comes from the server-side security context.

---

# 34. Authentication Failure

The JWT filter deliberately does not allow an invalid token to become an authenticated user.

The validation logic is wrapped in exception handling:

```java
try {
    ...
} catch (RuntimeException ex) {
    // Invalid or expired JWT.
    // Leave unauthenticated.
}
```

The request continues through the filter chain.

If the requested endpoint is public, it may still succeed.

If the requested endpoint requires authentication, Spring Security rejects it.

This produces the important distinction:

```text
Invalid JWT + public endpoint
        -> request can still be public

Invalid JWT + protected endpoint
        -> 401 Unauthorized
```

---

# 35. Environment Variables

Security-sensitive values are externalized.

Examples:

```text
DB_URL
DB_USERNAME
DB_PASSWORD

JWT_SECRET

ADMIN_USERNAME
ADMIN_EMAIL
ADMIN_PASSWORD
```

Application configuration references them:

```properties
jwt.secret=${JWT_SECRET}
admin.username=${ADMIN_USERNAME}
admin.email=${ADMIN_EMAIL}
admin.password=${ADMIN_PASSWORD}
```

Secrets should never be hardcoded into source code or committed to Git.

---

# 36. Common Spring Security Mental Model

For interviews, remember this sequence:

```text
HTTP Request
      |
      v
Security Filters
      |
      v
JWT Authentication Filter
      |
      v
SecurityContext
      |
      v
Authentication
      |
      v
Authorization Rules
      |
      v
Controller
```

Authentication happens before authorization.

So:

```text
Who are you?
     |
     v
Authentication
     |
     v
What can you do?
     |
     v
Authorization
```

---

# 37. Interview Questions

## Q1. Why use JWT instead of storing authentication in an HTTP session?

JWT allows the API to operate statelessly. The client sends the access token with each request, so the server does not need to maintain authentication state in an HTTP session.

---

## Q2. What does `UserDetailsService` do?

It loads user information from the application's user store and converts it into Spring Security's `UserDetails` representation.

---

## Q3. What is the purpose of `SecurityContextHolder`?

It stores the authentication information for the current request/thread so Spring Security and application code can determine who is authenticated.

---

## Q4. Why is the password BCrypt-hashed?

Because passwords should not be stored as plaintext. BCrypt is a password-hashing algorithm designed to make brute-force guessing computationally expensive.

---

## Q5. Why shouldn't registration accept a role from the client?

Because the client could request an elevated role such as `ADMIN`, resulting in privilege escalation.

---

## Q6. What does `STATELESS` mean?

Spring Security does not maintain authentication using an HTTP session. Each protected request must provide its authentication credentials, such as a JWT.

---

## Q7. What happens when a JWT expires?

JWT validation fails. The request remains unauthenticated, and if the endpoint requires authentication, Spring Security returns `401 Unauthorized`.

---

## Q8. What is the difference between authentication and authorization?

```text
Authentication -> Who are you?
Authorization  -> What are you allowed to do?
```

---

# 38. Sprint 5 Part 1 Completion Checklist

- [x] User entity
- [x] User repository
- [x] Registration endpoint
- [x] Duplicate username protection
- [x] Duplicate email protection
- [x] BCrypt password hashing
- [x] Public registration always creates USER
- [x] ADMIN startup seeding
- [x] AuthenticationManager
- [x] UserDetailsService
- [x] JWT dependencies
- [x] JWT generation
- [x] JWT expiration
- [x] JWT signature validation
- [x] JWT authentication filter
- [x] SecurityContext authentication
- [x] Stateless session configuration
- [x] Bearer token authentication
- [x] Protected `/auth/me`
- [x] Environment-based security configuration

---

# 39. Key Takeaway

The most important thing learned in this part of Sprint 5 is the complete authentication pipeline:

```text
REGISTER
   |
   v
User stored with BCrypt password
   |
   v
LOGIN
   |
   v
AuthenticationManager
   |
   v
UserDetailsService
   |
   v
Password verification
   |
   v
JWT generated
   |
   v
CLIENT
   |
   | Authorization: Bearer JWT
   v
JWT Filter
   |
   v
JWT verification
   |
   v
UserDetails
   |
   v
SecurityContext
   |
   v
Authenticated request
```

**Part 2 covers what happens after authentication: authorization, USER/ADMIN roles, `401` vs `403`, user-owned favorites, duplicate protection, ownership enforcement, database constraints, and the Flyway migration problem encountered during Sprint 5.**