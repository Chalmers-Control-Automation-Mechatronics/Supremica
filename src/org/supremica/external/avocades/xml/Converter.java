package org.supremica.external.avocades.xml;

import org.supremica.external.avocades.xml.Loader;

import org.jdom.Document;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;

public class Converter {
	
	public static ROP convertJDomCOPDocumentToJAXBCOPObject( Document cop ){
		
		Loader loader = new Loader();
		Object o = loader.open( cop.toString() );
		
		if( o instanceof ROP ){
			return (ROP) o;
		}
		
		return null;
	}
}
