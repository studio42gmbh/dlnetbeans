
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

import de.s42.dl.netbeans.editing.api.DLEditor;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Source;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Benjamin Schiller
 */
@MimeRegistration(mimeType = DL_MIME_TYPE, service = DLEditor.class, position = 1000)
public class SimpleDLEditor implements DLEditor
{
	
	public final static String EDITOR_EDIT_ENDING = ".project.dl";

	private final static Logger log = LogManager.getLogger(SimpleDLEditor.class.getName());

	protected final static String DISPLAY = NbBundle.getMessage(SimpleDLEditor.class, "Simple_DLEditorDisplay");
	
	/**
	 * This editor is activated if the DL file ends with .project.dl
	 * @param dataObject
	 * @return 
	 */
	@Override
	public boolean canEdit(DataObject dataObject)
	{
		assert dataObject != null;
		
		String fileName = dataObject.getPrimaryFile().getNameExt();
		
		return fileName.endsWith(EDITOR_EDIT_ENDING);
	}

	@Override
	public String getDisplay()
	{
		return DISPLAY;
	}
	
	@Override
	public JPanel getEditorPanel(DataObject dataObject)
	{
		assert dataObject != null;

		//log.debug("getEditorPanel");

		JPanel panel = new JPanel();

		panel.add(new JLabel("Simple:" + dataObject.getName()));

		JButton doStuff = new JButton("Do Stuff");
		panel.add(doStuff);
		doStuff.addActionListener((evt) -> {
			try {
				doStuff((BaseDocument) Source.create(dataObject.getPrimaryFile()).getDocument(false));
			} catch (BadLocationException ex) {
				throw new RuntimeException(ex);
			}
		});

		return panel;
	}

	protected synchronized void doStuff(BaseDocument document) throws BadLocationException
	{
		assert document != null;

		log.debug("doStuff");

		// Read whole document
		document.readLock();
		String text = document.getText(document.getStartPosition().getOffset(), document.getEndPosition().getOffset());
		document.readUnlock();

		log.debug("OLD\n", text);

		// Replace whole document
		document.extWriteLock();
		document.replace(0, document.getEndPosition().getOffset() - 1, "String t : 4;\n", null);
		document.extWriteUnlock();
	}
}
