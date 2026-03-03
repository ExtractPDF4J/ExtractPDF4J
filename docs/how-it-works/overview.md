# How It Works

ExtractPDF4J applies different extraction strategies and returns normalized `Table` outputs.

## Conceptual architecture

```text
PDF (text-based) -> Stream parsing (text coordinates) ----\
                                                           > normalized table model -> CSV/in-memory processing
PDF (scanned/image-heavy) -> image processing + OCR -----/
```

## Strategy summary

- **Stream**: uses text positioning from PDF text layers.
- **Lattice**: detects table structure from lines/grids in rendered page images.
- **OCR Stream**: OCR-first extraction for pages with missing text layer.
- **Hybrid**: combines modes to improve resilience across diverse page types.

## Core parser flow

1. Load PDF and select pages.
2. Apply chosen strategy.
3. Build table cells/rows.
4. Return `List<Table>` for export or mapping.

See mode-specific pages for tuning guidance.
