/*
 *  This class builds an Element from an IL object. It is an
 *  help class to SpecificationSynthesInputBuilder
 *  
 */

package org.supremica.external.specificationSynthesis.algorithms;

import org.jdom.Element;

import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.Term;
import org.supremica.manufacturingTables.xsd.il.ActuatorValue;
import org.supremica.manufacturingTables.xsd.il.SensorValue;

class ILtoElement
              extends
                  SpecificationSynthesXML
{
	
	//Local elements
	private static Element eventInterlocking = null;
	private static Element event = null;
	private static Element restriction = null;
	private static Element or = null;
	private static Element and = null;
	
	//Empty constructor
	ILtoElement(){}
	
	/**
	 * 
	 * Creates an Element from an IL object
	 * 
	 * @param il
	 * @return
	 */
	public static Element createElement( IL il ){
		
		if(il == null){
			return null;
		}
		
		init();
		
		//add attributes
		eventInterlocking.setAttribute( ID , il.getComment() );
		eventInterlocking.setAttribute( TYPE , "safe");
		
		event.setAttribute( ID , "" );
		
		//Create tree structure
		eventInterlocking.addContent( event );
		event.addContent( restriction );
		restriction.addContent( or );
		
		//Add and elements to or from Term objects
		for( Term term : il.getILStructure().getTerm() ){
			or.addContent( buildElement( term ) );
		}
		
		//Add to ilseops
		return eventInterlocking;
	}
	
	/**
	 * 
	 * Initialization of elements
	 * 
	 */
	private static void init(){
		
		//Initialize elements
		eventInterlocking = new Element( EVENT_IL );
		event = new Element( EVENT );
		restriction = new Element( RESTRICTION );
		or = new Element( OR );
		and = new Element( AND );
	}
	
	/**
	 * 
	 * Parses a Term object and creates an Element.
	 * 
	 * @param term
	 * @return
	 */
	private static Element buildElement(Term term){
		
		Element tmp = null;
		
		//Sanity check
		if(term == null){
			return null;
		}
		
		//init
		and = new Element( AND );
		
		//Internal state from actuators
		for( ActuatorValue actVal : term.getActuatorValue() ){
		
			tmp = createElement( actVal );
			if( tmp != null ){
				and.addContent( tmp );
			}
		}
		
		//Internal state from sensors
		for( SensorValue sensVal : term.getSensorValue() ){
			
			tmp = createElement( sensVal );
			if( tmp != null ){
	            and.addContent( createElement( sensVal ) );
			}
		}
		
		return and;
	}
	

	
	/**
	 * 
	 * Creates an Element from an instance of an ActuatorValue
	 * 
	 * @param actVal
	 * @return
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
			
			state = new Element( INT_STATE );
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
		if( sensVal == null ){
			return null;
		}
		
		/*
		 *  if sensor contains a valid value 
		 *  a new Element are created
		 * 
		 */
		if( isValidString( sensVal.getValue() ) ){
			
			state = new Element( INT_STATE );
			state.setAttribute( NAME, sensVal.getSensor() );
			state.setAttribute( ID, sensVal.getValue() );
		
		}else{
			return null;
		}
		
		return state;
	}
}
