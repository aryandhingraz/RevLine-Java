# RevLine

### Car Showroom Management System — Java 17

[![Java build](https://github.com/aryandhingraz/RevLine-Java/actions/workflows/build.yml/badge.svg?branch=main)](https://github.com/aryandhingraz/RevLine-Java/actions/workflows/build.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-orange)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

RevLine is a menu-driven Java application for managing the core workflows of a single car showroom. It brings inventory, buyer browsing, reservations, sales, reporting, and data utilities together in one terminal-based system. The application supports CSV persistence and SQLite persistence through JDBC.

The project was developed for **25CSE0202 — Programming in Java**. It is designed to demonstrate how core Java concepts can be combined into a layered application with domain models, service logic, interchangeable repositories, and a console interface.

> **Language:** Application logic is implemented in Java. The project includes a Java importer for a legacy flat-file data format; it does not include C application source files.

## Capabilities

### Buyer workflows

- Browse the showroom inventory from the console.
- Search and filter vehicles, sort results, and compare cars.
- Place or cancel timed reservations.
- Join the FIFO waitlist when a vehicle is unavailable.
- Purchase an available vehicle and generate a receipt.
- Review recently viewed vehicles during the session.

### Seller and management workflows

- Add, view, update, and remove inventory records.
- Review sales history and generate sales or inventory reports.
- Undo supported inventory operations.
- Import CSV and legacy-format records with validation.
- Run batch commands for repeatable inventory operations.
- Back up and restore application data, and inspect the audit log.
- Launch interactive demonstrations of Java concurrency concepts.

The Buyer/Seller menu selects a workflow. It is not an authentication or access-control boundary.

## Architecture

RevLine separates terminal interaction, application workflows, business rules, persistence, and domain objects. This keeps menu handling distinct from inventory and sales rules, while the repository interfaces support both file-based and JDBC storage.

```mermaid
flowchart LR
    UI[Console UI and application menus] --> S[Services and business rules]
    S --> R[Repository interfaces]
    R --> CSV[CSV repositories]
    R --> JDBC[JDBC repositories]
    R --> D[Domain model]
    S --> D
    S --> C[Concurrency utilities]
    S --> U[Validation, paths, logging, exceptions]
```

| Area | Responsibility |
| --- | --- |
| `app` and `ui` | Application startup, menus, console input, banners, and tables |
| `service` | Inventory and sales rules, search, reservations, reports, imports, backups, and waitlists |
| `repository` | Repository contracts and CSV/JDBC persistence implementations |
| `model` | Vehicle, car, sale, reservation, status, fuel type, and audit domain objects |
| `concurrency` | Background autosave and reservation expiry, request processing, and concurrency demonstrations |
| `util` | Validation, file/path utilities, logging, shared helpers, and application exceptions |

The detailed [package diagram](docs/diagrams/package.mmd), [class diagram](docs/diagrams/class.mmd), [entity relationship diagram](docs/diagrams/er.mmd), and [sequence diagrams](docs/diagrams/) are available in `docs/diagrams/`.

## Persistence and data

RevLine can use either of the following storage modes:

| Mode | Storage | How it is selected |
| --- | --- | --- |
| CSV | `data/cars.csv` and `data/sales.csv` | Default when no JDBC option is supplied |
| SQLite | `data/revline.db` | Pass `--storage=jdbc` when launching the app |

In CSV mode, if runtime data files do not exist, the application initializes them from the tracked seed files in `data/seed/`. Sample inputs for imports and batch commands are under `data/sample/`; a legacy-format example is under `data/legacy/`. Generated databases, runtime CSV files, backups, reports, and logs are excluded from version control.

The SQLite JDBC driver is included in `lib/`. SQL schema and example queries are provided in `sql/`.

## Requirements

- JDK 17 or newer
- A terminal or command prompt
- No separate dependency download is needed for SQLite mode; the JDBC driver is included

## Build and run

Run commands from the repository root. The build script compiles the application for Java 17 with `-Xlint:all -Werror`.

### Windows

```bat
scripts\build.bat
scripts\run.bat
```

Launch with SQLite persistence:

```bat
scripts\run.bat --storage=jdbc
```

### Linux or macOS

```bash
bash scripts/build.sh
bash scripts/run.sh
```

Launch with SQLite persistence:

```bash
bash scripts/run.sh --storage=jdbc
```

The run script builds the application first if compiled output is missing.

## Repository structure

```text
src/main/java/com/revline/
  app/           Startup and menu workflows
  concurrency/   Background tasks and concurrency demonstrations
  model/         Domain model and enums
  repository/    CSV and JDBC data access
  service/       Business operations and application features
  ui/            Console input, menus, tables, and presentation
  util/          Validation, logging, paths, helpers, and exceptions
data/
  seed/          Initial CSV data
  sample/        Example imports and batch input
  legacy/        Legacy-format import example
docs/
  diagrams/      Mermaid architecture and workflow diagrams
  RevLine_Presentation.pptx
lib/             SQLite JDBC driver
scripts/         Windows and Unix build/run scripts
sql/             SQLite schema, seed statements, and sample queries
```

## Java concepts demonstrated

- Object-oriented design with interfaces, abstract classes, encapsulation, and polymorphism.
- Collections, comparators, generics, lambdas, and stream-based operations.
- File input/output, CSV handling, validation, and exception hierarchies.
- JDBC persistence and SQL schema management.
- Threads, synchronization, queues, background work, and timed reservation expiry.
- Separation of application services from repository implementations.

## Continuous integration

GitHub Actions compiles the Java application on pushes, pull requests, and manual workflow dispatches using JDK 17. A successful workflow confirms compilation with the configured warning checks; it is not a claim that every interactive workflow has been exercised.

To publish local edits, commit and push them to GitHub. The repository is public, so its source, documentation, and project materials are available to view. Each push triggers the build workflow automatically.

## Scope and limitations

RevLine is a local, single-showroom console application. It does not provide a web interface, online payments, authentication, cloud hosting, or networked multi-user operation. The Buyer/Seller menu is for navigating the demo workflows only.

## Project materials

- [Presentation](docs/RevLine_Presentation.pptx)
- [Database schema](sql/schema.sql)
- [Seed SQL](sql/seed.sql)
- [Sample SQL queries](sql/sample_queries.sql)
- [Change history](CHANGELOG.md)

## License

Released under the [MIT License](LICENSE).
