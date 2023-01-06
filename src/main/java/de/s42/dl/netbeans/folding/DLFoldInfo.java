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
package de.s42.dl.netbeans.folding;

import java.util.Objects;
import org.netbeans.spi.editor.fold.FoldInfo;

/**
 *
 * @author Benjamin Schiller
 */
public class DLFoldInfo
{

	protected final FoldInfo foldInfo;

	public DLFoldInfo(FoldInfo foldInfo)
	{
		assert foldInfo != null;

		this.foldInfo = foldInfo;		
	}

	// <editor-fold desc="Getters/Setters" defaultstate="collapsed">
	public FoldInfo getFoldInfo()
	{
		return foldInfo;
	}
	//</editor-fold>

	@Override
	public int hashCode()
	{
		int hash = 7;
		hash = 41 * hash + Objects.hashCode(foldInfo.getType());
		hash = 41 * hash + foldInfo.getStart();
		hash = 41 * hash + foldInfo.getEnd();
		return hash;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final DLFoldInfo other = (DLFoldInfo) obj;

		if (!Objects.equals(this.foldInfo.getType(), other.foldInfo.getType())) {
			return false;
		}
		if (this.foldInfo.getStart() != other.foldInfo.getStart()) {
			return false;
		}
		if (this.foldInfo.getEnd() != other.foldInfo.getEnd()) {
			return false;
		}

		return true;
	}
}
