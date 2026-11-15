# UniqueVerse --- Phase 1 / Sprint 4

## Concepts & Interview Preparation

## 1. Sprint 4 Scope

Sprint 4 completed the Planet and Favorites persistence work: Spring
Data JPA CRUD, derived queries, custom JPQL with `@Query`, pagination
and sorting, filtering, transactions and rollback behavior, JPA entity
lifecycle, dirty checking, `@ManyToOne`, owning side and foreign keys,
`FetchType.LAZY`, `LazyInitializationException`, DTO mapping,
`spring.jpa.open-in-view=false`, N+1 detection and `JOIN FETCH`,
Favorites APIs, and Flyway migration discipline.

## 2. Architecture

``` text
HTTP Request → Controller → Service → Repository → PostgreSQL
```

For related data:

``` text
Repository → Entity → Service maps Entity to DTO → Controller → JSON
```

The Controller handles HTTP concerns. The Service owns business logic,
transaction boundaries, and entity-to-DTO mapping. The Repository owns
database access. Entities represent persistence; DTOs represent the API
contract.

## 3. CRUD and JpaRepository

`JpaRepository<Planet, Long>` provides common operations such as
`save()`, `findById()`, `findAll()`, `delete()`, and `existsById()`.
CRUD means Create, Read, Update, Delete. `save()` does not necessarily
mean INSERT; JPA/Hibernate decides whether an entity is inserted or
updated based on its state.

## 4. Derived Queries

Example:

``` java
Optional<Planet> findByName(String name);
```

Spring Data derives the query from the method name. `Containing` means
substring matching and `IgnoreCase` means case-insensitive matching.
Derived queries are convenient for simple queries.

## 5. Custom JPQL with @Query

Sprint 4 used a combined filter:

``` java
@Query("""
    SELECT p FROM Planet p
    WHERE (:query IS NULL OR :query = ''
           OR LOWER(p.name) LIKE LOWER(CONCAT('%', :query, '%')))
    AND (:type IS NULL OR :type = ''
         OR LOWER(p.type) = LOWER(:type))
    """)
Page<Planet> findByFilters(String query, String type, Pageable pageable);
```

JPQL operates on entities and entity fields rather than directly on
database table and column names.

## 6. Pagination

`Pageable` represents page number, page size, and sorting. Example:
`?page=0&size=10&sort=name,asc`. Pagination should happen at the
database level instead of loading every row into memory.

The API uses a custom `PageResponse<T>` containing `content`, `page`,
`size`, `totalElements`, and `totalPages`. This gives the API a stable
response contract instead of coupling it to Spring Data's raw `Page`
serialization.

## 7. Transactions

`@Transactional` defines a transaction boundary. A successful unit of
work commits; a failed transaction can roll back. Transactions belong
naturally at the service layer because a service method represents the
business operation.

Read operations use `@Transactional(readOnly = true)`. This communicates
intent and can provide read optimizations, but it does not make writes
physically impossible.

## 8. JPA Entity Lifecycle

The main states are:

-   **Transient:** ordinary Java object, not associated with persistence
    context.
-   **Managed:** attached to the persistence context and tracked by
    Hibernate.
-   **Detached:** previously managed but no longer attached.
-   **Removed:** marked for deletion.

Conceptually: `Transient → Managed → Detached`, while a managed entity
can also become `Removed`.

## 9. Dirty Checking

Hibernate tracks managed entities. If a managed entity changes inside a
transaction, Hibernate can detect the change and issue an UPDATE during
flush/commit. An explicit `save()` is not always required for an
already-managed entity.

## 10. @ManyToOne and Owning Side

Favorites contain:

``` java
@ManyToOne(fetch = FetchType.LAZY)
@JoinColumn(name = "planet_id", nullable = false)
private Planet planet;
```

This represents `Many Favorites → One Planet`. `Favorite` is the owning
side because it contains the foreign-key mapping through `@JoinColumn`.

## 11. LAZY Loading

`FetchType.LAZY` means the related entity is not necessarily loaded
immediately. Hibernate can load it when the relationship is accessed
while the persistence context is active.

## 12. LazyInitializationException

With `spring.jpa.open-in-view=false`, the persistence context closes
when the service transaction ends. If Jackson later serializes a JPA
entity and accesses a lazy relationship, Hibernate cannot initialize it
and `LazyInitializationException` can occur.

