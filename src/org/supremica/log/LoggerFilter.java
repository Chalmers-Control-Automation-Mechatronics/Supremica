
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
import org.apache.log4j.spi.*;

public class LoggerFilter
	extends Filter
{
	private boolean allowInfos = true;
	private boolean allowWarns = true;
	private boolean allowDebugs = false;
	private boolean allowFatals = true;
	private boolean allowErrors = true;

	public LoggerFilter() {}

	public void setOption(String option, String value)
	{    // Not implemented
	}

	public String[] getOptionStrings()
	{
		return new String[0];
	}

	public int decide(LoggingEvent event)
	{
		Level prio = event.getLevel();

		if (prio == Level.DEBUG)
		{
			if (allowDebugs)
			{
				return Filter.ACCEPT;
			}

			return Filter.DENY;
		}

		if (prio == Level.INFO)
		{
			if (allowInfos)
			{
				return Filter.ACCEPT;
			}

			return Filter.DENY;
		}

		if (prio == Level.WARN)
		{
			if (allowWarns)
			{
				return Filter.ACCEPT;
			}

			return Filter.DENY;
		}

		if (prio == Level.ERROR)
		{
			if (allowErrors)
			{
				return Filter.ACCEPT;
			}

			return Filter.DENY;
		}

		if (prio == Level.FATAL)
		{
			if (allowFatals)
			{
				return Filter.ACCEPT;
			}

			return Filter.DENY;
		}

		return Filter.NEUTRAL;
	}

	public boolean allowInfo()
	{
		return allowInfos;
	}

	public void setAllowInfo(boolean allow)
	{
		this.allowInfos = allow;
	}

	public boolean allowDebug()
	{
		return allowDebugs;
	}

	public void setAllowDebug(boolean allow)
	{
		this.allowDebugs = allow;
	}

	public boolean allowWarn()
	{
		return allowWarns;
	}

	public void setAllowWarn(boolean allow)
	{
		this.allowWarns = allow;
	}

	public boolean allowError()
	{
		return allowErrors;
	}

	public void setAllowError(boolean allow)
	{
		this.allowErrors = allow;
	}

	public boolean allowFatal()
	{
		return allowFatals;
	}

	public void setAllowFatal(boolean allow)
	{
		this.allowFatals = allow;
	}
}
