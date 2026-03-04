
---

## `docs/how-it-works/lattice.md`

```md
# Lattice Parser

`LatticeParser` is designed for **ruled** or **grid-based** tables where visible lines define the table structure.

This is especially useful for scanned PDFs, forms, or boxed tables where text alignment alone is not reliable.

## When to use LatticeParser

Use `LatticeParser` when:

- the table has visible horizontal and vertical lines
- the document is scanned
- rows and columns are defined by borders
- structure is visually grid-based

Common examples:
- boxed invoices
- forms with table cells
- ruled financial statements
- scanned reports with explicit table lines

## How it works

At a high level:

1. Render the page to an image
2. Detect horizontal and vertical lines
3. Find line intersections (joints)
4. Construct a cell grid
5. Assign text into cells
6. Return `List<Table>`

This is useful when the table is visually obvious, even if the text layer is missing or weak.

## Strengths

- Excellent for ruled tables
- Handles grid-heavy scans well
- Can preserve more explicit table structure
- Useful when text-only parsing fails

## Limitations

`LatticeParser` can struggle when:

- borders are faint or broken
- the document is low-resolution
- lines are skewed or noisy
- the table is implied only by spacing, not borders

In those cases:
- increase DPI
- enable debug output
- try `HybridParser`
- consider `OcrStreamParser` if the text is readable but borders are weak

## Example

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.LatticeParser;

import java.io.File;
import java.util.List;

public class LatticeExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new LatticeParser("scanned.pdf")
                .pages("all")
                .dpi(300f)
                .keepCells(true)
                .debug(true)
                .debugDir(new File("out/debug"))
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Why DPI matters

For scanned documents, resolution strongly affects:

- line detection
- cell boundary accuracy
- OCR text assignment quality

Recommended starting point:

- `300f`

For difficult scans:

- `400f` to `450f`

Higher DPI can improve accuracy, but increases CPU and memory usage.

## Debug mode

Use debug mode when:

- line detection seems wrong
- cells are merging incorrectly
- borders are partially missing
- you want to inspect intermediate output

Typical settings:
```java
.debug(true)
.debugDir(new File("out/debug"))
```

## Good fit vs poor fit

**Good fit**

- strong borders
- clearly ruled tables
- boxed cells
- structured forms

**Poor fit**

- borderless tables
- loosely aligned text-only layouts
- documents where spacing matters more than drawn lines
