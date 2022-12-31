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

import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.completion.DLCompletionItem;
import de.s42.dl.netbeans.semantic.DLSemanticCache;
import de.s42.dl.netbeans.semantic.model.Type;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.Color;
import java.net.MalformedURLException;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.openide.util.ImageUtilities;

/**
 *
 * @author Benjamin Schiller
 */
public class TypeDLCompletionItem extends DLCompletionItem
{

	private final static Logger log = LogManager.getLogger(TypeDLCompletionItem.class.getName());

	protected static String TYPE_RIGHT_HTML_TEXT = "<i>Type</i>";
	protected static Color TYPE_TEXT_COLOR = Color.decode("0x000055");
	protected static ImageIcon TYPE_ICON
		= new ImageIcon(ImageUtilities.loadImage("de/s42/dl/netbeans/navigator/type.png"));

	final static DLSemanticCache CACHE = MimeLookup.getLookup(DL_MIME_TYPE).lookup(DLSemanticCache.class);

	protected Type type;

	public TypeDLCompletionItem(Type type, Document document, int insertionOffset, int caretOffset)
	{
		super(document, insertionOffset, caretOffset, true);

		assert type != null;

		this.type = type;
	}

	public static void addTypeItems(CompletionResultSet result, Document document, String currentWord, int caretOffset)
	{
		String cacheKey = DLSemanticCache.getCacheKey(document);

		for (Type type : CACHE.findTypes(cacheKey, currentWord, caretOffset)) {

			CompletionItem item = new TypeDLCompletionItem(
				type,
				document,
				caretOffset - currentWord.length(),
				caretOffset
			);

			result.addItem(item);
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
		return "<h2>Type " + TypeDLCompletionItem.this.getText() + "</h2><p>This type ...</p><br><br><a href='https://github.com/studio42gmbh/dl'>Read more</a>";
	}

	@Override
	protected String getRightHtmlText()
	{
		StringBuilder builder = new StringBuilder();

		builder
			.append("<i>Type (")
			.append(type.getStartLine())
			.append(":")
			.append(type.getStartPosition())
			.append(")</i>");

		// Append alias info
		if (type.getAliasOf() != null) {
			builder
				.append(" alias of ")
				.append(type.getAliasOf().getIdentifier());
		}

		return builder.toString();
	}

	@Override
	protected Color getTextColor(boolean selected)
	{
		return null;//TYPE_TEXT_COLOR;
	}

	@Override
	protected ImageIcon getIcon()
	{
		return TYPE_ICON;
	}

	@Override
	public int getSortPriority()
	{
		return 90;
	}

	@Override
	public String getText()
	{
		return type.getIdentifier();
	}

	@Override
	public void setText(String text)
	{
		throw new UnsupportedOperationException("Can not set text for this item");
	}
	//</editor-fold>

}
