package com.agent.tests.cases.a;

import com.agent.tests.cases.PackageFilterInstrumentationTest;
import com.agent.tests.cases.a.b.B;
import com.agent.tests.cases.a.c.C;

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
