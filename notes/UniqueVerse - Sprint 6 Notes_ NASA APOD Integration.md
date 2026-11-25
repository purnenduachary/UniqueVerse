# UniqueVerse - Sprint 6 Notes
## NASA APOD Integration

---

## 1. Sprint Objective

The purpose of Sprint 6 was to integrate the **NASA Astronomy Picture of the Day (APOD) API** into UniqueVerse.

The main learning objective was not simply to call NASA.

It was to learn how a Spring Boot application:

- consumes an external REST API
- maps external JSON into Java objects
- separates external API communication from business logic
- exposes the external data through its own REST endpoint
- manages API credentials using environment variables
- handles differences between external JSON naming and Java naming
- integrates the external service with Spring Security
- verifies the complete integration using Postman

### Target architecture

```text
Client
   │
   ▼
GET /api/v1/apod
   │
   ▼
ApodController
   │
   ▼
ApodService
   │
   ▼
NasaApodClient
   │
   ▼
NASA APOD API
   │
   ▼
ApodResponse
   │
   ▼
Client
```

The important architectural idea is:

> The controller should not communicate directly with NASA.

Each layer has one responsibility.

---

# 2. What We Are Integrating

NASA provides an APOD endpoint:

```text
https://api.nasa.gov/planetary/apod
```

The API accepts an API key:

```text
api_key
```

A typical response contains fields such as:

```json
{
  "date": "2026-11-25",
  "title": "NASA Science",
  "explanation": "...",
  "media_type": "image",
  "url": "...",
  "hdurl": "...",
  "copyright": null
}
```

The external API uses:

```text
media_type
```

while Java conventionally uses:

```text
mediaType
```

This difference became important later.

---

# 3. Dependency: RestClient

UniqueVerse uses Spring's modern `RestClient` for making synchronous HTTP requests.

Because the project uses Spring Boot 4, the required starter is:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>
```

The project already had:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webmvc</artifactId>
</dependency>
```

These have different purposes.

```text
spring-boot-starter-webmvc
        ↓
Controllers
REST endpoints
Tomcat
Spring MVC

spring-boot-starter-restclient
        ↓
RestClient
External HTTP communication
```

### Important lesson

Having Spring MVC does not automatically mean the application has an auto-configured `RestClient.Builder`.

We initially encountered:

```text
No qualifying bean of type
'org.springframework.web.client.RestClient$Builder'
available
```

The fix was adding:

```xml
spring-boot-starter-restclient
```

---

# 4. APOD DTO

## File

```text
src/main/java/com/uniqueverse/apod/dto/ApodResponse.java
```

## Code

```java
package com.uniqueverse.apod.dto;

public class ApodResponse {

    private String date;
    private String title;
    private String explanation;
    private String mediaType;
    private String url;
    private String hdurl;
    private String copyright;

    public ApodResponse() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getHdurl() {
        return hdurl;
    }

    public void setHdurl(String hdurl) {
        this.hdurl = hdurl;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }
}
```

### Why do we need a DTO?

NASA returns JSON.

Spring/Jackson needs to convert that JSON into a Java object.

```text
NASA JSON
    ↓
Jackson
    ↓
ApodResponse
```

The DTO represents the **external API response**.

It is not a database entity.

That distinction matters.

```text
Entity
    → Database representation

DTO
    → API/data-transfer representation
```

---

# 5. Mapping `media_type` to `mediaType`

NASA uses:

```json
"media_type": "image"
```

Our Java field is:

```java
private String mediaType;
```

Jackson does not automatically know that these two names represent the same property in our current configuration.

Therefore we use:

```java
import com.fasterxml.jackson.annotation.JsonProperty;
```

and:

```java
@JsonProperty("media_type")
private String mediaType;
```

So the final field becomes:

```java
@JsonProperty("media_type")
private String mediaType;
```

### Why?

It explicitly tells Jackson:

> The JSON property called `media_type` should populate this Java field.

Without this mapping, Postman initially showed:

```json
"mediaType": null
```

After adding `@JsonProperty`, the response became:

```json
"mediaType": "image"
```

### General pattern

External API:

```json
{
    "first_name": "Mac"
}
```

Java:

```java
@JsonProperty("first_name")
private String firstName;
```

This is common when consuming APIs that use snake_case.

---

# 6. NASA API Client

