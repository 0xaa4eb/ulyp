package com.ulyp.agent;

import com.ulyp.agent.util.ByteBuddyMethodResolver;
import com.ulyp.core.MethodDescriptionMap;
import com.ulyp.core.Method;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

public class MethodFactory implements Advice.OffsetMapping.Factory<MethodId> {

    static final MethodDescriptionMap methodDescriptionMap = MethodDescriptionMap.getInstance();

    private final ForMethodIdOffsetMapping instance;

    public MethodFactory(RecordMethodList recordMethodList) {
        this.instance = new ForMethodIdOffsetMapping(recordMethodList);
    }

    @Override
    public Class<MethodId> getAnnotationType() {
        return MethodId.class;
    }

    @Override
    public Advice.OffsetMapping make(ParameterDescription.InDefinedShape target, AnnotationDescription.Loadable<MethodId> annotation, AdviceType adviceType) {
        return instance;
    }

    private static class IdMapping {

        private final MethodDescription instrumentedMethod;
        private final int methodId;

        IdMapping(MethodDescription instrumentedMethod, int methodId) {
            this.instrumentedMethod = instrumentedMethod;
            this.methodId = methodId;
        }
    }

    static class ForMethodIdOffsetMapping implements Advice.OffsetMapping {

        private final ThreadLocal<IdMapping> lastMethod = new ThreadLocal<>();
        private final ByteBuddyMethodResolver byteBuddyMethodResolver = new ByteBuddyMethodResolver();
        private final RecordMethodList recordMethodList;

        ForMethodIdOffsetMapping(RecordMethodList recordMethodList) {
            this.recordMethodList = recordMethodList;
        }

        public Target resolve(TypeDescription instrumentedType,
                              MethodDescription instrumentedMethod,
                              Assigner assigner,
                              Advice.ArgumentHandler argumentHandler,
                              Sort sort) {
            /*
             * Bytebuddy calls this method for enter and exit advice methods. Which means mapping to id and building to method info
             * could be done only once for enter advice. So we store last mapped instrumented method and reuse id if possible.
             * This gives small, but noticeable ~5% overall performance boost.
             */
            IdMapping idMapping = lastMethod.get();
            int id;
            if (idMapping != null && idMapping.instrumentedMethod == instrumentedMethod) {
                lastMethod.set(null);
                id = idMapping.methodId;
            } else {
                Method method = byteBuddyMethodResolver.resolve(instrumentedMethod);
                id = methodDescriptionMap.putAndGetId(method, recordMethodList.shouldStartRecording(method));
                lastMethod.set(new IdMapping(instrumentedMethod, id));
            }

            return Target.ForStackManipulation.of(id);
        }
    }
}
