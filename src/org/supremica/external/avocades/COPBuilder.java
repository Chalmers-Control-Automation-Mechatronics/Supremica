package org.supremica.external.avocades;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.LinkedList;

import org.jdom.Document;
import org.jdom.Element;

import org.supremica.external.avocades.relationextraction.Extractor;
import org.supremica.external.avocades.specificationsynthesis.ConverterILtoAutomata;
import org.supremica.external.avocades.specificationsynthesis.SpecificationSynthesInputBuilder;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

import org.supremica.external.avocades.xml.Converter;

import org.supremica.external.avocades.common.Module;
import org.supremica.external.avocades.dop2efa.DOPtoEFA;

public class COPBuilder {
	
	List<ROP> ropList = null;
	List<EOP> eopList = null;
	List<IL>  ilList  = null;
	
	/**
	 * Constructor
	 */
    public COPBuilder(){
        ropList = new ArrayList<ROP>();
        eopList = new LinkedList<EOP>();
        ilList = new LinkedList<IL>();
    }
    
    //-----------------------------------------------------------------------//
    // add
    //-----------------------------------------------------------------------//
    
    /**
     * Add new ROP to COPBuilder
     * @param rop - the ROP instance to be added
     */
    public void add( ROP rop ){
    	
    	//Sanity check
    	if( null == rop || ropList.contains( rop ) ){
    		return;
    	}
    	
    	ropList.add( rop );
    }
    
    /**
     * 
     * Add new EOP to COPBuilder
     * @param eop - the EOP instance to be added
     * 
     */
    public void add( EOP eop ){
    	
    	//Sanity check
    	if( null == eop || eopList.contains( eop ) ){
    		return;
    	}
    	
    	eopList.add( eop );
    }
    
    /**
     * 
     * Add new IL to COPBuilder
     * @param il - the IL instance to be added
     * 
     */
    public void add( IL il ){
    	
    	//Sanity check
    	if( null == il || ilList.contains( il ) ){
    		return;
    	}
    	
    	ilList.add( il );
    }
    
    //-----------------------------------------------------------------------//
    // get
    //-----------------------------------------------------------------------//
    public Document getSpecificationSynthesisOutput(){
    	
    	ConverterILtoAutomata convAut = null;
    	SpecificationSynthesInputBuilder builder = null;
    	
    	builder = new SpecificationSynthesInputBuilder();
    	
    	for( EOP eop : eopList ){
    		builder.add( eop );
    	}
    	
    	for( IL il : ilList ){
    		builder.add( il );
    	}
    	
    	convAut = new ConverterILtoAutomata();
		convAut.convertILtoAutomata( builder.getDoc() );
		
		return convAut.getDoc();
    }
    
    public Module getDOPtoEFAOutput(){
    	String moduleName = "DOPtoEFA";
    	return DOPtoEFA.buildModuleFromROP(ropList, moduleName, false); 
    }
    
    //TODO: Make this work, unfinished
    public List<ROP> getRelationExtractionOutput(){
    	
    	Extractor extractor = null;
    	Document supDoc = null;
    	ArrayList<ROP> ropList = null;
    	List<ROP> copList = null;
    	
    	/*
    	 * 1. Create Supervisor document.
    	 */
    	supDoc = new Document();
    	
    	/*
    	 * 2. Extract relation 
    	 */
    	
    	/*
    	 * Observe that the automaton representation of the supervisor must be
    	 * such that the states of the operation models are unique, and
    	 * separated by a dot in the supervisor states; 
    	 */
    	extractor = new Extractor();
		ropList = extractor.extractRestrictions( supDoc , ropList );

		/*
		 * 3. Fill COP list
		 */
		copList = new ArrayList<ROP>();
		for( Iterator cIter = ropList.iterator(); cIter.hasNext(); ){
			Document cop = (Document) cIter.next();
			copList.add( Converter.convertJDomCOPDocumentToJAXBCOPObject( cop ) );
		}
		
		return copList;
    }
    
    
}