## File

```text
src/main/java/com/uniqueverse/apod/client/NasaApodClient.java
```

## Code

```java
package com.uniqueverse.apod.client;

import com.uniqueverse.apod.dto.ApodResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NasaApodClient {

    private final RestClient restClient;

    public NasaApodClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .baseUrl("https://api.nasa.gov")
                .build();
    }

    public ApodResponse getTodayApod(String apiKey) {

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/planetary/apod")
                        .queryParam("api_key", apiKey)
                        .build())
                .retrieve()
                .body(ApodResponse.class);
    }
}
```

---

# 7. Understanding `NasaApodClient`

The client is responsible for **communication with NASA**.

It knows:

- NASA's base URL
- NASA's APOD path
- how to make the GET request
- how to send the API key
- how to convert the response into `ApodResponse`

It does NOT know anything about:

- HTTP endpoints exposed by UniqueVerse
- authentication of UniqueVerse users
- favorites
- planets
- database logic

That keeps responsibilities separated.

---

# 8. `RestClient.Builder`

The constructor receives:

```java
RestClient.Builder restClientBuilder
```

Spring Boot provides the builder.

We then create our client:

```java
this.restClient = restClientBuilder
        .baseUrl("https://api.nasa.gov")
        .build();
```

This means the individual request only needs:

```java
.path("/planetary/apod")
```

instead of repeatedly writing:

```text
https://api.nasa.gov/planetary/apod
```

---

# 9. Building the NASA Request

The request starts with:

```java
restClient.get()
```

This means:

```text
HTTP GET
```

Then:

```java
.uri(uriBuilder -> uriBuilder
        .path("/planetary/apod")
        .queryParam("api_key", apiKey)
        .build())
```

produces something conceptually like:

```text
https://api.nasa.gov/planetary/apod?api_key=YOUR_KEY
```

Then:

```java
.retrieve()
```

executes the request and prepares the response.

Finally:

```java
.body(ApodResponse.class)
```

tells Spring/Jackson:

> Convert the response body into an `ApodResponse`.

---

# 10. API Key Configuration

The NASA key must NOT be hardcoded into Java source code.

In:

```text
application.properties
```

we added:

```properties
# NASA
nasa.api-key=${NASA_API_KEY}
```

The actual key is supplied through an environment variable:

```text
NASA_API_KEY=<your NASA API key>
```

### Why?

Bad:

```java
String apiKey = "actual-secret-key";
```

Good:

```text
Environment variable
        ↓
application.properties
        ↓
nasa.api-key
        ↓
ApodService
        ↓
NasaApodClient
```

This prevents credentials from being committed into Git.

---

# 11. ApodService

## File

```text
src/main/java/com/uniqueverse/apod/service/ApodService.java
```

## Code

```java
package com.uniqueverse.apod.service;

import com.uniqueverse.apod.client.NasaApodClient;
import com.uniqueverse.apod.dto.ApodResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ApodService {

    private final NasaApodClient nasaApodClient;
    private final String apiKey;

    public ApodService(
            NasaApodClient nasaApodClient,
            @Value("${nasa.api-key}") String apiKey) {

        this.nasaApodClient = nasaApodClient;
        this.apiKey = apiKey;
    }

    public ApodResponse getTodayApod() {
        return nasaApodClient.getTodayApod(apiKey);
    }
}
```

---

# 12. Why do we need the Service?

We could technically make the controller call:

```java
nasaApodClient.getTodayApod(...)
```

directly.

But that would violate the architecture we're building.

Instead:

```text
Controller
    ↓
Service
    ↓
External Client
```

The service owns the application use case:

> Get today's APOD.

This becomes particularly useful when we add:

- caching
- error handling
- persistence
- transformations
- business rules

For example, later:

```java
public ApodResponse getTodayApod() {

    ApodResponse cached = cache.findToday();

    if (cached != null) {
        return cached;
    }

    ApodResponse response =
            nasaApodClient.getTodayApod(apiKey);

    cache.save(response);

    return response;
}
```

The controller doesn't need to know any of that.

---

# 13. `@Value`

We use:

```java
@Value("${nasa.api-key}")
```

This tells Spring to resolve:

```properties
nasa.api-key=${NASA_API_KEY}
```

and inject the resulting value into:

```java
private final String apiKey;
```

So:

