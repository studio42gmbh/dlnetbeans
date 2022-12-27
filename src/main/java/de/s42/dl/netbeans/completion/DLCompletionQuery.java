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
package de.s42.dl.netbeans.completion;

import de.s42.dl.netbeans.DLDataObject;
import de.s42.dl.netbeans.completion.items.KeywordDLCompletionItem;
import de.s42.dl.netbeans.completion.items.TypeDLCompletionItem;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.support.AsyncCompletionQuery;

/**
 *
 * @author Benjamin Schiller
 */
public class DLCompletionQuery extends AsyncCompletionQuery
{

	private final static Logger log = LogManager.getLogger(DLCompletionQuery.class.getName());
	
	@Override
	protected void query(CompletionResultSet result, Document document, int caretOffset)
	{
		assert result != null;
		assert document != null;
		
		BaseDocument baseDoc = (BaseDocument) document;

		baseDoc.readLock();

		String currentWord = getWordBefore(baseDoc, caretOffset);

		baseDoc.readUnlock();

		KeywordDLCompletionItem.addKeywordItems(result, document, currentWord, caretOffset);
		
		TypeDLCompletionItem.addTypeItems(result, document, currentWord, caretOffset);

		result.finish();
	}

	/**
	 * Retrieves the word before the given caret position
	 *
	 * @param document
	 * @param caretOffset
	 * @return the word or an empty string
	 */
	protected String getWordBefore(BaseDocument document, int caretOffset)
	{
		assert document != null;
		
		try {

			StringBuilder result = new StringBuilder();

			for (int i = caretOffset - 1; i > 0; i--) {
				char c = document.getChars(i, 1)[0];
				if (!document.isIdentifierPart(c)) {
					break;
				}
				result.append(c);
			}

			return result.reverse().toString();
		} catch (BadLocationException ex) {
			return "";
		}
	}
}
