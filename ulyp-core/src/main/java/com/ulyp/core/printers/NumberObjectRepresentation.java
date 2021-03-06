package com.ulyp.core.printers;

public class NumberObjectRepresentation extends ObjectRepresentation {

    private final String numberPrintedText;

    public NumberObjectRepresentation(TypeInfo typeInfo, String numberPrintedText) {
        super(typeInfo);
        this.numberPrintedText = numberPrintedText;
    }

    public String getNumberPrintedText() {
        return numberPrintedText;
    }
}
