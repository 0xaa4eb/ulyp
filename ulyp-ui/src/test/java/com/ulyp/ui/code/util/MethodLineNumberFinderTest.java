package com.ulyp.ui.code.util;

import com.ulyp.ui.code.SourceCode;
import com.ulyp.ui.code.util.MethodLineNumberFinder;
import org.junit.Assert;
import org.junit.Test;

public class MethodLineNumberFinderTest {

    @Test
    public void test() {

        MethodLineNumberFinder methodFinder = new MethodLineNumberFinder(
                new SourceCode(
                        "com.test.A",
                        "package com.test;\n" +
                                "\n" +
                                "public class A {\n" +
                                "    public void foo() {\n" +
                                "    }\n" +
                                "}\n"
                )
        );

        int fooLine = methodFinder.getLine("foo", 0);

        Assert.assertEquals(4, fooLine);

        int notFoundLine = methodFinder.getLine("abc", 1099999);

        Assert.assertEquals(1099999, notFoundLine);
    }
}