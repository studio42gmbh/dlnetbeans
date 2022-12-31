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
package de.s42.dl.netbeans.util;

import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.nio.file.Path;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;

/**
 *
 * @author Benjamin Schiller
 */
public final class FileObjectHelper
{

	private final static Logger log = LogManager.getLogger(FileObjectHelper.class.getName());

	private FileObjectHelper()
	{
		// never instantiated
	}

	/**
	 * Opens a file object and scrolls to the given line For full ui crazyness see
	 * https://github.com/apache/netbeans/blob/c084119009d2e0f736f225d706bc1827af283501/ide/utilities/src/org/netbeans/modules/openfile/DefaultOpenFileImpl.java
	 * For now this implements the minimal version - works fine so far
	 *
	 * @param fileObject
	 * @param line
	 */
	public static void openEditorForPathInLine(Path path, int line)
	{
		SwingUtilities.invokeLater(() -> {
			try {
				FileObject gotoFileObject = FileUtil.toFileObject(path.toFile());
				DataObject dataObject = DataObject.find(gotoFileObject);
				EditorCookie cookie = dataObject.getCookie(EditorCookie.class);
				cookie.open();
				final int offset = NbDocument.findLineOffset(cookie.getDocument(), line);
				JEditorPane[] openPanes = cookie.getOpenedPanes();
				if (openPanes != null && openPanes.length > 0) {
					openPanes[0].setCaretPosition(offset);
				}
			} catch (DataObjectNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		});
	}
}
