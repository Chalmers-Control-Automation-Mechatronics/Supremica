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

/**
 * @author Cengic
 */
public class Resource
{

    private String name;
    private Scheduler scheduler;
    private List applicationFragments = new ArrayList();

    // attributes for the one instance prototype
    BasicFBInstance fbInstance = null;
    BasicFBType fbType = null;

    private Resource() {}

    public Resource(String name)
    {
	System.out.println("Resource(" + name + ")");

        this.name = name;
	scheduler = new Scheduler(this);

	// creat the application from the Sync paper for testing
	fbType = new BasicFBType("DummyFBType", this);
	fbType.addAlgorithm(new TestAlgorithm("DummyAlgorithm"));
	fbInstance = fbType.createInstance("DummyFBInstance");
	fbInstance.addEventInputQueue(new EventQueue());
	fbInstance.queueEvent("DummyEventInput");
	
	// Test ST interpreter
	Variables testVars = new Variables();
	testVars.addVariable("bvar", new BooleanVariable("Local", true));
	testVars.addVariable("ivar", new IntegerVariable("Local", 10));
	testVars.addVariable("dvar", new DoubleVariable("Local", 3.14));
	testVars.addVariable("fvar", new FloatVariable("Local", 3.18F));
	testVars.addVariable("svar", new StringVariable("Local", "TestString"));

	System.out.println("Resource("+ name +"): Testing Expressions:");
	System.out.println("Testing Primary Expressions:");
	ECCondition testCond = new ECCondition("TRUE"); 
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("3.18F");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("3.14");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("3");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("\"blah\"");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("bvar");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("ivar");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("dvar");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("fvar");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));
	testCond.set("svar");
	System.out.println("evaluation result: " + testCond.evaluate(testVars));

	
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
