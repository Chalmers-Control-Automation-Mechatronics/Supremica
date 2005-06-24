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

package org.supremica.functionblocks.model.services;

import org.supremica.functionblocks.model.FBInstance;

public class DelayThread extends Thread
{

	private int delay;
	private boolean sendOutput = true;
	private FBInstance fbInstance;
	
	private boolean serviceActive = true;


	private DelayThread() {} 

	public DelayThread(FBInstance fb)
	{
		setName("DelayThread");
		fbInstance = fb;
	}

	public synchronized void setDelayTime(int d)
	{
		//System.out.println("DelayThread: Setting delay time to: " + d + " ms");
		delay = d;
	}

	public synchronized void startDelay()
	{
		//System.out.println("DelayThread: startDelay()");		
		sendOutput = true;
		notify();
	}

	public synchronized void stopDelay()
	{
		//System.out.println("DelayThread: stopDelay()");		
		sendOutput = false;
		notify();
	}

	public synchronized void deactivateService()
	{
		//System.out.println("DelayThread: deactivateService()");		
		serviceActive = false;
		notify();
		notify();
	}

	public synchronized void run()
	{
		while (serviceActive)
		{

			try
			{
				//System.out.println("DelayThread: Calling wait()");
				wait();
				//System.out.println("DelayThread: Calling wait(" + delay + ")");
				wait(delay);
			}
			catch(InterruptedException e)
			{
				System.err.println("DelayThread: Interrupted Exception");
				e.printStackTrace(System.err);
			}

			// send output only if the delay wasn't stoped
			if (sendOutput)
			{
				//System.out.println("DelayThread: sending EO event");
				fbInstance.sendEvent("EO");
			}
		}
		
		//System.out.println("DelayThread: exiting run()");

	}   	
}
