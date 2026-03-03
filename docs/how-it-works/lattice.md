# Lattice Parser

`LatticeParser` is designed for ruled tables where visible lines define cell boundaries.

## Best for

- Forms and invoices with grid lines
- Scanned PDFs with strong table structure

## Java example

```java
import com.extractpdf4j.parsers.LatticeParser;

var tables = new LatticeParser("invoice_scan.pdf")
    .dpi(300f)
    .parse();
```

## Notes

- Higher DPI can improve line detection quality.
- Debug images are useful for tuning thresholds and cell segmentation.
