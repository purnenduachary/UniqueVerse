# 🚀 Sprint 02 - Standard API Response & Exception Handling

## Sprint Goal

Build a standard API response model and implement global exception handling to ensure consistent and professional REST API responses.

---

# Why Standard API Response?

Without a common response format, every API returns different JSON structures.

Example:

API 1

```json
{
  "name":"Earth"
}
```

API 2

```json
{
  "planet":"Mars"
}
```

This becomes difficult for frontend developers.

---

# ApiResponse<T>

Created:

```java
ApiResponse<T>
```

Fields:

- success
- message
- data
- timestamp

---

# Why Generics?

Instead of creating:

PlanetResponse

UserResponse

FavoriteResponse

we use one reusable class.

Example:

```java
ApiResponse<PlanetDTO>

ApiResponse<UserDTO>

ApiResponse<List<PlanetDTO>>
```

---

# Why LocalDateTime?

Provides timestamp of response.

Example:

```
2026-07-23T10:15:30
```

Useful for debugging and logging.

---

# Factory Methods

Created:

```java
ApiResponse.success()

ApiResponse.error()
```

Reason:

Avoid repeated builder code.

Instead of:

```java
ApiResponse.builder()
...
```

Simply write:

```java
ApiResponse.success(...)
```

Cleaner.

Reusable.

---

# ResourceNotFoundException

Created custom exception.

```java
throw new ResourceNotFoundException("Planet not found");
```

Reason:

Instead of generic exceptions, use meaningful business exceptions.

---

# Global Exception Handler

Created:

```
GlobalExceptionHandler
```

Annotation:

```java
@RestControllerAdvice
```

Purpose:

Handle exceptions for the entire application.

---

# Mistake Made During Development

Initially used:

```java
@RestController
```

instead of

```java
@RestControllerAdvice
```

Result:

Spring did not recognize the class as a global exception handler.

Instead, the application showed the Whitelabel Error Page.

Fix:

Changed annotation to:

```java
@RestControllerAdvice
```

After fixing, exceptions were correctly converted into JSON responses.

---

# ResponseEntity

Purpose:

Allows returning:

- Status Code
- Headers
- Response Body

Example:

```java
return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(response);
```

---

# Exception Flow

```
Client

↓

Controller

↓

Service

↓

Exception Thrown

↓

GlobalExceptionHandler

↓

ApiResponse

↓

JSON Response
```

---

# Spring Security Issue

After adding Spring Security dependency, every endpoint redirected to the login page.

Reason:

Spring Security secures all endpoints by default.

Temporary Solution:

Created

```
SecurityConfig
```

and used

```java
.anyRequest().permitAll()
```

Authentication will be implemented later using JWT.

---

# Testing

Created:

```
GET /test
```

Inside:

```java
throw new ResourceNotFoundException("Planet not found");
```

Verified:

✔ Exception Handler executed

✔ HTTP 404 returned

✔ JSON response received

---

# Interview Questions

## What is Exception Handling?

Mechanism to handle runtime errors gracefully.

---

## Difference between checked and unchecked exceptions?

Checked:

Compile-time.

Unchecked:

RuntimeException.

---

## Why RuntimeException?

Business exceptions usually occur during execution.

Spring handles RuntimeException naturally.

---

## What is @RestControllerAdvice?

Global exception handler for REST APIs.

---

## Difference between @RestController and @RestControllerAdvice?

@RestController

Creates REST endpoints.

@RestControllerAdvice

Handles exceptions globally.

---

## What is ResponseEntity?

Represents the complete HTTP response.

---

## Why ApiResponse<T>?

Provides consistent response format across all APIs.

---

## What is a Factory Method?

A method responsible for creating objects.

Example:

```java
ApiResponse.success()

ApiResponse.error()
```

---

# Best Practices

✔ Never expose stack traces.

✔ Use custom exceptions.

✔ Return meaningful HTTP status codes.

✔ Keep response format consistent.

✔ Avoid try-catch inside every controller.

---

# Sprint Outcome

✅ ApiResponse created

✅ Generic response implemented

✅ Factory methods added

✅ ResourceNotFoundException created

✅ Global Exception Handler implemented

✅ SecurityConfig added

✅ Exception flow tested successfully

Sprint 2 Completed 🎉