package com.ulyp.core.impl;

import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.ulyp.core.*;
import com.ulyp.core.printers.ObjectBinaryPrinter;
import com.ulyp.core.printers.ObjectBinaryPrinterType;
import com.ulyp.core.printers.ObjectRepresentation;
import com.ulyp.core.printers.TypeInfo;
import com.ulyp.core.printers.bytes.BinaryInputImpl;
import com.ulyp.transport.*;
import it.unimi.dsi.fastutil.longs.*;
import org.agrona.concurrent.UnsafeBuffer;
import org.dizitart.no2.Nitrite;
import org.dizitart.no2.mvstore.MVStoreModule;
import org.dizitart.no2.repository.ObjectRepository;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class OnDiskFileBasedCallRecordDatabase implements CallRecordDatabase {

    private final OutputStream enterRecordsOutputStream;
    private final RandomAccessFile enterRecordRandomAccess;
    private final RandomAccessFile exitRecordRandomAccess;
    private final OutputStream exitRecordsOutputStream;
    private long lastEnterAddress = 0;
    private long lastExitAddress = 0;

    private final AtomicLong totalCount = new AtomicLong(0);
    private final Long2ObjectMap<TypeInfo> classIdMap = new Long2ObjectOpenHashMap<>();
    private final DecodingContext decodingContext = new DecodingContext(classIdMap);
    private final Long2ObjectMap<TMethodInfoDecoder> methodDescriptionMap = new Long2ObjectOpenHashMap<>();
    private final Deque<OnDiskCallRecordInfo> currentPathFromRoot = new ArrayDeque<>();
    private final byte[] tmpBuf = new byte[512 * 1024];

    private final ObjectRepository<OnDiskCallRecordInfo> repository;

    public OnDiskFileBasedCallRecordDatabase() {
        this("");
    }

    public OnDiskFileBasedCallRecordDatabase(String name) {
        try {
            // TODO shutdown hook

            File tempFile = File.createTempFile("ulyp-" + name + "-database", ".db");
            tempFile.delete();

            MVStoreModule storeModule = MVStoreModule.withConfig()
                    .filePath(tempFile)
                    .compress(false)
                    .build();

            Nitrite db = Nitrite.builder()
                    .loadModule(storeModule)
                    .openOrCreate("user", "password");

            this.repository = db.getRepository(OnDiskCallRecordInfo.class);

            File enterRecordsFile = File.createTempFile("ulyp-" + name + "-enter-records", ".db");
            enterRecordsFile.deleteOnExit();
            File exitRecordsFile = File.createTempFile("ulyp-" + name + "-exit-records", ".db");
            exitRecordsFile.deleteOnExit();

            this.enterRecordsOutputStream = new BufferedOutputStream(new FileOutputStream(enterRecordsFile, false));
            this.exitRecordsOutputStream = new BufferedOutputStream(new FileOutputStream(exitRecordsFile, false));
            this.enterRecordRandomAccess = new RandomAccessFile(enterRecordsFile, "r");
            this.exitRecordRandomAccess = new RandomAccessFile(exitRecordsFile, "r");
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

        try {
            updateChildrenParentAndSubtreeCountMaps(enterRecords, exitRecords);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void updateChildrenParentAndSubtreeCountMaps(CallEnterRecordList enterRecords, CallExitRecordList exitRecords) throws IOException {
        long prevEnterRecordPos = lastEnterAddress;
        long prevExitRecordPos = lastExitAddress;

        byte[] bytes = enterRecords.toByteString().toByteArray();
        enterRecordsOutputStream.write(bytes);
        enterRecordsOutputStream.flush();
        lastEnterAddress += bytes.length;

        bytes = exitRecords.toByteString().toByteArray();
        exitRecordsOutputStream.write(bytes);
        exitRecordsOutputStream.flush();
        lastExitAddress += bytes.length;

        AddressableItemIterator<TCallEnterRecordDecoder> enterRecordAddrIt = enterRecords.iterator();
        AddressableItemIterator<TCallExitRecordDecoder> exitRecordAddrIt = exitRecords.iterator();
        PeekingIterator<TCallEnterRecordDecoder> enterRecordIt = Iterators.peekingIterator(enterRecords.iterator());
        PeekingIterator<TCallExitRecordDecoder> exitRecordIt = Iterators.peekingIterator(exitRecords.iterator());

        if (currentPathFromRoot.isEmpty()) {
            TCallEnterRecordDecoder enterRecord = enterRecordIt.next();
            if (enterRecord.callId() != 0) {
                throw new RuntimeException("Call id of the root must be 0");
            }
            long addr = enterRecordAddrIt.address();
            enterRecordAddrIt.next();

            OnDiskCallRecordInfo record = new OnDiskCallRecordInfo();
            record.setId(enterRecord.callId());
            record.setSubtreeCallCount(1);
            record.setEnterRecordAddress(prevEnterRecordPos + addr);
            currentPathFromRoot.push(record);
            totalCount.lazySet(totalCount.get() + 1);
            repository.insert(record);
        }

        while (enterRecordIt.hasNext() || exitRecordIt.hasNext()) {
            OnDiskCallRecordInfo parent = currentPathFromRoot.getLast();
            long currentCallId = parent.getId();

            if (exitRecordIt.hasNext() && exitRecordIt.peek().callId() == currentCallId) {
                long address = exitRecordAddrIt.address();
                exitRecordIt.next();
                exitRecordAddrIt.next();
                OnDiskCallRecordInfo record = currentPathFromRoot.removeLast();
                record.setExitRecordAddress(prevExitRecordPos + address);
                repository.update(record);
            } else if (enterRecordIt.hasNext()) {
                TCallEnterRecordDecoder enterRecord = enterRecordIt.next();

                long addr = enterRecordAddrIt.address();
                enterRecordAddrIt.next();
                OnDiskCallRecordInfo record = new OnDiskCallRecordInfo();
                record.setId(enterRecord.callId());
                record.setSubtreeCallCount(1);
                record.setEnterRecordAddress(prevEnterRecordPos + addr);

                parent.getChildrenIds().add(record.getId());

                /*idToParentIdMap.put((int) enterRecord.callId(), (int) currentCallId);*/
                // children.computeIfAbsent((int) currentCallId, i -> new IntArrayList()).add((int) enterRecord.callId());

                for (OnDiskCallRecordInfo pathToRoot : currentPathFromRoot) {
                    pathToRoot.setSubtreeCallCount(pathToRoot.getSubtreeCallCount() + 1);
                }

                currentPathFromRoot.addLast(record);
                totalCount.lazySet(totalCount.get() + 1);
                repository.insert(record);
            } else {
                throw new RuntimeException("Inconsistent state");
            }
        }

        for (OnDiskCallRecordInfo pathToRoot : currentPathFromRoot) {
            repository.update(pathToRoot);
        }
    }

    private static final int RECORD_HEADER_LENGTH = 2 * Integer.BYTES;

    @Override
    public synchronized CallRecord find(long id) {
        OnDiskCallRecordInfo record = repository.getById(id);
        if (record == null) {
            return null;
        }

        long enterRecordAddress = record.getEnterRecordAddress();
        if (enterRecordAddress == -1) {
            return null;
        }
        try {
            enterRecordRandomAccess.seek(enterRecordAddress);

            int bytesRead = enterRecordRandomAccess.read(tmpBuf);
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

            long exitPos = record.getExitRecordAddress();
            if (record.getExitRecordAddress() == -1) {
                return callRecord;
            }
            exitRecordRandomAccess.seek(exitPos);
            bytesRead = exitRecordRandomAccess.read(tmpBuf);
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized List<Long> getChildrenIds(long id) {
        OnDiskCallRecordInfo record = repository.getById(id);
        return record.getChildrenIds();
    }

    @Override
    public long countAll() {
        return totalCount.get();
    }

    @Override
    public long getSubtreeCount(long id) {
        OnDiskCallRecordInfo record = repository.getById(id);
        return record.getSubtreeCallCount();
    }

    @Override
    public synchronized void close() {
        try {
            enterRecordsOutputStream.close();
            exitRecordsOutputStream.close();
            exitRecordRandomAccess.close();
            enterRecordRandomAccess.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
