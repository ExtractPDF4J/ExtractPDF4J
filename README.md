# ExtractPDF4J

A production-focused **Java** library for extracting **tables** and structured data from PDFs. This README mirrors the actual code in `package io.github.mehulimukherjee` while keeping the same depth and clarity as a full, project-level README.

> Built on PDFBox (text extraction & rendering), OpenCV (grid/lines), and optional OCR (Tesseract/Leptonica via Bytedeco).

---

## Table of Contents

- [Features](#features)
- [CLI Quickstart](#cli-quickstart)
- [Architecture](#architecture)
- [Project Status](#project-status)
- [Requirements](#requirements)
- [Install](#install)
  - [Maven](#maven)
  - [Gradle](#gradle)
  - [Native Notes](#native-notes)
- [Quick Start](#quick-start)
  - [Stream (Text-based)](#stream-text-based)
  - [Lattice (Ruled/Scanned)](#lattice-ruledscanned)
  - [Hybrid (Mixed Documents)](#hybrid-mixed-documents)
  - [OCR-assisted Stream](#ocr-assisted-stream)
- [Configuration](#configuration)
- [YAML Rules (Normalization)](#yaml-rules-normalization)
- [Logging](#logging)
- [Exports](#exports)
- [Performance Tips](#performance-tips)
- [OCR Preprocessing Tips](#ocr-preprocessing-tips)
- [Error Handling](#error-handling)
- [Known Limitations](#known-limitations)
- [Troubleshooting](#troubleshooting)
- [Roadmap](#roadmap)
- [Contributing](#contributing)
- [Versioning](#versioning)
- [License](#license)
- [Acknowledgements](#acknowledgements)

---

## Features

- **Parsers available**
  - `StreamParser` — ideal for digitally generated PDFs (uses text positions via PDFBox).
  - `LatticeParser` — detects lines/grids (OpenCV) to reconstruct table cells; works well for scans.
  - `HybridParser` — combines strategies and returns unified tables.
  - `OcrStreamParser` — performs OCR-backed stream parsing for image PDFs.
- **Page selection**: `BaseParser.pages(String)` — e.g., `"1"`, `"2-5"`, `"1,3-4"`, `"all"`.
- **CSV export**: `Table#toCSV(char)`.

> The current repository does **not** include `ParserConfig`, `AutoParser`, or `ParseResult`. If you want those APIs for compatibility, see **Roadmap** for drop-in stubs.

---

## Architecture

```
PDF (text-based) ──► PDFBox text positions ─┐
                                            ├─► StreamParser ──► Table (cells)
PDF (scanned)    ──► Render to image ──► OpenCV lines/grids ──► LatticeParser ──► Table
                                           └─► OCR (Tesseract) ──────► OcrStreamParser
HybridParser ── coordinates and merges results from the above
```

- `BaseParser` provides core workflow (file path, page ranges, `parse()` pipeline).
- `StreamParser` works from PDFBox text coordinates.
- `LatticeParser` runs line detection, grid construction, and cell assignment.
- `OcrStreamParser` adds OCR text where no text layer exists.
- `HybridParser` orchestrates multiple strategies, returning a `List<Table>`.

---

## CLI Quickstart

The CLI defaults to **hybrid mode**. If you do not pass `--mode`, it behaves like `--mode hybrid`.

```bash
java -jar extractpdf4j-parser-0.1.1.jar input.pdf \
  --pages all \
  --out tables.csv
```

Common flags:

- `--mode stream|lattice|ocrstream|hybrid` (default: hybrid)
- `--pages 1|all|1,3-5`
- `--sep ,` (CSV separator)
- `--out out.csv` (omit to print to STDOUT)
- `--dpi 300` (use 300–450 for scans)
- `--debug` and `--debug-dir debug/`
- `--ocr auto|cli|bytedeco`

See also the changelog entry for this documentation pass: [CHANGELOG](CHANGELOG.md).

---

## Project Status

- Build tool: **Maven**
- Coordinates (current): `io.github.mehulimukherjee:extractpdf4j-parser:0.1.0`
- Java: **17+** (recommended 17+ runtime)

---

## Requirements

- **JDK**: 17+
- **OS**: Linux / macOS / Windows
- **Libraries**:
  - Apache **PDFBox**
  - Bytedeco **OpenCV** (with native binaries)
  - (Optional) Bytedeco **Tesseract** + **Leptonica** for OCR

> Tip: Prefer bytedeco `*-platform` artifacts. They ship native binaries and avoid manual OS setup.

---

## Install

### Maven

```xml
<dependency>
  <groupId>io.github.mehulimukherjee</groupId>
  <artifactId>extractpdf4j-parser</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Gradle

```kotlin
implementation("io.github.mehulimukherjee:extractpdf4j-parser:0.1.0")
```

### Native Notes

- If using bytedeco `*-platform`, you should **not** need extra steps.
- If you bring your own OpenCV/Tesseract:
  - Ensure the native libs are on your OS library path (e.g., `LD_LIBRARY_PATH`, `DYLD_LIBRARY_PATH`, or Windows `PATH`).
  - Set `TESSDATA_PREFIX` to find language data if OCR is enabled.

---

## Quick Start

### Stream (Text-based)

```java

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.StreamParser;
import java.nio.file.*;
import java.util.List;

public class StreamQuickStart {
  public static void main(String[] args) throws Exception {
    List<Table> tables = new StreamParser("samples/statement.pdf")
            .pages("1-3")   // or "all"
            .parse();

    if (!tables.isEmpty()) {
      Files.createDirectories(Path.of("out"));
      Files.writeString(Path.of("out/stream_table.csv"), tables.get(0).toCSV(','));
    }
  }
}
```

### Lattice (Ruled/Scanned)

```java

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.LatticeParser;
import java.io.File;
import java.nio.file.*;
import java.util.List;

public class LatticeQuickStart {
  public static void main(String[] args) throws Exception {
    List<Table> tables = new LatticeParser("samples/scanned.pdf")
            .dpi(300f)           // common for scans
            .keepCells(true)     // keep empty cells (if method is present)
            .debug(true)         // write debug artifacts (if method is present)
            .debugDir(new File("out/debug"))
            .pages("all")
            .parse();

    Files.createDirectories(Path.of("out"));
    for (int i = 0; i < tables.size(); i++) {
      Files.writeString(Path.of("out/lattice_table_" + i + ".csv"), tables.get(i).toCSV(','));
    }
  }
}
```

### Hybrid (Mixed Documents)

```java

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.util.List;

public class HybridQuickStart {
  public static void main(String[] args) throws Exception {
    List<Table> tables = new HybridParser("samples/mixed.pdf")
            .pages("all")
            .parse();
    // process tables as needed
  }
}
```

### OCR-assisted Stream

```java

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.OcrStreamParser;
import java.util.List;

public class OcrQuickStart {
  public static void main(String[] args) throws Exception {
    List<Table> tables = new OcrStreamParser("samples/scan.pdf")
            .pages("1-2")
            .parse();
  }
}
```

---

## CLI

Run the bundled CLI to extract tables from a PDF.

Usage:

```bash
java -jar extractpdf4j-parser-0.1.1.jar <pdf>
     [--mode stream|lattice|ocrstream|hybrid]
     [--pages 1|all|1,3-5]
     [--sep ,]
     [--out out.csv]
     [--debug]
     [--dpi 300]
     [--ocr auto|cli|bytedeco]
     [--keep-cells]
     [--debug-dir <dir>]
```

- **--pages**: page selection. Accepts `"1"`, `"2-5"`, `"1,3-4"`, or `"all"`.
  - Examples:
    - `--pages 1` → only page 1
    - `--pages 1-3` → pages 1,2,3
    - `--pages 1-3,5` → pages 1,2,3 and 5
    - `--pages all` → all pages

Examples:

```bash
java -jar extractpdf4j-parser-0.1.1.jar scan.pdf --mode lattice --pages 1 --dpi 450 --ocr cli --debug --keep-cells --debug-dir debug_out --out p1.csv
java -jar extractpdf4j-parser-0.1.1.jar statement.pdf --mode hybrid --pages all --dpi 400 --out tables.csv
```

Notes:

- When `--out` is omitted, tables are printed to STDOUT in CSV form.
- When multiple tables are found and `--out` is provided, files are numbered by suffix (e.g., `out-1.csv`, `out-2.csv`).
- `--ocr` sets a system property read by OCR helpers; values: `auto`, `cli`, or `bytedeco`.

---

## Configuration

- `BaseParser#pages(String)` — set page ranges (e.g., `"1"`, `"2-5"`, `"1,3-4"`, or `"all"`).
- `LatticeParser` chainable options (available in source): `dpi(float)`, `keepCells(boolean)`, `debug(boolean)`, `debugDir(File)`.

> A full `ParserConfig` builder is **not** in this repository. If you want that style, we can add it in a minor release without breaking the current API.

---

## YAML Rules (Normalization)

While extraction focuses on finding table cells, many workflows need consistent headers and typed values. You can maintain a small YAML file to normalize results downstream (header aliases, alignment to canonical names, and date/number formats). Example:

```yaml
headers:
  aliases:
    "Txn Date": ["Transaction Date", "Date"]
    "Description": ["Details", "Narration"]
    "Amount": ["Debit/Credit", "Amt"]

schema:
  align:
    - ["Txn Date", "Description", "Amount"]

formats:
  date:
    input: ["yyyy-MM-dd", "dd/MM/yyyy", "dd-MMM-uuuu"]
    output: "yyyy-MM-dd"
  number:
    decimal_separator: "."
    thousand_separator: ","
    currency_symbols: ["$", "₹", "€"]
```

How to apply:

- Map extracted header texts to canonical names using `headers.aliases`.
- Reorder/ensure columns using `schema.align`.
- Parse and reformat values using `formats.date` and `formats.number`.

Note: The core library does not interpret YAML natively; this pattern keeps normalization explicit in your app while remaining stable across parser updates.

---

## Logging

This project uses [SLF4J](https://www.slf4j.org/). You can bind it to any backend (e.g., Logback, Log4j2, or the simple logger).

- **INFO** logs announce when a table is detected
- **DEBUG** logs provide details:
  - inferred column boundaries
  - detected row positions
  - final grid dimensions

### Enable DEBUG logs

If using the SLF4J Simple backend (`slf4j-simple`), enable debug output with:

```bash
java -Dorg.slf4j.simpleLogger.defaultLogLevel=debug 
```

---

## Exports

- **CSV**: `Table#toCSV(char sep)` → returns the CSV as a string.
- **Programmatic access**: common accessors include `nrows()`, `ncols()`, and `cell(int row, int col)` (if present in your Table class).

**CSV example**

```java
Files.writeString(Path.of("out/table.csv"), table.toCSV(','));
```

(If you want bulk exports, add the optional `Results` helper and use `Results.exportAllCsv(tables, Path.of("out/csv"), ',');`.)

---

## Performance Tips

- Prefer **page ranges** (`pages("1-3")`) over `"all"` when you know where tables are.
- For scans, choose appropriate **DPI** (e.g., 300f). Try `keepCells(true)` to preserve empty grid cells.
- Enable `debug(true)` in `LatticeParser` when tuning; inspect overlays and artifacts in `debugDir`.
- Process files in parallel if you have lots of independent documents.

---

## OCR Preprocessing Tips

For image/scanned PDFs, use lattice or OCR-assisted parsing. Helpful flags and settings:

- `--dpi 300` (try 300–450); higher DPI improves line and OCR accuracy.
- `--ocr auto|cli|bytedeco` to choose OCR backend. Default is `auto`.
- `--debug --debug-dir debug/` to dump intermediate artifacts.
- System properties for OCR CLI fine-tuning:
  - `-Dtess.lang=eng` language
  - `-Dtess.psm=6` page segmentation mode (6 = uniform block)
  - `-Dtess.oem=1` engine mode
  - `-Docr.debug=true` to dump last TSV when no words are detected

Example (lattice, 450 DPI, CLI OCR, debug artifacts):

```bash
java -Dtess.lang=eng -Dtess.psm=6 -Dtess.oem=1 -Docr.debug=true \
  -jar extractpdf4j-hybrid-0.2.0.jar scan.pdf \
  --mode lattice --dpi 450 --ocr cli --debug --debug-dir debug \
  --out tables.csv
```

Before/after (conceptual):

- Before: low-DPI scan at 150, faint grid → few or no tables detected.
- After: re-run at `--dpi 400` with `--debug` to inspect binarization; switch to `--mode ocrstream` if the text layer is missing.

---

## Error Handling

- `parse()` methods throw `IOException` for file issues.
- When no tables are found, parsers typically return an **empty list** — check `tables.isEmpty()` before writing files.

---

## Known Limitations

- Low-resolution or skewed scans can reduce grid detection and OCR accuracy.
- Handwritten notes/stamps can confuse line detection; crop or pre-process to avoid noisy regions.
- Nested/complex tables work best with lattice; hierarchical exports (JSON/XLSX) require additional code.

---

## Troubleshooting

- **UnsatisfiedLinkError**: ensure the bytedeco `*-platform` artifacts are used (they ship natives). If using system libs, check `LD_LIBRARY_PATH` / `DYLD_LIBRARY_PATH` / `PATH`.
- **No tables found**: try `HybridParser`, increase scan `dpi(300f)`, enable `debug(true)` and inspect overlays in `debugDir(...)`.
- **Garbled text on scans**: use `OcrStreamParser` instead of `StreamParser`.

---

## Roadmap

- Optional **AutoParser** (delegates to `HybridParser`) — convenience wrapper.
- `Table#toJson()` and `Table#toXlsx(Path)` methods.
- `Results.exportAllCsv/Json(...)` bulk helpers.
- (Future) A formal `ParserConfig` builder with common options.

A **stubs & patch** bundle is available to enable these APIs today without breaking changes.

---

## Contributing

PRs welcome! For lattice/OCR changes, include screenshots of debug artifacts if possible. Keep sample PDFs **tiny and sanitized**.
(See `CONTRIBUTING.md`)

---

## Versioning

Semantic Versioning (**SemVer**): MAJOR.MINOR.PATCH.

---

## License

ExtractPDF4J is licensed under Apache-2.0 (See `LICENSE`).

---

## Acknowledgements

- Inspired by **Camelot** (Python) and **Tabula**.
- Built on **Apache PDFBox**, **OpenCV/JavaCV**, and **Tesseract**.
- Thanks to contributors and users who reported edge cases and shared sample PDFs.

---
