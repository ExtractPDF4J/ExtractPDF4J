# Quickstart

## 1) Recommended default: Hybrid parser

Use hybrid mode when document type is unknown or mixed (text + scans).

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.util.List;

List<Table> tables = new HybridParser("invoices.pdf")
    .pages("all")
    .dpi(300f)
    .parse();

if (!tables.isEmpty()) {
  System.out.println(tables.get(0).toCSV(','));
}
```

## 2) Text-based PDFs: Stream parser

```java
import com.extractpdf4j.parsers.StreamParser;

var tables = new StreamParser("statement.pdf")
    .pages("1-3")
    .parse();
```

## 3) Scanned/image-heavy PDFs: OCR stream parser

```java
import com.extractpdf4j.parsers.OcrStreamParser;

var tables = new OcrStreamParser("scan.pdf")
    .dpi(300f)
    .pages("1-2")
    .parse();
```

## 4) Batch extraction example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

for (File pdf : new File("./invoices").listFiles(f -> f.getName().endsWith(".pdf"))) {
  List<Table> tables = new HybridParser(pdf.getPath()).dpi(300f).parse();
  if (!tables.isEmpty()) {
    Files.writeString(Path.of("./out/" + pdf.getName() + ".csv"), tables.get(0).toCSV(','));
  }
}
```

> If your installed version has different method names, adapt snippets to your release Javadocs.
