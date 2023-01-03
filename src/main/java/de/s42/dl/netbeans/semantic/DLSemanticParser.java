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

import de.s42.dl.netbeans.semantic.cache.DLSemanticCache;
import de.s42.base.strings.StringHelper;
import de.s42.dl.core.BaseDLCore;
import de.s42.dl.core.DLCoreResolver;
import de.s42.dl.exceptions.InvalidModule;
import de.s42.dl.netbeans.semantic.cache.DLSemanticCacheNode;
import de.s42.dl.netbeans.semantic.model.*;
import de.s42.dl.netbeans.syntax.DLParserResult;
import de.s42.dl.netbeans.syntax.DLSyntaxParser;
import de.s42.dl.parser.DLHrfParsing;
import de.s42.dl.parser.DLParser.*;
import de.s42.dl.parser.DLParserBaseListener;
import de.s42.log.LogManager;
import de.s42.log.Logger;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.antlr.v4.runtime.ParserRuleContext;
import org.netbeans.api.editor.mimelookup.MimeLookup;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import de.s42.dl.netbeans.util.FileObjectHelper;

/**
 *
 * @author Benjamin Schiller
 */
public class DLSemanticParser extends DLParserBaseListener
{

	private final static Logger log = LogManager.getLogger(DLSemanticParser.class.getName());

	protected final static DLSemanticCache CACHE = MimeLookup.getLookup(DL_MIME_TYPE).lookup(DLSemanticCache.class);

	protected final DLParserResult parserResult;
	protected final BaseDLCore core;
	protected final String moduleId;
	protected final DLSemanticCacheNode cacheNode;

	public DLSemanticParser(DLParserResult parserResult, BaseDLCore core, String moduleId)
	{
		assert parserResult != null;
		assert core != null;
		assert moduleId != null;

		this.core = core;
		this.moduleId = moduleId;
		this.parserResult = parserResult;

		String cacheKey = DLSemanticCache.getCacheKey(moduleId);
		ModuleEntry module = new ModuleEntry(moduleId);

		// This cache node will be filled while not being in the cache directly -> after scannning the whole document it will be published
		cacheNode = CACHE.createCacheNode(cacheKey, module);
	}

	@Override
	public void enterData(DataContext ctx)
	{
		assert ctx != null;

		// Load a nb-project.dl if given
		FileObjectHelper.resolveAutoRequireDl(Path.of(moduleId))
			.ifPresent((path) -> {
				requireModule(path.toAbsolutePath().normalize().toString(), ctx);
			});
	}

	@Override
	public void exitData(DataContext ctx)
	{
		// After successful parsing -> Update cache node
		CACHE.setCacheNode(cacheNode);
	}

	@Override
	public void exitEnumDefinition(EnumDefinitionContext ctx)
	{
		assert ctx != null;

		if (ctx.enumName() == null) {
			return;
		}

		List<String> values = new ArrayList<>();
		if (ctx.enumBody() != null && ctx.enumBody().enumValueDefinition() != null) {
			for (EnumValueDefinitionContext evCtx : ctx.enumBody().enumValueDefinition()) {
				values.add(evCtx.getText());
			}
		}

		EnumType type = addEnumDefinition(ctx.enumName(), values, true, null);

		// Add alias typenames -> dont warn if they start with lowercase
		if (ctx.aliases() != null) {
			for (AliasNameContext aliasCtx : ctx.aliases().aliasName()) {
				addEnumDefinition(aliasCtx, values, false, type);
			}
		}
	}

