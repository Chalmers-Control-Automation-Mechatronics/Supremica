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
 * @author Cengic
 */
package org.supremica.functionblocks.model;

import java.util.*;

public abstract class FBInstance extends NamedObject
{

	// resource to which this instance belongs
	Resource resource;
	// type of this instance
	FBType fbType;
	// maps event names to event classes representing them
	Map events;
	EventQueue eventInputQueue = new EventQueue();
	Map eventOutputConnections = new HashMap();
	Map dataInputConnections = new HashMap();
	// instance's variables
    Variables variables;


	public void setVariables(Variables vars)
	{
		variables = vars;
	}

    public void addVariable(String name, Variable var)
    {
		variables.addVariable(name,var);
    }

    public void setEvents(Map i)
    {
		events = i;
    }

    public void addEventOutputConnection(String output, Connection cnt)
    {
		eventOutputConnections.put(output, cnt);
    }
    
    public void addDataInputConnection(String input, Connection cnt)
    {	
		dataInputConnections.put(input, cnt);
    }

	public Connection getEventOutputConnection(String output)
	{
		return (Connection) eventOutputConnections.get(output);
	}

	// This method provides its output data to the calling BasicFBInstance
    public Variable getDataOutput(String name)
    {
		if (!((Variable) variables.getVariable(name)).getType().equals("DataOutput"))
		{
			System.out.println("BasicFBInstance: no such DataOutput " + name);
			System.exit(0);
		}
		return (Variable) variables.getVariable(name);
    }

		
    public void queueEvent(String eventInput)
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
					System.out.println("FBInstance: No event input " + eventInput);
					System.exit(0);
				}
			else
			{
				System.out.println("FBInstance: No event input " + eventInput);
				System.exit(0);
			}
		}
    }


	public abstract void handleEvent();
	
}
