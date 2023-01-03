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
import org.netbeans.modules.editor.indent.spi.Context.Region;

/**
 *
 * @author Benjamin Schiller
 */
public class DLIndenter
{

	public final static int MAX_LINE_LOOKAHEAD = 1000;

	private final static Logger log = LogManager.getLogger(DLIndenter.class.getName());

	protected final Context context;

	public DLIndenter(Context context)
	{
		assert context != null;
		
		this.context = context;
	}

	protected boolean needsBeginLineReindent(String token, int indent)
	{
		return true;
	}

	public void reindent() throws BadLocationException
	{
		for (Region region : context.indentRegions()) {
			reindentRegion(region.getStartOffset(), region.getEndOffset());
		}
	}

	public void reindentRegion(int startIndex, int endIndex) throws BadLocationException
	{
		assert startIndex >= 0;
		assert endIndex >= 0;
		assert endIndex >= startIndex;

		//log.debug("reindent", startIndex, endIndex);
		BaseDocument document = getDocument();
		String text = FileObjectHelper.getText(document);
		int indentLevel = IndentUtils.indentLevelSize(document);
		TokenStream tokens = getDLTokenStream(text);

		// Iterate tokens from lexer
		boolean justNewline = true;
		int indent = 0;
		int offset = 0;
		int adjustedEndIndex = endIndex;
		while (true) {
			Token token = tokens.LT(1);

			// End of file or behind of indent region
			if (token.getType() == DLLexer.EOF
				|| offset > adjustedEndIndex) {
				break;
			}

			// Just indent in given region
			if (justNewline && offset >= startIndex) {

				int adjustedIndent = Math.max(0, indent - countDirectClosingScopesInThisLine(tokens));

				int lineOff = context.lineStartOffset(offset);
				int newLineIndent = adjustedIndent * indentLevel;
				//int lineIndent = context.lineIndent(lineOff);
				//log.warn("justNewline", lineIndent, newLineIndent);
				context.modifyIndent(lineOff, newLineIndent);

				// Adjust for added whitespaces
				int oldLineIndenInChars = IndentUtils.createIndentString(document, newLineIndent).length();
				offset += oldLineIndenInChars;
				adjustedEndIndex += oldLineIndenInChars;

				// Adjust for removed whitespaces
				if (token.getType() == DLLexer.WHITESPACES) {
					offset -= token.getText().length();
					adjustedEndIndex -= token.getText().length();
				}
			}

			// Increase indentation on {
			if (token.getType() == DLLexer.SCOPE_OPEN) {
				indent += 1;
			} // Reduce indentation on }
			else if (token.getType() == DLLexer.SCOPE_CLOSE) {
				indent = Math.max(0, indent - 1);
			}

			// Proceed one token
			offset += token.getText().length();
			justNewline = (token.getType() == DLLexer.NEWLINE);
			tokens.consume();
		}
	}

	/**
	 * Constructs a new TokenStream with an underlying DLLexer from a given text
	 * @param content
	 * @return 
	 */
	protected TokenStream getDLTokenStream(String content)
	{
		assert content != null;

		DLLexer lexer = new DLLexer(CharStreams.fromString(content));
		lexer.removeErrorListeners();

		return new BufferedTokenStream(lexer);
	}

	/**
	 * Return the number of direct closing scopes from the beginning of a line. anything else but whitespaces ends the count
	 * @param tokens
	 * @return 
	 */
	protected int countDirectClosingScopesInThisLine(TokenStream tokens)
	{
		assert tokens != null;

		int count = 0;

		for (int i = 1; i < MAX_LINE_LOOKAHEAD; ++i) {

			Token token = tokens.LT(i);

			switch (token.getType()) {
				case DLLexer.WHITESPACES:
					break;
				case DLLexer.SCOPE_CLOSE:
					count += 1;
					break;
				default:
					return count;
			}
		}

		return count;
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public final BaseDocument getDocument()
	{
		return (BaseDocument) context.document();
	}
	//</editor-fold>
}
