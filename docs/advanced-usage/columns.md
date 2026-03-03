# Columns

Column hints can improve extraction when spacing is ambiguous.

> Placeholder guidance: confirm exact API names in Javadocs before integrating.

## Placeholder example

```java
// Placeholder API: adapt to actual methods if available.
var parser = new com.extractpdf4j.parsers.StreamParser("report.pdf");
// parser.columns("72,144,288,432");
var tables = parser.parse();
```
