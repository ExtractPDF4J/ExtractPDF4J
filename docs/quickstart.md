# Quickstart

## Hybrid mode (recommended default)

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

## Text-based documents

```java
import com.extractpdf4j.parsers.StreamParser;

var tables = new StreamParser("statement.pdf")
    .pages("1-3")
    .parse();
```

## Scanned or image-heavy documents

```java
import com.extractpdf4j.parsers.OcrStreamParser;

var tables = new OcrStreamParser("scan.pdf")
    .dpi(300f)
    .parse();
```

> If your installed version has different method names, adapt these snippets accordingly.
