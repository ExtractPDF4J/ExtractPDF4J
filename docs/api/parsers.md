# Parsers

ExtractPDF4J provides multiple parser implementations because different PDF layouts require different extraction strategies.

Each parser is optimized for a different kind of document structure, but all aim to return:

```java
List<Table>
```

## Available parsers

### StreamParser

Use `StreamParser` for text-based PDFs where the text layer is present and usable.

Best for:

- digital statements
- generated reports
- exported text-based PDFs

Example:

```java
import com.extractpdf4j.parsers.StreamParser;

new StreamParser("statement.pdf")
    .pages("1-3")
    .parse();
```

### LatticeParser

Use `LatticeParser` for ruled or grid-based tables with visible borders.

Best for:

- boxed invoices
- structured forms
- scanned tables with clear lines

Example:

```java
import com.extractpdf4j.parsers.LatticeParser;

new LatticeParser("scan.pdf")
    .pages("1")
    .dpi(300f)
    .keepCells(true)
    .parse();
```

### OcrStreamParser

Use `OcrStreamParser` when the PDF is scanned or image-heavy and text must be recovered via OCR.

Best for:

- scanned statements
- image-only PDFs
- OCR recovery use cases

Example:

```java
import com.extractpdf4j.parsers.OcrStreamParser;

new OcrStreamParser("scan.pdf")
    .pages("1-2")
    .dpi(400f)
    .parse();
```

### HybridParser

Use `HybridParser` when:

- the document type is uncertain
- the batch is mixed
- you want a strong production default

Best for:

- unknown input
- mixed text/scanned pipelines
- general-purpose extraction

Example:

```java
import com.extractpdf4j.parsers.HybridParser;

new HybridParser("mixed.pdf")
    .pages("all")
    .dpi(300f)
    .parse();
```

## Comparison table

| Parser | Best for | Needs OCR? | Best default? |
|---|---|---:|---:|
| StreamParser | Text-based PDFs | No | No |
| LatticeParser | Ruled/grid tables | Sometimes | No |
| OcrStreamParser | Scanned image-heavy PDFs | Yes | No |
| HybridParser | Mixed/unknown input | As needed | Yes |

## How to choose

### Choose StreamParser when
- text is selectable
- alignment is stable
- OCR is unnecessary

### Choose LatticeParser when
- borders define the table
- line structure is visually strong
- cell grids are clear

### Choose OcrStreamParser when
- there is no reliable text layer
- OCR must recover content
- border structure is weak or secondary

### Choose HybridParser when
- you are unsure
- multiple templates are involved
- you want a safer default route

## Parser tuning examples

### Stream-focused run

```java
new StreamParser("report.pdf")
    .pages("2-4")
    .parse();
```

### Lattice-focused run

```java
new LatticeParser("invoice.pdf")
    .pages("1")
    .dpi(300f)
    .debug(true)
    .keepCells(true)
    .parse();
```

### OCR-focused run

```java
new OcrStreamParser("archive_scan.pdf")
    .pages("all")
    .dpi(400f)
    .parse();
```

### Hybrid default run

```java
new HybridParser("unknown.pdf")
    .pages("all")
    .dpi(300f)
    .parse();
```

## Practical recommendation

For new implementations:

1. start with `HybridParser`
2. inspect output on real samples
3. switch to a specialized parser only when a document family is stable and predictable

## Related pages

- [API Overview](overview.md)
- [Extractor](extractor.md)
- [Models](models.md)
- [How It Works](../how-it-works/overview.md)
