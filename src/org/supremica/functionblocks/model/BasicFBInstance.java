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
 * @author Goran Cengic
 */
package org.supremica.functionblocks.model;

import java.util.*;

public class BasicFBInstance extends FBInstance
{

    private ECState currentECState;
    private ECAction currentECAction;
    private boolean handlingEvent = false;
    private Iterator actionsIterator;
    private int actionsLeft;

	private EventQueue eventInputQueue = new EventQueue();

	//================================================================
    private BasicFBInstance() {}
	
    public BasicFBInstance(String n, Resource r, BasicFBType t)
    {
		setName(n);
		resource = r;
		fbType = t;
		currentECState = ((BasicFBType) fbType).getECC().getInitialState();
    }
	//================================================================

    private Event getNextEvent()
    {
		return  (Event) eventInputQueue.remove();
    }

	public void receiveEvent(String eventInput)
	{
		synchronized(eventInputQueue)
		{
			if(variables.getVariable(eventInput) != null)
				if(variables.getVariable(eventInput).getType().equals("EventInput"))
				{
					eventInputQueue.add((Event) events.get(eventInput));
					resource.getScheduler().scheduleFBInstance(this);
				}
				else
				{
					System.out.println("BasicFBInstance(" + getName() + "): No event input " + eventInput);
					System.exit(0);
				}
			else
			{
				System.out.println("BasicFBInstance(" + getName() + "): No event input " + eventInput);
				System.exit(0);
			}
		}
    }
	
    public void handleEvent()
    {
	
		if(handlingEvent)
		{
			resource.getScheduler().scheduleFBInstance(this);
			return;			
		}

		currentEvent = getNextEvent();
		
		handlingEvent = true;

		//System.out.println("BasicFBInstance(" + getName() + "): handling event " + currentEvent.getName() + " from Ecc state " + currentECState.getName());
		
		// set all InputEvents to false
		for (Iterator iter = variables.iterator();iter.hasNext();)
		{
			String curName = (String) iter.next();
			if (((Variable) variables.getVariable(curName)).getType().equals("EventInput"))
			{
				((BooleanVariable) variables.getVariable(curName)).setValue(false);
			}
		}
		
		// set the corrensponding event var of the input event to TRUE 
		((BooleanVariable) variables.getVariable(currentEvent.getName())).setValue(true);
		
		// get input data values
		getDataInputs(currentEvent);
		
		// and execute the ecc
		ECState newECState = updateECC();
		if (newECState != currentECState)
		{
			handleNewState(newECState);
		}
    }
	
    public void finishedJob(Job theJob)
    {
		setVariables(theJob.getVariables());
		sendEvent();
		handleState();
    }


    private ECState updateECC()
    {
		return ((BasicFBType) fbType).getECC().execute(currentECState, variables);
    }

    // initializes the handling of new state
    private void handleNewState(ECState state)
    {
		currentECState = state;
		// set total number of actions in this state
		actionsLeft = currentECState.getNumberOfActions();
		// and get the iterator for them
		actionsIterator = currentECState.actionsIterator();
		handleState();
    }
	
    // handles currentState between actions
    private void handleState()
    {
		if (actionsLeft > 0 )
		{
			handleAction((ECAction) actionsIterator.next());
		}
		
		if (actionsLeft == 0)
		{
			// set event var to false
			((BooleanVariable) variables.getVariable(currentEvent.getName())).setValue(false);
			// repeat the handling of the state if state is changed
			System.out.println();
                        ECState newECState = updateECC(); 
			if (newECState != currentECState)
			{
				handleNewState(newECState);
			}
			else
			{
				// we're done with the event
				//System.out.println("BasicFBInstance: Done with event " + currentEvent.getName() + " and in ECState " + currentECState.getName());
				handlingEvent = false;
			}
		}
    }

    private void handleAction(ECAction action)
    {
		currentECAction = action;
		if (currentECAction.getAlgorithm() != null)
		{
			Variables algVars =  (Variables) variables.clone();
			resource.getScheduler().scheduleJob(new Job(this, ((BasicFBType) fbType).getAlgorithm(currentECAction.getAlgorithm()), algVars));
		}
		else if (currentECAction.getAlgorithm() == null && currentECAction.getOutput() != null)
		{
			sendEvent();
			handleState();
		}
    }

    private void sendEvent()
    {
		if (currentECAction.getOutput() != null)
		{
			Connection outputConnection = (Connection) eventOutputConnections.get(currentECAction.getOutput());
			if (outputConnection != null)
			{
				outputConnection.getFBInstance().receiveEvent(outputConnection.getSignalName());
			}
		}
		actionsLeft = actionsLeft - 1;
    }
	
}
