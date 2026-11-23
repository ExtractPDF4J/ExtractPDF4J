# CLI

ExtractPDF4J includes CLI support for running table extraction from the terminal.

## Default behavior

If you do not pass `--mode`, the CLI defaults to:

- `hybrid`

That means it behaves like:

```bash
--mode HYBRID
```

## Basic usage

```bash
java -jar extractpdf4j-cli-<version>.jar input.pdf \
  --pages all \
  --out tables.csv
```

## Supported modes

```bash
--mode STREAM|LATTICE|OCRSTREAM|HYBRID
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
CSV defaults to commas. Pass a single separator character, quoted when your shell treats it specially.

```bash
--sep ';'
```

## Output file
```bash
--out out.csv
```

If omitted, output is written to STDOUT.

## Try the synthetic bank statement

Download the [sample PDF](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/v2.3.1/examples/synthetic-bank-statement/synthetic_bank_statement_text.pdf) and the expected UTF-8 CSVs for [page 1](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/v2.3.1/examples/synthetic-bank-statement/bank_statement_hybrid-1.csv) and [page 2](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/v2.3.1/examples/synthetic-bank-statement/bank_statement_hybrid-2.csv). The statement contains fictional data.

From the root of a 2.3.1 checkout, build the CLI with Java 17 and run:

```bash
mvn -pl extractpdf4j-cli -am package -DskipTests
java -jar extractpdf4j-cli/target/extractpdf4j-cli-2.3.1.jar \
  examples/synthetic-bank-statement/synthetic_bank_statement_text.pdf \
  --mode STREAM --pages all --out bank_statement.csv
```

The CLI creates `bank_statement-1.csv` with 20 transactions and `bank_statement-2.csv` with 9. Both files have seven columns, including separate Value Date, Description, Debit, and Credit fields. STREAM uses the PDF's text layer. HYBRID produces the same CSVs for this sample.

## JSON output

JSON output is available starting in 2.3.0. To build the runnable CLI JAR from source,
run this from the repository root:

```bash
mvn -pl extractpdf4j-cli -am package -DskipTests
```

The JAR is in `extractpdf4j-cli/target/`. Replace `<version>` in the examples
with the version from the source POM.

Use `--json` to produce one structured JSON document for all detected tables.
This is useful when the result is consumed by services, Kafka consumers, or
other JSON-aware pipeline stages.

```bash
java -jar "extractpdf4j-cli/target/extractpdf4j-cli-<version>.jar" examples/Sample_Utility_Bill.pdf \
  --mode STREAM \
  --json \
  --out tables.json
```

The command writes one file even if the PDF contains multiple tables:

```json
{"tables":[{"rows":[["Date","Description","Amount"],["2026-01-01","Example","42.00"]]}]}
```

Omit `--out` to send the same JSON document to STDOUT, for example:

```bash
java -jar "extractpdf4j-cli/target/extractpdf4j-cli-<version>.jar" examples/Sample_Utility_Bill.pdf --mode STREAM --json
```

`--json` requires no additional libraries or environment variables. For scanned
PDFs, use the usual OCR prerequisites: bundled Bytedeco native artifacts are
recommended; otherwise ensure OpenCV/Tesseract libraries are available on the
system `PATH` and set `TESSDATA_PREFIX` when Tesseract language data is not
found. Each cell is emitted as a JSON string. If no tables are detected, the
CLI reports that on standard error and produces no JSON document.

## Run the CLI in Docker

Build the dedicated CLI image from the repository root. The default native
platform is `linux-x86_64`. For a supported alternative, set
`--build-arg JAVACPP_PLATFORM=<platform>` to match the image architecture.

```bash
docker build -f extractpdf4j-cli/Dockerfile -t extractpdf4j-cli:latest .
docker run --rm \
  --mount "type=bind,source=$PWD/examples,target=/app/inputs,readonly" \
  extractpdf4j-cli:latest /app/inputs/Sample_Utility_Bill.pdf --mode STREAM --json
```

To save files directly, mount a writable host directory and pass an `--out`
path inside that mount. See the [CLI Docker guide](https://github.com/ExtractPDF4J/ExtractPDF4J/blob/main/extractpdf4j-cli/cli-docker-image.md)
for output mounts and Windows commands.

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
java -jar extractpdf4j-cli-<version>.jar scan.pdf \
  --mode LATTICE \
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
java -jar extractpdf4j-cli-<version>.jar statement.pdf \
  --mode HYBRID \
  --pages all \
  --dpi 400 \
  --out tables.csv
```

## Output behavior

**When** `--out` **is omitted:**

The CLI prints CSV with a heading for each table, or one JSON document when `--json` is set.

**When** `--out` **is provided:**

Output is written to file.

**When multiple tables are exported as CSV:**

The CLI numbers the output files, for example:

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
