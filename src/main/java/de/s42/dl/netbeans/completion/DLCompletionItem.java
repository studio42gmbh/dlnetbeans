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
package de.s42.dl.netbeans.completion;

import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import org.netbeans.api.editor.completion.Completion;
import org.netbeans.spi.editor.completion.CompletionDocumentation;
import org.netbeans.spi.editor.completion.CompletionItem;
import org.netbeans.spi.editor.completion.CompletionResultSet;
import org.netbeans.spi.editor.completion.CompletionTask;
import org.netbeans.spi.editor.completion.support.CompletionUtilities;
import org.openide.util.Exceptions;

/**
 *
 * @author Benjamin Schiller
 */
public class DLCompletionItem implements CompletionItem
{

	private final static Logger log = LogManager.getLogger(DLCompletionItem.class.getName());

	protected String text;
	protected int insertionOffset;
	protected int caretOffset;
	protected boolean addWhitespace;
	protected Document document;

	public class DLCompletionDocumentation implements CompletionDocumentation
	{

		@Override
		public String getText()
		{
			return getDocumentationHtmlText();
		}

		@Override
		public URL getURL()
		{
			try {
				return getDocumentationUrl();
			} catch (MalformedURLException ex) {
				throw new RuntimeException(ex);
			}
		}

		@Override
		public CompletionDocumentation resolveLink(String link)
		{
			assert link != null;

			try {
				Desktop.getDesktop().browse(new URL(link).toURI());
			} catch (IOException | URISyntaxException ex) {
				throw new RuntimeException(ex);
			}
			return null;
		}

		@Override
		public Action getGotoSourceAction()
		{
			final Path gotoFile = getGotoFile();

			if (gotoFile == null) {
				return null;
			}

			if (!Files.isRegularFile(gotoFile)) {
				return null;
			}

			final int gotoLine = getGotoLine();

			return new AbstractAction()
			{
				@Override
				public void actionPerformed(ActionEvent e)
				{
					Completion.get().hideAll();
					FileObjectHelper.openEditorForPathInLine(gotoFile, gotoLine);
				}
			};
		}
	}

	public class DLDocumentationTask implements CompletionTask
	{

		@Override
		public void query(CompletionResultSet crs)
		{
			assert crs != null;

			crs.setDocumentation(new DLCompletionDocumentation());
			crs.finish();
		}

		@Override
		public void refresh(CompletionResultSet crs)
		{
		}

		@Override
		public void cancel()
		{
			Completion.get().hideDocumentation();
		}
	}

	public DLCompletionItem(Document document, int insertionOffset, int caretOffset, boolean addWhitespace)
	{
		assert document != null;
		assert caretOffset >= 0;
		assert insertionOffset >= 0;

		this.document = document;
		this.insertionOffset = insertionOffset;
		this.caretOffset = caretOffset;
		this.addWhitespace = addWhitespace;
	}

	public DLCompletionItem(String text, Document document, int insertionOffset, int caretOffset, boolean addWhitespace)
	{
		assert text != null;
		assert document != null;
		assert caretOffset >= 0;
		assert insertionOffset >= 0;

		this.text = text;
		this.document = document;
		this.insertionOffset = insertionOffset;
		this.caretOffset = caretOffset;
		this.addWhitespace = addWhitespace;
	}

	protected ImageIcon getIcon()
	{
		return null;
	}

	protected Color getTextColor(boolean selected)
	{
		return null;
	}

	protected URL getDocumentationUrl() throws MalformedURLException
	{
		return null;
	}

	protected Path getGotoFile()
	{
		return null;
	}

	protected int getGotoLine()
	{
		return 1;
	}

	protected String getDocumentationHtmlText()
	{
		return "";
	}

	protected String getRightHtmlText()
	{
		return "";
	}

