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
	
	private List scheduledFBInstances = new LinkedList();
	private List scheduledJobs = Collections.synchronizedList(new LinkedList());
	//private List finishedJobs = Collections.synchronizedList(new LinkedList());
	
	private AlgorithmExecutingThread algorithmThread = null;
	
	
	private Scheduler() {}
	
	
	public Scheduler(Resource res)
	{
		System.out.println("Scheduler(" + res.getName()  + ")");
		resource = res;
		algorithmThread = new AlgorithmExecutingThread(this);
	}
	
	
	public int getNumberOfScheduledJobs()
	{
		return scheduledJobs.size();
	}
	
	
	public Job getNextScheduledJob()
	{
		return (Job) scheduledJobs.remove(0);
	}
	
	
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
				System.err.println("Scheduler: InterruptedException");
			}
		}
		return (FBInstance) scheduledFBInstances.remove(0);
	}
	

	public void runEvents()
	{
		System.out.println("Scheduler.runEvents()");
		while (true)
		{
			FBInstance selectedFBInstance = getNextScheduledFBInstance();
			if(selectedFBInstance != null)
			{
				selectedFBInstance.handleEvent();
			}
			//resource.handleConfigurationRequests();
		}
	}
    
	public void scheduleJob(Job j)
	{
		//System.out.println("Scheduler.scheduleJob()");
		scheduledJobs.add(j);
	}

	public synchronized void scheduleFBInstance(FBInstance fbInst)
	{
		scheduledFBInstances.add(fbInst);
		notify();
	}
   
}
