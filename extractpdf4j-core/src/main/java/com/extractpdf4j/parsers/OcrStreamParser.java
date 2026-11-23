package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Ocr;
import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.helpers.ImagePdfUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.Size;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormatSymbols;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.imwrite;
import static org.bytedeco.opencv.global.opencv_imgproc.*;

/**
 * OcrStreamParser (header-aware):
 *  - Removes horizontal *and* vertical rules before OCR.
 *  - Uses Tesseract TSV to read words.
 *  - Anchors column boundaries using the table header ("Date", "Description", "Debit", "Credit", "Balance")
 *    via fuzzy matching; falls back to histogram of mid-gaps when header cannot be confidently detected.
 *  - Normalizes numeric/date columns.
 *
 * This version is a drop-in replacement for the original OcrStreamParser.
 */
public class OcrStreamParser extends BaseParser {
    private float renderDpi = 450f;   // OCR likes ~350–450
    private boolean debug = false;
    private File debugDir = new File("debug");
    private List<String> requiredHeaders = new ArrayList<>();
    private boolean requiredHeadersFoundInFile = false;

    public OcrStreamParser(String filepath){ super(filepath); }
    
    /**
     * Creates an {@code OcrStreamParser} for in-memory processing.
     * The PDF document must be passed to the parse() method.
     */
    public OcrStreamParser() {
        super();
    }
    
    public OcrStreamParser dpi(float dpi){ this.renderDpi = dpi; return this; }
    public OcrStreamParser debug(boolean on){ this.debug = on; return this; }
    public OcrStreamParser debugDir(File dir){ if (dir!=null) this.debugDir=dir; return this; }
    public OcrStreamParser requiredHeaders(List<String> headers){ this.requiredHeaders = headers; return this; }

    /**
     * @deprecated This method loads the document from disk on every call.
     *             Prefer loading the PDDocument once and using {@link #parse(PDDocument)}.
     */
    @Override
    @Deprecated
    protected List<Table> parsePage(int page) throws IOException {
        try (PDDocument doc = PDDocument.load(new File(filepath))) {
            List<Integer> pages = com.extractpdf4j.helpers.PageRange.parse(this.pages);
            List<Table> out = new ArrayList<>();
            if (pages.size()==1 && pages.get(0)==-1) {
                for (int p=1;p<=doc.getNumberOfPages();p++) out.add(extractFromPage(doc, p-1));
            } else {
                if (!pages.contains(page)) return Collections.emptyList();
                out.add(extractFromPage(doc, page-1));
            }
            if (!requiredHeaders.isEmpty() && !requiredHeadersFoundInFile){
                System.err.printf("Pdf file doesn't contain required headers: %s", String.join(", ", requiredHeaders));
                System.exit(1);
            }
            return out;
        }
    }

    @Override
    public List<Table> parse(PDDocument document) throws IOException {
        List<Table> tables = new ArrayList<>();

        // Parse the page selection string (e.g., "1-3,5", "all")
        List<Integer> pagesToProcess = com.extractpdf4j.helpers.PageRange.parse(this.pages);

        // Check for the "all pages" case
        if (pagesToProcess.size() == 1 && pagesToProcess.get(0) == -1) {
            // If "all", iterate from page 0 to the last page (zero-indexed for extractFromPage)
            for (int i = 0; i < document.getNumberOfPages(); i++) {
                Table table = extractFromPage(document, i);
                if (table != null && table.nrows() > 0) {
                    tables.add(table);
                }
            }
        } else {
            // Otherwise, iterate through the explicitly selected pages
            for (int pageNum : pagesToProcess) {
                int zeroIdx = pageNum - 1; // Convert to zero-based index
                // Ensure the page number is valid
                if (zeroIdx >= 0 && zeroIdx < document.getNumberOfPages()) {
                    Table table = extractFromPage(document, zeroIdx);
                    if (table != null && table.nrows() > 0) {
                        tables.add(table);
                    }
                }
            }
        }

        if (!requiredHeaders.isEmpty() && !requiredHeadersFoundInFile){
            System.err.printf("Pdf file doesn't contain required headers: %s%n", String.join(", ", requiredHeaders));
            // In a service context, throwing an exception might be better than System.exit()
            // For now, we'll return an empty list to avoid crashing the service.
            return Collections.emptyList();
        }

        return tables;
    }

