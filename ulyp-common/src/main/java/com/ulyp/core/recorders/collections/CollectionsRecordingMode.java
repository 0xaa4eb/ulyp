package com.ulyp.core.recorders.collections;

import java.util.List;

public enum CollectionsRecordingMode {

    /**
     * Records all java collections/maps (i.e. those which are concrete classes and have package java.*).
     * This is a safer, because some collections which have side-effect iteration (like Hibernate persisted collections)
     * are not recorded.
     */
    JDK {
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
     * Records all kotlin standard library collections
     */
    KT {
        @Override
        public boolean supports(Class<?> type) {
            return type.getName().startsWith("kotlin.");
        }

        @Override
        public String toString() {
            return "Recording kotlin.* collections";
        }
    },
    /**
     * The most intrusive mode: try to record elements on every collection. Might break things
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
     * Do not iterate and record elements in collections/maps. The most safe mode
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

    public static boolean isDisabled(List<CollectionsRecordingMode> modes) {
        return modes.size() == 1 && modes.get(0) == NONE;
    }
}
