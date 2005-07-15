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
/**
 * @author Goran Cengic
 */

package org.supremica.functionblocks.model.services;

import org.supremica.functionblocks.model.*;
import org.supremica.softplc.Drivers.AdlinkPCI7432;

public class AdlinkIOThread extends Thread
{

	private AdlinkPCI7432 driver;
	private boolean read;
	private boolean[] readValues = new boolean[32];
	private	boolean[] writeValues = new boolean[32];
	private boolean serviceActive = true;
	private ServiceFBInstance serviceFB;
	private Variables serviceFBVariables;

	//========================================================================
	private AdlinkIOThread() {}

	public AdlinkIOThread(ServiceFBInstance fb,Variables vars)
	{
		setName("AdlinkIOThread");
		serviceFB = fb;
		serviceFBVariables = vars;

		try
		{
			//driver = new AdlinkPCI7432();
		}
		catch(Exception e)
		{
			System.err.println("AdlinkIOThread: Cought exception trying to start Adlink PCI7432 driver!");
			e.printStackTrace(System.err);
		}
	}
	//========================================================================


	public synchronized void readInputs()
	{
		read = true;
		notify();
	}

	public synchronized void writeOutputs()
	{
		read = false;
		notify();
	}


	public synchronized void deactivateService()
	{
		//System.out.println("AdlinkIOThread: deactivateService()");
		serviceActive = false;
		notify();
	}

	public synchronized void run()
	{
		while (serviceActive)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
				System.err.println("AdlinkIOThread: Interrupted Exception");
				e.printStackTrace(System.err);
			}

			if(read)
			{
				// read from the Digital I/O card.
				try
				{
					System.out.println("AdlinkIOThread.run(): Reading inputs.");
					//driver.getSignalArray(readValues);
				}
				catch(Exception e)
				{
					System.err.println("AdlinkIOThread: Cought exception trying to read to Adlink PCI7432 card!");
					e.printStackTrace(System.err);
				}



				// set data output vars to read values
				for (int i=0; i<32; i++)
				{
					//System.out.println("AdlinkIOThread.run(): Setting var INPUT" + i + " to " + readValues[i]);
					((BooleanVariable) serviceFBVariables.getVariable("INPUT" + i)).setValue(new Boolean(readValues[i]));
				}

				// send output event
				serviceFB.sendEvent("VALUES");
			}
			else
			{
				// read data input vars to write
				for (int i=0; i<32; i++)
				{
					writeValues[i] = ((BooleanVariable) serviceFBVariables.getVariable("OUTPUT" + i)).getValue().booleanValue();
					//System.out.println("AdlinkIOThread.run(): Writing "+writeValues[i]+" to OUTPUT" + i);
				}

				// write to the Digital I/O card.
				try
				{
					System.out.println("AdlinkIOThread.run(): Writing outputs.");
					//driver.setSignalArray(writeValues);
				}
				catch(Exception e)
				{
					System.err.println("AdlinkIOThread: Cought exception trying to write to Adlink PCI7432 card!");
					e.printStackTrace(System.err);
				}

				// send output event
				serviceFB.sendEvent("CNF");
			}
		}
	}
}
