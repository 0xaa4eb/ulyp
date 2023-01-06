package com.agent.tests.general.a;

import com.agent.tests.general.PackageFilterInstrumentationTest;
import com.agent.tests.general.a.b.B;
import com.agent.tests.general.a.c.C;

/**
 * Used for {@link PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class A {

    public static void main(String[] args) {
        new B().b();
        new C().c();
    }
}
