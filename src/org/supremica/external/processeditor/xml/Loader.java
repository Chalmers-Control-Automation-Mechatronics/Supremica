package org.supremica.external.processeditor.xml;

import java.lang.Object;
import java.util.Iterator;
import java.util.List;
import java.io.*;
import javax.xml.bind.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;

public class Loader {
	private final String PKGS = "org.supremica.manufacturingTables.xsd.processeditor";
    private JAXBContext jaxbContext;
    private Unmarshaller u;
    private Marshaller m;
    
    public Loader() {}
    
    public Object open(File file) {	
	//DEBUG
	//System.out.println("Loader.open()");
	//END DEBUG
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
    private Object load(File file) {	
	try {		
	    if(file!=null) {			
		Object o = u.unmarshal(file);
		java.lang.System.err.println("The file is unmarshalled");
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
    public void save(Object o, File file) {	
	try {			
	    jaxbContext = JAXBContext.newInstance(PKGS);    
	    m = jaxbContext.createMarshaller();
	    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	    
	    //DEBUG
	    //m.marshal(o, System.out);	    
	    //END DEBUG
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
