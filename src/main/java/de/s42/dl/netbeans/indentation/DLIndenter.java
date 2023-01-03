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

import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.dl.parser.DLLexer;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import javax.swing.text.BadLocationException;
import org.antlr.v4.runtime.BufferedTokenStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
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

	public void completeReindent() throws BadLocationException
	{
		// Get the text safely
		BaseDocument document = getDocument();

		String text = FileObjectHelper.getText(document);
		int indentLevel = IndentUtils.indentLevelSize(document);

		// Prepare the lexer
		DLLexer lexer = new DLLexer(CharStreams.fromString(text));
		lexer.removeErrorListeners();
		TokenStream tokens = new BufferedTokenStream(lexer);

		// Iterate tokens from lexer
		StringBuilder builder = new StringBuilder(text.length() * 3 / 2);
		boolean justNewline = false;
		int indent = 0;
		while (true) {
			Token token = tokens.LT(1);
			Token nextToken = tokens.LT(2);

			//log.info("TOKEN : " + token);
			// End of file
			if (token.getType() == DLLexer.EOF) {
				break;
			}

			// Reduce indentation on }
			if (nextToken.getType() == DLLexer.SCOPE_CLOSE) {
				indent = Math.max(0, indent - 1);
			}
			
			// If there was a new line before and it is not immediately followed by another newline -> add indent
			if (justNewline && nextToken.getType() != DLLexer.NEWLINE) {
				builder.append(IndentUtils.createIndentString(document, indent * indentLevel));
			}

			// Increase indentation on {
			if (token.getType() == DLLexer.SCOPE_OPEN) {
				indent += 1;
			}

			// If it is not newline or a whitepace -> Just add the content
			if (!justNewline || token.getType() != DLLexer.WHITESPACES) {
				builder.append(token.getText());
			}

			// Newline sets justNewline for next iteration
			justNewline = (token.getType() == DLLexer.NEWLINE);

			// Proceed one token
			tokens.consume();
		}

		// Replace the whole text safely
		FileObjectHelper.replaceText(document, builder.toString());
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public final BaseDocument getDocument()
	{
		return (BaseDocument) context.document();
	}
	//</editor-fold>
}
