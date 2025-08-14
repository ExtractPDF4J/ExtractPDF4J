package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.PageRange;
import com.extractpdf4j.helpers.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * StreamParser
 *
 * <p>Extracts tables from digitally generated PDFs by reading text positions via
 * PDFBox and grouping glyphs into rows and columns. This strategy works best
 * when a reliable text layer exists (non-scanned documents).</p>
 *
 * <h3>High-level steps</h3>
 * <ol>
 *   <li>Collect glyphs on the page using PDFBox ({@code PDFTextStripper}).</li>
 *   <li>Group glyphs into visual rows using Y proximity.</li>
 *   <li>Within each row, merge adjacent glyphs into word spans; sort by X.</li>
 *   <li>Infer column boundaries from persistent gaps across rows.</li>
 *   <li>Assign spans to columns to build a {@link Table} grid.</li>
 * </ol>
 */
public class StreamParser extends BaseParser {
    public StreamParser(String filepath){ super(filepath); }

    /**
     * Parses a specific page (1-based) or all pages when {@code page == -1}.
     *
     * <p>For “all pages”, iterates through the document and calls
     * {@link #extractFromPage(PDDocument, int)} per page; otherwise only the
     * requested page is processed (subject to the configured page selection).</p>
     */

    @Override
    protected List<Table> parsePage(int page) throws IOException {
        try (PDDocument doc = PDDocument.load(new java.io.File(filepath))) {
            List<Table> out = new ArrayList<>();
            List<Integer> pages = PageRange.parse(this.pages);
            if (pages.size() == 1 && pages.get(0) == -1) {
                for (int p=1; p<=doc.getNumberOfPages(); p++) out.add(extractFromPage(doc, p));
            } else {
                if (!pages.contains(page)) return Collections.emptyList();
                out.add(extractFromPage(doc, page));
            }
            return out;
        }
    }

    /**
     * Extracts a {@link Table} from a single page using text positions.
     *
     * <p>Collects glyphs, groups them into rows, merges into spans within rows,
     * infers column boundaries from gaps, and assigns spans to columns.</p>
     *
     * @param doc            open document
     * @param pageOneIndexed 1-based page index
     */

    private Table extractFromPage(PDDocument doc, int pageOneIndexed) throws IOException {
        class Collector extends PDFTextStripper {
            final List<Glyph> glyphs = new ArrayList<>();
            Collector() throws IOException { setSortByPosition(true); setStartPage(pageOneIndexed); setEndPage(pageOneIndexed); }
            @Override protected void processTextPosition(TextPosition text) {
                String s = text.getUnicode();
                if (s == null || s.isEmpty()) return;
                glyphs.add(new Glyph(s, text.getXDirAdj(), text.getYDirAdj(), text.getWidthDirAdj()));
            }
        }
        Collector stripper = new Collector();
        stripper.getText(doc);

        // Group into lines by Y
        double yTol = 2.0;
        Map<Integer, List<Glyph>> lines = new TreeMap<>(Comparator.reverseOrder());
        for (Glyph g : stripper.glyphs) {
            int bucket = (int)Math.round(g.y / yTol);
            lines.computeIfAbsent(bucket, k->new ArrayList<>()).add(g);
        }
        // Sort and form spans
        List<Row> rows = new ArrayList<>();
        for (List<Glyph> gs : lines.values()) {
            gs.sort(Comparator.comparingDouble(a->a.x));
            rows.add(new Row(gs));
        }
        List<Double> bounds = inferColumnBounds(rows);
        List<List<String>> grid = new ArrayList<>();
        for (Row r : rows) {
            List<String> row = new ArrayList<>(Collections.nCopies(bounds.size()-1, ""));
            for (Span sp : r.spans()) {
                int col = findCol(bounds, sp.x);
                String prev = row.get(col);
                row.set(col, prev.isEmpty()? sp.text : prev + " " + sp.text);
            }
            if (stripText) for (int i=0;i<row.size();i++) row.set(i, row.get(i).trim());
            grid.add(row);
        }
        List<Double> rowBounds = new ArrayList<>();
        for (List<Glyph> gs : lines.values()) rowBounds.add(gs.stream().mapToDouble(g->g.y).average().orElse(0));
        return new Table(grid, bounds, rowBounds);
    }

    /** Returns the column index i where x ∈ [bounds[i], bounds[i+1}), or clamps to last. */

    private static int findCol(List<Double> bounds, double x) {
        for (int i=0;i+1<bounds.size();i++) if (x>=bounds.get(i) && x<bounds.get(i+1)) return i;
        return bounds.size()-2;
    }

    /**
     * Infers column boundary X positions by examining intra-row gaps between spans
     * and keeping separators that persist across multiple rows.
     */
    private static List<Double> inferColumnBounds(List<Row> rows) {
        Map<Integer,Integer> hist = new HashMap<>();
        for (Row r : rows) {
            List<Glyph> gs = r.glyphs;
            for (int i=0;i+1<gs.size();i++) {
                double right = gs.get(i).x + gs.get(i).w;
                double gap = gs.get(i+1).x - right;
                if (gap > 12.0) {
                    int bucket = (int)Math.round((right + gap/2)/10.0);
                    hist.put(bucket, hist.getOrDefault(bucket,0)+1);
                }
            }
        }
        List<Integer> keys = hist.entrySet().stream().sorted((a,b)->Integer.compare(a.getKey(), b.getKey()))
                .map(Map.Entry::getKey).collect(Collectors.toList());
        List<Double> bounds = new ArrayList<>();
        bounds.add(0.0);
        for (int k : keys) bounds.add(k*10.0);
        bounds.add(1e9);
        return bounds;
    }

    private static class Glyph { final String s; final double x,y,w; Glyph(String s,double x,double y,double w){this.s=s;this.x=x;this.y=y;this.w=w;} }
    private static class Row {
        final List<Glyph> glyphs;
        Row(List<Glyph> glyphs){ this.glyphs=glyphs; }
        List<Span> spans(){
            List<Span> out = new ArrayList<>();
            StringBuilder sb = new StringBuilder();
            double curX = glyphs.isEmpty()?0:glyphs.get(0).x;
            double lastRight = -1e9;
            for (Glyph g : glyphs) {
                double gap = g.x - lastRight;
                if (lastRight > -1e9 && gap > 6.0) {
                    out.add(new Span(curX, sb.toString().trim()));
                    sb.setLength(0);
                    curX = g.x;
                }
                sb.append(g.s);
                lastRight = g.x + g.w;
            }
            if (sb.length()>0) out.add(new Span(curX, sb.toString().trim()));
            return out;
        }
    }
    private static record Span(double x, String text){}
}