	@Override
	public void defaultAction(JTextComponent component)
	{
		assert component != null;

		try {
			final String tx = getText();

			StringBuilder textToBeInserted = new StringBuilder();
			textToBeInserted.append(tx);
			if (isAddWhitespace()) {
				textToBeInserted.append(" ");
			}

			final Document doc = getDocument();
			final int iOff = getInsertionOffset();
			final int cOff = getCaretOffset();

			if (getCaretOffset() != iOff) {
				String previousString = doc.getText(iOff, cOff - iOff);
				// Replace if string matches start of toen case independent
				if (tx.toLowerCase().startsWith(previousString.toLowerCase())) {
					doc.remove(iOff, cOff - iOff);
					doc.insertString(iOff,
						textToBeInserted.toString(),
						null);
				} // Otherwise just insert the string
				else {
					doc.insertString(cOff,
						textToBeInserted.toString(),
						null);
				}
			} else {
				doc.insertString(cOff,
					textToBeInserted.toString(),
					null);
			}

			Completion.get().hideAll();
		} catch (BadLocationException ex) {
			Exceptions.printStackTrace(ex);
		}
	}

	@Override
	public void processKeyEvent(KeyEvent event)
	{
		assert event != null;

		// Pressed space while open -> Close
		if (event.getID() == KeyEvent.KEY_TYPED
			&& Character.isWhitespace(event.getKeyChar())) {
			Completion.get().hideAll();
			return;
		}

		// Pressed ESC, LEFT or RIGHT while open -> Close
		if (event.getKeyCode() == KeyEvent.VK_ESCAPE
			|| event.getKeyCode() == KeyEvent.VK_LEFT
			|| event.getKeyCode() == KeyEvent.VK_RIGHT) {
			Completion.get().hideAll();
		}
	}

	@Override
	public int getPreferredWidth(Graphics graphics, Font font)
	{
		assert graphics != null;
		assert font != null;

		return CompletionUtilities.getPreferredWidth(
			getText(),
			getRightHtmlText(),
			graphics,
			font
		);
	}

	@Override
	public void render(Graphics graphics, Font font, Color defaultColor, Color backgroundColor, int width, int height, boolean selected)
	{
		assert graphics != null;
		assert font != null;
		assert defaultColor != null;
		assert backgroundColor != null;

		CompletionUtilities.renderHtml(
			getIcon(),
			getText(),
			getRightHtmlText(),
			graphics,
			font,
			getTextColor(selected),
			width,
			height,
			selected);
	}

	@Override
	public CompletionTask createDocumentationTask()
	{
		if (getDocumentationHtmlText() != null) {
			return new DLDocumentationTask();
		}

		return null;
	}

	@Override
	public CompletionTask createToolTipTask()
	{
		return null;
	}

	@Override
	public boolean instantSubstitution(JTextComponent component)
	{
		return false;
	}

	@Override
	public int getSortPriority()
	{
		return 0;
	}

	@Override
	public CharSequence getSortText()
	{
		return getText();
	}

	@Override
	public CharSequence getInsertPrefix()
	{
		return getText();
	}

	@Override
	public int hashCode()
	{
		int hash = 5;
		hash = 41 * hash + Objects.hashCode(getText());
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
		final DLCompletionItem other = (DLCompletionItem) obj;
		return Objects.equals(getText(), other.getText());
	}

	public String getText()
	{
		return text;
	}

	public void setText(String text)
	{
		this.text = text;
	}

	public int getInsertionOffset()
	{
		return insertionOffset;
	}

	public void setInsertionOffset(int insertionOffset)
	{
		this.insertionOffset = insertionOffset;
	}

	public int getCaretOffset()
	{
		return caretOffset;
	}

	public void setCaretOffset(int caretOffset)
	{
		this.caretOffset = caretOffset;
	}

	public boolean isAddWhitespace()
	{
		return addWhitespace;
	}

	public void setAddWhitespace(boolean addWhitespace)
	{
		this.addWhitespace = addWhitespace;
	}

	public Document getDocument()
	{
		return document;
	}

	public void setDocument(Document document)
	{
		this.document = document;
	}
}
