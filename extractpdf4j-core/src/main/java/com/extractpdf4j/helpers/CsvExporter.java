package com.extractpdf4j.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * CsvExporter
 *
 * <p>
 * Utility class for exporting extracted tables into CSV format with a
 * configurable delimiter and deterministic cell whitespace normalization.
 * </p>
 */
public class CsvExporter {

    private static final Pattern HORIZONTAL_WHITESPACE = Pattern.compile("\\h+");
    private static final Pattern LINE_EDGE_SPACES = Pattern.compile(" *(\\r\\n|\\r|\\n) *");

    private String delimiter = ",";

    public void setDelimiter(String delimiter) {
        if (delimiter == null || delimiter.isEmpty()) {
            throw new IllegalArgumentException("Delimiter cannot be null or empty");
        }
        if (delimiter.length() != 1) {
            throw new IllegalArgumentException("Delimiter must be a single character");
        }
        this.delimiter = delimiter;
    }

    public String export(List<Table> data) {
        StringBuilder result = new StringBuilder();
        for (Table table : data) {
            for (int r = 0; r < table.nrows(); r++) {
                List<String> row = new ArrayList<>();
                for (int c = 0; c < table.ncols(); c++) {
                    String v = normalizeCellWhitespace(table.cell(r, c));

                    boolean quote = v.contains(delimiter)
                            || v.contains("\"")
                            || v.contains("\n")
                            || v.contains("\r");
                    if (quote) {
                        row.add("\"" + v.replace("\"", "\"\"") + "\"");
                    } else {
                        row.add(v);
                    }
                }
                result.append(String.join(delimiter, row)).append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    /**
     * Applies deterministic CSV cell whitespace rules: collapses runs of
     * horizontal whitespace to a single ASCII space, trims leading and trailing
     * whitespace (including spaces adjacent to embedded line breaks), and
     * preserves single internal spaces.
     */
    static String normalizeCellWhitespace(String value) {
        if (value == null || value.isEmpty()) {
            return "";
        }
        String collapsed = HORIZONTAL_WHITESPACE.matcher(value).replaceAll(" ").strip();
        if (collapsed.isEmpty()) {
            return "";
        }
        if (collapsed.contains("\n") || collapsed.contains("\r")) {
            collapsed = LINE_EDGE_SPACES.matcher(collapsed).replaceAll("$1").strip();
        }
        return collapsed;
    }
}
