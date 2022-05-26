package com.ulyp.core.util;

import com.google.common.base.Preconditions;

import java.text.DecimalFormat;

/**
 * Prints byte size to human-readable format
 */
public class ByteSize {

    private static final long BYTE = 1L;
    private static final long KB = BYTE << 10;
    private static final long MB = KB << 10;
    private static final long GB = MB << 10;
    private static final long TB = GB << 10;
    private static final long PB = TB << 10;
    private static final long EB = PB << 10;
    private static final DecimalFormat DEC_FORMAT = new DecimalFormat("#.##");

    private final long byteSize;

    public ByteSize(long byteSize) {
        Preconditions.checkState(byteSize >= 0, "Byte size can't be negatie");
        this.byteSize = byteSize;
    }

    public static String toHumanReadable(long size) {
        if (size >= EB) return formatSize(size, EB, "EB");
        if (size >= PB) return formatSize(size, PB, "PB");
        if (size >= TB) return formatSize(size, TB, "TB");
        if (size >= GB) return formatSize(size, GB, "GB");
        if (size >= MB) return formatSize(size, MB, "MB");
        if (size >= KB) return formatSize(size, KB, "KB");
        return formatSize(size, BYTE, "Bytes");
    }

    private static String formatSize(long size, long divider, String unitName) {
        return DEC_FORMAT.format((double) size / divider) + " " + unitName;
    }

    public ByteSize addBytes(long byteSize) {
        return new ByteSize(this.byteSize + byteSize);
    }

    @Override
    public String toString() {
        return toHumanReadable(byteSize);
    }
}
