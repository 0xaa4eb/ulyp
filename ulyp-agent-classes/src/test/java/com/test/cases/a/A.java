package com.test.cases.a;

import com.test.cases.a.b.B;
import com.test.cases.a.c.C;

/**
 * Used for {@link com.test.cases.PackageFilterInstrumentationTest} for testing configurating instrumentation
 * by packages
 */
public class A {

    public static void main(String[] args) {
        new B().b();
        new C().c();
    }
}
