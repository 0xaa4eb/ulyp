package com.ulyp.core.recorders;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BufferBytesOut;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.DirectBytesIn;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.core.util.TypeMatcher;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

class PrintingRecorderTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);
    private final BytesOut out = new BufferBytesOut(buffer);
    private final BytesIn in = new DirectBytesIn(buffer);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    class X {
        public String toString() {
            throw new RuntimeException("not unsupported");
        }
    }

    @Test
    void test() throws Exception {
        PrintingRecorder recorder = (PrintingRecorder) ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance();
        recorder.addTypeMatchers(Arrays.asList(TypeMatcher.parse("**.X")));

        recorder.write(new X(), out, typeResolver);

        ObjectRecord identity = recorder.read(typeResolver.get(X.class), in, typeResolver::get);
        Assertions.assertTrue(identity instanceof IdentityObjectRecord);
    }
}