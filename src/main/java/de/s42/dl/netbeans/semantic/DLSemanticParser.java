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
package de.s42.dl.netbeans.semantic;

import de.s42.base.strings.StringHelper;
import de.s42.dl.netbeans.syntax.DLParserResult;
import de.s42.dl.parser.DLParser;
import de.s42.dl.parser.DLParserBaseListener;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Benjamin Schiller
 */
public class DLSemanticParser extends DLParserBaseListener
{

	private final static Logger log = LogManager.getLogger(DLSemanticParser.class.getName());

	protected final DLParserResult parserResult;
	
	protected final Set<String> typeNames = new HashSet<>();

	public DLSemanticParser(DLParserResult parserResult)
	{
		this.parserResult = parserResult;
	}

	@Override
	public void exitTypeDefinition(DLParser.TypeDefinitionContext ctx)
	{
		assert ctx != null;

		String typeName = ctx.typeDefinitionName().getText();
		
		// Error: Dont allow double definitions
		if (!typeNames.add(typeName)) {
			parserResult.addError("Type " + typeName + " is already defined",ctx.typeDefinitionName().getStart());
		}
		
		// Warning: Types should start with an uppercase letter
		if (StringHelper.isLowerCaseFirst(typeName)) {
			parserResult.addWarning("Type " + typeName + " start with a lowercase letter but types should always start with an uppercase letter",ctx.typeDefinitionName().getStart());
		}
	}

	@Override
	public void exitTypeIdentifier(DLParser.TypeIdentifierContext ctx)
	{
		assert ctx != null;
		
		String typeName = ctx.getText();

		// Error: Type must be defined in this context
		if (!typeNames.contains(typeName)) {
			parserResult.addError("Type " + typeName + " is not defined",ctx.getStart());
		}
	}
}
