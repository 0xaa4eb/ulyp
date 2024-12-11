package com.ulyp.agent;

import com.ulyp.agent.util.ConstructedTypesStack;
import com.ulyp.core.TypeResolver;
import com.ulyp.core.recorders.QueuedIdentityObject;

/**
 * An instance of {@link ConstructedTypesStack} tracks types which instances are currently being constructed.
 * For such types, if a constructor is in-progress for any instance, then we can't record instances of this type until constructor completes.
 * Because a recorder may see partially recorded object state which is something we want to avoid.
 * This comes with another problem, since we can't get an object instance if constructor is called,
 * so we only track types instead of concrete instances.
 */
public class ConstructorTrackingObjectConverter implements ObjectRecordingConverter {

    private final TypeResolver typeResolver;
    private final ObjectRecordingConverter strategy;

    public ConstructorTrackingObjectConverter(TypeResolver typeResolver, ObjectRecordingConverter strategy) {
        this.typeResolver = typeResolver;
        this.strategy = strategy;
    }

    @Override
    public Object prepare(Object obj, ConstructedTypesStack constructedObjects) {
        if (obj != null && !constructedObjects.isEmpty()) {
            int typeId = typeResolver.get(obj).getId();
            if (constructedObjects.contains(typeId)) {
                return new QueuedIdentityObject(typeId, System.identityHashCode(obj));
            }
        }

        return strategy.prepare(obj, constructedObjects);
    }

    @Override
    public Object[] prepare(Object[] objs, ConstructedTypesStack constructedObjects) {
        if (!constructedObjects.isEmpty()) {
            for (int i = 0; i < objs.length; i++) {
                Object obj = objs[i];
                if (obj != null) {
                    int typeId = typeResolver.get(obj).getId();
                    if (constructedObjects.contains(typeId)) {
                        objs[i] = new QueuedIdentityObject(typeId, System.identityHashCode(obj));
                    }
                }
            }
        }

        return strategy.prepare(objs, constructedObjects);
    }
}
