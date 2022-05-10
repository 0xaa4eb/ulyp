package com.ulyp.core.recorders;

import com.ulyp.core.Type;

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
