# Module 7: File I/O, Byte Streams & Enterprise Data Tools (L25–L27)

> **Module 7**: Byte Streams, Character Streams, StreamTokenizer & Data Migrations  
> **Target Milestone**: Phase 7 (`v0.7-datatools`)  
> **Target Syllabus**: 25CSE0202 Programming in Java (Lectures 25, 26, 27)

---

## 1. Architectural Overview

Phase 7 delivers RevLine's enterprise data exchange and disaster recovery tools:
- **Legacy C Flat-File Importer (L26–27)**: Parses comma-delimited `cars.txt` exported from the legacy C procedural codebase, mapping numeric status flags (`1` $\to$ AVAILABLE, `0` $\to$ SOLD).
- **Batch CSV Importer with Error Reporting (L3, L26–27)**: Stream-reads external CSV catalogs using `BufferedReader`, verifies constraints, isolates failures via multi-catch (`IOException | NumberFormatException | InvalidCarException`), and exports structured audit reports to `data/reports/import_errors_<ts>.txt`.
- **Command DSL Script Importer via `StreamTokenizer` (L25)**: Lexically parses showroom automation scripts (`ADD`, `SELL`, `RESERVE`), handling string literals, numeric tokens, EOL boundaries, and skipping comment lines starting with `#`.
- **Byte-Stream Lossless Backup & Restore (L25)**: Executes direct 4KB binary copying via `FileInputStream` and `FileOutputStream` for disaster recovery.
- **Generic Repository Migration (L24 Generics, L17–20)**: Cross-migrates vehicle entities between storage backends (CSV $\to$ SQLite) while verifying fleet count parity.
- **Synthetic Supercar Generator (L4)**: Generates 100 or 1,000 spec-valid synthetic supercars for system stress testing.

---

## 2. Byte Streams vs. Character Streams (L25)

Java provides two primary stream hierarchies for file I/O:

| Feature | Byte Streams (`InputStream` / `OutputStream`) | Character Streams (`Reader` / `Writer`) |
| :--- | :--- | :--- |
| **Data Unit** | Raw 8-bit binary bytes | 16-bit Unicode characters (`char`) |
| **Encoding** | Encoding-agnostic (treats files as raw octets) | Automatically decodes/encodes charset (UTF-8, ISO-8859-1) |
| **Typical Use** | Binary files: SQLite databases (`.db`), image assets, compressed backups (`.bak`) | Text files: `.csv`, `.txt`, `.json`, logs |
| **RevLine Implementation** | `DataToolsService.backupFile` (`FileInputStream` / `FileOutputStream`) | `FileCarRepository`, `InputReader` (`BufferedReader` / `BufferedWriter`) |

### Lossless Byte-Stream Backup Loop (L25):
```java
try (FileInputStream fis = new FileInputStream(sourceFile);
     FileOutputStream fos = new FileOutputStream(backupFile)) {
    byte[] buffer = new byte[4096];
    int bytesRead;
    while ((bytesRead = fis.read(buffer)) != -1) {
        fos.write(buffer, 0, bytesRead);
    }
    fos.flush();
}
```
Operating directly on raw bytes guarantees 100% byte-identical backup copies without character encoding corruption.

---

## 3. Parsing Script DSLs via `java.io.StreamTokenizer` (L25)

`StreamTokenizer` converts an input character stream into a sequence of lexical tokens.

### Configuration in `DataToolsService`:
```java
StreamTokenizer st = new StreamTokenizer(bufferedReader);
st.resetSyntax();
st.whitespaceChars(0, ' ');      // ASCII 0-32 treated as delimiters
st.wordChars('a', 'z');          // Lowercase identifiers
st.wordChars('A', 'Z');          // Uppercase commands
st.wordChars('0', '9');          // Digits
st.wordChars('_', '_');          // Underscores
st.wordChars('-', '-');          // Hyphens
st.wordChars('.', '.');          // Decimals
st.quoteChar('"');               // Quoted strings e.g. "Ferrari"
st.commentChar('#');             // Shell/Python style comments
st.eolIsSignificant(true);       // Treat line endings as distinct tokens
```

### Key Token Fields:
- **`st.ttype`**: Token type (`TT_WORD`, `TT_NUMBER`, `TT_EOL`, `TT_EOF`, or character code such as `"`).
- **`st.sval`**: String value for word tokens and quoted literals.
- **`st.nval`**: Double precision value for parsed numbers.
- **`st.lineno()`**: Current line number for contextual error logging.

---

## 4. Multi-Catch & Fault-Tolerant CSV Import (L3, L26–27)

When importing bulk external CSV files, invalid rows must not crash the import process. RevLine uses Java 7+ **multi-catch** blocks to isolate row-level failures:

```java
try {
    double price = Double.parseDouble(parts[2].trim());
    int speed = Integer.parseInt(parts[3].trim());
    FuelType fuel = FuelType.fromString(parts[4].trim());
    Car car = new Car(1, brand, model, price, speed, fuel, hp, accel, status);
    inventoryService.addCar(car);
    success++;
} catch (NumberFormatException | InvalidCarException e) {
    // Multi-catch captures both numeric formatting errors and business constraint violations
    rejected++;
    errorLines.add(String.format("Line %d: Rejected (%s) -> [%s]", lineNumber, e.getMessage(), line));
}
```
If errors occur, an audit file `data/reports/import_errors_<timestamp>.txt` is generated with line numbers and exact failure causes.

---

## 5. Verification & Gate 7 Invariants

The Gate 7 test suite (`DataToolsTest`) enforces two fundamental storage contracts:

1. **Lossless Round-Trip Guarantee (Import $\to$ Export $\to$ Import)**:
   - Exporting catalog cars to a CSV file and importing them back into a fresh repository preserves every single field (id, brand, model, price, speed, fuel, hp, accel, status) identically.
2. **Byte-Identical Backup Guarantee**:
   - `areFilesByteIdentical(source, restored)` performs a byte-by-byte comparison verifying that restoring a backup file yields an exact binary clone.
3. **Automated Suite Results**:
   - Total core checks passing: **116 / 116** (600 ms).
   - Student exercises complete: **9 / 9** (100% completion!).
