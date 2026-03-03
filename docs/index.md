# ExtractPDF4J

## Extract tables from any PDF — text-based, scanned, or image-heavy.

ExtractPDF4J is a Java-native table extraction toolkit built for real-world documents.

- **Text PDFs**: Use coordinate-based parsing.
- **Scanned PDFs**: Use OCR + structure detection.
- **Mixed PDFs**: Use hybrid extraction.

<div class="grid cards" markdown>

-   :material-rocket-launch: **Start quickly**

    ---

    Install dependencies and run your first extraction in minutes.

    [Getting Started](getting-started.md)

-   :material-console: **Use CLI or Java API**

    ---

    Use the command line for fast exports, or embed parsers in your app.

    [CLI Guide](cli.md)

-   :material-cog-transfer: **Choose parser strategy**

    ---

    Stream, Lattice, OCR Stream, and Hybrid each target different PDF structures.

    [How It Works](how-it-works/overview.md)

</div>

## Java example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.util.List;

List<Table> tables = new HybridParser("sample.pdf")
    .pages("all")
    .dpi(300f)
    .parse();
```

> This project evolves quickly. If method names differ in your version, treat snippets as implementation patterns.
