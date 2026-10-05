# cleanddd — project memory index

Per-project memory index, read at session startup. Each entry points at a file under
`.claude/memory/` with a one-line description of what it owns. Keep this index
institution-neutral — `.claude/` is versioned on a public repo.

- [project-context.md](memory/project-context.md) — quick-reference current facts: stack, package layout, entry points, run/test commands, repo workflow.
- [project-context-extended.md](memory/project-context-extended.md) — on-demand deep reference: domain model, use-case inventory, adapters, persistence and transaction specifics, test shape, conventions, recorded deviations, the leak-scan recipe.
- [design-notes.md](memory/design-notes.md) — rationale, methodology assessment, open threads, comparisons to reference projects (the *why*, never a changelog).
