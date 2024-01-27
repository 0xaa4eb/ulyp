/*
package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.Page;
import com.ulyp.core.mem.PageAllocator;
import org.agrona.DirectBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class PagedMemBinaryOutput extends AbstractBinaryOutput {

    private final PageAllocator pageAllocator;
    private final List<Page> pages;

    public PagedMemBinaryOutput(PageAllocator pageAllocator) {
        this.pageAllocator = pageAllocator;
        this.pages = new ArrayList<>();
        this.pages.add(pageAllocator.allocate());
    }

    private Page pageAt(int pos) {
        int pageIndex = pos >> Page.PAGE_BITS;
        if (pageIndex < pages.size()) {
            return pages.get(pageIndex);
        }
        if (pageIndex > pages.size() + 2) {
            throw new IllegalArgumentException("Can not borrow too many pages");
        }
        while (pageIndex >= pages.size()) {
            pages.add(pageAllocator.allocate());
        }
        return pages.get(pageIndex);
    }

    private int currentPageRemaining() {
        int pageIndex = pos >> Page.PAGE_BITS;

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
    public void write(DirectBuffer buffer) {

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
    public int size() {
        return 0;
    }

    @Override
    public void writeAt(int offset, int value) {
        buffer.putInt(offset, value);
    }

    @Override
    public void dispose() {
        for (int i = 0; i < pages.size(); i++) {
            pageDeallocator.accept(pages.get(i));
        }
    }

    @Override
    public DirectBuffer copy() {
        return null;
    }
}
*/
