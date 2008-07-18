package org.supremica.external.avocades.eop2efa;

import org.supremica.manufacturingTables.xsd.eop.EOP;

import org.supremica.external.avocades.common.Module;
import org.supremica.external.avocades.common.EFA;
import org.supremica.external.avocades.common.EGA;

import static org.supremica.external.avocades.AutomataNames.OPERATION_START_PREFIX;
import static org.supremica.external.avocades.AutomataNames.OPERATION_STOP_PREFIX;

public class EOPtoEFA {


	//States
	protected final static String INITIAL_STATE_POSTFIX = "_init";
	protected final static String EXECUTION_STATE_POSTFIX = "_exec";
	protected final static String END_STATE_POSTFIX = "_comp";
	
	private Module module = null;
	
	public EOPtoEFA(){
		module = new Module("EOPs", false);
	}
	
	public void add(EOP eop){
	    EFA	efa = null;
	    
	    EGA start = null;
		EGA stop = null;
		
		final String initialState;
		final String executionState;
		final String endState;
		
		//Sanity check
		if(null == eop){
			return;
		}
		
		initialState   = eop.getId() + INITIAL_STATE_POSTFIX;
		executionState = eop.getId() + EXECUTION_STATE_POSTFIX;
		endState = eop.getId() + END_STATE_POSTFIX;
		
		//Build EFA
	    efa = new EFA( eop.getId(), module );
	    module.addAutomaton(efa);
	    
	    //Add states
	    efa.addInitialState( initialState );
	    efa.addState( executionState );
	    efa.addState( endState );
	    
		//Build event
		start = new EGA(); //stop event
		stop = new EGA(); //start event
		
		start.setEvent( OPERATION_START_PREFIX + eop.getId() );
		stop.setEvent( OPERATION_STOP_PREFIX + eop.getId() );
		
	    //Add transitions
		efa.addTransition( initialState, executionState,
						   start.getEvent(),
						   start.getGuard(),
						   start.getAction());

		efa.addTransition( executionState, endState, 
					       stop.getEvent(),
					       stop.getGuard(),
					       stop.getAction()); 
	}
	
	public Module getModule(){
		return module;
	}
}
