# OCR Stream Parser

`OcrStreamParser` applies OCR before stream-like grouping.

## Best for

- Scanned/image-heavy PDFs
- Documents with no usable text layer

## Java example

```java
import com.extractpdf4j.parsers.OcrStreamParser;

var tables = new OcrStreamParser("receipt_scan.pdf")
    .dpi(350f)
    .parse();
```

## Notes

- OCR quality strongly depends on scan quality and DPI.
- Expect higher runtime cost than text-only stream parsing.
