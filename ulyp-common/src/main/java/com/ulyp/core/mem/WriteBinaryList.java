package com.ulyp.core.mem;

import com.ulyp.core.recorders.bytes.BinaryOutput;

import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Consumer;

public class WriteBinaryList {

    private static final int MAGIC = Integer.MAX_VALUE / 3;
    private static final int MAGIC_OFFSET = 0;
    private static final int SIZE_OFFSET = Integer.BYTES;
    private static final int ID_OFFSET = SIZE_OFFSET + Integer.BYTES;

    private final BinaryOutput binaryOutput;
    private int size = 0;

    public WriteBinaryList(int id, BinaryOutput binaryOutput) {
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

    public boolean isEmpty() {
        return size == 0;
    }
}
