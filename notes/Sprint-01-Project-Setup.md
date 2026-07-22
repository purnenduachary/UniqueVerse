# 🚀 Sprint 01 - Project Setup

## Sprint Goal

The goal of Sprint 1 was to establish a strong foundation for the UniqueVerse backend application. Instead of jumping directly into feature development, we focused on creating a clean project structure, configuring the development environment, and understanding the architecture that will support future modules.

---

# Technologies Used

- Java 21
- Spring Boot 3.x
- Maven
- PostgreSQL
- IntelliJ IDEA
- Git & GitHub
- Lombok

---

# Project Creation

The project was created using Spring Initializr.

Dependencies:

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Lombok
- Spring Security
- Validation

---

# Why Spring Initializr?

Spring Initializr generates a production-ready project structure with all required dependencies.

Benefits:

- Saves setup time
- Standard Maven structure
- Automatic dependency management
- Easy configuration

---

# Maven

Maven is a build automation and dependency management tool.

Responsibilities:

- Download dependencies
- Build the project
- Run tests
- Package the application
- Maintain project lifecycle

Important File:

pom.xml

---

# Package Structure

```
com.uniqueverse
```

This acts as the root package.

Spring Boot automatically scans all sub-packages under the root package.

---

# Database

Database selected:

PostgreSQL

Reason:

- Open Source
- Production Ready
- Excellent Spring Boot support
- ACID compliant
- Widely used in enterprise applications

---

# application.yml

Purpose:

Central configuration file for:

- Database
- Server
- Logging
- Profiles
- Security
- JPA

---

# Why YAML instead of Properties?

YAML provides better readability.

Example:

```yaml
spring:
  datasource:
    username: postgres
    password: password
```

Compared to:

```properties
spring.datasource.username=postgres
spring.datasource.password=password
```

YAML becomes much easier to maintain as projects grow.

---

# Git Initialization

Commands:

```bash
git init
git add .
git commit -m "Initial Spring Boot project setup"
git branch -M main
git remote add origin <repo-url>
git push -u origin main
```

---

# Architecture Decision

The project follows Layered Architecture.

```
Controller
      ↓
Service
      ↓
Repository
      ↓
Database
```

Benefits:

- Separation of Concerns
- Maintainability
- Scalability
- Testability

---

# Concepts Learned

- Spring Initializr
- Maven
- Dependencies
- YAML
- PostgreSQL
- Git
- Layered Architecture

---

# Interview Questions

## What is Spring Boot?

Spring Boot is an opinionated framework built on top of Spring that simplifies Java application development by providing auto-configuration, embedded servers, and starter dependencies.

---

## What is Maven?

Maven is a build automation and dependency management tool.

---

## Why PostgreSQL?

- Open Source
- Fast
- Reliable
- Enterprise Ready
- Excellent JPA support

---

## Why YAML?

Better readability and hierarchical configuration.

---

# Best Practices

- Keep configuration in application.yml
- Never hardcode credentials
- Use Git from Day 1
- Keep package structure clean

---

# Sprint Outcome

✅ Project created

✅ Dependencies configured

✅ PostgreSQL connected

✅ GitHub repository created

✅ Application running successfully