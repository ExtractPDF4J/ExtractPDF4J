# CLI

The CLI is useful for automation, one-off conversions, and data pipeline pre-processing.

## Usage

```bash
java -jar extractpdf4j-parser-<version>.jar <pdf> \
  [--mode stream|lattice|ocrstream|hybrid] \
  [--pages 1|all|1,3-5] \
  [--sep ,] \
  [--out out.csv] \
  [--debug] \
  [--dpi 300] \
  [--ocr auto|cli|bytedeco] \
  [--keep-cells] \
  [--debug-dir <dir>] \
  [--min-score 0-1] \
  [--require-headers Date,Description,Balance]
```

## Important options

- `--mode`: parser strategy; default behavior is typically hybrid.
- `--pages`: accepts `"1"`, `"1-3"`, `"1,3-5"`, or `"all"`.
- `--dpi`: commonly `300-450` for scanned documents.
- `--debug` + `--debug-dir`: emit intermediate images for troubleshooting.
- `--ocr`: choose OCR backend (`auto`, `cli`, `bytedeco`).

## Example commands

```bash
java -jar extractpdf4j-parser-<version>.jar scan.pdf --mode lattice --pages 1 --dpi 450 --ocr cli --debug --keep-cells --debug-dir debug_out --out p1.csv
java -jar extractpdf4j-parser-<version>.jar statement.pdf --mode hybrid --pages all --dpi 400 --out tables.csv
```

## Output behavior

- When `--out` is omitted, CSV is printed to stdout.
- With multiple tables, file outputs are suffixed (for example: `out-1.csv`, `out-2.csv`).
