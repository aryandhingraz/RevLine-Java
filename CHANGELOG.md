# Changelog

All notable changes to RevLine-Java will be documented in this file.
Format follows [Keep a Changelog](https://keepachangelog.com/).

## [v0.1-prototype] - 2026-11-19

### Added
- Domain Model: `Vehicle` (abstract), `Car`, `Sale`, `Reservation`, `AuditEntry`, `FuelType`, `Status`, `Describable`, `CarVisitor`
- Repository Layer: generic `Repository<T, ID>`, `CarRepository`, `SaleRepository`, `FileCarRepository`
- Crash-safe CSV persistence via temp file + atomic rename in `FileUtil.safeWrite`
- Service Layer: `InventoryService` (dual collection ArrayList + HashMap, duplicate guard, sorted brands TreeSet, defensive copy), `SearchService` (findById, findByStatus)
- UI: box-drawing table renderer (`ConsoleTable`), ASCII brand logo and boot panel (`Banner`), exception-safe line reader (`InputReader`), menus (`MenuPrinter`)
- Storage auto-bootstrap: automatically initializes `data/cars.csv` from verified seed data
- In-house testing framework: `Assert` and `TestRunner` with 34 automated unit checks
- Student exercise specifications: EX-1 (`Validator`) and EX-3 (`SearchService`) with failing test contracts and hints
- Relational SQL schema, seed data, and queries in `sql/`
- Documentation: `docs/00_UNDERSTANDING.md`, `docs/03_DESIGN.md` (6 Mermaid diagrams, data dictionary, collection-choice table), `docs/learning/M1_EXPLAINED.md`, `EX1_HINT.md`, `EX3_HINT.md`, `EX_LOG.md`
- Evidence: automated demo input/output logs in `docs/evidence/`
