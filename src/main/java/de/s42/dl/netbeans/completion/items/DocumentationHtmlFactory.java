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
package de.s42.dl.netbeans.completion.items;

import de.s42.dl.language.DLKeyword;
import de.s42.dl.netbeans.semantic.model.EnumType;
import de.s42.dl.netbeans.semantic.model.Type;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Benjamin Schiller
 */
public class DocumentationHtmlFactory
{

	private final static Logger log = LogManager.getLogger(DocumentationHtmlFactory.class.getName());

	public static URL createDocumentationUrl(DLKeyword keyword)
	{
		try {
			return new URL("https://github.com/studio42gmbh/dl/wiki/1.2-Keywords#" + keyword.keyword);
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static URL createDocumentationUrl(Type type)
	{
		try {
			return new URL("https://github.com/studio42gmbh/dl/wiki/2-Types");
		} catch (MalformedURLException ex) {
			throw new RuntimeException(ex);
		}
	}

	public static String createRightTextHtml(DLKeyword keyword)
	{
		StringBuilder builder = new StringBuilder();

		builder.append("<i>Keyword</i>");

		return builder.toString();
	}

	public static String createRightTextHtml(Type type)
	{
		assert type != null;

		StringBuilder builder = new StringBuilder();

		String typeKindDisplay = (type instanceof EnumType) ? "Enum" : "Type";

		builder.append("<i>")
			.append(typeKindDisplay)
			.append("</i>");

		return builder.toString();
	}
	
	public static String createDocumentationHtml(DLKeyword keyword)
	{
		assert keyword != null;

		StringBuilder builder = new StringBuilder();

		builder
			.append("<h2>Keyword ")
			.append(keyword.keyword)
			.append("</h2>")
			.append("<p><a href='https://github.com/studio42gmbh/dl/wiki/1.2-Keywords#")
			.append(keyword.keyword)
			.append("'>Read more about keywords in DL</a>");

		return builder.toString();
	}
	
	public static String createDocumentationHtml(Type type)
	{
		assert type != null;

		StringBuilder builder = new StringBuilder();
		
		String typeKindDisplay = (type instanceof EnumType) ? "Enum" : "Type";

		builder
			.append("<h2>")
			.append(typeKindDisplay)
			.append(" ")
			.append(type.getIdentifier())
			.append("</h2>")
			.append("<p>Defined in module <b>")
			.append(type.getModuleId())
			.append("</b> (")
			.append(type.getOriginalLine())
			.append(":")
			.append(type.getOriginalPosition())
			.append(")</p>");
		
		// Append alias info
		if (type.getAliasOf() != null) {
			builder
				.append("<p>Is an alias of <b>")
				.append(type.getAliasOf().getIdentifier())
				.append("</b></p>");
		}
		
		builder
			.append("<p><a href='https://github.com/studio42gmbh/dl/wiki/2-Types'>Read more about types in DL</a>");


		return builder.toString();
	}
}
