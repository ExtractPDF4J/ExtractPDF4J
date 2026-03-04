# ExtractPDF4J

**Java-native PDF table extraction for text-based, scanned, and image-heavy documents.**

ExtractPDF4J is a production-focused Java library for extracting **tables** and structured data from PDFs in real-world conditions.

It is designed for documents where extraction often becomes unreliable in practice:

- text-based PDFs with inconsistent layout
- scanned PDFs with no usable text layer
- ruled and borderless tables
- mixed multi-page documents
- OCR-heavy operational files

Whether you are processing invoices, statements, reports, forms, or internal business documents, ExtractPDF4J gives you multiple parsing strategies under one Java-first API.

## Why ExtractPDF4J?

PDF table extraction is not one problem — it is a family of problems.

A single parser often works for ideal documents but fails when:

- one PDF is text-based and the next is scanned
- some tables are ruled while others are spacing-based
- rows wrap across lines
- headers drift between document versions
- OCR quality changes across scans

ExtractPDF4J addresses this by providing multiple extraction modes built for different layout types:

- **StreamParser** for text-based PDFs
- **LatticeParser** for ruled and grid-based tables
- **OcrStreamParser** for OCR-backed recovery
- **HybridParser** for mixed or uncertain input

This gives you a more practical and production-ready extraction toolkit than a single-strategy approach.

## Quick example

Start with `HybridParser` if you want the safest default for mixed or unknown PDFs.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("sample.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        if (!tables.isEmpty()) {
            System.out.println(tables.get(0).toCSV(','));
        }
    }
}
```

This is a strong first choice because `HybridParser` can help when your input varies across files or pages.

## What problem it solves

Use ExtractPDF4J when you need to:

- extract tables from invoices and statements
- convert PDF tables into CSV for downstream processing
- process scanned operational documents
- reduce manual retyping of tabular data
- build Java applications that ingest structured PDF content
- support mixed text and scanned document pipelines

## Key strengths

- **Java-first API** for application integration
- **Multiple parser strategies** for different PDF layouts
- **Scanned PDF support** through OCR-backed extraction
- **CLI support** for one-off runs and batch workflows
- **Debug-friendly tuning** for real production troubleshooting
- **Docker-friendly service integration** for API-style deployment
- **Consistent `List<Table>` output model** across parser modes

## Choose your path

### New to the project?
Start with:

- [Getting Started](getting-started.md)

### Need dependencies and setup?
Go to:

- [Installation](installation.md)

### Want working Java examples?
See:

- [Quickstart](quickstart.md)

### Prefer terminal-based usage?
Use:

- [CLI](cli.md)

### Want to understand the parser internals?
Explore:

- [How It Works](how-it-works/overview.md)

### Need tuning for difficult PDFs?
Go to:

- [Advanced Usage](advanced-usage/page-ranges.md)

### Want API-level reference?
See:

- [API Overview](api/overview.md)
- [Javadocs](https://extractpdf4j.github.io/ExtractPDF4J/apidocs/index.html)

## Recommended first workflow

If you are evaluating ExtractPDF4J for the first time, this is the best path:

1. Read **Getting Started**
2. Follow **Installation**
3. Run the **Quickstart** example with `HybridParser`
4. Validate the first extracted table as CSV
5. Move to parser-specific pages only if you need tighter tuning

This keeps your first integration simple and avoids premature optimization.

## Typical use cases

ExtractPDF4J is well suited for:

- invoice line-item extraction
- bank statement parsing
- utility bill table extraction
- report-to-CSV conversion
- scanned archive processing
- document ingestion pipelines in internal enterprise systems

## API reference

Use the Java API when you need:

- application integration
- pipeline orchestration
- custom validation logic
- post-processing in code
- parser tuning inside your service

Use the CLI when you need:

- quick local extraction
- shell scripting
- batch jobs
- debugging parser behavior before embedding the library

## Javadocs

For exact classes and method signatures:

- [Javadocs](https://extractpdf4j.github.io/ExtractPDF4J/apidocs/index.html)

## Project links

- [GitHub Repository](https://github.com/ExtractPDF4J/ExtractPDF4J)

## Next step

If you are starting fresh, begin with:

- [Getting Started](getting-started.md)
