# Batch folder processing example

Minimal runnable demo that extracts tables from **every PDF in a directory** and writes one CSV file per document (first table only).

Uses ExtractPDF4J **2.2.0** (`extractpdf4j-core`) via the parent reactor.

## Prerequisites

- **JDK 17+**
- **Maven 3.9+**
- Native OCR/OpenCV binaries are bundled through Bytedeco platform artifacts (no separate Tesseract install required for most setups).
- If you use a custom Tesseract install, set `TESSDATA_PREFIX` to your `tessdata` directory.

## Build

From the repository root:

```bash
mvn -q -pl examples/batch-folder-processing -am package
```

## Run

Point the program at a folder of PDFs and an output directory (created if missing):

```bash
mvn -q -pl examples/batch-folder-processing exec:java \
  -Dexec.args="../../examples target/batch-out"
```

Or run the packaged JAR after a full reactor build:

```bash
java -jar examples/batch-folder-processing/target/extractpdf4j-example-batch-folder-2.2.0.jar \
  examples target/batch-out
```

### Expected output

For each input PDF named `Sample_Utility_Bill.pdf`, expect:

- Console lines such as `Extracting: Sample_Utility_Bill.pdf` and `Wrote Sample_Utility_Bill.csv`
- A CSV file under the output directory, for example:

```csv
Column1,Column2,...
...row data...
```

Table shape depends on the source PDF; use the sample files in `/examples` at the repo root for predictable demos.

## Tests

```bash
mvn -q -pl examples/batch-folder-processing test
```

The smoke test generates a tiny text PDF in a temporary folder and verifies that batch processing produces a non-empty CSV.
