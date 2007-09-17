package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.util.LinkedList;

import org.supremica.automata.ExtendedAutomata;

public class Module extends ExtendedAutomata{
	
   /**
	*list of events to store added event
	* in ExtendedAutomaton
	*/
	private LinkedList<String> events;
	
	private int parallels;
	private int arbitraryNode;
	private int arbitrary;
	
	private final String PARALLEL_PREFIX = "pa";
	private final String ARBITRARY_ORDER_PREFIX = "ao";
	
	private EFA global_variables_dummy_efa;
	   
	public Module(String name, boolean expand){
		super(name, expand);
		events = new LinkedList<String>();
		parallels = 0;
		arbitraryNode = 0;
		arbitrary = 0;
		global_variables_dummy_efa = null;
		
	}
	
	private void init_global_variables(){
		global_variables_dummy_efa =
			new EFA("global_variables",this);
		this.addAutomaton(global_variables_dummy_efa);
		global_variables_dummy_efa.addInitialState(
				"dummy_state_for_global_variables");
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
		//final int markedValue = 0;
		
		if(global_variables_dummy_efa == null){
			init_global_variables();
		}
		
		String name = PARALLEL_PREFIX + "_" + parallels;
		parallels = parallels + 1;
		
		global_variables_dummy_efa.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, null);
		
		return name;
	}
	
	/**
	 * Add new arbitrary node variable
	 * 
	 * @param efa
	 * @param upperBound
	 * @return name of new parallel variable created
	 */
	public String newArbitraryNodeInteger(int upperBound){
		
		final int lowerBound = 0;
		final int initialValue = 0;
		//final int markedValue = 0;
		
		if(global_variables_dummy_efa == null){
			init_global_variables();
		}
		
		String name = ARBITRARY_ORDER_PREFIX +"n_"+ arbitraryNode;
		arbitraryNode = arbitraryNode + 1;
		
		global_variables_dummy_efa.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, null);
		
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
		//final int markedValue = 0;
		
		if(global_variables_dummy_efa == null){
			init_global_variables();
		}
		
		String name = ARBITRARY_ORDER_PREFIX + "_" +arbitrary;
		arbitrary = arbitrary + 1;
		
		global_variables_dummy_efa.addIntegerVariable(
				name, lowerBound, upperBound, initialValue, null);
		
		return name;
	}
}
