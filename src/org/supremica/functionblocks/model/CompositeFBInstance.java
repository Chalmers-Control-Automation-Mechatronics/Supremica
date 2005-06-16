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
 * Created on Dec 16, 2004
 */
package org.supremica.functionblocks.model;

import java.util.*;

/**
 * @author cengic
 */
public class CompositeFBInstance extends FBInstance
{

	// internal instances
	private Map fbInstances = new HashMap();

	// map type event input to connection containing internal instance and its event input
	private Map internalEventInputConnections = new HashMap();
	// map type event output to connection containing internal instance and its event output
	private Map internalEventOutputConnections = new HashMap();
	// the same for internal data connection
	private Map internalDataInputConnections = new HashMap();
	private Map internalDataOutputConnections = new HashMap();
	
	//================================================================
    private CompositeFBInstance() {}
	
    public CompositeFBInstance(String n, Resource r, CompositeFBType t)
    {
		name = n;
		resource = r;
		fbType = t;
    }
	//================================================================


	//=================================================================================================	
	// following methods are used during the creation of the instance

	public void addFBInstance(String instName,String typeName)
	{
		fbInstances.put(instName,resource.getFBType(typeName).createInstance(instName));
	}	

	public FBInstance getFBInstance(String instName)
	{
		return (FBInstance) fbInstances.get(instName);
	}

	public void addInternalEventInputConnection(String fromEvent, String toInstance, String toEvent)
	{
		Connection newConnection = new Connection(getFBInstance(toInstance), toEvent);
		internalEventInputConnections.put(fromEvent, newConnection);
	}

	public void addInternalEventOutputConnection(String fromInstance, String fromEvent, String toEvent)
	{
		Connection newConnection = new Connection(getFBInstance(fromInstance), fromEvent);
		internalEventOutputConnections.put(toEvent, newConnection);
	}

	public void addInternalDataInputConnection(String fromData, String toInstance, String toData)
	{
		Connection newConnection = new Connection(getFBInstance(toInstance), toData);
		internalDataInputConnections.put(fromData, newConnection);
	}

	public void addInternalDataOutputConnection(String fromInstance, String fromData, String toData)
	{
		Connection newConnection = new Connection(getFBInstance(fromInstance), fromData);
		internalDataOutputConnections.put(toData, newConnection);
	}


	public void addEventConnection(String fromInstance,String fromEvent, String toInstance, String toEvent)
	{
		Connection newConnection = new Connection(getFBInstance(toInstance), toEvent);
		getFBInstance(fromInstance).addEventOutputConnection(fromEvent, newConnection);
	}

	public void addDataConnection(String fromInstance,String fromData, String toInstance, String toData)
	{
		Connection newConnection = new Connection(getFBInstance(fromInstance), fromData);
		getFBInstance(toInstance).addDataInputConnection(toData, newConnection);
	}

	//==================================================================================================
	// following methods are used after the instance has been created

	public void addEventOutputConnection(String output, Connection cnt)
    {
		Connection internalCnt = (Connection) internalEventOutputConnections.get(output);
		internalCnt.getFBInstance().addEventOutputConnection(internalCnt.getSignalName(),cnt);
    }
    
    public void addDataInputConnection(String input, Connection cnt)
    {	
		Connection internalCnt = (Connection) internalDataInputConnections.get(input);
		internalCnt.getFBInstance().addDataInputConnection(internalCnt.getSignalName(),cnt);		
    }

	public void receiveEvent(String eventInput)
	{
		Connection internalCnt = (Connection) internalEventInputConnections.get(eventInput);
		internalCnt.getFBInstance().receiveEvent(internalCnt.getSignalName());		
	}

	public Variable getDataOutput(String dataOutput)
    {
		Connection internalCnt = (Connection) internalDataOutputConnections.get(dataOutput);
		return internalCnt.getFBInstance().getDataOutput(internalCnt.getSignalName());				
    }
}
