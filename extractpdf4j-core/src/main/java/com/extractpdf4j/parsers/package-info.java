/**
 * PDF parsing strategies that convert document content into structured tables.
 *
 * <h2>Choose a strategy</h2>
 * <dl>
 *   <dt>{@link com.extractpdf4j.parsers.StreamParser STREAM}</dt>
 *   <dd>Use text positions to group rows and columns in PDFs with selectable text.</dd>
 *   <dt>{@link com.extractpdf4j.parsers.LatticeParser LATTICE}</dt>
 *   <dd>Use visible horizontal and vertical rules to find cells in a table grid.</dd>
 *   <dt>{@link com.extractpdf4j.parsers.OcrStreamParser OCRSTREAM}</dt>
 *   <dd>Recognize text and locate columns in scanned pages.</dd>
 *   <dt>{@link com.extractpdf4j.parsers.HybridParser HYBRID}</dt>
 *   <dd>Compare strategies for documents with mixed or unknown layouts.</dd>
 * </dl>
 *
 * <figure>
 *   <img src="doc-files/statement-extraction.png"
 *        alt="Synthetic PDF and CSV excerpts showing the 3 April ATM withdrawal in the Debit column."
 *        width="1200" height="363" style="max-width: 100%; height: auto;">
 *   <figcaption>Example transaction rows extracted from a text-based PDF.</figcaption>
 * </figure>
 */
package com.extractpdf4j.parsers;
