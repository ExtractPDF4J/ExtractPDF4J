# Changelog

All notable changes to **ExtractPDF4J** will be documented in this file.

The project follows **Semantic Versioning** and this changelog format is inspired by **Keep a Changelog**.

---

## [2.3.0]

### Added

- Export all detected tables as one JSON document with the CLI's `--json` flag.
- Add experimental, opt-in local ML fallback through `HybridMlParser` and the `MlTableExtractor` SPI.
- Add a runnable Maven example that processes a folder of PDFs and writes one CSV per detected table.
- Add a dedicated Docker image build for the CLI.

### Changed

- Align JavaCPP, OpenCV, Tesseract, and Leptonica on Bytedeco 1.5.14. Update PDFBox, Spring Boot, Picocli, Lombok, JUnit, and Maven Surefire.

### Fixed

- Preserve visual row order and column alignment in statement extraction, keep distinct ruled grids separate, and bound hybrid confidence scores to 0–1.
- Improve OCR stream normalization for locale-specific amounts, accounting signs, credit markers, and month-name dates.

- Include the Maven reactor module descriptors required to build the CLI and service Docker images.

## [2.2.0]

### Added

- Support custom, single-character delimiters in `CsvExporter`.
- Add opt-in extraction diagnostics for parser selection, timing, warnings, and fallback actions.

## [2.1.0] - 2026

### Added
- Introduced **`extractpdf4j-bom`** module for centralized dependency management.
- Added support for importing ExtractPDF4J dependencies via **Maven BOM**.
- Simplified dependency alignment across all ExtractPDF4J modules.
- Introduces a full documentation site powered by MkDocs.

Example:

```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.github.extractpdf4j</groupId>
      <artifactId>extractpdf4j-bom</artifactId>
      <version>2.1.0</version>
      <type>pom</type>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### Improved
- Improved **multi-module Maven project structure**.
- Improved dependency management across:
    - `extractpdf4j-core`
    - `extractpdf4j-service`
    - `extractpdf4j-cli`
- Improved Maven Central publishing workflow.

### Fixed
- Corrected **SCM metadata configuration** in the root POM.

---

## [2.0.0] - 2025

Major release introducing a **production-grade Java table extraction engine**.

### Added
- Hybrid table extraction engine combining:
    - **Stream parsing** for text-based PDFs
    - **Lattice parsing** for grid-based tables
    - **OCR fallback** for scanned documents
- Native OCR integration via **Tesseract and OpenCV**.
- **Command-line interface (CLI)** for batch extraction.
- **Spring Boot service module** for REST-based document processing.
- CSV export support for extracted tables.
- Configurable OCR DPI settings and extraction modes.

### Improved
- Improved extraction reliability for complex PDF layouts.
- Enhanced handling of multi-page tables.
- Improved OCR detection strategies for scanned documents.

---

## [0.1.2] - 2025

### Added
- CLI quality gates:
    - `--min-score`
    - `--require-headers`
- REST extraction endpoint:

```
POST /api/extract
```

- Asynchronous document extraction using `@Async`.
- Docker multi-stage build support.

### Improved
- OCR strategy improvements including:
    - automatic Tesseract PSM selection
    - support for multi-language OCR (`eng+fra`, etc.)
- Improved logging and extraction diagnostics.

---

## [0.1.1] - 2025

### Improved
- Improved table detection reliability.
- Minor improvements in parsing accuracy.

---

## [0.1.0] - 2025

Initial public release of **ExtractPDF4J**.

### Added
- Java-native PDF table extraction library.
- Stream-based parsing for text-based PDFs.
- Lattice-based parsing for structured tables.
- OCR integration using **Tesseract**.
- Support for multi-page table extraction.
- CSV export of extracted tables.

---

## About ExtractPDF4J

**ExtractPDF4J** is an open-source Java library designed to extract structured tables from both:

- text-based PDFs
- scanned PDFs

The library combines:

- layout-aware parsing
- lattice grid detection
- OCR-based extraction
- hybrid parsing strategies

to reliably extract tabular data from real-world documents such as:

- bank statements
- invoices
- financial reports
- operational documents

---

## Links

[2.3.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/compare/v2.2.0...v2.3.0
[2.2.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v2.2.0
[2.1.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v2.1.0
[2.0.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v2.0.0
[0.1.2]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.2
[0.1.1]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.1
[0.1.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.0  