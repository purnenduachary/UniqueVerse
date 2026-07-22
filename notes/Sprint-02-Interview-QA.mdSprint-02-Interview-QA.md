# 🚀 Sprint 02 - Interview Q&A Handbook (Part 1)
## Project: UniqueVerse

> Version: Sprint 02
> Focus: Package Structure, ApiResponse, Generics, Lombok, Builder Pattern, Factory Method, LocalDateTime

---

# Table of Contents

1. Package Structure
2. Common Package
3. ApiResponse<T>
4. Generics
5. Builder Pattern
6. Factory Method
7. Lombok
8. LocalDateTime
9. Boolean vs boolean
10. Standard API Response
11. Interview Questions
12. Best Practices

---

# 1. Why did we create multiple packages?

Instead of putting every class inside one package, we divided the project into multiple modules.

```
com.uniqueverse

├── auth
├── common
│     └── response
├── config
├── exception
├── favorite
├── nasa
├── planet
├── search
├── security
└── user
```

---

## Why?

Large projects become impossible to maintain if everything stays in one package.

Feature-based organization keeps code modular.

---

## Advantages

✔ Easier navigation

✔ Better scalability

✔ Cleaner architecture

✔ Easier collaboration

✔ Better maintainability

---

## Interview Question

Why Feature-Based Package Structure over Layer-Based Package Structure?

Feature-based packages group everything related to one feature together.

This improves scalability in enterprise projects.

---

# 2. Why Common Package?

```
common
```

stores reusable classes.

Example

```
ApiResponse

Constants

Utility Classes

Enums
```

---

## Why not inside Planet package?

Because multiple modules use ApiResponse.

Duplicating code violates the DRY Principle.

---

# 3. What is ApiResponse<T>?

ApiResponse is a generic wrapper class used to standardize API responses.

---

Instead of

```json
{
    "planet":"Earth"
}
```

every API returns

```json
{
    "success":true,
    "message":"Planet fetched successfully",
    "data":{
        ...
    },
    "timestamp":"2026-07-23T10:30:00"
}
```

---

## Fields

```
success

message

data

timestamp
```

---

## Why do we need ApiResponse?

Without it

Every endpoint returns different JSON.

Frontend developers must handle multiple response formats.

With ApiResponse

Every endpoint returns the same structure.

---

## Real World Example

Imagine Amazon.

Every API

- Login
- Products
- Orders
- Wishlist

returns responses in a standard format.

This makes frontend development much easier.

---

# 4. Why Generic ApiResponse<T>?

Instead of creating

```
PlanetResponse

UserResponse

FavoriteResponse
```

we create

```
ApiResponse<T>
```

---

Example

```
ApiResponse<UserDTO>

ApiResponse<PlanetDTO>

ApiResponse<List<PlanetDTO>>
```

---

One class.

Infinite possibilities.

---

# 5. What are Generics?

Generics allow classes and methods to work with multiple data types while maintaining compile-time type safety.

---

Example

Without Generics

```java
Object data;
```

Need casting.

---

With Generics

```java
T data;
```

No casting.

Compile-time safety.

---

Advantages

✔ Reusable

✔ Cleaner

✔ Type Safe

✔ Less Runtime Errors

---

# 6. Why letter T?

T stands for

Type

Convention only.

Could also write

```
E

K

V

R
```

But T is industry standard.

---

Interview Question

Can we replace T with Planet?

Yes.

Example

```java
ApiResponse<Planet>
```

But generic declaration should remain

```java
class ApiResponse<T>
```

---

# 7. Why use Builder Pattern?

Without Builder

```java
new ApiResponse(
true,
"Success",
planet,
LocalDateTime.now()
);
```

Hard to read.

---

With Builder

```java
ApiResponse.builder()

.success(true)

.message("Success")

.data(planet)

.timestamp(LocalDateTime.now())

.build();
```

Much cleaner.

---

Advantages

✔ Readability

✔ Optional Fields

✔ No Constructor Explosion

✔ Immutable Style Construction

---

# 8. What is Builder Pattern?

Builder Pattern is a Creational Design Pattern.

Instead of passing many constructor arguments,

objects are built step by step.

---

Interview Question

Who creates Builder?

Lombok

using

```
@Builder
```

---

Without Lombok

Developer writes Builder manually.

---

# 9. What is Lombok?

Lombok is a Java library that generates boilerplate code during compilation.

---

Annotations used

```
@Getter

@Setter

@Builder

@NoArgsConstructor

@AllArgsConstructor
```

---

Interview Question

