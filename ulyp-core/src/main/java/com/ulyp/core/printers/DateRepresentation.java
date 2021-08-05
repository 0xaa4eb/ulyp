package com.ulyp.core.printers;

import com.ulyp.core.Type;

public class DateRepresentation extends ObjectRepresentation {

    private final String datePrinted;

    public DateRepresentation(Type type, String numberPrintedText) {
        super(type);
        this.datePrinted = numberPrintedText;
    }

    public String getDatePrinted() {
        return datePrinted;
    }
}
