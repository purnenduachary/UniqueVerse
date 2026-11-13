# UniqueVerse — Sprint 03
## Spring Core & Dependency Injection: Concepts + Interview Q&A

**Focus:** IoC, Dependency Injection, Spring Beans, ApplicationContext, Component Scanning, Stereotype Annotations, `@Configuration`, `@Bean`, Constructor Injection, Bean Lifecycle and Bean Scopes.

---

## 1. Sprint Objective

Sprint 03 explains **how Spring creates, manages, and connects objects**.

By the end, you should be able to explain and use:

- IoC and Dependency Injection
- Spring Beans and the IoC container
- `ApplicationContext`
- Component scanning
- `@Component`, `@Service`, `@Repository`
- `@Controller`, `@RestController`
- `@Configuration` and `@Bean`
- Constructor injection
- Bean lifecycle basics
- Bean scopes
- Spring Boot auto-configuration
- How these concepts appear in UniqueVerse

---

# Part I — Concepts

## 2. Inversion of Control (IoC)

IoC means that object creation and dependency management are controlled by the Spring container instead of application classes manually creating their dependencies.

### Without Spring

```java
public class PlanetController {
    private PlanetService planetService = new PlanetService();
}
```

The controller creates its dependency.

### With Spring

```java
@RestController
public class PlanetController {

    private final PlanetService planetService;

    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }
}
```

The controller does not create `PlanetService`. Spring creates and supplies it.

**Mental model:** IoC = Spring controls object creation and wiring.

---

## 3. Dependency Injection (DI)

DI is the mechanism through which an object's dependencies are provided from outside.

```java
public class PlanetService {

    private final PlanetRepository planetRepository;

    public PlanetService(PlanetRepository planetRepository) {
        this.planetRepository = planetRepository;
    }
}
```

`PlanetService` depends on `PlanetRepository`, but it does not create the repository itself.

### IoC vs DI

- **IoC** = broader principle.
- **DI** = mechanism used to achieve IoC.

---

## 4. Why DI?

DI provides:

- Lower coupling
- Easier testing
- Easier replacement of implementations
- Explicit dependencies
- Centralized object management

---

## 5. Spring Bean

A **Spring Bean** is an object created and managed by the Spring IoC container.

Examples in UniqueVerse include:

- `PlanetService`
- `PlanetController`
- `GlobalExceptionHandler`
- `SecurityConfig` configuration Beans

A normal object created with `new` is not automatically a Spring Bean.

---

## 6. IoC Container and ApplicationContext

The Spring IoC container:

1. Discovers/configures Beans
2. Creates Bean instances
3. Resolves dependencies
4. Injects dependencies
5. Manages Bean lifecycle

`ApplicationContext` is the central Spring container abstraction.

At startup:

```java
SpringApplication.run(UniqueVerseApplication.class, args);
```

creates the application context and starts the application.

### Mental model

```text
SpringApplication.run()
        ↓
ApplicationContext
        ↓
Discover/configure Beans
        ↓
Resolve dependencies
        ↓
Inject dependencies
        ↓
Application ready
```

---

## 7. Component Scanning

Component scanning lets Spring discover classes that should become Beans.

Example:

```java
@Service
public class PlanetService {
}
```

Spring detects the class and registers it as a Bean.

`@SpringBootApplication` includes component-scanning behavior.

Because UniqueVerse's main class is under `com.uniqueverse`, Spring can discover components under that package hierarchy.

---

## 8. Stereotype Annotations

### `@Component`

Generic Spring-managed component.

```java
@Component
public class PlanetMapper {
}
```

### `@Service`

Used for business/service logic.

```java
@Service
public class PlanetService {
}
```

### `@Repository`

Used for persistence/data-access components.

```java
@Repository
public interface PlanetRepository extends JpaRepository<Planet, Long> {
}
```

Spring Data supplies the implementation.

### `@Controller`

Used for Spring MVC controllers.

### `@RestController`

Used for REST APIs.

Conceptually:

```text
@RestController = @Controller + @ResponseBody
```

---

## 9. `@Configuration`

`@Configuration` marks a class as a source of Bean definitions.

UniqueVerse uses:

```java
@Configuration
public class SecurityConfig {
}
```

---

## 10. `@Bean`

`@Bean` registers the object returned by a method as a Spring Bean.

UniqueVerse:

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.build();
}
```

### `@Component` vs `@Bean`

Use `@Component` when Spring should discover a class automatically.

Use `@Bean` when explicit configuration is appropriate or when the object is created through a configuration method.

---

## 11. Constructor Injection

UniqueVerse uses constructor injection.

```java
@Service
public class PlanetService {

    private final PlanetRepository planetRepository;

