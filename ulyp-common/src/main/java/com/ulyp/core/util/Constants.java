package com.ulyp.core.util;

public class Constants {

    public static final int CACHE_LINE_BYTES_PADDING = 256; // it's too much but memory isn't a problem
    public static final int CACHE_LINE_INTS_COUNT = CACHE_LINE_BYTES_PADDING / Integer.BYTES;
    public static final int CACHE_LINE_INTS_BITS_SHIFT = BitUtil.log2(CACHE_LINE_INTS_COUNT);
}