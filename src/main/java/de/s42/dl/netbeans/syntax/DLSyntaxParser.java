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
package de.s42.dl.netbeans.syntax;

import de.s42.dl.core.BaseDLCore;
import de.s42.dl.core.DefaultCore;
import de.s42.dl.core.resolvers.FileCoreResolver;
import de.s42.dl.exceptions.ReservedKeyword;
import de.s42.dl.netbeans.semantic.DLSemanticParser;
import de.s42.dl.netbeans.syntax.hints.DLParsingError;
import de.s42.dl.parser.DLLexer;
import de.s42.dl.parser.DLParser;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.nio.file.Path;
import javax.swing.event.ChangeListener;
import javax.swing.text.StyledDocument;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.api.Task;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.parsing.spi.Parser;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;
import org.openide.filesystems.FileObject;
import org.openide.text.NbDocument;

/**
 *
 * @author Benjamin Schiller
 */
public class DLSyntaxParser extends Parser
{

	private final static Logger log = LogManager.getLogger(DLSyntaxParser.class.getName());

	private DLParserResult parserResult;

	public static void parseContent(DLParserResult result, String moduleId, String content, BaseDLCore core)
	{
		assert result != null;
		assert content != null;
		assert core != null;

		// Setup lexer
		DLLexer lexer = new DLLexer(CharStreams.fromString(content));
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		DLParser parser = new DLParser(tokens);
		lexer.removeErrorListeners();
		lexer.addErrorListener(new DLParserErrorHandler(result));
		parser.removeErrorListeners();
		parser.addErrorListener(new DLParserErrorHandler(result));
		parser.addParseListener(new DLSemanticParser(result, core, moduleId));

		// Process the parser rules
		parser.data();
	}

	protected static class DLParserErrorHandler extends BaseErrorListener
	{

		private final DLParserResult parserResult;

		protected DLParserErrorHandler(DLParserResult parserResult)
		{
			this.parserResult = parserResult;
		}

		@Override
		public void syntaxError(Recognizer<?, ?> rcgnzr, Object offendingSymbol, int line, int position, String message, RecognitionException re)
		{
			int startPosition;
			int endPosition;

			Token token;
			if (offendingSymbol instanceof Token) {
				token = (Token) offendingSymbol;
				startPosition = token.getStartIndex();
				endPosition = token.getStopIndex() + 1;
			} // Currently if the lexer has an error the offendingSymbol is always null :/ -> So let restore the position from line and offset
			else {
				StyledDocument doc = (StyledDocument) parserResult.getSnapshot().getSource().getDocument(false);

				int off = NbDocument.findLineOffset(doc, line - 1) + position;

				startPosition = off;
				endPosition = off + 1;
			}

			parserResult.addError(
				message,
				startPosition,
				endPosition
			);
		}
	}

	@Override
	public synchronized void parse(Snapshot snapshot, Task task, SourceModificationEvent event) throws ParseException
	{
		//log.debug("parse", snapshot.getSource().getFileObject().getName());

		log.start("DLSyntaxParser.parse");

		parserResult = new DLParserResult(snapshot);
		final FileObject fileObject = snapshot.getSource().getFileObject();

		try {

			BaseDLCore core = new BaseDLCore(true);
			DefaultCore.loadResolvers(core);
			FileCoreResolver.setLocalPathInCore(core, Path.of(parserResult.getSnapshot().getSource().getFileObject().getPath()).getParent());
			String dlContent = String.valueOf(snapshot.getText());
			String moduleId = Path.of(fileObject.getPath()).toAbsolutePath().normalize().toString();

			parseContent(parserResult, moduleId, dlContent, core);

		} // Special handling for reserved keyword - this might to be changed in DL parsing as this induces issues -> Should add errors but not throw
		catch (ReservedKeyword ex) {

			StyledDocument doc = (StyledDocument) parserResult.getSnapshot().getSource().getDocument(false);
			int off = NbDocument.findLineOffset(doc, ex.getLine() - 1) + ex.getPosition();

			int startPosition = off;
			int endPosition = off + 1;

			parserResult.addError(new DLParsingError(
				fileObject,
				ex.getMessage(),
				ex.getMessage(),
				startPosition,
				endPosition
			));
		} // Handle an unknown error
		catch (RuntimeException ex) {

			log.error(ex);

			int startPosition = 0;
			int endPosition = 0;

			parserResult.addError(new DLParsingError(
				fileObject,
				ex.getMessage(),
				ex.getMessage(),
				startPosition,
				endPosition
			));
		}

		log.stopDebug("DLSyntaxParser.parse");
	}

	@Override
	public synchronized Result getResult(Task task) throws ParseException
	{
		//log.debug("getResult", parserResult.getSnapshot().getSource().getFileObject().getName());

		return parserResult;
	}

	@Override
	public void addChangeListener(ChangeListener cl)
	{
		// do nothing
	}

	@Override
	public void removeChangeListener(ChangeListener cl)
	{
		// do nothing
	}
}
