# Synthetic bank statement example

This two-page [sample PDF](synthetic_bank_statement_text.pdf) contains fictional account details and transactions. The example shows how ExtractPDF4J reads the transaction table from a PDF with selectable text.

## Reproduce the CSVs

With Java 17 and Maven installed, run these commands from the repository root using a checkout that includes the statement extraction fix.

```sh
mvn -pl extractpdf4j-cli -am package -DskipTests
java -jar extractpdf4j-cli/target/extractpdf4j-cli-2.3.1.jar \
  examples/synthetic-bank-statement/synthetic_bank_statement_text.pdf \
  --mode STREAM --pages all --out bank_statement.csv
```

The CLI creates `bank_statement-1.csv` and `bank_statement-2.csv`. Compare them with the checked-in UTF-8 exports for [page 1](bank_statement_hybrid-1.csv) and [page 2](bank_statement_hybrid-2.csv). Page 1 contains 20 transactions and page 2 contains 9. Each file has a header and seven fields per transaction, including separate Value Date, Description, Debit, and Credit fields.

STREAM is a good fit for this PDF because it has a text layer. HYBRID produced identical CSVs on this sample. LATTICE detected no tables.
