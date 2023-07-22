package com.ulyp.core.recorders;

import com.ulyp.core.Type;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Finds {@link ObjectRecorder} that best matches for any given {@link Type}
 */
public class RecorderChooser {

    private static final RecorderChooser instance = new RecorderChooser();
    private static final ObjectRecorder[] allRecorders;

    static {
        allRecorders = new ObjectRecorder[ObjectRecorderRegistry.values().length];

        List<ObjectRecorderRegistry> objectRecorderTypes = new ArrayList<>();
        objectRecorderTypes.addAll(Arrays.asList(ObjectRecorderRegistry.values()));
        objectRecorderTypes.sort(Comparator.comparing(ObjectRecorderRegistry::getOrder));

        for (int i = 0; i < objectRecorderTypes.size(); i++) {
            allRecorders[i] = objectRecorderTypes.get(i).getInstance();
        }
    }

    public static RecorderChooser getInstance() {
        return instance;
    }

    private final Map<Class<?>, ObjectRecorder> byTypeCache = new ConcurrentHashMap<>();

    public ObjectRecorder chooseForType(Class<?> type) {
        for (ObjectRecorder recorder : allRecorders) {
            if (recorder.supports(type)) {
                return recorder;
            }
        }
        // Should never happen
        throw new RuntimeException("Could not find a suitable recorder for type " + type);
    }
}
