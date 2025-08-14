package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/** Tests paging/selection behavior of BaseParser using a local fake subclass. */
class BaseParserTest {

    static Table t(String v){
        List<List<String>> cells = new ArrayList<>();
        List<String> row = new ArrayList<>();
        row.add(v);
        cells.add(row);
        return new Table(cells, List.of(0.0, 1.0), List.of(0.0, 1.0));
    }

    static class FakeParser extends BaseParser {
        final List<Integer> called = new ArrayList<>();
        FakeParser(String fp){ super(fp); }
        @Override protected List<Table> parsePage(int page) throws IOException {
            called.add(page);
            if (page == -1) return List.of(t("ALL"));
            return List.of(t("P"+page));
        }
    }

    @Test
    void parsesSpecificPages() throws Exception {
        FakeParser fp = (FakeParser) new FakeParser("x.pdf").pages("1,3-4");
        List<Table> out = fp.parse();
        assertEquals(List.of("P1","P3","P4"),
                out.stream().map(t -> t.cell(0,0)).toList());
        assertEquals(List.of(1,3,4), fp.called);
    }

    @Test
    void parsesAllPagesViaMinusOne() throws Exception {
        FakeParser fp = (FakeParser) new FakeParser("x.pdf").pages("all");
        List<Table> out = fp.parse();
        assertEquals(1, out.size());
        assertEquals("ALL", out.get(0).cell(0,0));
        assertEquals(List.of(-1), fp.called);
    }
}
