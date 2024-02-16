package com.ulyp.storage.reader;

import com.ulyp.core.*;
import com.ulyp.core.mem.*;
import com.ulyp.core.recorders.NumberRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.bytes.PagedMemBinaryOutput;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class RecordedMethodCallListTest {

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    public void testReadWriteRecordedCallsList() throws IOException {
        for (int iter = 0; iter < 500; iter++) {
            OutputBinaryList out = new OutputBinaryList(RecordedMethodCallList.WIRE_ID, new PagedMemBinaryOutput(pageAllocator()));
            RecordedMethodCallList recordedMethodCallList = new RecordedMethodCallList(333, out);

            Type type = typeResolver.get(A.class);
            Method method = Method.builder().id(5).name("convert").declaringType(type).build();

            int callsCount = 10000;
            for (int i = 0; i < callsCount; i++) {
                recordedMethodCallList.addEnterMethodCall(i, method.getId(), typeResolver, new A(), new Object[]{5});
                recordedMethodCallList.addExitMethodCall(i, typeResolver, "ABC");
            }

            InputBinaryList read = out.flip();

            RecordedMethodCalls list = new RecordedMethodCalls(read);
            Assert.assertEquals(callsCount * 2, list.size());
            AddressableItemIterator<RecordedMethodCall> it = list.iterator(new InMemoryRepository<>());

            for (int i = 0; i < callsCount * 2; i++) {
                it.next();
            }
        }
    }

    @Test
    public void testAddAndIterate() throws IOException {
        OutputBinaryList out = new OutputBinaryList(RecordedMethodCallList.WIRE_ID, new PagedMemBinaryOutput(pageAllocator()));
        RecordedMethodCallList recordedMethodCallList = new RecordedMethodCallList(333, out);

        Type type = typeResolver.get(A.class);
        Method method = Method.builder().id(5).name("convert").declaringType(type).build();

        recordedMethodCallList.addEnterMethodCall(134, method.getId(), typeResolver, new A(), new Object[]{5});
        recordedMethodCallList.addExitMethodCall(134, typeResolver, "ABC");

        InputBinaryList read = out.flip();
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