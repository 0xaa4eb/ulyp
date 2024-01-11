package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import lombok.Getter;
import org.agrona.DirectBuffer;

@Getter
public class QueuedRecordedObject {

    private final Type type;
    private final byte recorderId;
    private final DirectBuffer data;

    public QueuedRecordedObject(Type type, byte recorderId, DirectBuffer data) {
        this.type = type;
        this.recorderId = recorderId;
        this.data = data;
    }
}
