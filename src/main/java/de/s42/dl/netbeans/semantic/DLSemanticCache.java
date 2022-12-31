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
package de.s42.dl.netbeans.semantic;

import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.semantic.model.Type;
import de.s42.dl.netbeans.syntax.DLParserResult;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

	protected final Map<String, Set<Type>> typesByKey = Collections.synchronizedMap(new HashMap<>());

	// <editor-fold desc="public static String getCacheKey(.)" defaultstate="collapsed">
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

		return fileObject.getPath();
	}
	//</editor-fold>

	public synchronized boolean hasType(String key, String typeName)
	{
		assert key != null;
		assert typeName != null;

		Set<Type> types = typesByKey.get(key);

		if (types == null) {
			return false;
		}

		return types.stream().anyMatch((type) -> {
			return type.getIdentifier().equals(typeName);
		});
	}

	public List<Type> findTypes(String key, String filter, int caretOffset)
	{
		assert key != null;
		assert filter != null;

		Set<Type> types = typesByKey.get(key);

		if (types == null) {
			return Collections.EMPTY_LIST;
		}

		// Filter out types which match the start with filter and are defined before the offset
		List<Type> result = types.stream().filter((type) -> {
			return // Empty filter or match start case independent
				(filter.isBlank() || type.getIdentifier().toLowerCase().startsWith(filter.toLowerCase()))
				// End of type location before caret offset
				&& type.getEndOffset() < caretOffset;
		}).toList();

		return Collections.unmodifiableList(result);
	}

	public List<Type> getTypes(String key)
	{
		assert key != null;

		//log.debug("getTypeNames", key);
		Set<Type> types = typesByKey.get(key);

		if (types == null) {
			return Collections.EMPTY_LIST;
		}

		return Collections.unmodifiableList(new ArrayList(types));
	}

	public synchronized void addType(String key, Type type)
	{
		assert key != null;
		assert type != null;

		//log.debug("addTypeName", key, typeName);
		Set<Type> types = typesByKey.get(key);

		if (types == null) {
			types = Collections.synchronizedSet(new HashSet<>());
			typesByKey.put(key, types);
		}

		types.add(type);
	}

	public synchronized void clear(String key)
	{
		assert key != null;

		//log.debug("clearTypeNames", key);
		typesByKey.remove(key);
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	//</editor-fold>
}
