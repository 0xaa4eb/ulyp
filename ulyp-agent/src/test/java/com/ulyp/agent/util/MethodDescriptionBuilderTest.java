/*
package com.ulyp.agent.util;

import net.bytebuddy.description.method.MethodDescription;
import org.h2.jdbc.JdbcConnection;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;

public class MethodDescriptionBuilderTest {

    public interface Interface1 {

    }

    public interface Interface2 {

    }

    public interface Interface3 {

    }

    public static class BaseClass implements Interface1, Interface2 {

    }

    public static class TestClass extends BaseClass implements Interface3 {

        public void run() {

        }

        public Void runAndReturnVoid() {
            return null;
        }
    }

    @Test
    public void shouldHaveValidName() throws NoSuchMethodException {

        com.ulyp.core.MethodDescription methodDescription = MethodDescriptionBuilder.newMethodDescription(new MethodDescription.ForLoadedMethod(
                JdbcConnection.class.getDeclaredMethod("createStatement")
        ));

        Assert.assertThat(methodDescription.getMethodName(), Matchers.is("createStatement"));
    }

    @Test
    public void testReturnsSomethingField() throws NoSuchMethodException {

        com.ulyp.core.MethodDescription methodDescription = MethodDescriptionBuilder.newMethodDescription(new MethodDescription.ForLoadedMethod(
                TestClass.class.getDeclaredMethod("run")
        ));

        Assert.assertFalse(methodDescription.returnsSomething());

        methodDescription = MethodDescriptionBuilder.newMethodDescription(new MethodDescription.ForLoadedMethod(
                TestClass.class.getDeclaredMethod("runAndReturnVoid")
        ));

        Assert.assertTrue(methodDescription.returnsSomething());
    }
}*/
