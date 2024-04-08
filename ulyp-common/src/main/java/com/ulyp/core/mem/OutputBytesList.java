package com.ulyp.core.mem;

import com.ulyp.core.TypeResolver;
import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;
import com.ulyp.core.bytes.BytesOutputSink;
import com.ulyp.core.bytes.Mark;
import lombok.SneakyThrows;
import org.agrona.DirectBuffer;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.util.function.Consumer;

public class OutputBytesList implements AutoCloseable {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int ID_OFFSET = SIZE_OFFSET + Integer.BYTES;
    public static final int HEADER_LENGTH = ID_OFFSET + Integer.BYTES;

    private final BytesOut bytesOut;
    private final Writer writer = new Writer();
    private int size = 0;

    public OutputBytesList(int id, BytesOut bytesOut) {
        this.bytesOut = bytesOut;
        this.bytesOut.write(MAGIC);
        this.bytesOut.write(0);
        this.bytesOut.write(id);
    }

    public Writer writer() {
        writer.position = bytesOut.position();
        bytesOut.write(0);
        return writer;
    }

    public void add(Consumer<BytesOut> writeCallback) {
        int position = bytesOut.position();
        bytesOut.write(0);
        writeCallback.accept(bytesOut);
        int bytesWritten = bytesOut.bytesWritten(position) - Integer.BYTES; // TODO check initial int span multiple pages
        bytesOut.writeAt(position, bytesWritten);
        incSize();
    }

    public int writeTo(BytesOutputSink sink) throws IOException {
        return bytesOut.writeTo(sink);
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

    public void close() throws RuntimeException {
        this.bytesOut.close();
    }

    @TestOnly
    public InputBytesList flip() {
        return new InputBytesList(bytesOut.flip());
    }

    public class Writer implements BytesOut {
        int position;

        public void commit() {
            int bytesWritten = bytesOut.bytesWritten(position) - Integer.BYTES; // TODO check initial int span multiple pages
            bytesOut.writeAt(position, bytesWritten);
            incSize();
        }

        public int recursionDepth() {
            return bytesOut.recursionDepth();
        }

        public BytesOut nest() {
            return bytesOut.nest();
        }

        public Mark mark() {
            return bytesOut.mark();
        }

        public int position() {
            return bytesOut.position();
        }

        public int bytesWritten(int prevOffset) {
            return bytesOut.bytesWritten(prevOffset);
        }

        public void writeAt(int offset, int value) {
            bytesOut.writeAt(offset, value);
        }

        public void write(boolean value) {
            bytesOut.write(value);
        }

        public void write(int value) {
            bytesOut.write(value);
        }

        public void write(long value) {
            bytesOut.write(value);
        }

        public void write(byte c) {
            bytesOut.write(c);
        }

        public void write(DirectBuffer buffer) {
            bytesOut.write(buffer);
        }

        public void write(byte[] bytes) {
            bytesOut.write(bytes);
        }

        public void write(String value) {
            bytesOut.write(value);
        }

        public void write(Object object, TypeResolver typeResolver) throws Exception {
            bytesOut.write(object, typeResolver);
        }

        public void write(char val) {
            bytesOut.write(val);
        }

        public DirectBuffer copy() {
            return bytesOut.copy();
        }

        public void close() throws RuntimeException {
            bytesOut.close();
        }

        public int writeTo(BytesOutputSink sink) throws IOException {
            return bytesOut.writeTo(sink);
        }

        @Override
        @TestOnly
        @SneakyThrows
        public BytesIn flip() {
            return bytesOut.flip();
        }
    }
}
