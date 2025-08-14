package com.extractpdf4j.helpers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * OCR helper utilities.
 *
 * <p>Two execution paths are supported:</p>
 * <ul>
 *   <li><b>Bytedeco (Tesseract/Leptonica) via reflection</b> — used when the native bindings
 *       are on the classpath. No hard dependency is declared; all calls are reflective so
 *       the code runs even if the jars are absent.</li>
 *   <li><b>System Tesseract CLI</b> — falls back to the {@code tesseract} executable.</li>
 * </ul>
 *
 * <h3>Mode selection (system property)</h3>
 * <ul>
 *   <li>{@code -Docr=auto} (default) — try Bytedeco first, then CLI.</li>
 *   <li>{@code -Docr=cli} — force CLI.</li>
 *   <li>{@code -Docr=bytedeco} — force Bytedeco (returns empty string if unavailable).</li>
 * </ul>
 */
public final class Ocr {
    private Ocr() {}

    /**
     * Runs OCR on a PNG file and returns plain text.
     *
     * <p>Mode is controlled by {@code -Docr} (see class docs). In {@code auto} mode, this
     * method prefers the Bytedeco path and falls back to the CLI if necessary.</p>
     *
     * @param pngPath path to a PNG image
     * @return recognized text (never {@code null}); may be empty on failure
     */
    public static String ocrPng(String pngPath) {
        String mode = System.getProperty("ocr", "auto");

        if ("cli".equalsIgnoreCase(mode)) return tryCli(pngPath);

        if ("bytedeco".equalsIgnoreCase(mode)) {
            String out = tryBytedeco(pngPath);
            return out != null ? out : "";
        }

        // auto: prefer Bytedeco, then CLI
        String out = tryBytedeco(pngPath);
        if (out != null && !out.isEmpty()) return out;
        return tryCli(pngPath);
    }

    // ---------------------------------------------------------------------
    // Bytedeco path (reflection; safe when jars are missing)
    // ---------------------------------------------------------------------

    /**
     * Attempts OCR via Bytedeco Tesseract/Leptonica using reflection.
     *
     * @param pngPath path to a PNG image
     * @return recognized text; {@code null} if classes are missing; empty string on failure
     */
    static String tryBytedeco(String pngPath) {
        Object api = null, image = null, bytePtr = null;
        try {
            Class<?> tessBaseApi = Class.forName("org.bytedeco.tesseract.TessBaseAPI");
            Class<?> pixCls      = Class.forName("org.bytedeco.leptonica.PIX");
            Class<?> leptGlobal  = Class.forName("org.bytedeco.leptonica.global.lept");

            api = tessBaseApi.getDeclaredConstructor().newInstance();
            int rc = (int) tessBaseApi.getMethod("Init", String.class, String.class)
                    .invoke(api, (String) null, "eng");
            if (rc != 0) { safeCall(api, "End"); return ""; }

            // PSM 6 (Assume a single uniform block of text) and DPI hint
            try { tessBaseApi.getMethod("SetPageSegMode", int.class).invoke(api, 6); } catch (NoSuchMethodException ignore) {}
            try { tessBaseApi.getMethod("SetVariable", String.class, String.class).invoke(api, "user_defined_dpi", "300"); } catch (Throwable ignore) {}

            image = leptGlobal.getMethod("pixRead", String.class).invoke(null, pngPath);
            if (image == null) { safeCall(api, "End"); return ""; }
            tessBaseApi.getMethod("SetImage", pixCls).invoke(api, image);

            bytePtr = tessBaseApi.getMethod("GetUTF8Text").invoke(api);
            String text = (bytePtr == null) ? "" : (String) bytePtr.getClass().getMethod("getString").invoke(bytePtr);
            text = (text == null) ? "" : text.trim();

            safeDeallocate(bytePtr);
            safePixDestroy(leptGlobal, pixCls, image);
            safeCall(api, "End");
            return text;
        } catch (ClassNotFoundException e) {
            return null; // not on classpath → let caller try CLI
        } catch (Throwable t) {
            return "";   // on classpath but failed → empty string
        } finally {
            try { safeDeallocate(bytePtr); } catch (Throwable ignore) {}
            try { if (api != null) safeCall(api, "End"); } catch (Throwable ignore) {}
            try {
                if (image != null) {
                    Class<?> leptGlobal = Class.forName("org.bytedeco.leptonica.global.lept");
                    Class<?> pixCls     = Class.forName("org.bytedeco.leptonica.PIX");
                    safePixDestroy(leptGlobal, pixCls, image);
                }
            } catch (Throwable ignore) {}
        }
    }

    // ---------------------------------------------------------------------
    // CLI path (requires system tesseract installed)
    // ---------------------------------------------------------------------

    /**
     * Attempts OCR using the {@code tesseract} command-line tool.
     *
     * @param pngPath path to a PNG image
     * @return recognized text (empty if CLI is unavailable or fails)
     */
    static String tryCli(String pngPath) {
        try {
            Process p = new ProcessBuilder("tesseract", pngPath, "stdout", "-l", "eng", "--psm", "6")
                    .redirectErrorStream(true).start();
            p.waitFor(20, TimeUnit.SECONDS);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return ""; // CLI not available
        }
    }

    // ---------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------

    /** Safely invokes a no-arg method by name, ignoring exceptions. */
    private static void safeCall(Object obj, String name) { try { obj.getClass().getMethod(name).invoke(obj); } catch (Throwable ignore) {} }

