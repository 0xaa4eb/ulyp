package com.agent.tests.general;

import com.agent.tests.general.a.A;
import com.agent.tests.general.a.b.B;
import com.agent.tests.general.a.c.C;
import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExcludeClassesInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    void shouldNotExcludeAnyClassesIfOptionIsNotSet() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
        );


        assertThat(root.getChildren(), Matchers.hasSize(2));
        assertThat(root.getChildren().get(0).getMethod().getType().getName(), is(B.class.getName()));
        assertThat(root.getChildren().get(1).getMethod().getType().getName(), is(C.class.getName()));
    }

    @Test
    void shouldExcludeFromInstrumentationOneClass() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.B")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getType().getName(), is(C.class.getName()));
    }

    @Disabled
    @Test
    void shouldExcludeFromInstrumentationOneClassByInterface() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("**.BInterface")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
        assertThat(root.getChildren().get(0).getMethod().getType().getName(), is(C.class.getName()));
    }

    @Test
    void shouldExcludeTwoClassesFromInstrumentation() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.B, com.agent.tests.general.a.c.C")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }

    @Test
    void shouldExcludeOneClassFromInstrumentationByAntPattern() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.**")
        );


        assertThat(root.getChildren(), Matchers.hasSize(1));
    }

    @Test
    void shouldExcludeTwoClassesFromInstrumentationByAntPattern() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(A.class)
                        .withExcludeClassesProperty("com.agent.tests.general.a.b.**, com.agent.tests.general.a.c.**")
        );


        assertThat(root.getChildren(), Matchers.empty());
    }
}
