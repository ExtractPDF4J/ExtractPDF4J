package com.extractpdf4j.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OCR helper utilities.
 * (Javadocs of class and methods updated to reflect new heuristic functionality.)
 */
public final class Ocr {
    private Ocr() {}

    /**
     * Runs OCR on a PNG image using a heuristic to find the best Page Segmentation Mode (PSM).
     * It tries a predefined list of PSMs, finds the one with the best word coverage,
     * and prints a summary of the best combination found.
     *
     * @param pngPath Path to the PNG image.
     * @param lang    Language string for Tesseract (e.g., "eng", "por", "eng+fra").
     * @return A list of OCR words (possibly empty).
     */
    public static List<OcrWord> ocrTsvHeuristically(String pngPath, String lang) {
        // The issue requested trying PSMs 6, 4, and 11. PSM 3 is also a good default.
        String[] psmsToTry = {"6", "4", "11", "3"};

        List<OcrWord> bestResult = new ArrayList<>();
        String bestPsm = ""; // Variable to store the best performing PSM

        for (String psm : psmsToTry) {
            List<OcrWord> currentResult = ocrTsv(pngPath, lang, psm);

            // Acceptance criteria: "increased TSV coverage".
            // We will use the word count as the metric.
            if (currentResult.size() > bestResult.size()) {
                bestResult = currentResult;
                bestPsm = psm; // Store the psm that gave the best result
            }
        }
        
        // Print the custom summary log if a best result was found
        if (!bestPsm.isEmpty()) {
            String fileName = new File(pngPath).getName();
            int wordCount = bestResult.size();

            String summary1 = String.format(
                "[OCR] Best combo for %s → lang=%s, psm=%s (%d words)",
                fileName, lang, bestPsm, wordCount
            );
            String summary2 = String.format(
                "✅ Detected language: %s | PSM: %s | Words recognized: %d",
                lang, bestPsm, wordCount
            );
            
            System.out.println(summary1);
            System.out.println(summary2);
        }
        
        // Debug logging
        if (Boolean.parseBoolean(System.getProperty("ocr.debug", "false"))) {
             if (bestResult.isEmpty()) {
                System.err.println("OCR heuristic for " + pngPath + " found 0 words.");
            }
        }
        
        return bestResult;
    }

    /**
     * Runs OCR on a PNG file and returns plain text. (Legacy method)
     * NOTE: This method does not use the new heuristic.
     */
    public static String ocrPng(String pngPath) {
        String mode = System.getProperty("ocr", "auto");
        String lang = "eng";
        int psm = 6;
        
        if ("cli".equalsIgnoreCase(mode)) {
            return tryCli(pngPath, lang, String.valueOf(psm));
        }

        if ("bytedeco".equalsIgnoreCase(mode)) {
            String out = tryBytedeco(pngPath, lang, psm);
            return out != null ? out : "";
        }

        String out = tryBytedeco(pngPath, lang, psm);
        if (out != null && !out.isEmpty()) return out;
        
        return tryCli(pngPath, lang, String.valueOf(psm));
    }



