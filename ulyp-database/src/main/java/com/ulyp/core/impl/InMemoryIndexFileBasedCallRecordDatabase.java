package com.ulyp.core.impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.ulyp.core.*;
import com.ulyp.core.printers.*;
import com.ulyp.core.printers.bytes.BinaryInputImpl;
import com.ulyp.transport.*;
import it.unimi.dsi.fastutil.ints.*;
import it.unimi.dsi.fastutil.longs.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class InMemoryIndexFileBasedCallRecordDatabase implements CallRecordDatabase {

    private static final IntArrayList EMPTY_LIST = new IntArrayList();

    private boolean open = true;
    private final IndexedLog enterRecordsLog;
    private final IndexedLog exitRecordsLog;
    private final AtomicLong totalCount = new AtomicLong(0);
    private final Int2ObjectMap<IntArrayList> children = new Int2ObjectOpenHashMap<>();
    private final Int2IntMap idToSubtreeCountMap = new Int2IntOpenHashMap();
    private final Int2LongMap enterRecordPos = new Int2LongOpenHashMap();
    private final Int2LongMap exitRecordPos = new Int2LongOpenHashMap();
    private final Long2ObjectMap<TypeInfo> classIdMap = new Long2ObjectOpenHashMap<>();
    private final DecodingContext decodingContext = new DecodingContext(classIdMap);
    private final Long2ObjectMap<TMethodInfoDecoder> methodDescriptionMap = new Long2ObjectOpenHashMap<>();
    private final IntArrayList currentRootStack = new IntArrayList();
    private final byte[] tmpBuf = new byte[512 * 1024];

    public InMemoryIndexFileBasedCallRecordDatabase() {
        this("");
    }

    public InMemoryIndexFileBasedCallRecordDatabase(String name) {
        exitRecordPos.defaultReturnValue(-1L);
        enterRecordPos.defaultReturnValue(-1L);
        idToSubtreeCountMap.defaultReturnValue(-1);

        try {
            File enterRecodsLogFile = File.createTempFile("ulyp-" + name + "-enter-records", null);
            enterRecordsLog = new IndexedLog(enterRecodsLogFile);
            File exitRecordsLogFile = File.createTempFile("ulyp-" + name + "-exit-records", null);
            exitRecordsLog = new IndexedLog(exitRecordsLogFile);
            enterRecodsLogFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public synchronized void persistBatch(
            CallEnterRecordList enterRecords,
            CallExitRecordList exitRecords,
            MethodInfoList methodInfoList,
            List<TClassDescription> classDescriptionList)
    {
        checkOpen();

        Iterator<TMethodInfoDecoder> iterator = methodInfoList.copyingIterator();
        while (iterator.hasNext()) {
            TMethodInfoDecoder methodDescription = iterator.next();
            methodDescriptionMap.put(methodDescription.id(), methodDescription);
        }

        for (TClassDescription classDescription : classDescriptionList) {
            classIdMap.put(
                    classDescription.getId(),
                    new NameOnlyTypeInfo(classDescription.getId(), classDescription.getName())
            );
        }

        long prevEnterRecordPos = enterRecordsLog.pos();
        long prevExitRecordPos = exitRecordsLog.pos();

        enterRecordsLog.write(enterRecords.toByteString().toByteArray());
        exitRecordsLog.write(exitRecords.toByteString().toByteArray());

        AddressableItemIterator<TCallEnterRecordDecoder> enterRecordIterator = enterRecords.iterator();

        while (enterRecordIterator.hasNext()) {
            long addr = enterRecordIterator.address();
            TCallEnterRecordDecoder enterRecord = enterRecordIterator.next();
            enterRecordPos.put((int) enterRecord.callId(), prevEnterRecordPos + addr);
        }

        AddressableItemIterator<TCallExitRecordDecoder> exitIterator = exitRecords.iterator();

        while (exitIterator.hasNext()) {
            long addr = exitIterator.address();
            TCallExitRecordDecoder exitRecord = exitIterator.next();
            exitRecordPos.put((int) exitRecord.callId(), prevExitRecordPos + addr);
        }

        updateChildrenParentAndSubtreeCountMaps(enterRecords, exitRecords);
    }

    private synchronized void updateChildrenParentAndSubtreeCountMaps(CallEnterRecordList enterRecords, CallExitRecordList exitRecords) {
        checkOpen();

        PeekingIterator<TCallEnterRecordDecoder> enterRecordIt = Iterators.peekingIterator(enterRecords.iterator());
        PeekingIterator<TCallExitRecordDecoder> exitRecordIt = Iterators.peekingIterator(exitRecords.iterator());

        if (currentRootStack.isEmpty()) {
            TCallEnterRecordDecoder enterRecord = enterRecordIt.next();
            if (enterRecord.callId() != 0) {
                throw new RuntimeException("Call id of the root must be 0");
            }
            currentRootStack.push((int) enterRecord.callId());
            idToSubtreeCountMap.put((int) enterRecord.callId(), 1);
            totalCount.lazySet(totalCount.get() + 1);
        }

        while (enterRecordIt.hasNext() || exitRecordIt.hasNext()) {
            int currentCallId = currentRootStack.topInt();

            if (exitRecordIt.hasNext() && exitRecordIt.peek().callId() == currentCallId) {
                exitRecordIt.next();
                int id = currentRootStack.popInt();
                IntArrayList childrenList = children.get(id);
                if (childrenList != null) {
                    childrenList.trim();
                }
            } else if (enterRecordIt.hasNext()) {
                TCallEnterRecordDecoder enterRecord = enterRecordIt.next();

                children.computeIfAbsent(currentCallId, i -> new IntArrayList()).add((int) enterRecord.callId());
                idToSubtreeCountMap.put((int) enterRecord.callId(), 1);

                for (int i = 0; i < currentRootStack.size(); i++) {
                    int id = currentRootStack.getInt(i);
                    idToSubtreeCountMap.put(id, idToSubtreeCountMap.get(id) + 1);
                }

                currentRootStack.push((int) enterRecord.callId());
                totalCount.lazySet(totalCount.get() + 1);
            } else {
                if (!currentRootStack.isEmpty() && currentRootStack.size() > 1) {
                    currentRootStack.popInt();
                } else {
                    throw new RuntimeException("Inconsistent state");
                }
            }
        }
    }

    private static final int RECORD_HEADER_LENGTH = 2 * Integer.BYTES;

    @Override
    public synchronized CallRecord find(long id) {
        checkOpen();

        long enterRecordAddress = enterRecordPos.get((int) id);
        if (enterRecordAddress == -1) {
            return null;
        }

        int bytesRead = enterRecordsLog.readAt(enterRecordAddress, tmpBuf);
        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(tmpBuf, 0, bytesRead);
        TCallEnterRecordDecoder enterRecordDecoder = new TCallEnterRecordDecoder();
        int blockLength = unsafeBuffer.getInt(Integer.BYTES);
        enterRecordDecoder.wrap(unsafeBuffer, RECORD_HEADER_LENGTH, blockLength, 0);

        List<ObjectRepresentation> args = new ArrayList<>();

        TCallEnterRecordDecoder.ArgumentsDecoder arguments = enterRecordDecoder.arguments();
        while (arguments.hasNext()) {
            arguments = arguments.next();
            UnsafeBuffer buffer = new UnsafeBuffer();
            arguments.wrapValue(buffer);
            args.add(ObjectBinaryPrinterType.printerForId(arguments.printerId()).read(
                    classIdMap.get(arguments.classId()),
                    new BinaryInputImpl(buffer),
                    decodingContext)
            );
        }

        UnsafeBuffer buffer = new UnsafeBuffer();
        enterRecordDecoder.wrapCallee(buffer);

        TypeInfo calleeTypeInfo = classIdMap.get(enterRecordDecoder.calleeClassId());

        ObjectRepresentation callee = ObjectBinaryPrinterType.printerForId(enterRecordDecoder.calleePrinterId()).read(
                calleeTypeInfo,
                new BinaryInputImpl(buffer),
                decodingContext
        );
        CallRecord callRecord = new CallRecord(
                id,
                callee,
                args,
                methodDescriptionMap.get(enterRecordDecoder.methodId()),
                this
        );

        long exitPos = exitRecordPos.get((int) id);
        if (exitPos == -1) {
            return callRecord;
        }
        bytesRead = exitRecordsLog.readAt(exitPos, tmpBuf);
        unsafeBuffer = new UnsafeBuffer(tmpBuf, 0, bytesRead);
        TCallExitRecordDecoder exitRecordDecoder = new TCallExitRecordDecoder();
        exitRecordDecoder.wrap(unsafeBuffer, RECORD_HEADER_LENGTH, unsafeBuffer.getInt(Integer.BYTES), 0);

        UnsafeBuffer returnValueBuffer = new UnsafeBuffer();
        exitRecordDecoder.wrapReturnValue(returnValueBuffer);
        ObjectBinaryPrinter printer = ObjectBinaryPrinterType.printerForId(exitRecordDecoder.returnPrinterId());
        ObjectRepresentation returnValue = printer.read(classIdMap.get(exitRecordDecoder.returnClassId()), new BinaryInputImpl(returnValueBuffer), decodingContext);
        boolean thrown = exitRecordDecoder.thrown() == BooleanType.T;

        callRecord.setReturnValue(returnValue);
        callRecord.setThrown(thrown);

        return callRecord;
    }

    @Override
    public synchronized LongList getChildrenIds(long id) {
        IntList childrenIds = children.getOrDefault((int) id, EMPTY_LIST);
        LongArrayList longs = new LongArrayList();
        for (int i = 0; i < childrenIds.size(); i++) {
            longs.add(childrenIds.getInt(i));
        }
        return longs;
    }

    @Override
    public long countAll() {
        return totalCount.get();
    }

    @Override
    public long getSubtreeCount(long id) {
        return idToSubtreeCountMap.get((int) id);
    }

    private synchronized void checkOpen() {
        if (!open) {
            throw new IllegalStateException("Database is closed");
        }
    }

    @Override
    public synchronized void close() {
        try {
            checkOpen();

            enterRecordsLog.close();
            exitRecordsLog.close();
        } finally {
            open = false;
        }
    }
}
