// <editor-fold desc="The MIT License" defaultstate="collapsed">
/*
 * The MIT License
 * 
 * Copyright 2023 Studio 42 GmbH ( https://www.s42m.de ).
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

import de.s42.dl.netbeans.semantic.model.ModuleEntry;
import de.s42.dl.netbeans.semantic.model.Type;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Benjamin Schiller
 */
public interface DLSemanticCacheNode
{
	public DLSemanticCache getCache();
	
	public String getKey();
	
	public ModuleEntry getModule();

	public Set<Type> getTypes(boolean resolveReferences);

	/**
	 * Add a new type to this node
	 * @param type
	 * @return 
	 */
	public boolean addType(Type type);
	
	/**
	 * Adds a loose reference to another node which might not yet have been resolved (used to reflect relationships like require)
	 * @param key
	 * @param locationContext
	 * @return 
	 */
	public boolean addNodeReference(String key, ParserRuleContext locationContext);
	
	// QUERIES
	public boolean hasType(String typeName, int caretOffset, boolean resolveReferences);
	
	
	public Set<Type> findTypes(String query, int caretOffset, boolean resolveReferences);
}
