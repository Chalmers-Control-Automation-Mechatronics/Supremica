/*
 *  This class contains functions to build an Element from IL 
 * 
 * This class is an small step toward the input to the 
 * specification synthes algorithm made by Kristin Andersson
 *  
 */

package org.supremica.external.specificationSynthesis.algorithms;

import org.jdom.Element;

import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.il.Term;
import org.supremica.manufacturingTables.xsd.il.ActuatorValue;
import org.supremica.manufacturingTables.xsd.il.SensorValue;
import org.supremica.manufacturingTables.xsd.il.ExternalComponentValue;
import org.supremica.manufacturingTables.xsd.il.OperationCheck;

class ILtoElement
              extends
                  SpecificationSynthesXML
{
	
	//Local elements
	private static Element interlock = null;
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
	    return createElement( il , false );
	}
	
	/**
	 * 
	 * Creates an Element from an IL object, if robot is true
	 * an robot interlock is created else an event interlock is 
	 * created
	 * 
	 * @param il
	 * @return
	 */
	public static Element createElement( IL il , boolean robot ){
		
		//Sanity check
		if( il == null ){
			return null;
		}
		
		init();
		
		if( robot ){
			interlock = new Element( ROBOT_IL );
		}
		
		//add attributes
		interlock.setAttribute( ID , il.getId() );
		interlock.setAttribute( TYPE , "safe");
		
		//Create tree structure
		interlock.addContent( event );
		event.addContent( restriction );
		restriction.addContent( or );
		
		//Add and elements to or from Term objects
		for( Term term : il.getILStructure().getTerm() ){
			or.addContent( buildElement( term ) );
		}
		
		//Add to ilseops
		return interlock;
	}
	
	
	
	/**
	 * 
	 * Initialization of elements
	 * 
	 */
	private static void init(){
		
		//Initialize elements
		interlock = new Element( EVENT_IL );
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
	private static Element buildElement( Term term ){
		
		Element tmp = null;
		
		//Sanity check
		if( term == null ){
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
	            and.addContent( tmp );
			}
		}
		
		
		//External component state
		for( ExternalComponentValue extVal : term.getExternalComponentValue() ){
			
			tmp = createElement( extVal );
			if( tmp != null ){
	            and.addContent( tmp );
			}
		}
		
		
		//Operations state
		for( OperationCheck opCheck : term.getOperationCheck() ){
			
			//Add not ongoing operations
			if( null != opCheck.getNotOngoing() ){
			    for( String op : opCheck.getNotOngoing().getOperation() ){
			    	if( isValidString( op ) ){
						
						tmp = new Element( OP_NOT_ONGOING  );
						tmp.setAttribute( NAME, op );
			            
						and.addContent( tmp );
					}
			    }
			}
			
			//Add not started operations
			if( null != opCheck.getNotOngoing() ){
			    for( String op : opCheck.getNotStarted().getOperation() ){
			    	if( isValidString( op ) ){
						
						tmp = new Element( OP_NOT_ONGOING  );
						tmp.setAttribute( NAME, op );
			            
						and.addContent( tmp );
					}
			    }
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
		if( null == actVal ){
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
	
	
	
	
	
	
	
	/**
	 * 
	 * Creates an Element from an instance of an ExternalCompopnentValue
	 * 
	 * @param sensVal
	 * @return
	 */
	private static Element createElement( ExternalComponentValue extVal ){
		
		Element state = null;
		
		//Sanity check
		if( extVal == null ){
			return null;
		}else if( extVal.getExternalComponent() == null ){
			return null;
		}
		
		/*
		 *  if external component value contains a valid value 
		 *  a new Element are created
		 * 
		 */
		if( isValidString( extVal.getValue() ) ){
			
			state = new Element( EXT_STATE );
			state.setAttribute( NAME, extVal.getExternalComponent().
                                                              getComponent() );
			state.setAttribute( ID, extVal.getValue() );
		
		}else{
			return null;
		}
		
		return state;
	}
	
	
	
}
