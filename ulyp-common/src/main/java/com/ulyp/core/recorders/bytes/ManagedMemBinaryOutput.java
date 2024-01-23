package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.ManagedMemPool;
import com.ulyp.core.mem.Page;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ManagedMemBinaryOutput extends AbstractBinaryOutput {

    private final Supplier<Page> pageSupplier;
    private final List<Page> pages;

    public ManagedMemBinaryOutput(Supplier<Page> pageSupplier) {
        this.pageSupplier = pageSupplier;
        this.pages = new ArrayList<>();
    }

    private Page pageAt(int pos) {
        int pageIndex = pos >> ManagedMemPool.PAGE_BITS;
        if (pageIndex < pages.size()) {
            return pages.get(pageIndex);
        }
        if (pageIndex > pages.size() + 1) {
            throw new IllegalArgumentException("Can not borrow too many pages");
        }
        while (pageIndex >= pages.size()) {
            pages.add();
        }
    }

    public void write(boolean value) {
        write(value ? 1 : 0);
    }

    public void write(int value) {
        buffer.putInt(pos, value);
        pos += Integer.BYTES;
    }

    public void write(long value) {
        buffer.putLong(pos, value);
        pos += Long.BYTES;
    }

    public void write(byte c) {
        buffer.putByte(pos, c);
        pos += Byte.BYTES;
    }

    @Override
    public void write(char val) {
        buffer.putChar(pos, val);
        pos += Character.BYTES;
    }

    public void write(byte[] bytes) {
        write(bytes.length);
        buffer.putBytes(pos, bytes);
        pos += bytes.length;
    }

    @Override
    public int writeTo(OutputStream outputStream) throws IOException {
        for (int i = 0; i < pos; i++) {
            outputStream.write(buffer.getByte(i));
        }
        return pos;
    }

    @Override
    public void writeAt(int offset, int value) {
        buffer.putInt(offset, value);
    }

    @Override
    public void dispose() {

    }
}
