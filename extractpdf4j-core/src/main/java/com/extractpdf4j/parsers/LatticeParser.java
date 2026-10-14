package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.ImagePdfUtils;
import com.extractpdf4j.helpers.Ocr;
import com.extractpdf4j.helpers.PageRange;
import com.extractpdf4j.helpers.Table;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_core.Point;
import org.bytedeco.opencv.opencv_core.Rect;
import org.bytedeco.opencv.opencv_core.Scalar;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * LatticeParser
 *
 * <p>Detects table structure by rasterizing pages and finding horizontal/vertical
 * ruling lines with OpenCV. Reconstructs a cell grid, maps PDF text into cells,
 * and optionally runs OCR for sparsely filled cells.</p>
 *
 * <h3>Pipeline</h3>
 * <ol>
 *   <li>Render page to image at {@link #renderDpi}.</li>
 *   <li>Binarize for line detection (adaptive threshold).</li>
 *   <li>Extract horizontal/vertical lines via morphology; project to get line positions.</li>
 *   <li>Build grid from line intersections; map PDF glyphs to cell coords.</li>
 *   <li>Fallback OCR for cells if text coverage is low.</li>
 *   <li>Emit {@link Table} with grid + row/column boundaries.</li>
 * </ol>
 *
 * <p>Page indexing follows the {@link BaseParser} convention: this class expects
 * {@code parsePage(1)} for the first page; {@code parsePage(-1)} means “all pages”.</p>
 */
public class LatticeParser extends BaseParser {
    /** Enable debug artifacts (overlay grid image, optional cell crops & OCR text). */
    private boolean debug = false;
    /** Preserve empty cells in the reconstructed grid. */
    private boolean keepCells = false;
    /** Render DPI for rasterization; higher helps scans and OCR (e.g., 300f). */
    private float renderDpi = 300f;
    /** Minimum cell dimensions (in pixels) to consider for OCR. */
    private double minCellW = 20, minCellH = 10;
    /** Directory for debug artifacts. */
    private File debugDir = new File("debug");

    public LatticeParser(String filepath){ super(filepath); }

    /**
     * Creates a {@code LatticeParser} for in-memory processing.
     * The PDF document must be passed to the parse() method.
     */
    public LatticeParser() {
        super();
    }
    
    /** Toggle debug overlays/artifacts. */
    public LatticeParser debug(boolean on){ this.debug = on; return this; }
    /** Keep empty cells in the final grid (useful for fixed layouts). */
    public LatticeParser keepCells(boolean on){ this.keepCells = on; return this; }
    /** Set rasterization DPI. */
    public LatticeParser dpi(float dpi){ this.renderDpi = dpi; return this; }
    /** Set debug artifact directory. */
    public LatticeParser debugDir(File dir){ if (dir!=null) this.debugDir = dir; return this; }

    /**
     * @deprecated This method loads the document from disk on every call.
     *             Prefer loading the PDDocument once and using {@link #parse(PDDocument)}.
     */
    @Override
    @Deprecated
    protected List<Table> parsePage(int page) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(filepath))) {
            List<Integer> pages = PageRange.parse(this.pages);
            List<Table> out = new ArrayList<>();

            if (pages.size()==1 && pages.get(0)==-1) {
                // “all” → iterate all pages
                for (int p=1; p<=doc.getNumberOfPages(); p++) {
                    out.addAll(extractFromPage(doc, p-1));
                }
            } else {
                // Process only if requested page is in selection
                if (!pages.contains(page)) return Collections.emptyList();
                out.addAll(extractFromPage(doc, page-1));
            }
            // Keep only non-empty tables
            return out.stream().filter(t -> t.nrows()>0 && t.ncols()>0).collect(Collectors.toList());
        }
    }

    @Override
    public List<Table> parse(PDDocument document) throws IOException {
        List<Table> tables = new ArrayList<>();

        // Parse the page selection string (e.g., "1-3,5", "all")
        List<Integer> pagesToProcess = PageRange.parse(this.pages);

        // Check for the "all pages" case
        if (pagesToProcess.size() == 1 && pagesToProcess.get(0) == -1) {
            // If "all", iterate from page 0 to the last page (zero-indexed for extractFromPage)
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                tables.addAll(extractFromPage(document, i));
            }
        } else {
            // Otherwise, iterate through the explicitly selected pages
            for (int pageNum : pagesToProcess) {
                int zeroIdx = pageNum - 1; // Convert to zero-based index
                // Ensure the page number is valid
                if (zeroIdx >= 0 && zeroIdx < document.getNumberOfPages()) {
                    tables.addAll(extractFromPage(document, zeroIdx));
                }
            }
        }
        return tables;
    }
    
    /**
     * Extracts a table grid from a single zero-based page index.
     *
     * <p>Steps: render → binarize → line morphology → projections → grid → map glyphs → optional OCR → debug.</p>
     *
     * @param doc     open document
     * @param zeroIdx zero-based page index
     * @return independent ruled tables with cell contents and boundaries (empty if no grid)
     */
    private List<Table> extractFromPage(PDDocument doc, int zeroIdx) throws IOException {
        // 1) Render page to binary image, convert to OpenCV Mat, and binarize for line finding
        BufferedImage bim = ImagePdfUtils.renderPage(doc, zeroIdx, renderDpi);
        Mat gray = ImagePdfUtils.bufferedToMat(bim);
        Mat bw = ImagePdfUtils.binarizeForLines(gray);

        // 2) Line morphology: erode/dilate with long kernels to emphasize full-length lines
        int scale = Math.max(1, gray.cols()/1000);
        Mat horizontal = bw.clone(), vertical = bw.clone();

        Mat hStruct = getStructuringElement(MORPH_RECT, new org.bytedeco.opencv.opencv_core.Size(Math.max(10*scale,10), 1));
        erode(horizontal, horizontal, hStruct); dilate(horizontal, horizontal, hStruct);
        Mat vStruct = getStructuringElement(MORPH_RECT, new org.bytedeco.opencv.opencv_core.Size(1, Math.max(10*scale,10)));
        erode(vertical, vertical, vStruct); dilate(vertical, vertical, vStruct);

        // Each connected ruled region has its own grid. Page-wide projections merge
        // account summaries and transactions, and miss lines shorter than the page.
        Mat mask = new Mat();
        bitwise_or(horizontal, vertical, mask);
        MatVector contours = new MatVector();
        findContours(mask, contours, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE);
        List<Rect> regions = new ArrayList<>();
        for (long i = 0; i < contours.size(); i++) {
            Rect region = boundingRect(contours.get(i));
            if (region.width() >= 40 * scale && region.height() >= 20 * scale) regions.add(region);
        }
        regions.sort(Comparator.comparingInt((Rect r) -> r.y()).thenComparingInt(r -> r.x()));
        List<Table> tables = new ArrayList<>();
        for (Rect region : regions) {
            List<Integer> rowsY = projectLines(new Mat(horizontal, region), true).stream()
                    .map(y -> y + region.y()).collect(Collectors.toList());
            List<Integer> colsX = projectLines(new Mat(vertical, region), false).stream()
                    .map(x -> x + region.x()).collect(Collectors.toList());
            rowsY = dedupe(rowsY, Math.max(3, scale * 2));
            colsX = dedupe(colsX, Math.max(3, scale * 2));
            // Do not fabricate a table from page borders when no grid was detected.
            if (rowsY.size() < 2 || colsX.size() < 2) continue;
            tables.add(extractGrid(doc, zeroIdx, tables.size() + 1, gray, rowsY, colsX));
        }
        return tables;
    }

    private Table extractGrid(PDDocument doc, int zeroIdx, int tableNumber, Mat gray,
                              List<Integer> rowsY, List<Integer> colsX) throws IOException {
        int nrows = Math.max(0, rowsY.size()-1), ncols = Math.max(0, colsX.size()-1);
        if (nrows==0 || ncols==0) return new Table(List.of(), List.of(), List.of());

        // Prepare grid with empty strings
        List<List<String>> grid = new ArrayList<>();
        for (int r=0;r<nrows;r++) grid.add(new ArrayList<>(Collections.nCopies(ncols, "")));

        // 4) Map adjusted PDF text positions (72 DPI, origin top-left) into image pixels
        double scaleXY = renderDpi / 72.0; // PDF points → image pixels
        List<Glyph> glyphs = collectGlyphsAsImageCoords(doc, zeroIdx+1, scaleXY);
        glyphs.sort(Comparator.comparingDouble((Glyph g) -> g.cy).thenComparingDouble(g -> g.cx));
        for (Glyph g : glyphs) {
            int ci = locate(colsX, g.cx), ri = locate(rowsY, g.cy);
            if (ri>=0 && ri<nrows && ci>=0 && ci<ncols) {
                String prev = grid.get(ri).get(ci);
                grid.get(ri).set(ci, prev.isEmpty()? g.s : prev + g.s);
            }
        }

        // 5) Heuristic OCR decision: if less than 25% of cells have text, try OCR per cell
        int nonEmpty = 0, total = nrows*ncols;
        for (var rr: grid) for (var v: rr) if (v!=null && !v.isBlank()) nonEmpty++;
        boolean needOcr = nonEmpty < total * 0.25;

        // Optional debug directory for cell crops
        File cellsDir = null;
        if (debug && keepCells) {
            cellsDir = new File(new File(debugDir, "cells"), "page"+(zeroIdx+1)+"_table"+tableNumber);
            if (!cellsDir.exists()) cellsDir.mkdirs();
        }

        if (needOcr) {
            for (int r=0;r<nrows;r++) for (int c=0;c<ncols;c++) {
                int x = colsX.get(c), y = rowsY.get(r);
                int w = colsX.get(c+1)-x, h = rowsY.get(r+1)-y;
                if (w < minCellW || h < minCellH) continue;

                Mat roi = new Mat(gray, new Rect(x,y,w,h));

                // Upscale + adaptive binarize; invert if dark background
                Mat up = new Mat();
                resize(roi, up, new org.bytedeco.opencv.opencv_core.Size(w*2, h*2), 0, 0, INTER_CUBIC);
                Mat bin = new Mat();
                adaptiveThreshold(up, bin, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY, 35, 15);
                if (mean(bin).get(0) < 128) bitwise_not(bin, bin);

                // Write crops if debugging
                File rawPng = null, binPng = null;
                if (cellsDir != null) {
                    rawPng = new File(cellsDir, String.format("r%02d_c%02d_raw.png", r, c));
                    binPng = new File(cellsDir, String.format("r%02d_c%02d_bin.png", r, c));
                    imwrite(rawPng.getAbsolutePath(), roi);
                    imwrite(binPng.getAbsolutePath(), bin);
                }

                // OCR pass (temporary PNG for OCR helper)
                File tmp = File.createTempFile("extractpdf4j_cell_", ".png");
                imwrite(tmp.getAbsolutePath(), bin);
                String text = Ocr.ocrPng(tmp.getAbsolutePath());
                tmp.delete();

                grid.get(r).set(c, text);

                // Optional: write OCR text to file to ease inspection
                if (cellsDir != null) {
                    try (var fw = new java.io.FileWriter(new File(cellsDir, String.format("r%02d_c%02d.txt", r, c)))) {
                        fw.write(text == null ? "" : text);
                    } catch (Exception ignored) {}
                }
            }
        }

        // Row/column boundaries as doubles for Table
        List<Double> colBounds = colsX.stream().map(i->(double)i).collect(Collectors.toList());
        List<Double> rowBounds = rowsY.stream().map(i->(double)i).collect(Collectors.toList());

        // Debug overlay image with grid lines
        if (debug) {
            if (!debugDir.exists()) debugDir.mkdirs();
            Mat dbg = new Mat(); cvtColor(gray, dbg, COLOR_GRAY2BGR);
            for (int y : rowsY) line(dbg, new Point(0,y), new Point(gray.cols()-1, y), new Scalar(0,255,0,0),1,LINE_AA,0);
            for (int x : colsX) line(dbg, new Point(x,0), new Point(x, gray.rows()-1), new Scalar(255,0,0,0),1,LINE_AA,0);
            File outDbg = new File(debugDir, "debug_lattice_grid_page"+(zeroIdx+1)+"_table"+tableNumber+".png");
            imwrite(outDbg.getAbsolutePath(), dbg);
            System.out.println("Wrote debug grid: " + outDbg.getAbsolutePath());
        }

        return new Table(grid, colBounds, rowBounds);
    }

    /**
     * Projects white-pixel coverage along rows or columns to detect line locations.
     * A position is considered a line when coverage &gt; 35%.
     *
     * @param bin        binary image
     * @param horizontal true for row‑wise projection; false for column‑wise
     * @return list of y (rows) or x (columns) indices that look like lines
     */
    private static List<Integer> projectLines(Mat bin, boolean horizontal) {
        int rows = bin.rows(), cols = bin.cols();
        List<Integer> peaks = new ArrayList<>();
        if (horizontal) {
            for (int y=0;y<rows;y++) {
                int sum=0;
                for (int x=0;x<cols;x++) {
                    int v = bin.ptr(y, x).get(0) & 0xFF; // portable across JavaCPP versions
                    if (v > 0) sum++;
                }
                if (sum > cols * 0.35) peaks.add(y);
            }
        } else {
            for (int x=0;x<cols;x++) {
                int sum=0;
                for (int y=0;y<rows;y++) {
                    int v = bin.ptr(y, x).get(0) & 0xFF;
                    if (v > 0) sum++;
                }
                if (sum > rows * 0.35) peaks.add(x);
            }
        }
        return peaks;
    }

    /**
     * Removes near-duplicate positions within {@code tol} pixels.
     * Input is sorted before compaction.
     */
    private static List<Integer> dedupe(List<Integer> xs, int tol) {
        if (xs.isEmpty()) return xs;
        Collections.sort(xs);
        List<Integer> out = new ArrayList<>();
        int last = xs.get(0); out.add(last);
        for (int i=1;i<xs.size();i++) {
            int v = xs.get(i);
            if (Math.abs(v-last) > tol) { out.add(v); last = v; }
        }
        return out;
    }

    /**
     * Returns the index of the interval {@code [bounds[i], bounds[i+1])} that contains {@code coord},
     * or -1 if it lies outside all intervals.
     */
    private static int locate(List<Integer> bounds, double coord) {
        for (int i=0;i+1<bounds.size();i++) if (coord >= bounds.get(i) && coord < bounds.get(i+1)) return i;
        return -1;
    }

    /** A single PDF glyph positioned in image pixel coordinates. */
    private static class Glyph { final String s; final double cx, cy; Glyph(String s,double cx,double cy){this.s=s;this.cx=cx;this.cy=cy;} }

    /**
     * Collects glyphs (characters) for one page and maps their centers from PDF
     * coordinate space (points, origin bottom-left) to raster image pixel space
     * (origin top-left). Adjusted PDFBox Y coordinates already use a top-left origin; do not invert them.
     *
     * @param doc             document
     * @param pageOneIndexed  page number starting at 1
     * @param scaleXY         pixels-per-point factor (e.g., dpi/72)
     */
    private List<Glyph> collectGlyphsAsImageCoords(PDDocument doc, int pageOneIndexed, double scaleXY) throws IOException {
        class Collector extends PDFTextStripper {
            final List<Glyph> glyphs = new ArrayList<>();
            Collector() throws IOException { setSortByPosition(true); setStartPage(pageOneIndexed); setEndPage(pageOneIndexed); }
            @Override protected void processTextPosition(TextPosition t) {
                String s = t.getUnicode(); if (s == null || s.isEmpty()) return;
                double cxPdf = t.getXDirAdj() + t.getWidthDirAdj()/2.0;
                double cyPdf = t.getYDirAdj() - t.getHeightDir()/2.0;
                double cxImg = cxPdf * scaleXY;
                double cyImg = cyPdf * scaleXY;
                glyphs.add(new Glyph(s, cxImg, cyImg));
            }
        }
        Collector c = new Collector(); c.getText(doc); return c.glyphs;
    }
}