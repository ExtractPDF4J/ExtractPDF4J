package com.extractpdf4j.helpers;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class PageRangeTest {
    @Test void parsesSingle() { assertEquals(List.of(2), PageRange.parse("2")); }
    @Test void parsesRange() { assertEquals(List.of(2,3,4,5), PageRange.parse("2-5")); }
    @Test void parsesMixed() { assertEquals(List.of(1,3,4,6), PageRange.parse("1,3-4,6")); }
    @Test void parsesAll()   { assertEquals(List.of(-1), PageRange.parse("all")); }
    @Test void defaultOne()  { assertEquals(List.of(1), PageRange.parse(null)); }

    /** Aligns with implementation: invalid tokens currently cause NumberFormatException. */
    @Test void badTokensThrow() {
        assertThrows(NumberFormatException.class, () -> PageRange.parse("1,foo,5-bar"));
    }
}
