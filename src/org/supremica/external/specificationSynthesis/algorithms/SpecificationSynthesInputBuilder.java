/*
 * 
 * 
 * 
 * 
 * This class converts ROP and IL classes to appropriate input to
 * the specification synthesis algorithm made by Kristin Andersson
 * 
 * 
 * 
 * 
 * 
 * 
 * 
 */

package org.supremica.external.specificationSynthesis.algorithms;

import org.jdom.Document;
import org.jdom.Element;

import org.supremica.manufacturingTables.xsd.il.IL;
import org.supremica.manufacturingTables.xsd.eop.EOP;

public class SpecificationSynthesInputBuilder 
                                         extends
                                           SpecificationSynthesXML
{

	//Document input to specification synthes algorithm
	private Document docILEOP = null;
	
	private Element ilseops = null;

	/**
	 * Constructor
	 */
	public SpecificationSynthesInputBuilder() {
		init();
	}
	
	/*-----------------------------------------------------------------------*/
	/*
	/* Public functions
	/*  
	/*-----------------------------------------------------------------------*/
	
	
	/**
	 * 
	 * Function to add EOP to builder
	 * 
	 */
	public void addEOP( EOP eop, String opID ) {
		
		//Sanity check
		if( eop == null ){
			return;
		}
		
		ilseops.addContent( EOPtoElement.createElement( eop, opID ) );
	}

	/**
	 * Function to add IL to builder
	 * 
	 */
	public void addIL( IL il ) {
		
		//Sanity check
		if( il == null ){
			return;
		}
		
		ilseops.addContent( ILtoElement.createElement( il, false ) );
	}
	
	/**
	 * Function to add IL as a Robot IL to builder
	 * 
	 */
	public void addRobotIL( IL il ) {
		
		//Sanity check
		if( il == null ){
			return;
		}
		
		ilseops.addContent( ILtoElement.createElement( il, true ) );
	}
	
	/**
	 * 
	 * Returns the Document containing EOPs and ILs
	 * to the specification algorithm
	 * 
	 */
	public Document getDoc() {
		return docILEOP;
	}
	
	/*-----------------------------------------------------------------------*/
	/*
	/*    Private functions
	/* 
	/*-----------------------------------------------------------------------*/
	
	/**
	 * 
	 *	Initialization of internal variables
	 * 
	 */
	private void init() {
		ilseops = new Element( ILSEOPS );
		docILEOP = new Document( ilseops );
		placeSimultaneity();
	}
	
	/**
	 * 
	 * Function to place dummy Simultaneity
	 * to docILEOP. Used to avoid problem with
	 * old xml structure.
	 * 
	 */
	private void placeSimultaneity() {
		
		//create element
		Element sim = new Element( SIMULTANEITY );
		Element proc1 = new Element( PROCESS );
		Element proc2 = new Element( PROCESS );
		
		//set attributes
		proc1.setAttribute( ID , "OA" );
		proc2.setAttribute( ID , "OB" );
		
		sim.addContent( proc1 );
		sim.addContent( proc2 );
		
		ilseops.addContent( sim );
	}
}