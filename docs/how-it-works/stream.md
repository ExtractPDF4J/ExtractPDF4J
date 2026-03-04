# Stream Parser

`StreamParser` is designed for **text-based PDFs** where the document already contains a usable text layer.

Instead of relying on OCR or visible table borders, it uses the positions of text elements to infer rows and columns.

## When to use StreamParser

Use `StreamParser` when:

- the PDF is digitally generated
- you can highlight or copy text in a PDF viewer
- table borders are absent or inconsistent
- the structure is implied by alignment rather than visible grid lines

Common examples:
- bank statements
- generated financial reports
- system-exported tables
- machine-produced invoices

## How it works

At a high level:

1. Read the PDF text layer
2. Collect text positions from the page
3. Group nearby text into rows
4. Infer column boundaries from alignment and spacing
5. Build table cells
6. Return `List<Table>`

## Strengths

- Fast for clean text PDFs
- No OCR overhead
- Works well on structured exported documents
- Good default for digital reports and statements

## Limitations

`StreamParser` can struggle when:

- rows wrap unpredictably
- columns drift across pages
- sections are visually close but semantically separate
- spacing is inconsistent
- the file is scanned and lacks a real text layer

If that happens, try:
- `HybridParser`
- `LatticeParser` (if lines exist)
- `OcrStreamParser` (if the page is image-based)

## Example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.StreamParser;

import java.util.List;

public class StreamExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new StreamParser("statement.pdf")
                .pages("1-3")
                .parse();

        if (!tables.isEmpty()) {
            System.out.println(tables.get(0).toCSV(','));
        }
    }
}
```

## Best practices

- Use page ranges to focus on the table-bearing pages
- Normalize headers downstream when layouts vary slightly
- Validate extracted columns before relying on production ingestion
- Prefer HybridParser if you are not fully sure the input is text-based

## Good fit vs poor fit

**Good fit**

- consistent row spacing
- stable column alignment
- selectable text
- repeated statement/report layouts

**Poor fit**

- scans
- photographs
- image-only PDFs
- heavy skew or layout noise
- visually ruled tables where line structure is stronger than text alignment
