
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
 * Haradsgatan 26A
 * 431 42 Molndal
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
package org.supremica.util;

import java.util.Date;

public class ActionTimer
{
	private final static String hours = " hours ";
	private final static String minutes = " minutes ";
	private final static String seconds = " seconds ";
	private final static String milliseconds = " milliseconds";
	private Date startDate = null;
	private Date stopDate = null;

	public ActionTimer() {}

	/**
	 * Starts the timer.
	 */
	public void start()
	{
		stopDate = null;
		startDate = new Date();
	}

	/**
	 * Stops the timer.
	 */
	public void stop()
	{
		stopDate = new Date();
	}

	/**
	 * Returns the elapsed time in milliseconds between last start and last stop call.
	 * If stop is not called before this method then the time since last call
	 * to start is returned */
	public long elapsedTime()
		throws IllegalStateException
	{
		if (startDate == null)
		{
			throw new IllegalStateException("startDate is negative");
		}

		if (stopDate == null)
		{
			return (new Date()).getTime() - startDate.getTime();
		}

		return stopDate.getTime() - startDate.getTime();
	}

	public String toString()
	{
		long time = elapsedTime();    // time is in millisecs
		int hrs = (int) (time / (60 * 60 * 1000.0));

		time = time - hrs * (60 * 60 * 1000);

		int mins = (int) (time / (60 * 1000.0));

		time = time - mins * (60 * 1000);

		int secs = (int) (time / (1000.0));

		time = time - secs * 1000;

		int millis = (int) time;
		StringBuffer sbuf = new StringBuffer();

		if (hrs != 0)
		{
			sbuf.append(hrs + hours + mins + minutes + secs + seconds + millis + milliseconds);
		}
		else if (mins != 0)
		{
			sbuf.append(mins + minutes + secs + seconds + millis + milliseconds);
		}
		else if (secs != 0)
		{
			sbuf.append(secs + seconds + millis + milliseconds);
		}
		else
		{
			sbuf.append(millis + milliseconds);
		}

		return sbuf.toString();
	}

	public void clear()
	{
		startDate = null;
		stopDate = null;
	}
}
