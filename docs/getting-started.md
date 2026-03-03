# Getting Started

This guide helps you understand what ExtractPDF4J is for, when to use each parser, and how to get your first successful extraction.

## What ExtractPDF4J solves

Many PDF extraction tools work for ideal documents, but real-world files are messy:

- scanned pages with no text layer
- inconsistent column alignment
- wrapped descriptions
- mixed structured and unstructured sections
- partial tables across multiple pages

ExtractPDF4J is designed to handle these scenarios using multiple parsing strategies.

## Supported document types

ExtractPDF4J is useful for:

- invoices
- bank statements
- utility bills
- transaction reports
- financial statements
- tabular operational reports
- mixed scanned + text PDFs

## Parser overview

### StreamParser
Use this when the PDF has a clean text layer and the table structure can be inferred from text positions.

Best for:
- digital statements
- generated reports
- text-based exports

### LatticeParser
Use this when the PDF contains visible table borders, ruled cells, or grid-like structure.

Best for:
- boxed invoices
- ruled statements
- scanned forms with table lines

### OcrStreamParser
Use this when the document is scanned and lacks a usable text layer, but OCR can recover readable text.

Best for:
- scanned statements
- photographed PDFs
- image-heavy pages

### HybridParser
Use this when:
- the document type is mixed,
- you are unsure which strategy fits,
- or you want a strong default for production.

Best for:
- unknown PDFs
- mixed text/scanned batches
- automation pipelines

## Recommended first run

For most users, start with `HybridParser`:

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class FirstRun {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("sample.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Sample PDFs

The project includes sample PDFs in `/examples` to help you test extraction behavior safely.

**Examples include:**

- **utility-bill** style extraction
- **multi-page statement-style** extraction

## These are intended to help you understand:

- page structure
- table repetition
- realistic output formatting

## What success looks like

A successful extraction typically means:
- the expected number of tables is found
- rows and columns are aligned correctly
- CSV output is usable downstream
- headers are readable enough for normalization
- scanned pages return stable OCR-backed content

## Next steps

- Go to [Installation](installation.md) to set up dependencies
- Then use [Quickstart](quickstart.md) for real code examples
- If you prefer terminal-based use, go to [CLI](cli.md)

