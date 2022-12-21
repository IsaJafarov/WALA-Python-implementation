package com.ibm.wala.cast.python.parser;

import com.ibm.wala.cast.ir.translator.AbstractCodeEntity;
import com.ibm.wala.cast.python.loader.DynamicAnnotatableEntity;
import com.ibm.wala.cast.python.types.PythonTypes;
import com.ibm.wala.cast.tree.*;

import java.util.*;

public abstract class PythonCodeEntity extends AbstractCodeEntity implements DynamicAnnotatableEntity {

    private final Iterable<CAstNode> dynamicAnnotations;
    private final String functionName;
    private final String[] argumentNames;
    private final CAstNode[] defaultVars;
    private final HashSet<CAstAnnotation> annotationSet;

    protected PythonCodeEntity(CAstType type, Iterable<CAstNode> dynamicAnnotations, String functionName, String[] argumentNames, CAstNode[] defaultVars) {
        super(type);
        this.dynamicAnnotations = dynamicAnnotations;
        this.functionName = functionName;
        this.argumentNames = argumentNames;
        this.defaultVars = defaultVars;
        this.annotationSet = new HashSet<>();
        for (CAstNode node : dynamicAnnotations) {
            CAstAnnotation cAstAnnotation = new CAstAnnotation() {
                @Override
                public CAstType getType() {
                    return PythonTypes.cAstDynamicAnnotation;
                }

                @Override
                public Map<String, Object> getArguments() {
                    Map<String, Object> map = new HashMap<>();
                    map.put("dynamicAnnotation", node);
                    return map;
                }
            };
            annotationSet.add(cAstAnnotation);
        }
    }

    @Override
    public Iterable<CAstNode> dynamicAnnotations() {
        return dynamicAnnotations;
    }

    @Override
    public Collection<CAstAnnotation> getAnnotations() {
        return annotationSet;
    }

    @Override
    public int getKind() {
        return CAstEntity.FUNCTION_ENTITY;
    }


    @Override
    public String getName() {
        return functionName;
    }

    @Override
    public String[] getArgumentNames() {
        return argumentNames;
    }

    @Override
    public CAstNode[] getArgumentDefaults() {
        return defaultVars;
    }

    @Override
    public int getArgumentCount() {
        return argumentNames.length;

    }

    @Override
    public Collection<CAstQualifier> getQualifiers() {
        return Collections.emptySet();
    }

}
