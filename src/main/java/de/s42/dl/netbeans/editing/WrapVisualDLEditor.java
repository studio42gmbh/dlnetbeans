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
package de.s42.dl.netbeans.editing;

import de.s42.dl.DLModule;
import de.s42.dl.netbeans.editing.api.DLEditor;
import de.s42.dl.ui.visual.VisualDLEditor;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.openide.loaders.DataObject;

/**
 *
 * @author Benjamin Schiller
 */
public class WrapVisualDLEditor implements DLEditor
{

	private final static Logger log = LogManager.getLogger(WrapVisualDLEditor.class.getName());

	protected final DLModule module;
	protected final VisualDLEditor visualEditor;
	protected final Document document;

	public WrapVisualDLEditor(DLModule module, VisualDLEditor visualEditor, Document document)
	{
		assert module != null;
		assert visualEditor != null;
		assert document != null;

		this.module = module;
		this.visualEditor = visualEditor;
		this.document = document;
	}

	@Override
	public boolean canEdit(DataObject dataObject)
	{
		return visualEditor.canEdit(module);
	}

	@Override
	public String getDisplay()
	{
		return visualEditor.getDisplay();
	}

	@Override
	public JPanel getEditorPanel(DataObject dataObject)
	{
		return visualEditor.createEditor(module, document);
	}
}
