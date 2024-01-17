package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.recorders.bytes.BinaryInput;
import com.ulyp.core.recorders.bytes.BinaryOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.function.Consumer;


/**
 * Off-heap list which stores all data flat in memory
 */
public class BinaryList {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int MAGIC_OFFSET = 0;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int ID_OFFSET = SIZE_OFFSET + Integer.BYTES;
    public static final int HEADER_LENGTH = ID_OFFSET + Integer.BYTES;
    private static final int RECORD_HEADER_LENGTH = Integer.BYTES;

    public static class In implements Iterable<BinaryInput> {

        private final BinaryInput binaryInput;

        public In(BinaryInput binaryInput) {
            this.binaryInput = binaryInput;
            if (binaryInput.readInt() != MAGIC) {
                throw new RuntimeException("Magic is " + getMagic());
            }
        }

        public int size() {
            return binaryInput.readInt(SIZE_OFFSET);
        }

        private int getMagic() {
            return binaryInput.readInt(MAGIC_OFFSET);
        }

        public int id() {
            return binaryInput.readInt(ID_OFFSET);
        }

        @Override
        public AddressableItemIterator<BinaryInput> iterator() {
            return new Iterator();
        }

        public boolean isEmpty() {
            return size() == 0;
        }

        private class Iterator implements AddressableItemIterator<BinaryInput> {

            private int nextRecordAddress = HEADER_LENGTH;
            private int currentRecordAddress = -1;

            public Iterator() {
                binaryInput.moveTo(HEADER_LENGTH);
            }

            @Override
            public boolean hasNext() {
                return nextRecordAddress < binaryInput.available() && binaryInput.readIntAt(nextRecordAddress) > 0;
            }

            @Override
            public BinaryInput next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int pos = binaryInput.getPosition();
                int length = binaryInput.readInt();
                BinaryInput wrappedBytes = binaryInput.readBytes(pos + RECORD_HEADER_LENGTH, length);
                currentRecordAddress = pos;
                nextRecordAddress += RECORD_HEADER_LENGTH + length;
                binaryInput.moveTo(nextRecordAddress);
                return wrappedBytes;
            }

            @Override
            public long address() {
                return currentRecordAddress + RECORD_HEADER_LENGTH;
            }
        }
    }

    public static class Out {

        private final BinaryOutput binaryOutput;
        private int size = 0;

        public Out(int id, BinaryOutput binaryOutput) {
            this.binaryOutput = binaryOutput;
            this.binaryOutput.write(MAGIC);
            this.binaryOutput.write(0);
            this.binaryOutput.write(id);
        }

        public void add(Consumer<BinaryOutput> writeCallback) {
            int offset = binaryOutput.currentOffset();
            binaryOutput.write(0);
            writeCallback.accept(binaryOutput);
            int bytesWritten = binaryOutput.currentOffset() - offset - Integer.BYTES;
            binaryOutput.writeAt(offset, bytesWritten);
            incSize();
        }

        public int writeTo(OutputStream outputStream) throws IOException {
            return binaryOutput.writeTo(outputStream);
        }

        private void incSize() {
            size++;
            setSize(size);
        }

        private void setSize(int value) {
            binaryOutput.writeAt(SIZE_OFFSET, value);
        }

        public int bytesWritten() {
            return binaryOutput.currentOffset();
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }
    }
}
