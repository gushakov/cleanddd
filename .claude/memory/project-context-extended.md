# cleanddd — project context (extended)

> **Charter — deep reference for current facts, recipes, conventions.**
> This file owns the **on-demand deep reference** for what exists today: domain
> model overview, use-case inventory, infrastructure adapters, persistence /
> security / frontend / test specifics, code conventions (Lombok shape, mapper
> wiring, naming patterns), end-to-end recipes, and recorded methodology
> deviations.
>
> - Update when a convention changes, a pattern is added, a new adapter family
>   appears, or a recipe needs revising.
> - Facts only. Rationale ("why we chose X over Y") belongs in `design-notes.md`.
> - Quick-reference one-liners belong in `project-context.md`, not here.

## Domain model overview

Two aggregate roots and two value objects, all in `core/model/`.

- **`Course`** (`core/model/course/`) — `id: Integer`, `title: String`,
  `numberOfStudents` held internally as an `AtomicInteger` but exposed as `Integer`.
  Validating `@Builder` constructor: a null or blank title throws
  `InvalidDomainEntityError`; a null count defaults to 0. Equality by `id` only.
  Behaviour: `enrollStudent()` returns a **new** `Course` with the counter incremented
  (copy-on-write via a private `newCourse()` builder; the instance's counter is never
  mutated).
- **`Student`** (`core/model/student/`) — `id`, `fullName`, `coursesIds: Set<Integer>`.
  Null or blank full name throws `InvalidDomainEntityError`; the set is wrapped
  unmodifiable, null defaults to `Set.of()`. Equality by `id`. Behaviour:
  `enrollInCourse(courseId)` returns an `EnrollResult` carrying a new `Student` with
  the course id added and a `courseAdded` flag (false when already enrolled).
- **`EnrollResult`** (`core/model/enrollment/`) — `@Value` outcome VO: `student` +
  `courseAdded`.
- **`Enrollment`** (`core/model/enrollment/`) — `@Value` read-model VO: `courseId` +
  `courseTitle`. Not an entity; it is the projection returned by `findEnrollments`.

Inter-aggregate rule, as coded in the use case: enrolling touches both aggregates —
`Student` gains the course id, `Course` increments its counter — and the two writes
are atomic by virtue of the use case's transaction. No `@With` witherpaths, no
Specification objects, no domain events. Ids are database-generated `Integer`s, so a
freshly built aggregate has `id == null` until persisted.

Error hierarchy: `GenericEnrollmentError` (`core/`, extends `RuntimeException`) is the
root. `InvalidDomainEntityError` (`core/model/`) and `PersistenceError`
(`core/port/db/`, with subclass `EntityDoesNotExistError`) extend it.

## Use case inventory

One summary goal, `core/usecase/enrollstudent/` (no `package-info.java`):

- `EnrollStudentInputPort` — four `void` interactions.
- `EnrollStudentUseCase` — `@FieldDefaults(makeFinal, PRIVATE)` +
  `@RequiredArgsConstructor` + `@Slf4j`; fields `presenter`, `txOps`,
  `persistenceOps` (note the third keeps an explicit `private final`).
- `EnrollStudentPresenterOutputPort extends ErrorHandlingPresenterOutputPort` —
  six fine-grained `present*` methods, one per outcome.

Interaction shape, identical in all four methods: one outer `try { txOps.doInTransaction(...) } catch (Exception e) { presenter.presentError(e); }`.
**Everything** — reads, domain calls, writes — runs inside the transaction; the
success presentation is registered with `txOps.doAfterCommit(() -> presenter.present…)`
from inside the transactional block. `findEnrollmentsForStudent` uses the read-only
variant `doInTransaction(true, …)`. The "already exists" branches in `createCourse` /
`createStudent` also present via `doAfterCommit` then `return`. The `enroll` method
carries the "Point of interest" comments the article refers to (why no
`@Transactional`, where to inject `1/0` to watch the rollback).

## Infrastructure adapters

| Port | Adapter | Notes |
|---|---|---|
| `PersistenceOperationsOutputPort` | `infrastructure/adapter/db/PersistenceGateway` (`@Service`) | Holds `CourseEntityRepository`, `StudentEntityRepository` (Spring Data JPA), `NamedParameterJdbcOperations`, `DbMapper`. Every method is `@Transactional` (read-only where applicable) and joins the use case's outer transaction. Wraps failures: `EntityNotFoundException` → `EntityDoesNotExistError`; other exceptions → `PersistenceError` (the `studentExistsWithFullName` / `findEnrollments` wrappers drop the cause). `obtain*` use `JpaRepository.getById` (lazy reference; the `EntityNotFoundException` surfaces on first access inside the mapper). |
| `TransactionOperationsOutputPort` | `infrastructure/adapter/transaction/SpringTransactionAdapter` (`@Service`) | Two `TransactionTemplate`s injected: the `@Primary` default and a `@Qualifier("read-only")` one, both declared in `config/TransactionConfig`; `lombok.config` copies `@Qualifier` onto the generated constructor parameter. See §Transaction adapter specifics. |
| `EnrollStudentPresenterOutputPort` | `infrastructure/adapter/web/enrollstudent/EnrollStudentPresenter` | Not a Spring bean — constructed inside `UseCaseConfig`. Extends `AbstractRestPresenter`, which writes JSON straight to the current `HttpServletResponse` via `MappingJackson2HttpMessageConverter` (`presentOk` → 200; `presentError` → 400 for `EntityDoesNotExistError`, 500 otherwise, body `{"error": …}`). |