Does Lombok generate code during Runtime?

No.

It generates code during Compilation.

---

Advantages

✔ Less Boilerplate

✔ Cleaner Classes

✔ Better Readability

---

# 10. Explain Every Lombok Annotation

## @Getter

Creates Getter methods.

---

## @Setter

Creates Setter methods.

---

## @Builder

Creates Builder Pattern automatically.

---

## @NoArgsConstructor

Creates Empty Constructor.

---

## @AllArgsConstructor

Creates Constructor with all fields.

---

# 11. Why avoid wildcard import?

Bad

```java
import lombok.*;
```

Good

```java
import lombok.Getter;

import lombok.Setter;

import lombok.Builder;
```

---

Reason

✔ Better Readability

✔ Enterprise Standard

✔ Explicit Imports

---

# 12. Why LocalDateTime?

Stores current date and time.

Example

```
2026-07-23T09:45:33
```

---

Advantages

✔ Immutable

✔ Thread Safe

✔ Better API

✔ Java 8 Time API

---

Interview Question

Why not Date?

Date API is old.

Mutable.

Poor API Design.

---

Interview Question

Why not Instant?

Instant stores UTC time.

Preferred in distributed systems.

LocalDateTime is sufficient for this project currently.

---

# 13. Why LocalDateTime.now()?

Returns current timestamp.

Useful for

- Logging

- API Responses

- Debugging

---

Example

```
timestamp

2026-07-23T10:45:20
```

---

# 14. boolean vs Boolean

boolean

Primitive

Cannot be null

Consumes less memory

Faster

---

Boolean

Wrapper Class

Can be null

Works with Collections

---

Interview Question

Why success field is boolean?

Because response should always be

true

or

false

Never null.

---

# 15. Why timestamp is useful?

Suppose user reports

"My API failed."

Timestamp helps identify

- Server Logs

- Failure Time

- Monitoring Events

---

# 16. Why Standard API Response?

Benefits

✔ Predictable JSON

✔ Easy Frontend Integration

✔ Easy Debugging

✔ Professional REST API Design

---

# 17. Factory Methods

Created

```java
ApiResponse.success()

ApiResponse.error()
```

---

Why?

Instead of writing

```java
ApiResponse.builder()
```

everywhere,

we centralized object creation.

---

Interview Question

What is Factory Method?

A Factory Method creates objects instead of exposing object creation logic.

---

Interview Question

Why static?

Because object creation should not require another object.

Example

Good

```java
ApiResponse.success(...)
```

Bad

```java
new ApiResponse()

.success(...)
```

---

# 18. Why declare <T> inside static method?

Example

```java
public static <T> ApiResponse<T> success(...)
```

Reason

Static methods belong to the class.

They cannot directly use class generic type.

Hence,

method-level generic declaration is required.

---

# Common Mistakes

❌ Returning different JSON from every endpoint

❌ Using Object instead of Generics

❌ Forgetting timestamp

❌ Using wildcard imports

❌ Creating multiple response classes

❌ Not using Builder

---

# Best Practices

✔ One response format

✔ Generic wrapper

✔ Factory Methods

✔ Explicit imports

✔ LocalDateTime

✔ Builder Pattern

✔ Feature-based package structure

---

# Sprint Outcome (Part 1)

✅ Feature-based Package Structure

✅ ApiResponse<T>

✅ Generics

✅ Builder Pattern

✅ Lombok

✅ LocalDateTime

✅ Factory Methods


# 🚀 Sprint 02 - Interview Q&A Handbook (Part 2)
## Project: UniqueVerse

> Version: Sprint 02
> Focus: Exception Handling, ResponseEntity, Spring Security Basics, Global Exception Handling, Debugging

---

# Table of Contents

1. Exception Handling
2. Exception Hierarchy
3. Checked vs Unchecked Exceptions
4. RuntimeException
5. Custom Exceptions
6. ResourceNotFoundException
7. super(message)
8. Global Exception Handling
9. @RestControllerAdvice
10. @ExceptionHandler
11. ResponseEntity
12. DispatcherServlet
13. ExceptionResolver
14. Spring Security Basics
15. SecurityFilterChain
16. SecurityConfig
17. CSRF
18. Debugging Diary
19. Common Mistakes
20. Best Practices

---

# 1. What is Exception Handling?

Exception Handling is the mechanism used to handle runtime errors gracefully without crashing the application.

Without Exception Handling

```
Application

↓

Exception

↓

Application Crashes
```

With Exception Handling

