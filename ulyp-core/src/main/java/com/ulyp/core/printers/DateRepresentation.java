package com.ulyp.core.printers;

public class DateRepresentation extends ObjectRepresentation {

    private final String datePrinted;

    public DateRepresentation(TypeInfo typeInfo, String numberPrintedText) {
        super(typeInfo);
        this.datePrinted = numberPrintedText;
    }

    public String getDatePrinted() {
        return datePrinted;
    }
}
