# lib/ — External Dependencies

## sqlite-jdbc

| Property | Value |
|----------|-------|
| Group    | `org.xerial` |
| Artifact | `sqlite-jdbc` |
| Version  | `3.54.1.0` |
| SHA-256  | `f8dce420a6016278497f4f0560f65602b9db858de141b541cf91a4644ebe4639` |
| Source   | [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/) |

**This is the only external dependency.** `sqlite-jdbc-3.54.1.0.jar` is included here;
the Windows build and test scripts add `lib/*` to the classpath. CSV mode also works without it.

Download instructions are in `docs/TOOLING.md`.
