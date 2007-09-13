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
	
	private final String PARALLEL_PREFIX = "pa_";
	
	private EFA global_variables_dummy_efa;
	   
	public Module(String name, boolean expand){
		super(name, expand);
		events = new LinkedList<String>();
		parallels = 0;
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
	public String newParrallelInteger(int upperBound){
		if(global_variables_dummy_efa == null){
			init_global_variables();
		}
		
		String tmp = PARALLEL_PREFIX + parallels;
		parallels = parallels + 1;
		
		global_variables_dummy_efa.addIntegerVariable(
				tmp, 0, upperBound, 0, null);
		
		return tmp;
	}
}
