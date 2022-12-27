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
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.netbeans.api.editor.mimelookup.MimeRegistration;

/**
 *
 * @author Benjamin Schiller
 */
@MimeRegistration(mimeType = DL_MIME_TYPE,
	service = DLSemanticCache.class)
public class DLSemanticCache 
{
	
	private final static Logger log = LogManager.getLogger(DLSemanticCache.class.getName());
	
	protected final Map<String, Set<String>> typeNamesByKey = Collections.synchronizedMap(new HashMap<>());
	
	public synchronized List<String> getTypeNames(String key)
	{
		assert key != null;
		
		//log.debug("getTypeNames", key);
		
		Set<String> typeNames = typeNamesByKey.get(key);		
		
		if (typeNames == null) {
			return Collections.EMPTY_LIST;
		}
		
		return new ArrayList(typeNames);
	}

	public synchronized void addTypeName(String key, String typeName)
	{
		assert key != null;
		assert typeName != null;
		
		//log.debug("addTypeName", key, typeName);
		
		Set<String> typeNames = typeNamesByKey.get(key);		
		
		if (typeNames == null) {
			typeNames = Collections.synchronizedSortedSet(new TreeSet<>());		
			typeNamesByKey.put(key, typeNames);
		}
		
		typeNames.add(typeName);
	}
	
	public synchronized void clearTypeNames(String key)
	{
		assert key != null;
		
		//log.debug("clearTypeNames", key, typeName);
		
		typeNamesByKey.remove(key);
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	//</editor-fold>
}