```text
NASA_API_KEY
      ↓
Spring Environment
      ↓
nasa.api-key
      ↓
ApodService.apiKey
```

---

# 14. APOD Controller

## File

```text
src/main/java/com/uniqueverse/apod/controller/ApodController.java
```

## Code

```java
package com.uniqueverse.apod.controller;

import com.uniqueverse.apod.dto.ApodResponse;
import com.uniqueverse.apod.service.ApodService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/apod")
public class ApodController {

    private final ApodService apodService;

    public ApodController(ApodService apodService) {
        this.apodService = apodService;
    }

    @GetMapping
    public ApodResponse getTodayApod() {
        return apodService.getTodayApod();
    }
}
```

---

# 15. Understanding the Controller

The endpoint is:

```http
GET /api/v1/apod
```

The controller does only one meaningful thing:

```java
return apodService.getTodayApod();
```

It does not:

- construct NASA URLs
- handle API keys
- call `RestClient`
- communicate directly with NASA

That's the point of the layered architecture.

---

# 16. Final Request Flow

When a client sends:

```http
GET /api/v1/apod
```

the request flows through:

```text
Client
   │
   ▼
ApodController
   │
   │ getTodayApod()
   ▼
ApodService
   │
   │ getTodayApod(apiKey)
   ▼
NasaApodClient
   │
   │ HTTP GET
   ▼
NASA API
   │
   │ JSON
   ▼
Jackson
   │
   ▼
ApodResponse
   │
   ▼
ApodController
   │
   ▼
HTTP 200 JSON
```

This is the most important mental model from Sprint 6.

---

# 17. Making APOD Public

APOD is intended to be accessible without authentication.

Therefore `SecurityConfig` contains:

```java
.requestMatchers("/api/v1/apod")
.permitAll()
```

This must appear before:

```java
.anyRequest().denyAll()
```

The relevant security flow is:

```java
.authorizeHttpRequests(auth -> auth

        .requestMatchers(
                "/api/v1/auth/register",
                "/api/v1/auth/login"
        ).permitAll()

        .requestMatchers("/api/v1/apod")
        .permitAll()

        // other rules...

        .anyRequest().denyAll()
)
```

### Why did this matter?

Without the APOD rule:

```text
GET /api/v1/apod
        ↓
No matching authorization rule
        ↓
anyRequest().denyAll()
        ↓
403 Forbidden
```

After adding:

```java
.requestMatchers("/api/v1/apod")
.permitAll()
```

we get:

```text
GET /api/v1/apod
        ↓
permitAll()
        ↓
NASA
        ↓
200 OK
```

---

# 18. Postman Verification

The endpoint was tested using:

```http
GET http://localhost:8080/api/v1/apod
```

Authentication was removed:

```text
Authorization → No Auth
```

This was important because APOD is supposed to be publicly accessible.

### Actual result

```text
200 OK
```

The response successfully contained:

```json
{
  "copyright": null,
  "date": "2026-11-25",
  "explanation": "...",
  "hdurl": "...",
  "mediaType": "image",
  "title": "NASA Science",
  "url": "..."
}
```

Therefore the core integration was successfully verified.

---

# 19. Problems Encountered During Sprint 6

## Problem 1: `RestClient.Builder` not found

Error:

```text
No qualifying bean of type
'org.springframework.web.client.RestClient$Builder'
available
```

### Cause

The project did not have:

```xml
spring-boot-starter-restclient
```

### Fix

