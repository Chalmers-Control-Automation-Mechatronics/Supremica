/*
 * Supremica Software License Agreement
 * 
 * The Supremica software is not in the public domain However, it is freely
 * available without fee for education, research, and non-profit purposes. By
 * obtaining copies of this and other files that comprise the Supremica
 * software, you, the Licensee, agree to abide by the following conditions and
 * understandings with respect to the copyrighted software:
 * 
 * The software is copyrighted in the name of Supremica, and ownership of the
 * software remains with Supremica.
 * 
 * Permission to use, copy, and modify this software and its documentation for
 * education, research, and non-profit purposes is hereby granted to Licensee,
 * provided that the copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all such copies, and
 * that no charge be made for such copies. Any entity desiring permission to
 * incorporate this software into commercial products or to use it for
 * commercial purposes should contact:
 * 
 * Knut Akesson (KA), knut@supremica.org Supremica, Haradsgatan 26A 431 42
 * Molndal SWEDEN
 * 
 * to discuss license terms. No cost evaluation licenses are available.
 * 
 * Licensee may not use the name, logo, or any other symbol of Supremica nor the
 * names of any of its employees nor any adaptation thereof in advertising or
 * publicity pertaining to the software without specific prior written approval
 * of the Supremica.
 * 
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE SUITABILITY OF THE
 * SOFTWARE FOR ANY PURPOSE. IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED
 * WARRANTY.
 * 
 * Supremica or KA shall not be liable for any damages suffered by Licensee from
 * the use of this software.
 * 
 * Supremica is owned and represented by KA.
 */
/**
 * @author Cengic
 */
package org.supremica.functionblocks.model;


import java.util.*;

public class BasicFBInstance extends FBInstance
{
	// attributes
	private Resource resource;
	private BasicFBType fbType;

	// TODO: change to HashMap for eventName to eventQueue mapping
	private List eventInputQueues = new ArrayList();


	private ECState currentECState;
	private ECAction currentECAction;
	private Variables variables;

	private Event currentEvent;
	private boolean handlingEvent = false;

	private Map eventOutputConnections = new HashMap();
	private Map dataInputConnections = new HashMap();


	// contructor and methods for construction
	private BasicFBInstance() {}
	
	public BasicFBInstance(String n, Resource r, BasicFBType t)
	{
		this.name = n;
		fbType = t;
		resource = r;
		variables = new Variables();
	}

	public void addEventInputQueue(EventQueue e)
	{
		eventInputQueues.add(e);
	}

	public void addEventOutputConnection()
	{
	
	}
    
	public void addDataInputConnection()
	{
	
	}

	// methods
	public Event selectEventToHandle()
	{
		System.out.println("BasicFBInstace.selectEventToHandle()");
		// TODO: Implement better event selection
		// For the skeleton the first event of the first queue will do
		return ((EventQueue) eventInputQueues.get(0)).remove();
	}

	public void handleEvent()
	{
		System.out.println("BasicFBInstance.handleEvent()");

		currentEvent = selectEventToHandle();
		// update variables with the ones that came with the event
		// and execute the ecc
		//ECState newECState = fbType.getECC().execute(currentECState, variables);
		//if (newECState.getName() != currentECState.getName())
		//{
		// only set this if we find any algorithms
		//handlingEvent = true;
		// TODO: get actions, make the jobs and schedule them
		// schedule jobs one at a time and wait for them to finish
		//}

		//=========================
		//to test the threads
		//for every event queue one algorithm
		resource.getScheduler().scheduleJob(new Job(this, ((BasicFBType) fbType).getAlgorithm(), variables));
		handlingEvent = true;	
	}

	public void finishedJob(Job theJob)
	{
		System.out.println("BasicFBInstance.finishedJob()");
		handlingEvent = false;
	
		// for the dummy app make shure that the new event is queued 
		// since there are no external sources
		queueEvent("DummyEventInput");
	
	}

	public void queueEvent(String eventInput)
	{
		System.out.println("BasicFBInstace.queueEvent()");
		// get data, make the event object and then queue it
		((EventQueue) eventInputQueues.get(0)).add(new Event());
	}
    
	boolean isHandlingEvent()
	{
		return handlingEvent;
	}

}
