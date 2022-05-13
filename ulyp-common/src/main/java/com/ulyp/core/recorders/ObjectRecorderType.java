package com.ulyp.core.recorders;

public enum ObjectRecorderType {
    CLASS_OBJECT_RECORDER(new ClassObjectRecorder((byte) 1), 20),
    STRING_RECORDER(new StringRecorder((byte) 2), 0),
    THROWABLE_RECORDER(new ThrowableRecorder((byte) 5), 20),
    ENUM_RECORDER(new EnumRecorder((byte) 6), 5),
    DYNAMIC_OBJECT_RECORDER(new DynamicObjectRecorder((byte) 7), 100),
    INTEGRAL_RECORDER(new IntegralRecorder((byte) 12), 0),
    BOOLEAN_RECORDER(new BooleanRecorder((byte) 100), 1),
    ANY_NUMBER_RECORDER(new NumbersRecorder((byte) 8), 1),
    OBJECT_ARRAY_RECORDER(new ObjectArrayRecorder((byte) 11), 1),
    COLLECTION_RECORDER(new CollectionRecorder((byte) 10), 1),
    MAP_RECORDER(new MapRecorder((byte) 13), 1),
    OPTIONAL_RECORDER(new OptionalRecorder((byte) 25), 90),
    FILE_RECORDER(new FileRecorder((byte) 26), 90),
    DATE_RECORDER(new DateRecorder((byte) 20), 90),
    TO_STRING_RECORDER(new ToStringRecorder((byte) 91), 99),
    IDENTITY_RECORDER(new IdentityRecorder((byte) 0), Integer.MAX_VALUE / 2),
    NULL_RECORDER(new NullObjectRecorder((byte) 9), Integer.MAX_VALUE);

    public static final ObjectRecorder[] recorderInstances = new ObjectRecorder[256];

    static {
        for (ObjectRecorderType type : values()) {
            if (recorderInstances[type.getInstance().getId()] != null) {
                throw new RuntimeException("Duplicate id");
            }
            recorderInstances[type.getInstance().getId()] = type.getInstance();
        }
    }

    public static ObjectRecorder recorderForId(byte id) {
        return recorderInstances[id];
    }

    private final ObjectRecorder instance;
    private final int order;

    ObjectRecorderType(ObjectRecorder instance, int order) {
        this.instance = instance;
        this.order = order;
    }

    public ObjectRecorder getInstance() {
        return instance;
    }

    public int getOrder() {
        return order;
    }
}