    private Table extractFromPage(PDDocument doc, int zeroIdx) throws IOException {
        // 0) Render page (binary for crisp OCR; we convert to gray right away)
        BufferedImage bim = ImagePdfUtils.renderPage(doc, zeroIdx, renderDpi);
        Mat gray = ImagePdfUtils.bufferedToMat(bim);

        // 1) Adaptive binarization -> white text/lines
        Mat binInv = new Mat();
        adaptiveThreshold(gray, binInv, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 31, 15);

        // 2) Detect and remove *both* horizontal and vertical rules
        Mat noLines = removeRules(binInv);

        // 3) Light dilation to reconnect thin glyphs
        Mat textKernel = getStructuringElement(MORPH_RECT, new Size(1,1));
        dilate(noLines, noLines, textKernel);

        // 4) Invert to BLACK text on WHITE for Tesseract
        Mat cleaned = new Mat(); bitwise_not(noLines, cleaned);

        if (debug) {
            if (!debugDir.exists()) debugDir.mkdirs();
            imwrite(new File(debugDir, "ocrstream_binInv_page"+(zeroIdx+1)+".png").getAbsolutePath(), binInv);
            imwrite(new File(debugDir, "ocrstream_cleaned_page"+(zeroIdx+1)+".png").getAbsolutePath(), cleaned);
        }

        // 5) Save temp PNG fed to Tesseract TSV
        File pagePng = File.createTempFile("ocrstream_page_", ".png");
        imwrite(pagePng.getAbsolutePath(), cleaned);

        // 6) TSV OCR
        List<Ocr.OcrWord> words = Ocr.ocrTsv(pagePng.getAbsolutePath());
        pagePng.delete();
        words = words.stream().filter(w -> w.text != null && !w.text.isBlank()).collect(Collectors.toList());
        if (words.isEmpty()) return new Table(List.of(), List.of(), List.of());

        // 7) Group words by logical line
        Map<String, List<Ocr.OcrWord>> groups = new LinkedHashMap<>();
        for (var w : words) groups.computeIfAbsent(w.block+":"+w.par+":"+w.line, k->new ArrayList<>()).add(w);
        List<List<Ocr.OcrWord>> lines = new ArrayList<>(groups.values());
        // sort lines by top, words left-to-right
        lines.sort(Comparator.comparingInt(a -> a.stream().mapToInt(w->w.top).min().orElse(0)));
        for (var ln : lines) ln.sort(Comparator.comparingInt(w -> w.left));

        if (!requiredHeaders.isEmpty() && !requiredHeadersFoundInFile){
            // 8) Try to find all required headers by parsing lines
            requiredHeadersFoundInFile = findRequiredHeaders(lines);
        }

        // 9) Try to anchor columns using header detection (fuzzy)
        int pageWidth = cleaned.cols();
        List<Double> colBounds = headerAnchoredBounds(lines, pageWidth);

        if (colBounds == null) {
            // 10) Fallback: infer separators from mid-gaps histogram
            List<Integer> seps = inferSeparators(lines, pageWidth);
            colBounds = new ArrayList<>();
            colBounds.add(0.0);
            for (int s : seps) colBounds.add((double)s);
            colBounds.add((double) pageWidth);
        }

        int ncols = Math.max(1, colBounds.size()-1);

        // 11) Build grid by assigning each word to its column (center or right-edge for numeric-like)
        List<List<String>> grid = new ArrayList<>();
        boolean headerPassed = false;
        for (var ln : lines) {
            // skip header line if we anchored on it
            if (!headerPassed && looksLikeHeader(ln)) {
                headerPassed = true;
                continue;
            }
            List<String> row = new ArrayList<>(Collections.nCopies(ncols, ""));
            for (var w : ln) {
                double cx = isLikelyNumeric(w.text) ? (w.left + w.width) - 1 /* right-ish */ : w.left + w.width/2.0;
                int col = locate(colBounds, cx);
                row.set(col, (row.get(col).isEmpty() ? w.text : row.get(col) + " " + w.text));
            }
            for (int i=0;i<row.size();i++) row.set(i, row.get(i).trim());
            // ignore empty lines
            boolean any = row.stream().anyMatch(s -> s != null && !s.isBlank());
            if (any) grid.add(row);
        }

        normalizeNumericLikeColumns(grid);

        // For row bounds, use centerlines from OCR (not critical for CSV)
        List<Double> rowBounds = new ArrayList<>();
        for (var ln : lines) {
            int topMin = ln.stream().mapToInt(w->w.top).min().orElse(0);
            int botMax = ln.stream().mapToInt(w->w.top+w.height).max().orElse(topMin);
            rowBounds.add((double)(topMin+botMax)/2.0);
        }

        return new Table(grid, colBounds, rowBounds);
    }