```
Application

↓

Exception

↓

Meaningful Response

↓

Application Continues
```

---

## Why do we need Exception Handling?

- Prevent application crashes
- Improve user experience
- Return meaningful error messages
- Easier debugging
- Maintain API consistency

---

# 2. Exception Hierarchy

```
Throwable

├── Error

│     ├── OutOfMemoryError
│     ├── StackOverflowError

└── Exception

      ├── IOException
      ├── SQLException

      └── RuntimeException

             ├── NullPointerException
             ├── ArithmeticException
             ├── IllegalArgumentException
             └── ResourceNotFoundException
```

---

# 3. Error vs Exception

## Error

Serious JVM problems.

Examples

- OutOfMemoryError
- StackOverflowError

Usually not handled.

---

## Exception

Problems that applications can recover from.

Example

```
Planet not found
```

---

# 4. Checked vs Unchecked Exception

## Checked Exception

Checked during compilation.

Compiler forces handling.

Examples

```
IOException

SQLException
```

---

## Unchecked Exception

Occurs during runtime.

Compiler does not force handling.

Examples

```
NullPointerException

ArithmeticException

RuntimeException
```

---

## Interview Question

Which one should business exceptions extend?

RuntimeException.

---

# 5. Why RuntimeException?

Business exceptions occur while application is running.

Examples

- User not found
- Planet not found
- Invalid Token
- Payment Failed

Hence custom exceptions extend RuntimeException.

---

Example

```java
public class ResourceNotFoundException
        extends RuntimeException {
}
```

---

# 6. What is Custom Exception?

Custom exceptions describe business problems.

Instead of

```java
throw new RuntimeException();
```

we write

```java
throw new ResourceNotFoundException(
"Planet not found");
```

Advantages

✔ Readability

✔ Better Debugging

✔ Business Meaning

---

# 7. Why ResourceNotFoundException?

Suppose

```
GET

/planet/999
```

Planet doesn't exist.

Instead of returning

```
500 Internal Server Error
```

we throw

```
ResourceNotFoundException
```

and return

```
404 Not Found
```

---

# 8. Why super(message)?

Constructor

```java
public ResourceNotFoundException(
String message){

super(message);

}
```

passes the message to RuntimeException.

Later

```
ex.getMessage()
```

returns that message.

---

Interview Question

Where is the message stored?

Inside RuntimeException.

---

# 9. What is Global Exception Handling?

Instead of

```
Controller A

try-catch

Controller B

try-catch

Controller C

try-catch
```

we create

```
GlobalExceptionHandler
```

One class.

Entire application.

---

Advantages

✔ DRY Principle

✔ Cleaner Controllers

✔ Centralized Error Handling

---

# 10. What is @RestControllerAdvice?

Used to handle exceptions globally for REST APIs.

Spring automatically calls methods inside this class whenever matching exceptions occur.

---

Interview Question

Difference between

@RestController

and

@RestControllerAdvice

@RestController

Creates REST endpoints.

@RestControllerAdvice

Handles exceptions globally.

---

# 11. Biggest Debugging Mistake

Initially

```java
@RestController
```

was used.

Instead of

```java
@RestControllerAdvice
```

Result

Whitelabel Error Page.

Reason

Spring treated the class as a controller instead of an exception handler.

Fix

Replace annotation.

Problem solved.

---

# 12. What is @ExceptionHandler?

Annotation that tells Spring

"If this exception occurs,

execute this method."

Example

```java
@ExceptionHandler(
ResourceNotFoundException.class)
```

---

# 13. How does Spring know which handler to execute?

Flow

```
HTTP Request

↓

DispatcherServlet

↓

Controller

↓

Service

↓

Exception

↓

ExceptionResolver

↓

@RestControllerAdvice

↓

JSON Response
```

---

# 14. What is DispatcherServlet?

DispatcherServlet is the Front Controller of Spring MVC.

Every HTTP request first reaches DispatcherServlet.

Responsibilities

- Receive Request
- Find Controller
- Execute Controller
- Handle Exceptions
- Return Response

---

Interview Question

Can request reach Controller without DispatcherServlet?

No.

DispatcherServlet is mandatory.

---

# 15. What is ExceptionResolver?

Component inside Spring MVC.

Job

Find matching ExceptionHandler.

If found

↓

Execute handler.

Else

↓

Default Error Response.

---

# 16. What is ResponseEntity?

Represents complete HTTP Response.

Contains

- Status Code
- Headers
- Body

---

Without ResponseEntity

Spring may return

```
200 OK
```

even for errors.

---

With ResponseEntity

