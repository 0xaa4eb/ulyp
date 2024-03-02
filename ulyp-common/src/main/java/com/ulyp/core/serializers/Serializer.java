package com.ulyp.core.serializers;

import com.ulyp.core.bytes.BytesIn;
import com.ulyp.core.bytes.BytesOut;

public interface Serializer<T> {

    T deserialize(BytesIn input);

    void serialize(BytesOut out, T object);
}