    /** Remove horizontal and vertical rules using morphology, then subtract from the text mask. */
    private Mat removeRules(Mat binInv){
        int w = binInv.cols(), h = binInv.rows();
        // horizontals
        int hKernelW = Math.max(w/30, 35);
        Mat hKernel = getStructuringElement(MORPH_RECT, new Size(hKernelW, 1));
        Mat hLines = new Mat();
        morphologyEx(binInv, hLines, MORPH_OPEN, hKernel);
        // verticals
        int vKernelH = Math.max(h/24, 30);
        Mat vKernel = getStructuringElement(MORPH_RECT, new Size(1, vKernelH));
        Mat vLines = new Mat();
        morphologyEx(binInv, vLines, MORPH_OPEN, vKernel);
        // union rules
        Mat rules = new Mat(); bitwise_or(hLines, vLines, rules);
        Mat noLines = new Mat(); subtract(binInv, rules, noLines);
        return noLines;
    }

    /** Returns true if any single line contains all required headers. */
    private boolean findRequiredHeaders(List<List<Ocr.OcrWord>> lines){
        for (var line : lines) {
            if (matchRequiredHeaders(line)) return true;
        }
        return false;
    }

    // ---------- Column anchoring via header ----------

    private static final String[] EXPECTED_HEADERS = {"date","description","debit","credit","balance"};

    /** Returns bounds if header is found, else null. */
    private List<Double> headerAnchoredBounds(List<List<Ocr.OcrWord>> lines, int pageWidth){
        // Consider the first ~35% vertical region for header
        int cutoffY = Integer.MAX_VALUE;
        if (!lines.isEmpty()) {
            int firstTop = lines.get(0).stream().mapToInt(w->w.top).min().orElse(0);
            cutoffY = firstTop + (int)(pageWidth * 0.35); // approximate scale
        }
        for (var ln : lines) {
            int top = ln.stream().mapToInt(w->w.top).min().orElse(0);
            if (top > cutoffY) break;
            // map words
            Map<String,Ocr.OcrWord> match = matchHeader(ln);
            if (match.size() >= 4) { // 4/5 header words found → reliable
                // order by EXPECTED_HEADERS
                List<Double> xs = new ArrayList<>();
                for (String h : EXPECTED_HEADERS) {
                    Ocr.OcrWord w = match.get(h);
                    if (w != null) xs.add((double) w.left);
                }
                if (xs.size() >= 3) {
                    Collections.sort(xs);
                    List<Double> bounds = new ArrayList<>();
                    bounds.add(0.0);
                    for (int i=0;i+1<xs.size();i++){
                        double mid = (xs.get(i) + xs.get(i+1)) / 2.0;
                        bounds.add(mid);
                    }
                    bounds.add((double) pageWidth);
                    return bounds;
                }
            }
        }
        return null;
    }

    /** Try to match the five header titles on a single line using fuzzy matching. */
    private Map<String,Ocr.OcrWord> matchHeader(List<Ocr.OcrWord> line){
        Map<String,Ocr.OcrWord> out = new HashMap<>();
        for (var w : line) {
            String n = normalizeAlpha(w.text);
            for (String target : EXPECTED_HEADERS) {
                if (out.containsKey(target)) continue;
                int d = editDistance(n, target);
                if (d <= 1 || n.contains(target) || target.contains(n)) {
                    out.put(target, w);
                    break;
                }
            }
        }
        return out;
    }

    /** Try to match all required headers with words in line.
     * If line contains all headers return true, else return false
     * */
    private boolean matchRequiredHeaders (List<Ocr.OcrWord> line){
        List<String> found = new ArrayList<>();
        for (var word: line){
            String n = normalizeAlpha(word.text);
            for (String req: requiredHeaders){
                if (found.contains(req)) continue;
                if (n.equals(req) || editDistance(n, req) <= 1){
                    found.add(req);
                }
            }
        }
        return found.size() == requiredHeaders.size();
    }

    private static String normalizeAlpha(String s){
        StringBuilder b = new StringBuilder(s.length());
        for (int i=0;i<s.length();i++){
            char c = Character.toLowerCase(s.charAt(i));
            if (c>='a' && c<='z') b.append(c);
        }
        return b.toString();
    }

