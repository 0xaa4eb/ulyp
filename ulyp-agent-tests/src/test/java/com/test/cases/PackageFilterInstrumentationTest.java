package com.test.cases;

import com.test.cases.a.A;
import com.test.cases.a.c.C;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class PackageFilterInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldInstrumentAndTraceAllClasses() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.test.cases.a")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is(A.class.getName()));
        assertThat(root.getChildren(), Matchers.hasSize(2));
    }

    @Test
    public void shouldExcludeInstrumentationPackage() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.test.cases.a")
                        .setExcludedFromInstrumentationPackages("com.test.cases.a.b")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is("com.test.cases.a.A"));
        assertThat(root.getChildren(), Matchers.hasSize(1));

        CallRecord callRecord = root.getChildren().get(0);

        assertThat(callRecord.getClassName(), is(C.class.getName()));
        assertThat(callRecord.getMethodName(), is("c"));
    }

    @Test
    public void shouldExcludeTwoPackages() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setInstrumentedPackages("com.test.cases.a")
                        .setExcludedFromInstrumentationPackages("com.test.cases.a.b", "com.test.cases.a.c")
        );

        assertThat(root.getMethodName(), is("main"));
        assertThat(root.getClassName(), is("com.test.cases.a.A"));
        assertThat(root.getChildren(), Matchers.empty());
    }
}
