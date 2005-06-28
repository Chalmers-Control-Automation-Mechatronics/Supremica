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
import java.lang.*;

/**
 * @author Goran Cengic
 */
public class CompositeFBType extends FBType
{

	// map instance names to type names
	private Map fbInstanceNames = new HashMap();

	// map from event cnt spec to to event cnt spec 
	private Map eventConnectionSpecs = new HashMap();
	private Map dataConnectionSpecs = new HashMap();


	//====================================================================================
	private CompositeFBType() {}
	
	public CompositeFBType(String n,Resource r)
	{
		System.out.println("CompositeFBType(" + n + "," + r.getName()  + ")");
		setName(n);
		resource = r;
	}
	//====================================================================================

	public FBInstance createInstance(String name)	
	{
		System.out.println("CompositeFBType.createInstace(" + name + ")");
		CompositeFBInstance newInstance = new CompositeFBInstance(name,resource,this);	
		
		// first instantiate all internal instances
		for (Iterator iter = fbInstanceNames.keySet().iterator(); iter.hasNext();)
		{
			String curFBInstName = (String) iter.next();
			String curFBTypeName = (String) fbInstanceNames.get(curFBInstName);
			newInstance.addFBInstance(curFBInstName,curFBTypeName);
		}
		// then make event connections
		for (Iterator iter = eventConnectionSpecs.keySet().iterator(); iter.hasNext();)
		{
			String curFrom = (String) iter.next();
			String curTo = (String) eventConnectionSpecs.get(curFrom);

			String curFromInstance = getInstanceName(curFrom);
			String curFromSignal = getSignalName(curFrom);
			String curToInstance = getInstanceName(curTo);
			String curToSignal = getSignalName(curTo);

			if (curFromInstance.equals(""))
			{
				// internal event input connection
				newInstance.addInternalEventInputConnection(curFromSignal, curToInstance, curToSignal);
			}
			else if (curToInstance.equals(""))
			{
				// internal event output connection
				newInstance.addInternalEventOutputConnection(curFromInstance, curFromSignal, curToSignal);
			}
			else
			{
				// internal instance event connection
				newInstance.addEventConnection(curFromInstance, curFromSignal, curToInstance, curToSignal);
			}

		}
		// finally make data connections
		for (Iterator iter = dataConnectionSpecs.keySet().iterator(); iter.hasNext();)
		{
			String curFrom = (String) iter.next();
			String curTo = (String) dataConnectionSpecs.get(curFrom);

			String curFromInstance = getInstanceName(curFrom);
			String curFromSignal = getSignalName(curFrom);
			String curToInstance = getInstanceName(curTo);
			String curToSignal = getSignalName(curTo);

			if (curFromInstance.equals(""))
			{
				// internal data input connection
				newInstance.addInternalDataInputConnection(curFromSignal, curToInstance, curToSignal);
			}
			else if (curToInstance.equals(""))
			{
				// internal data output connection
				newInstance.addInternalDataOutputConnection(curFromInstance, curFromSignal, curToSignal);
			}
			else
			{
				// internal instance data connection
				newInstance.addDataConnection(curFromInstance, curFromSignal, curToInstance, curToSignal);
			}

		}

		newInstance.setEvents(events);

		newInstance.setVariables((Variables) variables.clone());

		instances.put(name,newInstance);

		return newInstance;
	}

	public void addFBInstance(String instName,String typeName)
	{
		fbInstanceNames.put(instName,typeName);
	}
	
	public void addEventConnection(String from, String to)
	{
		eventConnectionSpecs.put(from,to);
	}
	
	public void addDataConnection(String from, String to)
	{
		dataConnectionSpecs.put(from,to);
	}

	private String getInstanceName(String cntSpec)
	{
		if (cntSpec.indexOf(".") < 0)
		{
			return "";
		}
		return cntSpec.substring(0,cntSpec.indexOf("."));
	}
	
	private String getSignalName(String cntSpec)
	{
		if (cntSpec.indexOf(".") < 0)
		{
			return cntSpec;
		}
		
		return cntSpec.substring(cntSpec.indexOf(".")+1,cntSpec.length());
	}
	
}