    private static int editDistance(String a, String b){
        int n=a.length(), m=b.length();
        int[][] dp = new int[n+1][m+1];
        for (int i=0;i<=n;i++) dp[i][0]=i;
        for (int j=0;j<=m;j++) dp[0][j]=j;
        for (int i=1;i<=n;i++){
            for (int j=1;j<=m;j++){
                int cost = (a.charAt(i-1)==b.charAt(j-1))?0:1;
                dp[i][j] = Math.min(Math.min(dp[i-1][j]+1, dp[i][j-1]+1), dp[i-1][j-1]+cost);
            }
        }
        return dp[n][m];
    }

    private static boolean looksLikeHeader(List<Ocr.OcrWord> ln){
        int hit=0;
        for (var w : ln){
            String n = normalizeAlpha(w.text);
            for (String target : EXPECTED_HEADERS) {
                if (editDistance(n, target) <= 1 || n.contains(target) || target.contains(n)) { hit++; break; }
            }
        }
        return hit >= 3;
    }

    private static boolean isLikelyNumeric(String s){
        if (s == null || !NUM_LIKE.matcher(s).matches()) return false;
        Matcher signs = AMOUNT_SIGN.matcher(s);
        if (signs.find() && signs.find()) return false;
        return !hasConflictingCurrencies(s);
    }

    // ---------- Fallback separator inference ----------

    private static List<Integer> inferSeparators(List<List<Ocr.OcrWord>> lines, int pageWidth) {
        List<Integer> mids = new ArrayList<>();
        for (var ln : lines) {
            if (ln.size() < 2) continue;
            for (int i=0;i+1<ln.size();i++) {
                Ocr.OcrWord a = ln.get(i), b = ln.get(i+1);
                int rightA = a.left + a.width;
                int gap = b.left - rightA;
                if (gap > 18) mids.add(rightA + gap/2);
            }
        }
        if (mids.isEmpty()) return List.of();
        int bin = 20;
        Map<Integer,Integer> hist = new HashMap<>();
        for (int x : mids) {
            int k = Math.max(0, Math.min(pageWidth, x)) / bin;
            hist.put(k, hist.getOrDefault(k,0)+1);
        }
        int minVotes = Math.max(3, lines.size()/3);
        List<Integer> peaks = hist.entrySet().stream()
                .filter(e -> e.getValue() >= minVotes)
                .map(e -> e.getKey()*bin)
                .sorted().collect(Collectors.toList());

        List<Integer> seps = new ArrayList<>();
        int last = -10_000;
        for (int x : peaks) {
            if (seps.isEmpty() || Math.abs(x-last) > 30) { seps.add(x); last = x; }
        }
        return seps.stream().filter(x -> x > 25 && x < pageWidth-25).collect(Collectors.toList());
    }

    // ---------- Placement helpers & normalization ----------

    /** Returns column index i where x ∈ [bounds[i], bounds[i+1}), clamped. */
    private static int locate(List<Double> bounds, double x) {
        for (int i=0;i+1<bounds.size();i++) if (x>=bounds.get(i) && x<bounds.get(i+1)) return i;
        return Math.max(0, bounds.size()-2);
    }

