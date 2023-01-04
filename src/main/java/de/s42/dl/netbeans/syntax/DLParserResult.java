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
package de.s42.dl.netbeans.syntax;

import de.s42.dl.DLModule;
import de.s42.dl.netbeans.syntax.hints.AbstractDLParsingHint;
import de.s42.dl.netbeans.syntax.hints.DLParsingError;
import de.s42.dl.netbeans.syntax.hints.DLParsingWarning;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.csl.spi.ParserResult;
import org.netbeans.modules.parsing.api.Snapshot;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Benjamin Schiller
 */
public class DLParserResult extends ParserResult
{

	protected final List<AbstractDLParsingHint> hints = new ArrayList<>();
	protected DLModule module;
	protected int warnings;
	protected int errors;

	public DLParserResult(Snapshot snapshot)
	{
		super(snapshot);
	}

	public boolean addWarning(String message, ParserRuleContext context)
	{
		return addWarning(message, context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1, context.getStart().getLine(), context.getStart().getCharPositionInLine() + 1);
	}

	public boolean addWarning(String message, Token token)
	{
		return addWarning(message, token.getStartIndex(), token.getStopIndex() + 1, token.getLine(), token.getCharPositionInLine() + 1);
	}

	public boolean addWarning(String message, int startPosition, int endPosition, int line, int positionInLine)
	{
		FileObject fileObject = getSnapshot().getSource().getFileObject();

		return addWarning(new DLParsingWarning(
			fileObject,
			message,
			message,
			startPosition,
			endPosition,
			line,
			positionInLine
		));
	}

	public boolean addWarning(DLParsingWarning warning)
	{
		assert warning != null;
		
		warnings+=1;

		return hints.add(warning);
	}

	public boolean addError(String message, ParserRuleContext context)
	{
		return addError(message, context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1, context.getStart().getLine(), context.getStart().getCharPositionInLine() + 1);
	}

	public boolean addError(String message, Token token)
	{
		return addError(message, token.getStartIndex(), token.getStopIndex() + 1, token.getLine(), token.getCharPositionInLine() + 1);
	}

	public boolean addError(String message, int startPosition, int endPosition, int line, int positionInLine)
	{
		FileObject fileObject = getSnapshot().getSource().getFileObject();

		return addError(new DLParsingError(
			fileObject,
			message,
			message,
			startPosition,
			endPosition,
			line,
			positionInLine			
		));
	}

	public boolean addError(DLParsingError error)
	{
		assert error != null;

		errors+=1;
		
		return hints.add(error);
	}

	@Override
	protected void invalidate()
	{
		// Do nothing here -> 
		// ATTENTION: might get called and afterwards it will be used again to render hints 
		// ... seems to be an issue with NB16 atm
	}

	@Override
	public List<AbstractDLParsingHint> getDiagnostics()
	{
		return Collections.unmodifiableList(hints);
	}

	public DLModule getModule()
	{
		return module;
	}

	public void setModule(DLModule module)
	{
		this.module = module;
	}
	
	public int getHintsCount()
	{
		return warnings + errors;
	}
	
	public int getErrorCount()
	{
		return errors;
	}

	public int getWarningCount()
	{
		return warnings;
	}

	public boolean hasWarnings()
	{
		return warnings > 0;
	}

	public boolean hasErrors()
	{
		return errors > 0;
	}
}
