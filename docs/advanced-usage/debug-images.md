# Debug Images

Debug artifacts help inspect line detection, segmentation, and OCR preprocessing.

## CLI example

```bash
java -jar extractpdf4j-parser-<version>.jar input.pdf \
  --mode lattice \
  --debug \
  --debug-dir debug/
```

## Java example

```java
// Placeholder method names where needed.
var parser = new com.extractpdf4j.parsers.LatticeParser("input.pdf")
    .dpi(300f);
// parser.debug(true).debugDir(new java.io.File("debug"));
var tables = parser.parse();
```