Flow:

``` text
Controller → Service transaction → Favorite loaded → transaction ends
→ persistence context closes → Jackson accesses lazy Planet → exception
```

## 13. DTO Boundary

The fix was to map the entity to `FavoriteResponse` inside the
transactional service method:

``` java
return new FavoriteResponse(
    favorite.getId(),
    favorite.getPlanet().getId(),
    favorite.getPlanet().getName()
);
```

The controller returns plain DTO data rather than a JPA entity. The core
principle is: **entities are persistence models; DTOs are API models.**

## 14. Jackson

Jackson serializes Java objects into JSON and deserializes JSON into
Java objects. In Sprint 4, Jackson exposed the lazy-loading problem
because serialization occurred after the persistence context had closed.

## 15. N+1 Query Problem

N+1 occurs when one query loads a parent collection and N additional
queries load related data. For 100 Favorites this can become 101
queries. The API may still be functionally correct while performance
degrades as data grows.

## 16. JOIN FETCH

Sprint 4 fixed the Favorites N+1 problem with:

``` java
@Query("""
    SELECT f
    FROM Favorite f
    JOIN FETCH f.planet
    """)
List<Favorite> findAllWithPlanet();
```

`JOIN FETCH` tells Hibernate to fetch the related Planet as part of the
query. This is not the same as changing the relationship to EAGER. A
good pattern is to keep relationships appropriately lazy and explicitly
fetch related data for use cases that need it.

## 17. Favorite API

``` text
POST   /api/v1/favorites?planetId={id}
GET    /api/v1/favorites/{id}
GET    /api/v1/favorites
```

Create returns `201 Created`. Reads return `200 OK`. Missing resources
are handled as `404 Not Found`.

## 18. HTTP Vocabulary

-   **GET:** read a resource.
-   **POST:** create a resource.
-   **PUT:** replace/update a resource representation.
-   **PATCH:** partially update a resource.
-   **DELETE:** remove a resource.
-   **Path variable:** value inside the URL path, such as
    `/favorites/7`.
-   **Request parameter:** value after `?`, such as `?type=terrestrial`.
-   **Request body:** payload sent to the server.
-   **Response body:** payload returned by the server.

## 19. HTTP Status Codes

  Status   Meaning
  -------- ------------------------------------------
  200      Successful request
  201      Resource created
  204      Successful request with no response body
  400      Bad request
  404      Resource not found
  401      Authentication required/failed
  403      Authenticated but not authorized
  500      Server-side error

## 20. Exception Handling

`ResourceNotFoundException` represents missing resources.
`GlobalExceptionHandler` converts exceptions into consistent HTTP
responses, avoiding repetitive exception-handling code in controllers.

## 21. Flyway

Flyway versions database schema changes through ordered migrations such
as `V1`, `V2`, `V3`, and `V4`. Once an applied migration is shared, do
not edit it. Create the next migration instead. Editing an applied
migration can cause checksum and environment-consistency problems.

# Interview Questions

### What is JPA?

JPA is a Java specification for persistence and ORM. Hibernate is a
common implementation.

### What is Hibernate?

An ORM framework and common JPA implementation that maps Java objects to
relational database data and manages persistence.

### What is ORM?

Object-Relational Mapping maps Java objects to relational database
structures.

### What is JpaRepository?

A Spring Data abstraction providing CRUD, pagination, sorting, and
persistence operations.

### Derived query vs @Query?

Derived queries are generated from method names and are good for simple
cases. `@Query` provides explicit JPQL for more complex queries and
filtering.

### What is Pageable?

An abstraction representing pagination and sorting information.

### Why use a custom PageResponse?

To expose a stable API response contract and avoid coupling the API to
Spring Data's raw `Page` serialization.

### What is @Transactional?

It defines a transaction boundary around a method or class so a business
operation can commit or roll back as a unit.

### Where should @Transactional usually be placed?

At the service layer, because that is where business operations and
transaction boundaries belong.

### What is dirty checking?

Hibernate detects changes to managed entities and synchronizes them with
the database during flush/commit.

### What are JPA entity lifecycle states?

Transient, Managed, Detached, and Removed.

### What is @ManyToOne?

A relationship where many records of one entity reference one record of
another entity.

