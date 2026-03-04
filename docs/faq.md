# FAQ

## What kind of PDFs does ExtractPDF4J support?

ExtractPDF4J is designed for:

- text-based PDFs with a usable text layer
- scanned PDFs
- image-heavy PDFs
- ruled tables
- mixed-layout documents

Common examples include:

- invoices
- bank statements
- reports
- forms
- operational documents with tables

## Which parser should I start with?

For most users, start with:

- `HybridParser`

It is the safest general-purpose default when:

- you are unsure what kind of PDF you have
- the input batch is mixed
- you want one practical entry point first

If the document family is well understood later, you can switch to a more specialized parser.

## When should I use StreamParser?

Use `StreamParser` when:

- the PDF is digitally generated
- text is selectable
- the table structure is implied by text alignment

It is usually best for exported reports and text-based statements.

## When should I use LatticeParser?

Use `LatticeParser` when:

- table borders are visible
- rows and columns are clearly ruled
- the layout is grid-based

It is especially useful for boxed invoices and structured scanned forms.

## When should I use OcrStreamParser?

Use `OcrStreamParser` when:

- the document is scanned
- text cannot be selected
- OCR must recover content from an image-based page

If the scan is readable but the text layer is missing, this is a strong option.

## Why is OCR slow?

OCR-backed extraction is heavier because it may involve:

- rendering the page to an image
- running OCR over the image
- reconstructing text structure from OCR output

Speed is affected by:

- DPI
- page count
- scan quality
- OCR engine configuration

To reduce cost:

- narrow page ranges
- avoid unnecessary high DPI
- process only the relevant pages

## What DPI should I use?

Recommended starting point:

- `300f`

For more difficult scans:

- `400f`
- `450f`

Higher DPI may improve recognition, but it also increases CPU and memory usage.

## How do I fix native library errors?

If you are not using bundled platform artifacts, make sure native libraries are available on the system path.

Typical environment paths:

- `LD_LIBRARY_PATH` on Linux
- `DYLD_LIBRARY_PATH` on macOS
- `PATH` on Windows

For OCR, also ensure Tesseract language data is available.

## What is `TESSDATA_PREFIX`?

`TESSDATA_PREFIX` tells Tesseract where its language data files live.

If OCR cannot find language data, set:

```bash
export TESSDATA_PREFIX=/path/to/tessdata
```

Without this, OCR may fail or return poor-quality output.

## How do I debug incorrect extraction?

Use:

- debug mode
- page ranges
- higher DPI for scans
- specialized parser selection
- table areas or column hints where supported

Typical debug pattern:

```java
new LatticeParser("scanned.pdf")
    .pages("1")
    .dpi(300f)
    .debug(true)
    .debugDir(new File("out/debug"))
    .parse();
```

Then inspect the debug artifacts.

## What if no tables are found?

Possible causes include:

- wrong parser choice
- scanned input without OCR support
- weak table borders
- noisy or skewed page images
- incorrect page selection

Try:

1. switching to `HybridParser`
2. increasing DPI
3. narrowing page ranges
4. enabling debug mode
5. testing a specialized parser directly

## Can I use this from the command line?

Yes.

ExtractPDF4J includes CLI support for quick runs, scripting, and batch jobs.

Example:

```bash
java -jar extractpdf4j-parser-<version>.jar input.pdf \
  --mode hybrid \
  --pages all \
  --out output.csv
```

See the [CLI](cli.md) page for more options.

## Can I convert output to CSV?

Yes.

A common pattern is:

```java
table.toCSV(',')
```

You can then:

- print it
- save it to disk
- pass it into downstream workflows

## What if there are multiple tables?

Parsers return:

```java
List<Table>
```

That means a single PDF may produce multiple extracted tables.

You should:

- iterate through all returned tables
- validate which table is relevant
- handle numbered output files when using CLI workflows

## Is this better than a single-parser approach?

For real-world PDFs, yes.

A single extraction strategy often breaks across mixed document types. ExtractPDF4J is designed around multiple strategies because production PDFs vary widely in:

- structure
- scan quality
- borders
- embedded text
- layout consistency

That is why the project supports:

- stream parsing
- lattice parsing
- OCR-backed parsing
- hybrid coordination

## Where do I find exact method signatures?

Use the Javadocs:

- [Javadocs](https://extractpdf4j.github.io/ExtractPDF4J/apidocs/index.html)
