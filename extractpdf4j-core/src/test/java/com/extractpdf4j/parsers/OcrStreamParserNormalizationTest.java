package com.extractpdf4j.parsers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Acceptance regressions for the numeric/date normalization issue. These deliberately
 * fail until normalization is fixed; do not disable them or assert the broken output.
 * Values mirror the six numbered normalization PDFs in examples/, with extra controls for data loss.
 *
 * Contract: amounts become ungrouped decimal strings; CR denotes a negative credit
 * under the synthetic statements' convention, without double-negating an existing
 * negative. Dates use the existing space-separated style (dd MMM yy), retaining
 * two-digit years rather than introducing an undocumented century policy. Invalid
 * dates remain unchanged. Ambiguous amounts such as 1,234 are intentionally omitted:
 * the current API has no locale argument to disambiguate them.
 *
 * Exercise both individual helpers and the actual grid normalization entry point.
 * No PDF rendering, OCR executable, or native library initialization is required.
 * Reflection follows OcrStreamParserTest so production visibility need not change.
 */
class OcrStreamParserNormalizationTest {

    static Stream<Arguments> amounts() {
        return Stream.of(
                Arguments.of("plain positive control", "1234.56", "1234.56"),
                Arguments.of("plain negative control", "-1234.56", "-1234.56"),
                Arguments.of("leading minus and grouping", "-1,234.56", "-1234.56"),
                Arguments.of("accounting parentheses", "(1,234.56)", "-1234.56"),
                Arguments.of("trailing minus", "1,234.56-", "-1234.56"),
                Arguments.of("Unicode minus U+2212", "\u22121,234.56", "-1234.56"),
                Arguments.of("currency before minus", "$-1,234.56", "-1234.56"),
                Arguments.of("minus before currency", "-$1,234.56", "-1234.56"),
                Arguments.of("parenthesized currency", "($1,234.56)", "-1234.56"),
                Arguments.of("US grouping", "1,234.56", "1234.56"),
                Arguments.of("German grouping", "1.234,56", "1234.56"),
                Arguments.of("Brazilian currency", "R$ 1.234,56", "1234.56"),
                Arguments.of("euro currency", "\u20ac 1.234,56", "1234.56"),
                Arguments.of("multiple US groups", "1,234,567.89", "1234567.89"),
                Arguments.of("multiple German groups", "1.234.567,89", "1234567.89"),
                Arguments.of("negative decimal comma", "-1.234,56", "-1234.56"),
                Arguments.of("ungrouped decimal comma control", "1234,56", "1234.56"),
                Arguments.of("regular space grouping", "1 234,56", "1234.56"),
                Arguments.of("no-break space U+00A0", "1\u00a0234,56", "1234.56"),
                Arguments.of("narrow no-break space U+202F", "1\u202f234,56", "1234.56"),
                Arguments.of("narrow space and euro suffix", "1\u202f234,56 \u20ac", "1234.56"),
                Arguments.of("ASCII apostrophe grouping", "1'234.56", "1234.56"),
                Arguments.of("curly apostrophe grouping", "1\u2019234.56", "1234.56"),
                Arguments.of("multiple narrow spaces", "1\u202f234\u202f567,89", "1234567.89"),
                Arguments.of("uppercase credit suffix", "1,234.56 CR", "-1234.56"),
                Arguments.of("attached credit suffix", "1,234.56CR", "-1234.56"),
                Arguments.of("lowercase credit suffix", "1,234.56 cr", "-1234.56"),
                Arguments.of("mixed-case credit suffix", "1,234.56 Cr", "-1234.56"),
                Arguments.of("credit prefix", "CR 1,234.56", "-1234.56"),
                Arguments.of("currency and credit", "$1,234.56 CR", "-1234.56"),
                Arguments.of("decimal comma before credit", "1.234,56 CR", "-1234.56"),
                Arguments.of("already negative credit", "-1,234.56 CR", "-1234.56"),
                Arguments.of("parenthesized credit", "(1,234.56) CR", "-1234.56"),
                Arguments.of("small credit without grouping", "0.01 CR", "-0.01")
        );
    }

    @ParameterizedTest(name = "amount helper: {0}")
    @MethodSource("amounts")
    void normalizesAmount(String scenario, String raw, String expected) throws Exception {
        assertEquals(expected, normalizeText("normalizeAmount", raw), scenario);
    }

    @ParameterizedTest(name = "numeric column detection: {0}")
    @MethodSource("amounts")
    void detectsAndNormalizesAnEntireColumnOfTheSameFormat(
            String scenario, String raw, String expected) throws Exception {
        // Three data rows + header clear the 60% threshold if the format is recognized.
        // Testing only the helper would miss formats rejected by NUM_LIKE altogether.
        List<List<String>> grid = grid(
                new String[]{"Amount"}, new String[]{raw}, new String[]{raw}, new String[]{raw});

        normalizeGrid(grid);

        assertAll(scenario,
                () -> assertEquals(expected, grid.get(1).get(0)),
                () -> assertEquals(expected, grid.get(2).get(0)),
                () -> assertEquals(expected, grid.get(3).get(0)));
    }

    static Stream<Arguments> dates() {
        return Stream.of(
                Arguments.of("hyphenated two-digit year", "01-Jan-26", "01 Jan 26"),
                Arguments.of("lowercase month", "09-feb-26", "09 Feb 26"),
                Arguments.of("uppercase month", "15-MAR-26", "15 Mar 26"),
                Arguments.of("valid leap day", "29-Feb-24", "29 Feb 24"),
                Arguments.of("previous century retained", "31-Dec-99", "31 Dec 99"),
                Arguments.of("year zero retained", "01-Jan-00", "01 Jan 00"),
                Arguments.of("four-digit year retained", "29-Feb-2024", "29 Feb 2024"),
                Arguments.of("compact date", "01Jan26", "01 Jan 26"),
                Arguments.of("existing spaced date control", "01 Jan 26", "01 Jan 26"),
                Arguments.of("whitespace control", "  01   Jan 26  ", "01 Jan 26")
        );
    }

