package org.supremica.external.processeditor.xml;

import java.lang.Object;
import java.io.*;
import javax.xml.bind.*;
import javax.xml.transform.stream.StreamSource;

public class Loader {
	
	private final String PKGS = "org.supremica.manufacturingTables.xsd.processeditor";
	private final String PKGS_IL = "org.supremica.manufacturingTables.xsd.il";
	
	private JAXBContext jaxbContext;
    private Unmarshaller u;
    private Marshaller m;
    
    public Loader() {}
    
    public Object open(File file) {	
	
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS);
    		u = jaxbContext.createUnmarshaller();	    	    
    		return load(file);
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();
    		return null;
    	}
    }
    
    public Object openIL(File file) {
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS_IL);
    		u = jaxbContext.createUnmarshaller();	    	    
    		return load(file);
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();
    	}
    	return null;
    }
    
    
    public Object open(String xmlStr, String PKGS) {	
    	
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS);
    		u = jaxbContext.createUnmarshaller();
    		return load(xmlStr, PKGS);
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();
    		return null;
    	}
    } 
    
    private Object load(File file) {	
    	try {		
    		if(file!=null) {			
    			Object o = u.unmarshal(file);
    			return o;
    		}else {
    			java.lang.System.err.println("Problems reading the file!");
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
    
    public void saveIL(Object o, File file){
    	try {
    		// Save IL		
        	jaxbContext = JAXBContext.newInstance(PKGS_IL);    
        	m = jaxbContext.createMarshaller();
        	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true); 
    	    
        	try {				
        		m.marshal(o, new FileOutputStream(file));
        	}catch (Exception ex) {		
        		//System.out.println("ERROR! in FileOutputStrem " + PKGS_IL);	
        	}
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();
    	}
    }
    
    public void save(Object o, File file) {	
    	
    	try {
    		
    		//Save processeditor
    		jaxbContext = JAXBContext.newInstance(PKGS);    
    		m = jaxbContext.createMarshaller();
    		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	    
	    
    		try {				
    			m.marshal(o, new FileOutputStream(file));
    		}catch (Exception ex) {		
    			//System.out.println("ERROR! in FileOutputStrem " + PKGS);		
    		}
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();
    	}
    }
    
    public void createAutomatas(Object o, File file) {	
    	try {			
    		jaxbContext = JAXBContext.newInstance(PKGS);    
    		m = jaxbContext.createMarshaller();
    		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	    
    		m.marshal(o, System.out);	    
	    
    		try {				
    			m.marshal(o, new FileOutputStream(file));	    
    		}catch (Exception ex) {		
    			System.out.println("ERROR! in FileOutputStrem");		
    		}		      	    
    	}
    	catch(JAXBException je) {
    		je.printStackTrace();	    
    	}
    }       
}
