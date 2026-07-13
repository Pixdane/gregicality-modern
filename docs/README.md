# Gregicality Modern Migration Notes

This directory contains early migration notes for porting GCY / Gregicality
content onto GTCEu Modern.

These documents are intentionally incomplete and should be treated as working
notes, not as a final migration specification. Many classifications, recipe
decisions, material properties, machine mappings, and nuclear-related entries
still need source-level verification before implementation.

Current documents are useful for:

- tracking source modules and approximate migration scope;
- recording material dedupe decisions against GTCEu Modern;
- preserving provisional material package routing;
- collecting old id to modern id mappings for later recipe migration.
- designing compile-time Scala DSL and generated registration source workflows.

When a later implementation contradicts these notes, update the relevant
document with the verified behavior instead of treating this snapshot as
authoritative.

Design notes:

- [`compile-time-scala-dsl-design.md`](compile-time-scala-dsl-design.md):
  proposed typed Scala DSL, pure ADT/codegen pipeline, macro boundaries, and
  Gradle source generation strategy for registration code.
- [`symbolgen-redesign-plan.md`](symbolgen-redesign-plan.md):
  completed symbolgen refactor plan, execution record, and verification notes.
