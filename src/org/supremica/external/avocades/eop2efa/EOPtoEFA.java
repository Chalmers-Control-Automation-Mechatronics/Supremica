package org.supremica.external.avocades.eop2efa;

import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.external.avocades.common.*;

public class EOPtoEFA {


	//States
	protected final static String INITIAL_STATE = "init";
	protected final static String EXCECUTION_STATE = "exec";
	protected final static String END_STATE = "comp";
	
	//event prefix
	protected final static String EVENT_START_PREFIX = "sta_";
	protected final static String EVENT_STOP_PREFIX = "sto_";
	
	private Module module = null;
	
	public EOPtoEFA(){
		module = new Module("EOPs", false);
	}
	
	public void add(EOP eop){
	    EFA	efa = null;
	    
	    EGA start = null;
		EGA stop = null;
		
		//Sanity check
		if(null == eop){
			return;
		}
		
		//Build EFA
	    efa = new EFA( eop.getId(), module );
	    module.addAutomaton(efa);
	    
	    //Add states
	    efa.addInitialState( INITIAL_STATE );
	    efa.addState( EXCECUTION_STATE );
	    efa.addState( END_STATE );
	    
		//Build event
		start = new EGA(); //stop event
		stop = new EGA(); //start event
		
		start.setEvent( EVENT_START_PREFIX + eop.getId() );
		stop.setEvent( EVENT_STOP_PREFIX + eop.getId() );
		
	    //Add transitions
		efa.addTransition( INITIAL_STATE, EXCECUTION_STATE,
						   start.getEvent(),
						   start.getGuard(),
						   start.getAction());

		efa.addTransition( EXCECUTION_STATE, END_STATE, 
					       stop.getEvent(),
					       stop.getGuard(),
					       stop.getAction()); 
	}
	
	public Module getModule(){
		return module;
	}
}
