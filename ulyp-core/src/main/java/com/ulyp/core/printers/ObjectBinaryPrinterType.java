package com.ulyp.core.printers;

public enum ObjectBinaryPrinterType {
    CLASS_OBJECT_PRINTER(new ClassObjectPrinter((byte) 1), 20),
    STRING_PRINTER(new StringPrinter((byte) 2), 0),
    THROWABLE_PRINTER(new ThrowablePrinter((byte) 5), 20),
    ENUM_PRINTER(new EnumPrinter((byte) 6), 5),
    DYNAMIC_OBJECT_PRINTER(new DynamicObjectBinaryPrinter((byte) 7), 100),
    INTEGRAL_PRINTER(new IntegralPrinter((byte) 12), 0),
    BOOLEAN_PRINTER(new BooleanPrinter((byte) 100), 1),
    ANY_NUMBER_PRINTER(new NumbersPrinter((byte) 8), 1),
    OBJECT_ARRAY_PRINTER(new ObjectArrayDebugPrinter((byte) 11), 1),
    COLLECTION_DEBUG_PRINTER(new CollectionPrinter((byte) 10), 1),
    MAP_PRINTER(new MapPrinter((byte) 13), 1),
    DATE_PRINTER(new DatePrinter((byte) 20), 90),
    TO_STRING_PRINTER(new ToStringPrinter((byte) 91), 99),

    // identity can be used for any objects
    IDENTITY_PRINTER(new IdentityPrinter((byte) 0), Integer.MAX_VALUE / 2),

    NULL_PRINTER(new NullObjectPrinter((byte) 9), Integer.MAX_VALUE);

    public static final ObjectBinaryPrinter[] printers = new ObjectBinaryPrinter[256];

    static {
        for (ObjectBinaryPrinterType printerType : values()) {
            if (printers[printerType.getInstance().getId()] != null) {
                throw new RuntimeException("Duplicate id");
            }
            printers[printerType.getInstance().getId()] = printerType.getInstance();
        }
    }

    public static ObjectBinaryPrinter printerForId(byte id) {
        return printers[id];
    }

    private final ObjectBinaryPrinter instance;
    private final int order;

    ObjectBinaryPrinterType(ObjectBinaryPrinter instance, int order) {
        this.instance = instance;
        this.order = order;
    }

    public ObjectBinaryPrinter getInstance() {
        return instance;
    }

    public int getOrder() {
        return order;
    }
}
