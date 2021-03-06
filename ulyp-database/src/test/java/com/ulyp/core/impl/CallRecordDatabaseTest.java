package com.ulyp.core.impl;

import com.ulyp.core.*;
import com.ulyp.core.printers.*;
import com.ulyp.transport.TClassDescription;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public abstract class CallRecordDatabaseTest {

    protected abstract CallRecordDatabase build();

    private final TestAgentRuntime agentRuntime = new TestAgentRuntime();

    @Test
    public void testRecordsSaving() {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();
        MethodInfoList methodInfos = new MethodInfoList();

        MethodInfo toStringMethod = new MethodInfo(
                100,
                "toString",
                false,
                false,
                true,
                new ArrayList<>(),
                agentRuntime.get(String.class),
                agentRuntime.get(OnDiskFileBasedCallRecordDatabaseTest.class)
        );

        methodInfos.add(toStringMethod);

        enterRecords.add(
                0,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(0, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");

        CallRecordDatabase database = build();

        database.persistBatch(enterRecords, exitRecords, methodInfos, Collections.emptyList());

        CallRecord root = database.getRoot();

        assertThat(root.getChildren(), Matchers.empty());
        assertEquals("toString", root.getMethodName());
    }

    @Test
    public void testFieldsSavingWithTwoCallRecords() {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();
        MethodInfoList methodInfos = new MethodInfoList();

        MethodInfo toStringMethod = new MethodInfo(
                100,
                "toString",
                false,
                false,
                true,
                new ArrayList<>(),
                agentRuntime.get(String.class),
                agentRuntime.get(OnDiskFileBasedCallRecordDatabaseTest.class)
        );

        methodInfos.add(toStringMethod);

        enterRecords.add(
                0,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(1, 100, agentRuntime, false, ObjectBinaryPrinterType.STRING_PRINTER.getInstance(), "zzzxzxzx");
        exitRecords.add(0, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");

        CallRecordDatabase database = build();

        database.persistBatch(enterRecords, exitRecords, methodInfos, Collections.emptyList());

        assertEquals(2L, database.countAll());

        CallRecord firstCall = database.getRoot();

        CallRecord secondCall = firstCall.getChildren().get(0);

        StringObjectRepresentation returnValue = (StringObjectRepresentation) secondCall.getReturnValue();
        assertEquals("zzzxzxzx", returnValue.getValue());
    }

    @Test
    public void testSavingAsWholeChunk() {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();
        MethodInfoList methodInfos = new MethodInfoList();

        MethodInfo toStringMethod = new MethodInfo(
                100,
                "toString",
                false,
                false,
                true,
                new ArrayList<>(),
                agentRuntime.get(String.class),
                agentRuntime.get(OnDiskFileBasedCallRecordDatabaseTest.class)
        );

        methodInfos.add(toStringMethod);

        enterRecords.add(
                0,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                2,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(2, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");
        exitRecords.add(1, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");
        exitRecords.add(0, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");

        CallRecordDatabase database = build();

        database.persistBatch(enterRecords, exitRecords, methodInfos, Collections.emptyList());

        assertEquals(3L, database.countAll());

        CallRecord root = database.find(0);

        assertEquals(0, root.getId());
        assertTrue(root.getParameterNames().isEmpty());
        assertThat(root.getMethodName(), Matchers.is("toString"));

        ObjectRepresentation returnValue = root.getReturnValue();
        assertThat(returnValue, Matchers.instanceOf(IdentityObjectRepresentation.class));

        assertEquals(1, root.getChildren().size());
    }

    @Test
    public void testSavingPartialChunk() {
        CallEnterRecordList enterRecords = new CallEnterRecordList();
        CallExitRecordList exitRecords = new CallExitRecordList();
        MethodInfoList methodInfos = new MethodInfoList();
        List<TClassDescription> classDescriptionList = new ArrayList<>();

        MethodInfo toStringMethod = new MethodInfo(
                100,
                "toString",
                false,
                false,
                true,
                new ArrayList<>(),
                agentRuntime.get(String.class),
                agentRuntime.get(OnDiskFileBasedCallRecordDatabaseTest.class));

        methodInfos.add(toStringMethod);

        enterRecords.add(
                0,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                1,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        enterRecords.add(
                2,
                100,
                agentRuntime,
                new ObjectBinaryPrinter[] {ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance()},
                this,
                new Object[]{}
        );
        exitRecords.add(2, 100, agentRuntime, false, ObjectBinaryPrinterType.IDENTITY_PRINTER.getInstance(), "asdasdad");

        CallRecordDatabase database = build();

        database.persistBatch(enterRecords, exitRecords, methodInfos, classDescriptionList);

        assertEquals(3L, database.countAll());

        CallRecord root = database.getRoot();

        assertEquals(0, root.getId());
        assertTrue(root.getParameterNames().isEmpty());
        assertFalse(root.isComplete());

        assertThat(database.find(1), Matchers.notNullValue());
        assertThat(database.find(2), Matchers.notNullValue());
    }
}
