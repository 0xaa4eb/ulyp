package com.ulyp.core.printers;

public enum ObjectBinaryPrinterType {
    CLASS_OBJECT_PRINTER(new ClassObjectRecorder((byte) 1), 20),
    STRING_PRINTER(new StringRecorder((byte) 2), 0),
    THROWABLE_PRINTER(new ThrowableRecorder((byte) 5), 20),
    ENUM_PRINTER(new EnumRecorder((byte) 6), 5),
    DYNAMIC_OBJECT_PRINTER(new DynamicObjectBinaryRecorder((byte) 7), 100),
    INTEGRAL_PRINTER(new IntegralRecorder((byte) 12), 0),
    BOOLEAN_PRINTER(new BooleanRecorder((byte) 100), 1),
    ANY_NUMBER_PRINTER(new NumbersRecorder((byte) 8), 1),
    OBJECT_ARRAY_PRINTER(new ObjectArrayDebugRecorder((byte) 11), 1),
    COLLECTION_DEBUG_PRINTER(new CollectionRecorder((byte) 10), 1),
    MAP_PRINTER(new MapRecorder((byte) 13), 1),
    DATE_PRINTER(new DateRecorder((byte) 20), 90),
    TO_STRING_PRINTER(new ToStringRecorder((byte) 91), 99),

    // identity can be used for any objects
    IDENTITY_PRINTER(new IdentityRecorder((byte) 0), Integer.MAX_VALUE / 2),

    NULL_PRINTER(new NullObjectRecorder((byte) 9), Integer.MAX_VALUE);

    public static final ObjectBinaryRecorder[] printers = new ObjectBinaryRecorder[256];

    static {
        for (ObjectBinaryPrinterType printerType : values()) {
            if (printers[printerType.getInstance().getId()] != null) {
                throw new RuntimeException("Duplicate id");
            }
            printers[printerType.getInstance().getId()] = printerType.getInstance();
        }
    }

    public static ObjectBinaryRecorder printerForId(byte id) {
        return printers[id];
    }

    private final ObjectBinaryRecorder instance;
    private final int order;

    ObjectBinaryPrinterType(ObjectBinaryRecorder instance, int order) {
        this.instance = instance;
        this.order = order;
    }

    public ObjectBinaryRecorder getInstance() {
        return instance;
    }

    public int getOrder() {
        return order;
    }
}
