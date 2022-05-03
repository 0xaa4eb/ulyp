package com.agent.tests.cases.a.b;

import com.agent.tests.cases.PackageFilterInstrumentationTest;
import com.agent.tests.cases.a.interfaces.BInterface;

/**
 * Used for {@link PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class B implements BInterface {

    @Override
    public int b() {
        return 5;
    }
}
