package com.ulyp.core.recorders;

import com.ulyp.core.Type;

/**
 * Object record for some number. Can be both integer or float point.
 * Stores text representation, since for some instances of {@link Number} we can only capture
 * their string representation.
 */
public class NumberRecord extends ObjectRecord {

    private final String numberPrintedText;

    public NumberRecord(Type type, String numberPrintedText) {
        super(type);
        this.numberPrintedText = numberPrintedText;
    }

    public String getNumberPrintedText() {
        return numberPrintedText;
    }

    @Override
    public String toString() {
        return numberPrintedText;
    }
}
