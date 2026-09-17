package com.extractpdf4j.helpers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Result wrapper containing extracted tables and optional extraction diagnostics. */
public class ExtractionResult {
    private final List<Table> tables;
    private final ExtractionDiagnostics diagnostics;

    public ExtractionResult(List<Table> tables, ExtractionDiagnostics diagnostics) {
        this.tables = tables == null ? Collections.emptyList() : Collections.unmodifiableList(new ArrayList<>(tables));
        this.diagnostics = diagnostics;
    }

    public List<Table> tables() { return tables; }
    public ExtractionDiagnostics diagnostics() { return diagnostics; }
}