    // Grouping marks must separate groups of three. Decimal dots/commas may follow
    // grouping, while horizontal whitespace includes NBSP and narrow NBSP.
    private static final String NUMBER = "(?:[0-9]{1,3}(?:,[0-9]{3})+(?:\\.[0-9]+)?"
            + "|[0-9]{1,2}(?:,[0-9]{2})+,[0-9]{3}(?:\\.[0-9]+)?"
            + "|[0-9]{1,3}(?:\\.[0-9]{3})+(?:,[0-9]+)?"
            + "|[0-9]{1,3}(?:(?:\\h+|['\u2019])[0-9]{3})+(?:[.,][0-9]+)?"
            + "|[0-9]+(?:[.,][0-9]+)?|[.,][0-9]+)";
    private static final Pattern NUMBER_TOKEN = Pattern.compile(NUMBER);
    private static final String CURRENCY_CODES = Currency.getAvailableCurrencies().stream()
            .map(Currency::getCurrencyCode).sorted().collect(Collectors.joining("|"));
    private static final String CURRENCY = "(?:US\\$|CA\\$|C\\$|AU\\$|A\\$|NZ\\$|HK\\$|SG\\$|S\\$|R\\$|\\p{Sc}|"
            + CURRENCY_CODES + ")";
    private static final Pattern CURRENCY_TOKEN = Pattern.compile(CURRENCY, Pattern.CASE_INSENSITIVE);
    private static final String SIGN = "[+\\-\u2212\uff0d]";
    private static final Pattern AMOUNT_SIGN = Pattern.compile(SIGN);
    // A sign may surround the currency or number. isLikelyNumeric rejects multiple signs.
    private static final String AMOUNT = "(?:" + SIGN + "\\h*)?(?:" + CURRENCY + "\\h*)?"
            + "(?:" + SIGN + "\\h*)?" + NUMBER + "(?:\\h*" + SIGN + ")?"
            + "(?:\\h*" + CURRENCY + ")?(?:\\h*" + SIGN + ")?";
    private static final Pattern NUM_LIKE = Pattern.compile(
            "^\\h*(?<creditPrefix>CR\\h*)?(?<value>(?:(?:" + SIGN + "\\h*)?(?:" + CURRENCY + "\\h*)?"
                    + "\\(\\h*" + AMOUNT + "\\h*\\)(?:\\h*" + CURRENCY + ")?"
                    + "(?:\\h*" + SIGN + ")?|" + AMOUNT + "))\\h*(?<creditSuffix>CR)?\\h*$",
            Pattern.CASE_INSENSITIVE);
    private static final Set<Locale> SOURCE_LOCALES = Set.copyOf(Arrays.asList(Locale.getAvailableLocales()));
    private static final String MONTHS = "Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec";
    private static final Pattern DATE_LIKE = Pattern.compile(
            "^\\h*([0-9]{1,2})(?:\\h*-\\h*|\\h*)(" + MONTHS + ")"
                    + "(?:(?:\\h*-\\h*|\\h*)([0-9]{4}|[0-9]{2}))?\\h*$",
            Pattern.CASE_INSENSITIVE);

    private static void normalizeNumericLikeColumns(List<List<String>> grid) {
        if (grid.isEmpty()) return;
        int cols = grid.get(0).size();
        int localeColumn = sourceLocaleColumn(grid.get(0));
        for (int c=0;c<cols;c++){
            int numish=0, dateish=0, nonempty=0;
            for (var row : grid) {
                if (c >= row.size()) continue;
                String s = row.get(c);
                if (s==null || s.isBlank()) continue;
                nonempty++;
                if (isLikelyNumeric(s)) numish++;
                else if (DATE_LIKE.matcher(s).matches()) dateish++;
            }
            if (nonempty==0) continue;

            double pNum = (double)numish/nonempty, pDate = (double)dateish/nonempty;
            if (pNum >= 0.6) {
                for (var row : grid) if (c < row.size() && isLikelyNumeric(row.get(c)))
                    row.set(c, normalizeAmount(row.get(c), sourceLocale(row, localeColumn)));
            } else if (pDate >= 0.6) {
                for (var row : grid) if (c < row.size()) row.set(c, normalizeDate(row.get(c)));
            }
        }
    }

    private static String normalizeAmount(String s) {
        return normalizeAmount(s, null);
    }

    /** Source locale describes the original text; normalization runs once per extracted grid. */
    private static String normalizeAmount(String s, Locale sourceLocale) {
        if (s == null || s.replaceAll("\\h", "").isBlank()) return "";
        if (!isLikelyNumeric(s)) return s;
        Matcher number = NUMBER_TOKEN.matcher(s);
        if (!number.find()) return s;
        String value = number.group().replaceAll("[\\h'\u2019]", "");
        int comma = value.lastIndexOf(',');
        int dot = value.lastIndexOf('.');
        if (comma >= 0 && dot >= 0) {
            // With both marks present, the rightmost one is the decimal separator.
            value = comma > dot ? value.replace(".", "").replace(',', '.')
                    : value.replace(",", "");
        } else if (comma >= 0 || dot >= 0) {
            int separator = Math.max(comma, dot);
            char mark = value.charAt(separator);
            if (value.indexOf(mark) != separator) {
                value = value.replace(String.valueOf(mark), "");
            } else if (value.length() - separator - 1 == 3 && separator > 0 && separator <= 3) {
                if (sourceLocale != null) {
                    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(sourceLocale);
                    if (mark == symbols.getDecimalSeparator()) value = value.replace(',', '.');
                    else if (mark == symbols.getGroupingSeparator()) value = value.replace(String.valueOf(mark), "");
                    else return s;
                } else {
                    Character decimal = currencyDecimalSeparator(s);
                    if (decimal == null) return s;
                    value = mark == decimal ? value.replace(',', '.')
                            : value.replace(String.valueOf(mark), "");
                }
            } else {
                value = value.replace(',', '.');
            }
        }
        if (value.startsWith(".")) value = "0" + value;
        Matcher amount = NUM_LIKE.matcher(s);
        if (!amount.matches()) return s;
        boolean negative = s.indexOf('-') >= 0 || s.indexOf('\u2212') >= 0 || s.indexOf('\uff0d') >= 0
                || s.indexOf('(') >= 0 || amount.group("creditPrefix") != null || amount.group("creditSuffix") != null;
        return negative ? "-" + value : value;
    }

