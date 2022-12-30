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

import de.s42.dl.DLInstance;
import de.s42.dl.DLModule;
import de.s42.dl.DLType;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.List;
import java.util.Map.Entry;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Node;

/**
 *
 * @author Benjamin Schiller
 */
public class ModuleNodeChildFactory extends ChildFactory<Object>
{

	private final static Logger log = LogManager.getLogger(ModuleNodeChildFactory.class.getName());

	protected final DLModule module;

	public ModuleNodeChildFactory(DLModule module)
	{
		assert module != null;

		this.module = module;
	}

	@Override
	protected boolean createKeys(List<Object> list)
	{
		list.addAll(module.getChildren().stream().filter((child) -> {
			return child instanceof DLModule;
		}).toList());
		list.addAll(module.getDefinedTypes());
		list.addAll(module.getChildren().stream().filter((child) -> {
			return !(child instanceof DLModule);
		}).toList());
		list.addAll(module.getAttributes().entrySet());

		return true;
	}

	@Override
	protected Node createNodeForKey(Object entity)
	{
		if (entity instanceof DLType) {
			return new TypeNode((DLType) entity);
		}
		
		if (entity instanceof DLModule) {
			return new ModuleNode((DLModule) entity);
		}

		if (entity instanceof DLInstance) {
			return new InstanceNode((DLInstance) entity);
		}

		if (entity instanceof Entry) {
			return new ValueNode(
				(String) ((Entry) entity).getKey(),
				((Entry) entity).getValue()
			);
		}

		return null;
	}
}
