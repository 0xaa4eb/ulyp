package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class NumberObjectRecord extends ObjectRecord {

    private final String numberPrintedText;

    public NumberObjectRecord(Type type, String numberPrintedText) {
        super(type);
        this.numberPrintedText = numberPrintedText;
    }

    public String getNumberPrintedText() {
        return numberPrintedText;
    }
}
