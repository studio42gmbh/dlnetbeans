// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2022 Studio 42 GmbH ( https://www.s42m.de ).
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
//</editor-fold>
package de.s42.dl.netbeans.semantic.model;

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Benjamin Schiller
 */
public class Type extends Entry
{

	private final static Logger log = LogManager.getLogger(Type.class.getName());

	protected final List<Type> contained = new ArrayList<>();
	protected final List<Type> extended = new ArrayList<>();
	protected final Type aliasOf;

	public Type()
	{
		aliasOf = null;
	}

	public Type(String typeName, ParserRuleContext context, String moduleId, int originalLine, int originalPosition)
	{
		this(typeName, context, moduleId, originalLine, originalPosition, null);
	}

	public Type(String typeName, ParserRuleContext context, String moduleId, int originalLine, int originalPosition, Type aliasOf)
	{
		assert typeName != null;
		assert context != null;
		assert moduleId != null;

		this.aliasOf = aliasOf;
		this.moduleId = moduleId;
		identifier = typeName;
		this.originalLine = originalLine;
		this.originalPosition = originalPosition;
		startLine = context.start.getLine();
		startPosition = context.start.getCharPositionInLine() + 1;
		startOffset = context.start.getStartIndex();
		endLine = context.start.getLine();
		endOffset = context.start.getStopIndex();
		endPosition = context.start.getCharPositionInLine() + 1 + endOffset - startOffset;
	}

	public void addContained(Type type)
	{
		assert type != null;

		contained.add(type);
	}

	public void addExtended(Type type)
	{
		assert type != null;

		extended.add(type);
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public List<Type> getExtended()
	{
		return Collections.unmodifiableList(extended);
	}

	public List<Type> getContained()
	{
		return Collections.unmodifiableList(contained);
	}

	public Type getAliasOf()
	{
		return aliasOf;
	}

	public String getPath()
	{
		int dotIndex = identifier.lastIndexOf('.');
		if (dotIndex > -1) {
			return identifier.substring(0, dotIndex);
		} else {
			return "";
		}
	}

	public String getSimpleName()
	{
		int dotIndex = identifier.lastIndexOf('.');
		if (dotIndex > -1) {
			return identifier.substring(dotIndex + 1);
		} else {
			return identifier;
		}
	}
	//</editor-fold>
}
