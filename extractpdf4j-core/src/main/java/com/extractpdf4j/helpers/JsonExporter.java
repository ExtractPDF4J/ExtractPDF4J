package com.extractpdf4j.helpers;

import java.util.List;

/**
 * Serializes extracted tables as a JSON document.
 *
 * <p>The document has a {@code tables} array. Each table contains a {@code rows}
 * array whose items are arrays of cell values, preserving the original table
 * order and cell boundaries.</p>
 */
public final class JsonExporter {

    private JsonExporter() {
    }

    /**
     * Exports tables as a JSON document suitable for command-line pipelines.
     * Null cell values are represented as empty strings, matching CSV export.
     *
     * @param tables tables to serialize
     * @return a JSON document containing all tables
     */
    public static String export(List<Table> tables) {
        StringBuilder json = new StringBuilder("{\"tables\":[");
        for (int tableIndex = 0; tableIndex < tables.size(); tableIndex++) {
            if (tableIndex > 0) {
                json.append(',');
            }
            appendTable(json, tables.get(tableIndex));
        }
        return json.append("]}").toString();
    }

    private static void appendTable(StringBuilder json, Table table) {
        json.append("{\"rows\":[");
        for (int row = 0; row < table.nrows(); row++) {
            if (row > 0) {
                json.append(',');
            }
            json.append('[');
            for (int column = 0; column < table.ncols(); column++) {
                if (column > 0) {
                    json.append(',');
                }
                appendString(json, table.cell(row, column));
            }
            json.append(']');
        }
        json.append("]}");
    }

    private static void appendString(StringBuilder json, String value) {
        json.append('"');
        String text = value == null ? "" : value;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            switch (character) {
                case '"':
                    json.append("\\\"");
                    break;
                case '\\':
                    json.append("\\\\");
                    break;
                case '\b':
                    json.append("\\b");
                    break;
                case '\f':
                    json.append("\\f");
                    break;
                case '\n':
                    json.append("\\n");
                    break;
                case '\r':
                    json.append("\\r");
                    break;
                case '\t':
                    json.append("\\t");
                    break;
                default:
                    if (character < 0x20) {
                        json.append(String.format("\\u%04x", (int) character));
                    } else {
                        json.append(character);
                    }
            }
        }
        json.append('"');
    }
}
