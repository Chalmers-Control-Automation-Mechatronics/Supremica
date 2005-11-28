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

/*
 * Created on Dec 14, 2004
 */
/**
 * @author cengic
 */
package org.supremica.functionblocks.model;

import java.util.*;

public class Scheduler
{
	private Resource resource;

	private List scheduledFBInstances = Collections.synchronizedList(new LinkedList());

	//private List scheduledJobs = new LinkedList();

	private AlgorithmExecutingThread algorithmThread = null;


	private Scheduler() {}


	public Scheduler(Resource res)
	{
		System.out.println("Scheduler(" + res.getName()  + ")");
		resource = res;
		algorithmThread = new AlgorithmExecutingThread(this);
	}


	//public synchronized int getNumberOfScheduledJobs()
	//{
	//	return scheduledJobs.size();
	//}


	//public synchronized Job getNextScheduledJob()
	//{
	//	return (Job) scheduledJobs.remove(0);
	//}


	public synchronized FBInstance getNextScheduledFBInstance()
	{
		while(scheduledFBInstances.size() == 0)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
				System.err.println("Scheduler.getNextScheduledFBInstance(): InterruptedException");
			}
		}

		//System.out.println("Scheduler.getNextScheduledFBInstance(): Getting first instance from the queue ...");
		//for(Iterator iter = scheduledFBInstances.iterator(); iter.hasNext();)
		//{
		//	System.out.println("    scheduledFBInstances: " + ((FBInstance) iter.next()).toString());
		//}

		return (FBInstance) scheduledFBInstances.remove(0);
	}


	public void runEvents()
	{
		System.out.println("Scheduler.runEvents()");
		System.out.println("===================================================================================");
		// find all E_RESTART COLD connections and queue events on them
		for (Iterator iter = resource.getFBType("E_RESTART").instanceIterator(); iter.hasNext();)
		{
			FBInstance eRestartInstance = (FBInstance) iter.next();
			eRestartInstance.sendEvent("COLD");
		}

		while (true)
		{
			getNextScheduledFBInstance().handleEvent();
			//resource.handleConfigurationRequests();
		}
	}

 	public void scheduleJob(Job j)
 	{
		algorithmThread.scheduleJob(j);
 	}

	//public synchronized void scheduleJob(Job j)
	//{
	//	scheduledJobs.add(j);
	//	algorithmThread.notifyNewJob();
	//}

	public synchronized void scheduleFBInstance(FBInstance fbInst)
	{
		if (fbInst instanceof ServiceFBInstance)
		{
			scheduledFBInstances.add(0,fbInst);
		}
		else
		{
			scheduledFBInstances.add(fbInst);
		}
		//System.out.println("Scheduler.scheduleFBInstance("+fbInst.getName()+"): Scheduled instance. Now the queue is ...");
		//for(Iterator iter = scheduledFBInstances.iterator(); iter.hasNext();)
		//{
		//	System.out.println("    scheduledFBInstances: " + ((FBInstance) iter.next()).toString());
		//}
		notifyAll();

	}
}
