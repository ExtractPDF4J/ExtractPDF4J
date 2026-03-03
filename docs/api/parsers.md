# Parsers

## StreamParser
Text-layer extraction for digital PDFs.

## LatticeParser
Line/grid-driven extraction for ruled tables.

## OcrStreamParser
OCR-backed extraction for image-heavy scans.

## HybridParser
Combined strategy for robust general-purpose extraction.

## Example chooser

```java
String mode = "hybrid"; // stream | lattice | ocrstream | hybrid
var parser = switch (mode) {
  case "stream" -> new com.extractpdf4j.parsers.StreamParser("input.pdf");
  case "lattice" -> new com.extractpdf4j.parsers.LatticeParser("input.pdf");
  case "ocrstream" -> new com.extractpdf4j.parsers.OcrStreamParser("input.pdf");
  default -> new com.extractpdf4j.parsers.HybridParser("input.pdf");
};
var tables = parser.parse();
```
