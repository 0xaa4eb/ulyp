package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.bytes.BytesIn;

import java.util.NoSuchElementException;

public class InputBytesList implements Iterable<BytesIn> {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int MAGIC_OFFSET = 0;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int ID_OFFSET = SIZE_OFFSET + Integer.BYTES;
    public static final int HEADER_LENGTH = ID_OFFSET + Integer.BYTES;
    private static final int RECORD_HEADER_LENGTH = Integer.BYTES;

    private final BytesIn bytesIn;

    public InputBytesList(BytesIn bytesIn) {
        this.bytesIn = bytesIn;
        if (bytesIn.readInt() != MAGIC) {
            throw new IllegalArgumentException("Magic is " + getMagic() + ", wrong buffer is provided");
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
    public AddressableItemIterator<BytesIn> iterator() {
        return new Iterator();
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    private class Iterator implements AddressableItemIterator<BytesIn> {

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
        public BytesIn next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int pos = bytesIn.getPosition();
            int length = bytesIn.readInt();
            BytesIn wrappedBytes = bytesIn.readBytes(pos + RECORD_HEADER_LENGTH, length);
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
