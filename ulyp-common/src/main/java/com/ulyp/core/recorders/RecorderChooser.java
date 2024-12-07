package com.ulyp.core.recorders;

import com.ulyp.core.Type;
import com.ulyp.core.exception.RecordingException;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Finds {@link ObjectRecorder} that best matches for any given {@link Type}
 */
public class RecorderChooser {

    @Getter
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

    public ObjectRecorder chooseForType(Class<?> type) {
        for (ObjectRecorder recorder : allRecorders) {
            if (recorder.supports(type)) {
                return recorder;
            }
        }
        // Should never happen
        throw new RecordingException("Could not find a suitable recorder for type " + type);
    }
}
