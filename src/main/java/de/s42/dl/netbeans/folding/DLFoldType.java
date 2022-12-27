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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.editor.fold.FoldTemplate;
import org.netbeans.api.editor.fold.FoldType;

/**
 *
 * @author Benjamin Schiller
 */
public enum DLFoldType
{
	Scope(FoldType.create(
		"scope",
		"Scope",
		new FoldTemplate(
			1,
			1,
			"{...}"
		)
	));

	public final FoldType type;

	public final static List<FoldType> TYPES;

	static {

		// Init TYPES list iterating the given DLFoldTypes
		List<FoldType> types = new ArrayList<>();
		for (int i = 0; i < DLFoldType.values().length; ++i) {
			types.add(DLFoldType.values()[i].type);
		}
		TYPES = Collections.unmodifiableList(types);
	}

	private DLFoldType(FoldType type)
	{
		assert type != null;

		this.type = type;
	}

	public static List<FoldType> getFoldTypes()
	{
		return TYPES;
	}
}
