# Stream Parser

`StreamParser` is optimized for text-based PDFs with selectable text layers.

## Best for

- Statements and reports with consistent text alignment
- Tables without strong border lines

## Java example

```java
import com.extractpdf4j.parsers.StreamParser;

var tables = new StreamParser("statement.pdf")
    .pages("1-3")
    .parse();
```

## Notes

- Usually faster than OCR-based pipelines.
- May struggle if text is embedded as image content.
