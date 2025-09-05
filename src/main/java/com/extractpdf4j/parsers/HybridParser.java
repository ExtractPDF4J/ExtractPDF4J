package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * HybridParser
 *
 * <p>A high-level parser that tries multiple underlying strategies and returns
 * the best table set for the requested page(s). Specifically, it runs:
 * <ul>
 *   <li>{@link StreamParser} — text-position based parsing (good for digitally created PDFs)</li>
 *   <li>{@link LatticeParser} — grid/line detection using OpenCV (good for ruled or scanned PDFs)</li>
 *   <li>{@link OcrStreamParser} — OCR-backed stream parsing (good for image PDFs without text layer)</li>
 * </ul>
 * and chooses the result using a simple heuristic scoring function (see {@link #score(Table)}).</p>
 *
 * <h3>Usage</h3>
 * <pre>{@code
 * List<Table> tables = new HybridParser("path/to/file.pdf")
 *     .dpi(300f)       // optional, helps scans
 *     .debug(true)     // optional, write lattice/ocr debug artifacts
 *     .pages("all")    // "1", "2-5", "1,3-4", or "all"
 *     .parse();
 * }</pre>
 *
 * <h3>Page selection contract</h3>
 * <p>Inherits the {@link BaseParser} convention: if {@link #parsePage(int)} is invoked with
 * {@code -1}, the implementation must parse <strong>all</strong> pages. For any non-negative
 * value, only that page is parsed. This class narrows its internal subparsers accordingly.</p>
 *
 * <h3>Thread-safety</h3>
 * <p>Instances are not inherently thread-safe. Create one instance per input file or perform
 * external synchronization if sharing across threads.</p>
 *
 * @author Mehuli Mukherjee
 * @since 2025
 */
public class HybridParser extends BaseParser {
    /** Underlying text-position parser (digitally created PDFs). */
    private final StreamParser stream;
    /** Underlying grid/line parser (ruled or scanned PDFs). */
    private final LatticeParser lattice;
    /** Underlying OCR-enabled parser (image PDFs without text layer). */
    private final OcrStreamParser ocrstream;
    /** Minimum allowed score for a table (default: 0.0) */
    private double minScore = 0.0;

    /**
     * Creates a {@code HybridParser} for the given PDF file path.
     *
     * @param filepath path to the PDF file
     */
    public HybridParser(String filepath){
        super(filepath);
        this.stream    = new StreamParser(filepath);
        this.lattice   = new LatticeParser(filepath);
        this.ocrstream = new OcrStreamParser(filepath);
    }

    // ---------------------------------------------------------------------
    // Configuration passthrough (chainable)
    // ---------------------------------------------------------------------

    /**
     * Sets DPI for image-based parsing (used by lattice + OCR strategies).
     * @param dpi dots per inch used for rasterization (e.g., 300f for scans)
     * @return this parser
     */
    public HybridParser dpi(float dpi){
        lattice.dpi(dpi);
        ocrstream.dpi(dpi);
        return this;
    }

    /**
     * Enables or disables debug outputs for lattice/OCR strategies.
     * @param on {@code true} to enable, {@code false} to disable
     * @return this parser
     */
    public HybridParser debug(boolean on){
        lattice.debug(on);
        ocrstream.debug(on);
        return this;
    }

    /**
     * Whether to preserve empty cells when reconstructing grids (lattice only).
     * @param on {@code true} to keep empty cells
     * @return this parser
     */
    public HybridParser keepCells(boolean on){
        lattice.keepCells(on);
        return this;
    }

    /**
     * Directory where debug artifacts should be written (lattice + OCR).
     * @param dir destination directory
     * @return this parser
     */
    public HybridParser debugDir(File dir){
        lattice.debugDir(dir);
        ocrstream.debugDir(dir);
        return this;
    }

    /**
     * Sets the minimum allowed average score across a list of tables. If the
     * list's average score is below this threshold, it will be rejected.
     * @param score minimal score in [0, 1]
     * @return this parser
     */
    public HybridParser minScore(double score){
        this.minScore = score;
        return this;
    }

    /**
     * Sets the page selection for this parser and propagates the same selection
     * to all underlying strategies.
     *
     * @param pages page selection string (e.g., {@code "all"}, {@code "1"}, {@code "2-5"}, {@code "1,3-4"})
     * @return this parser
     */
    @Override
    public BaseParser pages(String pages){
        super.pages(pages);
        stream.pages(pages);
        lattice.pages(pages);
        ocrstream.pages(pages);
        return this;
    }

    // ---------------------------------------------------------------------
    // Parsing
    // ---------------------------------------------------------------------

    /**
     * Runs stream, lattice, and OCR-backed stream for the requested page(s) and
     * returns the best-scoring set of tables.
     *
     * <p>If {@code page == -1}, each strategy is run across <em>all pages</em>.
     * Otherwise, each strategy is temporarily narrowed to the single requested
     * page (restoring the original page spec afterward).</p>
     *
     * @param page page index to parse, or {@code -1} to parse all pages
     * @return the winning list of {@link Table} objects (possibly empty)
     * @throws IOException if an underlying parser fails
     */
    @Override
    protected List<Table> parsePage(int page) throws IOException {
        // Handle "all pages" case: run each strategy across the entire document.
        if (page == -1) {
            List<Table> a = stream.parse();
            List<Table> b = lattice.parse();
            List<Table> c = ocrstream.parse();

            if (a.isEmpty() && b.isEmpty() && c.isEmpty()) {
                return Collections.emptyList();
            }
            double sa = scoreAll(a), sb = scoreAll(b), sc = scoreAll(c);
            double avgScore = Math.max(sa, Math.max(sb, sc));
            if (avgScore < minScore){
                System.err.printf("Average score of tables (%f) is lower than minimum allowed(%f)", avgScore, minScore);
                System.exit(1);
            }
            return (sb >= sa && sb >= sc) ? b : (sc >= sa && sc >= sb) ? c : a;
        }

        // Narrow subparsers to a single page, run them, then restore the original spec.
        String prev = this.pages;
        try {
            String one = String.valueOf(page);
            stream.pages(one);
            lattice.pages(one);
            ocrstream.pages(one);

            List<Table> a = stream.parse();
            List<Table> b = lattice.parse();
            List<Table> c = ocrstream.parse();

            if (a.isEmpty() && b.isEmpty() && c.isEmpty()) {
                return Collections.emptyList();
            }
            double sa = scoreAll(a), sb = scoreAll(b), sc = scoreAll(c);
            double avgScore = Math.max(sa, Math.max(sb, sc));
            if (avgScore < minScore){
                System.err.printf("Average score of tables (%f) is lower than minimum allowed(%f)", avgScore, minScore);
            }
            return (sb >= sa && sb >= sc) ? b : (sc >= sa && sc >= sb) ? c : a;
        } finally {
            // Always restore original page specification on subparsers.
            stream.pages(prev);
            lattice.pages(prev);
            ocrstream.pages(prev);
        }
    }

    // ---------------------------------------------------------------------
    // Scoring (heuristic)
    // ---------------------------------------------------------------------

    /**
     * Computes the average score of a list of tables.
     * @param ts tables to score
     * @return average score in [0, 1], or 0 if list is null/empty
     */
    private static double scoreAll(List<Table> ts){
        if (ts == null || ts.isEmpty()) return 0.0;
        double sum = 0;
        for (Table t : ts) {
            sum += score(t);
        }
        return sum / ts.size();
    }

    /**
     * Scores a single table using simple quality indicators:
     * <ul>
     *   <li><b>Fill ratio</b> — fraction of non-empty cells (encourages informative tables)</li>
     *   <li><b>Row structure</b> — fraction of rows with more than one non-empty cell
     *       (penalizes sparse line noise)</li>
     *   <li><b>Richness</b> — a mild preference for wider tables</li>
     * </ul>
     * The final score is a weighted sum: {@code 0.6*fill + 0.3*structure + 0.1*richness}.
     *
     * @param t table to score
     * @return score in [0, 1]
     */
    private static double score(Table t){
        int rows = t.nrows(), cols = t.ncols();
        if (rows == 0 || cols == 0) return 0.0;

        int total = rows * cols;
        int nonEmpty = 0;
        int multiColRows = 0;

        for (int r = 0; r < rows; r++){
            int rowNE = 0;
            for (int c = 0; c < cols; c++){
                String v = t.cell(r, c);
                if (v != null && !v.isBlank()){
                    nonEmpty++;
                    rowNE++;
                }
            }
            if (rowNE > 1) multiColRows++;
        }

        double fill = (double) nonEmpty / total;
        double structure = (double) multiColRows / rows;
        double richness = Math.log(1 + cols) / Math.log(4); // grows slowly with #cols

        return 0.6 * fill + 0.3 * structure + 0.1 * richness;
    }
}
