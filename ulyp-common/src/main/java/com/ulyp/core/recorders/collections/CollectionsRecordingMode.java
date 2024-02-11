package com.ulyp.core.recorders.collections;

public enum CollectionsRecordingMode {

    /**
     * Records all java collections (i.e. those which are concrete classes and have package java.*).
     * This is a safer, because some collections which have side-effect iteration (like Hibernate persisted collections)
     * are not recorded.
     */
    JAVA {
        @Override
        public boolean supports(Class<?> type) {
            return type.getName().startsWith("java.") && !type.getName().startsWith("java.util.concurrent");
        }

        @Override
        public String toString() {
            return "Recording java.* collections";
        }
    },
    /**
     * The most intrusive mode: try to record items on every collection. Might break recordings
     */
    ALL {
        @Override
        public boolean supports(Class<?> type) {
            return true;
        }

        @Override
        public String toString() {
            return "Recording all collections";
        }
    },
    /**
     * Do not iterate and record items in collections. The most safe mode
     */
    NONE {
        @Override
        public boolean supports(Class<?> type) {
            return false;
        }

        @Override
        public String toString() {
            return "Do not record collections";
        }
    };

    public abstract boolean supports(Class<?> type);
}
