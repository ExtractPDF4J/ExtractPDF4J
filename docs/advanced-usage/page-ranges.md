# Page Ranges

ExtractPDF4J supports flexible page selection so you can target only the pages that matter.

This is useful when:

- only part of the PDF contains tables
- cover pages should be skipped
- appendices add noise
- you want faster, more focused extraction

## Supported formats

### Single page

```java
.pages("1")
```

Extracts only page 1.

### Page range

```java
.pages("1-3")
```

Extracts pages 1 through 3.

### Mixed selection

```java
.pages("1,3-5")
```

Extracts page 1, then pages 3 through 5.

### All pages

```java
.pages("all")
```

Extracts all pages in the document.

## Example: Java API

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class PageRangesExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("statement.pdf")
                .pages("2-4")
                .dpi(300f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Example: CLI

```bash
java -jar extractpdf4j-parser-<version>.jar statement.pdf \
  --mode hybrid \
  --pages 2-4 \
  --out result.csv
```

## When to use page ranges

Use page ranges when:

- the first page is just a cover or summary
- tables start from page 2 onward
- only selected sections contain tabular data
- you want to reduce OCR cost on long scans

## Common patterns

### Skip the first page

```java
.pages("2-all")
```

If your implementation does not support `2-all`, use explicit ranges instead, such as:

```java
.pages("2-10")
```

### Extract two separate regions of the document

```java
.pages("1-2,5-6")
```

Useful when the middle pages are irrelevant.

## Best practices

- Narrow page ranges before enabling heavy OCR
- Keep extraction focused on known table-bearing pages
- Use page targeting before adding complex tuning
- Validate page assumptions when document templates change

## Common mistakes

### Using invalid syntax

Incorrect:

```java
.pages("page 1")
```

Correct:

```java
.pages("1")
```

### Forgetting commas in mixed selections

Incorrect:

```java
.pages("1 3-5")
```

Correct:

```java
.pages("1,3-5")
```

### Over-processing every page

Using `all` on large scanned PDFs can be slower than needed. Prefer specific ranges when you know where tables are.

## Related pages

- [Quickstart](../quickstart.md)
- [CLI](../cli.md)
- [OCR Tuning](ocr-tuning.md)
