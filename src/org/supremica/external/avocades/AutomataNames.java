/******************************************************************************
 *  
 *  This class contains common names for building automatons to be handled by
 *  algorithms in this package. 
 *  
 ******************************************************************************/

package org.supremica.external.avocades;

public final class AutomataNames {
	
	public static final String STATE_INDICATOR = "_";
	
	public static final String STATE_SEPARATOR = ".";
	
	public static final String EVENT_MACHINE_SEPARATOR = "::";
	
	//States
	public static final String INITIAL_STATE_POSTFIX	= STATE_INDICATOR + "init";
	public static final String EXECUTION_STATE_POSTFIX	= STATE_INDICATOR + "exec";
	public static final String END_STATE_POSTFIX		= STATE_INDICATOR + "comp";
	public static final String DONT_CARE_STATE_POSTFIX	= STATE_INDICATOR + "-";
	
	/*
	 * The following constants can't contain 
	 * STATE_INDICATOR, 
	 * STATE_SEPARATOR and
	 * EVENT_MACHINE_SEPARATOR
	 * 
	 */
	
	//Operation prefix
	public static final String OPERATION_START_PREFIX	= "sta";
	public static final String OPERATION_STOP_PREFIX	= "sto";
	
	/*
	 * Don't care start and stop prefix are used instead of operation
	 * start and stop prefix so algorithms don't bother to parse.
	 */
	public static final String DONT_CARE_START_PREFIX	= "dcsta";
	public static final String DONT_CARE_STOP_PREFIX	= "dcsto";
	
	public static final String RUNNING_STATE_SUFFIX	= "run";
	
	public static final String PRECON_NOT_FULFILLED_STATE	= "preconNotFulfilled";
	public static final String PRECON_FULFILLED_STATE		= "preconFulfilled";
}
