package org.supremica.external.avocades.xml;

import java.lang.Object;
import java.io.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

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
    
    /**
     * Creates JAXB object from a <code>String</code>
     * @param xmlStr XML <code>String</code> to be parsed
     * @return JAXB object <code>ROP</code>, <code>EOP</code>, <code>IL</code>.
     *         <code>null</code> 
     */
    public Object open( String xmlStr ) {	
    	Object o = null;
    	
    	printDebugInfo = false;
    	
    	//ROP
        o = open( xmlStr, PKGS_ROP );
    	if (o instanceof ROP){
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//EOP
    	o = open( xmlStr, PKGS_EOP );
    	if(o instanceof EOP){
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//IL
    	o = open( xmlStr, PKGS_IL );
    	if (o instanceof IL){
    		printDebugInfo = true;
    		return o;
    	}
    	
    	//Unknown object
    	return o; 
    }
    
    public EOP openEOP(String xmlStr){
    	
    	final Object o;
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS_EOP);
    		u = jaxbContext.createUnmarshaller();
    		o = load(xmlStr, PKGS_EOP);
    	}
    	catch(JAXBException je) {
    		if(printDebugInfo){
    			je.printStackTrace();
    		}
    		return null;
    	}
    	
    	if(o instanceof EOP){
    		return (EOP)o;
    	}
    	
    	return null;
    }
    
    public ROP openROP(String xmlStr){
    	
    	final Object o;
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS_ROP);
    		u = jaxbContext.createUnmarshaller();
    		o = load(xmlStr, PKGS_ROP);
    	}
    	catch(JAXBException je) {
    		if(printDebugInfo){
    			je.printStackTrace();
    		}
    		return null;
    	}
    	
    	if(o instanceof ROP){
    		return (ROP)o;
    	}
    	
    	return null;
    }
    
    public IL openIL(String xmlStr){
    	
    	final Object o;
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS_IL);
    		u = jaxbContext.createUnmarshaller();
    		o = load(xmlStr, PKGS_IL);
    	}
    	catch(JAXBException je) {
    		if(printDebugInfo){
    			je.printStackTrace();
    		}
    		return null;
    	}
    	
    	if(o instanceof IL){
    		return (IL)o;
    	}
    	
    	return null;
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
