package com.agent.tests.cases;

import com.agent.tests.cases.util.ForkProcessBuilder;
import com.ulyp.storage.CallRecord;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class UserDefinedClassLoaderTest extends AbstractInstrumentationTest {

    @Test
    public void testUserDefinedClassLoader() {

        CallRecord root = run(
                new ForkProcessBuilder()
                        .setMainClassName(UserDefinedClassLoaderTestCase.class)
                        .setMethodToRecord("runInOwnClassLoader")
        );

        assertThat(root.getMethodName(), is("runInOwnClassLoader"));
        assertThat(root.getChildren(), hasSize(1));

        CallRecord callRecord = root.getChildren().get(0);
        assertThat(callRecord.getMethodName(), is("hello"));
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
                Class<?> aClass = cl.loadClass("com.agent.tests.cases.UserDefinedClassLoaderTest$UserDefinedClassLoaderTestCase");

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
