/*
 * Copyright (c) 2007 Mockito contributors
 * This program is made available under the terms of the MIT License.
 */
package org.mockito.internal.configuration;

import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.configuration.AnnotationEngine;
import org.mockito.exceptions.Reporter;
import org.mockito.exceptions.base.MockitoException;
import org.mockito.internal.util.MockUtil;
import org.mockito.internal.util.reflection.FieldInitializer;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import static org.mockito.Mockito.withSettings;

@SuppressWarnings({"unchecked"})
public class SpyAnnotationEngine implements AnnotationEngine {

    public Object createMockFor(Annotation annotation, Field field) {
        return null;
    }
    
    @SuppressWarnings("deprecation")
    public void process(Class<?> context, Object testClass) {
        Field[] fields = context.getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Spy.class)) {
                assertNoAnnotations(Spy.class, field, Mock.class, org.mockito.MockitoAnnotations.Mock.class, Captor.class);
                boolean wasAccessible = field.isAccessible();
                field.setAccessible(true);
                try {
                    Object instance = new FieldInitializer(testClass, field).initialize();
                    //TODO: delete below:
                    if (instance == null) {
                        throw new MockitoException("Cannot create a @Spy for '" + field.getName() + "' field because the *instance* is missing\n" +
                        		  "The instance must be created *before* initMocks();\n" +
                                  "Example of correct usage of @Spy:\n" +
                            	  "   @Spy List mock = new LinkedList();\n" +
                            	  "   //also, don't forget about MockitoAnnotations.initMocks();");

                    }
                    if (new MockUtil().isMock(instance)) { 
                        // instance has been spied earlier
                        Mockito.reset(instance);
                    } else {
                        field.set(testClass, Mockito.mock(instance.getClass(), withSettings()
                                .spiedInstance(instance)
                                .defaultAnswer(Mockito.CALLS_REAL_METHODS)
                                .name(field.getName())));
                    }
                } catch (IllegalAccessException e) {
                    throw new MockitoException("Problems initiating spied field " + field.getName(), e);
                } finally {
                    field.setAccessible(wasAccessible);
                }
            }
        }
    }
    
    //TODO duplicated elsewhere
    void assertNoAnnotations(Class annotation, Field field, Class ... undesiredAnnotations) {
        for (Class u : undesiredAnnotations) {
            if (field.isAnnotationPresent(u)) {
                new Reporter().unsupportedCombinationOfAnnotations(annotation.getSimpleName(), annotation.getClass().getSimpleName());
            }
        }        
    }    
}
