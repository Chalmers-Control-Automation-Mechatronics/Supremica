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
	private Map eventInputQueues = new HashMap();


	private ECState currentECState;
	private Variables variables;

	private boolean handlingEvent = false;
	private ECAction currentECAction;
	private int actionsLeft = 0;

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

		// fix for testing -- will not be here too long
		variables.addVariable("OCCURRED", new BooleanVariable("EventInput",false));
		variables.addVariable("DONE", new BooleanVariable("EventOutput",false));
		variables.addVariable("invoked", new IntegerVariable("Local",0));
		
		currentECState = fbType.getECC().getInitialState();
	}

	public void addEventInputQueue(String event)
	{
		eventInputQueues.put(event,new EventQueue());
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
		if( ((EventQueue) eventInputQueues.get("OCCURRED")).size() > 0 )
		{
			return  ((EventQueue) eventInputQueues.get("OCCURRED")).remove();
		}
		return null;
	}

	public void handleEvent()
	{
		System.out.println("BasicFBInstance.handleEvent()");

		Event currentEvent = selectEventToHandle();
		if(currentEvent != null)
		{
			// update variables with the ones that came with the event
			((BooleanVariable) variables.getVariable(currentEvent.getName())).setValue(true);
			// and execute the ecc
			ECState newECState = fbType.getECC().execute(currentECState, variables);
			if (newECState != currentECState)
			{
				// get actions, make the jobs and schedule them
				// one at a time and wait for them to finish
				actionsLeft = newECState.getNumberOfActions();
				for (Iterator iter = newECState.actionsIterator();iter.hasNext();)
				{
					currentECAction = (ECAction) iter.next();
					if (currentECAction.getAlgorithm() != null)
					{
						resource.getScheduler().scheduleJob(new Job(this, currentECAction.getAlgorithm(), variables));
						handlingEvent = true;
					}
					else if (currentECAction.getAlgorithm() == null && currentECAction.getOutput() != null)
					{
						// send the output
					}
				}
			}
		}
	}
	
	public void finishedJob(Job theJob)
	{
		System.out.println("BasicFBInstance.finishedJob()");
		if (currentECAction.getOutput() != null)
		{
			// make the connection for this to fully function
			queueEvent("OCCURRED");
		}
		actionsLeft = actionsLeft - 1;
		if (actionsLeft == 0){
			handlingEvent = false;
		}
	}

	public void queueEvent(String eventInput)
	{
		System.out.println("BasicFBInstace.queueEvent(): " + eventInput);
		// get data, make the event object and then queue it
		((EventQueue) eventInputQueues.get(eventInput)).add(new Event(eventInput));
	}
    
	boolean isHandlingEvent()
	{
		return handlingEvent;
	}

}
