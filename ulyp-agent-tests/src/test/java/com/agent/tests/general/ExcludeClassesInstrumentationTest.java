package com.agent.tests.general;

import com.agent.tests.general.a.A;
import com.agent.tests.general.a.b.B;
import com.agent.tests.general.a.c.C;
import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class ExcludeClassesInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldNotExcludeAnyClassesIfOptionIsNotSet() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
        );


        assertThat(root.getChildren(), Matchers.hasSize(2));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(B.class.getName()));
        assertThat(root.getChildren().get(1).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Test
    public void shouldExcludeFromInstrumentationOneClass() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.B")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Ignore
    @Test
    public void shouldExcludeFromInstrumentationOneClassByInterface() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("**.BInterface")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getDeclaringType().getName(), is(C.class.getName()));
    }

    @Test
    public void shouldExcludeTwoClassesFromInstrumentation() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.B, com.agent.tests.general.a.c.C")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }

    @Test
    public void shouldExcludeOneClassFromInstrumentationByAntPattern() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.**")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
    }

    @Test
    public void shouldExcludeTwoClassesFromInstrumentationByAntPattern() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.**, com.agent.tests.general.a.c.**")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }
}
