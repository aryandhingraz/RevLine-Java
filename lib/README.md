# lib/ — External Dependencies

## sqlite-jdbc

| Property | Value |
|----------|-------|
| Group    | `org.xerial` |
| Artifact | `sqlite-jdbc` |
| Version  | `3.54.1.0` |
| SHA-256  | `f8dce420a6016278497f4f0560f65602b9db858de141b541cf91a4644ebe4639` |
| Source   | [Maven Central](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.54.1.0/) |

**This is the only external dependency.** `sqlite-jdbc-3.54.1.0.jar` is included directly in this directory;
the build and run scripts automatically append `lib/*` to the classpath. The application's CSV mode also functions without this jar.

### Checksum Verification

Verified release artifact checksums from upstream Xerial build:

```text
f8dce420a6016278497f4f0560f65602b9db858de141b541cf91a4644ebe4639  sqlite-jdbc-3.54.1.0.jar
36141a613eb82eb776e82fc51f8257c2d046541d918cfb745becafd05778bc55  sqlite-jdbc-3.54.1.0-natives-windows.jar
fc7c39087617c708cecec40e7ff13b15c5cf4eb9c8524d8429c96a4c9b5d9487  sqlite-jdbc-3.54.1.0-natives-linux.jar
bad441132e6efaf7874a7cbe38d0ca48492464a4203f87fa546bb473da15bdff  sqlite-jdbc-3.54.1.0-natives-mac.jar
```
