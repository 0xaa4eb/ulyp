package com.test.cases;

import com.test.cases.a.A;
import com.test.cases.a.b.B;
import com.test.cases.a.c.C;
import com.test.cases.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExcludeClassesInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotExcludeAnyClassesIfOptionIsNotSet() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
        );


        assertThat(root.getChildren(), Matchers.hasSize(2));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(B.class.getName()));
        assertThat(root.getChildren().get(1).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Test
    public void shouldExcludeFromInstrumentationOneClass() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setExcludeClassesProperty("com.test.cases.a.b.B")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Test
    public void shouldExcludeFromInstrumentationOneClassByInterface() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setExcludeClassesProperty("**.BInterface")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Test
    public void shouldExcludeTwoClassesFromInstrumentation() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setExcludeClassesProperty("com.test.cases.a.b.B, com.test.cases.a.c.C")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }

    @Test
    public void shouldExcludeOneClassFromInstrumentationByAntPattern() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setExcludeClassesProperty("com.test.cases.a.b.**")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
    }

    @Test
    public void shouldExcludeTwoClassesFromInstrumentationByAntPattern() {
        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(A.class)
                        .setExcludeClassesProperty("com.test.cases.a.b.**, com.test.cases.a.c.**")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }
}
