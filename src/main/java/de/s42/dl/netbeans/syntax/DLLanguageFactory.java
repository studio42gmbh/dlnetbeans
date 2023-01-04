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

import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import java.util.Collection;
import java.util.EnumSet;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.api.lexer.Language;
import org.netbeans.spi.lexer.LanguageHierarchy;
import org.netbeans.spi.lexer.Lexer;
import org.netbeans.spi.lexer.LexerRestartInfo;

/**
 *
 * @author Benjamin Schiller
 */
public class DLLanguageFactory
{

	@MimeRegistration(mimeType = DL_MIME_TYPE, service = Language.class)
	public static final Language<?> getLanguage()
	{
		return LANGUAGE;
	}

	private static final Language<DLTokenId> LANGUAGE = new LanguageHierarchy<DLTokenId>()
	{
		@Override
		protected Collection<DLTokenId> createTokenIds()
		{
			return EnumSet.allOf(DLTokenId.class);
		}

		@Override
		protected Lexer<DLTokenId> createLexer(LexerRestartInfo<DLTokenId> info)
		{
			return new DLLexer(info);
		}

		@Override
		protected String mimeType()
		{
			return DL_MIME_TYPE;
		}
	}.language();
}
