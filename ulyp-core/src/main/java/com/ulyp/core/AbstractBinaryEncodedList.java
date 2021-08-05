package com.ulyp.core;

import com.google.protobuf.ByteString;
import org.agrona.ExpandableDirectByteBuffer;
import org.agrona.MutableDirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;
import org.agrona.sbe.MessageDecoderFlyweight;
import org.agrona.sbe.MessageEncoderFlyweight;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Off-heap list which stores all data flat in memory using SBE.
 * Both SBE encoder and decoder must be specified in descendant classes
 */
public abstract class AbstractBinaryEncodedList<Encoder extends MessageEncoderFlyweight, Decoder extends MessageDecoderFlyweight> implements Iterable<Decoder> {

    private static final int LIST_HEADER_LENGTH = 2 * Integer.BYTES;
    private static final int RECORD_HEADER_LENGTH = 2 * Integer.BYTES;

    private Encoder encoder;
    private Decoder decoder;
    private Supplier<Decoder> decoderSupplier;

    protected final MutableDirectBuffer buffer;

    public AbstractBinaryEncodedList() {
        buffer = new ExpandableDirectByteBuffer(64 * 1024);
        setSize(0);
        setLength(0);

        initEncoder();
    }

    public AbstractBinaryEncodedList(ByteString bytes) {
        buffer = new UnsafeBuffer(bytes.asReadOnlyByteBuffer());

        initEncoder();
    }

    public ByteString toByteString() {
        int bytesCount = length() + LIST_HEADER_LENGTH;
        ByteString.Output output = ByteString.newOutput(bytesCount);
        for (int i = 0; i < bytesCount; i++) {
            output.write(buffer.getByte(i));
        }
        return output.toByteString();
    }

    @SuppressWarnings("unchecked")
    private void initEncoder() {
        ParameterizedType parameterizedSuperclass = (ParameterizedType) getClass().getGenericSuperclass();
        Type[] genericTypes = parameterizedSuperclass.getActualTypeArguments();
        try {
            encoder = (Encoder) Class.forName(genericTypes[0].getTypeName()).newInstance();
            decoderSupplier = () -> {
                try {
                    return (Decoder) Class.forName(genericTypes[1].getTypeName()).newInstance();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            };
            decoder = decoderSupplier.get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected void add(Consumer<Encoder> writer) {
        int recordHeaderAddr = length() + LIST_HEADER_LENGTH;
        int recordAddr = recordHeaderAddr + RECORD_HEADER_LENGTH;
        encoder.wrap(buffer, recordAddr);
        writer.accept(encoder);
        buffer.putInt(recordHeaderAddr, encoder.encodedLength());
        buffer.putInt(recordHeaderAddr + Integer.BYTES, encoder.sbeBlockLength());

        addLength(RECORD_HEADER_LENGTH + encoder.encodedLength());
        incSize();
    }

    public int size() {
        return buffer.getInt(0);
    }

    protected void incSize() {
        setSize(size() + 1);
    }

    private void setSize(int value) {
        buffer.putInt(0, value);
    }

    public int length() {
        return buffer.getInt(4);
    }

    private void setLength(int value) {
        buffer.putInt(4, value);
    }

    private void addLength(int delta) {
        setLength(length() + delta);
    }

    @Override
    public AddressableItemIterator<Decoder> iterator() {
        return new ZeroCopyIterator();
    }

    public AddressableItemIterator<Decoder> copyingIterator() {
        return new CopyingIterator();
    }

    private class ZeroCopyIterator implements AddressableItemIterator<Decoder> {

        private int addr = LIST_HEADER_LENGTH;

        @Override
        public boolean hasNext() {
            return addr < length() && buffer.getInt(addr) > 0;
        }

        @Override
        public Decoder next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int encodedLength = buffer.getInt(addr);
            int blockLength = buffer.getInt(addr + Integer.BYTES);

            decoder.wrap(buffer, addr + RECORD_HEADER_LENGTH, blockLength, 0);
            addr += RECORD_HEADER_LENGTH + encodedLength;
            return decoder;
        }

        @Override
        public long address() {
            return addr;
        }
    }

    private class CopyingIterator implements AddressableItemIterator<Decoder> {

        private int addr = LIST_HEADER_LENGTH;

        @Override
        public boolean hasNext() {
            return addr < length() && buffer.getInt(addr) > 0;
        }

        @Override
        public Decoder next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            int encodedLength = buffer.getInt(addr);
            int blockLength = buffer.getInt(addr + Integer.BYTES);

            Decoder decoder = decoderSupplier.get();
            decoder.wrap(buffer, addr + RECORD_HEADER_LENGTH, blockLength, 0);
            addr += RECORD_HEADER_LENGTH + encodedLength;
            return decoder;
        }

        @Override
        public long address() {
            return addr;
        }
    }
}
