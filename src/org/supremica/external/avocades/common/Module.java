package org.supremica.external.avocades.common;

import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

import org.supremica.automata.ExtendedAutomata;

public class Module extends ExtendedAutomata{
	
   /**
	*list of events to store added event
	* in ExtendedAutomaton
	*/
	private LinkedList<String> events;
	private Hashtable<String,Integer> integerVariables;
	
	private int parallels;
	private int arbitraryNode;
	private int arbitrary;
	
	private final String PARALLEL_PREFIX = "pa";
	private final String ARBITRARY_ORDER_PREFIX = "ao";
	
	private final String BLOCKED_STATE = "not_rechable_state";
	
	private EFA nodeVariablesEFA;
	private EFA resourceVariablesEFA;
	private EFA blockedEventsEFA;
	
	public Module(String name, boolean expand){
		super(name, expand);
		
		events = new LinkedList<String>();
		integerVariables = new Hashtable<String,Integer>();
		
		parallels = 0;
		arbitraryNode = 0;
		arbitrary = 0;
		
		nodeVariablesEFA = null;
		resourceVariablesEFA = null;
		
	}
	
	public List<String> getEvents(){
		return events;
	}
	
	public void initNodeVariables(){
		if(nodeVariablesEFA != null){
			return;
		}
		
		nodeVariablesEFA = new EFA("Node_variables",this);
		this.addAutomaton(nodeVariablesEFA);
		nodeVariablesEFA.addInitialState("dummy_state_for_nod_variables");
		
	}
	
	public void initResourceVariables(){
		if(resourceVariablesEFA != null){
			return;
		}
		
		resourceVariablesEFA = new EFA("Resource_variables",this);
		this.addAutomaton(resourceVariablesEFA);
		resourceVariablesEFA.addInitialState("dummy_state_for_resource_variables");
	}
	
	public void initBlockedEvents(){
		
		if(blockedEventsEFA != null){
			return;
		}
		
		blockedEventsEFA = new EFA("Blocked_event",this);
		this.addAutomaton(blockedEventsEFA);
		blockedEventsEFA.addInitialState("dummy_inital_state_for_blocked");
		blockedEventsEFA.addState(BLOCKED_STATE);
		
	}
	
	public void blockEvents(List<String> events){
		
		String event = "";
		
		//check in data
		if(events == null || events.size() == 0){
			return;
		}
		
		if(blockedEventsEFA == null){
			initBlockedEvents();
		}
		
		for(String e : events){
			if(e.endsWith(";")){
				event = event + e;
			}else{
				event = event + e + ";";
			}
		}
		
		blockedEventsEFA.addTransition(BLOCKED_STATE,
									   BLOCKED_STATE,
									   event,"","");
	}
	
	public void addEvent(String event){
		//check in data
	    if(event == null){
	           return;
	    }else if(event.length() == 0){
	    	   return;
	    }
	       
	    //check if we already added this event
	    if(events.contains(event)){
	    	return;
	    }else{
	    	events.add(event);
	    	
	    	//add new event to module
		    super.addEvent(event);
	    }
	}
	
	/**
	 * Add new integer variables for parallel node
	 * 
	 * @param efa
	 * @param upperBound
	 * @return name of new parallel variable created
	 */
	public String newParrallelNodeInteger(int upperBound){
		
		final int lowerBound = 0;
		final int initialValue = 0;
		final int markedValue = 0;
		
		if(nodeVariablesEFA == null){
			initNodeVariables();
		}
		
		String name = PARALLEL_PREFIX + "_" + parallels;
		parallels = parallels + 1;
		
		integerVariables.put(name, upperBound);
		
		nodeVariablesEFA.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, markedValue);
		
		return name;
	}
	
	/**
	 * Add new arbitrary node variable
	 * 
	 * @param efa
	 * @param upperBound
	 * @return name of new parallel variable created
	 */
	public String newArbitraryOrderNodeInteger(int upperBound){
		
		final int lowerBound = 0;
		final int initialValue = 0;
		final int markedValue = 0;
		
		if(nodeVariablesEFA == null){
			initNodeVariables();
		}
		
		String name = ARBITRARY_ORDER_PREFIX +"n_"+ arbitraryNode;
		arbitraryNode = arbitraryNode + 1;
		
		integerVariables.put(name, upperBound);
		
		nodeVariablesEFA.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, markedValue);
		
		return name;
	}
	
	/**
	 * Add new integer variables for arbitrary node
	 * 
	 * @param efa
	 * @param upperBound
	 * @return name of new parallel variable created
	 */
	public String newArbitraryInteger(){
		
		final int lowerBound = 0;
		final int upperBound = 1;
		
		final int initialValue = 1;
		final int markedValue = 1;
		
		if(nodeVariablesEFA == null){
			initNodeVariables();
		}
		
		String name = ARBITRARY_ORDER_PREFIX + "_" +arbitrary;
		arbitrary = arbitrary + 1;
		
		integerVariables.put(name, upperBound);
		
		nodeVariablesEFA.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, markedValue);
		
		return name;
	}
	
	public int getMaxValueResourceInteger(String resourceName){
		return integerVariables.get(resourceName);
	}
	
	/**
	 * Create new resource variable in this module.
	 * 
	 * @param resourceName name for resource
	 * @param upperBound max value for resource
	 */
	public void newResourceInteger(String resourceName, int upperBound){
		
		final int lowerBound = 0;
		final int initialValue = upperBound;
		//final int markedValue = upperBound; 
		
		//check in data
		if(resourceName == null || resourceName.length() == 0){
			return;
		}
		
		/* Already added */
		if(integerVariables.containsKey(resourceName)){
			return;
		}
		
		integerVariables.put(resourceName, upperBound);
		
		if(resourceVariablesEFA == null){
			initResourceVariables();
		}
		
		/* create resource variable */
		resourceVariablesEFA.addIntegerVariable(
				resourceName, lowerBound, upperBound, initialValue, null);
	}
}
