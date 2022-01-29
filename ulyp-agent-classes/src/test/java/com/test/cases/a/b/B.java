package com.test.cases.a.b;

import com.test.cases.a.interfaces.BInterface;

/**
 * Used for {@link com.test.cases.PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class B implements BInterface {

    @Override
    public int b() {
        return 5;
    }
}
