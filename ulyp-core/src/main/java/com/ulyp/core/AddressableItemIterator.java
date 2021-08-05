package com.ulyp.core;

import java.util.Iterator;

public interface AddressableItemIterator<T> extends Iterator<T> {

    long address();
}
