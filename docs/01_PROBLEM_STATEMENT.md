# Problem Statement — RevLine

> **DRAFT OUTLINE — The student must rewrite this in his own words before committing.**

## Context
Small and mid-size car showrooms and individual sellers still track stock in notebooks
or loose spreadsheets.

## Pain Points
1. Records drift out of date: a car that was sold keeps showing as available.
2. Sales are written down without buyer, date, or price — so revenue cannot be calculated.
3. Buyers cannot search or compare quickly.
4. Two buyers can be promised the same car (no duplicate-sale protection).

## Users
- **Seller:** keeps stock accurate, records sales, sees reports, manages data import/export/backup.
- **Buyer:** searches, filters, sorts, compares cars; reserves or buys; sees receipt.
- **System:** auto-saves, expires reservations, audits every action.

## Need
A Java application that gives a seller one reliable inventory, a recorded sales trail,
fast search/filter/compare for buyers, and report-ready analytics — while protecting
every sale from duplicate bookings and data loss.

## Why Feasible
Console-based backend, single machine, single JVM process, offline-capable, ≤10,000 cars,
prices in USD. No GUI, no web, no cloud — scoped for one semester.

---

> **Student TODO:** Rewrite the above in your own words. Keep it ≤1 page. This is rubric
> row 1 (3 marks). The examiner checks that it is "clearly stated, relevant, feasible,
> and sufficiently specific for an individual Java application."
