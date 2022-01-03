package com.ulyp.core.impl;

import com.ulyp.core.*;
import com.ulyp.core.recorders.*;
import com.ulyp.database.DatabaseException;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public abstract class CallRecordDatabaseTest {

    protected abstract CallRecordDatabase build(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws DatabaseException;

    private final MethodInfoDatabase methodInfoDatabase = new MethodInfoDatabase();
    private final TypeInfoDatabase typeInfoDatabase = new TypeInfoDatabase();
    private final TestTypeResolver typeResolver = new TestTypeResolver();

    @Before
    public final void setUp() {
        MethodInfoList methodInfos = new MethodInfoList();

        Method toStringMethods = Method.builder()
                .id(100)
                .name("toString")
                .returnsSomething(true)
                .isStatic(false)
                .isConstructor(false)
                .declaringType(typeResolver.get(CallRecordDatabaseTest.class))
                .build();

        methodInfos.add(toStringMethods);

        methodInfoDatabase.addAll(methodInfos);
    }

    @Test
    public void testRecordsSaving() throws DatabaseException {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();

        enterRecords.add(
                0,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(0, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");

        CallRecordDatabase database = build(methodInfoDatabase, typeInfoDatabase);

        database.persistBatch(enterRecords, exitRecords);

        CallRecord root = database.getRoot();

        assertThat(root.getChildren(), Matchers.empty());
        assertEquals("toString", root.getMethodName());
    }

    @Test
    public void testFieldsSavingWithTwoCallRecords() throws DatabaseException {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();

        enterRecords.add(
                0,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(1, 100, typeResolver, false, RecorderType.STRING_RECORDER.getInstance(), "zzzxzxzx");
        exitRecords.add(0, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");

        CallRecordDatabase database = build(methodInfoDatabase, typeInfoDatabase);

        database.persistBatch(enterRecords, exitRecords);

        assertEquals(2L, database.countAll());

        CallRecord firstCall = database.getRoot();

        CallRecord secondCall = firstCall.getChildren().get(0);

        StringObjectRecord returnValue = (StringObjectRecord) secondCall.getReturnValue();
        assertEquals("zzzxzxzx", returnValue.value());
    }

    @Test
    public void testSavingAsWholeChunk() throws DatabaseException {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();

        enterRecords.add(
                0,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                2,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(2, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");
        exitRecords.add(1, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");
        exitRecords.add(0, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");

        CallRecordDatabase database = build(methodInfoDatabase, typeInfoDatabase);

        database.persistBatch(enterRecords, exitRecords);

        assertEquals(3L, database.countAll());

        CallRecord root = database.getRoot();

        assertEquals(0, root.getId());
        assertTrue(root.getParameterNames().isEmpty());
        assertThat(root.getMethodName(), Matchers.is("toString"));

        ObjectRecord returnValue = root.getReturnValue();
        assertThat(returnValue, Matchers.instanceOf(IdentityObjectRecord.class));

        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testSavingPartialChunk() throws DatabaseException {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();

        enterRecords.add(
                0,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                2,
                100,
                typeResolver,
                new ObjectRecorder[] {RecorderType.IDENTITY_RECORDER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(2, 100, typeResolver, false, RecorderType.IDENTITY_RECORDER.getInstance(), "asdasdad");

        CallRecordDatabase database = build(methodInfoDatabase, typeInfoDatabase);

        database.persistBatch(enterRecords, exitRecords);

        assertEquals(3L, database.countAll());

        CallRecord root = database.getRoot();

        assertTrue(root.getParameterNames().isEmpty());
        assertFalse(root.isComplete());

        assertThat(database.find(root.getId() + 1), Matchers.notNullValue());
        assertThat(database.find(root.getId() + 2), Matchers.notNullValue());
    }
}
