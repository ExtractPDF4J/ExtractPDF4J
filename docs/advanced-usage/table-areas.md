# Table Areas

Some releases may support selecting table regions explicitly.

> Placeholder guidance: confirm exact API names in Javadocs before integrating.

## Why constrain areas?

- Ignore headers/footers
- Improve precision on complex layouts
- Speed up extraction by reducing search space

## Placeholder example

```java
// Placeholder API: adapt to actual methods if available.
var parser = new com.extractpdf4j.parsers.StreamParser("statement.pdf");
// parser.tableArea("x1,y1,x2,y2");
var tables = parser.parse();
```
