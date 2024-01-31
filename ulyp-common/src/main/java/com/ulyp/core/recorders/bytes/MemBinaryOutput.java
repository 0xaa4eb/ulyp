package com.ulyp.core.recorders.bytes;

import com.ulyp.core.mem.MemPool;
import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MemBinaryOutput extends AbstractBinaryOutput {

    private final MemPageAllocator pageAllocator;
    private final List<MemPage> pages;

    public MemBinaryOutput(MemPageAllocator pageAllocator) {
        this.pageAllocator = pageAllocator;
        this.pages = new ArrayList<>();
        this.pages.add(pageAllocator.allocate());
    }

    private MemPage currentPage() {
        return pageAt(pos);
    }

    private MemPage pageAt(int pos) {
        int pageIndex = pos >> MemPool.PAGE_BITS;
        if (pageIndex < pages.size()) {
            return pages.get(pageIndex);
        }
        if (pageIndex > pages.size() + 2) {
            throw new IllegalArgumentException("Can not borrow too many pages at once");
        }
        while (pageIndex >= pages.size()) {
            pages.add(pageAllocator.allocate());
        }
        return pages.get(pageIndex);
    }

    private int currentPageRemainingBytes() {
        return MemPool.PAGE_SIZE - (pos & MemPool.PAGE_BYTE_SIZE_MASK);
    }

    private void ensureAllocated() {
        pageAt(pos);
    }

    public void write(boolean value) {
        write(value ? 1 : 0);
    }

    public void write(int value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Integer.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putInt(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            pos += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putInt(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        }
        pos += Integer.BYTES;
    }

    public void write(long value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Long.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putLong(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            pos += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putLong(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        }
        pos += Long.BYTES;
    }

    public void write(byte value) {
        MemPage page = currentPage();
        UnsafeBuffer buffer = page.getBuffer();
        buffer.putByte(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        pos += Byte.BYTES;
    }

    @Override
    public void write(char value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Character.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putChar(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            pos += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putChar(pos & MemPool.PAGE_BYTE_SIZE_MASK, value);
        }
        pos += Character.BYTES;
    }

    public void write(byte[] value) {
        write(value.length);
        int bytesLength = value.length;
        int totalBytesWritten = 0;
        int offset = 0;
        while (totalBytesWritten != bytesLength) {
            MemPage page = currentPage();
            int remainingBytes = currentPageRemainingBytes();
            int bytesToWriteToPage = Math.min(remainingBytes, bytesLength);
            page.getBuffer().putBytes(pos, value, offset, bytesToWriteToPage);
            offset += bytesToWriteToPage;
            totalBytesWritten += bytesToWriteToPage;
        }
        pos += value.length;
    }

    @Override
    public int writeTo(OutputStream outputStream) throws IOException {
        // TODO this is slow
        int totalBytesWritten = 0;
        int lastTouchedPage = pos >> MemPool.PAGE_BITS;
        for (int i = 0; i < lastTouchedPage; i++) {
            MemPage memPage = pages.get(i);
            int bytesToWrite = MemPool.PAGE_SIZE - memPage.getUnused();
            UnsafeBuffer buffer = memPage.getBuffer();
            for (int b = 0; b < bytesToWrite; b++) {
                outputStream.write(buffer.getByte(b));
            }
            totalBytesWritten += bytesToWrite;
        }
        if (lastTouchedPage < pages.size()) {
            MemPage lastPage = pages.get(lastTouchedPage);
            int bytesToWrite = pos & MemPool.PAGE_BYTE_SIZE_MASK;
            UnsafeBuffer buffer = lastPage.getBuffer();
            for (int b = 0; b < bytesToWrite; b++) {
                outputStream.write(buffer.getByte(b));
            }
            totalBytesWritten += bytesToWrite;
        }
        return totalBytesWritten;
    }

    @Override
    public void writeAt(int offset, int value) {
/*        buffer.putInt(offset, value);*/
    }

    @Override
    public void dispose() {

    }
}