Added:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-restclient</artifactId>
</dependency>
```

### Lesson

The HTTP client infrastructure must actually be present before Spring can provide its `RestClient.Builder`.

---

# 20. Problem 2: `NASA_API_KEY` missing

Error:

```text
Could not resolve placeholder 'NASA_API_KEY'
```

### Cause

`application.properties` contained:

```properties
nasa.api-key=${NASA_API_KEY}
```

but the environment variable was not available to the process running Spring Boot.

### Fix

Configured the variable in the IntelliJ Run Configuration:

```text
NASA_API_KEY=<your-key>
```

### Lesson

Environment variables belong to the **process environment**.

A variable existing in one PowerShell terminal does not automatically mean an IntelliJ-launched application can see it.

For IntelliJ:

```text
NAME=value
```

not:

```text
$env:NAME=value
```

---

# 21. Problem 3: `ADMIN_USERNAME` missing

Error:

```text
Could not resolve placeholder 'ADMIN_USERNAME'
```

### Cause

The application already required:

```properties
admin.username=${ADMIN_USERNAME}
admin.email=${ADMIN_EMAIL}
admin.password=${ADMIN_PASSWORD}
```

but the IntelliJ process did not have those environment variables.

### Fix

Configured all required environment variables in the IntelliJ Run Configuration.

### Lesson

When using externalized configuration, every required placeholder must exist in the environment from which the application is launched.

---

# 22. Problem 4: `mediaType` returned `null`

Initial response:

```json
"mediaType": null
```

### Cause

NASA sends:

```json
"media_type": "image"
```

while Java had:

```java
private String mediaType;
```

### Fix

Added:

```java
@JsonProperty("media_type")
private String mediaType;
```

### Result

```json
"mediaType": "image"
```

### Lesson

External JSON naming does not always match Java naming conventions.

Jackson annotations allow explicit mapping.

---

# 23. Problem 5: 403 Forbidden

The APOD request initially returned:

```text
403 Forbidden
```

### Cause

The endpoint was not included in the public authorization rules of the running security configuration.

Because the application contained:

```java
.anyRequest().denyAll()
```

anything not explicitly allowed was blocked.

### Fix

Added:

```java
.requestMatchers("/api/v1/apod")
.permitAll()
```

Then restarted the application and tested again without a Bearer token.

Result:

```text
200 OK
```

---

# 24. Why We Use a Client Layer

A common beginner implementation would be:

```java
@RestController
public class ApodController {

    @GetMapping("/apod")
    public Object getApod() {

        // Call NASA directly here
    }
}
```

This is bad architecture.

The controller would now know:

- NASA's URL
- API authentication
- HTTP communication
- response mapping
- potentially error handling
- potentially caching

Instead:

```text
Controller
    ↓
Service
    ↓
Client
    ↓
External API
```

Each layer has a focused responsibility.

---

# 25. Why We Use a DTO Instead of Returning Raw JSON

We could technically return:

```java
Map<String, Object>
```

or raw JSON.

But that gives us:

- no compile-time structure
- weaker type safety
- harder maintenance
- poorer readability

Instead:

```java
ApodResponse
```

gives us a defined contract.

For example:

```java
response.getTitle();
response.getExplanation();
response.getMediaType();
```

The code becomes strongly typed.

---

# 26. External API vs Database

Sprint 6 also demonstrates an important distinction.

Before:

```text
UniqueVerse
    ↓
PostgreSQL
```

Now:

```text
UniqueVerse
    ↓
External API
```

PostgreSQL is controlled by us.

NASA is not.

That means external integrations introduce new failure possibilities:

```text
NASA unavailable
Network failure
Timeout
Rate limit
Invalid API key
NASA 4xx
NASA 5xx
Unexpected response
```

This is why external API integration needs more defensive handling than simply calling `.body()` and hoping the universe cooperates.

---

# 27. Current Architecture

After Sprint 6:

```text
com.uniqueverse
│
├── apod
│   ├── client
│   │   └── NasaApodClient
│   │
│   ├── controller
│   │   └── ApodController
│   │
│   ├── dto
│   │   └── ApodResponse
│   │
│   └── service
│       └── ApodService
│
├── planet
│   ├── controller
│   ├── entity
│   ├── repository
│   └── service
│
├── security
│   ├── SecurityConfig
│   ├── JwtAuthenticationFilter
│   ├── JwtService
│   └── CustomUserDetailsService
│
└── exception
    ├── GlobalExceptionHandler
    └── ...
```

---

# 28. What Sprint 6 Teaches From a Spring Boot Perspective

### 1. External REST API consumption

Using:

```java
RestClient
```

to communicate with an external service.

### 2. Dependency injection

Spring injects:

```java
RestClient.Builder
NasaApodClient
ApodService
```

through constructors.

### 3. Configuration management

Using:

```properties
nasa.api-key=${NASA_API_KEY}
```

instead of hardcoding secrets.

### 4. DTO mapping

Using:

```java
@JsonProperty
```

to map external JSON into Java.

### 5. Layered architecture

```text
Controller → Service → Client
```

### 6. Spring Security integration

Using:

```java
permitAll()
```

for a public API.

### 7. End-to-end verification

Testing:

```text
Postman
   ↓
