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
    private BasicFBType fbType;

    private List eventQueues = new ArrayList();

    private ECState currentECState;
    private ECAction currentECAction;
    private Variables variables;

    private Event currentEvent;
    private boolean handlingEvent = false;

    //private Map eventOutputConnections = new ArrayList();
    //private Map dataInputConnections = new ArrayList();


    // contructor and methods for construction
    public BasicFBInstance(String n, BasicFBType t)
    {
        this.name = n;
	fbType = t;
    }

    public void addEventQueue(EventQueue e)
    {
	eventQueues.add(e);
    }

    public void addEventOutputConnection()
    {
	
    }
    
    public void addDataInputConnection()
    {
	
    }

    // methods
    public Event getEventToHandle()
    {
        System.out.println("BasicFBInstace.selectEventToHandle()");
        // TODO: Implement better event selection
        // For the skeleton the first queue will do
        return ((EventQueue) eventQueues.get(0)).remove();
    }

    public void handleEvent()
    {
        System.out.println("BasicFBInstance.handleEvent()");

        currentEvent = getEventToHandle();
	// update variables with the ones that come with the event
	ECState newECState = fbType.getECC().execute(currentECState, variables);
        if (newECState.getName() != currentECState.getName())
        {
            handlingEvent = true;
            // TODO: get actions, make the jobs and schedule them
	    // schedule jobs one at a time and wit for them to finish
        }
    }

    public void finishedJob(Job theJob)
    {
        System.out.println("BasicFBInstance.finishedJob()");
	handlingEvent = false;
    }

    public void queueEvent(String eventInput)
    {
	System.out.println("BasicFBInstace.queueEvent()");
	
    }
}
