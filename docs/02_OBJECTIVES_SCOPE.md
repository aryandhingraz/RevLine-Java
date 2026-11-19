# Project Objectives & Scope — RevLine

> **DRAFT OUTLINE — The student must rewrite the objectives in his own words.**

## Measurable Objectives

| ID | Objective | How to Measure |
|----|-----------|----------------|
| O1 | Replace notebook tracking with a persistent inventory | 100% of add/update/delete operations persist across restarts (verified by test) |
| O2 | Record every sale | Every sale stores saleId, carId, buyer, date, price; 0 sales without a record |
| O3 | Prevent double-selling | 0 duplicate sales in a 50-thread stress demo (synchronized proof) |
| O4 | Fast discovery | Search + combined filter returns in <50 ms on 1,000 cars |
| O5 | Reliable storage | Survive a simulated crash mid-save with no data loss (temp-file + rename) |
| O6 | Dual persistence | Same services run on CSV and JDBC/SQLite by swapping one repository object |
| O7 | Analytics | ≥10 Stream reports incl. stock value (AVAILABLE only), fuel distribution, top-N, avg price by brand |
| O8 | Robustness | No uncaught exception from any user input; all invalid input produces a clear message |
| O9 | Traceability | Every user action is written to an audit log with timestamp |
| O10 | Maintainability | 6 packages, no class >300 lines, ≥60 automated checks passing |

## Use Cases

| UC | Actor | Use Case | Notes |
|----|-------|----------|-------|
| UC1 | Seller | Add a car | Auto-ID, validated |
| UC2 | Seller | Update / delete a car | Blocked if RESERVED/SOLD or has sales |
| UC3 | Seller | Mark car sold / record sale | Saves buyer, date, price |
| UC4 | Seller | Generate sales & stock reports | Streams |
| UC5 | Buyer | Search by brand/model/fuel | Case-insensitive, partial |
| UC6 | Buyer | Filter by budget, fuel, speed, status | Combinable predicates |
| UC7 | Buyer | Sort / compare cars | Comparator library; side-by-side table |
| UC8 | Buyer | Reserve (timed) or buy a car | One winner under concurrency |
| UC9 | Buyer | Join waitlist for sold/reserved car | Queue |
| UC10 | Seller | Import / export / backup | Legacy C, CSV, command script |
| UC11 | Seller | Undo last add/delete | Deque |
| UC12 | Anyone | View audit log | File + table |
| UC13 | Presenter | Run Concurrency Lab | Viva demo |

## In Scope
- Console application; inventory CRUD; search/filter/sort/compare
- Reserve/buy/cancel; sales history; Stream reports
- CSV + JDBC persistence; CSV/script import; export; backup
- Migration of legacy C `cars.txt`; audit log; concurrency lab
- In-house tests; documentation

## Out of Scope
- GUI / web front-end
- Payments processing
- User authentication & roles (removed by design — see DECISIONS.md ADR-003)
- Multi-showroom networking
- Cloud deployment
- Images / multimedia
- REST APIs

## Boundaries & Assumptions
- Single showroom, ≤10,000 cars, single machine
- Prices in USD, one JVM process
- JDK 17 LTS target

## Expected Outputs
- Working Java console application with 7 modules
- 15+ stream analytics reports
- Dual persistence (CSV files + SQLite database)
- 60+ automated test checks
- Complete documentation suite (problem statement, design, concept map, viva prep)
- Real Git history with 60–120 commits

---

> **Student TODO:** Rewrite the objectives in your own words. Keep them clear and
> measurable. This is rubric row 2 (2 marks).
