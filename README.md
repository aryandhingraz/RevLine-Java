# RevLine — Car Showroom Management System

**A Java 17 console application for managing showroom inventory, reservations, sales, and reports.**

RevLine models the day-to-day workflows of a single car showroom. It includes buyer and seller menus, searchable vehicle listings, timed reservations, sales processing, reporting, data import and backup tools, and two persistence options: CSV files and SQLite through JDBC.

This project was developed for **25CSE0202 — Programming in Java** and demonstrates object-oriented design, collections, generics, lambdas, file I/O, JDBC, exception handling, and concurrency.

## Highlights

- Maintain vehicle inventory and review sales history.
- Search, filter, sort, and compare vehicles.
- Place and cancel timed reservations; handle a FIFO waitlist.
- Complete sales and generate receipts.
- Generate inventory and sales reports.
- Import CSV and legacy-format data, run batch commands, and back up or restore application data.
- Choose CSV persistence or SQLite JDBC persistence.
- Explore interactive demonstrations of Java concurrency concepts.

> The Buyer/Seller menu is a workflow selector, not an authentication or authorization system. RevLine is a local, single-showroom console application; it does not provide a web interface, online payments, or networked multi-user access.

## Requirements

- JDK 17 or newer
- A terminal or command prompt
- The SQLite JDBC driver is included in `lib/` for JDBC mode

## Build and run

From the repository root:

### Windows

```bat
scripts\build.bat
scripts\run.bat
```

To use SQLite:

```bat
scripts\run.bat --storage=jdbc
```

### Linux or macOS

```bash
bash scripts/build.sh
bash scripts/run.sh
```

To use SQLite:

```bash
bash scripts/run.sh --storage=jdbc
```

The build targets Java 17 and treats compiler warnings as errors.

## Storage and sample data

- CSV mode uses `data/cars.csv` and `data/sales.csv`. When runtime files are absent, the application initializes them from `data/seed/`.
- SQLite mode stores its database at `data/revline.db`.
- `data/sample/` contains example import and batch input files.
- Generated databases, reports, backups, logs, and runtime CSV files are excluded from version control.

## Repository layout

```text
src/main/java/com/revline/  Application, domain model, services, repositories, UI, utilities
data/                       Seed data and sample inputs
docs/                       Design diagrams and project presentation
lib/                        SQLite JDBC driver
scripts/                    Windows and Unix build/run scripts
sql/                        Database schema, seed statements, sample queries
```

## Continuous integration

GitHub Actions compiles the project on pushes and pull requests using JDK 17. To publish local changes to GitHub, commit and push them; each push starts a fresh build check automatically. A successful build verifies compilation, while it does not claim to verify every interactive workflow.

## Project materials

- [Project presentation](docs/RevLine_Presentation.pptx)
- [Class diagram](docs/diagrams/class.mmd)
- [ER diagram](docs/diagrams/er.mmd)
- [Package diagram](docs/diagrams/package.mmd)
- [Sequence diagrams](docs/diagrams/)

## License

Released under the MIT License. See [LICENSE](LICENSE).