```java
return ResponseEntity

.status(HttpStatus.NOT_FOUND)

.body(response);
```

Proper HTTP response.

---

Interview Question

Difference

ApiResponse

vs

ResponseEntity

ApiResponse

Only Body.

ResponseEntity

Entire HTTP Response.

---

# 17. Spring Security Surprise

After adding

```
spring-boot-starter-security
```

Browser displayed

Default Login Page.

Reason

Spring Boot secures every endpoint automatically.

---

# 18. Why Login Page appeared?

Spring Boot detected Security dependency.

Created default security configuration.

Every request became protected.

---

Request Flow

```
Browser

↓

Security Filters

↓

Authentication

↓

Authorization

↓

Controller
```

Since authentication failed

↓

Login Page.

---

# 19. Temporary Fix

Created

```
SecurityConfig
```

Configured

```java
.anyRequest().permitAll()
```

Now every endpoint became accessible.

---

Reason

Authentication module has not been built yet.

---

# 20. What is SecurityFilterChain?

Spring Security processes requests through multiple filters.

```
HTTP Request

↓

Filter

↓

Filter

↓

Authentication

↓

Authorization

↓

Controller
```

SecurityFilterChain configures this pipeline.

---

# 21. Why disable CSRF?

Configuration

```java
csrf(csrf -> csrf.disable())
```

Reason

Currently testing REST APIs.

Browser-based CSRF protection not required.

Later

JWT Authentication

will secure requests.

---

# 22. Testing Exception Handling

Created endpoint

```
GET

/test
```

Inside

```java
throw new ResourceNotFoundException(
"Planet not found");
```

Verified

✔ Exception thrown

✔ Exception caught

✔ JSON Response returned

✔ Status 404

---

# 23. Expected Response

```json
{
  "success": false,
  "message": "Planet not found",
  "data": null,
  "timestamp": "2026-07-23T11:10:00"
}
```

---

# 24. Debugging Diary

## Issue 1

Browser showed Login Page.

Reason

Spring Security enabled.

Solution

SecurityConfig

---

## Issue 2

Whitelabel Error Page.

Reason

@RestController

instead of

@RestControllerAdvice

Solution

Correct annotation.

---

## Issue 3

Exception not handled.

Reason

Global handler not registered.

Fix

@RestControllerAdvice

---

# Common Mistakes

❌ Using Exception instead of RuntimeException

❌ Returning stack traces

❌ Forgetting ResponseEntity

❌ Using @RestController

instead of

@RestControllerAdvice

❌ Writing try-catch in every controller

❌ Returning different JSON structures

❌ Ignoring HTTP Status Codes

---

# Best Practices

✔ Create custom exceptions

✔ Keep Controllers clean

✔ Use Global Exception Handler

✔ Return ResponseEntity

✔ Standardize responses

✔ Use RuntimeException for business errors

✔ Test exception flow

✔ Use meaningful messages

---

# Sprint 02 Summary

Concepts Covered

✔ Package Structure

✔ ApiResponse<T>

✔ Generics

✔ Builder Pattern

✔ Factory Method

✔ Lombok

✔ LocalDateTime

✔ boolean vs Boolean

✔ Exception Handling

✔ RuntimeException

✔ Checked vs Unchecked Exceptions

✔ ResourceNotFoundException

✔ super(message)

✔ @RestControllerAdvice

✔ @ExceptionHandler

✔ ResponseEntity

✔ DispatcherServlet

✔ ExceptionResolver

✔ Spring Security Basics

✔ SecurityFilterChain

✔ SecurityConfig

✔ CSRF

✔ Whitelabel Error

✔ Debugging

---

# Sprint 02 Revision Checklist

- [ ] ApiResponse<T>
- [ ] Generics
- [ ] Builder Pattern
- [ ] Factory Method
- [ ] Lombok
- [ ] LocalDateTime
- [ ] boolean vs Boolean
- [ ] Exception Hierarchy
- [ ] RuntimeException
- [ ] Custom Exceptions
- [ ] ResponseEntity
- [ ] @ExceptionHandler
- [ ] @RestControllerAdvice
- [ ] DispatcherServlet
- [ ] ExceptionResolver
- [ ] SecurityFilterChain
- [ ] SecurityConfig
- [ ] CSRF
- [ ] Debugging Process

---

🎯 **Sprint 02 Completed Successfully**

After Sprint 02, UniqueVerse now has a production-style response structure, centralized exception handling, a reusable API response wrapper, and the foundation for Spring Security and future JWT authentication.