Spring Security
   ↓
Controller
   ↓
Service
   ↓
RestClient
   ↓
NASA
```

---

# 29. Interview Questions From Sprint 6

### Q1. How do you consume an external REST API in Spring Boot?

Use an HTTP client such as Spring's `RestClient`, isolate the communication in a client component, and invoke it from the service layer.

---

### Q2. Why shouldn't the controller call the external API directly?

Because the controller should handle HTTP concerns while the service handles application logic and the client handles external communication.

A cleaner structure is:

```text
Controller → Service → External Client
```

---

### Q3. Why use `RestClient`?

`RestClient` provides a modern synchronous API for making HTTP requests in Spring applications.

It provides fluent request construction and integrates with Spring's infrastructure.

---

### Q4. Why is the API key stored in an environment variable?

To prevent credentials from being hardcoded into source code and committed to version control.

---

### Q5. What does `@Value("${nasa.api-key}")` do?

It injects the value of the Spring property:

```properties
nasa.api-key
```

into the Java field/constructor parameter.

---

### Q6. Why use a DTO?

A DTO provides a typed representation of the data transferred between systems and prevents raw external JSON structures from leaking throughout the application.

---

### Q7. What does `@JsonProperty("media_type")` do?

It tells Jackson that the JSON property:

```text
media_type
```

maps to the Java field:

```text
mediaType
```

---

### Q8. What happens when `.body(ApodResponse.class)` is called?

Spring/Jackson deserializes the HTTP response body into an instance of `ApodResponse`.

---

### Q9. Why is `/api/v1/apod` configured with `permitAll()`?

Because APOD is a public UniqueVerse feature and users should not need authentication simply to retrieve the daily astronomy picture.

---

### Q10. Why is `.anyRequest().denyAll()` useful?

It creates a secure default.

If a new endpoint is added and nobody explicitly defines its authorization rule, the endpoint is denied instead of accidentally becoming public.

---

### Q11. What is the difference between an external API client and a service?

```text
Client
→ Technical communication with the external system.

Service
→ Application/business use case.
```

For UniqueVerse:

```text
NasaApodClient
→ Talks to NASA.

ApodService
→ Provides "get today's APOD" as a UniqueVerse use case.
```

---

# 30. Sprint 6 Mental Model

Remember this:

```text
             EXTERNAL SYSTEM
                   │
                   ▼
             NASA APOD API
                   │
                   ▼
            NasaApodClient
          "How do I call NASA?"
                   │
                   ▼
              ApodService
        "What does our app need?"
                   │
                   ▼
            ApodController
       "How does the client access it?"
                   │
                   ▼
          GET /api/v1/apod
```

The three layers answer three different questions:

```text
Controller
→ How does the outside world talk to us?

Service
→ What does UniqueVerse need to do?

Client
→ How do we talk to NASA?
```

---

# 31. Current Sprint 6 Status

### Completed

- [x] NASA APOD API integration
- [x] `RestClient` dependency
- [x] `ApodResponse` DTO
- [x] `NasaApodClient`
- [x] `ApodService`
- [x] `ApodController`
- [x] Environment-based NASA API key
- [x] `media_type` JSON mapping
- [x] Public security configuration
- [x] Postman verification
- [x] End-to-end `200 OK`

### Remaining before declaring the entire sprint closed

- [ ] External NASA error handling
- [ ] Timeout/failure strategy
- [ ] APOD caching
- [ ] Final failure/success verification
- [ ] Commit and push Sprint 6

---

# 32. Final Takeaway

Sprint 6 transformed UniqueVerse from an application that primarily communicates with its own database into an application that can communicate with an external service.

The core pattern is:

```text
REST Controller
      ↓
Service
      ↓
External API Client
      ↓
External REST API
```

For UniqueVerse:

```text
GET /api/v1/apod
      ↓
ApodController
      ↓
ApodService
      ↓
NasaApodClient
      ↓
NASA APOD
      ↓
ApodResponse
      ↓
200 OK
```

The most important thing to remember is not the NASA-specific code.

It is the **integration architecture**.

External APIs should be treated as unreliable boundaries, isolated behind a client layer, configured through externalized properties, mapped into controlled DTOs, and exposed through your own API contract.

That pattern will show up again and again in real backend systems.