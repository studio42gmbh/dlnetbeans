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
import de.s42.dl.DLCore;
import de.s42.dl.core.BaseDLCore;
import de.s42.dl.core.resolvers.FileCoreResolver;
import de.s42.dl.core.resolvers.LibraryCoreResolver;
import de.s42.dl.exceptions.InvalidModule;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.semantic.model.Type;
import de.s42.dl.netbeans.syntax.DLParserResult;
import de.s42.dl.netbeans.syntax.DLSyntaxParser;
import de.s42.dl.parser.DLParser;
import de.s42.dl.parser.DLParser.AliasNameContext;
import de.s42.dl.parser.DLParser.ContainsTypeNameContext;
import de.s42.dl.parser.DLParser.GenericParameterContext;
import de.s42.dl.parser.DLParser.ParentTypeNameContext;
import de.s42.dl.parser.DLParser.TypeHeaderContext;
import de.s42.dl.parser.DLParser.TypeIdentifierContext;
import de.s42.dl.parser.DLParserBaseListener;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import org.antlr.v4.runtime.ParserRuleContext;
import org.netbeans.api.editor.mimelookup.MimeLookup;

/**
 *
 * @author Benjamin Schiller
 */
public class DLSemanticParser extends DLParserBaseListener
{

	private final static Logger log = LogManager.getLogger(DLSemanticParser.class.getName());

	protected final static DLSemanticCache CACHE = MimeLookup.getLookup(DL_MIME_TYPE).lookup(DLSemanticCache.class);

	protected final DLParserResult parserResult;
	protected final String cacheKey;
	protected final ParserRuleContext overrideContext;

	public DLSemanticParser(DLParserResult parserResult, ParserRuleContext overrideContext)
	{
		assert parserResult != null;

		this.parserResult = parserResult;
		this.overrideContext = overrideContext;

		cacheKey = DLSemanticCache.getCacheKey(parserResult);
	}

	protected ParserRuleContext getOverridableContext(ParserRuleContext ctx)
	{
		return (overrideContext != null) ? overrideContext : ctx;
	}

	protected void validateTypeNameContext(ParserRuleContext ctx)
	{
		assert ctx != null;

		String typeName = ctx.getText();

		// Error: Type must be defined in this context
		if (!CACHE.hasType(cacheKey, typeName)) {
			parserResult.addError("Type " + typeName + " is not defined", ctx);
		}
	}

	protected Type addTypeDefinition(ParserRuleContext context, boolean warnOnLowerCase, Type aliasOf)
	{
		assert cacheKey != null;
		assert context != null;

		String typeName = context.getText();

		// Error: Dont allow double definitions
		if (CACHE.hasType(cacheKey, typeName)) {
			parserResult.addError("Type " + typeName + " is already defined", context);
		}

		Type type = new Type(typeName, getOverridableContext(context), aliasOf);

		CACHE.addType(cacheKey, type);

		// Warning: Types simple name should start with an uppercase letter
		if (warnOnLowerCase && StringHelper.isLowerCaseFirst(type.getSimpleName())) {
			parserResult.addWarning("Type " + typeName + " start with a lowercase letter but types should always start with an uppercase letter", context);
		}

		return type;
	}

	@Override
	public void exitTypeHeader(TypeHeaderContext ctx)
	{
		assert ctx != null;

		if (ctx.typeDefinitionName() == null) {
			return;
		}

		String typeName = ctx.typeDefinitionName().getText();

		Type type = addTypeDefinition(ctx.typeDefinitionName(), true, null);

		// Add alias typenames -> dont warn if they start with lowercase
		if (ctx.aliases() != null) {
			for (AliasNameContext aliasCtx : ctx.aliases().aliasName()) {
				addTypeDefinition(aliasCtx, false, type);
			}
		}
	}

	@Override
	public void exitGenericParameter(GenericParameterContext ctx)
	{
		validateTypeNameContext(ctx);
	}

	@Override
	public void exitContainsTypeName(ContainsTypeNameContext ctx)
	{
		validateTypeNameContext(ctx);
	}

	@Override
	public void exitParentTypeName(ParentTypeNameContext ctx)
	{
		validateTypeNameContext(ctx);
	}

	@Override
	public void exitTypeIdentifier(TypeIdentifierContext ctx)
	{
		validateTypeNameContext(ctx);
	}

	@Override
	public void exitRequire(DLParser.RequireContext ctx)
	{
		assert ctx != null;

		// Retrieve the module id
		String moduleId = ctx.requireModule().getText();

		// This core is created to have an anchor for the resolvers
		DLCore core = new BaseDLCore();
		LibraryCoreResolver libResolver = new LibraryCoreResolver(core);

		// @todo Add resolvment for file resolver
		FileCoreResolver fileResolver = new FileCoreResolver(core);

		// @todo Is there a more generic way to make sure the according resolvers are used?
		// Try to resolve module with the library resolver
		if (libResolver.canParse(moduleId)) {

			try {
				DLParserResult result = new DLParserResult(parserResult.getSnapshot());

				// @todo merge the results with according prefix into this result
				DLSyntaxParser.parseContent(result, libResolver.getContent(moduleId), getOverridableContext(ctx));
			} catch (InvalidModule | IOException ex) {
				parserResult.addError("Library module " + ctx.requireModule().getText() + " could not get parsed - " + ex.getMessage(), ctx);
				log.error(ex);
			}
		} else if (fileResolver.canParse(moduleId)) {

			try {
				DLParserResult result = new DLParserResult(parserResult.getSnapshot());

				// @todo merge the results with according prefix into this result
				DLSyntaxParser.parseContent(result, fileResolver.getContent(moduleId), getOverridableContext(ctx));
			} catch (InvalidModule | IOException ex) {
				parserResult.addError("File module " + ctx.requireModule().getText() + " could not get parsed - " + ex.getMessage(), ctx);
				log.error(ex);
			}
		} else {
			parserResult.addError("Resolver module " + ctx.requireModule().getText() + " could not get resolved", ctx);
		}
	}
}
