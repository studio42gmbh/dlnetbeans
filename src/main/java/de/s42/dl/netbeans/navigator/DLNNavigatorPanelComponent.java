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

import de.s42.dl.DLModule;
import de.s42.dl.core.BaseDLCore;
import de.s42.dl.core.DefaultCore;
import de.s42.dl.exceptions.DLException;
import de.s42.dl.netbeans.DLDataObject;
import de.s42.dl.netbeans.navigator.nodes.ErrorNode;
import de.s42.dl.netbeans.navigator.nodes.ModuleNode;
import static de.s42.dl.netbeans.navigator.nodes.WaitNode.getWaitNode;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.BorderLayout;
import java.io.IOException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.loaders.DataObject;
import org.openide.util.RequestProcessor;

/**
 * See
 * https://github.com/apache/netbeans/blob/c084119009d2e0f736f225d706bc1827af283501/java/beans/src/org/netbeans/modules/beans/BeanPanelUI.java
 *
 * @author Benjamin Schiller
 */
public class DLNNavigatorPanelComponent extends JPanel implements ExplorerManager.Provider
{

	private final static Logger log = LogManager.getLogger(DLNNavigatorPanelComponent.class.getName());

	protected DLDataObject dataObject;
	protected final ExplorerManager manager = new ExplorerManager();
	protected MyBeanTreeView elementView;
	private static final RequestProcessor WORKER = new RequestProcessor(DLNNavigatorPanelComponent.class.getName());

	public DLNNavigatorPanelComponent()
	{
		init();
	}

	private void init()
	{
		setLayout(new BorderLayout());
		elementView = createBeanTreeView();
		elementView.setRootVisible(true);
		add(elementView, BorderLayout.CENTER);
		manager.setRootContext(getWaitNode());
	}

	public void setNewContent(DataObject dataObject)
	{
		//log.debug("setNewContent");

		// do something
		this.dataObject = (DLDataObject) dataObject;

		if (this.dataObject != null) {
			showContentNode();
		} else {
			showWaitNode();
		}
	}

	public void showContentNode()
	{
		showWaitNode();

		// Update the navigator async
		WORKER.post(() -> {

			try {
				// Parse the DL and create module as root
				//log.start("DLNNavigatorPanelComponent.showContentNode");
				String dlContent = dataObject.getPrimaryFile().asText();
				BaseDLCore core = new BaseDLCore(true);
				DefaultCore.loadResolvers(core);
				DefaultCore.loadAnnotations(core);
				DefaultCore.loadExports(core);
				DefaultCore.loadPragmas(core);
				final DLModule module = core.parse(dataObject.getPrimaryFile().getName(), dlContent);
				final ModuleNode root = new ModuleNode(module);
				//log.stopDebug("DLNNavigatorPanelComponent.showContentNode");

				// Update inside UI thread
				SwingUtilities.invokeLater(() -> {
					elementView.setRootVisible(true);
					manager.setRootContext(root);
				});
			} catch (DLException | IOException | RuntimeException ex) {

				showErrorNode(ex);
				//log.stopDebug("DLNNavigatorPanelComponent.showContentNode");
			}
		});
	}

	public void showErrorNode(Exception ex)
	{
		assert ex != null;

		//log.error(ex.getMessage());
		SwingUtilities.invokeLater(() -> {
			elementView.setRootVisible(true);
			manager.setRootContext(new ErrorNode(ex));
		});
	}

	public void showWaitNode()
	{
		SwingUtilities.invokeLater(() -> {
			elementView.setRootVisible(true);
			manager.setRootContext(getWaitNode());
		});
	}

	private MyBeanTreeView createBeanTreeView()
	{
		return new MyBeanTreeView();
	}

	@Override
	public ExplorerManager getExplorerManager()
	{
		return manager;
	}

	protected static class MyBeanTreeView extends BeanTreeView
	{

		public MyBeanTreeView()
		{
		}

		public boolean getScrollOnExpand()
		{
			return tree.getScrollsOnExpand();
		}

		public void setScrollOnExpand(boolean scroll)
		{
			tree.setScrollsOnExpand(scroll);
		}
	}

	public DLDataObject getDataObject()
	{
		return dataObject;
	}
}
