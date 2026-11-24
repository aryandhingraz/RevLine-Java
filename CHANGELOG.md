# Changelog

This file summarizes notable project changes. Historical milestones are retained at a high level; this is not a list of test results or a claim that every historical feature was independently verified.

## [1.2] — 2026-11-24

### Fixed
- Fixed SQLite database restore in JDBC mode under Windows by managing JDBC connection lifecycle during file operations (BUG-A).
- Fixed `SalesService` transactional rollback by introducing `revertCarStatus` to cleanly restore vehicle status when sale persistence fails (BUG-B).
- Added optional input handling in `InputReader` (`readOptionalLine`, `readOptionalInt`, `readOptionalDouble`) so pressing Enter in Update Supercar retains current specifications (BUG-C).
- Added duplicate car selection validation in `BuyerMenuHandler.handleCompareCars` to prevent self-comparison (BUG-D).
- Fixed Seller menu prompt option range string from `[1-8, S, 0]` to `[1-9, A, S, 0]`.

### Refactored & Improved
- Enforced hard limit of $\le 300$ lines across all 70 Java classes by extracting `SalesReceiptFormatter` and delegating static helpers to `InventorySupport` (maximum class length is now 291 lines).
- Enforced explicit `StandardCharsets.UTF_8` encoding in `FileCarRepository`, `DataToolsService`, `LegacyCImporter`, `ScriptRunner`, and `AppLogger`.
- Added GC and yield retry in `ByteStreamBackup.restoreFile` for robust file renaming on Windows.

## [1.1] — 2026-11-24

### Changed
- Updated the project README and coursework documents to describe the current Java application and its actual scope.
- Removed the original C source files from the final Java repository while retaining the legacy-format sample data required by the Java importer.
- Clarified that the legacy-format importer is implemented in Java and does not require a C compiler.
- Improved inventory undo handling so a failed storage operation does not add a false undo entry.
- Improved undo execution so an action remains available if its reversal fails.
- Added a duplicate brand/model guard when restoring an inventory snapshot.
- Routed sale, reservation, and cancellation workflows through reservation expiry cleanup so expired holds can release the vehicle state.
- Made reservation creation roll back its in-memory hold if the vehicle status cannot be persisted.
- Improved file recovery checks and UTF-8 handling for shared file utilities.
- Added confirmation and completeness checks before restoring a backup set.

### Removed
- Removed generated class files, runtime databases/CSVs, logs, and generated report output from the deliverable.
- Removed the original C source folder and obsolete source-path constants.
- Kept only the production build and run scripts.

## [1.0] — 2026-11-19

### Added
- Formal project documentation, architecture diagrams, syllabus concept map, and project presentation.
- Modular console menu handlers.
- CSV and SQLite JDBC repository implementations.
- Inventory, sales, reservations, reporting, import/export, and concurrency features.
- A Java migration report describing the transition from the legacy procedural data model.

## Earlier development milestones

- `v0.1-prototype` — initial vehicle model, inventory flow, console interaction, and file persistence.
- `v0.3-ca3` — collections, search, filtering, sorting, comparison, and undo functionality.
- `v0.4-jdbc` — SQLite JDBC persistence and relational data model.
- `v0.5-analytics` — stream-based showroom and sales reports.
- `v0.6-concurrency` — threading, synchronization, and producer-consumer demonstrations.
- `v0.7-datatools` — file import/export, legacy-format import, and backup utilities.
