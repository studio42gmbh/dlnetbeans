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
import java.util.Optional;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;

/**
 *
 * @author Benjamin Schiller
 */
public class DLQuickToolsPanel extends JPanel implements PropertyChangeListener
{

	private final static Logger log = LogManager.getLogger(DLQuickToolsPanel.class.getName());

	private static final RequestProcessor WORKER = new RequestProcessor(DLQuickToolsTopComponent.class.getName(), 1, false, false); //NOI18N

	public final static String QUICK_TOOLS_DL_NAME = "quick-tools.dl";

	protected Path currentQuickToolsDLPath;

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
		TopComponent.getRegistry().addPropertyChangeListener(this);
	}

	public void suspend()
	{
		TopComponent.getRegistry().removePropertyChangeListener(this);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt)
	{
		if (evt.getPropertyName().equals("activatedNodes")) {

			Node[] nodes = (Node[]) evt.getNewValue();

			if (nodes != null && nodes.length == 1) {

				Node node = nodes[0];

				FileObject fObject = node.getLookup().lookup(FileObject.class);

				if (fObject != null) {

					Path path = Path.of(fObject.getPath());

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
			Optional<Path> optQuickToolsDLPath = FileObjectHelper.resolveTraversedRequiredDl(path, QUICK_TOOLS_DL_NAME);
			
			// If a path was found -> Load new quick tools
			if (optQuickToolsDLPath.isPresent()) {

				Path quickToolsDLPath = optQuickToolsDLPath.orElseThrow();
				updateQuickTools(quickToolsDLPath);
			} 
			// Otherwise display empty info
			else {
				updatePanelContent(new JLabel("No quicktools found for this file/folder"));
				currentQuickToolsDLPath = null;
			}
		});
	}
	
	protected void updateQuickTools(Path newToolsDLPath)
	{
		assert newToolsDLPath != null;

		if (Objects.equals(currentQuickToolsDLPath, newToolsDLPath)) {
			return;
		}

		log.debug("Loading quick tools from", newToolsDLPath);
		
		try {
			DLModule module = FileObjectHelper.parseModule(newToolsDLPath);
			
			List<Component> comps = module.getChildrenAsJavaType(Component.class);
			
			if (!comps.isEmpty()) {
				
				// @todo Might want to allow multiple components to be loaded in vertical flow
				
				updatePanelContent((JComponent)comps.get(0).createSwingComponent());				
			}
		} catch (DLException ex) {
			updatePanelContent(new JLabel("Error loading quicktools: " + ex.getMessage()));
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
}
