# OCR Stream Parser

`OcrStreamParser` is designed for **scanned** or **image-heavy PDFs** where the text layer is missing, weak, or unusable.

It uses OCR to recover text and then applies stream-style interpretation to build tabular output.

## When to use OcrStreamParser

Use `OcrStreamParser` when:

- the PDF is a scan
- text cannot be selected in the PDF viewer
- the text layer is corrupt or incomplete
- OCR can recover readable content from the page image

Common examples:
- scanned invoices
- scanned bank statements
- photographed documents
- image-heavy archival PDFs

## How it works

At a high level:

1. Render the page to an image
2. Run OCR on the rendered content
3. Recover text blocks and positions
4. Group text into row/column-like structure
5. Build tables from OCR output
6. Return `List<Table>`

This gives you a way to extract tables even when the original PDF has no usable embedded text.

## Strengths

- Works on image-only PDFs
- Useful for legacy scans
- Helps recover structure where no text layer exists
- Good fallback for OCR-readable documents

## Limitations

`OcrStreamParser` depends on OCR quality.

It can degrade when:
- the scan is blurry
- the page is skewed
- text is faint or noisy
- the language data is missing
- resolution is too low

If visible table borders are strong, `LatticeParser` may perform better.

## Example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.OcrStreamParser;

import java.util.List;

public class OcrStreamExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new OcrStreamParser("scan.pdf")
                .pages("1-2")
                .dpi(300f)
                .parse();

        if (!tables.isEmpty()) {
            System.out.println(tables.get(0).toCSV(','));
        }
    }
}
```

## OCR setup notes

For OCR-backed parsing, you may need:

- Tesseract language data
- `TESSDATA_PREFIX` if language data is not automatically found

Example:
```bash
export TESSDATA_PREFIX=/path/to/tessdata
```
## DPI guidance

OCR quality often improves when you render at a better DPI.

Recommended starting point:

- `300f`

For low-quality scans:

- `400f` to `450f`

Trade-off:

- higher DPI may improve recognition
- but increases processing cost

## When OCR Stream is a better choice than Lattice

Choose OcrStreamParser over LatticeParser when:

- text is readable after OCR
- borders are weak or absent
- layout is not strongly ruled
- you need text-first recovery rather than line-first reconstruction
