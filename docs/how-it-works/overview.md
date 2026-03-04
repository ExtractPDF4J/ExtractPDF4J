# How It Works

ExtractPDF4J supports multiple extraction strategies because no single parser works well for every PDF layout.

Some PDFs contain a clean text layer. Others are scanned images. Some have clear ruled tables. Others are visually tabular but structurally inconsistent.

That is why ExtractPDF4J provides multiple parser modes and a coordinating hybrid strategy.

## High-level pipeline

```text
PDF (text-based) ──► PDFBox text positions ─┐
                                            ├─► StreamParser ──► Table (cells)
PDF (scanned)    ──► Render to image ───────┼─► LatticeParser ─► Table
                                            └─► OCR ───────────► OcrStreamParser

HybridParser ──► chooses / coordinates strategies and returns List<Table>
```

# Core parser roles

## BaseParser

`BaseParser` provides the shared workflow used by all concrete parsers.

It typically manages:

- file path input
- page selection
- parser configuration
- the common `parse()` pipeline
- returning `List<Table>`

## StreamParser

Uses PDF text positions to infer tabular structure from text layout.

**Best when:**

- the PDF has a real text layer
- rows and columns can be inferred from alignment
- no OCR is needed
- LatticeParser

Works by detecting visible table lines and constructing a grid.

**Best when:**

- table borders are drawn
- the document is scanned
- a structured grid is visually present

## OcrStreamParser

Uses OCR to recover text from image-based documents, then applies table-oriented interpretation.

**Best when:**

- the document has no usable text layer
- OCR can recover readable text
- you still want row/column-style extraction

## HybridParser

Combines or coordinates parser strategies.

**Best when:**

- the document type is mixed
- you want a strong default
- production input varies across files

**Why multiple parsers matter**

Real-world PDFs vary by:

- source system
- scan quality
- border visibility
- text encoding
- page complexity
- OCR recoverability

A single extraction strategy often fails across a mixed batch. Multiple parser modes improve practical reliability.

## Output model

All parsers return:

```java
List<Table>
```

This gives you a consistent downstream contract even when the underlying extraction strategy differs.

## Typical decision flow

**Use StreamParser when**

- the PDF is digitally generated
- you can select text in a PDF viewer
- the layout is text-aligned

**Use LatticeParser when**

- cell borders are visible
- the table is ruled
- line-based structure is strong

**Use OcrStreamParser when**

- the PDF is scanned
- the text layer is missing or unusable
- OCR recovery is required

**Use HybridParser when**

- you are unsure
- the batch contains mixed document types
- you want the safest general-purpose default

## Related pages

[Stream Parser](stream.md)

[Lattice Parser](lattice.md)

[OCR Stream Parser](ocr-stream.md)

[Hybrid Parser](hybrid.md)

[Quickstart](../quickstart.md)

[CLI](../cli.md)
