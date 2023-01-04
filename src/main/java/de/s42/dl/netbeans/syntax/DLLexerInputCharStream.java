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

import de.s42.log.LogManager;
import de.s42.log.Logger;
import org.antlr.v4.runtime.CharStream;
import static org.antlr.v4.runtime.IntStream.UNKNOWN_SOURCE_NAME;
import org.antlr.v4.runtime.misc.Interval;
import org.netbeans.spi.lexer.LexerInput;

/**
 *
 * @author Benjamin Schiller
 */
public class DLLexerInputCharStream implements CharStream
{

	private final static Logger log = LogManager.getLogger(DLLexerInputCharStream.class.getName());

	private final LexerInput input;
	private int tokenMark = Integer.MAX_VALUE;
	private int index = 0;

	public DLLexerInputCharStream(LexerInput input)
	{
		this.input = input;
	}

	@Override
	public String getText(Interval intrvl)
	{
		if (intrvl.a < tokenMark) {
			throw new UnsupportedOperationException("Can't read before the last token end: " + tokenMark);
		}
		int start = intrvl.a - tokenMark;
		int end = intrvl.b - tokenMark + 1;
		int toread = end - start - input.readLength();
		for (int i = 0; i < toread; i++) {
			input.read();
		}

		String ret = String.valueOf(input.readText(0, end - start));

		if (toread > 0) {
			input.backup(toread);
		}
		return ret;
	}

	@Override
	public void consume()
	{
		read();
	}

	@Override
	public int LA(int count)
	{
		if (count == 0) {
			throw new UnsupportedOperationException("Can't LA with 0");
		}

		int c = 0;
		if (count > 0) {
			for (int i = 0; i < count; i++) {
				c = read();
			}
			backup(count);
		} else {
			backup(count);
			c = read();
		}
		return c;
	}

	@Override
	public int mark()
	{
		return -1;
	}

	public void markToken()
	{
		tokenMark = index;
	}

	@Override
	public void release(int marker)
	{
		// Nothing todo for now
	}

	@Override
	public int index()
	{
		return index;
	}

	@Override
	public void seek(int i)
	{
		if (i < index()) {
			backup(index() - i);
		} else {
			while (index() < i) {
				if (read() == LexerInput.EOF) {
					break;
				}
			}
		}
	}

	private int read()
	{
		int ret = input.read();
		index += 1;
		return ret;
	}

	private void backup(int count)
	{
		index -= count;
		input.backup(count);
	}

	@Override
	public int size()
	{
		throw new UnsupportedOperationException("Stream size is unknown.");
	}

	@Override
	public String getSourceName()
	{
		return UNKNOWN_SOURCE_NAME;
	}
}
