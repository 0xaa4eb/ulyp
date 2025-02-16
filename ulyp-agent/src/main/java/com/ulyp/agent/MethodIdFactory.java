package com.ulyp.agent;

import com.ulyp.agent.advice.ConstructorAdvice;
import com.ulyp.agent.advice.MethodAdvice;
import com.ulyp.core.Converter;
import com.ulyp.core.Method;
import com.ulyp.core.MethodRepository;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.annotation.AnnotationDescription;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.implementation.bytecode.assign.Assigner;

import javax.annotation.concurrent.ThreadSafe;


/**
 * Allows wiring method id into advice classes {@link ConstructorAdvice} and {@link MethodAdvice}
 * <p>
 * Uses a singleton instance of {@link MethodRepository} to store methods into it.
 */
@ThreadSafe
public class MethodIdFactory implements Advice.OffsetMapping.Factory<MethodId> {

    private final ForMethodIdOffsetMapping instance;

    public MethodIdFactory(MethodRepository methodRepository, Converter<MethodDescription, Method> methodResolver) {
        this.instance = new ForMethodIdOffsetMapping(methodResolver, methodRepository);
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

        private final MethodRepository methodRepository;
        private final ThreadLocal<IdMapping> lastMethod = new ThreadLocal<>();
        private final Converter<MethodDescription, Method> byteBuddyMethodResolver;

        ForMethodIdOffsetMapping(Converter<MethodDescription, Method> byteBuddyMethodResolver, MethodRepository methodRepository) {
            this.byteBuddyMethodResolver = byteBuddyMethodResolver;
            this.methodRepository = methodRepository;
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
                Method method = byteBuddyMethodResolver.convert(instrumentedMethod);
                id = methodRepository.putAndGetId(method);
                lastMethod.set(new IdMapping(instrumentedMethod, id));
            }

            return Target.ForStackManipulation.of(id);
        }
    }
}
