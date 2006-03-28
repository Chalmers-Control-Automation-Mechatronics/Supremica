
/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */
package org.supremica.log;

import org.apache.log4j.*;
import org.supremica.properties.Config;
import java.io.StringWriter;
import java.io.PrintWriter;

public final class SupremicaCategory
	implements Logger
{
	private final Category category;

	public SupremicaCategory(Category theCategory)
	{
		category = theCategory;
	}

	public void debug(Object message)
	{
		category.debug(message);
	}

	public void debug(Object message, Throwable t)
	{
		category.debug(message, t);
	}

	/**
	 * Print the stack trace to the registered listeners.
	 */
	public void debug(StackTraceElement[] trace)
	{
		for (int i = 0; i < trace.length; ++i)
		{
			category.debug(trace[i].toString());
		}
	}

	public void error(Object message)
	{
		category.error(message);
	}

	public void error(Object message, Throwable t)
	{
		category.error(message + "\n" + t.toString());
		category.debug(getStackTraceAsString(t));
	}

	public void error(Throwable t)
	{
		category.error(t.toString());
		category.debug(t.getStackTrace());
	}

	/**
	 * Print the stack trace to the registered listeners.
	 */
	public void error(StackTraceElement[] trace)
	{
		for (int i = 0; i < trace.length; ++i)
		{
			category.error(trace[i].toString());
		}
	}

	public void fatal(Object message)
	{
		category.fatal(message);
	}

	public void fatal(Object message, Throwable t)
	{
		category.fatal(message, t);
	}

	/**
	 * Print the stack trace to the registered listeners.
	 */
	public void fatal(StackTraceElement[] trace)
	{
		for (int i = 0; i < trace.length; ++i)
		{
			category.fatal(trace[i].toString());
		}
	}

	public void warn(Object message)
	{
		category.warn(message);
	}

	public void warn(Object message, Throwable t)
	{
		category.warn(message, t);
	}

	/**
	 * Print the stack trace to the registered listeners.
	 */
	public void warn(StackTraceElement[] trace)
	{
		for (int i = 0; i < trace.length; ++i)
		{
			category.warn(trace[i].toString());
		}
	}

	public void info(Object message)
	{
		category.info(message);
	}

	public void info(Object message, Throwable t)
	{
		category.info(message, t);
	}

	/**
	 * Logs the message as an "info"-message only if Supremica is currently in "verbose mode".
	 */
	public void verbose(Object message)
	{
		if (Config.VERBOSE_MODE.isTrue())
		{
			info(message);
		}
	}

	/**
	 * Print the stack trace to the registered listeners.
	 */
	public void info(StackTraceElement[] trace)
	{
		for (int i = 0; i < trace.length; ++i)
		{
			category.info(trace[i].toString());
		}
	}

	public boolean isDebugEnabled()
	{
		return category.isDebugEnabled();
	}

	public void setLogToConsole(boolean log)
	{
		if (log)
		{
			if (!LoggerFactory.hasConsoleAppender())
			{
				category.addAppender(LoggerFactory.getConsoleAppender());
			}
		}
	}

	private String getStackTraceAsString(Throwable t)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter printWriter = new PrintWriter(stringWriter);
		t.printStackTrace(printWriter);
		StringBuffer error = stringWriter.getBuffer();
		return error.toString();
	}

}
