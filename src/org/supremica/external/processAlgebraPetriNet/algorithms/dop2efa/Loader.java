package org.supremica.external.processAlgebraPetriNet.algorithms.dop2efa;

import java.lang.Object;
import java.util.Iterator;
import java.util.List;
import java.io.*;

import javax.xml.bind.*;

import org.supremica.manufacturingTables.xsd.rop_copvision.*;

//OBS
//class to save and open files in XML format
//use org.supremica.manufacturingTables.management.Loader;
//then it is done
//OBS

public class Loader {
    
    private final String PKGS = "org.supremica.manufacturingTables.xsd.rop_copvision";
    
    private JAXBContext jaxbContext;
    private Unmarshaller u;
    private Marshaller m;
    
    public Loader() {}
    
    //open a file
    public Object open(File file) {	
		//DEBUG
		System.out.println("Loader.open()");
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

    //used by open    
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
    
    //convert Object and save as XML file
    public void save(Object o, File file) {	
		try {			
	    	jaxbContext = JAXBContext.newInstance(PKGS);    
	    	m = jaxbContext.createMarshaller();
	    	m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);	    
	    
            //print to standard output
            m.marshal(o, System.out);
	    
            try {		
				m.marshal(o, new FileOutputStream(file));	    
	    	}catch (Exception ex) {
				//DEBUG
				System.out.println("ERROR! in FileOutputStrem");
				//END DEBUG
	    	}		      	    
		}
		catch(JAXBException je) {
            System.out.println("JAXBException caught!");
	    	je.printStackTrace();	    
		}
    }       
}
