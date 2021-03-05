package com.ulyp.core.printers;

public enum CollectionsRecordingMode {

    JAVA {
        @Override
        public boolean supports(TypeInfo typeInfo) {
            return typeInfo.getTraits().contains(TypeTrait.CONCRETE_CLASS) && typeInfo.getName().startsWith("java");
        }
    },
    ALL {
        @Override
        public boolean supports(TypeInfo typeInfo) {
            return true;
        }
    },
    NONE {
        @Override
        public boolean supports(TypeInfo typeInfo) {
            return false;
        }
    };

    public abstract boolean supports(TypeInfo typeInfo);
}
