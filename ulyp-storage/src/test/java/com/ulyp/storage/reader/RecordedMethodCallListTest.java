package com.ulyp.storage.reader;

import com.ulyp.core.*;
import com.ulyp.core.mem.*;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.bytes.BufferBinaryInput;
import com.ulyp.core.recorders.bytes.MemBinaryOutput;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class RecordedMethodCallListTest {

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testCalls() throws IOException {
        BinaryList.Out out = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new MemBinaryOutput(pageAllocator()));
        RecordedMethodCallList recordedMethodCallList = new RecordedMethodCallList(333, out);

        Type type = typeResolver.get(A.class);
        Method method = Method.builder().id(5).name("convert").declaringType(type).build();

        int callsCount = 10000;
        for (int i = 0; i < callsCount; i++) {
            recordedMethodCallList.addEnterMethodCall(i, method.getId(), typeResolver, new A(), new Object[]{5});
            recordedMethodCallList.addExitMethodCall(i, typeResolver, "ABC");
        }

        BinaryList.In read = flip(out);

        RecordedMethodCalls list = new RecordedMethodCalls(read);
        Assert.assertEquals(callsCount * 2, list.size());
        AddressableItemIterator<RecordedMethodCall> it = list.iterator(new InMemoryRepository<>());

        for (int i = 0; i < callsCount * 2; i++) {
            it.next();
        }
    }

    @Test
    public void testAddAndIterate() throws IOException {
        BinaryList.Out out = new BinaryList.Out(RecordedMethodCallList.WIRE_ID, new MemBinaryOutput(pageAllocator()));
        RecordedMethodCallList recordedMethodCallList = new RecordedMethodCallList(333, out);

        Type type = typeResolver.get(A.class);
        Method method = Method.builder().id(5).name("convert").declaringType(type).build();

        recordedMethodCallList.addEnterMethodCall(134, method.getId(), typeResolver, new A(), new Object[]{5});
        recordedMethodCallList.addExitMethodCall(134, typeResolver, "ABC");

        BinaryList.In read = flip(out);
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

    private static BinaryList.In flip(BinaryList.Out out) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int length = out.writeTo(outputStream);
        return new BinaryList.In(new BufferBinaryInput(outputStream.toByteArray(), length));
    }

    private MemPageAllocator pageAllocator() {
        return new MemPageAllocator() {

            @Override
            public MemPage allocate() {
                System.out.println("allocated a new page");
                return new MemPage(0, new UnsafeBuffer(new byte[MemPool.PAGE_SIZE]));
            }

            @Override
            public void deallocate(MemPage page) {

            }
        };
    }

    public static class A {
        public String convert(int x) {
            return String.valueOf(x);
        }
    }
}