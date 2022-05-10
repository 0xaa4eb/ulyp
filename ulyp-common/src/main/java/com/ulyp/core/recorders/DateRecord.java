package com.ulyp.core.recorders;

import com.ulyp.core.Type;

public class DateRecord extends ObjectRecord {

    private final String datePrinted;

    public DateRecord(Type type, String numberPrintedText) {
        super(type);
        this.datePrinted = numberPrintedText;
    }

    public String getDatePrinted() {
        return datePrinted;
    }

    @Override
    public String toString() {
        return datePrinted;
    }
}
