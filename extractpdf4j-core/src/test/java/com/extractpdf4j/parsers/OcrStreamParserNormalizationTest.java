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
 * Acceptance regressions for the numeric/date normalization issue.
 * Values mirror the six numbered normalization PDFs in examples/, with extra controls for data loss.
 *
 * Contract: amounts become ungrouped decimal strings; CR denotes a negative credit
 * under the synthetic statements' convention, without double-negating an existing
 * negative. Dates use the existing space-separated style (dd MMM yy), retaining
 * two-digit years rather than introducing an undocumented century policy. Invalid
 * dates remain unchanged. Ambiguous amounts such as 1,234 require an explicit
 * source Locale column or a supported currency hint; otherwise they are preserved.
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
                Arguments.of("small credit without grouping", "0.01 CR", "-0.01"),
                Arguments.of("explicit positive", "+1,234.56", "1234.56"),
                Arguments.of("omitted leading zero", ".50", "0.50"),
                Arguments.of("decimal comma without leading zero", ",50", "0.50"),
                Arguments.of("negative fractional credit", "-.50 CR", "-0.50"),
                Arguments.of("three decimal places without integer", ".123", "0.123"),
                Arguments.of("Swiss currency code", "CHF 1'234.56", "1234.56"),
                Arguments.of("spaced parentheses", "( 1.234,56 )", "-1234.56"),
                Arguments.of("negative narrow-space amount", "\u22121\u202f234,56", "-1234.56"),
                Arguments.of("Brazilian grouping hint", "R$ 1.234", "1234"),
                Arguments.of("Brazilian decimal hint", "R$ 1,234", "1.234"),
                Arguments.of("Brazilian code hint", "BRL 1.234", "1234"),
                Arguments.of("US grouping hint", "USD 1,234", "1234"),
                Arguments.of("US decimal hint", "USD 1.234", "1.234"),
                Arguments.of("qualified dollar symbol", "US$ 1,234", "1234"),
                Arguments.of("sterling grouping hint", "\u00a3 1,234", "1234"),
                Arguments.of("sterling code hint", "GBP 1.234", "1.234"),
                Arguments.of("currency hint with credit", "BRL 1.234 CR", "-1234"),
                Arguments.of("currency hint with parentheses", "(USD 1,234)", "-1234"),
                Arguments.of("currency suffix hint", "1,234 USD", "1234"),
                Arguments.of("minus before euro suffix", "1.234,56- \u20ac", "-1234.56"),
                Arguments.of("minus after euro suffix", "1.234,56 \u20ac-", "-1234.56"),
                Arguments.of("spaced trailing minus", "1,234.56 -", "-1234.56"),
                Arguments.of("unicode minus before suffix", "1.234,56 \u2212 EUR", "-1234.56"),
                Arguments.of("fullwidth minus", "\uff0d1,234.56", "-1234.56"),
                Arguments.of("currency outside parentheses", "$ (1,234.56)", "-1234.56"),
                Arguments.of("suffix outside parentheses", "(1.234,56) EUR", "-1234.56"),
                Arguments.of("parentheses and credit", "USD (1,234.56) CR", "-1234.56"),
                Arguments.of("Canadian dollar symbol", "CA$ 1,234.56", "1234.56"),
                Arguments.of("short Canadian dollar symbol", "C$1,234.56", "1234.56"),
                Arguments.of("Australian dollar symbol", "AU$1,234.56", "1234.56"),
                Arguments.of("short Australian symbol", "A$1,234.56", "1234.56"),
                Arguments.of("New Zealand dollar symbol", "NZ$1,234.56", "1234.56"),
                Arguments.of("Hong Kong dollar symbol", "HK$1,234.56", "1234.56"),
                Arguments.of("Singapore dollar symbol", "SG$1,234.56", "1234.56"),
                Arguments.of("Canadian code decimal comma", "CAD 1 234,56", "1234.56"),
                Arguments.of("rupee Indian grouping", "\u20b91,23,456.78", "123456.78"),
                Arguments.of("Indian code multiple groups", "INR 12,34,56,789.01", "123456789.01"),
                Arguments.of("Indian grouping credit", "INR 1,23,456.78 CR", "-123456.78"),
                Arguments.of("yen symbol", "\u00a5 1234", "1234"),
                Arguments.of("yen code", "JPY 1234", "1234"),
                Arguments.of("Kuwaiti three decimals", "KWD 1234.567", "1234.567"),
                Arguments.of("ruble symbol", "\u20bd 1 234,56", "1234.56"),
                Arguments.of("matching symbol and code", "$ 1,234 USD", "1234"),
                Arguments.of("matching ruble symbol and code", "\u20bd 1 234,56 RUB", "1234.56"),
                Arguments.of("matching won symbol and code", "\u20a9 1234 KRW", "1234"),
                Arguments.of("currency code containing CR", "SCR 1234.56", "1234.56"),
                Arguments.of("currency code starting CR", "CRC 1234.56", "1234.56"),
                Arguments.of("credit with CR in currency code", "SCR 1234.56 CR", "-1234.56"),
                Arguments.of("credit attached to Canadian code", "CRCAD1234.56", "-1234.56"),
                Arguments.of("credit attached to Swiss code", "CRCHF1234.56", "-1234.56"),
                Arguments.of("credit attached to CRC currency", "CRCRC1234.56", "-1234.56")
        );
    }

    @ParameterizedTest(name = "amount helper: {0}")
    @MethodSource("amounts")
    void normalizesAmount(String scenario, String raw, String expected) throws Exception {
        assertEquals(expected, normalizeText("normalizeAmount", raw), scenario);
    }

    @ParameterizedTest(name = "numeric recognition: {0}")
    @MethodSource("amounts")
    void recognizesSupportedAmountFormats(String scenario, String raw, String expected) throws Exception {
        assertEquals(true, isLikelyNumeric(raw), scenario);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "$", "CR", "R", "C", "--123.45", "(123.45", "123.45)",
            "12,34,56", "123CR456", "REF123", "1234 USD extra", "01-Jan-26", "1''234.56",
            "+123.45-", "-123.45-", "$-123.45-", "R$1.23 USD", "EUR 1.23 USD",
            "ZZZ 123.45", "1,2,345.67"})
    void rejectsNonAmountsAndMalformedTokens(String raw) throws Exception {
        assertEquals(false, isLikelyNumeric(raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"+123.45-", "-123.45-", "$-123.45-", "R$1.23 USD", "EUR 1.23 USD",
            "ZZZ 123.45", "1,2,345.67"})
    void leavesMalformedOrConflictingAmountsUnchanged(String raw) throws Exception {
        assertEquals(raw, normalizeText("normalizeAmount", raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"1,234", "1.234", "$1,234", "1.234 CR", "\u20ac 1.234", "EUR 1,234",
            "CHF 1.234", "R$ 1.234 USD", "USD 1,234 EUR", "CAD 1,234", "CA$1,234"})
    void preservesAmountsThatNeedLocaleContext(String raw) throws Exception {
        assertEquals(raw, normalizeText("normalizeAmount", raw));
    }

    @ParameterizedTest
    @ValueSource(strings = {"R$ 1,234", "USD 1,234", "BRL 1.234 CR", "(USD 1,234)"})
    void currencyHintNormalizationIsIdempotent(String raw) throws Exception {
        String once = normalizeText("normalizeAmount", raw);
        assertEquals(once, normalizeText("normalizeAmount", once));
    }

    static Stream<Arguments> sourceLocales() {
        return Stream.of(
                Arguments.of("en-US", "1,234", "1234"),
                Arguments.of("de-DE", "1,234", "1.234"),
                Arguments.of("de-DE", "1.234", "1234"),
                Arguments.of("en-US", "1.234", "1.234"),
                Arguments.of("pt_BR", "R$ 1.234", "1234"),
                Arguments.of("de-DE", "USD 1,234", "1.234"),
                Arguments.of("en-US", "BRL 1,234", "1234"),
                Arguments.of("fr-CA", "CAD 1,234", "1.234"),
                Arguments.of("en-CA", "CAD 1,234", "1234"),
                Arguments.of("de-DE", "1.234- EUR", "-1234")
        );
    }

    @ParameterizedTest(name = "source locale {0}: {1}")
    @MethodSource("sourceLocales")
    void usesExplicitLocaleColumnBeforeCurrencyHints(String locale, String raw, String expected) throws Exception {
        List<List<String>> rows = grid(new String[]{"Locale", "Amount"},
                new String[]{locale, raw}, new String[]{locale, raw}, new String[]{locale, raw});
        normalizeGrid(rows);
        assertAll(
                () -> assertEquals(expected, rows.get(1).get(1)),
                () -> assertEquals(expected, rows.get(2).get(1)),
                () -> assertEquals(expected, rows.get(3).get(1)),
                () -> assertEquals(locale, rows.get(1).get(0)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "en", "xx-YY", "de-DE / description", "not a locale"})
    void doesNotGuessFromMissingOrUnsupportedLocale(String locale) throws Exception {
        List<List<String>> rows = grid(new String[]{"Locale", "Amount"},
                new String[]{locale, "1,234"}, new String[]{locale, "1,234"},
                new String[]{locale, "1,234"});
        normalizeGrid(rows);
        assertEquals("1,234", rows.get(1).get(1));
    }

    @Test
    void localeLikeDescriptionIsNotTreatedAsSourceMetadata() throws Exception {
        List<List<String>> rows = grid(new String[]{"Description", "Amount"},
                new String[]{"en-US", "1,234"}, new String[]{"en-US", "1,234"},
                new String[]{"en-US", "1,234"});
        normalizeGrid(rows);
        assertEquals("1,234", rows.get(1).get(1));
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
                Arguments.of("whitespace control", "  01   Jan 26  ", "01 Jan 26"),
                Arguments.of("Unicode whitespace", "01\u00a0Jan\u202f26", "01 Jan 26"),
                Arguments.of("spaces around hyphens", "01 - Jan - 26", "01 Jan 26"),
                Arguments.of("single-digit day", "1-May-26", "1 May 26"),
                Arguments.of("month without year", "01Jan", "01 Jan")
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
    @ValueSource(strings = {"29-Feb-25", "31-Apr-26", "00-Jan-26", "32-Jan-26", "01-Xyz-26",
            "29-Feb-1900", "01-Jan-026", "01-Jan-20260", "ref 01-Jan-26"})
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

    private static boolean isLikelyNumeric(String raw) throws Exception {
        Method method = OcrStreamParser.class.getDeclaredMethod("isLikelyNumeric", String.class);
        method.setAccessible(true);
        return (boolean) method.invoke(null, raw);
    }

    private static void normalizeGrid(List<List<String>> grid) throws Exception {
        Method method = OcrStreamParser.class.getDeclaredMethod("normalizeNumericLikeColumns", List.class);
        method.setAccessible(true);
        method.invoke(null, grid);
    }
}
