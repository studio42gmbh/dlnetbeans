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
package de.s42.dl.netbeans.navigator;

import de.s42.dl.netbeans.DLDataObject;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.Collection;
import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.spi.navigator.NavigatorPanel;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;

/**
 *
 * @author Benjamin Schiller
 */
@NavigatorPanel.Registration(mimeType = DL_MIME_TYPE, displayName = "#Navigator_DisplayName")
public class DLNavigatorPanel implements NavigatorPanel
{

	private final static Logger log = LogManager.getLogger(DLNavigatorPanel.class.getName());

	private static final Lookup.Template DL_DATA = new Lookup.Template(DLDataObject.class);

	protected DLNNavigatorPanelComponent component;
	protected Lookup.Result currentContext;
	protected LookupListener contextListener;
	private long lastSaveTime = -1;
	private FileChangeListener fileChangeListener;

	@Override
	public String getDisplayName()
	{
		return NbBundle.getMessage(DLNavigatorPanel.class, "Navigator_DisplayName");
	}

	@Override
	public String getDisplayHint()
	{
		return NbBundle.getMessage(DLNavigatorPanel.class, "Navigator_DisplayHint");
	}

	@Override
	public synchronized DLNNavigatorPanelComponent getComponent()
	{
		if (component == null) {
			component = new DLNNavigatorPanelComponent();
		}

		return component;
	}

	@Override
	public void panelActivated(Lookup context)
	{
		assert context != null;

		currentContext = context.lookup(DL_DATA);
		currentContext.addLookupListener(getContextListener());
		Collection data = currentContext.allInstances();

		setNewContent(getDataObject(data));
	}

	@Override
	public void panelDeactivated()
	{
		currentContext.removeLookupListener(getContextListener());
		currentContext = null;

		setNewContent(null);
	}

	@Override
	public Lookup getLookup()
	{
		// go with default activated Node strategy
		return null;
	}

	protected DataObject getDataObject(Collection data)
	{
		assert data != null;

		DataObject dataObject = null;
		Iterator<?> it = data.iterator();
		while (it.hasNext()) {
			Object o = it.next();
			if (o instanceof DataObject) {
				dataObject = (DataObject) o;
				break;
			}
		}
		return dataObject;
	}

	protected void setNewContent(DataObject dataObject)
	{
		// Remove file change listener from old data object
		DataObject oldDataObject = getComponent().getDataObject();
		if (oldDataObject != null) {
			oldDataObject.getPrimaryFile().removeFileChangeListener(getFileChangeListener());
		}

		getComponent().setNewContent((DLDataObject) dataObject);

		// Add file change listener to new data object
		if (dataObject != null) {
			dataObject.getPrimaryFile().addFileChangeListener(getFileChangeListener());
		}
	}

	/**
	 * Accessor for listener to context
	 */
	protected synchronized LookupListener getContextListener()
	{
		if (contextListener == null) {
			contextListener = new ContextListener();
		}
		return contextListener;
	}

	protected synchronized FileChangeListener getFileChangeListener()
	{
		if (fileChangeListener == null) {
			fileChangeListener = new DLFileChangeAdapter();
		}

		return fileChangeListener;
	}

	/**
	 * Listens to changes of context and triggers proper action
	 */
	private class ContextListener implements LookupListener
	{

		@Override
		public void resultChanged(LookupEvent ev)
		{
			Collection data = ((Lookup.Result) ev.getSource()).allInstances();
			setNewContent(getDataObject(data));
		}
	}

	private class DLFileChangeAdapter extends FileChangeAdapter
	{

		@Override
		public void fileChanged(final FileEvent event)
		{
			assert event != null;

			//log.debug("fileChanged", event.getFile().getName());
			if (event.getTime() > lastSaveTime) {
				lastSaveTime = System.currentTimeMillis();

				// Refresh image viewer
				SwingUtilities.invokeLater(() -> {
					try {
						getComponent().setNewContent((DLDataObject) DataObject.find(event.getFile()));
					} catch (DataObjectNotFoundException ex) {
						throw new RuntimeException(ex);
					}
				});
			}
		}
	}
}
