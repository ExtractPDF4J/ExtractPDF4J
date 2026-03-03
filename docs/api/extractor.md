# Extractor

Most integrations create a parser instance with a PDF path and call `parse()`.

## Common pattern

```java
import com.extractpdf4j.helpers.Table;
import java.util.List;

// Choose parser implementation based on document characteristics.
List<Table> tables = new com.extractpdf4j.parsers.HybridParser("input.pdf")
    .parse();
```

> Placeholder note: constructor overloads and builder-style options may vary by version.