    private static int sourceLocaleColumn(List<String> header) {
        int column = -1;
        for (int c = 0; c < header.size(); c++) {
            if (header.get(c) != null && header.get(c).strip().equalsIgnoreCase("Locale")) {
                if (column != -1) return -1;
                column = c;
            }
        }
        return column;
    }

    private static Locale sourceLocale(List<String> row, int column) {
        if (column < 0 || column >= row.size() || row.get(column) == null) return null;
        String tag = row.get(column).strip();
        if (!tag.matches("(?i)[a-z]{2,3}[-_][a-z]{2}")) return null;
        Locale locale = Locale.forLanguageTag(tag.replace('_', '-'));
        return SOURCE_LOCALES.contains(locale) ? locale : null;
    }

    private static Set<String> amountCurrencies(String s) {
        Set<String> currencies = new HashSet<>();
        Matcher amount = NUM_LIKE.matcher(s);
        if (!amount.matches()) return currencies;
        Matcher tokens = CURRENCY_TOKEN.matcher(amount.group("value"));
        while (tokens.find()) {
            String currency = switch (tokens.group().toUpperCase(Locale.ROOT)) {
                case "R$" -> "BRL";
                case "US$" -> "USD";
                case "CA$", "C$" -> "CAD";
                case "AU$", "A$" -> "AUD";
                case "NZ$" -> "NZD";
                case "HK$" -> "HKD";
                case "SG$", "S$" -> "SGD";
                case "\u20ac" -> "EUR";
                case "\u00a3" -> "GBP";
                case "\u20b9" -> "INR";
                case "\u20bd" -> "RUB";
                case "\u20a9" -> "KRW";
                default -> tokens.group().toUpperCase(Locale.ROOT);
            };
            currencies.add(currency);
        }
        if (!Collections.disjoint(currencies, Set.of("USD", "CAD", "AUD", "NZD", "HKD", "SGD")))
            currencies.remove("$");
        if (currencies.contains("JPY") || currencies.contains("CNY")) currencies.remove("\u00a5");
        return currencies;
    }

    private static boolean hasConflictingCurrencies(String s) {
        return amountCurrencies(s).size() > 1;
    }

    /**
     * Currency-specific formatting conventions are hints, not language detection.
     * Bare dollar/euro symbols, CHF, and conflicting hints remain ambiguous.
     * Apply these defaults only when the number itself is otherwise ambiguous.
     */
    private static Character currencyDecimalSeparator(String s) {
        Set<String> currencies = amountCurrencies(s);
        if (currencies.size() != 1) return null;
        return switch (currencies.iterator().next()) {
            case "BRL" -> ',';
            case "USD", "GBP" -> '.';
            default -> null;
        };
    }

    private static String normalizeDate(String s) {
        if (s == null) return "";
        Matcher date = DATE_LIKE.matcher(s);
        if (!date.matches()) return s;
        String month = date.group(2).toLowerCase(Locale.ROOT);
        int monthNumber = Arrays.asList(MONTHS.toLowerCase(Locale.ROOT).split("\\|"))
                .indexOf(month) + 1;
        String year = date.group(3);
        // Validate without expanding two-digit years in the returned value.
        int validationYear = year == null ? 2000 : Integer.parseInt(year);
        if (year != null && year.length() == 2) validationYear += 2000;
        try {
            LocalDate.of(validationYear, monthNumber, Integer.parseInt(date.group(1)));
        } catch (DateTimeException e) {
            return s;
        }
        month = month.substring(0, 1).toUpperCase(Locale.ROOT) + month.substring(1);
        return date.group(1) + " " + month + (year == null ? "" : " " + year);
    }
}
