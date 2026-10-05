# cleanddd — design notes

> **Charter — rationale, history, open threads. The *why*, not the *what*.**
> This file owns the narrative: design decisions and their motivations,
> methodology assessment, open architectural threads, Q&A captured from
> sessions, comparisons to reference projects, and pointers to ADRs (if the
> project maintains `doc/decisions/`).
>
> - Update when a decision is made and its *why* is worth remembering — or
>   when an open thread is opened, closed, or revised.
> - Current facts (what something is, where it lives, which class owns it)
>   belong in `project-context.md` or `project-context-extended.md`. If you
>   catch yourself writing a fact table here, move it.
> - Pointers to reference files (§-ref style) are welcome so this file can
>   stay focused on reasoning while still being navigable.

## Project overview

`cleanddd` is the companion code for the owner's *Clean DDD* article: the smallest
Spring Boot application that still shows the whole shape of the approach — a use
case as the unit of business logic, aggregates that are immutable and always-valid,
ports on the core side, adapters on the infrastructure side, and an ArchUnit rule
that keeps the dependency arrow pointing inward. The domain (students enrolling in
courses) was chosen for being instantly understood, so attention stays on the
architecture.

The distinctive emphasis is **transaction demarcation owned by the use case**. The
`enroll` interaction is the article's worked example: the use case opens the
transaction itself through a port, both aggregates are written inside it, and the
success response is deferred until after commit so the actor is never told "done"
before the database agrees. The code has since been reworked several times around
exactly that concern (the git history is a series of "another take at
transactions"), and the repo now serves as a laboratory for doctrine questions in
that area rather than as a product.

## Methodology assessment

The code predates the current modular doctrine and was written while the ideas were
still being formed, so it conforms in spirit and diverges in several mechanics.
The *what* of each deviation is listed in `project-context-extended.md` §Recorded
methodology deviations; this section records what is known about the *why*.

- **JPA/Hibernate, no Flyway, hand-written mapper.** Historical: the project started
  from the default Boot JPA stack before the methodology's "no ORM, Spring Data JDBC
  + MapStruct + Flyway" stance existed. Whether to converge is undecided; the
  persistence layer is not what the article is about, so it may stay as is.
- **Whole interaction inside the transaction.** The article's point is that the use
  case *demarcates*; narrowing the transaction to the writes (reads and validation
  outside) is a later refinement of the doctrine. Not an oversight to fix silently —
  it is part of what the owner wants to examine here.
- **`afterCompletion` instead of `afterCommit`.** The doctrine now requires the
  after-commit hook to fail loudly; this adapter uses the swallowing hook. Why the
  swallowing variant was chosen is not recorded (the adapter cites Stack Overflow and
  Baeldung references on synchronization). The use case test stubs run callbacks
  immediately, so the tests currently assert semantics the production adapter does
  not have — the exact hazard the doctrine describes.
- **`rollback()` and project-local runnable interfaces on the port.** Reflects an
  earlier design of the port; the doctrine since dropped `rollback()` (failure is
  expressed by throwing) and uses `Runnable`/`Supplier`. Reason for keeping them
  here: unknown, likely inertia.
- **Request scope + presenter built in the composition root.** A workable way to get a
  per-request presenter that holds the `HttpServletResponse` without making the
  controller the presenter. The doctrine's prototype-scope wiring arrived later.
- **One use case, four interactions.** Consistent with the doctrine's "interaction =
  void method on the input port" mapping; what is debatable is whether creating
  courses and students belongs to the *enroll student* summary goal. Left as is — the
  demo is deliberately small.
- **Conformant and worth keeping as examples:** void interactions, outermost
  `catch → presentError`, presenter port per use case extending the base error port,
  domain objects passed straight to the presenter, copy-on-write aggregates with
  validating builders, `EnrollResult` as an explicit outcome object, segregated
  read model via plain SQL, error wrapping at the driven boundary, ArchUnit guard.

## Open threads

- **The owner intends to use this repo to explore one specific doctrine question**
  (raised at onboarding, not yet stated). Record the question and the conclusion here
  once the discussion happens.
- `doAfterRollback` outside a transaction: javadoc, log message and HEAD commit say
  no-op, the code runs the runnable. Decide which is intended before relying on it.
- Should the after-commit hook move to `afterCommit()` (fail-loud) and should the
  use case test be complemented by an adapter test over a real
  `AbstractPlatformTransactionManager` double, as the doctrine prescribes?
- `dev` branch exists locally only; previous PRs merged into `main`. Confirm the
  intended branch model before the next PR.

## Comparisons to reference projects

- `HexagonalArchitectureTest` is copied from `cargo-clean` (comment in source);
  `AbstractRestPresenter` is copied from the owner's `clean-rest` repo. Both carry the
  older JUnit-`@Test` ArchUnit style and the direct-response presenter.
- Compared with `hotel-clean` (the reference for transaction demarcation), this
  project is the earlier, coarser form: whole-interaction transaction, no optimistic
  locking, no lock-conflict presentation.

## ADR pointer

This project does not currently maintain a `doc/decisions/` directory. If formal
ADRs are introduced, link them from here.
