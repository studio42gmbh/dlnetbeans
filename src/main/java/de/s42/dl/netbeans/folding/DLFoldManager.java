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
package de.s42.dl.netbeans.folding;

import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.dl.parser.DLLexer;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.netbeans.api.editor.fold.Fold;
import org.netbeans.api.editor.fold.FoldUtilities;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.editor.fold.FoldHierarchyTransaction;
import org.netbeans.spi.editor.fold.FoldInfo;
import org.netbeans.spi.editor.fold.FoldManager;
import org.netbeans.spi.editor.fold.FoldManagerFactory;
import org.netbeans.spi.editor.fold.FoldOperation;
import org.openide.util.Exceptions;

/**
 *
 * @author Benjamin Schiller
 */
public class DLFoldManager implements FoldManager
{

	public final static String COMMENT_COLLAPSE_PREFIX = "/*^";
	public final static String COMMENT_SINGLECOLLAPSE_PREFIX = "//^";

	private final static Logger log = LogManager.getLogger(DLFoldManager.class.getName());

	protected FoldOperation operation;

	protected Map<DLFoldInfo, Fold> currentFolds;

	@Override
	public void init(FoldOperation operation)
	{
		assert operation != null;

		this.operation = operation;
	}

	@Override
	public void initFolds(FoldHierarchyTransaction transaction)
	{
		assert transaction != null;

		if (!FoldUtilities.isFoldingEnabled(DL_MIME_TYPE)) {
			return;
		}

		Map<DLFoldInfo, Fold> newFolds = new HashMap<>();

		List<DLFoldInfo> folds = parseFolds();
		for (DLFoldInfo foldInfo : folds) {
			try {
				FoldInfo info = foldInfo.getFoldInfo();
				newFolds.put(foldInfo, operation.addToHierarchy(info.getType(),
					info.getStart(),
					info.getEnd(),
					info.getCollapsed(),
					info.getTemplate(),
					info.getDescriptionOverride(),
					info.getExtraInfo(),
					transaction));
			} catch (BadLocationException ex) {
				Exceptions.printStackTrace(ex);
			}
		}

		currentFolds = newFolds;
	}

	protected void updateFolds(FoldHierarchyTransaction transaction)
	{
		assert transaction != null;
		assert currentFolds != null;

		if (!FoldUtilities.isFoldingEnabled(DL_MIME_TYPE)) {
			return;
		}

		Map<DLFoldInfo, Fold> newFolds = new HashMap<>(currentFolds);

		// Find all folds which shall get added
		// Get the old and the new folds and iterate the new list 
		//  to create a delta in each list by removing matches in both lists
		HashSet<DLFoldInfo> oldFoldsToRemove = new HashSet<>(newFolds.keySet());
		List<DLFoldInfo> newFoldsToAdd = parseFolds();
		Iterator<DLFoldInfo> it = newFoldsToAdd.iterator();
		while (it.hasNext()) {

			DLFoldInfo info = it.next();

			// If both lists contain the fold -> neither add nor remove that element later
			if (oldFoldsToRemove.remove(info)) {
				it.remove();
			}
		}

		// oldFoldsToRemove now contains all folds that have to be removed
		Iterator<DLFoldInfo> foldInfoIt = oldFoldsToRemove.iterator();
		while (foldInfoIt.hasNext()) {

			DLFoldInfo info = foldInfoIt.next();
			Fold fold = newFolds.remove(info);
			operation.removeFromHierarchy(fold, transaction);
		}

		// newFoldsToAdd now just contains the folds which have to get added
		for (DLFoldInfo foldInfo : newFoldsToAdd) {
			try {
				FoldInfo info = foldInfo.getFoldInfo();
				newFolds.put(foldInfo, operation.addToHierarchy(info.getType(),
					info.getStart(),
					info.getEnd(),
					info.getCollapsed(),
					info.getTemplate(),
					info.getDescriptionOverride(),
					info.getExtraInfo(),
					transaction));
			} catch (BadLocationException ex) {
				Exceptions.printStackTrace(ex);
			}
		}

		currentFolds = newFolds;
	}

	/**
	 * Checks if this region is contained in folds
	 *
	 * @todo This is an intermediate version - properly we had to handle the document changes and change only the
	 * relevant folds there
	 * @param regionId
	 *
	 * @return
	 */
	protected boolean isFoldCollapsed(String regionId)
	{
		if (currentFolds == null) {
			return true;
		}

		for (Fold fold : currentFolds.values()) {
			if (fold.getDescription().equals(regionId)) {
				return fold.isCollapsed();
			}
		}

		return true;
	}

