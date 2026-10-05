# cleanddd — project context (short)

> **Charter — current facts, scannable index.**
> This file owns the **quick-reference** view of the project as it exists *today*:
> stack, top-level package layout, entry points, run/test commands, repo workflow,
> and any short fact tables (glossary, use-case index, profile table, key files)
> that earn their place by being scannable.
>
> - Update when a stack version changes, a new top-level package appears, a use
>   case is added or renamed, or a run/test command changes.
> - Do **not** put rationale, history, or architectural discussion here. Those
>   belong in `design-notes.md`.
> - Do **not** put recipes or long-form conventions here. Those belong in
>   `project-context-extended.md`.
> - Do **not** keep a per-issue changelog or "Recent Changes" log here. Change
>   history is git's job; memory holds the distilled current state. See
>   `session-wrap-up.md` §"Memory is not a changelog".

## What this is

A single-use-case Spring Boot REST demo in which students are created, courses are
created, and a student is enrolled in a course. It accompanies the owner's *Clean DDD*
article (link in `README.md`) and exists to demonstrate Clean Architecture + DDD with
use-case-driven transaction demarcation. Tutorial / reference project on public GitHub
(`gushakov/cleanddd`); no production deployment.

## Stack

| Concern | Choice |
|---|---|
| Java | 17 (`<java.version>` in `pom.xml`) |
| Build | Maven, `spring-boot-starter-parent` **2.7.18** (Boot 2.x, `javax.*` namespace) |
| Web | Spring MVC (`spring-boot-starter-web`), springdoc-openapi-ui 1.6.11 (Swagger UI at `/swagger-ui/index.html`) |
| Persistence | **JPA / Hibernate** via `spring-boot-starter-data-jpa` + `NamedParameterJdbcOperations` for the one read model; PostgreSQL |
| Schema | Hibernate `ddl-auto=update` — **no Flyway**, no migration scripts |
| Mapping | Hand-written `DefaultDbMapper` (no MapStruct) |
| Lombok | yes; `lombok.config` copies `@Qualifier` onto generated constructors |
| Tests | JUnit 5, Mockito, AssertJ, ArchUnit 1.0.1 |
| Local DB | `docker-compose.yml` — single `postgres:latest` service on 5432, DB `postgres`, credentials in `application.properties` (demo values) |

## Package layout (`com.github.cleanddd`)

```
core/
  GenericEnrollmentError                 ← root runtime error for the whole core
  model/
    InvalidDomainEntityError             ← always-valid construction error
    course/Course                        ← aggregate root
    student/Student                      ← aggregate root
    enrollment/Enrollment, EnrollResult  ← read-model VO; enrollment outcome VO
  port/
    ErrorHandlingPresenterOutputPort     ← base presenter port (presentError)
    db/PersistenceOperationsOutputPort, PersistenceError, EntityDoesNotExistError
    transaction/TransactionOperationsOutputPort, TransactionRunnableWithResult,
                TransactionRunnableWithoutResult
  usecase/
    enrollstudent/EnrollStudentInputPort, EnrollStudentUseCase,
                  EnrollStudentPresenterOutputPort
infrastructure/
  CleanDddApplication                    ← @SpringBootApplication main class
  adapter/db/PersistenceGateway + course/, student/ (JPA entities + repos),
             enrollment/ (EnrollmentRow read model + SQL, EnrollmentsQuery), map/
  adapter/transaction/SpringTransactionAdapter
  adapter/web/AbstractRestPresenter + enrollstudent/ (controller, presenter, DTOs)
  config/UseCaseConfig, TransactionConfig
```

## Use-case index

| Summary goal (package) | Input port | Interactions |
|---|---|---|
| `enrollstudent` | `EnrollStudentInputPort` | `createCourse(title)`, `createStudent(fullName)`, `enroll(courseId, studentId)`, `findEnrollmentsForStudent(studentId)` |

REST entry points (all `POST`, JSON body): `/create-course`, `/create-student`,
`/enroll`, `/enrollments`.

## Run and test

- Needs JDK 17 on `JAVA_HOME` (match `<java.version>`).
- Run the app: start Postgres with `docker compose up -d`, then `mvn spring-boot:run`
  (port 8080). Hibernate creates/updates the three tables on startup.
- Tests: `mvn test` — 9 unit tests, **no database needed** (no Spring context tests).
- Single test class: `mvn test -Dtest=EnrollStudentUseCaseTest`.
- Logging is `debug` for the project package and `org.springframework.orm`, and
  `spring.jpa.show-sql=true` — transaction/SQL tracing is on by design.

## Repo workflow

- Public GitHub, remote `origin`, default branch `main`.
- Integration branch `dev` exists **locally only** at onboarding — push it before
  opening a PR with `--base dev`; earlier PRs (`#1`, `#2`) targeted `main`.
- `.claude/` is versioned (machine-local `settings.local.json` excluded); keep
  committed content institution-neutral and free of machine setup.
- Standard flow otherwise: `claude/<issue>_*` branch from `dev`, PR `--base dev`.
- Issues are plain `gh issue create` — no assignee, no sprint.
