# Getting Started

This guide gives you the shortest path to extracting tables from PDFs in Java.

## Prerequisites

- Java 17+
- Maven or Gradle
- Optional OCR runtime when processing scanned/image-heavy PDFs

## Pick a parser

- **StreamParser**: Best for text-based PDFs.
- **LatticeParser**: Best for ruled/gridded tables.
- **OcrStreamParser**: Best when no text layer exists.
- **HybridParser**: Best default for mixed documents.

## Minimal flow

1. Add dependency (`extractpdf4j-parser`).
2. Instantiate parser with PDF path.
3. Configure pages/options.
4. Call `parse()`.
5. Export `Table` data as CSV or process in memory.

Continue to [Installation](installation.md) and [Quickstart](quickstart.md).
