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
	
	private List scheduledJobs = Collections.synchronizedList(new LinkedList());
	private List finishedJobs = Collections.synchronizedList(new LinkedList());
	
	//private EventHandlingThread eventThread = null; // maybe not needed
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
	
	
	public void addFinishedJob(Job j)
	{
		finishedJobs.add(j);
	}
	
	
	public FBInstance selectFBInstanceToHandleEvent()
	{
		//System.out.print("Scheduler.selectFBInstanceToHandleEvent(): ");
		// TODO: implement selection algorithm
		if(resource.fbInstance.isHandlingEvent())
		{
			
			//System.out.println("all FB instances are busy handling an event!");
			return null;
		} 
		
		System.out.println("Scheduler.selectFBInstanceToHandleEvent(): found FB idle instance!");
		return resource.fbInstance;
	}
	
	
	public void notifyFinished()
	{
		
		synchronized(finishedJobs)
		{
			//System.out.println("Scheduler.notifyFinished()");
			for (Iterator iter = finishedJobs.iterator(); iter.hasNext();)
			{
				Job finishedJob = (Job) iter.next();
				finishedJob.getInstance().finishedJob(finishedJob);
				System.out.println("Scheduler.notifyFinished(): notified one");
				iter.remove();
			}
		}
	}
	
	
	public void runEvents()
	{
		System.out.println("Scheduler.runEvents()");
		while (true)
		{
			BasicFBInstance selectedFBInstance = (BasicFBInstance) selectFBInstanceToHandleEvent();
			if(selectedFBInstance == null)
			{
				//System.out.println("Scheduler.runEvents(): no instances to shedule");
				//try{ Thread.sleep(1); } catch(Exception e) {}
			}
			else
			{
				selectedFBInstance.handleEvent();
			}
			notifyFinished();
			//resource.handleConfigurationRequests();


		}
	}
    
	public void scheduleJob(Job j)
	{
		System.out.println("Scheduler.scheduleJob()");
		scheduledJobs.add(j);
	}
    
}
