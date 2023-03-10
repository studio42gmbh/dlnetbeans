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
package de.s42.dl.netbeans.completion.items;

import de.s42.dl.language.DLKeyword;
import de.s42.dl.netbeans.completion.DLCompletionItem;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Benjamin Schiller
 */
public class KeywordDLCompletionItem extends DLCompletionItem
{

	protected static ImageIcon KEYWORD_ICON
		= new ImageIcon(ImageUtilities.loadImage("de/s42/dl/netbeans/dl-icon-keyword.png"));

	protected final DLKeyword keyword;

	public KeywordDLCompletionItem(DLKeyword keyword, Document document, int insertionOffset, int caretOffset)
	{
		super(document, insertionOffset, caretOffset, true);

		assert keyword != null;

		this.keyword = keyword;
	}

	public static void addKeywordItems(CompletionResultSet result, Document document, String currentWord, int caretOffset)
	{
		for (DLKeyword keyword : DLKeyword.values()) {

			if (currentWord.isBlank() || keyword.keyword.startsWith(currentWord.toLowerCase())) {

				CompletionItem item = new KeywordDLCompletionItem(
					keyword,
					document,
					caretOffset - currentWord.length(),
					caretOffset
				);

				result.addItem(item);
			}
		}
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	@Override
	protected URL getDocumentationUrl() throws MalformedURLException
	{
		return DocumentationHtmlFactory.createDocumentationUrl(keyword);
	}

	@Override
	protected String getDocumentationHtmlText()
	{
		return DocumentationHtmlFactory.createDocumentationHtml(keyword);
	}

	@Override
	protected String getRightHtmlText()
	{
		return DocumentationHtmlFactory.createRightTextHtml(keyword);
	}

	@Override
	protected ImageIcon getIcon()
	{
		return KEYWORD_ICON;
	}

	@Override
	public int getSortPriority()
	{
		return 100;
	}

	@Override
	public String getText()
	{
		return keyword.keyword;
	}

	@Override
	public void setText(String text)
	{
		throw new UnsupportedOperationException("Can not set text for this item");
	}
	//</editor-fold>
}
