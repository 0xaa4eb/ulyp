package com.agent.tests.general.a.b;

import com.agent.tests.general.PackageFilterInstrumentationTest;
import com.agent.tests.general.a.interfaces.BInterface;

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