	@Override
	public void exitTypeHeader(TypeHeaderContext ctx)
	{
		assert ctx != null;

		if (ctx.typeDefinitionName() == null) {
			return;
		}

		Type type = addTypeDefinition(ctx.typeDefinitionName(), true, null);

		// Ensure all parent types are defined and valid
		if (ctx.parentTypeName()!= null) {
			for (ParentTypeNameContext parentCtx : ctx.parentTypeName()) {
				
				// Check if a parent is
				if (parentCtx.getText().equals(ctx.typeDefinitionName().getText())) {
					parserResult.addError("Parent of type can not be the type itself", parentCtx);
				}
				
				validateTypeNameContext(parentCtx);
			}
		}
		
		// Ensure all contains types are defined
		if (ctx.containsTypeName() != null) {
			for (ContainsTypeNameContext containsCtx : ctx.containsTypeName()) {
				validateTypeNameContext(containsCtx);
			}
		}
		
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
	public void exitTypeIdentifier(TypeIdentifierContext ctx)
	{
		validateTypeNameContext(ctx);
	}

	@Override
	public void exitRequire(RequireContext ctx)
	{
		assert ctx != null;

		// Retrieve the module id and require
		requireModule(DLHrfParsing.getRequireModuleId(ctx.requireModuleId()), ctx);
	}
	
	protected void validateTypeNameContext(ParserRuleContext ctx)
	{
		assert ctx != null;

		String typeName = ctx.getText();

		// Error: Type must be defined in this context
		if (!cacheNode.hasType(typeName, ctx.getStart().getStartIndex(), true)) {
			parserResult.addWarning("Type " + typeName + " is not defined", ctx);
		}
	}

	protected Type addTypeDefinition(ParserRuleContext context, boolean warnOnLowerCase, Type aliasOf)
	{
		assert context != null;

		String typeName = context.getText();

		// Error: Dont allow double definitions
		if (cacheNode.hasType(typeName, context.getStart().getStartIndex(), true)) {
			parserResult.addError("Type " + typeName + " is already defined", context);
		}

		Type type = new Type(
			typeName,
			context,
			moduleId,
			aliasOf
		);

		cacheNode.addType(type);

		// Warning: Types simple name should start with an uppercase letter
		if (warnOnLowerCase && StringHelper.isLowerCaseFirst(type.getSimpleName())) {
			parserResult.addWarning("Type " + typeName + " start with a lowercase letter but types should always start with an uppercase letter", context);
		}

		return type;
	}

	protected EnumType addEnumDefinition(ParserRuleContext context, List<String> values, boolean warnOnLowerCase, EnumType aliasOf)
	{
		assert context != null;

		String enumName = context.getText();

		// Error: Dont allow double definitions
		if (cacheNode.hasType(enumName, context.getStart().getStartIndex(), true)) {
			parserResult.addError("Enum " + enumName + " is already defined", context);
		}

		EnumType enumType = new EnumType(
			enumName,
			values,
			context,
			moduleId,
			aliasOf
		);

		cacheNode.addType(enumType);

		// Warning: Types simple name should start with an uppercase letter
		if (warnOnLowerCase && StringHelper.isLowerCaseFirst(enumType.getSimpleName())) {
			parserResult.addWarning("Type " + enumName + " start with a lowercase letter but types should always start with an uppercase letter", context);
		}

		return enumType;
	}

	protected boolean loadModule(DLCoreResolver resolver, String moduleId, ParserRuleContext locationContext)
	{
		assert resolver != null;
		assert moduleId != null;
		assert locationContext != null;

		if (!resolver.canParse(core, moduleId, null)) {
			return false;
		}

		try {

			String resolvedModuleId = resolver.resolveModuleId(core, moduleId);

			// Just immediatly parse the required module if it has not been parsed already
			if (!CACHE.hasCacheNode(resolvedModuleId)) {

				String content = resolver.getContent(core, resolvedModuleId, null);
				DLParserResult result = new DLParserResult(parserResult.getSnapshot());

				DLSyntaxParser.parseContent(
					result,
					resolvedModuleId,
					content,
					core
				);
			}

			// Make sure inclusion from data context are put right at the start of this content
			if (locationContext instanceof DataContext) {
				cacheNode.addNodeReference(resolvedModuleId);
			} else {
				cacheNode.addNodeReference(resolvedModuleId, locationContext);
			}

			// @todo if the referenced cache node contains errors it might be nice to add them here to signal the user the ref has errors			
		} catch (InvalidModule | IOException ex) {
			parserResult.addError("Module " + moduleId + " could not get parsed - " + ex.getMessage(), locationContext);
			log.error(ex);
		}

		return true;
	}

	protected void requireModule(String requiredModuleId, ParserRuleContext locationContext)
	{
		assert requiredModuleId != null;
		assert locationContext != null;

		for (DLCoreResolver resolver : core.getResolvers()) {
			if (loadModule(resolver, requiredModuleId, locationContext)) {
				return;
			}
		}

		parserResult.addError("Module " + requiredModuleId + " could not get resolved", locationContext);
	}	
}
