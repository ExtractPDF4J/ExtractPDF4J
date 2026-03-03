# Models

`Table` is the central output model used across parser implementations.

## Typical operations

- Iterate rows/cells (API depends on release)
- Export to CSV (`toCSV(',')` pattern in current docs)
- Map extracted data into domain models

## Example

```java
var tables = new com.extractpdf4j.parsers.HybridParser("input.pdf").parse();
if (!tables.isEmpty()) {
  String csv = tables.get(0).toCSV(',');
  System.out.println(csv);
}
```

> Placeholder note: row/cell accessor names may differ; check Javadocs for your version.
