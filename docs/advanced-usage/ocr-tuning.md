# OCR Tuning

OCR tuning is critical when working with scanned or image-heavy PDFs.

If text is not embedded in the PDF, extraction quality often depends on:

- render resolution
- OCR engine quality
- language data availability
- scan clarity
- page noise and skew

## When OCR tuning matters

Focus on OCR tuning when:

- text is missing or unselectable
- scanned pages produce weak output
- rows are incomplete
- headers are unreadable
- numeric values are misrecognized

## DPI guidance

A major OCR quality factor is render DPI.

### Recommended starting point

```java
.dpi(300f)
```

This is a strong general default for scanned PDFs.

### For difficult scans

```java
.dpi(400f)
.dpi(450f)
```

Higher DPI can improve:

- character sharpness
- OCR recognition
- line detection support in some workflows

### Trade-off

Higher DPI also increases:

- memory usage
- CPU usage
- processing time

## Example: Java API

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.OcrStreamParser;

import java.util.List;

public class OcrTuningExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new OcrStreamParser("scan.pdf")
                .pages("1-2")
                .dpi(400f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Example: CLI

```bash
java -jar extractpdf4j-parser-<version>.jar scan.pdf \
  --mode ocrstream \
  --pages 1-2 \
  --dpi 400 \
  --out scan.csv
```

## OCR provider selection

If your CLI supports OCR provider selection, you may see options such as:

```bash
--ocr auto|cli|bytedeco
```

### Typical meaning

- `auto` → let the tool choose
- `cli` → use installed OCR tooling
- `bytedeco` → use Java-friendly native bindings if configured

Use the mode that best matches your environment.

## Tesseract language data

If OCR language data is not found automatically, set:

```bash
export TESSDATA_PREFIX=/path/to/tessdata
```

On Windows, set this in environment variables.

Without this, OCR may fail or return poor results.

## Practical OCR tips

- Use the cleanest source PDF available
- Prefer higher-resolution scans where possible
- Avoid repeated rescans or compressed photocopies
- Restrict page ranges to only the relevant pages
- Use `HybridParser` if you are not sure whether OCR is needed

## Common symptoms and likely fixes

### OCR returns very little text
- increase DPI
- verify OCR engine is installed correctly
- check `TESSDATA_PREFIX`

### Numbers are misread
- increase DPI
- test a cleaner scan
- reduce noisy pages with page ranges

### Headers are unreadable
- check whether the scan is skewed or blurred
- try a different parser if visible lines are strong

### OCR is slow
- reduce page count
- avoid unnecessary high DPI on easy pages
- restrict to relevant regions if supported

## Choosing OCR Stream vs Hybrid vs Lattice

### Use OcrStreamParser when
- text must be recovered from images
- border structure is weak

### Use LatticeParser when
- table lines are strong and visible

### Use HybridParser when
- you want a safe default across varied inputs

## Related pages

- [OCR Stream Parser](../how-it-works/ocr-stream.md)
- [Lattice Parser](../how-it-works/lattice.md)
- [Page Ranges](page-ranges.md)
