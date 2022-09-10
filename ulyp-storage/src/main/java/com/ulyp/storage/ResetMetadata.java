package com.ulyp.storage;

import com.ulyp.core.ProcessMetadata;
import com.ulyp.core.mem.MethodList;
import com.ulyp.core.mem.TypeList;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ResetMetadata {

    private final ProcessMetadata processMetadata;
    private final TypeList types;
    private final MethodList methods;
}

