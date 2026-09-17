# Installation

This page covers dependencies, runtime requirements, and native setup notes for **ExtractPDF4J**.

---

# Requirements

- **Java:** 17+
- **OS:** Linux, macOS, or Windows
- **Build tool:** Maven or Gradle

---

# Recommended Installation (Using BOM)

Starting from **v2.1.0**, ExtractPDF4J provides a **BOM (Bill of Materials)** that simplifies dependency management.

Import the BOM once, then declare modules without specifying versions.

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

Then simply declare the modules you need.

Example:

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-service</artifactId>
</dependency>
```

---

# Modules

ExtractPDF4J is split into multiple modules so you can depend only on what you need.

| Module | Description |
|------|-------------|
| `extractpdf4j-core` | Core extraction utilities and shared models |
| `extractpdf4j-service` | High-level API for table extraction |
| `extractpdf4j-cli` | Command-line interface |
| `extractpdf4j-bom` | Centralized dependency management |

---

# Maven Usage (Without BOM)

If you prefer not to use the BOM, you can declare modules directly.

## Core helpers

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-core</artifactId>
  <version>2.1.0</version>
</dependency>
```

## Service module

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-service</artifactId>
  <version>2.1.0</version>
</dependency>
```

## CLI module

```xml
<dependency>
  <groupId>io.github.extractpdf4j</groupId>
  <artifactId>extractpdf4j-cli</artifactId>
  <version>2.1.0</version>
</dependency>
```

---

# Gradle

Using the BOM:

```groovy
implementation platform("io.github.extractpdf4j:extractpdf4j-bom:2.1.0")

implementation "io.github.extractpdf4j:extractpdf4j-service"
```

Without BOM:

```groovy
implementation "io.github.extractpdf4j:extractpdf4j-core:2.1.0"
implementation "io.github.extractpdf4j:extractpdf4j-service:2.1.0"
implementation "io.github.extractpdf4j:extractpdf4j-cli:2.1.0"
```

---

# Native Dependencies

ExtractPDF4J relies on several PDF and image-processing libraries.

These include:

- Apache PDFBox
- OpenCV
- optionally **Tesseract + Leptonica** for OCR

---

# Recommended Native Setup

The easiest setup is to use **Bytedeco `*-platform` artifacts**, which bundle native binaries automatically.

This avoids manual native library installation.

---

# Manual Native Setup (Advanced)

If you provide your own native libraries:

Ensure they are available on your system library path.

Environment variables:

| OS | Variable |
|---|---|
| Linux | `LD_LIBRARY_PATH` |
| macOS | `DYLD_LIBRARY_PATH` |
| Windows | `PATH` |

---

# OCR Setup

If OCR is enabled and Tesseract language data is not found, configure:

```bash
export TESSDATA_PREFIX=/path/to/tessdata
```

On Windows, set `TESSDATA_PREFIX` in **System Environment Variables**.

---

# Local Documentation Setup

If you want to build the documentation locally:

```bash
pip install -r docs/requirements.txt
mkdocs build --strict
```

For local preview:

```bash
mkdocs serve
```

---

# Installation Guidance by Scenario

### Calling the Java API

Use:

```
extractpdf4j-service
```

---

### Building command-line workflows

Use:

```
extractpdf4j-cli
```

---

### Exposing extraction as a service

Use:

```
extractpdf4j-service
```

---

### Building full applications

Use:

```
extractpdf4j-service
```

and add additional modules only if needed.

---

See **[OCR tuning](advanced-usage/ocr-tuning.md)** for recommended OCR configuration and performance settings.