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
package de.s42.dl.netbeans.editing;

import de.s42.dl.DLModule;
import de.s42.dl.core.BaseDLCore;
import de.s42.dl.core.DefaultCore;
import de.s42.dl.exceptions.DLException;
import de.s42.dl.netbeans.DLDataObject;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.editing.api.DLEditor;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Box;
import javax.swing.ListCellRenderer;
import javax.swing.SwingUtilities;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.dl.ui.visual.VisualDLEditor;
import java.nio.file.Path;
import java.util.Optional;
import javax.swing.JComboBox;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.parsing.api.Source;
import org.openide.filesystems.FileObject;

@MultiViewElement.Registration(
	displayName = "#LBL_DL_VISUAL",
	iconBase = "de/s42/dl/netbeans/dl-icon.png",
	mimeType = DL_MIME_TYPE,
	persistenceType = TopComponent.PERSISTENCE_NEVER,
	preferredID = "DLVisual",
	position = 2000
)
@Messages("LBL_DL_VISUAL=Visual")
public final class DLEditorPanel extends JPanel implements MultiViewElement
{

	private final static Logger log = LogManager.getLogger(DLEditorPanel.class.getName());

	private final DLDataObject dataObject;
	private final JToolBar toolbar = new JToolBar();
	private transient MultiViewElementCallback callback;

	public DLEditorPanel(Lookup lkp)
	{
		dataObject = lkp.lookup(DLDataObject.class);

		assert dataObject != null;
		initComponents();
		initEditor();
	}

	private static void loadVisualDLEditors(FileObject fileObject, JComboBox<DLEditor> selection) throws DLException
	{
		assert fileObject != null;
		assert selection != null;

		// Add all editors which are defined in an auto resolved visual-require.dl
		Path fileObjectPath = Path.of(fileObject.getPath());

		// Resolve optional "visual-require.dl"
		Optional<Path> optVisualRequire = FileObjectHelper.resolveTraversedRequiredDl(fileObjectPath, "visual-require.dl");
		if (optVisualRequire.isPresent()) {

			// Parse the visual require
			BaseDLCore core = new BaseDLCore(true);
			DefaultCore.loadResolvers(core);
			DefaultCore.loadAnnotations(core);
			DefaultCore.loadPragmas(core);
			DefaultCore.loadTypes(core);
			DefaultCore.loadExports(core);
			core.getPathResolver().addResolveDirectory(fileObjectPath.getParent());
			DLModule visualRequireModule = core.parse(optVisualRequire.orElseThrow().toString());

			// If visual require contains at least one VisuaDLEditor instance
			List<VisualDLEditor> visualEditors = visualRequireModule.getChildrenAsJavaType(VisualDLEditor.class);
			if (!visualEditors.isEmpty()) {

				// Resolve the document and parse the dl file pointed by for this main editor
				BaseDocument document = (BaseDocument) Source.create(fileObject).getDocument(true);
				DLModule module = core.parse(fileObjectPath.toString());

				// Construct wrapper to embed the VisualDLEditors
				for (VisualDLEditor visualEditor : visualEditors) {

					WrapVisualDLEditor wrapEditor = new WrapVisualDLEditor(module, visualEditor, document);

					if (visualEditor.canEdit(wrapEditor)) {
						selection.addItem(wrapEditor);
					}
				}
			}
		}
	}

	private void initEditor()
	{
		log.debug("initEditor");

		// Setup toolbar - filler code is a bit specific
		// see also https://github.com/apache/netbeans/blob/4ae01ea70f4530443343beee3292e880a74099bd/profiler/lib.profiler.ui/src/org/netbeans/lib/profiler/ui/components/ProfilerToolbar.java
		toolbar.addSeparator();
		toolbar.add(selectEditor);
		toolbar.addSeparator();
		Dimension minDim = new Dimension(0, 0);
		Dimension maxDim = new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
		toolbar.add(new Box.Filler(minDim, minDim, maxDim));
		toolbar.repaint();

		updateEditor();
	}

