package com.extractpdf4j.helpers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** Structured diagnostic metadata captured while extracting tables from a PDF. */
public class ExtractionDiagnostics {
    private final ParserSelection parserSelected;
    private final String reason;
    private final Duration totalDuration;
    private final Duration ocrDuration;
    private final Duration parsingDuration;
    private final int pagesProcessed;
    private final int tablesDetected;
    private final List<String> warnings;
    private final List<String> fallbackActions;

    public ExtractionDiagnostics(ParserSelection parserSelected, String reason, Duration totalDuration,
                                 Duration ocrDuration, Duration parsingDuration, int pagesProcessed,
                                 int tablesDetected, List<String> warnings, List<String> fallbackActions) {
        this.parserSelected = parserSelected;
        this.reason = reason == null ? "Diagnostics not available" : reason;
        this.totalDuration = totalDuration == null ? Duration.ZERO : totalDuration;
        this.ocrDuration = ocrDuration == null ? Duration.ZERO : ocrDuration;
        this.parsingDuration = parsingDuration == null ? Duration.ZERO : parsingDuration;
        this.pagesProcessed = Math.max(0, pagesProcessed);
        this.tablesDetected = Math.max(0, tablesDetected);
        this.warnings = immutableCopy(warnings);
        this.fallbackActions = immutableCopy(fallbackActions);
    }

    private static List<String> immutableCopy(List<String> values) {
        return values == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(values));
    }

    public ParserSelection parserSelected() { return parserSelected; }
    public String reason() { return reason; }
    public Duration totalDuration() { return totalDuration; }
    public Duration ocrDuration() { return ocrDuration; }
    public Duration parsingDuration() { return parsingDuration; }
    public int pagesProcessed() { return pagesProcessed; }
    public int tablesDetected() { return tablesDetected; }
    public List<String> warnings() { return warnings; }
    public List<String> fallbackActions() { return fallbackActions; }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Parser selected: ").append(parserSelected).append(System.lineSeparator());
        sb.append("Reason: ").append(reason).append(System.lineSeparator());
        sb.append("Pages processed: ").append(pagesProcessed).append(System.lineSeparator());
        sb.append("Tables detected: ").append(tablesDetected).append(System.lineSeparator());
        sb.append("OCR duration: ").append(formatDuration(ocrDuration)).append(System.lineSeparator());
        sb.append("Parsing duration: ").append(formatDuration(parsingDuration)).append(System.lineSeparator());
        sb.append("Total duration: ").append(formatDuration(totalDuration));
        if (!warnings.isEmpty()) {
            sb.append(System.lineSeparator()).append("Warnings:");
            for (String warning : warnings) sb.append(System.lineSeparator()).append("- ").append(warning);
        }
        if (!fallbackActions.isEmpty()) {
            sb.append(System.lineSeparator()).append("Fallback actions:");
            for (String fallbackAction : fallbackActions) sb.append(System.lineSeparator()).append("- ").append(fallbackAction);
        }
        return sb.toString();
    }

    private static String formatDuration(Duration duration) {
        long millis = duration.toMillis();
        if (millis < 1000) return millis + " ms";
        return String.format(Locale.ROOT, "%.2f seconds", millis / 1000.0);
    }
}