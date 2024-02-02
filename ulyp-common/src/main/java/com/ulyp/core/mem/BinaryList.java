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

    public static class Out {

        private final BinaryOutput bytesOut;
        private int size = 0;

        public Out(int id, BinaryOutput bytesOut) {
            this.bytesOut = bytesOut;
            this.bytesOut.write(MAGIC);
            this.bytesOut.write(0);
            this.bytesOut.write(id);
        }

        public void add(Consumer<BinaryOutput> writeCallback) {
            // TODO this is bad, should be redone
            int position = bytesOut.position();
            bytesOut.write(0);
            writeCallback.accept(bytesOut);
            // int bytesWritten = bytesOut.position() - position - Integer.BYTES;
            int bytesWritten = bytesOut.bytesWritten(position) - Integer.BYTES; // TODO check initial int span multiple pages
            bytesOut.writeAt(position, bytesWritten);
            incSize();
        }

        public int writeTo(OutputStream outputStream) throws IOException {
            return bytesOut.writeTo(outputStream);
        }

        private void incSize() {
            size++;
            setSize(size);
        }

        private void setSize(int value) {
            bytesOut.writeAt(SIZE_OFFSET, value);
        }

        public int bytesWritten() {
            return bytesOut.position();
        }

        public boolean isEmpty() {
            return size == 0;
        }

        public int size() {
            return size;
        }

        public void dispose() {
            this.bytesOut.dispose();
        }
    }

    public static class In implements Iterable<BinaryInput> {

        private final BinaryInput bytesIn;

        public In(BinaryInput bytesIn) {
            this.bytesIn = bytesIn;
            if (bytesIn.readInt() != MAGIC) {
                throw new RuntimeException("Magic is " + getMagic());
            }
        }

        public int size() {
            return bytesIn.readInt(SIZE_OFFSET);
        }

        private int getMagic() {
            return bytesIn.readInt(MAGIC_OFFSET);
        }

        public int id() {
            return bytesIn.readInt(ID_OFFSET);
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
                bytesIn.moveTo(HEADER_LENGTH);
            }

            @Override
            public boolean hasNext() {
                return nextRecordAddress < bytesIn.available() && bytesIn.readIntAt(nextRecordAddress) > 0;
            }

            @Override
            public BinaryInput next() {
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }
                int pos = bytesIn.getPosition();
                int length = bytesIn.readInt();
                BinaryInput wrappedBytes = bytesIn.readBytes(pos + RECORD_HEADER_LENGTH, length);
                currentRecordAddress = pos;
                nextRecordAddress += RECORD_HEADER_LENGTH + length;
                bytesIn.moveTo(nextRecordAddress);
                return wrappedBytes;
            }

            @Override
            public long address() {
                return (long) currentRecordAddress + RECORD_HEADER_LENGTH;
            }
        }
    }
}
