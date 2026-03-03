# How It Works

ExtractPDF4J provides multiple extraction paths and lets you choose by document type.

- **Stream**: text-position analysis from PDF text layers.
- **Lattice**: image/grid-based cell detection.
- **OCR Stream**: OCR text recovery + table grouping.
- **Hybrid**: combines strategies for robust extraction.

Architecture (conceptual):

```text
PDF -> parser strategy -> normalized table model -> CSV/JSON/in-memory data
```

Use strategy-specific pages in this section to pick and tune parser behavior.
