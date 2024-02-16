package com.ulyp.core.recorders;

import java.util.Arrays;
import java.util.HashSet;

import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.core.util.TypeMatcher;

import static org.junit.Assert.*;

public class ToStringPrintingRecorderTest {

    private final UnsafeBuffer buffer = new UnsafeBuffer(new byte[16 * 1024]);
    private final BinaryOutput out = new BufferBinaryOutput(buffer);
    private final BinaryInput in = new BufferBinaryInput(buffer);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    class X {
        public String toString() {
            throw new RuntimeException("not unsupported");
        }
    }

    @Test
    public void test() throws Exception {
        ToStringPrintingRecorder recorder = (ToStringPrintingRecorder) ObjectRecorderRegistry.TO_STRING_RECORDER.getInstance();
        recorder.addClassesToPrint(new HashSet<>(Arrays.asList(TypeMatcher.parse("**.X"))));

        recorder.write(new X(), out, typeResolver);

        ObjectRecord identity = recorder.read(typeResolver.get(X.class), in, typeResolver::get);
        assertTrue(identity instanceof IdentityObjectRecord);
    }
}