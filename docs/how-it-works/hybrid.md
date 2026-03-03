# Hybrid Parser

`HybridParser` combines multiple strategies to improve resilience across mixed PDFs.

## Why use it

- Single default mode for unknown document characteristics
- Handles text-based, scanned, and image-heavy pages in one flow

## Java example

```java
import com.extractpdf4j.parsers.HybridParser;

var tables = new HybridParser("mixed-document.pdf")
    .pages("all")
    .dpi(300f)
    .parse();
```

## Notes

- Great first choice in production pipelines.
- Fine-tune by falling back to strategy-specific parsers when needed.
