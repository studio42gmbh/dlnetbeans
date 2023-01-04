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

import de.s42.dl.instances.DefaultDLModule;
import java.util.Objects;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Benjamin Schiller
 */
public abstract class Entry
{

	protected int startLine;
	protected int startPosition;
	protected int startOffset;
	protected int endLine;
	protected int endPosition;
	protected int endOffset;
	protected String identifier;
	protected String moduleId;

	protected Entry(String identifier, ParserRuleContext locationContext, String moduleId)
	{
		assert identifier != null;
		assert moduleId != null;

		this.moduleId = moduleId;
		this.identifier = identifier;

		if (locationContext != null) {
			startLine = locationContext.getStart().getLine();
			startPosition = locationContext.getStart().getCharPositionInLine() + 1;
			startOffset = locationContext.getStart().getStartIndex();
			endLine = locationContext.getStop().getLine();
			endOffset = locationContext.getStop().getStopIndex() + 1;
			endPosition = locationContext.getStop().getCharPositionInLine() + 1 + endOffset - startOffset;
		} else {
			startLine = 0;
			startPosition = 0;
			startOffset = 0;
			endLine = 0;
			endOffset = 0;
			endPosition = 0;
		}
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public String getIdentifier()
	{
		return identifier;
	}

	public void setIdentifier(String identifier)
	{
		this.identifier = identifier;
	}

	public int getStartLine()
	{
		return startLine;
	}

	public void setStartLine(int startLine)
	{
		this.startLine = startLine;
	}

	public int getStartPosition()
	{
		return startPosition;
	}

	public void setStartPosition(int startPosition)
	{
		this.startPosition = startPosition;
	}

	public int getStartOffset()
	{
		return startOffset;
	}

	public void setStartOffset(int startOffset)
	{
		this.startOffset = startOffset;
	}

	public int getEndLine()
	{
		return endLine;
	}

	public void setEndLine(int endLine)
	{
		this.endLine = endLine;
	}

	public int getEndPosition()
	{
		return endPosition;
	}

	public void setEndPosition(int endPosition)
	{
		this.endPosition = endPosition;
	}

	public int getEndOffset()
	{
		return endOffset;
	}

	public void setEndOffset(int endOffset)
	{
		this.endOffset = endOffset;
	}

	public String getModuleId()
	{
		return moduleId;
	}

	public void setModuleId(String moduleId)
	{
		this.moduleId = moduleId;
	}

	public String getShortModuleId()
	{
		return DefaultDLModule.createShortName(moduleId);
	}
	//</editor-fold>

	// <editor-fold desc="EqualsHashcode" defaultstate="collapsed">
	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 79 * hash + this.startOffset;
		hash = 79 * hash + this.endOffset;
		hash = 79 * hash + Objects.hashCode(this.identifier);
		hash = 79 * hash + Objects.hashCode(this.moduleId);
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Entry other = (Entry) obj;
		if (this.startOffset != other.startOffset) {
			return false;
		}
		if (this.endOffset != other.endOffset) {
			return false;
		}
		if (!Objects.equals(this.identifier, other.identifier)) {
			return false;
		}
		return Objects.equals(this.moduleId, other.moduleId);
	}
	//</editor-fold>	
}
