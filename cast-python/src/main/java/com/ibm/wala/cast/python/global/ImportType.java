package com.ibm.wala.cast.python.global;

public enum ImportType {
    NORMAL("import"), INIT("importInit"), BUILTIN("importBuiltIn");
    private final String type;

    @Override
    public String toString() {
        return "ImportType{type='" + type + '}';
    }

    private ImportType(String type){
        this.type=type;
    }
}
