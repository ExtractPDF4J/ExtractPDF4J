# Quickstart

This page shows the fastest way to start extracting tables using different parser modes.

## 1) Hybrid parser (recommended default)

Use this first when you want one entry point that can handle text-based and scanned PDFs.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.util.List;

public class HybridQuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new HybridParser("samples/mixed.pdf")
                .pages("all")
                .dpi(300f)
                .parse();

        if (!tables.isEmpty()) {
            System.out.println(tables.get(0).toCSV(','));
        }
    }
}
```

**Why use it:**

- good default for unknown input
- useful for mixed batches
- reduces parser-selection guesswork

## 2) Stream parser (text-based PDFs)

Use this when the PDF already has a usable text layer.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.StreamParser;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class StreamQuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new StreamParser("samples/statement.pdf")
                .pages("1-3")
                .parse();

        if (!tables.isEmpty()) {
            Files.createDirectories(Path.of("out"));
            Files.writeString(Path.of("out/stream_table.csv"), tables.get(0).toCSV(','));
        }
    }
}
```

**Best for:**

- generated PDFs
- statements
- text-layer reports

## 3) Lattice parser (ruled / grid tables)

Use this when table borders are clearly visible.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.LatticeParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LatticeQuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new LatticeParser("samples/scanned.pdf")
                .dpi(300f)
                .keepCells(true)
                .debug(true)
                .debugDir(new File("out/debug"))
                .pages("all")
                .parse();

        Files.createDirectories(Path.of("out"));

        for (int i = 0; i < tables.size(); i++) {
            Files.writeString(Path.of("out/lattice_table_" + i + ".csv"), tables.get(i).toCSV(','));
        }
    }
}
```

**Best for:**

- boxed tables
- scanned tables with visible lines
- grid-heavy layouts

## 4) OCR-assisted stream parser

Use this when the text layer is weak or missing.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.OcrStreamParser;

import java.util.List;

public class OcrQuickStart {
    public static void main(String[] args) throws Exception {
        List<Table> tables = new OcrStreamParser("samples/scan.pdf")
                .pages("1-2")
                .dpi(300f)
                .parse();

        System.out.println("Tables found: " + tables.size());
    }
}
```

**Best for:**

- scanned statements
- image-heavy PDFs
- OCR recovery cases

## 5) Batch extraction (folder to CSV)

Use this when processing multiple PDFs.

```java
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.HybridParser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class BatchQuickStart {
    public static void main(String[] args) throws Exception {
        Files.createDirectories(Path.of("./out"));

        for (File pdf : new File("./invoices").listFiles(f -> f.getName().endsWith(".pdf"))) {
            List<Table> tables = new HybridParser(pdf.getPath())
                    .dpi(300f)
                    .parse();

            if (!tables.isEmpty()) {
                Files.writeString(
                        Path.of("./out/" + pdf.getName() + ".csv"),
                        tables.get(0).toCSV(',')
                );
            }
        }
    }
}
```

## Tips for better results

- Start with HybridParser
- Use dpi(300f) for scanned PDFs
- Use page ranges to limit noisy pages
- Use debug output when layout detection is unclear
- Normalize headers downstream when documents vary
