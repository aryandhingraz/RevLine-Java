# Problem Statement — RevLine

## Context

A small car showroom needs a dependable way to maintain its vehicle inventory and record completed sales. When stock information is kept in notebooks or separate spreadsheets, the information can become outdated and difficult to search.

## Problem

The showroom needs one consistent record of its vehicles and sales. Without a central inventory, a vehicle may continue to appear available after it has been sold. Sales records may also be incomplete, making it difficult to review buyers, dates, prices, and revenue. Buyers need a convenient way to search for and compare vehicles before making a decision.

## Proposed solution

RevLine is a Java console application that manages a showroom's vehicle inventory and sales records. It allows a seller to add, update, and remove eligible vehicles, while buyers can browse, search, filter, sort, compare, reserve, and purchase vehicles. The application records sales, produces receipts and reports, and supports CSV or SQLite JDBC storage.

The application uses object-oriented design, collections, exception handling, file handling, JDBC, streams, generics, and basic multithreading where these concepts support the project.

## Users

- **Buyer:** browses, searches, filters, compares, reserves, and purchases vehicles.
- **Seller / manager:** maintains inventory, reviews sales, generates reports, and uses the available data tools.
- **Application:** validates input, stores records, manages reservation expiry, and protects shared operations against conflicting updates.

## Scope and feasibility

RevLine is designed for one showroom on one computer, using a Java console interface and a single application process. It is intended as a guided Java course project, not as a commercial dealership platform.

The project does not include online payments, user authentication, a graphical interface, web services, cloud deployment, or multi-showroom networking.

*The student should review this statement and adjust it to accurately reflect their own understanding and contribution before submission.*
