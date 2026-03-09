package com.extractpdf4j.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * CsvExporter
 *
 * <p>
 * Utility class for exporting extracted tables into CSV format with a
 * configurable delimiter.
 * </p>
 */
public class CsvExporter {

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
                    String v = table.cell(r, c);
                    if (v == null)
                        v = "";

                    boolean quote = v.contains(delimiter) || v.contains("\"") || v.contains(System.lineSeparator());
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
}