    public PlanetService(PlanetRepository planetRepository) {
        this.planetRepository = planetRepository;
    }
}
```

Controller:

```java
@RestController
public class PlanetController {

    private final PlanetService planetService;

    public PlanetController(PlanetService planetService) {
        this.planetService = planetService;
    }
}
```

### Why preferred?

- Dependencies are explicit.
- Dependencies can be `final`.
- Easier unit testing.
- Prevents the object from being constructed without required dependencies.
- Avoids hidden dependencies associated with field injection.

---

## 12. Field Injection vs Constructor Injection

### Field injection

```java
@Autowired
private PlanetService planetService;
```

Problems:

- Dependency is hidden.
- Harder to unit test cleanly.
- Field cannot normally be `final`.

### Constructor injection

```java
private final PlanetService planetService;

public PlanetController(PlanetService planetService) {
    this.planetService = planetService;
}
```

This is the approach used in UniqueVerse.

---

## 13. Bean Lifecycle — Basics

Simplified lifecycle:

```text
Bean definition
      ↓
Instantiation
      ↓
Dependency injection
      ↓
Initialization
      ↓
Bean ready
      ↓
Application shutdown
      ↓
Destruction
```

Spring provides lifecycle callbacks such as:

```java
@PostConstruct
@PreDestroy
```

---

## 14. Bean Scopes

Bean scope determines how Spring manages Bean instances.

| Scope | Meaning |
|---|---|
| Singleton | One instance per Spring container |
| Prototype | New instance when requested |
| Request | One instance per HTTP request |
| Session | One instance per HTTP session |

The default scope is **singleton**.

---

## 15. Spring Boot Auto-Configuration

Spring Boot automatically configures infrastructure based on the application's dependencies and configuration.

For example:

- Spring Web provides web infrastructure.
- Spring Data JPA provides JPA-related infrastructure.
- Spring Security provides security infrastructure.

Auto-configuration does not invent your business logic. It configures supported infrastructure based on conditions.

---

# Part II — UniqueVerse Wiring

The architecture is:

```text
HTTP Request
     ↓
PlanetController
     ↓
PlanetService
     ↓
PlanetRepository
     ↓
PostgreSQL
```

Spring connects these layers.

```text
PlanetController
      │
      │ constructor injection
      ▼
PlanetService
      │
      │ constructor injection
      ▼
PlanetRepository
```

This is IoC and DI in the actual project.

---

# Part III — Interview Q&A

## Q1. What is IoC in Spring?

**Answer:** IoC means Inversion of Control. Instead of application classes creating and managing dependencies themselves, the Spring container manages object creation, dependency resolution, and dependency injection.

## Q2. What is Dependency Injection?

**Answer:** DI is a technique where an object's dependencies are provided from outside instead of the object creating them itself.

## Q3. Difference between IoC and DI?

**Answer:** IoC is the broader principle. DI is a mechanism used to implement IoC by supplying dependencies to objects.

## Q4. What is a Spring Bean?

**Answer:** A Spring Bean is an object instantiated and managed by the Spring IoC container.

## Q5. Who creates Spring Beans?

**Answer:** The Spring IoC container creates and manages them. Beans can be discovered through component scanning or explicitly registered with `@Bean`.

## Q6. What is `ApplicationContext`?

**Answer:** It is Spring's central IoC container abstraction. It manages Beans, resolves dependencies, performs injection, and manages application context lifecycle.

## Q7. What is component scanning?

**Answer:** It is the process by which Spring searches configured packages for classes such as `@Component`, `@Service`, `@Repository`, and `@Controller`, then registers them as Beans.

## Q8. What does `@SpringBootApplication` do?

**Answer:** It is a convenience annotation combining configuration, auto-configuration, and component-scanning behavior.

## Q9. What is `@Component`?

**Answer:** It marks a class as a Spring-managed component so component scanning can register it as a Bean.

## Q10. `@Component` vs `@Service`?

**Answer:** Both can register a Bean. `@Service` is a specialized stereotype that communicates that the class belongs to the service/business layer.

## Q11. `@Component` vs `@Repository`?

**Answer:** `@Repository` is a specialized persistence/data-access stereotype. It also participates in Spring's repository-related exception translation support.

## Q12. `@Controller` vs `@RestController`?

**Answer:** `@Controller` is used for MVC controllers and can return views. `@RestController` is intended for REST APIs and combines `@Controller` with `@ResponseBody`.

## Q13. What is `@Configuration`?

**Answer:** It marks a class as a source of Bean definitions and application configuration.

## Q14. What is `@Bean`?

**Answer:** It marks a method whose returned object should be registered and managed as a Spring Bean.

## Q15. When use `@Bean` instead of `@Component`?

**Answer:** Use `@Component` when Spring should discover the class automatically. Use `@Bean` for explicit configuration or when registering an object produced by a configuration method.

## Q16. Why is constructor injection preferred?

**Answer:** It makes dependencies explicit, supports `final` fields, improves testability, and prevents required dependencies from being omitted.

## Q17. Is `@Autowired` required for a single constructor?

**Answer:** No. Spring can use the single constructor automatically for dependency injection.

## Q18. What happens if Spring cannot find a required dependency?

**Answer:** Application startup generally fails because Spring cannot create the dependent Bean.

## Q19. What if two Beans of the same type are available?

**Answer:** Spring can report an ambiguity such as `NoUniqueBeanDefinitionException`. `@Qualifier` or `@Primary` can resolve the choice.

## Q20. What is the default Bean scope?

**Answer:** Singleton.

## Q21. Does singleton mean one object for the entire JVM?

**Answer:** No. It means one instance per Spring IoC container.

## Q22. Spring Bean vs normal Java object?

**Answer:** A normal object is generally created and managed directly by application code. A Spring Bean is created and managed by Spring, including dependency injection and lifecycle management.

## Q23. How does `PlanetController` get `PlanetService`?

**Answer:** `PlanetService` is a Spring Bean because it uses `@Service`. Spring resolves that Bean and passes it into the `PlanetController` constructor.

## Q24. Explain UniqueVerse's dependency chain.

**Answer:**

```text
PlanetController
      ↓
