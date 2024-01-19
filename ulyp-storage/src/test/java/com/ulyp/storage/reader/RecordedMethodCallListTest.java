package com.ulyp.storage.reader;

import com.ulyp.core.*;
import com.ulyp.core.mem.BinaryList;
import com.ulyp.core.mem.RecordedMethodCallList;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.BufferBinaryOutput;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class RecordedMethodCallListTest {

    private final byte[] buf = new byte[16 * 1024];

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testAddAndIterate() {
        BinaryList.Out write = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new BufferBinaryOutput(new UnsafeBuffer(buf)));
        RecordedMethodCallList recordedMethodCallList = new RecordedMethodCallList(333, write);

        Type type = typeResolver.get(A.class);

        Method method = Method.builder().id(5).name("convert").declaringType(type).build();

        recordedMethodCallList.addEnterMethodCall(134, method, typeResolver, new A(), new Object[]{5});
        recordedMethodCallList.addExitMethodCall(134, typeResolver, "ABC");

        BinaryList.In read = new BinaryList.In(new BufferBinaryInput(new UnsafeBuffer(buf)));
        RecordedMethodCalls list = new RecordedMethodCalls(read);

        AddressableItemIterator<RecordedMethodCall> it = list.iterator(new InMemoryRepository<>());

        RecordedEnterMethodCall enterCall = (RecordedEnterMethodCall) it.next();

        assertEquals(134, enterCall.getCallId());
        assertEquals(5, enterCall.getMethodId());
        List<ObjectRecord> arguments = enterCall.getArguments();
        assertEquals(1, arguments.size());
        NumberRecord numberRecord = (NumberRecord) arguments.get(0);
        assertEquals("5", numberRecord.getNumberPrintedText());

        RecordedExitMethodCall exitCall = (RecordedExitMethodCall) it.next();

        assertEquals(134, exitCall.getCallId());
    }

    public static class A {
        public String convert(int x) {
            return String.valueOf(x);
        }
    }
}