    @ParameterizedTest(name = "date helper: {0}")
    @MethodSource("dates")
    void normalizesDate(String scenario, String raw, String expected) throws Exception {
        assertEquals(expected, normalizeText("normalizeDate", raw), scenario);
    }

    @ParameterizedTest(name = "date column detection: {0}")
    @MethodSource("dates")
    void detectsAndNormalizesAnEntireDateColumn(
            String scenario, String raw, String expected) throws Exception {
        List<List<String>> grid = grid(
                new String[]{"Date"}, new String[]{raw}, new String[]{raw}, new String[]{raw});

        normalizeGrid(grid);

        assertAll(scenario,
                () -> assertEquals(expected, grid.get(1).get(0)),
                () -> assertEquals(expected, grid.get(2).get(0)),
                () -> assertEquals(expected, grid.get(3).get(0)));
    }

    @ParameterizedTest(name = "invalid date preserved: {0}")
    @ValueSource(strings = {"29-Feb-25", "31-Apr-26", "00-Jan-26", "32-Jan-26", "01-Xyz-26"})
    void leavesInvalidDatesUnchangedInAnOtherwiseValidDateColumn(String invalid) throws Exception {
        // Spaced valid dates force the existing date branch, independent of hyphen detection.
        List<List<String>> grid = grid(new String[]{"Date"}, new String[]{"01 Jan 26"},
                new String[]{"02 Jan 26"}, new String[]{"03 Jan 26"}, new String[]{invalid});

        normalizeGrid(grid);

        assertEquals(invalid, grid.get(4).get(0), "Invalid dates must not roll over or be guessed");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    void emptyAmountsRemainEmpty(String raw) throws Exception {
        assertEquals("", normalizeText("normalizeAmount", raw));
    }

    @Test
    void numericColumnDoesNotCorruptItsHeaderOrTextAnnotations() throws Exception {
        // 3/5 numeric cells trigger normalization for the whole column today.
        List<List<String>> grid = grid(new String[]{"Balance"}, new String[]{"10.00"},
                new String[]{"20.00"}, new String[]{"30.00"}, new String[]{"CLOSING BALANCE"});

        normalizeGrid(grid);

        assertAll(
                () -> assertEquals("Balance", grid.get(0).get(0), "Header is not an OCR number"),
                () -> assertEquals("CLOSING BALANCE", grid.get(4).get(0),
                        "OCR digit substitutions must not rewrite arbitrary text"));
    }

    @Test
    void normalizesMixedFormatsWithoutChangingDescriptionsOrBlanks() throws Exception {
        List<List<String>> grid = grid(
                new String[]{"Date", "Description", "Amount"},
                new String[]{"01-Jan-26", "CR SERVICES", "123.45 CR"},
                new String[]{"02-Jan-26", "Invoice IS001", "(123.45)"},
                new String[]{"03-Jan-26", "MIXED CASE", "1.234,56"},
                new String[]{"04-Jan-26", "Space grouping", "1\u202f234,56"},
                new String[]{"05-Jan-26", "Empty amount", ""});
        List<List<String>> expected = grid(
                new String[]{"Date", "Description", "Amount"},
                new String[]{"01 Jan 26", "CR SERVICES", "-123.45"},
                new String[]{"02 Jan 26", "Invoice IS001", "-123.45"},
                new String[]{"03 Jan 26", "MIXED CASE", "1234.56"},
                new String[]{"04 Jan 26", "Space grouping", "1234.56"},
                new String[]{"05 Jan 26", "Empty amount", ""});

        normalizeGrid(grid);

        assertEquals(expected, grid);
    }

    @Test
    void repeatedNormalizationDoesNotReverseCreditsOrRewriteCanonicalValues() throws Exception {
        List<List<String>> grid = grid(
                new String[]{"Date", "Amount"},
                new String[]{"01-Jan-26", "123.45 CR"},
                new String[]{"02-Jan-26", "-123.45 CR"},
                new String[]{"03-Jan-26", "(123.45) CR"});
        List<List<String>> expected = grid(
                new String[]{"Date", "Amount"},
                new String[]{"01 Jan 26", "-123.45"},
                new String[]{"02 Jan 26", "-123.45"},
                new String[]{"03 Jan 26", "-123.45"});

        normalizeGrid(grid);
        List<List<String>> firstPass = grid.stream().<List<String>>map(ArrayList::new).toList();
        normalizeGrid(grid);

        assertAll(
                () -> assertEquals(expected, firstPass, "First pass must meet the contract"),
                () -> assertEquals(firstPass, grid, "Normalization must be idempotent"));
    }

    private static List<List<String>> grid(String[]... rows) {
        List<List<String>> grid = new ArrayList<>();
        for (String[] row : rows) grid.add(new ArrayList<>(Arrays.asList(row)));
        return grid;
    }

    private static String normalizeText(String name, String raw) throws Exception {
        Method method = OcrStreamParser.class.getDeclaredMethod(name, String.class);
        method.setAccessible(true);
        return (String) method.invoke(null, raw);
    }

    private static void normalizeGrid(List<List<String>> grid) throws Exception {
        Method method = OcrStreamParser.class.getDeclaredMethod("normalizeNumericLikeColumns", List.class);
        method.setAccessible(true);
        method.invoke(null, grid);
    }
}
