package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class NumberObjectRepresentation extends ObjectRepresentation {

    private final String numberPrintedText;

    public NumberObjectRepresentation(Type type, String numberPrintedText) {
        super(type);
        this.numberPrintedText = numberPrintedText;
    }

    public String getNumberPrintedText() {
        return numberPrintedText;
    }
}
