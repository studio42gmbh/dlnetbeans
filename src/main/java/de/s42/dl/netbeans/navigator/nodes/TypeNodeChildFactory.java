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
package de.s42.dl.netbeans.navigator.nodes;

import de.s42.dl.DLAttribute;
import de.s42.dl.DLEnum;
import de.s42.dl.DLType;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Benjamin Schiller
 */
public class TypeNodeChildFactory extends ChildFactory<Object>
{

	private final static Logger log = LogManager.getLogger(TypeNodeChildFactory.class.getName());

	protected final DLType type;

	public TypeNodeChildFactory(DLType type)
	{
		assert type != null;

		this.type = type;
	}

	@Override
	protected boolean createKeys(List<Object> list)
	{
		if (type instanceof DLEnum) {
			list.addAll(((DLEnum)type).getValues());
		}
		else {
			list.addAll(type.getParents());
			list.addAll(type.getOwnAttributes());
		}

		return true;
	}

	@Override
	protected Node createNodeForKey(Object entry)
	{
		if (entry instanceof DLAttribute) {		
			return new AttributeNode((DLAttribute)entry);
		}
		
		if (entry instanceof DLType) {		
			return new TypeNode((DLType)entry);
		}

		if (type instanceof DLEnum && entry != null) {		
			return new ValueNode(entry.toString(), "");
		}
		
		return null;		
	}
}
