/******************************************************************************
 * Copyright (c) 2018 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *****************************************************************************/
package com.ibm.wala.cast.python3.parser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;

import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CharStream;

import com.ibm.wala.cast.tree.CAstEntity;
import com.ibm.wala.cast.tree.impl.CAstTypeDictionaryImpl;
import com.ibm.wala.cast.util.CAstPrinter;
import com.ibm.wala.classLoader.ModuleEntry;
import com.ibm.wala.classLoader.SourceModule;
import com.ibm.wala.classLoader.SourceURLModule;

public class PythonModuleParser extends PythonParser<ModuleEntry> {

	private final SourceModule moduleName;
	
	protected URL getParsedURL() throws IOException {
		return moduleName.getURL();
	}

	protected WalaPythonParser makeParser() throws IOException {
		CharStream file = new ANTLRInputStream(moduleName.getInputStream());
		return new WalaPythonParser(file, moduleName.getName(), "UTF-8");
	}

	public PythonModuleParser(SourceModule moduleName, CAstTypeDictionaryImpl<String> types) {
		super(types);
		this.moduleName = moduleName;
	}

	@Override
	protected String scriptName() {
		return moduleName.getName();
	}

	public static void main(String[] args) throws Exception {
		URL url = new URL(args[0]);
		PythonParser<ModuleEntry> p = new PythonModuleParser(new SourceURLModule(url), new CAstTypeDictionaryImpl<String>());
		CAstEntity script = p.translateToCAst();
		System.err.println(script);
		System.err.println(CAstPrinter.print(script));
	}

	@Override
	protected Reader getReader() throws IOException {
		return new InputStreamReader(moduleName.getInputStream());
	}

}
