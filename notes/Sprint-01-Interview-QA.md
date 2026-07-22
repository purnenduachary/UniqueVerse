# 🚀 Sprint 01 - Interview Q&A Handbook
## Project: UniqueVerse

> Version: Sprint 01
> Focus: Project Setup, Spring Boot Fundamentals, Maven, Package Structure, PostgreSQL, YAML, Layered Architecture

---

# 1. What is Spring?

## Answer

Spring is an open-source Java framework used to build enterprise-level applications. It provides features like Dependency Injection (DI), Inversion of Control (IoC), transaction management, security, data access, and much more.

---

## Why was Spring created?

Before Spring, Enterprise Java applications used EJB (Enterprise Java Beans), which were:

- Complex
- Heavyweight
- Difficult to test
- Required a lot of configuration

Spring solved these problems by making Java development lightweight and modular.

---

## Real World Example

Think of Spring as the operating system of your application.

Just like Windows manages hardware resources, Spring manages Java objects (Beans), dependencies, configurations, and application lifecycle.

---

## Interview Follow-up

### Is Spring Framework and Spring Boot same?

No.

Spring Framework is the core framework.

Spring Boot is built on top of Spring Framework and removes boilerplate configuration.

---

# 2. What is Spring Boot?

## Answer

Spring Boot is an extension of the Spring Framework that simplifies application development by providing:

- Auto Configuration
- Starter Dependencies
- Embedded Servers
- Production Ready Features

---

## Why Spring Boot?

Without Spring Boot:

You manually configure

- Tomcat
- XML
- Dependencies
- DispatcherServlet

With Spring Boot:

Most configurations happen automatically.

---

## Advantages

- Faster Development
- Less Configuration
- Embedded Server
- Production Ready
- Easy Deployment

---

## Interview Question

### Does Spring Boot replace Spring?

No.

Spring Boot is built on top of Spring Framework.

---

# 3. What is Spring Initializr?

## Answer

Spring Initializr is an online tool that generates a production-ready Spring Boot project.

Website:

https://start.spring.io

---

## Why use Spring Initializr?

Instead of manually creating:

- Maven project
- Folder structure
- Dependencies

Spring Initializr generates everything automatically.

---

## Generated Files

- pom.xml
- src/
- resources/
- application.properties
- Main Application Class

---

# 4. What is Maven?

## Answer

Maven is a Build Automation and Dependency Management Tool.

---

## Responsibilities

- Download dependencies
- Compile project
- Run tests
- Package application
- Generate JAR

---

## Important File

pom.xml

---

## Interview Question

### Why Maven?

Without Maven

Every library must be downloaded manually.

With Maven

Everything is downloaded automatically from Maven Central Repository.

---

# 5. What is pom.xml?

## Answer

pom.xml stands for Project Object Model.

It contains

- Dependencies
- Plugins
- Build Configuration
- Java Version
- Project Metadata

---

# 6. What are Dependencies?

Dependencies are external libraries required by our application.

Example

Spring Web

Spring Data JPA

Lombok

PostgreSQL Driver

---

# 7. What is Layered Architecture?

```
Client

↓

Controller

↓

Service

↓

Repository

↓

Database
```

---

## Why use Layered Architecture?

- Separation of Concerns
- Easy Testing
- Maintainability
- Scalability
- Reusability

---

# 8. Why shouldn't Business Logic be written inside Controller?

Controller should only

- Receive Request
- Validate Request
- Return Response

Business logic belongs inside Service Layer.

---

# 9. What is Package Structure?

Packages organize related classes.

Our project uses

```
com.uniqueverse

auth
common
config
exception
favorite
nasa
planet
search
security
user
```

---

## Why Feature Based Package Structure?

Each module becomes independent.

Easy to maintain.

Easy to scale.

---

# 10. Why common package?

Stores reusable code.

Example

ApiResponse

Constants

Utilities

Enums

---

# 11. Why exception package?

Stores

- Custom Exceptions
- Global Exception Handler

Centralized error handling.

---

# 12. Why security package?

Stores

- Security Configuration
- JWT
- Filters
- Authentication Logic

---

# 13. Why PostgreSQL?

Advantages

- Open Source
- Enterprise Ready
- ACID Compliant
- Fast
- Excellent Spring Support

---

# 14. What is application.yml?

Central configuration file.

Stores

- Database
- Server
- Logging
- Security
- JPA

---

# 15. Why YAML over Properties?

YAML is hierarchical.

Example

```yaml
spring:
  datasource:
    username: postgres
```

Compared to

```properties
spring.datasource.username=postgres
```

YAML is easier to read.

---

# 16. What is Git?

Git is a Distributed Version Control System.

Tracks source code changes.

---

# 17. Why Git?

Benefits

- Version History
- Collaboration
- Rollback
- Branching
- Backup

---

# 18. What is GitHub?

GitHub is a cloud platform for hosting Git repositories.

---

# 19. Difference between Git and GitHub?

Git

Version Control Tool

GitHub

Cloud Hosting Platform

---

# 20. Why commit frequently?

Benefits

- Easy rollback
- Better history
- Easier debugging
- Team collaboration

---

# 21. What is a Good Commit Message?

Good

```
feat: add project setup
```

Bad

```
updated
```

---

# 22. Why Layered Architecture instead of writing everything in one class?

Single class becomes

- Hard to maintain
- Hard to debug
- Hard to test

Layered architecture separates responsibilities.

---

# 23. What is Embedded Tomcat?

Spring Boot includes Tomcat inside the application.

No need to install Tomcat separately.

---

# 24. What happens when Spring Boot application starts?

1. main() executes.
2. SpringApplication.run() starts.
3. IoC Container is created.
4. Beans are initialized.
5. Embedded Tomcat starts.
6. Application becomes ready.

---

# 25. What is IoC Container?

Spring Container that creates and manages Beans.

Instead of developers creating objects,

Spring creates objects.

---

# 26. What is Dependency Injection?

Instead of

```java
new Service();
```

Spring automatically injects required objects.

---

# 27. What is a Bean?

Any object managed by Spring IoC Container.

---

# 28. Why Root Package Matters?

Spring Boot scans packages from the package where the main class exists.

Anything outside root package won't be detected automatically.

---

# 29. What is Component Scanning?

Spring automatically searches for

- @Component
- @Service
- @Repository
- @Controller

and registers them as Beans.

---

# 30. Sprint 01 Key Learnings

✅ Spring Framework

✅ Spring Boot

✅ Spring Initializr

✅ Maven

✅ pom.xml

✅ Dependencies

✅ PostgreSQL

✅ application.yml

✅ Git

✅ GitHub

✅ Layered Architecture

✅ Package Structure

✅ Root Package

✅ Component Scanning

✅ IoC

✅ Dependency Injection

✅ Bean Basics

---

# Revision Checklist

- [ ] Spring Framework
- [ ] Spring Boot
- [ ] Spring Initializr
- [ ] Maven
- [ ] pom.xml
- [ ] Dependencies
- [ ] PostgreSQL
- [ ] YAML
- [ ] Git
- [ ] GitHub
- [ ] Layered Architecture
- [ ] Package Structure
- [ ] Root Package
- [ ] Component Scanning
- [ ] IoC
- [ ] Dependency Injection
- [ ] Bean
- [ ] Embedded Tomcat