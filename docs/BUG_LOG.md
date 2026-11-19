# RevLine-Java Bug Log & Defect Tracking

This document logs all bugs, compiler warnings, regression defects, and environmental challenges encountered during the development of RevLine-Java, along with their root causes, fixes, and regression test verifications.

---

## Bug Log Table

| Bug ID | Phase | Component | Description / Symptom | Root Cause | Resolution & Regression Test | Status |
|---|---|---|---|---|---|---|
| **BUG-001** | Phase -1 | Build Tooling | Scripts failed with `fatal: not a git repository`. | Subprocesses were executed in the parent directory `D:\JAVA_PROJECT` instead of the project root `D:\JAVA_PROJECT\RevLine-Java`. | Explicitly navigate to workspace root before executing git or build commands. | RESOLVED |
| **BUG-002** | Phase 0 | Scripts | PowerShell strings with `$var` expanded to empty strings. | Passing `-Command "powershell ... $var"` caused nested PowerShell expansion before execution. | Switched to direct script invocation via `.\scripts\*.bat`. | RESOLVED |
| **BUG-003** | Phase 6 | Model Validation | Compiler error: `cannot find symbol: method validateId(int)`. | `PurchaseRequest.java` called `Validator.validateId(id)`, which was not part of the `Validator` utility contract. | Replaced with inline parameter check `if (id <= 0) throw new InvalidCarException(...)`. | RESOLVED |
| **BUG-004** | Phase 6 | Multithreading | Compiler warning: `[this-escape] possible 'this' escape before subclass is fully initialized`. | `AutoSaveThread` called non-final methods `setDaemon(true)` and `setPriority(...)` within its constructor. | Declared `public final class AutoSaveThread extends Thread`. Verified zero compiler warnings under `javac -Xlint:all`. | RESOLVED |
| **BUG-005** | Phase 6 | Concurrency Lab | Interactive menu hung when launching Concurrency Lab Demo 4 while EX-8 was incomplete. | Unhandled `UnsupportedOperationException` in `take()` terminated consumer worker, leaving 3 producers blocked on full bounded buffer. | Added pre-execution capability probe on `PurchaseRequestQueue.take()`; if unsupported, Demo 4 displays a friendly student instruction banner. | RESOLVED |
| **BUG-006** | Phase 6 | Data Seed Integrity | `SeedConsistencyTest` failed unexpectedly after executing Concurrency Lab Demo 2. | Demo 2 sold Car #1 and #2 directly against live inventory and saved to `data/seed/cars.csv`. | Refactored `runDemo2DoubleSellShowdown()` to create isolated disposable CSV files in `build/` directory; restored pristine seed data via `git checkout data/seed/`. | RESOLVED |
| **BUG-007** | Phase 7 | Enum Parsing | Compiler error: `cannot find symbol: method fromString(String) in class Status`. | `DataToolsService.java` attempted to parse status tokens via `Status.fromString()`, which had not been implemented. | Added case-insensitive `public static Status fromString(String text)` to `Status.java`. | RESOLVED |
| **BUG-008** | Phase 8 | Architecture / Quality | Classes `MenuController`, `DataToolsService`, `ConcurrencyLab`, `InventoryService`, `ReportService` exceeded 300 lines (PART 15 guideline). | Classes accumulated cross-cutting handlers, reporting builders, and demo runners over multiple phases. | Extracted modular helpers (`BuyerMenuHandler`, `SellerMenuHandler`, `ReportsMenuHandler`, `DataToolsMenuHandler`, `ReportSummaryFormatter`, `ConcurrencyLabDemos`, `ByteStreamBackup`, `ScriptRunner`, `SyntheticDataGenerator`). All classes now <= 275 lines. | RESOLVED |
