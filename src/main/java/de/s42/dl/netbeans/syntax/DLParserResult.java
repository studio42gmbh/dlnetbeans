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

import de.s42.dl.netbeans.syntax.hints.AbstractDLParsingHint;
import de.s42.dl.netbeans.syntax.hints.DLParsingError;
import de.s42.dl.netbeans.syntax.hints.DLParsingWarning;
import de.s42.log.LogManager;
import de.s42.log.Logger;
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

	private final static Logger log = LogManager.getLogger(DLParserResult.class.getName());

	protected final List<AbstractDLParsingHint> hints = new ArrayList<>();

	public DLParserResult(Snapshot snapshot)
	{
		super(snapshot);
	}

	public boolean addWarning(String message, ParserRuleContext context)
	{
		return addWarning(message, context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1);
	}
	
	public boolean addWarning(String message, Token token)
	{
		return addWarning(message, token.getStartIndex(), token.getStopIndex() + 1);
	}

	public boolean addWarning(String message, int startPosition, int endPosition)
	{
		FileObject fileObject = getSnapshot().getSource().getFileObject();

		return addWarning(new DLParsingWarning(
			fileObject,
			message,
			"Warning " + message,
			startPosition,
			endPosition
		));
	}

	public boolean addWarning(DLParsingWarning warning)
	{
		assert warning != null;

		return hints.add(warning);
	}

	public boolean addError(String message, ParserRuleContext context)
	{
		return addError(message, context.getStart().getStartIndex(), context.getStop().getStopIndex() + 1);
	}

	public boolean addError(String message, Token token)
	{
		return addError(message, token.getStartIndex(), token.getStopIndex() + 1);
	}

	public boolean addError(String message, int startPosition, int endPosition)
	{
		FileObject fileObject = getSnapshot().getSource().getFileObject();

		return addError(new DLParsingError(
			fileObject,
			message,
			"Error " + message,
			startPosition,
			endPosition
		));
	}

	public boolean addError(DLParsingError error)
	{
		assert error != null;

		return hints.add(error);
	}

	@Override
	protected void invalidate()
	{
		// Do nothing here -> might get called and afterwards it will be used again to render hints ... seems to be an issue with NB16 atm
	}

	@Override
	public List<AbstractDLParsingHint> getDiagnostics()
	{
		return Collections.unmodifiableList(hints);
	}
}
