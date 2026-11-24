# Project Objectives and Scope — RevLine

## Objectives

| ID | Objective | Evidence / completion condition |
|---|---|---|
| O1 | Maintain a consistent vehicle inventory. | Add, update, and delete operations apply the defined business rules and persist successfully. |
| O2 | Maintain a reliable sales record. | Each successful sale records the vehicle, buyer, seller, date, and selling price. |
| O3 | Prevent conflicting vehicle sales. | A sold vehicle cannot be sold again, and concurrent purchase attempts cannot complete duplicate sales. |
| O4 | Help buyers find suitable vehicles. | Search, filters, sorting, and comparison use the vehicle data correctly. |
| O5 | Preserve application data. | CSV and/or SQLite storage retains supported changes after restart; backup and restore match their documented scope. |
| O6 | Support two storage implementations. | CSV and SQLite JDBC modes follow the same user-facing business rules when both are enabled. |
| O7 | Provide useful showroom reports. | Reports calculate inventory, sales, and revenue information from the stored records. |
| O8 | Handle invalid input and failures clearly. | Invalid values, missing records, malformed import rows, and storage errors produce understandable messages. |
| O9 | Record important application actions. | The audit log records supported actions with timestamps. |
| O10 | Keep the application maintainable. | Packages have clear responsibilities, code uses readable names and methods, and the project builds for Java 17. |

## Main use cases

| ID | User | Use case |
|---|---|---|
| UC1 | Seller | View, add, update, and delete eligible vehicles |
| UC2 | Buyer | Browse and search the showroom |
| UC3 | Buyer | Filter, sort, and compare vehicles |
| UC4 | Buyer | Reserve or purchase a vehicle |
| UC5 | Buyer | Join a waitlist for a vehicle that is not currently available |
| UC6 | Seller | Review sales history and generate reports |
| UC7 | Seller | Undo supported inventory changes |
| UC8 | Seller | Import legacy-format or CSV data |
| UC9 | Seller | Run supported batch commands |
| UC10 | Seller | Back up and restore application data |
| UC11 | User | View the audit log |
| UC12 | User / evaluator | Explore the interactive concurrency demonstrations |

## In scope

- Java console application for one showroom.
- Vehicle inventory management.
- Search, filtering, sorting, and comparison.
- Timed reservations, sales, receipts, and sales history.
- Waitlist and recently viewed features.
- Inventory and sales reports using collections and streams.
- CSV and SQLite JDBC persistence.
- CSV/legacy-format import, command-file import, export, and backup/restore.
- Audit logging and relevant concurrency demonstrations.
- Formal project documentation and presentation.

## Out of scope

- Graphical, web, or mobile interface.
- Online payment processing.
- Authentication, user accounts, or role-based access control.
- Multi-showroom networking or distributed deployment.
- Cloud services and REST APIs.
- Framework-based enterprise architecture.

## Assumptions and boundaries

- One showroom and one local application instance.
- JDK 17 or newer.
- Vehicle prices are represented in USD in the current sample data.
- CSV is the default storage mode; SQLite JDBC is an alternative mode.
- Reservations are temporary application holds. Their restart behaviour is defined by the implementation and documented in the design report.

## Expected deliverables

- Working Java source code and build/run scripts.
- Seed and sample data required by application features.
- SQL schema and seed resources for JDBC mode.
- Project problem statement, objectives, design, concept map, migration report, progress record, diagrams, and presentation.
- A truthful record of build and manual functionality verification.

*The student should review these objectives and confirm that they accurately represent the implemented project and their own understanding before submission.*
