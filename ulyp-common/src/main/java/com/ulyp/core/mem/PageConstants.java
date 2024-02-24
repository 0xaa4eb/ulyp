package com.ulyp.core.mem;

import com.ulyp.core.util.BitUtil;
import com.ulyp.core.util.SystemPropertyUtil;

public class PageConstants {

    public static final int PAGE_SIZE = SystemPropertyUtil.getInt("ulyp.common.page.size", 32768);
    public static final int PAGE_BITS = BitUtil.log2(PAGE_SIZE);
    public static final int PAGE_BYTE_SIZE_MASK = PAGE_SIZE - 1;

    private PageConstants() {
        throw new UnsupportedOperationException();
    }
}
