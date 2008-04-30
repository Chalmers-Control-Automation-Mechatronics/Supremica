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

import java.io.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;

import org.supremica.manufacturingTables.xsd.il.*;
import org.supremica.manufacturingTables.xsd.eop.*;

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
	public void addEOP( EOP eop ){
		ilseops.addContent( EOPtoElement.createElement( eop ) );
	}

	/**
	 * Function to add IL to builder
	 * 
	 */
	public void addIL( IL il ){
		ilseops.addContent( ILtoElement.createElement( il ) );
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
	private void init(){
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
	private void placeSimultaneity()
	{
		Element sim = new Element( SIMULTANEITY );
		Element proc1 = new Element( PROCESS );
		
		proc1.setAttribute( ID , "OA" );
		sim.addContent( proc1 );
		
		Element proc2 = new Element( PROCESS );
		proc2.setAttribute( ID , "OB");
		sim.addContent(proc2);
		ilseops.addContent(sim);
	}
}