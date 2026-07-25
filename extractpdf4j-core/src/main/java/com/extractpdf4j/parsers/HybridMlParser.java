package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.Table;
import com.extractpdf4j.ml.MlTableExtractor;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * Experimental hybrid parser that invokes an injected ML extractor only when
 * the existing Stream, Lattice, and OCR strategies return no tables.
 *
 * <p>This keeps ML optional and preserves the current deterministic parser
 * behaviour. The ML implementation can live in a separate module and may use
 * ONNX Runtime without introducing that dependency into the core module.</p>
 */
public class HybridMlParser extends HybridParser {

    private final MlTableExtractor mlExtractor;
    private boolean mlFallbackEnabled = true;
    private boolean failOnMlError = false;

    /**
     * Creates an ML-enabled hybrid parser for a file.
     *
     * @param filepath PDF path
     * @param mlExtractor optional ML extraction strategy
     */
    public HybridMlParser(String filepath, MlTableExtractor mlExtractor) {
        super(filepath);
        this.mlExtractor = Objects.requireNonNull(mlExtractor, "mlExtractor");
    }

    /**
     * Creates an ML-enabled parser for in-memory PDFBox documents.
     *
     * @param mlExtractor optional ML extraction strategy
     */
    public HybridMlParser(MlTableExtractor mlExtractor) {
        super();
        this.mlExtractor = Objects.requireNonNull(mlExtractor, "mlExtractor");
    }

    /** Enables or disables ML fallback. Enabled by default for this parser. */
    public HybridMlParser enableMlFallback(boolean enabled) {
        this.mlFallbackEnabled = enabled;
        return this;
    }

    /**
     * Controls whether ML inference errors fail parsing or fall back to an
     * empty ML result. The default is {@code false}.
     */
    public HybridMlParser failOnMlError(boolean enabled) {
        this.failOnMlError = enabled;
        return this;
    }

    @Override
    public HybridMlParser pages(String pages) {
        super.pages(pages);
        return this;
    }

    @Override
    public HybridMlParser dpi(float dpi) {
        super.dpi(dpi);
        return this;
    }

    @Override
    public HybridMlParser debug(boolean enabled) {
        super.debug(enabled);
        return this;
    }

    @Override
    public HybridMlParser stripText(boolean strip) {
        super.stripText(strip);
        return this;
    }

    @Override
    protected List<Table> parsePage(int page) throws IOException {
        List<Table> ruleBased = super.parsePage(page);
        if (!mlFallbackEnabled || (ruleBased != null && !ruleBased.isEmpty())) {
            return ruleBased;
        }

        String pageSelection = page == -1 ? pages : String.valueOf(page);
        try {
            List<Table> mlTables = mlExtractor.extract(filepath, pageSelection);
            return mlTables == null ? Collections.emptyList() : mlTables;
        } catch (IOException ex) {
            if (failOnMlError) {
                throw ex;
            }
            return Collections.emptyList();
        } catch (RuntimeException ex) {
            if (failOnMlError) {
                throw new IOException("ML table extraction failed", ex);
            }
            return Collections.emptyList();
        }
    }

    @Override
    public List<Table> parse(PDDocument document) throws IOException {
        List<Table> ruleBased = super.parse(document);
        if (!mlFallbackEnabled || (ruleBased != null && !ruleBased.isEmpty())) {
            return ruleBased;
        }

        try {
            List<Table> mlTables = mlExtractor.extract(document, pages);
            return mlTables == null ? Collections.emptyList() : mlTables;
        } catch (IOException ex) {
            if (failOnMlError) {
                throw ex;
            }
            return Collections.emptyList();
        } catch (RuntimeException ex) {
            if (failOnMlError) {
                throw new IOException("ML table extraction failed", ex);
            }
            return Collections.emptyList();
        }
    }
}
