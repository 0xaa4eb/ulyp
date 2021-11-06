package com.ulyp.core.impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.ulyp.core.*;
import com.ulyp.core.recorders.ObjectRecorder;
import com.ulyp.core.recorders.ObjectBinaryPrinterType;
import com.ulyp.core.recorders.ObjectRecord;
import com.ulyp.core.Type;
import com.ulyp.core.recorders.bytes.BinaryInputImpl;
import com.ulyp.database.DatabaseException;
import com.ulyp.transport.BooleanType;
import com.ulyp.transport.TCallEnterRecordDecoder;
import com.ulyp.transport.TCallExitRecordDecoder;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FileBasedCallRecordDatabase implements CallRecordDatabase {

    private static final int RECORD_HEADER_LENGTH = 2 * Integer.BYTES;

    private boolean open = true;
    private final Index index;
    private final IndexedLog log;
    private final AtomicLong totalCount = new AtomicLong(0);

    private final TypeInfoDatabase typeInfoDatabase;
    private final MethodInfoDatabase methodInfoDatabase;
    private final ByIdTypeResolver decodingContext;

    private final MemCache memCache = new MemCache();

    private final byte[] tmpBuf = new byte[512 * 1024];
    private long rootId = -1;

    public FileBasedCallRecordDatabase(MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws DatabaseException {
        this("", methodInfoDatabase, typeInfoDatabase);
    }

    public FileBasedCallRecordDatabase(String name, MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase) throws DatabaseException {
        this(name, methodInfoDatabase, typeInfoDatabase, new InMemoryIndex());
    }

    public FileBasedCallRecordDatabase(String name, MethodInfoDatabase methodInfoDatabase, TypeInfoDatabase typeInfoDatabase, Index index) throws DatabaseException {
        this.methodInfoDatabase = methodInfoDatabase;
        this.typeInfoDatabase = typeInfoDatabase;
        this.decodingContext = new DecodingContext(typeInfoDatabase);

        try {
            File enterRecodsLogFile = File.createTempFile("ulyp-" + name + "-enter-records", null);
            this.log = new IndexedLog(enterRecodsLogFile);
            enterRecodsLogFile.deleteOnExit();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        this.index = index;
    }

    public synchronized void persistBatch(CallEnterRecordList enterRecords, CallExitRecordList exitRecords) throws DatabaseException {
        checkOpen();

        Long2LongMap enterRecordPosCache = new Long2LongOpenHashMap();
        Long2LongMap exitRecordPosCache = new Long2LongOpenHashMap();

        long prevEnterRecordPos = log.pos();

        log.write(enterRecords.toByteString().toByteArray());

        AddressableItemIterator<TCallEnterRecordDecoder> enterRecordIterator = enterRecords.iterator();

        while (enterRecordIterator.hasNext()) {
            long addr = enterRecordIterator.address();
            TCallEnterRecordDecoder enterRecord = enterRecordIterator.next();
            enterRecordPosCache.put(enterRecord.callId(), prevEnterRecordPos + addr);
        }

        long prevExitRecordPos = log.pos();
        log.write(exitRecords.toByteString().toByteArray());

        AddressableItemIterator<TCallExitRecordDecoder> exitIterator = exitRecords.iterator();

        while (exitIterator.hasNext()) {
            long addr = exitIterator.address();
            TCallExitRecordDecoder exitRecord = exitIterator.next();
            exitRecordPosCache.put(exitRecord.callId(), prevExitRecordPos + addr);
        }

        updateChildrenParentAndSubtreeCountMaps(enterRecords, exitRecords, enterRecordPosCache, exitRecordPosCache);
    }

    @Override
    public CallRecord getRoot() throws DatabaseException {
        return find(rootId);
    }

    private synchronized void updateChildrenParentAndSubtreeCountMaps(
            CallEnterRecordList enterRecords,
            CallExitRecordList exitRecords,
            Long2LongMap enterRecordPosCache,
            Long2LongMap exitRecordPosCache) throws DatabaseException
    {
        checkOpen();

        PeekingIterator<TCallEnterRecordDecoder> enterRecordIt = Iterators.peekingIterator(enterRecords.iterator());
        PeekingIterator<TCallExitRecordDecoder> exitRecordIt = Iterators.peekingIterator(exitRecords.iterator());

        if (memCache.isEmpty()) {
            TCallEnterRecordDecoder enterRecord = enterRecordIt.next();
            rootId = enterRecord.callId();
            memCache.insert(new CallRecordIndexMetadata(enterRecord.callId(), enterRecordPosCache.get(enterRecord.callId())));
            totalCount.lazySet(totalCount.get() + 1);
        }

        while (enterRecordIt.hasNext() || exitRecordIt.hasNext()) {
            long currentCallId = memCache.lastCallId();

            if (exitRecordIt.hasNext() && exitRecordIt.peek().callId() == currentCallId) {
                exitRecordIt.next();
                CallRecordIndexMetadata callRecordIndexMetadata = memCache.popLast();
                callRecordIndexMetadata.setExitAddressPos(exitRecordPosCache.get(callRecordIndexMetadata.getCallId()));
                index.update(callRecordIndexMetadata);
            } else if (enterRecordIt.hasNext()) {
                TCallEnterRecordDecoder enterRecord = enterRecordIt.next();
                memCache.insert(new CallRecordIndexMetadata(enterRecord.callId(), enterRecordPosCache.get(enterRecord.callId())));
                totalCount.lazySet(totalCount.get() + 1);
            } else {
                if (!memCache.isEmpty() && memCache.size() > 1) {
                    memCache.popLast();
                } else {
                    throw new RuntimeException("Inconsistent state");
                }
            }
        }
    }

    @Override
    public synchronized CallRecord find(long callId) throws DatabaseException {
        checkOpen();

        CallRecordIndexMetadata callRecordIndexMetadata = memCache.find(callId);

        long enterRecordAddress;
        if (callRecordIndexMetadata != null) {
            enterRecordAddress = callRecordIndexMetadata.getEnterAddressPos();
        } else {
            enterRecordAddress = index.getEnterCallAddress(callId);
        }
        if (enterRecordAddress == -1) {
            return null;
        }

        int bytesRead = log.readAt(enterRecordAddress, tmpBuf);
        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(tmpBuf, 0, bytesRead);
        TCallEnterRecordDecoder enterRecordDecoder = new TCallEnterRecordDecoder();
        int blockLength = unsafeBuffer.getInt(Integer.BYTES);
        enterRecordDecoder.wrap(unsafeBuffer, RECORD_HEADER_LENGTH, blockLength, 0);

        List<ObjectRecord> args = new ArrayList<>();

        TCallEnterRecordDecoder.ArgumentsDecoder arguments = enterRecordDecoder.arguments();
        while (arguments.hasNext()) {
            arguments = arguments.next();
            UnsafeBuffer buffer = new UnsafeBuffer();
            arguments.wrapValue(buffer);
            args.add(ObjectBinaryPrinterType.printerForId(arguments.printerId()).read(
                    typeInfoDatabase.find(arguments.typeId()),
                    new BinaryInputImpl(buffer),
                    decodingContext)
            );
        }

        UnsafeBuffer buffer = new UnsafeBuffer();
        enterRecordDecoder.wrapCallee(buffer);

        Type calleeType = typeInfoDatabase.find(enterRecordDecoder.calleeTypeId());

        ObjectRecord callee = ObjectBinaryPrinterType.printerForId(enterRecordDecoder.calleePrinterId()).read(
                calleeType,
                new BinaryInputImpl(buffer),
                decodingContext
        );
        CallRecord callRecord = new CallRecord(
                callId,
                callee,
                args,
                methodInfoDatabase.find(enterRecordDecoder.methodId()),
                this
        );

        long exitRecordAddress;
        if (callRecordIndexMetadata != null) {
            exitRecordAddress = callRecordIndexMetadata.getExitAddressPos();
        } else {
            exitRecordAddress = index.getExitCallAddress(callId);
        }
        if (exitRecordAddress == -1) {
            return callRecord;
        }
        bytesRead = log.readAt(exitRecordAddress, tmpBuf);
        unsafeBuffer = new UnsafeBuffer(tmpBuf, 0, bytesRead);
        TCallExitRecordDecoder exitRecordDecoder = new TCallExitRecordDecoder();
        exitRecordDecoder.wrap(unsafeBuffer, RECORD_HEADER_LENGTH, unsafeBuffer.getInt(Integer.BYTES), 0);

        UnsafeBuffer returnValueBuffer = new UnsafeBuffer();
        exitRecordDecoder.wrapReturnValue(returnValueBuffer);
        ObjectRecorder printer = ObjectBinaryPrinterType.printerForId(exitRecordDecoder.returnPrinterId());
        ObjectRecord returnValue = printer.read(typeInfoDatabase.find(exitRecordDecoder.returnTypeId()), new BinaryInputImpl(returnValueBuffer), decodingContext);
        boolean thrown = exitRecordDecoder.thrown() == BooleanType.T;

        callRecord.setReturnValue(returnValue);
        callRecord.setThrown(thrown);

        return callRecord;
    }

    @Override
    public synchronized LongList getChildrenIds(long id) throws DatabaseException {
        CallRecordIndexMetadata state = memCache.find(id);
        if (state != null) {
            return new LongArrayList(state.getChildren());
        }

        return new LongArrayList(index.getChildren(id));
    }

    @Override
    public long countAll() {
        return totalCount.get();
    }

    @Override
    public long getSubtreeCount(long callId) throws DatabaseException {

        CallRecordIndexMetadata callRecordIndexMetadata = memCache.find(callId);
        if (callRecordIndexMetadata != null) {
            return callRecordIndexMetadata.getSubtreeCount();
        }

        return index.getSubtreeCount(callId);
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

            log.close();
        } finally {
            open = false;
        }
    }
}
