package com.test.cases.a.c;

import com.test.cases.a.interfaces.CInterface;

/**
 * Used for {@link com.test.cases.PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class C implements CInterface {

    @Override
    public int c() {
        return 5;
    }
}
