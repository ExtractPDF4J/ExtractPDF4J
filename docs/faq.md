# FAQ

## Which parser should I start with?
Start with `HybridParser` for unknown/mixed inputs, then specialize if needed.

## Does this work for scanned PDFs?
Yes. Use `OcrStreamParser` or `HybridParser`, and raise DPI (often `300-450`).

## Why are extracted tables fragmented?
Try the following:

1. Increase DPI for scanned sources.
2. Switch parser mode (`stream`, `lattice`, `ocrstream`, `hybrid`).
3. Enable debug artifacts and inspect segmentation outputs.
4. Limit pages to avoid noisy sections (covers/appendices).

## Is this only for CSV export?
No. CSV is convenient, but parser results are available as in-memory table models for application-level transformation.

## Where are exact method signatures documented?
Use the project Javadocs as the source of truth for your release version.
