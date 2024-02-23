package com.ulyp.core.bytes;

import com.ulyp.core.mem.PageConstants;
import com.ulyp.core.mem.MemPage;
import com.ulyp.core.mem.MemPageAllocator;
import org.agrona.DirectBuffer;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PagedMemBinaryOutput extends AbstractBinaryOutput {

    private final List<MarkImpl> unusedMarks = new ArrayList<>();

    private final MemPageAllocator pageAllocator;
    private final List<MemPage> pages;

    public PagedMemBinaryOutput(MemPageAllocator pageAllocator) {
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
            PagedMemBinaryOutput.this.position = markPos;
        }

        @Override
        public void close() throws RuntimeException {
            if (unusedMarks.size() < 3) {
                unusedMarks.add(this);
            }
        }
    }

    @Override
    public Mark mark() {
        MarkImpl newMark;
        if (!unusedMarks.isEmpty()) {
            newMark = unusedMarks.remove(unusedMarks.size() - 1);
        } else {
            newMark = new MarkImpl();
        }
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
        write(value ? 1 : 0);
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
        write(bytesLength);
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
        write(bytesLength);
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
    public int writeTo(BinaryOutputSink sink) throws IOException {
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

    @Override
    public void dispose() {
        for (MemPage page : pages) {
            page.dispose();
        }
    }
}
