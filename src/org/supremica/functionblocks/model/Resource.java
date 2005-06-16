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
package org.supremica.functionblocks.model;

import java.util.*;
import java.io.File;

/**
 * @author Cengic
 */
public class Resource extends NamedObject
{

    private Device device; // device the resource belongs to
	
    private Scheduler scheduler;
    private Map fbTypes = new HashMap();
    private Map fbNetworks = new HashMap();


    private Resource() {}

    public Resource(String name, Device d)
    {
		System.out.println("Resource(" + name + ")");
		
		setName(name);

		device = d;
		
		scheduler = new Scheduler(this);
	
    }

    void handleConfigurationRequests()
    {
		System.out.println("Resource.handleConfigurationRequests()");
    }

    void runResource()
    {
		System.out.println("Resource.runResource()");
		scheduler.runEvents();
    }

    String getName()
    {
		return name;
    }

    Scheduler getScheduler()
    {
		return scheduler;
    }
	
    public void addFBNetwork(String name)
    {
		fbNetworks.put(name, new FBNetwork(this));
    }

    public FBNetwork getFBNetwork(String name)
    {
		return (FBNetwork) fbNetworks.get(name);
    }

    public void addBasicFBType(String name)
    {
		fbTypes.put(name,new BasicFBType(name,this));
    }

    public void addCompositeFBType(String name)
    {
		fbTypes.put(name,new CompositeFBType(name,this));
    }

    public void addServiceFBType(String name, File serviceScript)
    {
		fbTypes.put(name,new ServiceFBType(name,this,serviceScript));
    }

    public FBType getFBType(String name)
    {
		return (FBType) fbTypes.get(name);
    }

}
