# Extractor

This page covers the common parser usage pattern in ExtractPDF4J.

Although there are multiple parser implementations, they are typically used in a similar way:

1. create a parser instance
2. configure extraction options
3. call `parse()`
4. work with the returned `List<Table>`

## Common construction pattern

A parser is generally created with a PDF path:

```java
new HybridParser("input.pdf")
```

Then you optionally add configuration:

```java
new HybridParser("input.pdf")
    .pages("1-3")
    .dpi(300f)
```

Then you call:

```java
.parse()
```

## Full example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class ExtractorExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("statement.pdf")
                .pages("1-3")
                .dpi(300f)
                .parse();

        for (Table table : tables) {
            System.out.println(table.toCSV(','));
        }
    }
}
```

## Common fluent methods

Depending on parser type, you may use methods like:

### `pages(...)`

```java
.pages("1")
.pages("1-3")
.pages("1,3-5")
.pages("all")
```

Used to restrict extraction to specific pages.

### `dpi(...)`

```java
.dpi(300f)
.dpi(400f)
```

Used mainly for scanned/image-based parsing.

### `debug(...)`

```java
.debug(true)
```

Enables debug output.

### `debugDir(...)`

```java
.debugDir(new File("out/debug"))
```

Controls where debug artifacts are written.

### `keepCells(...)`

```java
.keepCells(true)
```

Useful when preserving explicit cell structure is important.

## CSV conversion

Once a `Table` is returned, a common next step is converting it to CSV:

```java
table.toCSV(',')
```

This is useful for:

- quick inspection
- local validation
- downstream file generation
- pipeline handoff

## Writing CSV to disk

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.StreamParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class CsvWriteExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new StreamParser("report.pdf")
                .pages("1")
                .parse();

        if (!tables.isEmpty()) {
            Files.writeString(Path.of("out.csv"), tables.get(0).toCSV(','));
        }
    }
}
```

## Error handling guidance

In production usage, you should handle cases such as:

- no tables found
- scanned input requiring OCR
- weak or malformed layouts
- unexpected document template changes

A simple defensive pattern:

```java
if (tables.isEmpty()) {
    System.out.println("No tables found.");
}
```

## Recommended usage strategy

### First pass
Use `HybridParser` to get a baseline.

### Stabilization phase
If a document family is well understood, switch to a more specialized parser if needed.

### Production phase
Validate:

- expected table count
- required headers
- column count consistency
- output quality before ingestion

## Related pages

- [API Overview](overview.md)
- [Parsers](parsers.md)
- [Models](models.md)
- [Quickstart](../quickstart.md)
