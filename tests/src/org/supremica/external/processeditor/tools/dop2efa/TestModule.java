/**
 * class to test DOP to EFA 
 */
package org.supremica.external.processeditor.tools.dop2efa;

import java.util.List;
import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.supremica.external.avocades.common.Module;


public class TestModule extends TestCase{
	
	private Module module;
    
    public TestModule(String name){
        super(name);
    }
    
    /**
     * Sets up the test fixture.
     * Called before every test case method.
     */
    protected void setUp(){
    	module = new Module("Test", false);
    }
    
    /**
     * Tears down the test fixture.
     * Called after every test case method.
     */
    protected void tearDown(){
    	module = null;
    }
    
    /**
     * Assembles and returns a test suite
     * for all the test methods of this test case.
     */
    public static Test suite(){
        TestSuite suite = new TestSuite(TestModule.class);
        return suite;
    }
    
    
    public void testAddEvent(){
    	
    	List<String> eventList = null;
    	String event = "testevent";
    	
    	/*
    	 *	Add one event
    	 */
    	module.addEvent(event);
    	
    	
    	/*
    	 * Event should have same name and be
    	 * added to list
    	 */
    	eventList = module.getEvents();
    	
    	assertEquals(1, eventList.size());
    	assertEquals(event, eventList.get(0));
    	
    	/*
    	 * Add same event again
    	 */
    	module.addEvent(event);
    	
    	/*
    	 *	An event should not be
    	 *	added if it already exist
    	 */
    	eventList = module.getEvents();
    	assertEquals(1, eventList.size());
    	
    	/*
    	 * Add another event
    	 */
    	module.addEvent(event + "2");
    	
    	/*
    	 *	this event should be added
    	 */
    	eventList = module.getEvents();
    	assertEquals(2, eventList.size());
	}
    
    public void testArbitraryOrederIntegerMaxValue(){
    	//random integer with max value 100
    	Random rnd = new Random();
    	int upperBound = rnd.nextInt(100);
    	
    	//variable name
    	String varName = "Zon1";
    	
    	//test arbitrary order integer
    	varName = module.newArbitraryOrderNodeInteger(upperBound);
    	assertEquals(upperBound, module.getMaxValueResourceInteger(varName));
    }
 
    public void testParallelIntegerMaxValue(){
    	
    	//random integer with max value 100
    	Random rnd = new Random();
    	int upperBound = rnd.nextInt(100);
    	
    	//variable name
    	String varName = "Zon1";
    	
    	//test parallel integer
    	varName = module.newParrallelNodeInteger(upperBound);
    	assertEquals(upperBound, module.getMaxValueResourceInteger(varName));
    }
    
    public void testResourceIntegerMaxValue(){
    	
    	//random integer with max value 100
    	Random rnd = new Random();
    	int upperBound = rnd.nextInt(100);
    	
    	//variable name
    	String varName = "Zon1";
    	
    	//test resource integer
    	module.newResourceInteger(varName, upperBound);
    	assertEquals(upperBound, module.getMaxValueResourceInteger(varName));
    }
}

