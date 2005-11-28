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
 * Created on Jan 11, 2005
 */
/**
 * @author Goran Cengic
 */
package org.supremica.functionblocks.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Collections;

class AlgorithmExecutingThread extends Thread
{

	private Scheduler scheduler = null;

	private AlgorithmExecutingThread() {}

	private List scheduledJobs = Collections.synchronizedList(new LinkedList());

	public AlgorithmExecutingThread(Scheduler s)
	{
		scheduler = s;
		setName("AlgorithmExecuting");
		start();
	}

	public void run()
	{
		while (true)
		{
			Job currentJob = getNextScheduledJob();
			//System.out.println("AlgorithmExecutingThread.run(): Executing " + currentJob.getAlgorithm().getName() + " with text:");
			//System.out.println(currentJob.getAlgorithm().toString());
			currentJob.getAlgorithm().execute(currentJob.getVariables());
			currentJob.getInstance().finishedJob(currentJob);
		}
	}

	public synchronized Job getNextScheduledJob()
	{
		while(scheduledJobs.size() == 0)
		{
			try
			{
				wait();
			}
			catch(InterruptedException e)
			{
				System.err.println("AlgorithmExecutingThread: InterruptedException");
				e.printStackTrace(System.err);
			}
		}
		return (Job) scheduledJobs.remove(0);
	}

	//public synchronized int getNumberOfScheduledJobs()
	//{
	//	return scheduledJobs.size();
	//}

	//public synchronized void notifyNewJob()
	//{
	//	notifyAll();
	//}

	public synchronized void scheduleJob(Job j)
	{
		scheduledJobs.add(j);
		notifyAll();
	}
}
