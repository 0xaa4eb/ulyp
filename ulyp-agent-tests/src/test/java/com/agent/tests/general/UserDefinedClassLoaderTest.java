package com.agent.tests.general;

import com.agent.tests.util.AbstractInstrumentationTest;
import com.agent.tests.util.ForkProcessBuilder;
import com.ulyp.storage.tree.CallRecord;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;

class UserDefinedClassLoaderTest extends AbstractInstrumentationTest {

    @Test
    void testUserDefinedClassLoader() {

        CallRecord root = runSubprocessAndReadFile(
                new ForkProcessBuilder()
                        .withMainClassName(UserDefinedClassLoaderTestCase.class)
                        .withMethodToRecord("runInOwnClassLoader")
        );

        assertThat(root.getMethod().getName(), is("runInOwnClassLoader"));
        assertThat(root.getChildren(), hasSize(1));

        CallRecord callRecord = root.getChildren().get(0);
        assertThat(callRecord.getMethod().getName(), is("hello"));
    }

    static class UserDefinedClassLoaderTestCase {

        public static void hello() {

        }

        public static void runInOwnClassLoader() {
            URL[] urls;
            try {
                URL url = Paths.get(".", "build", "classes", "java", "test").toFile().toURL();
                urls = new URL[]{url};
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            ClassLoader cl = new URLClassLoader(urls);
            try {
                Class<?> aClass = cl.loadClass("com.agent.tests.general.UserDefinedClassLoaderTest$UserDefinedClassLoaderTestCase");

                Method hello = aClass.getDeclaredMethod("hello");

                hello.invoke(null);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public static void main(String[] args) {
            runInOwnClassLoader();
        }
    }
}
