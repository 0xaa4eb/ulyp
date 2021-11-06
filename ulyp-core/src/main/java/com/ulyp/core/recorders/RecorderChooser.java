package com.ulyp.core.recorders;

import com.ulyp.core.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Finds {@link ObjectRecorder} that best matches for any given {@link Type}
 */
public class RecorderChooser {

    private static final RecorderChooser instance = new RecorderChooser();
    private static final ObjectRecorder[] empty = new ObjectRecorder[0];
    private static final ObjectRecorder[] allRecorders;

    static {
        allRecorders = new ObjectRecorder[RecorderType.values().length];

        List<RecorderType> printerTypes = new ArrayList<>();
        printerTypes.addAll(Arrays.asList(RecorderType.values()));
        printerTypes.sort(Comparator.comparing(RecorderType::getOrder));

        for (int i = 0; i < printerTypes.size(); i++) {
            allRecorders[i] = printerTypes.get(i).getInstance();
        }
    }

    public static RecorderChooser getInstance() {
        return instance;
    }

    public ObjectRecorder[] chooseRecordersForParameterTypes(List<Type> paramsTypes) {
        try {
            if (paramsTypes.isEmpty()) {
                return empty;
            }
            ObjectRecorder[] convs = new ObjectRecorder[paramsTypes.size()];
            for (int i = 0; i < convs.length; i++) {
                convs[i] = chooseRecorderForType(paramsTypes.get(i));
            }
            return convs;
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare converters for method params " + paramsTypes, e);
        }
    }

    public ObjectRecorder chooseRecordersForReturnType(Type returnType) {
        try {
            return chooseRecorderForType(returnType);
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare converters for method params " + returnType, e);
        }
    }

    private static final ConcurrentMap<Type, ObjectRecorder> cache = new ConcurrentHashMap<>(1024);

    public ObjectRecorder chooseRecorderForType(Type type) {
//        return cache.computeIfAbsent(
//                type, t -> {
//                    for (ObjectBinaryPrinter printer : printers) {
//                        if (printer.supports(t)) {
//                            return printer;
//                        }
//                    }
//                    throw new RuntimeException("Could not find a suitable printer for type " + type);
//                }
//        );
        for (ObjectRecorder recorder : allRecorders) {
            if (recorder.supports(type)) {
                return recorder;
            }
        }
        throw new RuntimeException("Could not find a suitable printer for type " + type);
    }
}
