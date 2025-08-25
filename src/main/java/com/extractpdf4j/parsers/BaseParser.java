package com.extractpdf4j.parsers;

import com.extractpdf4j.helpers.PageRange;
import com.extractpdf4j.helpers.Table;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BaseParser
 *
 * <p>Abstract base for all PDF table parsers in <code>com.extractpdf4j</code>.
 * Concrete implementations (e.g., {@code StreamParser}, {@code LatticeParser},
 * {@code OcrStreamParser}, {@code HybridParser}) should extend this class and
 * implement {@link #parsePage(int)}.</p>
 *
 * <h3>Responsibilities</h3>
 * <ul>
 *   <li>Holds common configuration shared by all parsers (file path, page ranges, flags).</li>
 *   <li>Provides a final, high-level {@link #parse()} that resolves the page
 *       selection and delegates work to {@link #parsePage(int)}.</li>
 * </ul>
 *
 * <h3>Page selection contract</h3>
 * <p>Page ranges are provided as a human-friendly string via {@link #pages(String)}.
 * The format supports values such as {@code "1"}, {@code "2-5"}, {@code "1,3-4"},
 * and {@code "all"}. The helper {@code PageRange.parse(..)} converts this into
 * a list of integers. Implementations must honor the following convention:</p>
 * <ul>
 *   <li>If {@code parsePage(-1)} is called, it indicates <strong>all pages</strong> should be parsed.</li>
 *   <li>Otherwise, {@code parsePage(p)} is called once per requested page number {@code p}.</li>
 * </ul>
 *
 * <h3>Thread-safety</h3>
 * <p>Instances are not inherently thread-safe. Create one parser instance per input file
 * or synchronize external access if you share state across threads.</p>
 *
 * @author Mehuli Mukherjee
 * @since 2025
 */
public abstract class BaseParser {
	
	/** Logger for parser base events. */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseParser.class);
	
    /** Absolute or relative path to the PDF file being parsed. */
    protected final String filepath;

    /**
     * Page selection string, defaulting to {@code "1"}.
     * Accepts formats like {@code "1"}, {@code "2-5"}, {@code "1,3-4"}, or {@code "all"}.
     */
    protected String pages = "1";

    /**
     * Whether to normalize/strip text (e.g., trim, collapse whitespace) in stream-based extraction.
     * Implementations may choose how to interpret this flag.
     */
    protected boolean stripText = true;

    /**
     * Constructs a parser for the given PDF file.
     *
     * @param filepath path to the PDF file
     */
    protected BaseParser(String filepath) {
        this.filepath = filepath;
    }

    /**
     * Sets the pages to parse. See the class docs for supported formats.
     *
     * @param pages page selection string (e.g., {@code "all"}, {@code "1"}, {@code "2-5"}, {@code "1,3-4"})
     * @return this parser (for chaining)
     */
    public BaseParser pages(String pages) {
        this.pages = pages;
        return this;
    }

    /**
     * Enables or disables text normalization for stream-style extraction.
     * Implementations may ignore this flag if not applicable.
     *
     * @param strip {@code true} to normalize/strip text, {@code false} to keep raw text
     * @return this parser (for chaining)
     */
    public BaseParser stripText(boolean strip) {
        this.stripText = strip;
        return this;
    }

    /**
     * Parses the configured pages from the PDF file.
     *
     * <p>This method resolves the page selection via {@code PageRange.parse(pages)} and
     * then delegates to {@link #parsePage(int)}. If the parsed list contains only {@code -1},
     * {@link #parsePage(int)} is called with {@code -1} to indicate "all pages". Otherwise,
     * it is called once for each requested page number.</p>
     *
     * @return a list of {@link Table} instances extracted from the requested pages (possibly empty)
     * @throws IOException if reading the file fails or a parsing error occurs
     * @since 2025
     */
    public List<Table> parse() throws IOException {
        List<Integer> pageList = PageRange.parse(this.pages);
        List<Table> out = new ArrayList<>();

        // Convention: a single -1 means "all" — subclasses should parse the entire document.
        if (pageList.size() == 1 && pageList.get(0) == -1) {
            List<Table> all = parsePage(-1);
            if (all != null) {
            	out.addAll(all);
            }
        } else {
            // Otherwise, parse each page number individually.
            for (int p : pageList) {
                List<Table> pageTables = parsePage(p);
                if (pageTables != null) {
                	out.addAll(pageTables);
                }
            }
        }
        return finalizeResults(out, this.filepath);
    }

    /**
     * Parses a single page or the entire document.
     *
     * <p><strong>Contract:</strong> If {@code page == -1}, the implementation must parse
     * the <em>entire document</em>. For any non-negative value, the implementation must
     * parse only the specified page index (1-based or 0-based is implementation-defined,
     * but should be consistent across the codebase and documented in concrete classes).</p>
     *
     * @param page page index to parse, or {@code -1} to parse all pages
     * @return a list of {@link Table} objects extracted from the requested page(s) (possibly empty)
     * @throws IOException if an error occurs while parsing
     * @since 2025
     */
    protected abstract List<Table> parsePage(int page) throws IOException;
    
    
    /** 
     * Normalizes parser output for "no tables" situations.
     * <p>If {@code tables} is {@code null} or empty, logs a concise message and returns
     * {@link java.util.Collections#emptyList()}. Otherwise return the input list unchanged.</p>
     * 
     * @param tables     tables collected for the requested page(s)
     * @param sourcePath     path to the input PDF (logging only)
     * @return a non-null list of tables
     * @since 2025
     */
    protected List<Table> finalizeResults(List<Table> tables, String sourcePath) {
    	if (tables == null || tables.isEmpty()) {
    		LOGGER.info("No tables detected in PDF: {}", sourcePath);
    		return Collections.emptyList();
    	}
    	
    	return tables;
    }
   
}
