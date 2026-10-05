<!-- methodology-template: clean-ddd-core-only -->

# Project: cleanddd

A small Spring Boot demo (students enrolling in courses) that accompanies the
article *Clean DDD* (linked from `README.md`). Its purpose is to show the fusion of
Clean Architecture and DDD in a minimal setting: one use case, two aggregates, a
persistence port, and **explicit transaction demarcation driven from the use case**
with deferred (after-commit) presentation. It is a tutorial / reference project, not
a product.

> **Public repository — `.claude/` is versioned on purpose.** This repo lives on
> public GitHub (`github.com`). The `.claude/` memory files document the Clean DDD
> design reasoning, which is itself part of what this project shares with the
> community, so they are committed. Keep all committed content **institution-neutral
> and free of machine-specific setup** — no absolute paths, usernames, or internal
> hostnames. Machine-local Claude settings stay out of git via `.gitignore`
> (`.claude/settings.local.json`). The `@~/.claude/methodology/*` references below
> point at the author's *local* methodology library; they will **not** resolve in a
> fresh clone and are kept as provenance, not as a runnable dependency.

## Methodology in scope

- @~/.claude/methodology/clean-ddd-core.md
- @~/.claude/methodology/persistence-and-transactions.md
- @~/.claude/methodology/testing.md
- @~/.claude/methodology/session-wrap-up.md

## Working rules for this project

- **Discuss before implementing.** This repo is a laboratory for doctrine questions.
  Default to architectural discussion; treat "NO CODE" prompts as pure design
  conversation and do not touch source without explicit go-ahead.
- **The code predates the current doctrine.** It uses JPA/Hibernate, a single
  multi-interaction use case, request-scoped wiring and an `afterCompletion`-based
  after-commit hook — all recorded as deviations in the memory files. Do not "fix"
  them opportunistically; they are discussion material unless the owner decides to
  converge.
- **Issues are plain.** Create issues with plain `gh issue create` — no assignee,
  no sprint, no external issue-tracker attribution. This is a personal demo project.
- **Leak-scan `.claude/` before publishing.** `.claude/` is versioned on a public
  repo (only `settings.local.json` is ignored). Before any commit or push that
  touches `.claude/`, scan the changed content for machine/institution leakage —
  absolute paths, the local username, internal hostnames, internal tooling names,
  JDK install paths — and confirm `settings.local.json` is still the lone ignored
  file. Recipe + exact command in `memory/project-context-extended.md`.

## Project-specific context

See `.claude/memory/project-context.md` and `.claude/memory/project-context-extended.md`
in this repository. Deep-dive narrative documents (domain context, onboarding notes,
architectural decisions) also live under `.claude/memory/`. The index is
`.claude/MEMORY.md`.

## Auto-loaded project memory

- @.claude/MEMORY.md

## On-demand resources

- `~/.claude/methodology/INDEX.md` — map of all methodology modules (the ones relevant to this project are loaded automatically via @-refs above; consult the INDEX only when a cross-cutting question arises that falls outside the loaded set).
- `~/.claude/methodology-log.md` — cross-project learning journal (maintainer-curated; read-only here).
