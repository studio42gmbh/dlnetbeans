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

import de.s42.dl.netbeans.util.FileObjectHelper;
import de.s42.dl.parser.DLLexer;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import javax.swing.event.DocumentEvent;
import javax.swing.text.BadLocationException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.netbeans.api.editor.fold.Fold;
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

	private final static Logger log = LogManager.getLogger(DLFoldManager.class.getName());

	protected FoldOperation operation;

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

		List<FoldInfo> folds = parseFolds();
		for (FoldInfo foldInfo : folds) {
			try {
				operation.addToHierarchy(foldInfo.getType(),
					foldInfo.getStart(),
					foldInfo.getEnd(),
					foldInfo.getCollapsed(),
					foldInfo.getTemplate(),
					foldInfo.getDescriptionOverride(),
					foldInfo.getExtraInfo(),
					transaction);
			} catch (BadLocationException ex) {
				Exceptions.printStackTrace(ex);
			}
		}
	}

	protected void clearFolds(FoldHierarchyTransaction transaction)
	{
		assert transaction != null;

		Iterator<Fold> foldIt = operation.foldIterator();
		while (foldIt.hasNext()) {
			Fold fold = foldIt.next();
			operation.removeFromHierarchy(fold, transaction);
			foldIt = operation.foldIterator();
		}
	}

	protected List<FoldInfo> parseFolds()
	{
		List<FoldInfo> result = new ArrayList<>();
		Stack<Integer> openedScopes = new Stack();
		TokenStream tokens = FileObjectHelper.getDLTokenStream(FileObjectHelper.getText(getDocument()));

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
					result.add(foldInfo);
				}
			} // Add fold for multiline comments 
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

				FoldInfo foldInfo = FoldInfo.range(
					token.getStartIndex(),
					token.getStopIndex() + 1,
					DLFoldType.MultiLineComment.type
				).withDescription(description).collapsed(collapsed);
				result.add(foldInfo);
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

		clearFolds(transaction);
		initFolds(transaction);
	}

	@Override
	public void removeUpdate(DocumentEvent de, FoldHierarchyTransaction transaction)
	{
		assert transaction != null;

		clearFolds(transaction);
		initFolds(transaction);
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
