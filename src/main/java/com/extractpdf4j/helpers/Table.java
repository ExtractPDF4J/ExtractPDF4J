package com.extractpdf4j.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Table
 *
 * <p>Immutable-style value object describing a rectangular grid of cell text
 * along with the column/row boundary positions (in pixel or point units
 * depending on the parser). Cell lists are defensively copied on construction.</p>
 */
public class Table {
    private final List<List<String>> cells;
    private final List<Double> colBoundaries;
    private final List<Double> rowBoundaries;

    public Table(List<List<String>> cells, List<Double> colBoundaries, List<Double> rowBoundaries) {
        this.cells = new ArrayList<>();
        for (List<String> row : cells) this.cells.add(new ArrayList<>(row));
        this.colBoundaries = new ArrayList<>(colBoundaries);
        this.rowBoundaries = new ArrayList<>(rowBoundaries);
    }

    /** Number of rows in the table. */

    public int nrows() { return cells.size(); }

    /** Number of columns in the table (0 if there are no rows). */
    public int ncols() { return cells.isEmpty() ? 0 : cells.get(0).size(); }

    /** Returns the cell value at row {@code r}, column {@code c}. */
    public String cell(int r, int c) { return cells.get(r).get(c); }

    /** Mutates the cell at row {@code r}, column {@code c} with value {@code v}. */
    public void setCell(int r, int c, String v) { cells.get(r).set(c, v); }

    /**
     * Returns an unmodifiable deep copy view of the cells.
     * Each inner row list is unmodifiable to prevent external mutation.
     */
    public List<List<String>> asList() {
        List<List<String>> copy = new ArrayList<>();
        for (List<String> row : cells) copy.add(Collections.unmodifiableList(row));
        return Collections.unmodifiableList(copy);
    }

    /**
     * Serializes the table to CSV using the given separator.
     * Fields containing the separator, double quotes, or newlines are quoted;
     * embedded quotes are escaped as a pair of quotes. No trailing newline is added.
     */
    public String toCSV(char sep) {
        StringBuilder sb = new StringBuilder();
        for (int r=0;r<nrows();r++) {
            for (int c=0;c<ncols();c++) {
                String v = cells.get(r).get(c);
                if (v == null) v = "";
                boolean quote = v.indexOf(sep) >= 0 || v.contains("\"") || v.contains("\n");
                if (quote) sb.append('"').append(v.replace("\"", "\"\"")).append('"');
                else sb.append(v);
                if (c < ncols()-1) sb.append(sep);
            }
            if (r < nrows()-1) sb.append('\n');
        }
        return sb.toString();
    }
    public List<Double> getColBoundaries(){ return Collections.unmodifiableList(colBoundaries); }
    public List<Double> getRowBoundaries(){ return Collections.unmodifiableList(rowBoundaries); }
}
