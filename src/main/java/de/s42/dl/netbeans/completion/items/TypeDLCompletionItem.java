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
import de.s42.dl.netbeans.semantic.cache.DLSemanticCache;
import de.s42.dl.netbeans.semantic.cache.DLSemanticCacheNode;
import de.s42.dl.netbeans.semantic.model.Type;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.net.URL;
import java.nio.file.Path;
import java.util.Optional;
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

	protected static ImageIcon TYPE_ICON
		= new ImageIcon(ImageUtilities.loadImage("de/s42/dl/netbeans/navigator/type.png"));

	final static DLSemanticCache CACHE = MimeLookup.getLookup(DL_MIME_TYPE).lookup(DLSemanticCache.class);

	protected final Type type;

	public TypeDLCompletionItem(Type type, Document document, int insertionOffset, int caretOffset)
	{
		super(document, insertionOffset, caretOffset, false);

		assert type != null;

		this.type = type;
	}

	public static void addTypeItems(CompletionResultSet result, Document document, String currentWord, int caretOffset)
	{
		String cacheKey = DLSemanticCache.getCacheKey(document);
		
		Optional<DLSemanticCacheNode> optCacheNode = CACHE.getCacheNode(cacheKey);
		
		if (optCacheNode.isEmpty()) {
			return;
		}
		
		DLSemanticCacheNode cacheNode = optCacheNode.orElseThrow();

		for (Type type : cacheNode.findTypes(currentWord, caretOffset, true)) {

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
	protected Path getGotoFile()
	{
		return Path.of(type.getModuleId());
	}

	@Override
	protected int getGotoLine()
	{
		return type.getStartLine()- 1;
	}

	@Override
	protected URL getDocumentationUrl()
	{
		return DocumentationHtmlFactory.createDocumentationUrl(type);
	}

	@Override
	protected String getDocumentationHtmlText()
	{
		return DocumentationHtmlFactory.createDocumentationHtml(type);
	}

	@Override
	protected String getRightHtmlText()
	{
		return DocumentationHtmlFactory.createRightTextHtml(type);
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
