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
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.Color;
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

	private final static Logger log = LogManager.getLogger(KeywordDLCompletionItem.class.getName());

	protected static String KEYWORD_RIGHT_HTML_TEXT = "<i>Keyword</i>";
	protected static Color KEYWORD_TEXT_COLOR = Color.decode("0x0000B2");
	protected static ImageIcon KEYWORD_ICON
		= new ImageIcon(ImageUtilities.loadImage("de/s42/dl/netbeans/dl-icon-keyword.png"));

	public KeywordDLCompletionItem(String text, Document document, int insertionOffset, int caretOffset)
	{
		super(text, document, insertionOffset, caretOffset, true);
	}
	
	public static void addKeywordItems(CompletionResultSet result, Document document, String currentWord, int caretOffset)
	{
		for (String keyword : DLKeyword.getKeywords()) {

			if (currentWord.isBlank() || keyword.startsWith(currentWord)) {

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
		return new URL("https://github.com/studio42gmbh/dl/wiki/1.2.-Keywords");
	}

	@Override
	protected String getDocumentationHtmlText()
	{
		return "<h2>Keyword " + KeywordDLCompletionItem.this.getText() + "</h2><p>This keyword ...</p><br><br><a href='https://github.com/studio42gmbh/dl'>Read more</a>";
	}

	@Override
	protected String getRightHtmlText()
	{
		return KEYWORD_RIGHT_HTML_TEXT;
	}

	@Override
	protected Color getTextColor(boolean selected)
	{
		return KEYWORD_TEXT_COLOR;
	}

	@Override
	protected ImageIcon getIcon()
	{
		return KEYWORD_ICON;
	}

	@Override
	public int getSortPriority()
	{
		return 1;
	}
	//</editor-fold>
}
