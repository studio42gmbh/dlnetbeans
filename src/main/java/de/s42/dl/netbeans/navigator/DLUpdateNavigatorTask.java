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
package de.s42.dl.netbeans.navigator;

import de.s42.dl.netbeans.syntax.*;
import static de.s42.dl.netbeans.DLDataObject.DL_MIME_TYPE;
import java.util.Collection;
import java.util.Collections;
import org.netbeans.api.editor.mimelookup.MimeRegistration;
import org.netbeans.modules.parsing.api.Snapshot;
import org.netbeans.modules.parsing.spi.ParserResultTask;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.SchedulerTask;
import org.netbeans.modules.parsing.spi.TaskFactory;

/**
 *
 * @author Benjamin Schiller
 */
public class DLUpdateNavigatorTask extends ParserResultTask<DLParserResult>
{

	protected final DLNNavigatorPanelComponent navigatorPanel;

	public DLUpdateNavigatorTask()
	{
		navigatorPanel = DLNavigatorPanel.getGlobalComponent();

		assert navigatorPanel != null;
	}

	@Override
	public void run(DLParserResult result, SchedulerEvent event)
	{
		assert result != null;

		navigatorPanel.setParserResult(result);
	}

	@Override
	public int getPriority()
	{
		return Integer.MAX_VALUE;
	}

	@Override
	public Class<? extends Scheduler> getSchedulerClass()
	{
		return Scheduler.EDITOR_SENSITIVE_TASK_SCHEDULER;
	}

	@Override
	public void cancel()
	{
	}

	@MimeRegistration(mimeType = DL_MIME_TYPE, service = TaskFactory.class)
	public static class Factory extends TaskFactory
	{

		@Override
		public Collection<? extends SchedulerTask> create(Snapshot snapshot)
		{
			return Collections.singleton(new DLUpdateNavigatorTask());
		}

	}
}
