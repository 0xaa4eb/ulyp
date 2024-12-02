package com.ulyp.storage.reader;

import com.ulyp.core.*;
import com.ulyp.core.mem.*;
import com.ulyp.core.recorders.numeric.IntegralRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.bytes.PagedMemBytesOut;
import com.ulyp.core.repository.InMemoryRepository;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.agrona.concurrent.UnsafeBuffer;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SerializedRecordedMethodCallListTest {

    private final ReflectionBasedTypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void testReadWriteRecordedCallsList() {
        for (int iter = 0; iter < 500; iter++) {
            OutputBytesList out = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(pageAllocator()));
            SerializedRecordedMethodCallList serializedRecordedMethodCallList = new SerializedRecordedMethodCallList(333, out);

            Type type = typeResolver.get(A.class);
            Method method = Method.builder().id(5).name("convert").type(type).build();

            int callsCount = 10000;
            for (int i = 0; i < callsCount; i++) {
                serializedRecordedMethodCallList.addEnterMethodCall(method.getId(), typeResolver, new A(), new Object[]{5});
                serializedRecordedMethodCallList.addExitMethodCall(i, typeResolver, "ABC");
            }

            InputBytesList read = out.flip();

            RecordedMethodCalls list = new RecordedMethodCalls(read);
            assertEquals(callsCount * 2, list.size());
            AddressableItemIterator<RecordedMethodCall> it = list.iterator(new InMemoryRepository<>());

            for (int i = 0; i < callsCount * 2; i++) {
                it.next();
            }
        }
    }

    @Test
    void testAddAndIterate() {
        OutputBytesList out = new OutputBytesList(SerializedRecordedMethodCallList.WIRE_ID, new PagedMemBytesOut(pageAllocator()));
        SerializedRecordedMethodCallList serializedRecordedMethodCallList = new SerializedRecordedMethodCallList(333, out);

        Type type = typeResolver.get(A.class);
        Method method = Method.builder().id(5).name("convert").type(type).build();

        serializedRecordedMethodCallList.addEnterMethodCall(method.getId(), typeResolver, new A(), new Object[]{5});
        serializedRecordedMethodCallList.addExitMethodCall(1, typeResolver, "ABC");

        InputBytesList read = out.flip();
        RecordedMethodCalls list = new RecordedMethodCalls(read);

        AddressableItemIterator<RecordedMethodCall> it = list.iterator(new InMemoryRepository<>());

        RecordedEnterMethodCall enterCall = (RecordedEnterMethodCall) it.next();

        assertEquals(5, enterCall.getMethodId());
        List<ObjectRecord> arguments = enterCall.getArguments();
        assertEquals(1, arguments.size());
        IntegralRecord numberRecord = (IntegralRecord) arguments.get(0);
        assertEquals(5, numberRecord.getValue());

        RecordedExitMethodCall exitCall = (RecordedExitMethodCall) it.next();

        assertEquals(1, exitCall.getCallId());
    }

    private MemPageAllocator pageAllocator() {
        return new MemPageAllocator() {

            @Override
            public MemPage allocate() {
                return new MemPage(0, new UnsafeBuffer(new byte[PageConstants.PAGE_SIZE]));
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