    static String tryBytedeco(String pngPath, String lang, int psm) {
        Object api = null, image = null, bytePtr = null;
        try {
            Class<?> tessBaseApi = Class.forName("org.bytedeco.tesseract.TessBaseAPI");
            Class<?> pixCls      = Class.forName("org.bytedeco.leptonica.PIX");
            Class<?> leptGlobal  = Class.forName("org.bytedeco.leptonica.global.lept");

            api = tessBaseApi.getDeclaredConstructor().newInstance();
            int rc = (int) tessBaseApi.getMethod("Init", String.class, String.class)
                    .invoke(api, (String) null, lang);
            if (rc != 0) { safeCall(api, "End"); return ""; }

            try { tessBaseApi.getMethod("SetPageSegMode", int.class).invoke(api, psm); } catch (NoSuchMethodException ignore) {}
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
            return null;
        } catch (Throwable t) {
            return "";
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

    static String tryCli(String pngPath, String lang, String psm) {
        try {
            Process p = new ProcessBuilder("tesseract", pngPath, "stdout", "-l", lang, "--psm", psm)
                    .redirectErrorStream(true).start();
            p.waitFor(20, TimeUnit.SECONDS);
            StringBuilder sb = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
                String line; while ((line = br.readLine()) != null) sb.append(line).append('\n');
            }
            return sb.toString().trim();
        } catch (Exception e) {
            return "";
        }
    }

    private static void safeCall(Object obj, String name) { try { obj.getClass().getMethod(name).invoke(obj); } catch (Throwable ignore) {} }

    private static void safeDeallocate(Object bp) {
        if (bp == null) return;
        try { bp.getClass().getMethod("deallocate").invoke(bp); return; } catch (Throwable ignore) {}
        try { bp.getClass().getMethod("close").invoke(bp); } catch (Throwable ignore) {}
    }

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

    public static class OcrWord {
        public final int left, top, width, height, conf;
        public final String text;
        public final int block, par, line, word;

        public OcrWord(int left,int top,int width,int height,int conf,String text,int block,int par,int line,int word){
            this.left=left; this.top=top; this.width=width; this.height=height; this.conf=conf; this.text=text;
            this.block=block; this.par=par; this.line=line; this.word=word;
        }
    }

    public static List<OcrWord> ocrTsv(String pngPath) {
        String lang = System.getProperty("tess.lang", "eng");
        String psm  = System.getProperty("tess.psm",  "6");
        return ocrTsv(pngPath, lang, psm);
    }
    
    public static java.util.List<OcrWord> ocrTsv(String pngPath, String lang, String psm) {
        java.util.List<OcrWord> out = new java.util.ArrayList<>();
        String oem  = System.getProperty("tess.oem",  "1");
        boolean dbg = Boolean.parseBoolean(System.getProperty("ocr.debug", "false"));

        String[] candidates = {
                "tesseract", "/opt/homebrew/bin/tesseract", "/usr/local/bin/tesseract", 
                "/usr/bin/tesseract", "C:\\\\Program Files\\\\Tesseract-OCR\\\\tesseract.exe"
        };

        IOException last = null;
        for (String bin : candidates) {
            try {
                Process p = new ProcessBuilder(
                        bin, pngPath, "stdout", "-l", lang, "--oem", oem, "--psm", psm,
                        "-c", "preserve_interword_spaces=1", "-c", "user_defined_dpi=300", "tsv"
                ).redirectErrorStream(true).start();

                StringBuilder all = new StringBuilder(64_000);
                try (java.io.BufferedReader br = new java.io.BufferedReader(
                        new java.io.InputStreamReader(p.getInputStream(), java.nio.charset.StandardCharsets.UTF_8))) {
                    String header = br.readLine();
                    if (header != null) all.append(header).append('\n');
                    String line;
                    while ((line = br.readLine()) != null) {
                        all.append(line).append('\n');
                        String[] t = line.split("\t", -1);
                        if (t.length < 12) continue;
                        if (parseIntSafe(t[0]) != 5) continue;
                        int block = parseIntSafe(t[2]), par = parseIntSafe(t[3]), ln = parseIntSafe(t[4]), wd = parseIntSafe(t[5]);
                        int left = parseIntSafe(t[6]), top = parseIntSafe(t[7]), width = parseIntSafe(t[8]), height = parseIntSafe(t[9]);
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
                return out;
            } catch (IOException e) {
                last = e;
            } catch (Exception ignore) {}
        }

        if (last != null && dbg) {
            System.err.println("All tesseract candidates failed: " + last);
        }
        return out;
    }

    private static int parseIntSafe(String s){ try { return Integer.parseInt(s.trim()); } catch(Exception e){ return 0; } }
}