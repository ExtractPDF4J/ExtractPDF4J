# Debug Images

Debug output helps you understand what the parser is seeing internally.

This is especially useful when:

- tables are not detected
- rows merge incorrectly
- columns shift
- cell boundaries look wrong
- OCR or line detection seems inconsistent

## Why debug mode matters

PDF extraction is often affected by visual and structural issues that are hard to understand from CSV output alone.

Debug artifacts can help you inspect:

- detected lines
- inferred cell regions
- OCR-backed layouts
- cropped parsing regions
- parser-specific intermediate output

## Typical Java API usage

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.LatticeParser;

import java.io.File;
import java.util.List;

public class DebugImagesExample {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new LatticeParser("scanned.pdf")
                .pages("1")
                .dpi(300f)
                .debug(true)
                .debugDir(new File("out/debug"))
                .keepCells(true)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

## Typical CLI usage

```bash
java -jar extractpdf4j-parser-<version>.jar scanned.pdf \
  --mode lattice \
  --pages 1 \
  --dpi 300 \
  --debug \
  --debug-dir out/debug \
  --out page1.csv
```

## What debug output can show

Depending on parser mode, debug output may include:

- rendered page images
- detected horizontal and vertical lines
- intersection points
- inferred table boundaries
- intermediate overlays
- OCR-linked visual diagnostics

## When to enable debug mode

Enable debug mode when:

- `LatticeParser` misses a table
- ruled lines are detected incorrectly
- cells appear merged or fragmented
- OCR-backed extraction is unstable
- you are tuning a new document template

## Best workflow for troubleshooting

### Step 1: Narrow scope
Use a single page first.

```java
.pages("1")
```

### Step 2: Enable debug
Turn on debug output and write artifacts to a known folder.

### Step 3: Inspect intermediate images
Check whether the parser sees:

- the correct table boundaries
- usable lines
- sensible regions

### Step 4: Adjust settings
Then refine:

- DPI
- parser type
- page range
- table area
- column hints

## Common examples

### Table lines not detected
Try:

- higher DPI
- `LatticeParser`
- checking whether the source scan is too faint

### Text extraction unstable
Try:

- `OcrStreamParser`
- better OCR tuning
- tighter page ranges

### Wrong content included
Try:

- table areas
- excluding cover pages
- narrowing the region of interest

## Good production practice

Use debug mode heavily during:

- parser evaluation
- template onboarding
- support investigations
- extraction tuning

Then disable it in normal production flows to avoid unnecessary storage and overhead.

## Related pages

- [Lattice Parser](../how-it-works/lattice.md)
- [OCR Tuning](ocr-tuning.md)
- [Table Areas](table-areas.md)
- [CLI](../cli.md)
