/*
 * Copyright (c) 2010-2016. Axon Framework
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.axonframework.commandhandling.model;

import org.axonframework.common.ReflectionUtils;
import org.axonframework.common.annotation.*;
import org.axonframework.messaging.Message;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;

public abstract class AbstractMessageHandler<T> implements MessageHandler<T>, AnnotatedElement {

    private final Class<?> payloadType;
    private final int parameterCount;
    private final ParameterResolver<?>[] parameterResolvers;
    private final Executable executable;

    public AbstractMessageHandler(Executable executable, Class<?> explicitPayloadType,
                                  ParameterResolverFactory parameterResolverFactory) {
        this.executable = executable;
        ReflectionUtils.ensureAccessible(this.executable);
        Parameter[] parameters = executable.getParameters();
        this.parameterCount = executable.getParameterCount();
        parameterResolvers = new ParameterResolver[parameterCount];
        Class<?> supportedPayloadType = explicitPayloadType;
        for (int i = 0; i < parameterCount; i++) {
            parameterResolvers[i] = (parameterResolverFactory.createInstance(executable, parameters, i));
            if (supportedPayloadType.isAssignableFrom(parameterResolvers[i].supportedPayloadType())) {
                supportedPayloadType = parameterResolvers[i].supportedPayloadType();
            } else if (!parameterResolvers[i].supportedPayloadType().isAssignableFrom(supportedPayloadType)) {
                throw new UnsupportedHandlerException(String.format("The method %s seems to have parameters that put conflicting requirements on the payload type applicable on that method: %s vs %s", executable.toGenericString(), supportedPayloadType, parameterResolvers[i].supportedPayloadType()), executable);
            }
        }
        this.payloadType = supportedPayloadType;
    }

    @Override
    public Class<?> payloadType() {
        return payloadType;
    }

    @Override
    public int priority() {
        return parameterCount;
    }

    @Override
    public boolean canHandle(Message<?> message) {
        return typeMatches(message) && parametersMatch(message);
    }

    protected abstract boolean typeMatches(Message<?> message);

    protected boolean parametersMatch(Message<?> message) {
        for (ParameterResolver resolver : parameterResolvers) {
            if (!resolver.matches(message)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public Object handle(Message<?> message, T target) throws Exception {
        try {
            if (executable instanceof Method) {
                return ((Method) executable).invoke(target, resolveParameterValues(message));
            } else if (executable instanceof Constructor) {
                return ((Constructor) executable).newInstance(resolveParameterValues(message));
            } else {
                throw new IllegalStateException("What kind of handler is this?");
            }
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            if (e.getCause() instanceof Exception) {
                throw (Exception) e.getCause();
            } else if (e.getCause() instanceof Error) {
                throw (Error) e.getCause();
            }
            throw new MessageHandlerInvocationException(
                    String.format("Error handling an object of type [%s]", message.getPayloadType()), e);
        }
    }

    private Object[] resolveParameterValues(Message<?> message) {
        Object[] params = new Object[parameterCount];
        for (int i = 0; i < parameterCount; i++) {
            params[i] = parameterResolvers[i].resolveParameterValue(message);
        }
        return params;
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
        return executable.getAnnotation(annotationClass);
    }

    @Override
    public Annotation[] getAnnotations() {
        return executable.getAnnotations();
    }

    @Override
    public Annotation[] getDeclaredAnnotations() {
        return executable.getDeclaredAnnotations();
    }

    @Override
    public <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
        return executable.getDeclaredAnnotationsByType(annotationClass);
    }

    @Override
    public <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
        return executable.getDeclaredAnnotation(annotationClass);
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
        return executable.getAnnotationsByType(annotationClass);
    }

    @Override
    public boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return executable.isAnnotationPresent(annotationClass);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " " + executable.toGenericString();
    }
}