Driving adapter: `EnrollStudentController` (`@RestController`). All handler methods
are `void` and return nothing to Spring MVC — the presenter has already written the
response. Each handler fetches the use case with
`appContext.getBean(EnrollStudentInputPort.class)` per call.

Wiring (`config/UseCaseConfig`): one `@Bean` of type `EnrollStudentInputPort`,
`@Scope(WebApplicationContext.SCOPE_REQUEST)`, which `new`s the presenter from the
request's `HttpServletResponse` + the Jackson converter and then `new`s the use case.
Whitelabel error page and `ErrorMvcAutoConfiguration` are disabled in
`application.properties`.

## Persistence specifics

- **No Flyway, no DDL in the repo.** `spring.jpa.hibernate.ddl-auto=update` lets
  Hibernate create/alter tables at startup. Tables: `course` (`id`, `title`,
  `number_of_students`), `student` (`id`, `full_name`), `enrollment` (`student_id`,
  `course_id`) — the last is an `@ElementCollection` / `@CollectionTable` on
  `StudentEntity`, so **saving a `Student` rewrites its enrollment rows**; there is
  no `EnrollmentEntity`.
- JPA entities (`CourseEntity`, `StudentEntity`): `@Getter @Setter @Builder
  @NoArgsConstructor @AllArgsConstructor`, `GenerationType.AUTO` ids.
- Read model: `EnrollmentRow` (`@Data`, holds the `SQL` constant joining
  `student`/`enrollment`/`course`), executed with `jdbcOps.queryForStream` +
  `BeanPropertyRowMapper`, mapped to the domain `Enrollment` VO.
- `DbMapper` / `DefaultDbMapper` (`adapter/db/map/`): hand-written, bean
  (`@Service`), five `map` overloads. Domain objects are rebuilt through their
  validating builders, so a corrupt row fails at mapping time.
- `EnrollmentsQuery` (`adapter/db/enrollment/`) is the **request DTO** for
  `POST /enrollments`; it lives in the db package although only the controller uses it.

## Transaction adapter specifics

`SpringTransactionAdapter` implements the port as follows (facts only; the doctrine
comparison is in `design-notes.md`):

- `doInTransaction(readOnly, r)` → `TransactionTemplate.executeWithoutResult`;
  `doInTransactionWithResult(readOnly, r)` → `TransactionTemplate.execute`. Default
  propagation (REQUIRED), so nested calls join.
- `doAfterCommit(r)`: if no actual transaction is active, runs `r` immediately;
  otherwise registers a `TransactionSynchronization` whose
  **`afterCompletion(status)`** runs `r` when `status == STATUS_COMMITTED`.
- `doAfterRollback(r)`: same registration pattern on `STATUS_ROLLED_BACK`. In the
  no-transaction branch the log line says "will not do anything" **but the code
  calls `r.run()`** — the javadoc on the port and the HEAD commit message
  ("no-op for doAfterRollback") describe a no-op the implementation does not do.
- `rollback()`: `TransactionInterceptor.currentTransactionStatus().setRollbackOnly()`,
  swallowing `NoTransactionException`. Not called anywhere in the codebase.
- The port keeps `doInTransactionWithResult`; no use case method uses it.
- Functional interfaces are project-local (`TransactionRunnableWithoutResult`,
  `TransactionRunnableWithResult<R>`), not `Runnable` / `Supplier`.

## Security specifics

None. No Spring Security dependency, no actor assertion in the use case, no auth
profiles. The REST endpoints are open.

## Test specifics

Four test classes under `src/test/java/com/github/cleanddd/`, none boot a Spring
context, no database required:

- `usecase/EnrollStudentUseCaseTest` — Mockito interaction test. All three ports are
  mocked; the `txOps` stubs run every runnable **immediately** (`doInTransaction`,
  `doInTransactionWithResult`, `doAfterCommit`, `doAfterRollback`), so the test
  observes fail-loud, synchronous semantics. One test: enrolling in a new course
  captures the `EnrollResult` via `ArgumentCaptor` and asserts no `presentError`.
