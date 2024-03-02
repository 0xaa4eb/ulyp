package com.ulyp.storage.writer;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.mem.SerializedMethodList;
import com.ulyp.core.mem.SerializedTypeList;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResetRequest {

    private final ProcessMetadata processMetadata;
    private final SerializedTypeList types;
    private final SerializedMethodList methods;
}

