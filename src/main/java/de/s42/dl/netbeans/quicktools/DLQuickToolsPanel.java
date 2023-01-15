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
package de.s42.dl.netbeans.quicktools;

import de.s42.dl.DLModule;
import de.s42.dl.exceptions.DLException;
import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.dl.ui.components.Component;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import org.netbeans.modules.progress.spi.Controller;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileRenameEvent;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Benjamin Schiller
 */
public class DLQuickToolsPanel extends JPanel implements PropertyChangeListener, ListDataListener, FileChangeListener
{

	private final static Logger log = LogManager.getLogger(DLQuickToolsPanel.class.getName());

	private static final RequestProcessor WORKER = new RequestProcessor(DLQuickToolsTopComponent.class.getName(), 1, false, false); //NOI18N

	public final static String QUICK_TOOLS_DL_NAME = "quick-tools.dl";

	protected Path currentQuickToolsDLPath;

	protected FileObject currentFileObject;

	public DLQuickToolsPanel()
	{
		initComponent();
	}

	private void initComponent()
	{
		setLayout(new BorderLayout());
	}

	public void init()
	{
		// This makes sure to be informed about changed selections in the ui
		TopComponent.getRegistry().addPropertyChangeListener(this);

		// This is a hackish way of determining that some long running task (-> compilation) has finished -> update view
		// I still did not find out another generic way of determining when a build has been finished ...
		Controller.getDefault().getModel().addListDataListener(this);
	}

	public void suspend()
	{
		TopComponent.getRegistry().removePropertyChangeListener(this);
		Controller.getDefault().getModel().removeListDataListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		//log.warn(evt.getPropertyName());

		if (evt.getPropertyName().equals("activatedNodes")) {

			Node[] nodes = (Node[]) evt.getNewValue();

			if (nodes != null && nodes.length == 1) {

				Node node = nodes[0];

				FileObject fObject = node.getLookup().lookup(FileObject.class);

				if (fObject != null) {

					Path path = Path.of(fObject.getPath());

					if (currentFileObject != null) {
						currentFileObject.removeFileChangeListener(this);
					}

					fObject.addFileChangeListener(this);
					currentFileObject = fObject;

					findAndLoadQuickToolsForPath(path);
				}
			}
		}
	}

	protected void findAndLoadQuickToolsForPath(Path path)
	{
		assert path != null;

		WORKER.execute(() -> {

			// Retrieve quick-tool.dl
			Path quickToolsDLPath = FileObjectHelper.resolveTraversedRequiredDl(path, QUICK_TOOLS_DL_NAME).orElse(null);

			updateQuickTools(quickToolsDLPath, false);
		});
	}

	protected void updateQuickTools(Path newToolsDLPath, boolean force)
	{
		if (!force && Objects.equals(currentQuickToolsDLPath, newToolsDLPath)) {
			return;
		}

		if (newToolsDLPath == null) {
			updatePanelContent(new JLabel("No quicktools found for this file/folder"));
			currentQuickToolsDLPath = null;
			return;
		}

		log.debug("Loading quick tools from", newToolsDLPath);

		try {
			DLModule module = FileObjectHelper.parseModule(newToolsDLPath);

			List<Component> comps = module.getChildrenAsJavaType(Component.class);

			if (!comps.isEmpty()) {

				// @todo Might want to allow multiple components to be loaded in vertical flow
				updatePanelContent((JComponent) comps.get(0).createSwingComponent());
			}
		} catch (DLException ex) {
			updatePanelContent(new JLabel("Error loading quicktools"));
		}

		currentQuickToolsDLPath = newToolsDLPath;
	}

	protected void updatePanelContent(JComponent content)
	{
		assert content != null;

		SwingUtilities.invokeLater(() -> {
			removeAll();
			add(content, BorderLayout.CENTER);
			getParent().revalidate();
			getParent().repaint();
		});
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	//</editor-fold>
	@Override
	public void intervalAdded(ListDataEvent e)
	{
		//log.debug("intervalAdded",e);
	}

	@Override
	public void intervalRemoved(ListDataEvent e)
	{
		//log.debug("intervalRemoved",e);
		updateQuickTools(currentQuickToolsDLPath, true);
	}

	@Override
	public void contentsChanged(ListDataEvent e)
	{
		log.warn("contentsChanged");

		WORKER.schedule(() -> {
			updateQuickTools(currentQuickToolsDLPath, true);
		}, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void fileFolderCreated(FileEvent fe)
	{
		//log.debug("fileFolderCreated", e);
	}

	@Override
	public void fileDataCreated(FileEvent fe)
	{
		//log.debug("fileDataCreated", e);
	}

	@Override
	public void fileChanged(FileEvent fe)
	{
		log.warn("fileChanged");

		WORKER.schedule(() -> {
			updateQuickTools(currentQuickToolsDLPath, true);
		}, 500, TimeUnit.MILLISECONDS);
	}

	@Override
	public void fileDeleted(FileEvent fe)
	{
		//log.debug("fileDeleted", e);
	}

	@Override
	public void fileRenamed(FileRenameEvent fre)
	{
		//log.debug("fileRenamed", e);
	}

	@Override
	public void fileAttributeChanged(FileAttributeEvent fae)
	{
		//log.debug("fileAttributeChanged");
	}
}
