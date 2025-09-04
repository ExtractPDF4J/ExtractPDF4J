package com.extractpdf4j;

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.parsers.BaseParser;
import com.extractpdf4j.parsers.HybridParser;
import com.extractpdf4j.parsers.LatticeParser;
import com.extractpdf4j.parsers.OcrStreamParser;
import com.extractpdf4j.parsers.StreamParser;
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
 * java -jar extractpdf4j-hybrid-0.2.0.jar <pdf>
 *      [--mode stream|lattice|ocrstream|hybrid]
 *      [--pages 1|all|1,3-5]
 *      [--sep ,]
 *      [--out out.csv]
 *      [--debug]
 *      [--dpi 300]
 *      [--ocr auto|cli|bytedeco]
 *      [--keep-cells]
 *      [--debug-dir <dir>]
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
public class Main {

    /** Prints CLI usage and examples to STDOUT. */
    private static void usage() {
        System.out.println("Usage: java -jar extractpdf4j-parser-0.1.1.jar <pdf> " +
                "[--mode stream|lattice|ocrstream|hybrid] " +
                "[--pages 1|all|1,3-5] [--sep ,] [--out out.csv] " +
                "[--debug] [--dpi 300] [--ocr auto|cli|bytedeco] [--keep-cells] [--debug-dir <dir>]");
        System.out.println("Examples:");
        System.out.println("  java -jar extractpdf4j-parser-0.1.1.jar scan.pdf --mode lattice --pages 1 --dpi 450 --ocr cli --debug --keep-cells --debug-dir debug_out --out p1.csv");
        System.out.println("  java -jar extractpdf4j-parser-0.1.1.jar statement.pdf --mode hybrid --pages all --dpi 400 --out tables.csv");
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
        if (args.length == 0) { usage(); return; }

        // Positional and optional arguments with defaults
        String pdf = args[0];
        String mode = "hybrid";
        String pages = "all";
        String sep = ",";
        String out = null;
        boolean debug = false;
        boolean keepCells = false;
        float dpi = 300f;
        String ocrMode = null;
        String debugDirPath = null;
        double minScore = 0.0;
        List<String> requiredHeaders = new ArrayList<>();

        // Parse flags (simple linear scan)
        for (int i = 1; i < args.length; i++) {
            switch (args[i]) {
                case "--mode":            mode = args[++i]; break;
                case "--pages":           pages = args[++i]; break;
                case "--sep":             sep = args[++i]; break;
                case "--out":             out = args[++i]; break;
                case "--debug":           debug = true; break;
                case "--dpi":             dpi = Float.parseFloat(args[++i]); break;
                case "--ocr":             ocrMode = args[++i]; break;
                case "--keep-cells":      keepCells = true; break;
                case "--debug-dir":       debugDirPath = args[++i]; break;
                case "--min-score":       minScore = Double.parseDouble(args[++i]); break;
                case "--require-headers": requiredHeaders = new ArrayList<>(Arrays.asList(args[++i].toLowerCase().split("\\s*,\\s*"))); break;
                case "--help":            usage(); return;
                default:
                    System.err.println("Unknown arg: " + args[i]);
                    usage();
                    return;
            }
        }

        // Configure OCR selection for downstream utilities (read by Ocr helpers).
        // Accepted values: "auto", "cli", "bytedeco".
        if (ocrMode != null) {
            System.setProperty("ocr", ocrMode);
        }

        // Debug directory (used by lattice/ocr flows when --debug is enabled)
        File dbgDir = (debugDirPath == null) ? new File("debug") : new File(debugDirPath);

        // Construct the appropriate parser based on --mode.
        BaseParser parser;
        switch (mode.toLowerCase()) {
            case "stream":
                parser = new StreamParser(pdf);
                break;
            case "lattice":
                parser = new LatticeParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .keepCells(keepCells)
                        .debugDir(dbgDir);
                break;
            case "ocrstream":
                parser = new OcrStreamParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .debugDir(dbgDir)
                        .requiredHeaders(requiredHeaders);
                break;
            case "hybrid":
            default:
                parser = new HybridParser(pdf)
                        .dpi(dpi)
                        .debug(debug)
                        .keepCells(keepCells)
                        .debugDir(dbgDir)
                        .minScore(minScore);
                break;
        }

        // Page selection is handled by BaseParser; see README for accepted formats.
        parser.pages(pages);

        // Run extraction
        List<Table> tables = parser.parse();
        if (tables.isEmpty()) {
            System.err.println("No tables detected.");
            return;
        }

        // Output handling: print to STDOUT if --out is omitted; otherwise write file(s).
        if (out == null) {
            int i = 1;
            for (Table t : tables) {
                System.out.println("### Table " + (i++));
                System.out.println(t.toCSV(sep.charAt(0)));
                System.out.println();
            }
        } else {
            File outFile = new File(out);

            if (tables.size() == 1) {
                // Single table → write exactly to the chosen path
                try (FileWriter fw = new FileWriter(outFile)) {
                    fw.write(tables.get(0).toCSV(sep.charAt(0)));
                }
                System.out.println("Wrote " + outFile.getAbsolutePath());
            } else {
                // Multiple tables → split into numbered files next to the chosen path
                String base = outFile.getName();
                String prefix = base, ext = "";
                int dot = base.lastIndexOf('.');
                if (dot > 0) { prefix = base.substring(0, dot); ext = base.substring(dot); }
                File parent = outFile.getParentFile();
                if (parent == null) parent = new File(".");

                int i = 1;
                for (Table t : tables) {
                    File f = new File(parent, prefix + "-" + i + ext);
                    try (FileWriter fw = new FileWriter(f)) {
                        fw.write(t.toCSV(sep.charAt(0)));
                    }
                    System.out.println("Wrote " + f.getAbsolutePath());
                    i++;
                }
            }
        }
    }
}
