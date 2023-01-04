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
package de.s42.dl.netbeans.syntax.hints;

import org.netbeans.modules.csl.api.Error.Badging;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Benjamin Schiller
 */
public abstract class AbstractDLParsingHint implements Badging
{

	protected final FileObject fileObject;
	protected final String display;
	protected final String description;
	protected final int startPosition;
	protected final int endPosition;
	protected final Object[] parameters;

	public AbstractDLParsingHint(FileObject fileObject, String display, String description, int startPosition, int endPosition)
	{
		this(fileObject, display, description, startPosition, endPosition, null);
	}

	public AbstractDLParsingHint(FileObject fileObject, String display, String description, int startPosition, int endPosition, Object[] parameters)
	{
		assert fileObject != null;

		this.fileObject = fileObject;
		this.display = display;
		this.description = description;
		this.startPosition = startPosition;
		this.endPosition = endPosition;
		this.parameters = parameters;
	}

	public Severity getHintSeverity()
	{
		org.netbeans.modules.csl.api.Severity severity = getSeverity();
		if (severity == org.netbeans.modules.csl.api.Severity.ERROR) {
			return Severity.ERROR;
		} else if (severity == org.netbeans.modules.csl.api.Severity.WARNING) {
			return Severity.WARNING;
		} else if (severity == org.netbeans.modules.csl.api.Severity.INFO) {
			return Severity.HINT;
		} else {
			return Severity.VERIFIER;
		}
	}

	@Override
	public String getDisplayName()
	{
		return display;
	}

	@Override
	public String getDescription()
	{
		return description;
	}

	@Override
	public String getKey()
	{
		return "AbstractDLParsingHint#" + getStartPosition() + ":" + getEndPosition();
	}

	@Override
	public FileObject getFile()
	{
		return fileObject;
	}

	@Override
	public int getStartPosition()
	{
		return startPosition;
	}

	@Override
	public int getEndPosition()
	{
		return endPosition;
	}

	@Override
	public boolean isLineError()
	{
		return true;
	}

	@Override
	public Object[] getParameters()
	{
		return parameters;
	}

	@Override
	public boolean showExplorerBadge()
	{
		return false;
	}
}
