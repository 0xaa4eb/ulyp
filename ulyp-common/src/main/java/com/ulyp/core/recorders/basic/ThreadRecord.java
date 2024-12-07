package com.ulyp.core.recorders.basic;

import com.ulyp.core.Type;
import com.ulyp.core.recorders.IdentityObjectRecord;
import com.ulyp.core.recorders.ObjectRecord;
import lombok.Getter;

@Getter
public class ThreadRecord extends ObjectRecord {

    private final IdentityObjectRecord identity;
    private final long tid;
    private final String name;

    protected ThreadRecord(Type type, IdentityObjectRecord identity, long tid, String name) {
        super(type);

        this.identity = identity;
        this.tid = tid;
        this.name = name;
    }

    @Override
    public String toString() {
        return identity + "(tid=" + tid + ", name='" + name + "')";
    }
}
