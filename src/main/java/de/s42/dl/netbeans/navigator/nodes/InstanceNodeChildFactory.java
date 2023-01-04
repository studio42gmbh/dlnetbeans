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
import de.s42.dl.DLInstance;
import java.util.List;
import java.util.Map;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Benjamin Schiller
 */
public class InstanceNodeChildFactory extends ChildFactory<Object>
{

	protected final DLInstance instance;

	public InstanceNodeChildFactory(DLInstance instance)
	{
		assert instance != null;

		this.instance = instance;
	}

	@Override
	protected boolean createKeys(List<Object> list)
	{
		//list.addAll(instance.getType().getAttributes());
		list.addAll(instance.getChildren());
		list.addAll(instance.getAttributes().entrySet());

		return true;
	}

	@Override
	protected Node createNodeForKey(Object entity)
	{
		if (entity instanceof DLInstance) {
			return new InstanceNode((DLInstance) entity);
		}

		if (entity instanceof DLAttribute) {
			return new AttributeNode((DLAttribute) entity);
		}

		if (entity instanceof String) {
			return new AttributeNode((DLAttribute) entity);
		}

		if (entity instanceof Map.Entry) {
			return new ValueNode(
				(String) ((Map.Entry) entity).getKey(),
				((Map.Entry) entity).getValue()
			);
		}

		return null;
	}
}
