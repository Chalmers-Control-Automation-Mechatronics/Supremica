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
 * @author Goran Cengic
 */
/*
 * Created on Dec 16, 2004
 */
package org.supremica.functionblocks.model;

import java.util.Map;
import java.util.HashMap;
public class FBNetwork
{

	private Resource resource;
	// instances
	private Map fbInstances = new HashMap();

	private FBNetwork() {}
	
	public FBNetwork(Resource res)
	{
		resource = res;
	}

	public void addFBInstance(String instName,String typeName)
	{
		fbInstances.put(instName,resource.getFBType(typeName).createInstance(instName));
	}

	public FBInstance getFBInstance(String name)
	{
		if (fbInstances.get(name) == null)
		{
			System.err.println("FBNetwork.getFBInstance(" + name+ "): No such FB instance " + name);
			System.exit(1);
		}
		return (FBInstance) fbInstances.get(name);
	}

	public void addEventConnection(String source, String dest)
	{
		
		String fromInstance = getInstanceName(source);
		String fromSignal = getSignalName(source);
		String toInstance = getInstanceName(dest);
		String toSignal = getSignalName(dest);
		
		if (fromInstance.equals(""))
		{
			System.err.println("FBNetwork.addEventConnection(): from instance is empty in connection from " + source +" to " + dest);
		}
		else if (toInstance.equals(""))
		{
			System.err.println("FBNetwork.addEventConnection(): to instance is empty in connection from " + source +" to " + dest);
		}
		else if (fromInstance.equals(toInstance))
		{
			System.err.println("FBNetwork.addEventConnection(): source instance (" + source +") is equal to destination instance (" + dest + ")");
			System.err.println("                              : that is not allowed according to standard!");
			System.exit(0);
		}
		else
		{
		
			System.out.println("FBNetwork.addEventConnection: From " + source + " to " + dest);
			Connection newConn = new Connection(getFBInstance(toInstance), toSignal);
			getFBInstance(fromInstance).addEventOutputConnection(fromSignal, newConn);
			
		}

	}

	public void addDataConnection(String source, String dest)
	{
		
		String fromInstance = getInstanceName(source);
		String fromSignal = getSignalName(source);
		String toInstance = getInstanceName(dest);
		String toSignal = getSignalName(dest);
		
		if (fromInstance.equals(""))
		{
			// Constant specification
			System.out.println("FBNetwork.addDataConnection(): setting data input " + dest +" to " + source);
			getFBInstance(toInstance).setVariableValue(toSignal, new Integer(fromSignal));
		}
		else if (toInstance.equals(""))
		{
			System.err.println("FBNetwork.addDataConnection(): to instance is empty in connection from " + source +" to " + dest);
		}
		else
		{
			System.out.println("FBNetwork.addDataConnection: From " + source + " to " + dest);			
			Connection newConn = new Connection(getFBInstance(fromInstance), fromSignal);
			getFBInstance(toInstance).addDataInputConnection(toSignal, newConn);			
		}

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
