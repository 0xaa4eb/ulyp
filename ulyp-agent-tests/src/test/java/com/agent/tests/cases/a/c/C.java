package com.agent.tests.cases.a.c;

import com.agent.tests.cases.PackageFilterInstrumentationTest;
import com.agent.tests.cases.a.interfaces.CInterface;

/**
 * Used for {@link PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class C implements CInterface {

    @Override
    public int c() {
        return 5;
    }
}
