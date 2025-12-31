package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import org.apache.pdfbox.pdmodel.PDDocument; // <-- IMPORTANTE: ADICIONAR ESTE IMPORT
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
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

        // Added to satisfy the abstract method requirement from BaseParser
        @Override
        public List<Table> parse(PDDocument document) throws IOException {
            // This test class doesn't use this method, so we can return an empty list.
            called.add(-99); 
            return Collections.emptyList();
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
    
    @Test
    void handlesEmptyResultGracefully_singlePage() throws Exception {
        class EmptyResultParser extends BaseParser {
            EmptyResultParser(String fp) { super(fp); }
            @Override
            protected List<Table> parsePage(int page) {
                return new ArrayList<>();
            }

            // Added to satisfy the abstract method requirement from BaseParser
            @Override
            public List<Table> parse(PDDocument document) throws IOException {
                // Consistent with the purpose of this test class, return an empty list.
                return Collections.emptyList();
            }
        }

        BaseParser parser = new EmptyResultParser("mock_path/empty_doc.pdf");
        List<Table> result = parser.parse();

        assertNotNull(result, "The result should not be null.");
        assertTrue(result.isEmpty(), "The result list should be empty.");
    }
    
    @Test
    void handlesEmptyResultGracefully_multiPage() throws Exception {
        class EmptyMultiPageParser extends BaseParser {
            EmptyMultiPageParser(String fp) { super(fp); }
            @Override
            protected List<Table> parsePage(int page) {
                return new ArrayList<>();
            }

            // Added to satisfy the abstract method requirement from BaseParser
            @Override
            public List<Table> parse(PDDocument document) throws IOException {
                // Consistent with the purpose of this test class, return an empty list.
                return Collections.emptyList();
            }
        }

        BaseParser parser = (BaseParser) new EmptyMultiPageParser("mock_path/multi_empty_doc.pdf")
                .pages("1,2,3,4");

        List<Table> result = parser.parse();

        assertNotNull(result, "The result should not be null.");
        assertTrue(result.isEmpty(), "The result list should be empty even if multiple pages are processed.");
    }
    
    @Test
    void handlesEmptyResultGracefully_allPages() throws Exception {
        class EmptyAllPagesParser extends BaseParser {
            EmptyAllPagesParser(String fp) { super(fp); }
            @Override
            protected List<Table> parsePage(int page) {
                return new ArrayList<>();
            }

            // Added to satisfy the abstract method requirement from BaseParser
            @Override
            public List<Table> parse(PDDocument document) throws IOException {
                // Consistent with the purpose of this test class, return an empty list.
                return Collections.emptyList();
            }
        }

        BaseParser parser = (BaseParser) new EmptyAllPagesParser("mock_path/all_empty_doc.pdf")
                .pages("all");

        List<Table> result = parser.parse();

        assertNotNull(result, "The result should not be null.");
        assertTrue(result.isEmpty(), "The result list should be empty when parsing 'all' pages of an empty document.");
    }
}