PlanetService
      ↓
PlanetRepository
```

Spring creates and wires these components through dependency injection.

## Q25. Why should controllers avoid business logic?

**Answer:** Controllers should handle HTTP concerns. Business logic belongs in services so it remains separated, reusable, and easier to test.

## Q26. Why make injected dependencies `final`?

**Answer:** It prevents reassignment after constructor initialization and makes the dependency relationship explicit.

## Q27. What happens during Spring startup?

**Answer:** Spring creates the application context, discovers/configures Beans, resolves dependencies, creates and initializes them, and starts the application infrastructure.

## Q28. What is Bean lifecycle?

**Answer:** A simplified lifecycle is definition → instantiation → dependency injection → initialization → ready → destruction.

## Q29. What is tight coupling?

**Answer:** Tight coupling occurs when a class directly depends on concrete implementations or creates its dependencies itself. DI reduces this coupling.

## Q30. How does DI improve unit testing?

**Answer:** Dependencies can be replaced with mocks or test implementations.

```java
PlanetRepository repository = mock(PlanetRepository.class);
PlanetService service = new PlanetService(repository);
```

The service can then be tested independently of the real repository/database.

---

# Part IV — Interview Trap Questions

### Trap 1: Is `@Service` fundamentally different from `@Component`?

**Correct:** Not for basic Bean registration. `@Service` is a specialized stereotype communicating service-layer intent.

### Trap 2: Does Spring create every Java object?

**Correct:** No. Spring manages objects registered as Beans. Objects created manually with `new` are not automatically Spring-managed.

### Trap 3: Does `@Autowired` create the dependency?

**Correct:** No. It indicates where Spring should inject a dependency. The container resolves and supplies it.

### Trap 4: Is IoC the same as DI?

**Correct:** No. IoC is the principle; DI is a mechanism for achieving it.

### Trap 5: Does singleton mean exactly one instance forever?

**Correct:** No. It means one instance per Spring IoC container.

### Trap 6: Can `@Bean` be used anywhere?

**Correct:** It is intended for Bean-definition methods, normally inside configuration classes.

---

# Part V — Revision Checklist

Before Sprint 04, explain these without notes:

- [ ] IoC
- [ ] DI
- [ ] IoC vs DI
- [ ] Spring Bean
- [ ] IoC container
- [ ] ApplicationContext
- [ ] Component scanning
- [ ] `@SpringBootApplication`
- [ ] `@Component`
- [ ] `@Service`
- [ ] `@Repository`
- [ ] `@Controller`
- [ ] `@RestController`
- [ ] `@Configuration`
- [ ] `@Bean`
- [ ] Constructor injection
- [ ] Field vs constructor injection
- [ ] Bean lifecycle
- [ ] Bean scopes
- [ ] Singleton
- [ ] Auto-configuration
- [ ] Controller → Service → Repository wiring

---

# Sprint 03 Verdict

Sprint 03 is complete when these concepts can be traced through actual UniqueVerse code rather than merely memorized.

The core mental model:

```text
Spring Boot starts
      ↓
ApplicationContext is created
      ↓
Component scanning finds components
      ↓
Spring creates Beans
      ↓
Spring resolves dependencies
      ↓
Constructor injection wires layers
      ↓
Application is ready
```

**Next:** Sprint 04 — Persistence / Spring Data JPA / Entity Mapping / Repository Layer.
