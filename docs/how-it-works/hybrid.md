# Hybrid Parser

`HybridParser` is the most flexible parser mode in ExtractPDF4J.

It is intended as the **best general-purpose default**, especially when you do not know in advance whether a PDF is text-based, scanned, ruled, or mixed.

## When to use HybridParser

Use `HybridParser` when:

- you are unsure which parser is best
- your batch contains mixed PDF types
- some pages are text-based and others are scanned
- you want one practical entry point for automation pipelines

For many production use cases, this is the safest starting point.

## How it works

At a high level, `HybridParser` coordinates multiple parser strategies.

It can:

- choose the most suitable strategy
- combine results from multiple approaches
- return a consistent `List<Table>` output
- reduce the need for manual parser selection

This is useful when:
- document quality varies
- layouts change across files
- input sources are inconsistent

## Why HybridParser is the recommended default

In real systems, PDF inputs are rarely uniform.

You may receive:
- clean exported statements
- partially scanned PDFs
- mixed-layout invoices
- OCR-needed archival files
- files where one strategy works for some pages but not others

`HybridParser` reduces operational guesswork by giving you a stronger default path.

## Example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class HybridExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("mixed.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Strengths

- Best default for unknown inputs
- Useful for mixed text/scanned batches
- Reduces parser selection effort
- Good for production ingestion pipelines

## Limitations

HybridParser is broad and practical, but it is not magic.

For highly specialized documents, a direct parser may still be better:

- use StreamParser for clearly text-based PDFs
- use LatticeParser for strongly ruled tables
- use OcrStreamParser for OCR-first recovery

If you already know the exact document type, a specialized parser may be more predictable.

## Recommended workflow

**For first-time users**

Start with `HybridParser`.

**For debugging**

Once you understand the document better:
- switch to a specialized parser if needed
- compare outputs
- keep the better strategy for that document family

**For production**

Use:

- HybridParser as the default route
- targeted overrides only when specific document classes need them

## Good fit vs poor fit

**Good fit**

- unknown PDFs

- varied input sources

- mixed batches

- automation pipelines

**Less ideal when**

- the document type is fully known and stable
- you want tightly specialized extraction behavior
- you are tuning one narrow layout family
