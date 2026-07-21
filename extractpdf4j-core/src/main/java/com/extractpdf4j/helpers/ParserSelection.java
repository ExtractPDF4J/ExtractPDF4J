package com.extractpdf4j.helpers;

/** Parser strategy selected for an extraction result. */
public enum ParserSelection {
    STREAM,
    LATTICE,
    OCR,
    HYBRID,
    OCR_STREAM
}