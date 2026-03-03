# CLI

ExtractPDF4J supports command-line extraction for scripts and pipelines.

## Basic usage

```bash
java -jar extractpdf4j-parser-<version>.jar input.pdf --out tables.csv
```

## Common options

- `--mode stream|lattice|ocrstream|hybrid`
- `--pages 1|all|1,3-5`
- `--out output.csv`
- `--sep ,`
- `--dpi 300`
- `--debug`
- `--debug-dir debug/`
- `--ocr auto|cli|bytedeco`

## Example commands

```bash
java -jar extractpdf4j-parser-<version>.jar scan.pdf --mode hybrid --dpi 400 --pages all --out scan.csv
java -jar extractpdf4j-parser-<version>.jar statement.pdf --mode stream --pages 1-2 --out p1-2.csv
```

When `--out` is omitted, output is printed to stdout.