    /** Tries common deallocation patterns for JavaCPP pointer types. */
    private static void safeDeallocate(Object bp) {
        if (bp == null) return;
        try { bp.getClass().getMethod("deallocate").invoke(bp); return; } catch (Throwable ignore) {}
        try { bp.getClass().getMethod("close").invoke(bp); } catch (Throwable ignore) {}
    }

    /** Destroys a Leptonica PIX object using reflection, supporting both signatures. */
    private static void safePixDestroy(Class<?> leptGlobal, Class<?> pixCls, Object pix) {
        try {
            leptGlobal.getMethod("pixDestroy", pixCls).invoke(null, pix);
        } catch (NoSuchMethodException e1) {
            try {
                Object arr = java.lang.reflect.Array.newInstance(pixCls, 1);
                java.lang.reflect.Array.set(arr, 0, pix);
                leptGlobal.getMethod("pixDestroy", arr.getClass()).invoke(null, arr);
            } catch (Throwable ignore) {}
        } catch (Throwable ignore) {}
    }

    // ---------------------------------------------------------------------
    // TSV parsing (CLI only)
    // ---------------------------------------------------------------------

    /** Simple value object for OCR words parsed from Tesseract TSV output. */
    public static class OcrWord {
        public final int left, top, width, height, conf;
        public final String text;
        public final int block, par, line, word;

        public OcrWord(int left,int top,int width,int height,int conf,String text,int block,int par,int line,int word){
            this.left=left; this.top=top; this.width=width; this.height=height; this.conf=conf; this.text=text;
            this.block=block; this.par=par; this.line=line; this.word=word;
        }
    }

    /**
     * Runs Tesseract in TSV mode and parses word-level results.
     *
     * <p>Configurable via system properties:</p>
     * <ul>
     *   <li>{@code -Dtess.lang} (default {@code eng})</li>
     *   <li>{@code -Dtess.psm}  (default {@code 6})</li>
     *   <li>{@code -Dtess.oem}  (default {@code 1})</li>
     *   <li>{@code -Docr.debug=true} to dump raw TSV on empty output</li>
     * </ul>
     *
     * <p>Searches a few common locations for the {@code tesseract} binary to tolerate
     * missing PATH entries in dev environments.</p>
     *
     * @param pngPath path to a PNG image
     * @return list of words (possibly empty)
     */
    public static java.util.List<OcrWord> ocrTsv(String pngPath) {
        java.util.List<OcrWord> out = new java.util.ArrayList<>();

        String lang = System.getProperty("tess.lang", "eng");
        String psm  = System.getProperty("tess.psm",  "6");
        String oem  = System.getProperty("tess.oem",  "1");
        boolean dbg = Boolean.parseBoolean(System.getProperty("ocr.debug", "false"));

        String[] candidates = {
                "tesseract",
                "/opt/homebrew/bin/tesseract",   // macOS Apple Silicon (brew)
                "/usr/local/bin/tesseract",      // macOS Intel / some Linux
                "/usr/bin/tesseract",            // Linux
                "C:\\\\Program Files\\\\Tesseract-OCR\\\\tesseract.exe" // Windows
        };

        IOException last = null;
        for (String bin : candidates) {
            try {
                Process p = new ProcessBuilder(
                        bin, pngPath, "stdout",
                        "-l", lang,
                        "--oem", oem,
                        "--psm", psm,
                        "-c", "preserve_interword_spaces=1",
                        "-c", "user_defined_dpi=300",
                        "tsv"
                ).redirectErrorStream(true).start();

                StringBuilder all = new StringBuilder(64_000);
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String header = br.readLine(); // may be null if tesseract errored
                    if (header != null) all.append(header).append('\n');
                    String line;
                    while ((line = br.readLine()) != null) {
                        all.append(line).append('\n');
                        String[] t = line.split("\t", -1);
                        if (t.length < 12) continue;
                        int level = parseIntSafe(t[0]);
                        if (level != 5) continue; // word rows only
                        int block = parseIntSafe(t[2]), par = parseIntSafe(t[3]), ln = parseIntSafe(t[4]), wd = parseIntSafe(t[5]);
                        int left = parseIntSafe(t[6]), top = parseIntSafe(t[7]),
                                width = parseIntSafe(t[8]), height = parseIntSafe(t[9]);
                        int conf = parseIntSafe(t[10]);
                        String text = t[11] == null ? "" : t[11].trim();
                        if (!text.isEmpty()) out.add(new OcrWord(left, top, width, height, conf, text, block, par, ln, wd));
                    }
                }
                p.waitFor();

                if (out.isEmpty() && dbg) {
                    java.io.File dump = new java.io.File("debug/ocr_last.tsv");
                    dump.getParentFile().mkdirs();
                    try (java.io.FileWriter fw = new java.io.FileWriter(dump)) { fw.write(all.toString()); }
                    System.err.println("OCR TSV had 0 words. Dumped raw TSV to: " + dump.getAbsolutePath());
                    System.err.println("Used tesseract: " + bin + " (psm=" + psm + ", oem=" + oem + ", lang=" + lang + ")");
                }
                return out; // success (even if empty)
            } catch (IOException e) {
                last = e; // try the next candidate
            } catch (Exception ignore) {
                // fall through, try next candidate
            }
        }

        if (last != null && Boolean.parseBoolean(System.getProperty("ocr.debug","false"))) {
            System.err.println("All tesseract candidates failed: " + last);
        }
        return out;
    }

    private static int parseIntSafe(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }
}
