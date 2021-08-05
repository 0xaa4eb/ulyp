/*
package com.ulyp.agent.util;

import com.ulyp.core.util.MethodMatcher;
import net.bytebuddy.description.method.MethodDescription;
import org.h2.jdbc.JdbcConnection;
import org.junit.Assert;
import org.junit.Test;

public class MethodMatcherTest {

    @Test
    public void testSimpleMatching() throws NoSuchMethodException {
        com.ulyp.core.MethodDescription methodDescription = MethodDescriptionBuilder.newMethodDescription(new MethodDescription.ForLoadedMethod(
                JdbcConnection.class.getDeclaredMethod("createStatement")
        ));

        Assert.assertTrue(MethodMatcher.parse("Connection.createStatement").matches(methodDescription));

        Assert.assertTrue(MethodMatcher.parse("JdbcConnection.createStatement").matches(methodDescription));
    }
}
*/
