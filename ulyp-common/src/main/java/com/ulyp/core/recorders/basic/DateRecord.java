package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.ObjectRecord;

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
