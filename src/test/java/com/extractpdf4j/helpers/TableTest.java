package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

class TableTest {
    @Test
    void nrowsNcolsAndCell() {
        List<List<String>> cells = new ArrayList<>();
        cells.add(new ArrayList<>(List.of("a","b")));
        cells.add(new ArrayList<>(List.of("c","")));
        Table t = new Table(cells, List.of(0.0,10.0,20.0), List.of(0.0,10.0,20.0));
        assertEquals(2, t.nrows());
        assertEquals(2, t.ncols());
        assertEquals("a", t.cell(0,0));
        t.setCell(1,1,"x");
        assertEquals("x", t.cell(1,1));
    }

    @Test
    void csvQuoting() {
        Table t = new Table(List.of(
                new ArrayList<>(List.of("a","b,c","d\"e", "x\ny"))),
                List.of(0.0, 10.0, 20.0, 30.0, 40.0),
                List.of(0.0, 10.0));
        String csv = t.toCSV(',');
        assertEquals("a,\"b,c\",\"d\"\"e\",\"x\ny\"", csv);
    }

    @Test
    void asListIsUnmodifiable() {
        Table t = new Table(List.of(new ArrayList<>(List.of("z"))), List.of(0.0,1.0), List.of(0.0,1.0));
        var copy = t.asList();
        assertThrows(UnsupportedOperationException.class, () -> copy.add(List.of("x")));
        assertThrows(UnsupportedOperationException.class, () -> copy.get(0).add("x"));
    }
}
