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
package de.s42.dl.netbeans;

import de.s42.dl.language.DLConstants;
import de.s42.dl.language.DLFileType;
import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import org.openide.filesystems.MIMEResolver;

@Messages({
	"LBL_DL_LOADER=Files of DL"
})
@MIMEResolver.ExtensionRegistration(
	displayName = "#LBL_DL_LOADER",
	mimeType = DL_MIME_TYPE,
	extension = {DLFileType.HRF_EXTENSION},
	position = 420
)
@DataObject.Registration(
	mimeType = DL_MIME_TYPE,
	iconBase = "de/s42/dl/netbeans/dl-icon.png",
	displayName = "#LBL_DL_LOADER",
	position = 300
)
@ActionReferences({
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
		position = 100,
		separatorAfter = 200
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
		position = 300
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
		position = 400,
		separatorAfter = 500
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
		position = 600
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
		position = 700,
		separatorAfter = 800
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
		position = 900,
		separatorAfter = 1000
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
		position = 1100,
		separatorAfter = 1200
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
		position = 1300
	),
	@ActionReference(
		path = "Loaders/text/dl/Actions",
		id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
		position = 1400
	)
})
public class DLDataObject extends MultiDataObject
{

	public static final String DL_MIME_TYPE = DLConstants.MIME_TYPE;

	public DLDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException
	{
		super(pf, loader);
		registerEditor(DL_MIME_TYPE, true);
	}

	@Override
	protected int associateLookup()
	{
		return 1;
	}

	@MultiViewElement.Registration(
		displayName = "#LBL_DL_EDITOR",
		iconBase = "de/s42/dl/netbeans/dl-icon.png",
		mimeType = DL_MIME_TYPE,
		persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
		preferredID = "DL",
		position = 201
	)
	@Messages("LBL_DL_EDITOR=Source")
	public static MultiViewEditorElement createEditor(Lookup lookup)
	{
		MultiViewEditorElement multiView = new MultiViewEditorElement(lookup);
		return multiView;
	}
}
