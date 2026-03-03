# Getting Started

ExtractPDF4J is designed for Java teams that need table extraction from:

- **text-based PDFs** (digital statements/reports)
- **scanned PDFs** (image pages)
- **image-heavy/mixed PDFs** (hybrid extraction scenarios)

## What problem it solves

Many PDF workflows fail when documents do not have a clean text layer.
ExtractPDF4J provides parser strategies that handle both coordinate-based text extraction and OCR-assisted extraction.

## Choose a parser mode

| Parser | Best for | Typical tradeoff |
| --- | --- | --- |
| `StreamParser` | Text-based PDFs with selectable text | Fast, but depends on text layer quality |
| `LatticeParser` | Ruled/gridded tables, many scanned statements | Needs stronger line structure |
| `OcrStreamParser` | Scanned/image-heavy documents with weak/no text layer | Higher runtime due to OCR |
| `HybridParser` | Mixed or unknown PDF types | Most robust default |

## Minimal flow

1. Add dependency (`extractpdf4j-parser`).
2. Instantiate parser with PDF path.
3. Set page range and optional tuning (DPI/debug/OCR backend).
4. Call `parse()`.
5. Export table data as CSV or map to your domain models.

## First Java snippet

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.util.List;

List<Table> tables = new HybridParser("scanned_invoice.pdf")
    .pages("all")
    .dpi(300f)
    .parse();

if (!tables.isEmpty()) {
  System.out.println(tables.get(0).toCSV(','));
}
```

Next: [Installation](installation.md) → [Quickstart](quickstart.md).