- `model/ModelTest` — always-valid construction of `Student`, immutability of
  `enrollInCourse`.
- `adapter/db/map/DbMapperTest` — mapper round trips, null `coursesIds` → empty set.
- `arhunit/HexagonalArchitectureTest` (package name typo is in the source) —
  ArchUnit, JUnit `@Test` style (not `@ArchTest`): core must not depend on
  infrastructure; core may depend only on itself plus `java..`, `javax..`,
  `lombok..`, `org.slf4j..`. No `src/test/resources/archunit.properties`, so the
  importer resolves the full classpath (~4 s).

Fixture names are placeholders ("Brad Pitt", "Software architecture 101"); ids are
plain small integers.

## Code conventions (as established)

- Aggregates: `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` + `@Include` on the
  id, `@Getter` per field, explicit `private final` fields, `@Builder` on the
  validating constructor. Copy-on-write through a private `newX()` builder helper
  rather than `@With`.
- VOs: `@Value @Builder`.
- Use case / adapters: `@FieldDefaults(makeFinal = true, level = PRIVATE)` +
  `@RequiredArgsConstructor` + `@Slf4j` (adapters add `@Service`).
- Request/response DTOs (`adapter/web/enrollstudent/`): `@Data` (+ `@Builder` and
  `@JsonInclude(NON_NULL)` on responses).
- Imports: wildcard `lombok.*` / `javax.persistence.*` appear in the JPA entities;
  elsewhere single-class imports.
- Log prefixes `[Transaction]`, `[Transaction][Start]`, `[Transaction][After commit]`
  etc. mark the transaction trace the article walks through.
- `.gitattributes` forces LF line endings for all text files.

## Recorded methodology deviations (the *what*)

Rationale and whether to converge live in `design-notes.md` §Methodology assessment.

1. **JPA/Hibernate with `ddl-auto=update`** instead of Spring Data JDBC + Flyway;
   hand-written mapper instead of MapStruct.
2. **Whole interaction inside the transaction** — reads and validation are not
   hoisted outside `doInTransaction`; the "already exists" check runs inside it.
3. **`doAfterCommit` is built on `afterCompletion(STATUS_COMMITTED)`**, not
   `afterCommit()`; a throwing presenter is logged by Spring and swallowed.
4. **Port surface**: keeps `rollback()`, project-local runnable interfaces, and
   `doAfterRollback` that executes immediately outside a transaction.
5. **Request scope, not prototype scope**, for the use case bean; the controller
   pulls it from the `WebApplicationContext` instead of having it injected.
6. **Presenter constructed in `UseCaseConfig`**, not implemented by / injected
   through the controller; it writes directly to `HttpServletResponse`.
7. **One use case class carries four interactions** spanning course creation,
   student creation, enrollment and a query; no `package-info.java`.
8. **Named error is `InvalidDomainEntityError`**, not `InvalidDomainObjectError`;
   `Course` keeps an `AtomicInteger` field inside an otherwise immutable object.
9. **ArchUnit setup** lacks `archunit.properties` and uses `@Test` rather than
   `@ArchTest`.
10. **No actor assertion / no security port** — tutorial scope.

## Recipe — leak-scan `.claude/` before publishing

`.claude/` is versioned on a public repo; only `settings.local.json` is git-ignored
(it holds machine-local absolute paths, the username and the JDK location). Before
any commit/push that touches `.claude/`, confirm the publishable content is clean:

```
# The path/tooling fragments are literal and generic; replace each <placeholder>
# with your own machine/institution string. Do NOT write those real strings into
# this file — describe them as categories, or the scan would flag (and republish)
# its own recipe.
grep -rniE 'C:/Users|/c/Users|\.jdks|<local-username>|<institution>|<internal-host>|<internal-tooling>' \
  .claude --exclude=settings.local.json     # expect: no matches
git check-ignore .claude/settings.local.json  # must still print the path
```

- Clean scan **and** `settings.local.json` still ignored ⇒ safe to commit.
- The public repo handle (`<owner>/cleanddd`) and the owner's other public GitHub
  repos cited as reference projects are fine — they are public identities, not leaks.

## Recipe — watch the rollback demo

1. `docker compose up -d`, then `mvn spring-boot:run`.
2. Create a course and a student via Swagger UI (`/create-course`, `/create-student`),
   note the returned ids.
3. Insert `int t = 1 / 0;` where the "Point of interest" comment in
   `EnrollStudentUseCase.enroll` suggests (after the student save, before the course
   save), restart, call `/enroll`.
4. The debug log shows the `[Transaction]` trace and the rollback; neither the
   `enrollment` row nor `course.number_of_students` changes. Remove the line afterwards.
