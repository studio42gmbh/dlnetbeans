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

import de.s42.dl.DLType;
import de.s42.dl.netbeans.navigator.DLNavigatorPanel;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.Image;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Benjamin Schiller
 */
public class TypeNode extends AbstractNode
{

	private final static Logger log = LogManager.getLogger(TypeNode.class.getName());

	private final Image ICON = ImageUtilities.loadImage("de/s42/dl/netbeans/navigator/type.png"); // NOI18N

	protected final DLType type;

	public TypeNode(DLType type)
	{
		super(Children.LEAF);

		assert type != null;

		this.type = type;

		setChildren(Children.create(new TypeNodeChildFactory(this.type), true));
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	@Override
	public Image getIcon(int type)
	{
		return ICON;
	}

	@Override
	public Image getOpenedIcon(int type)
	{
		return getIcon(type);
	}

	@Override
	public String getHtmlDisplayName()
	{
		return NbBundle.getMessage(DLNavigatorPanel.class, "LBL_TypeNode", type); // NOI18N
	}
	//</editor-fold>
}
