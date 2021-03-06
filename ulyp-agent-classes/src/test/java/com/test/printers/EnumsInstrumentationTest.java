package com.test.printers;

import com.test.cases.AbstractInstrumentationTest;
import com.test.cases.SafeCaller;
import com.test.cases.util.TestSettingsBuilder;
import com.ulyp.core.CallRecord;
import com.ulyp.core.printers.EnumRepresentation;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EnumsInstrumentationTest extends AbstractInstrumentationTest {

    @Test
    public void shouldPrintEnumNames() {
        CallRecord root = runSubprocessWithUi(
                new TestSettingsBuilder()
                        .setMainClassName(EnumTestCases.class)
                        .setMethodToRecord("consumesMapAndEnums")
        );

        assertThat(root.getArgs(), Matchers.hasSize(3));

        EnumRepresentation arg2 = (EnumRepresentation) root.getArgs().get(1);
        EnumRepresentation arg3 = (EnumRepresentation) root.getArgs().get(2);

        assertThat(arg2.getType().getName(), is("com.test.printers.EnumsInstrumentationTest$EnumTestCases$TestEnum"));
        assertThat(arg2.getName(), is("T1"));
        assertThat(arg3.getType().getName(), is("com.test.printers.EnumsInstrumentationTest$EnumTestCases$TestEnum"));
        assertThat(arg3.getName(), is("T2"));
    }

    public static class EnumTestCases {

        public static void main(String[] args) {
            SafeCaller.call(() -> new EnumTestCases().consumesMapAndEnums(
                    new HashMap<TestEnum, TestEnum>() {{
                        put(TestEnum.T1, TestEnum.T2);
                    }},
                    TestEnum.T1,
                    TestEnum.T2));
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
