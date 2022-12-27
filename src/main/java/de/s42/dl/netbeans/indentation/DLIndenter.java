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
package de.s42.dl.netbeans.indentation;

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.StringTokenizer;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.indent.api.IndentUtils;
import org.netbeans.modules.editor.indent.spi.Context;

/**
 *
 * @author Benjamin Schiller
 */
public class DLIndenter
{

	private final static Logger log = LogManager.getLogger(DLIndenter.class.getName());

	protected final Context context;

	public DLIndenter(Context context)
	{
		this.context = context;
	}

	protected boolean needsBeginLineReindent(String token, int indent)
	{
		return true;
	}

	public void reindent() throws BadLocationException
	{
		BaseDocument document = getDocument();

		//log.warn("reindent", context.caretOffset(), context.startOffset(), context.endOffset(), document.getStartPosition().getOffset(), document.getEndPosition().getOffset());

		// Do a complete reindent
		if (context.startOffset() == document.getStartPosition().getOffset()
			&& context.endOffset() == document.getEndPosition().getOffset() - 1) {
			completeReindent();
		}
	}

	@SuppressWarnings("ConvertToStringSwitch")
	public void completeReindent() throws BadLocationException
	{
		//log.debug("completeReindent");
		
		int indentLevel = IndentUtils.indentLevelSize(context.document());

		BaseDocument document = getDocument();

		// Read current document content into a string
		document.readLock();
		String text = document.getText(document.getStartPosition().getOffset(), document.getEndPosition().getOffset());
		document.readUnlock();

		// Update the indentations
		document.extWriteLock();

		// Parse the document
		StringTokenizer tok = new StringTokenizer(text, "{}\n", true);
		int indent = 0;
		int position = document.getStartPosition().getOffset();
		boolean beginLine = true;
		while (tok.hasMoreTokens()) {

			String token = tok.nextToken();

			// Indent by -1 on '}'
			if (token.equals("}")) {
				indent = Math.max(0, indent - 1);

			}

			if (token.equals("\n")) {
				beginLine = true;
			} // Remove leading whitespaces and add indent tabs or expanded of it
			else if (beginLine) {

				if (needsBeginLineReindent(token, indent)) {

					// @todo Could be done more elegant without string changes 
					int len = token.length();

					token = token.stripLeading();

					int totalIndent = indent * indentLevel;

					// Cheap way of recognizing comment stars ...
					if (token.startsWith("*")) {
						totalIndent++;
					}

					// Indent tabs
					StringBuilder builder = new StringBuilder(len + 64);
					builder.append(IndentUtils.createIndentString(document, totalIndent));
					builder.append(token);
					token = builder.toString();

					// Replace token in document
					document.replace(position, len, token, null);
				}
				beginLine = false;
			}

			// Indent by +1 on '{'
			if (token.endsWith("{")) {
				indent++;
			} // At a newline -> do begin line operation next token

			// Keep track of the position
			position += token.length();
		}

		document.extWriteUnlock();
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public final BaseDocument getDocument()
	{
		return (BaseDocument) context.document();
	}
	//</editor-fold>
}
