# ExtractPDF4J

## Java-native table extraction for text-based, scanned, and image-heavy PDFs.

Build reliable table extraction pipelines for statements, invoices, forms, and mixed-quality PDFs.

- **Text-based PDFs**: Coordinate-aware stream parsing.
- **Scanned PDFs**: OCR-assisted extraction.
- **Image-heavy PDFs**: Hybrid strategy for robust fallback behavior.

<div class="grid cards" markdown>

-   :material-rocket-launch: **Get productive fast**

    ---

    Install and run your first extraction in minutes.

    [Getting Started](getting-started.md)

-   :material-console: **CLI + Java API**

    ---

    Automate with CLI or embed parsers directly in Java services.

    [CLI Guide](cli.md)

-   :material-cog-transfer: **Parser strategies**

    ---

    Learn when to use Stream, Lattice, OCR Stream, or Hybrid.

    [How It Works](how-it-works/overview.md)

</div>

## Quick Java snippet

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.util.List;

List<Table> tables = new HybridParser("sample.pdf")
    .pages("all")
    .dpi(300f)
    .parse();
```

## Why teams choose ExtractPDF4J

| Capability | ExtractPDF4J |
| --- | --- |
| Text PDFs | ✅ |
| Scanned/Image PDFs | ✅ Native OCR support |
| Multiple strategies | ✅ Stream, Lattice, OCR Stream, Hybrid |
| Java-first integration | ✅ |
