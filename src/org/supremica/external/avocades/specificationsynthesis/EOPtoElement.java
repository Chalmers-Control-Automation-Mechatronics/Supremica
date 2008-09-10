/*
 * This class contains functions to build an Element from EOP 
 * 
 * This class is an small step toward the input to the 
 * specification synthes algorithm made by Kristin Andersson
 * 
 */

package org.supremica.external.avocades.specificationsynthesis;

import org.jdom.Element;

import org.supremica.external.avocades.xml.SpecificationSynthesXML;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.eop.Action;
import org.supremica.manufacturingTables.xsd.eop.ActuatorValue;
import org.supremica.manufacturingTables.xsd.eop.SensorValue;
import org.supremica.manufacturingTables.xsd.eop.InitialState;

//-----------------------------------------------------------------------------
//
//Imported constants
//
//-----------------------------------------------------------------------------

import static org.supremica.external.avocades.AutomataNames.EVENT_MACHINE_SEPARATOR;

class EOPtoElement
               extends
                   SpecificationSynthesXML
{
	
	//private elements
	private static Element process = null;
	private static Element event = null;
	private static Element restriction = null;
	private static Element and = null;
	
	//Empty constructor
	EOPtoElement(){}
	
	/**
	 * 
	 * Function to create an instance of Element from an instance of EOP
	 * 
	 */
	public static Element createElement( EOP eop ){
		
		Element element = null;
		
		//Sanity check
		if( eop == null ){
			return null;
		}
		
		//init
		element = new Element( OPERATION );
		
		//Add attributes
		if( isValidString( eop.getId() ) ){
		    element.setAttribute( ID , eop.getId() );
		}
		
		//Add initial process
		element.addContent( createElement( eop.getInitialState(), eop.getId() ) );
		
		//Add processes from action
		for( Action action : eop.getAction() ){
			element.addContent( createElement( action ) );
		}
		
		return element;
	}
	
	
	
	
	/**
	 * 
	 * Function to initialize and create elements. Should
	 * be done before elements are used, or to be sure they
	 * are empty.
	 * 
	 */
	private static void init(){
		
		//create elements
		process = new Element( PROCESS );
		event = new Element( EVENT );
		restriction = new Element( RESTRICTION );
		and = new Element( AND );
	}
	
	
	
	
	/**
	 * 
	 * Creates an Process element from an InitialState object
	 * 
	 * @param initialState
	 * @return the initial state represented in a Element
	 */
	private static Element createElement(final InitialState initialState, final String id){
	
		final String ID_PREFIX = "init";
		final String noMachineNameId;
		
		Element element = null;
		
		//Sanity check
		if(initialState == null){
			return null;
		}
		
		init();
		
		noMachineNameId = removeMachineName(id);
		
		//add attributes
		if( isValidString( ID_PREFIX + id ) ){
		    process.setAttribute( ID , ID_PREFIX + noMachineNameId );
		    event.setAttribute( ID , ID_PREFIX + noMachineNameId );
		}
		
		//Create structure
		process.addContent( event );
		event.addContent( restriction );
		restriction.addContent( and );
		
		//add states
		//Actuator values
		for( ActuatorValue actVal : initialState.getActuatorValue() ){
			
			element = createElement( actVal );
			if( element != null ){
				and.addContent( element );
			}
		}
		
		//Sensor values
		for(SensorValue sensVal : initialState.getSensorValue()){
			
			element = createElement( sensVal );
			if( element != null ){
			    and.addContent( createElement( sensVal ) );
			}
		}
		
		
		return process;
	}
	
	private static String removeMachineName(String str){
		
		if (!str.contains( EVENT_MACHINE_SEPARATOR )){
			return str;
		}
		
		return str.substring(0, str.indexOf( EVENT_MACHINE_SEPARATOR ));
		
	}
	
	
	/**
	 * 
	 * Creates an Process element from an Action object
	 * 
	 * @param action
	 * @return action represented in an Element
	 */
	private static Element createElement( Action action){
	
		final String ACTION = "action";
		
		Element element = null;
		
		if(action == null){
			return null;
		}
		
		init();
		
		//add attributes
		if( isValidString( ACTION + action.getActionNbr() ) ){
		    process.setAttribute( ID , ACTION + action.getActionNbr() );
		}
		
		if( isValidString( action.getId() ) ){
		    event.setAttribute( ID , action.getId() );
		}
		
		//Create structure
		process.addContent( event );
		event.addContent( restriction );
		restriction.addContent( and );
		
		
		//Actuator values
		for( ActuatorValue actVal : action.getActuatorValue() ){
			
			element = createElement( actVal );
			if( element != null ){
				and.addContent( element );
			}
		}
		
		//Sensor values
		for(SensorValue sensVal : action.getSensorValue()){
			
			element = createElement( sensVal );
			if( element != null ){
			    and.addContent( createElement( sensVal ) );
			}
		}
		
		return process;
	}
	
	
	/**
	 * 
	 * Creates an Element from an instance of ActuatorValue
	 * 
	 * @param actVal
	 * @return actVal represented in an Element
	 */
	private static Element createElement( ActuatorValue actVal ){
		
		Element state = null;
		
		//Sanity check
		if(actVal == null){
			return null;
		}
		
		
		/*
		 *  if actuator contains a valid value 
		 *  a new Element are created
		 * 
		 */
		if( isValidString( actVal.getValue() ) ){
			
			state = new Element( STATE );
			state.setAttribute( NAME, actVal.getActuator() );
			state.setAttribute( ID, actVal.getValue() );
		
		}else{
			return null;
		}
		
		return state;
	}
	
	
	
	/**
	 * 
	 * Creates an Element from an instance of an SensorValue
	 * 
	 * @param sensVal
	 * @return
	 */
	private static Element createElement( SensorValue sensVal ){
		
		Element state = null;
		
		//Sanity check
		if(sensVal == null){
			return null;
		}
		
		/*
		 *  if sensor contains a valid value 
		 *  a new Element are created
		 * 
		 */
		if( isValidString( sensVal.getValue() ) ){
			
			state = new Element( STATE );
			state.setAttribute( NAME, sensVal.getSensor() );
			state.setAttribute( ID, sensVal.getValue() );
		
		}else{
			return null;
		}
		
		return state;
	}
	
	
	
	
	
	
	
}
