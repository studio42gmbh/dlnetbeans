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
package de.s42.dl.netbeans.syntax;

import static de.s42.dl.netbeans.syntax.DLTokenId.*;
import static de.s42.dl.parser.DLLexer.*;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import static org.antlr.v4.runtime.Token.EOF;
import org.antlr.v4.runtime.misc.IntegerStack;
import org.netbeans.api.lexer.Token;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;
import org.netbeans.spi.lexer.TokenFactory;

/**
 *
 * @author Benjamin Schiller
 */
public class DLLexer implements Lexer<DLTokenId>
{

	private final static Logger log = LogManager.getLogger(DLLexer.class.getName());

	private final TokenFactory<DLTokenId> tokenFactory;
	private final de.s42.dl.parser.DLLexer lexer;
	private final DLLexerInputCharStream input;

	public DLLexer(LexerRestartInfo<DLTokenId> info)
	{
		this.tokenFactory = info.tokenFactory();
		this.input = new DLLexerInputCharStream(info.input());
		try {
			this.lexer = new de.s42.dl.parser.DLLexer(input);
			this.lexer.removeErrorListeners();
			if (info.state() != null) {
				((LexerState) info.state()).restore(lexer);
			}
			input.markToken();
		} catch (Throwable ex) {
			log.error(ex);
			throw ex;
		}
	}

	@Override
	public Token<DLTokenId> nextToken()
	{
		org.antlr.v4.runtime.Token nextToken = lexer.nextToken();

		int tokenType = nextToken.getType();

		switch (tokenType) {
			case EOF:
				return null;
			case WHITESPACES:
			case NEWLINE:
				return token(WHITESPACE);
			case AT:
			case COLON:
			case SEMI_COLON:
			case SCOPE_OPEN:
			case SCOPE_CLOSE:
			case PARENTHESES_OPEN:
			case PARENTHESES_CLOSE:
			case GENERIC_OPEN:
			case GENERIC_CLOSE:
			case COMMA:
			case EQUALS:
			case XOR:
			case LIKE:
			case NOT:
			case AND:
			case OR:
			case PLUS:
			case MINUS:
			case MUL:
			case DIV:
			case POW:
				return token(OPERATOR);
			case KEYWORD_ANNOTATION:
			case KEYWORD_ABSTRACT:
			case KEYWORD_ALIAS:
			case KEYWORD_ASSERT:
			case KEYWORD_CONTAINS:
			case KEYWORD_DECLARE:
			case KEYWORD_ENUM:
			case KEYWORD_EXTENDS:
			case KEYWORD_EXTERN:
			case KEYWORD_FINAL:
			case KEYWORD_PRAGMA:
			case KEYWORD_REQUIRE:
			case KEYWORD_TYPE:
			case KEYWORD_DYNAMIC:
			case BOOLEAN_LITERAL:
				return token(KEYWORD);
			case RESERVED_KEYWORD:
				return token(ERROR);
			case STRING_LITERAL:
				return token(STRING);
			case FLOAT_LITERAL:
			case INTEGER_LITERAL:
				return token(NUMBER);
			case REF:
				return token(REFERENCE);
			case SYMBOL:
				return token(IDENTIFIER);
			case MULTILINE_COMMENT:
			case SINGLELINE_COMMENT:
				return token(COMMENT);
			case UNKNOWN:
			default:
				return token(ERROR);
		}
	}

	@Override
	public Object state()
	{
		return new LexerState(lexer);
	}

	@Override
	public void release()
	{
	}

	private Token<DLTokenId> token(DLTokenId id)
	{
		input.markToken();
		return tokenFactory.createToken(id);
	}

	private static class LexerState
	{

		final int state;
		final int mode;
		final IntegerStack modes;

		LexerState(de.s42.dl.parser.DLLexer lexer)
		{
			this.state = lexer.getState();

			this.mode = lexer._mode;
			this.modes = new IntegerStack(lexer._modeStack);
		}

		public void restore(de.s42.dl.parser.DLLexer lexer)
		{
			lexer.setState(state);
			lexer._modeStack.addAll(modes);
			lexer._mode = mode;
		}

		@Override
		public String toString()
		{
			return String.valueOf(state);
		}
	}
}
