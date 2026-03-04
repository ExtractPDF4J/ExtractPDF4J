# Models

This page describes the core output model used by ExtractPDF4J.

The most important model for consumers is:

- `Table`

This is the main structure returned by parser operations.

## `Table`

All parser flows typically return:

```java
List<Table>
```

Each `Table` represents one extracted table from the PDF.

A `Table` is the bridge between extraction and downstream use cases such as:

- CSV export
- data normalization
- validation
- ingestion into applications or services

## Common usage pattern

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class ModelsExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("sample.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        if (!tables.isEmpty()) {
            Table first = tables.get(0);
            System.out.println(first.toCSV(','));
        }
    }
}
```

## CSV export

A common operation on a `Table` is:

```java
table.toCSV(',')
```

This is useful for:

- quick manual review
- writing to files
- testing extraction quality
- feeding CSV-based downstream pipelines

## Example: writing CSV

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.StreamParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class TableCsvExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new StreamParser("statement.pdf")
                .pages("1")
                .parse();

        if (!tables.isEmpty()) {
            Files.writeString(Path.of("statement.csv"), tables.get(0).toCSV(','));
        }
    }
}
```

## Cells and structure

Depending on parser mode and configuration, a `Table` may reflect:

- inferred row/column structure
- explicit cell-grid structure
- OCR-backed text grouping
- lattice-detected cell boundaries

When using options like:

```java
.keepCells(true)
```

the parser may preserve more explicit cell-level structure where supported.

## Downstream normalization

After extraction, it is common to normalize:

- header names
- date formats
- numeric formatting
- empty values
- column ordering

Typical examples:

- mapping `Txn Date` → `Date`
- trimming whitespace
- standardizing currency columns
- dropping noise rows

## Defensive handling

Always handle:

- empty result sets
- multiple tables
- partial or noisy tables

Example:

```java
if (tables.isEmpty()) {
    System.out.println("No tables found.");
} else {
    for (Table table : tables) {
        System.out.println(table.toCSV(','));
    }
}
```

## Related model usage patterns

A `Table` is often used together with:

- parser configuration methods
- CSV file writing
- template validation logic
- downstream mappers and schema normalizers

## Javadocs

For exact type details and method signatures, use:

- [Javadocs](https://extractpdf4j.github.io/ExtractPDF4J/apidocs/index.html)

## Related pages

- [API Overview](overview.md)
- [Extractor](extractor.md)
- [Parsers](parsers.md)
- [Quickstart](../quickstart.md)
