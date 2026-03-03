# Page Ranges

Use page filters to reduce runtime and target relevant pages only.

## Common patterns

- `"all"`
- `"1"`
- `"1-3"`
- `"1,3-5"`

## Java example

```java
// Method names shown as common patterns; verify against your current release.
var tables = new com.extractpdf4j.parsers.HybridParser("report.pdf")
    .pages("2-4")
    .parse();
```