	private void updateEditor()
	{
		log.debug("updateEditor");
		log.start("updateEditor");

		selectEditor.removeAllItems();

		try {

			List<DLEditor> editors = new ArrayList<>(MimeLookup.getLookup(DL_MIME_TYPE).lookupAll(DLEditor.class));

			// Add all editors that could edit this DL
			for (DLEditor editor : editors) {
				if (editor.canEdit(dataObject)) {
					selectEditor.addItem(editor);
				}
			}

			loadVisualDLEditors(dataObject.getPrimaryFile(), selectEditor);

			// If no editor found -> Disable dropdown
			if (selectEditor.getItemCount() == 0) {
				selectEditor.setEnabled(false);
			} else {
				selectEditor.setSelectedIndex(0);

				if (selectEditor.getItemCount() == 1) {
					selectEditor.setEnabled(false);
				}
			}
		} catch (DLException ex) {
			throw new RuntimeException("Error loading editor - " + ex.getMessage(), ex);
		} finally {
			log.stopDebug("updateEditor");
		}
	}

	protected ListCellRenderer createSelectEditorRenderer()
	{
		return new DLEditorRenderer();
	}

	@Override
	public String getName()
	{
		return "DLVisualElement";
	}

	/**
	 * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
	 * content of this method is always regenerated by the Form Editor.
	 */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        selectEditor = new javax.swing.JComboBox<>();
        editorContainer = new javax.swing.JPanel();
        emptyInfo = new javax.swing.JLabel();

        selectEditor.setPreferredSize(new java.awt.Dimension(150, 20));
        selectEditor.setRenderer(createSelectEditorRenderer());
        selectEditor.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                selectEditorActionPerformed(evt);
            }
        });

        editorContainer.setToolTipText(org.openide.util.NbBundle.getMessage(DLEditorPanel.class, "DLEditorPanel.editorContainer.toolTipText")); // NOI18N

        emptyInfo.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        emptyInfo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        emptyInfo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/de/s42/dl/netbeans/navigator/error.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(emptyInfo, org.openide.util.NbBundle.getMessage(DLEditorPanel.class, "DLEditorPanel.emptyInfo.text")); // NOI18N

        javax.swing.GroupLayout editorContainerLayout = new javax.swing.GroupLayout(editorContainer);
        editorContainer.setLayout(editorContainerLayout);
        editorContainerLayout.setHorizontalGroup(
            editorContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(editorContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(emptyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        editorContainerLayout.setVerticalGroup(
            editorContainerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, editorContainerLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(emptyInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 288, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editorContainer, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(editorContainer, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void selectEditorActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_selectEditorActionPerformed
    {//GEN-HEADEREND:event_selectEditorActionPerformed

		DLEditor editor = (DLEditor) selectEditor.getSelectedItem();

		// Make sure the given editor can edit the data object
		if (editor != null && editor.canEdit(dataObject)) {

			final JPanel editPanel = editor.getEditorPanel(dataObject);

			// Insert the new editor within the swing thread and update
			SwingUtilities.invokeLater(() -> {
				callback.updateTitle(editor.getDisplay());
				editorContainer.removeAll();
				editorContainer.setLayout(new BorderLayout());
				editorContainer.add(editPanel, BorderLayout.CENTER);
				editorContainer.revalidate();
				editorContainer.repaint();
			});
		}
    }//GEN-LAST:event_selectEditorActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel editorContainer;
    private javax.swing.JLabel emptyInfo;
    private javax.swing.JComboBox<DLEditor> selectEditor;
    // End of variables declaration//GEN-END:variables
	@Override
	public JComponent getVisualRepresentation()
	{
		return this;
	}

	@Override
	public JComponent getToolbarRepresentation()
	{
		return toolbar;
	}

	@Override
	public Action[] getActions()
	{
		return new Action[0];
	}

	@Override
	public Lookup getLookup()
	{
		return dataObject.getLookup();
	}

	@Override
	public void componentOpened()
	{
		log.debug("componentOpened");
	}

	@Override
	public void componentClosed()
	{
		log.debug("componentClosed");
	}

	@Override
	public void componentShowing()
	{
		log.debug("componentShowing");
		updateEditor();
	}

	@Override
	public void componentHidden()
	{
		log.debug("componentHidden");
		editorContainer.removeAll();
	}

	@Override
	public void componentActivated()
	{
		log.debug("componentActivated");
	}

	@Override
	public void componentDeactivated()
	{
		log.debug("componentDeactivated");
	}

	@Override
	public UndoRedo getUndoRedo()
	{
		return UndoRedo.NONE;
	}

	@Override
	public void setMultiViewCallback(MultiViewElementCallback callback)
	{
		this.callback = callback;
	}

	@Override
	public CloseOperationState canCloseElement()
	{
		return CloseOperationState.STATE_OK;
	}
}
