# Experimental ML fallback

ExtractPDF4J can integrate a local machine-learning table extractor as an optional fallback without adding ML dependencies to the core module.

The existing Stream, Lattice, and OCR strategies always run first. ML is invoked only when those strategies return no tables.

```java
MlTableExtractor extractor = new MyOnnxTableExtractor(modelPath);

List<Table> tables = new HybridMlParser("statement.pdf", extractor)
    .enableMlFallback(true)
    .failOnMlError(false)
    .dpi(300f)
    .pages("all")
    .parse();
```

`MlTableExtractor` is a lightweight SPI in `extractpdf4j-core`. Implementations should live in an optional module and may use ONNX Runtime, DJL, or another local inference engine.

## Expected implementation responsibilities

An ML extractor should:

1. render the requested pages to images;
2. detect table regions, rows, columns, headers, and spanning cells;
3. reuse PDFBox or OCR word coordinates for text;
4. assign words to predicted cells;
5. return normal ExtractPDF4J `Table` objects;
6. close model sessions and tensors safely.

## Fallback behaviour

By default, model-loading or inference failures do not fail the complete extraction. Set `failOnMlError(true)` when strict behaviour is required.

ML is intentionally opt-in because model inference adds memory, latency, native dependencies, and model-distribution considerations. No document data needs to leave the local process.
