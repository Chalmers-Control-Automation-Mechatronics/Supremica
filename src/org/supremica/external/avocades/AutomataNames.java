/******************************************************************************
 *  
 *  This class contains common names for building automatons to be handled by
 *  algorithms in this package. 
 *  
 ******************************************************************************/

package org.supremica.external.avocades;

public final class AutomataNames {
	//Operation prefix
	public static final String OPERATION_START_PREFIX = "sta_";
	public static final String OPERATION_STOP_PREFIX = "sto_";
	
	//Relation prefix
	public static final String RELATION_START_PREFIX = "rel_sta_";
	public static final String RELATION_STOP_PREFIX = "rel_sto_";
	
	
	public static final String EVENT_MACHINE_SEPARATOR = "::";
	public static final String RUNNING_STATE_SUFFIX = "_run";
	
	public static final String PRECON_NOT_FULFILLED_STATE = "precon_not_fulfilled";
	public static final String PRECON_FULFILLED_STATE = "precon_fulfilled";
}