	protected List<DLFoldInfo> parseFolds()
	{
		List<DLFoldInfo> result = new ArrayList<>();
		Stack<Integer> openedScopes = new Stack();
		TokenStream tokens = FileObjectHelper.getDLTokenStream(FileObjectHelper.getText(getDocument()));

		int activeSingleCommentCollapseStart = 0;
		String activeSingleCommentCollapseInfo = null;

		while (true) {
			Token token = tokens.LT(1);

			if (token.getType() == DLLexer.EOF) {
				break;
			} else if (token.getType() == DLLexer.SCOPE_OPEN) {
				openedScopes.push(token.getStartIndex());
			} // Add fold for scopes
			else if (token.getType() == DLLexer.SCOPE_CLOSE) {
				if (!openedScopes.isEmpty()) {
					int startPosition = openedScopes.pop();

					FoldInfo foldInfo = FoldInfo.range(
						startPosition,
						token.getStopIndex() + 1,
						DLFoldType.Scope.type
					);
					result.add(new DLFoldInfo(foldInfo));
				}
			} // Add fold for multiline comments /**^ Display ... */
			else if (token.getType() == DLLexer.MULTILINE_COMMENT) {

				String commentText = token.getText();
				boolean collapsed = commentText.startsWith(COMMENT_COLLAPSE_PREFIX);

				// Parse first line of multiline comment out as text for fold
				String description;
				int nlIndex = commentText.indexOf('\n');
				if (nlIndex > 3) {
					description = commentText.substring(3, nlIndex).trim();
				} else {
					description = DLFoldType.MultiLineComment.type.getTemplate().getDescription();
				}

				FoldInfo foldInfo = FoldInfo
					.range(
						token.getStartIndex(),
						token.getStopIndex() + 1,
						DLFoldType.MultiLineComment.type
					)
					.withDescription(description)
					.collapsed((collapsed) ? isFoldCollapsed(description) : false);
				result.add(new DLFoldInfo(foldInfo));
			} // Allows //^ Display ... //^ Region collapses
			else if (token.getType() == DLLexer.SINGLELINE_COMMENT) {

				String commentText = token.getText();

				if (commentText.startsWith(COMMENT_SINGLECOLLAPSE_PREFIX)) {

					// Start active single collapse region
					if (activeSingleCommentCollapseInfo == null) {

						// Parse first line of comment out as text for fold
						if (commentText.length() > 3) {
							activeSingleCommentCollapseInfo = commentText.substring(3).trim();
						} else {
							activeSingleCommentCollapseInfo = DLFoldType.SingleCommentRegion.type.getTemplate().getDescription();
						}
						activeSingleCommentCollapseStart = token.getStartIndex();
					} else {

						FoldInfo foldInfo = FoldInfo
							.range(
								activeSingleCommentCollapseStart,
								token.getStopIndex() + 1,
								DLFoldType.SingleCommentRegion.type
							)
							.withDescription(activeSingleCommentCollapseInfo)
							.collapsed(isFoldCollapsed(activeSingleCommentCollapseInfo));

						result.add(new DLFoldInfo(foldInfo));
						activeSingleCommentCollapseInfo = null;
					}
				}
			}

			tokens.consume();
		}

		return result;
	}

	public BaseDocument getDocument()
	{
		return (BaseDocument) operation.getHierarchy().getComponent().getDocument();
	}

	@Override
	public void insertUpdate(DocumentEvent de, FoldHierarchyTransaction transaction)
	{
		assert transaction != null;

		updateFolds(transaction);
	}

	@Override
	public void removeUpdate(DocumentEvent de, FoldHierarchyTransaction transaction)
	{
		assert transaction != null;

		updateFolds(transaction);
	}

	@Override
	public void changedUpdate(DocumentEvent de, FoldHierarchyTransaction transaction)
	{
		// do nothing
	}

	@Override
	public void removeEmptyNotify(Fold fold)
	{
		// do nothing
	}

	@Override
	public void removeDamagedNotify(Fold fold)
	{
		// do nothing
	}

	@Override
	public void expandNotify(Fold fold)
	{
		// do nothing
	}

	@Override
	public void release()
	{
		// do nothing
	}

	/**
	 * This factory creates FoldManager instances and gets registered in the Netbeans system to activate the folding
	 * handling ATTENTION: It is important to have this Factory inside the manager otherwise the factory will not be
	 * recognized by Netbeans!
	 */
	@MimeRegistration(mimeType = "", service = FoldManagerFactory.class, position = 421)
	public static class DLFoldManagerFactory implements FoldManagerFactory
	{

		@Override
		public FoldManager createFoldManager()
		{
			return new DLFoldManager();
		}
	}
}
