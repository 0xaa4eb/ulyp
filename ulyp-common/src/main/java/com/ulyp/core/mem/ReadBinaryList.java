package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.core.recorders.bytes.BinaryInput;
import java.util.NoSuchElementException;

public class ReadBinaryList implements Iterable<BinaryInput> {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int MAGIC_OFFSET = 0;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int ID_OFFSET = SIZE_OFFSET + Integer.BYTES;
    public static final int HEADER_LENGTH = ID_OFFSET + Integer.BYTES;
    private static final int RECORD_HEADER_LENGTH = Integer.BYTES;

    private final BinaryInput binaryInput;

    public ReadBinaryList(BinaryInput binaryInput) {
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
            return currentRecordAddress;
        }
    }
}
