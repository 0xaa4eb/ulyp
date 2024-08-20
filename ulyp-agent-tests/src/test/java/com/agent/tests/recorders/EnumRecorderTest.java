package com.agent.tests.recorders;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.core.recorders.EnumRecord;
import com.ulyp.storage.tree.CallRecord;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class EnumRecorderTest extends AbstractInstrumentationTest {

    @Test
    void shouldPrintEnumNames() {
        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(EnumTestCases.class)
                        .withMethodToRecord("consumesMapAndEnums")
        );

        assertThat(root.getArgs(), Matchers.hasSize(3));

        EnumRecord arg2 = (EnumRecord) root.getArgs().get(1);
        EnumRecord arg3 = (EnumRecord) root.getArgs().get(2);

        assertThat(arg2.getType().getName(), is(EnumTestCases.TestEnum.class.getName()));
        assertThat(arg2.getName(), is("T1"));
        assertThat(arg3.getType().getName(), is(EnumTestCases.TestEnum.class.getName()));
        assertThat(arg3.getName(), is("T2"));
    }

    public static class EnumTestCases {

        public static void main(String[] args) {
            new EnumTestCases().consumesMapAndEnums(
                    new HashMap<TestEnum, TestEnum>() {{
                        put(TestEnum.T1, TestEnum.T2);
                    }},
                    TestEnum.T1,
                    TestEnum.T2);
        }

        public void consumesMapAndEnums(Map<TestEnum, TestEnum> map, TestEnum l1, TestEnum l2) {
        }

        public enum TestEnum {
            T1("3.4"),
            T2("3.5");

            private final String s;

            TestEnum(String x) {
                s = x;
            }

            public String toString() {
                return s;
            }
        }
    }
}
