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
import java.io.Reader;
import java.io.FileReader;

/**
 * @author Cengic
 */
public class Resource extends NamedObject
{
	
	private Loader loader;

	private Scheduler scheduler;
	private Map fbTypes = new HashMap();
	private Map appFragments = new HashMap();


	private Resource() {}
	
	public Resource(String name)
	{
		System.out.println("Resource(" + name + ")");

		setName(name);

		loader = new Loader(this);
		scheduler = new Scheduler(this);



		// creat the test application
		
		loader.load("/home/cengic/devel/workspace/Supremica/examples/functionblocks/FBRuntime/TestType.fbt");
		
		/*
		// FB types
		addBasicFBType("P1");
		BasicFBType fbType = (BasicFBType) getFBType("P1");

		// only one event input and output for now
		fbType.addVariable("OCCURRED", new BooleanVariable("EventInput",false));
		fbType.addDataAssociation("OCCURRED","DI");
		fbType.addVariable("DONE", new BooleanVariable("EventOutput",false));
		fbType.addDataAssociation("DONE","DO");
		fbType.addVariable("DI", new IntegerVariable("DataInput",0));
		fbType.addVariable("DO", new IntegerVariable("DataOutput",0));
		fbType.addVariable("invoked", new IntegerVariable("Local",0));

		// Build ECC 
		fbType.getECC().addInitialState("INIT");
		fbType.getECC().addState("STATE");
		fbType.getECC().addTransition("INIT", "STATE", "OCCURRED");
		fbType.getECC().addTransition("STATE", "INIT", "TRUE");
		// create algorithm
		fbType.addAlgorithm(new JavaTextAlgorithm("TestAlg",
							  "invoked = invoked + 1;" +
							  "DO = DI + 1;" +
							  "System.out.println(\"TestAlgorithm.execute(): invoked: \" + invoked + \" times.\");" +
							  "System.out.println(\"TestAlgorithm.execute(): DO: \" + DO + \".\");"
							  ));
							  
		fbType.getECC().getState("STATE").addAction("TestAlg", "DONE");
		*/
		// FB application fragment
		addApplicationFragment("AppFrag");
		ApplicationFragment appFrag =  getApplicationFragment("AppFrag");
 
		// add FB instances to app frag
		appFrag.addFBInstance("inst1","TestType");
		appFrag.addFBInstance("inst2","TestType");

		// connections
		appFrag.addEventConnection("inst1","DONE","inst2","OCCURRED");
		appFrag.addEventConnection("inst2","DONE","inst1","OCCURRED");

		appFrag.addDataConnection("inst1","DO","inst2","DI");
		appFrag.addDataConnection("inst2","DO","inst1","DI");


		// kick off 
		appFrag.getFBInstance("inst1").queueEvent("OCCURRED");
	
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
	
	public void addApplicationFragment(String name)
	{
		appFragments.put(name, new ApplicationFragment(this));
	}

	public ApplicationFragment getApplicationFragment(String name)
	{
		return (ApplicationFragment) appFragments.get(name);
	}

	public void addBasicFBType(String name)
	{
		fbTypes.put(name,new BasicFBType(name,this));
	}

	public FBType getFBType(String name)
	{
		return (FBType) fbTypes.get(name);
	}

}
