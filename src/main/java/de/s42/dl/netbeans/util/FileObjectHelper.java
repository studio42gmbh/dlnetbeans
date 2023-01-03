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
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.NbDocument;
import org.openide.util.RequestProcessor;

/**
 *
 * @author Benjamin Schiller
 */
public final class FileObjectHelper
{

	public final static int SET_LINE_RETRIES = 10;
	public final static int SET_LINE_RETRY_WAIT_MS = 200;
	public final static int MAX_RESOLVE_DEPTH = 100;
	public final static String AUTO_REQUIRE_DL_NAME = "auto-require.dl";

	private final static Logger log = LogManager.getLogger(FileObjectHelper.class.getName());

	private FileObjectHelper()
	{
		// never instantiated
	}

	/**
	 * Tries to resolve a fitting nb-project.dl for a given path. It traverses from this directory up until root.
	 * The first macthed is returned. it does not return itself if the path denotes an auto require already.
	 *
	 * @param fileObject
	 *
	 * @return
	 */
	public static Optional<Path> resolveAutoRequireDl(FileObject fileObject)
	{
		assert fileObject != null;

		return resolveAutoRequireDl(Path.of(fileObject.getPath()));
	}

	/**
	 * Tries to resolve a fitting nb-project.dl for a given path. It traverses from this directory up until root.
	 * The first macthed is returned. it does not return itself if the path denotes an auto require already.
	 *
	 * @param path
	 *
	 * @return
	 */
	public static Optional<Path> resolveAutoRequireDl(Path path)
	{
		assert path != null;
		
		try {

			Path currentPath = path.toAbsolutePath().normalize();

			// Get the dir of a given file
			if (Files.isRegularFile(currentPath)) {

				// If the file itself is a auto require file -> return empty to prevent nasty loops in client code
				if (currentPath.endsWith(AUTO_REQUIRE_DL_NAME)) {
					return Optional.empty();
				}

				currentPath = currentPath.getParent();
			}

			// Traverse up the directories and search for first matching auto require
			for (int i = 0; i < MAX_RESOLVE_DEPTH && currentPath != null && Files.isDirectory(currentPath); ++i) {
				Path autoRequireDL = currentPath.resolve(AUTO_REQUIRE_DL_NAME);
				if (Files.isRegularFile(autoRequireDL)) {
					return Optional.of(autoRequireDL);
				}
				currentPath = currentPath.getParent();
			}
		}
		catch (InvalidPathException ex) {
			log.error(ex.getMessage());
		}
		
		return Optional.empty();
	}

	/**
	 * Safely returns the complete text of a document
	 *
	 * @param document
	 *
	 * @return
	 */
	public static String getText(BaseDocument document)
	{
		try {
			document.readLock();
			String text = document.getText(
				document.getStartPosition().getOffset(),
				document.getEndPosition().getOffset() - document.getStartPosition().getOffset() - 1
			);
			document.readUnlock();

			return text;
		} catch (BadLocationException ex) {
			// Should not happen as the access happens in locked state
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Replaces the complete text of the document safely
	 *
	 * @param document
	 * @param text
	 */
	public static void replaceText(BaseDocument document, String text)
	{
		assert document != null;
		assert text != null;

		try {
			document.extWriteLock();
			document.replace(
				document.getStartPosition().getOffset(),
				document.getEndPosition().getOffset() - document.getStartPosition().getOffset() - 1,
				text,
				null
			);
			document.extWriteUnlock();
		} catch (BadLocationException ex) {
			// Should not happen as the access happens in locked state
			throw new RuntimeException(ex);
		}
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
				EditorCookie cookie = dataObject.getLookup().lookup(EditorCookie.class);
				cookie.open();
				final int offset = NbDocument.findLineOffset(cookie.getDocument(), line);
				setLineInDocument(cookie, offset, SET_LINE_RETRIES);
			} catch (DataObjectNotFoundException ex) {
				throw new RuntimeException(ex);
			}
		});
	}

	/**
	 * Set the caret into a certain line in a document
	 *
	 * @param cookie
	 * @param offset
	 * @param retries
	 */
	public static void setLineInDocument(EditorCookie cookie, int offset, int retries)
	{
		assert cookie != null;

		if (retries <= 0) {
			return;
		}

		RequestProcessor.getDefault().post(() -> {
			SwingUtilities.invokeLater(() -> {
				JEditorPane[] openPanes = cookie.getOpenedPanes();
				if (openPanes != null && openPanes.length > 0) {
					openPanes[0].setCaretPosition(offset);
				} else {
					setLineInDocument(cookie, offset, retries - 1);
				}
			});
		}, SET_LINE_RETRY_WAIT_MS);
	}
}
