# CLI

ExtractPDF4J includes CLI support for running table extraction from the terminal.

## Default behavior

If you do not pass `--mode`, the CLI defaults to:

- `hybrid`

That means it behaves like:

```bash
--mode hybrid
```

## Basic usage

```bash
java -jar extractpdf4j-parser-<version>.jar input.pdf \
  --pages all \
  --out tables.csv
```

## Supported modes

```bash
--mode stream|lattice|ocrstream|hybrid
```

## Mode guidance

- `stream` → text-based PDFs
- `lattice` → ruled/grid tables
- `ocrstream` → scanned/OCR-heavy pages
- `hybrid` → best general-purpose default

## Common flags

**Page selection**

```bash
--pages 1|all|1,3-5
```

Examples:

- `--pages 1` → page 1 only
- `--pages 1-3` → pages 1, 2, 3
- `--pages 1-3,5` → pages 1, 2, 3, and 5
- `--pages all` → all pages

## CSV separator
```bash
--sep ,
```

## Output file
```bash
--out out.csv
```

If omitted, output is written to STDOUT.

## JSON output

Use `--json` to produce one structured JSON document for all detected tables.
This is useful when the result is consumed by services, Kafka consumers, or
other JSON-aware pipeline stages.

```bash
java -jar extractpdf4j-cli-2.0.0.jar examples/Sample_Utility_Bill.pdf \
  --mode stream \
  --json \
  --out tables.json
```

The command writes one file even if the PDF contains multiple tables:

```json
{"tables":[{"rows":[["Date","Description","Amount"],["2026-01-01","Example","42.00"]]}]}
```

Omit `--out` to send the same JSON document to STDOUT, for example:

```bash
java -jar extractpdf4j-cli-2.0.0.jar examples/Sample_Utility_Bill.pdf --mode stream --json
```

`--json` requires no additional libraries or environment variables. For scanned
PDFs, use the usual OCR prerequisites: bundled Bytedeco native artifacts are
recommended; otherwise ensure OpenCV/Tesseract libraries are available on the
system `PATH` and set `TESSDATA_PREFIX` when Tesseract language data is not
found. The repository Docker image packages the Spring Boot service rather than
the CLI, so run the CLI command on the host or add the CLI shaded JAR to a
custom image.

## DPI
```bash
--dpi 300
```

Recommended:

- use 300–450 for scanned PDFs

## Debug output
```bash
--debug
--debug-dir debug_out
```

Use these when you want intermediate artifacts for troubleshooting.

## OCR mode
```bash
--ocr auto|cli|bytedeco
```

This controls how OCR helpers are selected.

## Extra controls
```bash
--keep-cells
--min-score 0-1
--require-headers Date,Description,Balance
```

Use these to tighten output control in more advanced workflows.

## Example: scanned PDF with lattice mode

```bash
java -jar extractpdf4j-parser-<version>.jar scan.pdf \
  --mode lattice \
  --pages 1 \
  --dpi 450 \
  --ocr cli \
  --debug \
  --keep-cells \
  --debug-dir debug_out \
  --out p1.csv
```

## Example: full-document hybrid extraction

```bash
java -jar extractpdf4j-parser-<version>.jar statement.pdf \
  --mode hybrid \
  --pages all \
  --dpi 400 \
  --out tables.csv
```

## Output behavior

**When** `--out` **is omitted:**

Tables are printed to STDOUT as CSV.

**When** `--out` **is provided:**

Output is written to file.

**When multiple tables are found:**

Files may be numbered with suffixes, for example:

- `out-1.csv`
- `out-2.csv`

## When to use the CLI

**The CLI is useful for:**

- ad hoc extraction
- batch processing
- shell scripting
- CI jobs
- pre-ingestion validation
- debugging parser behavior before integrating the Java API
