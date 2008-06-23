package org.supremica.external.avocades.xml;

import java.lang.Object;
import java.io.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

public class Loader {
	
	private final String PKGS_ROP = "org.supremica.manufacturingTables.xsd.processeditor";
	private final String PKGS_IL  = "org.supremica.manufacturingTables.xsd.il";
	private final String PKGS_EOP = "org.supremica.manufacturingTables.xsd.eop";
	
	private JAXBContext jaxbContext;
    private Unmarshaller u;
    private Marshaller m;
    
    private boolean printDebugInfo = true;
    
    public Loader() {
    	jaxbContext = null;
        u = null;
        m = null;
    }
    
    public Object open( String xmlStr ) {	
    	Object o = null;
    	
    	printDebugInfo = false;
    	
    	//ROP
        o = open( xmlStr, PKGS_ROP );
    	if( null != o){
    		
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//EOP
    	o = open( xmlStr, PKGS_EOP );
    	if( null != o){
    		
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//IL
    	o = open( xmlStr, PKGS_IL );
    	if( null != o){
    		
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//Unknown object
    	return o; 
    } 

    private Object open(String xmlStr, String PKGS) {	
    	
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS);
    		u = jaxbContext.createUnmarshaller();
    		return load(xmlStr, PKGS);
    	}
    	catch(JAXBException je) {
    		if(printDebugInfo){
    			je.printStackTrace();
    		}
    		return null;
    	}
    } 
    
    private Object load(String xmlStr, String PKGS) {	
    	try {		
    		if(xmlStr!=null) {			
    			JAXBContext jc = JAXBContext.newInstance(PKGS);
				Unmarshaller u = jc.createUnmarshaller();
				StringBuffer xmlStrB = new StringBuffer(xmlStr);
				Object o = u.unmarshal(new StreamSource(new StringReader(xmlStrB.toString()))); 
    			return o;
    		}else {
    			java.lang.System.err.println("Problem reading the XML string!");
    		}
    	}catch(UnmarshalException ue) {
    		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
    		ue.printStackTrace();
    	}catch(JAXBException je) {
    		java.lang.System.err.println("JAXBException caught!");
    		je.printStackTrace();
    	}
    	return null;
    }
}
