# ExtractPDF4J

**Java-native PDF table extraction for text-based, scanned, and image-heavy documents.**

ExtractPDF4J is a production-focused Java library for extracting **tables** and structured data from PDFs. It supports both:

- **text-based PDFs** (where a text layer exists), and
- **scanned/image-heavy PDFs** (where OCR and image-based table detection are required).

It is built for real-world extraction problems such as invoices, statements, reports, forms, and mixed-layout documents.

## Why ExtractPDF4J?

PDF extraction often fails in production because real documents are inconsistent:

- some pages are text-based, others are scanned,
- some tables are ruled, others are free-form,
- headers can shift,
- rows can wrap,
- and OCR quality can vary.

ExtractPDF4J addresses that by offering multiple extraction modes:

- **StreamParser** for text-based layouts
- **LatticeParser** for ruled tables and grid-based layouts
- **OcrStreamParser** for OCR-assisted recovery
- **HybridParser** for mixed or uncertain document types

## Quick example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class QuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("scanned_invoice.pdf")
                .dpi(300f)
                .parse();

        if (!tables.isEmpty()) {
            System.out.println(tables.get(0).toCSV(','));
        }
    }
}
```

## What problem it solves

**Use ExtractPDF4J when you need to:**
- extract tables from invoices
- parse bank statements
- convert PDF reports into CSV
- automate ingestion of scanned operational documents
- reduce manual retyping of rows and columns

## Key strengths

- Java-native extraction workflow
- Supports scanned and OCR-driven documents
- Multiple parser strategies for different layouts
- CLI support for batch and one-off extraction
- Docker-ready sample microservice
- Suitable for developer tooling and production integrations

## Choose your path

- New to the project? Start with [Getting Started](getting-started.md)
- Installing dependencies? Go to [Installation](installation.md)
- Want code examples? See [Quickstart](quickstart.md)
- Want terminal usage? See [CLI](cli.md)
- Want deeper internals? Explore [How It Works](how-it-works/overview.md)
