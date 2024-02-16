package com.ulyp.core.serializers;

import com.ulyp.core.bytes.BinaryInput;
import com.ulyp.core.bytes.BinaryOutput;

public interface Serializer<T> {

    T deserialize(BinaryInput input);

    void serialize(BinaryOutput out, T object);
}
