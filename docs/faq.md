# FAQ

## Which parser should I start with?
Use `HybridParser` first for mixed and unknown PDFs.

## Does this work for scanned PDFs?
Yes. Use OCR-backed modes (`OcrStreamParser` or `HybridParser`) and set appropriate DPI.

## Why are tables empty or fragmented?
- Increase DPI (`300–450`) for scans.
- Try another parser mode (`stream` vs `lattice` vs `hybrid`).
- Use debug output to inspect intermediate artifacts.

## Is there a stable API reference?
Use the hosted Javadocs for authoritative method signatures.
