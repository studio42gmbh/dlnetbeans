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

import de.s42.dl.netbeans.navigator.DLNavigatorPanel;
import java.awt.Image;
import java.util.Arrays;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Benjamin Schiller
 */
public class ValueNode extends AbstractNode
{

	private final Image ICON = ImageUtilities.loadImage("de/s42/dl/netbeans/navigator/value.png"); // NOI18N

	protected final String name;
	protected final Object value;

	public ValueNode(String name, Object value)
	{
		super(Children.LEAF);

		assert name != null;

		this.value = value;
		this.name = name;
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
		String valueDisplay;

		// Print arrays nicely
		if (value != null && value.getClass().isArray()) {
			valueDisplay = Arrays.toString((Object[]) value);
		} // toString the rest
		else {
			valueDisplay = (value != null) ? value.toString() : "<null>";
		}

		if (valueDisplay.isBlank()) {
			return NbBundle.getMessage(DLNavigatorPanel.class, "LBL_ValueNodeBlankValue", name); // NOI18N
		}

		return NbBundle.getMessage(DLNavigatorPanel.class, "LBL_ValueNode", name, valueDisplay); // NOI18N
	}
	//</editor-fold>
}