### What is the owning side?

The side that controls the database relationship, usually the side
containing the foreign-key mapping with `@JoinColumn`.

### What is lazy loading?

A strategy where related data is loaded when it is accessed rather than
immediately.

### Why does LazyInitializationException happen?

A lazy relationship is accessed after the Hibernate persistence
context/session has closed.

### How did we fix LazyInitializationException?

We kept the relationship LAZY, used a transactional service, accessed
the required relationship inside the transaction, mapped the result to a
DTO, and returned the DTO instead of the entity. We also use
`spring.jpa.open-in-view=false`.

### What is a DTO?

A Data Transfer Object designed to carry data across an application
boundary while keeping the API representation separate from persistence
entities.

### Why not return entities directly from controllers?

It can expose persistence details, trigger lazy loading, create
recursive serialization, cause accidental database access, and tightly
couple the API to the database model.

### What is Jackson?

The JSON serialization/deserialization library used by Spring Boot to
convert between Java objects and JSON.

### What is N+1?

One query loads the parent records and N additional queries load related
records.

### How did we fix N+1?

With an explicit `JOIN FETCH` query that fetches Favorites and their
Planet relationship together.

### Why not make everything EAGER?

EAGER loading can cause unnecessary database work and large object
graphs. Explicit fetching is more controlled.

### What does spring.jpa.open-in-view=false do?

It prevents the persistence context from being kept open for the entire
HTTP request, encouraging explicit transaction boundaries and preventing
hidden lazy database access from controllers/serialization.

### What is Flyway?

A database migration tool that versions schema changes and applies them
in order.

### Why not edit an applied Flyway migration?

Because Flyway tracks applied migrations and their checksums. Editing an
old migration can cause checksum mismatches and inconsistent
environments.

# Scenario Questions

### A Favorite entity is returned from a controller and Jackson throws LazyInitializationException. Why?

The service transaction ended before serialization. Jackson accessed the
lazy Planet after the persistence context closed. Map the entity to a
DTO inside the transactional service.

### The Favorites endpoint becomes slow with thousands of rows. What do you inspect first?

Check SQL logs and query behavior for an N+1 relationship-loading
problem. Use an explicit fetch strategy such as `JOIN FETCH` where
appropriate.

### A developer changes LAZY to EAGER to fix the exception. Is that a good fix?

Not generally. It can hide the symptom while introducing unnecessary
queries and object loading. Fix the transaction boundary and DTO mapping
instead.

### You need a new column after V4 has been applied. What do you do?

Create `V5__...sql`. Do not modify V4.

### A transactional method loads a managed Planet, changes its name, and never calls save(). Can it still update the database?

Yes. Dirty checking can detect the change and flush the UPDATE at
transaction commit.

# Quick Revision

``` text
JPA → persistence specification
Hibernate → ORM/JPA implementation
JpaRepository → CRUD + persistence abstraction
@Query → custom JPQL
Pageable → pagination + sorting
@Transactional → transaction boundary
Dirty checking → Hibernate detects managed-entity changes
@ManyToOne → many-to-one relationship
LAZY → related entity loaded when accessed
LazyInitializationException → lazy data accessed after context closes
DTO → API representation separate from entity
N+1 → one parent query + N relationship queries
JOIN FETCH → fetch relationship in the query
open-in-view=false → persistence context not kept open for whole request
Flyway → versioned database migrations
```

# Sprint 4 Mental Model

``` text
HTTP
 ↓
Controller
 ↓
Service
 ├── Transaction
 ├── Business logic
 └── Entity → DTO
 ↓
Repository
 ↓
Hibernate/JPA
 ↓
PostgreSQL
```

For relationships:

``` text
Favorite
   │
   └── LAZY → Planet

Service transaction active
   ↓
Access Planet safely
   ↓
Map to FavoriteResponse
   ↓
Transaction ends
   ↓
Controller returns DTO
   ↓
Jackson → JSON
```

For performance:

``` text
Bad:     1 Favorites query + N Planet queries
Better:  JOIN FETCH → Favorites + Planet
```

# Sprint 4 Final Takeaway

The key lesson is not merely how to write JPA code. It is to control
where persistence logic happens, control transaction boundaries, fetch
relationships deliberately, and keep database entities separate from the
public API contract.
