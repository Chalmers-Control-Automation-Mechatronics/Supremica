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

import bsh.Interpreter;


class AlgorithmExecutingThread extends Thread
{

	private Interpreter interpreter = new Interpreter();

    private Scheduler scheduler = null;

    private AlgorithmExecutingThread() {}

    public AlgorithmExecutingThread(String name, Scheduler s)
    {
		scheduler = s;
		setName(name);
		start();
    }

    public void run()
    {
		while (true)
		{
			Job currentJob = scheduler.getNextScheduledJob();
			BasicFBInstance currentFBInstance = currentJob.getInstance();

			//System.out.println("AlgorithmExecutingThread.run(): Executing " + currentJob.getAlgorithm().getName() + " with text:");
			//System.out.println(currentJob.getAlgorithm().toString());

			currentFBInstance.eventTime = (System.nanoTime() - currentFBInstance.eventTime)/1000000;
			currentFBInstance.algorithmTime = System.nanoTime();

			//-----------------------------------------------------------------
			currentJob.getAlgorithm().execute(currentJob.getVariables(),interpreter);
			currentJob.getInstance().finishedJob(currentJob);
			//-----------------------------------------------------------------

			currentFBInstance.algorithmTime = (System.nanoTime() - currentFBInstance.algorithmTime)/1000000;
			currentFBInstance.finishTime = (System.nanoTime()-Scheduler.startTime)/1000000;
			currentFBInstance.totalTime = currentFBInstance.eventTime + currentFBInstance.algorithmTime;

			BasicFBInstance.allTime = BasicFBInstance.allTime + currentFBInstance.totalTime;
			BasicFBInstance.allEventTime = BasicFBInstance.allEventTime + currentFBInstance.eventTime;
			BasicFBInstance.count++;

			//System.out.println("Block times for instance " + currentFBInstance.getName() + " :" );
			//System.out.println("\t t_CON = " + currentFBInstance.eventTime + " ms" );
			//System.out.println("\t t_ALG = " + currentFBInstance.algorithmTime + " ms" );
			//System.out.println("\t t_TOT = " + currentFBInstance.totalTime + " ms" );
			//System.out.println("\t t_FIN = " + currentFBInstance.finishTime + " ms" );
		}
    }   
}
