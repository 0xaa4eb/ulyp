package com.ulyp.core.recorders.basic;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.recorders.basic.ThreadRecord;
import com.ulyp.core.recorders.basic.ThreadRecorder;
import com.ulyp.core.util.ReflectionBasedTypeResolver;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ThreadRecorderTest {

    private final ThreadRecorder recorder = new ThreadRecorder((byte) 1);
    private final TypeResolver typeResolver = new ReflectionBasedTypeResolver();

    @Test
    void shouldRecordCurrentThread() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Thread currentThread = Thread.currentThread();

        recorder.write(currentThread, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Thread.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThreadRecord.class, objectRecord);
        ThreadRecord threadRecord = (ThreadRecord) objectRecord;
        assertEquals(currentThread.getId(), threadRecord.getTid());
        assertEquals(currentThread.getName(), threadRecord.getName());
    }

    @Test
    void shouldRecordCustomThread() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Thread customThread = new Thread("CustomThreadName");

        recorder.write(customThread, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Thread.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThreadRecord.class, objectRecord);
        ThreadRecord threadRecord = (ThreadRecord) objectRecord;
        assertEquals(customThread.getId(), threadRecord.getTid());
        assertEquals("CustomThreadName", threadRecord.getName());
    }

    @Test
    void shouldOnlySupportThreadClass() {
        assertTrue(recorder.supports(Thread.class));
        assertFalse(recorder.supports(Object.class));
        assertFalse(recorder.supports(Runnable.class));
        assertFalse(recorder.supports(ThreadGroup.class));
    }

    @Test
    void shouldSupportAsyncRecording() {
        assertTrue(recorder.supportsAsyncRecording());
    }

    @Test
    void shouldRecordThreadWithSpecialCharactersInName() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Thread specialThread = new Thread("Thread with special chars: !@#$%^&*()");

        recorder.write(specialThread, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Thread.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThreadRecord.class, objectRecord);
        ThreadRecord threadRecord = (ThreadRecord) objectRecord;
        assertEquals(specialThread.getId(), threadRecord.getTid());
        assertEquals("Thread with special chars: !@#$%^&*()", threadRecord.getName());
    }

    @Test
    void shouldRecordThreadWithUnicodeName() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Thread unicodeThread = new Thread("线程 🧵 поток");

        recorder.write(unicodeThread, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Thread.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThreadRecord.class, objectRecord);
        ThreadRecord threadRecord = (ThreadRecord) objectRecord;
        assertEquals(unicodeThread.getId(), threadRecord.getTid());
        assertEquals("线程 🧵 поток", threadRecord.getName());
    }

    @Test
    void shouldPreserveIdentityHashCode() throws Exception {
        BytesOut out = BytesOut.expandableArray();
        Thread testThread = new Thread("TestThread");

        recorder.write(testThread, out, typeResolver);

        ObjectRecord objectRecord = recorder.read(
            typeResolver.get(Thread.class),
            out.flip(),
            typeResolver::getById
        );

        assertInstanceOf(ThreadRecord.class, objectRecord);
        ThreadRecord threadRecord = (ThreadRecord) objectRecord;
        
        IdentityObjectRecord identityRecord = threadRecord.getIdentity();
        assertEquals(System.identityHashCode(testThread), identityRecord.getHashCode());
    }
} 