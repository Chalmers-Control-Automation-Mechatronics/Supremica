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

import org.supremica.functionblocks.model.interpreters.st.Tester;
import java.util.*;

/**
 * @author Cengic
 */
public class Resource
{
	
	private String name;
	private Scheduler scheduler;
	private List applicationFragments = new ArrayList();
	
	BasicFBInstance fbInstance;
	
	private Resource() {}
	
	public Resource(String name)
	{
		System.out.println("Resource(" + name + ")");

		this.name = name;
		scheduler = new Scheduler(this);

		// creat the test application
	
		// FB types
		BasicFBType fbType = new BasicFBType("P1", this);
		// only one event input and output for now
		fbType.addVariable("OCCURRED", new BooleanVariable("EventInput",false));
		fbType.addVariable("DONE", new BooleanVariable("EventOutput",false));
		fbType.addVariable("invoked", new IntegerVariable("Local",0));
		// Build ECC 
		fbType.getECC().addInitialState("INIT");
		fbType.getECC().addState("STATE");
		fbType.getECC().addTransition("INIT", "STATE", "OCCURRED");
		fbType.getECC().addTransition("STATE", "INIT", "TRUE");
		fbType.getECC().getState("STATE").addAction(new TestAlgorithm(), "DONE");

		// FB instances
		fbInstance = fbType.createInstance("P1inst");

		// connections
		fbInstance.addEventOutputConnection("DONE", new Connection(fbInstance,"OCCURRED"));

		fbInstance.queueEvent("OCCURRED");
	
		//Interpreter tester
		//new Tester();

	
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
}
