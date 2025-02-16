package com.ulyp.core.recorders;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import com.ulyp.core.util.TypeMatcher;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class PrintingRecorderTest {

    private final PrintingRecorder recorder = new PrintingRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldNotFailIfToStringThrows() throws Exception {
        recorder.addTypeMatchers(Arrays.asList(TypeMatcher.parse("**.X")));
        BytesOut out = BytesOut.expandableArray();
        X value = new X();

        recorder.write(value, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(typeResolver.get(X.class), out.flip(), typeResolver::getById);

        assertInstanceOf(IdentityObjectRecord.class, objectRecord);
        IdentityObjectRecord identityObjectRecord = (IdentityObjectRecord) objectRecord;
        assertEquals(System.identityHashCode(value), identityObjectRecord.getHashCode());
    }

    class X {
        public String toString() {
            throw new RuntimeException("not unsupported");
        }
    }
}