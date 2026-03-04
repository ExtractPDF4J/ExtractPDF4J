# API Overview

ExtractPDF4J provides a Java-first API for extracting tabular data from PDFs using multiple parser strategies.

The API is designed to give you:

- a consistent parser construction pattern
- a common `parse()` workflow
- a stable `List<Table>` output model
- flexibility across text-based, scanned, and mixed-layout PDFs

## Core modules

Depending on how you use the project, you may interact with one or more modules:

- `extractpdf4j-parser` — parser implementations and extraction entry points
- `extractpdf4j-core` — shared helpers and core data structures
- `extractpdf4j-cli` — command-line execution layer
- `extractpdf4j-service` — service-oriented integration layer

## Core concepts

### Parser
A parser is the main extraction entry point.

Examples:

- `StreamParser`
- `LatticeParser`
- `OcrStreamParser`
- `HybridParser`

Each parser focuses on a different extraction strategy, but all aim to return the same kind of result.

### Table
A `Table` represents extracted tabular data.

You typically use it to:

- inspect extracted rows and cells
- convert output to CSV
- feed downstream normalization or ingestion logic

### Fluent configuration
Parsers are typically configured using fluent methods such as:

- `.pages(...)`
- `.dpi(...)`
- `.debug(...)`
- `.keepCells(...)`

This makes parser setup concise and readable.

## Typical API flow

A standard Java API usage pattern looks like this:

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class ApiOverviewExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("sample.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## What `parse()` returns

All primary parser flows return:

```java
List<Table>
```

This consistency is important because it means:

- you can swap parser strategies more easily
- downstream code can remain stable
- parser choice does not force a different result model

## Recommended starting point

For most new users:

- start with `HybridParser`
- use `pages("all")` or a narrow page range
- use `dpi(300f)` for scanned or uncertain input
- inspect the first `Table`
- convert to CSV for quick validation

## API reference map

- [Extractor](extractor.md)
- [Parsers](parsers.md)
- [Models](models.md)
- [Javadocs](https://extractpdf4j.github.io/ExtractPDF4J/apidocs/index.html)

## When to use the Java API instead of CLI

Prefer the Java API when you need:

- application integration
- pipeline orchestration
- custom validation logic
- post-processing in code
- fine-grained control over parser behavior

Prefer the CLI when you need:

- quick terminal execution
- debugging outside code
- batch jobs and scripting
- simple CSV generation without embedding the library
