package com.agent.tests.general.a.c;

import com.agent.tests.general.PackageFilterInstrumentationTest;
import com.agent.tests.general.a.interfaces.CInterface;

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
