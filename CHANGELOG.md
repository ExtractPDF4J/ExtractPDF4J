# Changelog

All notable changes to **ExtractPDF4J** will be documented in this file.

The project follows **Semantic Versioning** and this changelog format is inspired by **Keep a Changelog**.

---

## [2.1.0] - 2026

### Added
- Introduced **`extractpdf4j-bom`** module for centralized dependency management.
- Added support for importing ExtractPDF4J dependencies via **Maven BOM**.
- Simplified dependency alignment across all ExtractPDF4J modules.
- Updated the documentation and published in GitHub pages.

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

[2.1.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v2.1.0
[2.0.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v2.0.0
[0.1.2]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.2
[0.1.1]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.1
[0.1.0]: https://github.com/ExtractPDF4J/ExtractPDF4J/releases/tag/v0.1.0  