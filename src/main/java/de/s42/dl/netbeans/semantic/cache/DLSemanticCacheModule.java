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
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.antlr.v4.runtime.ParserRuleContext;

/**
 *
 * @author Benjamin Schiller
 */
class DLSemanticCacheModule implements DLSemanticCacheNode
{

	private final static Logger log = LogManager.getLogger(DLSemanticCacheModule.class.getName());

	protected final ModuleEntry module;
	protected final String key;
	protected final DLSemanticCache cache;
	protected final Map<String, Type> types = Collections.synchronizedMap(new HashMap<>());
	protected final Set<NodeReference> references = Collections.synchronizedSet(new HashSet<>());

	protected static class NodeReference
	{

		protected int startLine;
		protected int startPosition;
		protected int startOffset;
		protected int endLine;
		protected int endPosition;
		protected int endOffset;
		protected String key;

		// <editor-fold desc="equals/hashCode" defaultstate="collapsed">
		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 79 * hash + Objects.hashCode(this.key);
			return hash;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			final NodeReference other = (NodeReference) obj;
			return Objects.equals(this.key, other.key);
		}
		//</editor-fold>
	}

	public DLSemanticCacheModule(DLSemanticCache cache, String key, ModuleEntry module)
	{
		assert module != null;
		assert key != null;
		assert cache != null;

		this.module = module;
		this.key = key;
		this.cache = cache;
	}

	@Override
	public boolean addType(Type type)
	{
		assert type != null;

		return types.putIfAbsent(type.getIdentifier(), type) == null;
	}

	@Override
	public boolean addNodeReference(String key, ParserRuleContext locationContext)
	{
		assert key != null;
		assert locationContext != null;

		NodeReference reference = new NodeReference();
		reference.key = key;
		reference.startLine = locationContext.start.getLine();
		reference.startPosition = locationContext.start.getCharPositionInLine() + 1;
		reference.startOffset = locationContext.start.getStartIndex();
		reference.endLine = locationContext.start.getLine();
		reference.endOffset = locationContext.start.getStopIndex();
		reference.endPosition = locationContext.start.getCharPositionInLine() + 1 + reference.endOffset - reference.startOffset;

		return references.add(reference);
	}

	@Override
	public boolean hasType(String typeName, int caretOffset, boolean resolveReferences)
	{
		Type type = types.get(typeName);

		if (type != null && type.getEndOffset() <= caretOffset) {
			return true;
		}

		if (!resolveReferences) {
			return false;
		}

		for (NodeReference reference : references) {

			if (reference.endOffset > caretOffset) {
				continue;
			}

			Optional<DLSemanticCacheNode> optNode = cache.getCacheNode(reference.key);

			if (optNode.isEmpty()) {
				continue;
			}

			// The MAX_VALUE makes sure you get the complete results of that node
			if (optNode.orElseThrow().hasType(typeName, Integer.MAX_VALUE, true)) {
				return true;
			}
		}

		return false;
	}

	@Override
	public Set<Type> getTypes(boolean resolveReferences)
	{
		HashSet<Type> result = new HashSet<>(types.values());

		if (!resolveReferences) {
			return result;
		}

		for (NodeReference reference : references) {

			Optional<DLSemanticCacheNode> optNode = cache.getCacheNode(reference.key);

			if (optNode.isEmpty()) {
				continue;
			}

			result.addAll(optNode.orElseThrow().getTypes(true));
		}

		return result;
	}

	@Override
	public Set<Type> findTypes(String query, int caretOffset, boolean resolveReferences)
	{
		HashSet<Type> result = new HashSet<>(types.values().stream().filter((type) -> {
			return // Empty filter or match start case independent
				(query.isBlank() || type.getIdentifier().toLowerCase().startsWith(query.toLowerCase()))
				// End of type location before caret offset
				&& type.getEndOffset() < caretOffset;
		}).toList());

		if (!resolveReferences) {
			return result;
		}

		for (NodeReference reference : references) {

			if (reference.endOffset > caretOffset) {
				continue;
			}

			Optional<DLSemanticCacheNode> optNode = cache.getCacheNode(reference.key);

			if (optNode.isEmpty()) {
				continue;
			}

			// The MAX_VALUE makes sure you get the complete results of that node
			result.addAll(optNode.orElseThrow().findTypes(query, Integer.MAX_VALUE, true));
		}

		return result;
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	@Override
	public ModuleEntry getModule()
	{
		return module;
	}

	@Override
	public String getKey()
	{
		return key;
	}

	@Override
	public DLSemanticCache getCache()
	{
		return cache;
	}
	//</editor-fold>

	@Override
	public String toString()
	{
		return "DLSemanticCacheModule[key = " + key + "]";
	}
}
