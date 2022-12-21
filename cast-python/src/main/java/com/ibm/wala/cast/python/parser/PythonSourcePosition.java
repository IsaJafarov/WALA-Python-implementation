package com.ibm.wala.cast.python.parser;

import com.ibm.wala.cast.tree.impl.AbstractSourcePosition;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;

public abstract class PythonSourcePosition extends AbstractSourcePosition {

    public abstract String getString();
}
