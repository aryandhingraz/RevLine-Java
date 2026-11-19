# Tooling — RevLine-Java

## Development Environment

| Tool | Version | Purpose |
|------|---------|---------|
| JDK | OpenJDK 25.0.4.1 (Temurin) — target: JDK 17 LTS | Compilation and runtime |
| Git | 2.54.0 | Version control |
| Antigravity IDE | Current | AI-assisted development |

## Java Dependencies

| Dependency | Version | Source | SHA-256 | Why |
|------------|---------|--------|---------|-----|
| `org.xerial:sqlite-jdbc` | *(to be downloaded in Phase 4)* | Maven Central | *(to be verified)* | JDBC persistence for SQLite (the only DB driver needed) |

**No other jars.** The project compiles and runs in CSV mode without any external dependency.

## MCP Servers / Plugins / Skills

| Tool | Source | Pinned Version | Purpose | Vetting Notes |
|------|--------|----------------|---------|---------------|
| *(none installed yet)* | | | | |

### Vetting procedure
Per MASTER_PROMPT §4.4: every candidate must be reviewed for shell commands on install,
remote script downloads, credential access, broad permissions, and relevance to plain Java.
At most 3 skills. Any wholesale code generators are rejected.

## Workflows / Commands

| Command | Purpose |
|---------|---------|
| `scripts/build.bat` / `.sh` | Compile with `-Xlint:all`, fail on warning |
| `scripts/run.bat` / `.sh` | Build + run the application |
| `scripts/test.bat` / `.sh` | Build + compile tests + run TestRunner |
| `scripts/demo.bat` / `.sh` | Build + run with scripted input, save output |
