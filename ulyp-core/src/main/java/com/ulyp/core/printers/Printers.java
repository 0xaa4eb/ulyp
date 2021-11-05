package com.ulyp.core.printers;

import com.ulyp.core.Type;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Printers {

    private static final Printers instance = new Printers();
    private static final ObjectRecorder[] empty = new ObjectRecorder[0];
    private static final ObjectRecorder[] printers;

    static {
        printers = new ObjectRecorder[ObjectBinaryPrinterType.values().length];

        List<ObjectBinaryPrinterType> printerTypes = new ArrayList<>();
        printerTypes.addAll(Arrays.asList(ObjectBinaryPrinterType.values()));
        printerTypes.sort(Comparator.comparing(ObjectBinaryPrinterType::getOrder));

        for (int i = 0; i < printerTypes.size(); i++) {
            printers[i] = printerTypes.get(i).getInstance();
        }
    }

    public static Printers getInstance() {
        return instance;
    }

    public ObjectRecorder[] determinePrintersForParameterTypes(List<Type> paramsTypes) {
        try {
            if (paramsTypes.isEmpty()) {
                return empty;
            }
            ObjectRecorder[] convs = new ObjectRecorder[paramsTypes.size()];
            for (int i = 0; i < convs.length; i++) {
                convs[i] = determinePrinterForType(paramsTypes.get(i));
            }
            return convs;
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare converters for method params " + paramsTypes, e);
        }
    }

    public ObjectRecorder determinePrinterForReturnType(Type returnType) {
        try {
            return determinePrinterForType(returnType);
        } catch (Exception e) {
            throw new RuntimeException("Could not prepare converters for method params " + returnType, e);
        }
    }

    private static final ConcurrentMap<Type, ObjectRecorder> cache = new ConcurrentHashMap<>(1024);

    public ObjectRecorder determinePrinterForType(Type type) {
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
        for (ObjectRecorder printer : printers) {
            if (printer.supports(type)) {
                return printer;
            }
        }
        throw new RuntimeException("Could not find a suitable printer for type " + type);
    }
}
