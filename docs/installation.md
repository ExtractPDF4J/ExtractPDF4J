# Installation

This page covers dependencies, runtime requirements, and native setup notes.

## Requirements

- **Java:** 17+
- **OS:** Linux, macOS, or Windows
- **Build tool:** Maven or Gradle

## Modules

ExtractPDF4J is split into multiple modules so you can depend only on what you need.

### Core parser module

Use this for table extraction logic in Java applications:

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-parser</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Core helpers

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-core</artifactId>
  <version>2.0.0</version>
</dependency>
```

## CLI module

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-cli</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Service module

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-service</artifactId>
  <version>2.0.0</version>
</dependency>
```

## Gradle

```bash
implementation("io.github.extractpdf4j:extractpdf4j-parser:2.0.0")
implementation("io.github.extractpdf4j:extractpdf4j-core:2.0.0")
implementation("io.github.extractpdf4j:extractpdf4j-cli:2.0.0")
implementation("io.github.extractpdf4j:extractpdf4j-service:2.0.0")
```

## Native dependencies

ExtractPDF4J relies on PDF and image-processing libraries, including:

- Apache PDFBox
- OpenCV
- optionally Tesseract + Leptonica for OCR

## Recommended setup

The easiest path is to use Bytedeco *-platform artifacts so native binaries are bundled with your application.
This avoids most manual native library setup.

**If you bring your own native libraries**

If you are not using bundled native artifacts:

- ensure native libraries are on your OS library path
- set the appropriate environment configuration:
    - LD_LIBRARY_PATH on Linux
    - DYLD_LIBRARY_PATH on macOS
    - PATH on Windows

## OCR setup

**If OCR is enabled and Tesseract language data is not found, set:**
```bash
export TESSDATA_PREFIX=/path/to/tessdata
```
On Windows, you can set it in system environment variables.

## Local docs setup

If you want to build the documentation locally:

```bash
pip install -r docs/requirements.txt
mkdocs build --strict
```

For local preview:

```bash
mkdocs serve
```

## Installation guidance by scenario

**Only calling the Java API**

Use: `extractpdf4j-parser`

**Building scripts or command-line workflows**

Use: `extractpdf4j-cli`

**Exposing extraction as a service**

Use: `extractpdf4j-service`

**Building end-to-end applications**

Use: `extractpdf4j-parser`

**and add supporting modules only if needed**

See [OCR tuning](advanced-usage/ocr-tuning.md) for practical settings.
