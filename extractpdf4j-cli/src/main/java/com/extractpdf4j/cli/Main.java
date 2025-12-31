package com.extractpdf4j.cli;

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.BaseParser;
import com.extractpdf4j.parsers.HybridParser;
import com.extractpdf4j.parsers.LatticeParser;
import com.extractpdf4j.parsers.OcrStreamParser;
import com.extractpdf4j.parsers.StreamParser;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Main
 *
 * <p>CLI entry point for ExtractPDF4J. Parses command-line flags, constructs the
 * appropriate parser (stream / lattice / ocrstream / hybrid), runs extraction,
 * and writes CSV output either to STDOUT or to file(s).</p>
 *
 * <h3>Synopsis</h3>
 * <pre>{@code
 * java -jar extractpdf4j-cli-1.0.0.jar <pdf>
 *      [--mode stream|lattice|ocrstream|hybrid]
 *      [--pages 1|all|1,3-5]
 *      [--sep ,]
 *      [--out out.csv]
 *      [--debug]
 *      [--dpi 300]
 *      [--ocr auto|cli|bytedeco]
 *      [--keep-cells]
 *      [--debug-dir <dir>]
 *      [--min-score 0-1]
 *      [--require-headers Date,Description,Balance]
 * }</pre>
 *
 * <h3>Notes</h3>
 * <ul>
 *   <li>When <code>--out</code> is omitted, tables are printed to STDOUT in CSV form.</li>
 *   <li>When multiple tables are found and <code>--out</code> is provided, files are
 *       numbered by suffix (e.g., <code>out-1.csv</code>, <code>out-2.csv</code>).</li>
 *   <li><code>--pages</code> accepts <code>"1"</code>, <code>"2-5"</code>, <code>"1,3-4"</code>, or <code>"all"</code>.</li>
 *   <li><code>--ocr</code> sets a system property read by the OCR helpers; values: <i>auto</i>, <i>cli</i>, or <i>bytedeco</i>.</li>
 * </ul>
 *
 * <p>Exit behavior: this method returns after printing errors/usage; it does not call {@code System.exit}.</p>
 *
 * @author Mehuli Mukherjee
 * @since 2025
 */
@Command(
        name = "extractpdf4j",
        mixinStandardHelpOptions = true,
        caseInsensitiveEnumValues = true,
        description = "Extract tables from PDFs using ExtractPDF4J.")
public class Main implements Runnable {

    @Parameters(index = "0", description = "Path to the PDF file.")
    private String pdf;

    @Option(names = "--mode", description = "Extraction mode: ${COMPLETION-CANDIDATES}.", defaultValue = "HYBRID")
    private Mode mode;

    @Option(names = "--pages", description = "Pages to parse (1, 1,3-5, all).", defaultValue = "all")
    private String pages;

    @Option(names = "--sep", description = "CSV separator.", defaultValue = ",")
    private String sep;

    @Option(names = "--out", description = "Output CSV file path. If omitted, writes to STDOUT.")
    private String out;

    @Option(names = "--debug", description = "Enable debug output.")
    private boolean debug;

    @Option(names = "--dpi", description = "OCR DPI.", defaultValue = "300")
    private float dpi;

    @Option(names = "--ocr", description = "OCR mode: auto, cli, bytedeco.")
    private String ocrMode;

    @Option(names = "--keep-cells", description = "Keep detected cells (lattice/hybrid).")
    private boolean keepCells;

    @Option(names = "--debug-dir", description = "Directory for debug artifacts.")
    private String debugDirPath;

    @Option(names = "--min-score", description = "Minimum hybrid score threshold.", defaultValue = "0")
    private double minScore;

    @Option(names = "--require-headers", description = "Comma-separated headers required for OCR stream.")
    private String requiredHeadersCsv;

    /** Prints CLI usage and examples to STDOUT. */
    private static void usage() {
        CommandLine.usage(new Main(), System.out);
    }

    /**
     * Program entry point.
     *
     * <p>Parses flags, constructs a {@link BaseParser} (or subclass), runs extraction,
     * then writes or prints CSV results. Errors and invalid flags cause usage to be printed
     * and the method to return.</p>
     *
     * @param args command-line arguments (see {@link #usage()} for details)
     * @throws Exception if an unrecoverable I/O error occurs during parsing/writing
     */
    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            usage();
            return;
        }
        new CommandLine(new Main()).execute(args);
    }

    @Override
    public void run() {
        if (ocrMode != null) {
            System.setProperty("ocr", ocrMode);
        }

        File dbgDir = (debugDirPath == null) ? new File("debug") : new File(debugDirPath);
        BaseParser parser;
        switch (mode) {
            case STREAM:
                parser = new StreamParser(pdf);
                break;
            case LATTICE:
                parser = new LatticeParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .keepCells(keepCells)
                        .debugDir(dbgDir);
                break;
            case OCRSTREAM:
                parser = new OcrStreamParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .debugDir(dbgDir)
                        .requiredHeaders(parseHeaders(requiredHeadersCsv));
                break;
            case HYBRID:
            default:
                parser = new HybridParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .keepCells(keepCells)
                        .debugDir(dbgDir)
                        .minScore(minScore);
                break;
        }

        parser.pages(pages);

        List<Table> tables;
        try {
            tables = parser.parse();
        } catch (Exception e) {
            throw new CommandLine.ExecutionException(new CommandLine(this), e.getMessage(), e);
        }

        if (tables.isEmpty()) {
            System.err.println("No tables detected.");
            return;
        }

        if (out == null) {
            int i = 1;
            for (Table table : tables) {
                System.out.println("### Table " + (i++));
                System.out.println(table.toCSV(sep.charAt(0)));
                System.out.println();
            }
        } else {
            writeTables(tables, out, sep.charAt(0));
        }
    }

    private static List<String> parseHeaders(String csv) {
        if (csv == null || csv.isBlank()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(Arrays.asList(csv.toLowerCase().split("\\s*,\\s*")));
    }

    private static void writeTables(List<Table> tables, String outPath, char separator) {
        File outFile = new File(outPath);

        if (tables.size() == 1) {
            try (FileWriter fw = new FileWriter(outFile)) {
                fw.write(tables.get(0).toCSV(separator));
            } catch (Exception e) {
                throw new CommandLine.ExecutionException(new CommandLine(new Main()), e.getMessage(), e);
            }
            System.out.println("Wrote " + outFile.getAbsolutePath());
            return;
        }

        String base = outFile.getName();
        String prefix = base;
        String ext = "";
        int dot = base.lastIndexOf('.');
        if (dot > 0) {
            prefix = base.substring(0, dot);
            ext = base.substring(dot);
        }
        File parent = outFile.getParentFile();
        if (parent == null) {
            parent = new File(".");
        }

        int i = 1;
        for (Table table : tables) {
            File file = new File(parent, prefix + "-" + i + ext);
            try (FileWriter fw = new FileWriter(file)) {
                fw.write(table.toCSV(separator));
            } catch (Exception e) {
                throw new CommandLine.ExecutionException(new CommandLine(new Main()), e.getMessage(), e);
            }
            System.out.println("Wrote " + file.getAbsolutePath());
            i++;
        }
    }

    enum Mode {
        STREAM,
        LATTICE,
        OCRSTREAM,
        HYBRID
    }
}
