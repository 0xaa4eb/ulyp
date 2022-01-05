package com.ulyp.core.mem;

import com.ulyp.core.AddressableItemIterator;
import com.ulyp.transport.BinaryDataDecoder;
import com.ulyp.transport.BinaryDataEncoder;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.NoSuchElementException;
import java.util.function.Consumer;


/**
 * Off-heap list which stores all data flat in memory using SBE.
 * Both SBE encoder and decoder must be specified in descendant classes
 */
public class BinaryList implements Iterable<BinaryDataDecoder> {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int MAGIC_OFFSET = 0;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int BYTES_LENGTH_OFFSET = SIZE_OFFSET + Integer.BYTES;
    private static final int ID_OFFSET = BYTES_LENGTH_OFFSET + Integer.BYTES;

    private static final int LIST_HEADER_LENGTH = ID_OFFSET + Integer.BYTES;
    private static final int RECORD_HEADER_LENGTH = 2 * Integer.BYTES;

    private final BinaryDataEncoder encoder = new BinaryDataEncoder();

    protected final MutableDirectBuffer buffer;

    public BinaryList(byte[] buf) {
        buffer = new UnsafeBuffer(buf);
        if (getMagic() != MAGIC) {
            throw new RuntimeException("Magic is " + getMagic());
        }
    }

    public BinaryList(int id) {
        buffer = new ExpandableDirectByteBuffer(64 * 1024);
        setMagic(MAGIC);
        setSize(0);
        setId(id);
        setByteLength(LIST_HEADER_LENGTH);
    }

    protected void add(Consumer<BinaryDataEncoder> writer) {
        int recordHeaderAddr = byteLength();
        int recordAddr = recordHeaderAddr + RECORD_HEADER_LENGTH;
        encoder.wrap(buffer, recordAddr);
        writer.accept(encoder);
        buffer.putInt(recordHeaderAddr, encoder.encodedLength());
        buffer.putInt(recordHeaderAddr + Integer.BYTES, encoder.sbeBlockLength());

        addToLength(RECORD_HEADER_LENGTH + encoder.encodedLength());
        incSize();
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        int length = byteLength();
        for (int i = 0; i < length; i++) {
            outputStream.write(buffer.getByte(i));
        }
    }

    public byte[] toByteArray() {
        int length = byteLength();
        byte[] output = new byte[length];
        for (int i = 0; i < length; i++) {
            output[i] = buffer.getByte(i);
        }
        return output;
    }

    public MutableDirectBuffer getBuffer() {
        return buffer;
    }

    public int size() {
        return buffer.getInt(4);
    }

    private void incSize() {
        setSize(size() + 1);
    }

    private int getMagic() {
        return buffer.getInt(MAGIC_OFFSET);
    }

    private void setMagic(int value) {
        buffer.putInt(MAGIC_OFFSET, value);
    }

    public int getId() {
        return buffer.getInt(ID_OFFSET);
    }

    private void setId(int value) {
        buffer.putInt(ID_OFFSET, value);
    }

    private void setSize(int value) {
        buffer.putInt(4, value);
    }

    public int byteLength() {
        return buffer.getInt(BYTES_LENGTH_OFFSET);
    }

    private void setByteLength(int value) {
        buffer.putInt(BYTES_LENGTH_OFFSET, value);
    }

    private void addToLength(int delta) {
        setByteLength(byteLength() + delta);
    }

    @Override
    public AddressableItemIterator<BinaryDataDecoder> iterator() {
        return new Iterator();
    }

    private class Iterator implements AddressableItemIterator<BinaryDataDecoder> {

        private int recordAddress = LIST_HEADER_LENGTH;
        private int currentRecordAddress = -1;

        @Override
        public boolean hasNext() {
            return recordAddress < byteLength() && buffer.getInt(recordAddress) > 0;
        }

        @Override
        public BinaryDataDecoder next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int encodedLength = buffer.getInt(recordAddress);
            int blockLength = buffer.getInt(recordAddress + Integer.BYTES);

            BinaryDataDecoder decoder = new BinaryDataDecoder();
            decoder.wrap(buffer, recordAddress + RECORD_HEADER_LENGTH, blockLength, 0);
            currentRecordAddress = recordAddress + RECORD_HEADER_LENGTH;
            recordAddress += RECORD_HEADER_LENGTH + encodedLength;
            return decoder;
        }

        @Override
        public long address() {
            return currentRecordAddress;
        }
    }

}
