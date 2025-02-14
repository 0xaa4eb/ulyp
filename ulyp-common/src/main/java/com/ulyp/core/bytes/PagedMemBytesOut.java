package com.ulyp.core.bytes;

import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import com.ulyp.core.mem.PageConstants;
import com.ulyp.core.util.FixedSizeObjectPool;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Growable binary output which requests page by page from the specified allocator. When container grows a new page is
 * requested and no copying happens
 */
public class PagedMemBytesOut extends AbstractBytesOut {

    private final FixedSizeObjectPool<MarkImpl> marksPool = new FixedSizeObjectPool<>(
            MarkImpl::new,
            3
    );

    private final MemPageAllocator pageAllocator;
    private final List<MemPage> pages;

    public PagedMemBytesOut(MemPageAllocator pageAllocator) {
        this.pageAllocator = pageAllocator;
        this.pages = new ArrayList<>();
        this.pages.add(pageAllocator.allocate());
    }

    private class MarkImpl implements Mark {

        private int markPos;
        private int writtenBytes = 0;

        @Override
        public int writtenBytes() {
            return writtenBytes;
        }

        @Override
        public void rollback() {
            PagedMemBytesOut.this.position = markPos;
        }

        @Override
        public void close() throws RuntimeException {
            marksPool.requite(this);
        }
    }

    @Override
    public Mark mark() {
        MarkImpl newMark = marksPool.borrow();
        newMark.markPos = this.position;
        newMark.writtenBytes = 0;
        return newMark;
    }

    @Override
    public int bytesWritten(int prevPosition) {
        MemPage prevPage = pageAt(prevPosition);
        MemPage currentPage = pageAt(position);
        if (prevPage == currentPage) {
            return position - prevPosition;
        } else {
            int prevPageIndex = pages.indexOf(prevPage);
            int bytesWritten = position - prevPosition;
            for (int i = prevPageIndex; pages.get(i) != currentPage; i++) {
                bytesWritten -= pages.get(i).getUnused();
            }
            return bytesWritten;
        }
    }

    private MemPage currentPage() {
        return pageAt(position);
    }

    private MemPage pageAt(int pos) {
        int pageIndex = pos >> PageConstants.PAGE_BITS;
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
        return PageConstants.PAGE_SIZE - (position & PageConstants.PAGE_BYTE_SIZE_MASK);
    }

    private void ensureAllocated() {
        pageAt(position);
    }

    public void write(boolean value) {
        byte byteValue = value ? (byte) 1 : (byte) 0;
        write(byteValue);
    }

    public void write(int value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Integer.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putInt(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            position += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putInt(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        }
        addWrittenBytes(Integer.BYTES);
    }

    @Override
    public void writeVarInt(int v) {
        // TODO 99..99999% writes hit same page, optimize this
        do {
            int bits = v & 0x7F;
            v >>>= 7;
            byte b = (byte) (bits + ((v != 0) ? 0x80 : 0));
            write(b);
        } while (v != 0);
    }

    public void write(long value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Long.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putLong(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            position += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putLong(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        }
        addWrittenBytes(Long.BYTES);
    }

    private void addWrittenBytes(int delta) {
        position += delta;
    }

    public void write(byte value) {
        MemPage page = currentPage();
        UnsafeBuffer buffer = page.getBuffer();
        buffer.putByte(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        addWrittenBytes(Byte.BYTES);
    }

    @Override
    public void write(char value) {
        MemPage page = currentPage();
        int remainingBytes = currentPageRemainingBytes();

        if (Character.BYTES <= remainingBytes) {
            UnsafeBuffer buffer = page.getBuffer();
            buffer.putChar(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        } else {
            page.setUnused(remainingBytes);
            position += remainingBytes;
            ensureAllocated();
            page = currentPage();
            page.getBuffer().putChar(position & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        }
        addWrittenBytes(Character.BYTES);
    }

    @Override
    public DirectBuffer copy() {
        return null;
    }

    @Override
    public void write(DirectBuffer buffer) {
        int bytesLength = buffer.capacity();
        writeVarInt(bytesLength);
        int offset = 0;
        while (bytesLength > 0) {
            MemPage page = currentPage();
            int remainingBytes = currentPageRemainingBytes();
            int bytesWritten = Math.min(remainingBytes, bytesLength);
            page.getBuffer().putBytes(position & PageConstants.PAGE_BYTE_SIZE_MASK, buffer, offset, bytesWritten);
            offset += bytesWritten;
            bytesLength -= bytesWritten;
            addWrittenBytes(bytesWritten);
        }
    }

    public void write(byte[] value) {
        int bytesLength = value.length;
        writeVarInt(bytesLength);
        int offset = 0;
        while (bytesLength > 0) {
            MemPage page = currentPage();
            int remainingBytes = currentPageRemainingBytes();
            int bytesWritten = Math.min(remainingBytes, bytesLength);
            page.getBuffer().putBytes(position & PageConstants.PAGE_BYTE_SIZE_MASK, value, offset, bytesWritten);
            offset += bytesWritten;
            bytesLength -= bytesWritten;
            addWrittenBytes(bytesWritten);
        }
    }

    @Override
    public int writeTo(BytesOutputSink sink) throws IOException {
        int totalBytesWritten = 0;
        int lastTouchedPage = position >> PageConstants.PAGE_BITS;
        for (int i = 0; i < lastTouchedPage; i++) {
            MemPage memPage = pages.get(i);
            int bytesToWrite = PageConstants.PAGE_SIZE - memPage.getUnused();
            sink.write(memPage.getBuffer(), bytesToWrite);
            totalBytesWritten += bytesToWrite;
        }
        if (lastTouchedPage < pages.size()) {
            MemPage lastPage = pages.get(lastTouchedPage);
            int bytesToWrite = position & PageConstants.PAGE_BYTE_SIZE_MASK;
            sink.write(lastPage.getBuffer(), bytesToWrite);
            totalBytesWritten += bytesToWrite;
        }
        return totalBytesWritten;
    }

    @Override
    public void writeAt(int offset, int value) {
        MemPage page = pageAt(offset);
        int posWithinPage = offset & PageConstants.PAGE_BYTE_SIZE_MASK;
        int remBytesPage = PageConstants.PAGE_SIZE - posWithinPage;
        if (remBytesPage >= Integer.BYTES) {
            page.getBuffer().putInt(posWithinPage, value);
        } else {
            offset += remBytesPage;
            page = pageAt(offset);
            page.getBuffer().putInt(offset & PageConstants.PAGE_BYTE_SIZE_MASK, value);
        }
    }

    private void dispose() {
        for (MemPage page : pages) {
            pageAllocator.deallocate(page);
        }
        pages.clear();
    }

    public void close() {
        recursionDepth--;
        if (recursionDepth < 0) {
            dispose();
        }
    }
}
