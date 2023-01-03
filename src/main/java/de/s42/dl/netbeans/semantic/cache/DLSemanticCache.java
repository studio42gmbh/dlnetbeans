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
package de.s42.dl.netbeans.semantic.cache;

import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.semantic.model.ModuleEntry;
import de.s42.dl.netbeans.syntax.DLParserResult;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import javax.swing.text.Document;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Benjamin Schiller
 */
@MimeRegistration(mimeType = DL_MIME_TYPE,
	service = DLSemanticCache.class)
public class DLSemanticCache
{

	private final static Logger log = LogManager.getLogger(DLSemanticCache.class.getName());

	//protected final Map<String, Set<Type>> typesByKey = Collections.synchronizedMap(new HashMap<>());
	protected final Map<String, DLSemanticCacheNode> nodesByKey = Collections.synchronizedMap(new HashMap<>());

	// <editor-fold desc="public static String getCacheKey(.)" defaultstate="collapsed">
	public static String getCacheKey(String moduleId)
	{
		assert moduleId != null;

		// Check if the id refs a valid path -> If so return the absolute normalized version of it
		Path modulePath = Path.of(moduleId);		
		if (Files.isRegularFile(modulePath)) {
			return modulePath.toAbsolutePath().normalize().toString();
		}
		
		// If a module id does not represent a valid file -> return the given id as key
		return moduleId;
	}

	public static String getCacheKey(Document document)
	{
		assert document != null;

		Source source = Source.create(document);

		return getCacheKey(source.getFileObject());
	}

	public static String getCacheKey(DLParserResult result)
	{
		assert result != null;

		return getCacheKey(result.getSnapshot());
	}

	public static String getCacheKey(Snapshot snapshot)
	{
		assert snapshot != null;

		return getCacheKey(snapshot.getSource().getFileObject());
	}

	public static String getCacheKey(FileObject fileObject)
	{
		assert fileObject != null;

		return Path.of(fileObject.getPath()).toAbsolutePath().normalize().toString();
	}
	//</editor-fold>

	public DLSemanticCacheNode createCacheNode(String key, ModuleEntry module)
	{
		return new DLSemanticCacheModule(this, key, module);
	}

	public boolean hasCacheNode(String key)
	{
		assert key != null;

		return nodesByKey.containsKey(key);
	}

	public Optional<DLSemanticCacheNode> getCacheNode(String key)
	{
		assert key != null;

		return Optional.ofNullable(nodesByKey.get(key));
	}

	public DLSemanticCacheNode setCacheNode(DLSemanticCacheNode node)
	{
		assert node != null;

		log.debug("setCacheNode", node);

		return nodesByKey.put(node.getKey(), node);
	}
}
