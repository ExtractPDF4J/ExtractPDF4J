# Edge-case PDF post-normalization results

All **110 checks pass** across the six original PDFs: 65 amounts and 45 dates. Previously, 107 passed and three locale-dependent amounts failed.

Each CSV records raw, normalized, and expected values plus PASS/FAIL for one PDF. PDFBox extracts cells using the known geometry of these synthetic fixtures. The complete table, including headers and descriptions, then passes through `OcrStreamParser.normalizeNumericLikeColumns`. This tests post-normalization on embedded PDF text, not Tesseract or automatic table detection. Unicode spacing is also tested directly because text extraction may collapse nonbreaking spaces.

| Source PDF | Result file | Passed | Failed |
| --- | --- | ---: | ---: |
| [01-negative-amounts.pdf](../01-negative-amounts.pdf) | [01-negative-amounts-results.csv](01-negative-amounts-results.csv) | 18 | 0 |
| [02-locale-decimal-grouping.pdf](../02-locale-decimal-grouping.pdf) | [02-locale-decimal-grouping-results.csv](02-locale-decimal-grouping-results.csv) | 11 | 0 |
| [03-space-and-apostrophe-grouping.pdf](../03-space-and-apostrophe-grouping.pdf) | [03-space-and-apostrophe-grouping-results.csv](03-space-and-apostrophe-grouping-results.csv) | 9 | 0 |
| [04-credit-cr-markers.pdf](../04-credit-cr-markers.pdf) | [04-credit-cr-markers-results.csv](04-credit-cr-markers-results.csv) | 22 | 0 |
| [05-dd-mmm-yy-dates.pdf](../05-dd-mmm-yy-dates.pdf) | [05-dd-mmm-yy-dates-results.csv](05-dd-mmm-yy-dates-results.csv) | 26 | 0 |
| [06-mixed-statement.pdf](../06-mixed-statement.pdf) | [06-mixed-statement-results.csv](06-mixed-statement-results.csv) | 24 | 0 |
| **Total** | | **110** | **0** |

## Normalization rules

- Currency symbols, qualified dollar symbols, and Java-recognized ISO codes are accepted. Matching symbols and codes can appear together. Conflicting currencies remain unchanged.
- Leading/trailing signs around the currency or amount, accounting parentheses, Unicode minus, and fullwidth minus are handled. Conflicting multiple signs remain unchanged.
- Western/Indian grouping, decimal commas/dots, Unicode spaces, and apostrophes are supported. Digits and precision are preserved.
- An explicit `Locale` header in the first table row supplies source formatting through supported language-country tags such as `en-US`, `de-DE`, or `pt-BR`. It takes precedence over currency defaults for ambiguous values. The original source values are parsed once per extraction.
- Without a supported source locale, conventional hints apply to BRL and USD/GBP amounts. Bare dollar/euro symbols and other ambiguous cases remain unchanged. Locale-like descriptions are not treated as metadata.
- Currency codes such as SCR/CRC are not mistaken for CR credit markers. A separate CR marker denotes a negative credit under these fixtures' convention.
- Month-name dates retain two/four-digit years; invalid dates remain unchanged.

## Previously failing locale cases

| ID | Source locale | Raw | Normalized |
| --- | --- | --- | --- |
| L05 | en-US | `1,234` | `1234` |
| L06 | de-DE | `1,234` | `1.234` |
| L08 | de-DE | `1.234` | `1234` |

## Automated verification

- `OcrStreamParserNormalizationTest`: 361 passed; zero failures/errors/skips.
- `OcrStreamParserNormalizationPdfTest`: 6 passed; zero failures/errors/skips.
- `OcrStreamParserTest`: 5 passed; zero failures/errors/skips.

Total: **372 tests passed**, including six PDF cases containing the 110 individual field assertions. Independent PDF expectations are stored in `extractpdf4j-core/src/test/resources/normalization-pdf-expectations.tsv`.

```sh
JAVA_HOME=/usr/lib/jvm/java-17-temurin-jdk mvn -o -pl extractpdf4j-core -Dtest=OcrStreamParserNormalizationTest,OcrStreamParserNormalizationPdfTest,OcrStreamParserTest test
```

## Input and implementation hashes

SHA-256 values identify the exact PDFs and normalizer tested.

- `examples/01-negative-amounts.pdf`: `ac2bf19f3655f122083af8b0d251613604f724bda5088936d2f38ac49f2d144d`
- `examples/02-locale-decimal-grouping.pdf`: `22d3f3269aef8625503c69427646d833dfd81fda13dc48d318d2f1b7fc8fecb3`
- `examples/03-space-and-apostrophe-grouping.pdf`: `ad5a8abe97acf1c40b73dd22beb68fe033f6fd6efddbd80fc2a7085632aa42ee`
- `examples/04-credit-cr-markers.pdf`: `06eda8d6a8a80979df3b640856f8e898d5ce9288f00764085da21022f2cb79b4`
- `examples/05-dd-mmm-yy-dates.pdf`: `f7c59b32eecc0b24464a580d1bb192586293f06c78220f6137117ef7f14da75b`
- `examples/06-mixed-statement.pdf`: `7bd8c63f4e2d858ff749a8cb14eec386619f7e0dfcf9b9e7319ba83f7c5303ed`
- `extractpdf4j-core/src/main/java/com/extractpdf4j/parsers/OcrStreamParser.java`: `1a284c105bf567e1563f360457d4143286d1d5ada9a6b9b96b983a765bb65745`
