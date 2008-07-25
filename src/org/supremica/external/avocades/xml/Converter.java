package org.supremica.external.avocades.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.UnmarshalException;
import javax.xml.transform.stream.StreamResult;

import org.supremica.external.avocades.xml.Loader;

import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

public class Converter {
	
	private static final String PKGS_ROP = "org.supremica.manufacturingTables.xsd.processeditor";
	private static final String PKGS_EOP = "org.supremica.manufacturingTables.xsd.eop";
	private static final String PKGS_IL = "org.supremica.manufacturingTables.xsd.il";
	
	private static final Loader loader = new Loader();
	
	/*-------------------------------------------------------------------------
	 * 
	 * Convert functions
	 * 
	 *-------------------------------------------------------------------------*/
	
	public static ROP convertToROP( Document doc ){
		
		final XMLOutputter outputter = new XMLOutputter();
		final StringWriter sw = new StringWriter();
		
		final Object o;
		
		//Sanity check
		if(null == doc){
			return null;
		}
		
		try{
			outputter.output(doc, sw );
		}catch (IOException e) {
			System.err.println(e);
		}
		
		o = loader.open( sw.toString() );
		
		if( o instanceof ROP ){
			return (ROP) o;
		}
		
		return null;
	}
	
	
	public static String convertToXMLString(ROP rop){
		
		final StreamResult sr = new StreamResult( new StringWriter() );
    	
    	try{
    		JAXBContext jc = JAXBContext.newInstance(PKGS_ROP);
    		Marshaller m = jc.createMarshaller();
    		
    		m.marshal(rop,  sr);
    		
    	}catch(UnmarshalException ue) {
    		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
    		ue.printStackTrace();
    	}catch(JAXBException je) {
    		java.lang.System.err.println("JAXBException caught!");
    		je.printStackTrace();
    	}
    	
		return sr.getWriter().toString();
	}
	
	public static String convertToXMLString(EOP eop){
		
		final StreamResult sr = new StreamResult( new StringWriter() );
    	
    	try{
    		JAXBContext jc = JAXBContext.newInstance(PKGS_EOP);
    		Marshaller m = jc.createMarshaller();
    		
    		m.marshal(eop,  sr);
    		
    	}catch(UnmarshalException ue) {
    		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
    		ue.printStackTrace();
    	}catch(JAXBException je) {
    		java.lang.System.err.println("JAXBException caught!");
    		je.printStackTrace();
    	}
    	
		return sr.getWriter().toString();
	}

	public static String convertToXMLString(IL il){
		
		final StreamResult sr = new StreamResult( new StringWriter() );
    	
    	try{
    		JAXBContext jc = JAXBContext.newInstance(PKGS_IL);
    		Marshaller m = jc.createMarshaller();
    		
    		m.marshal(il,  sr);
    		
    	}catch(UnmarshalException ue) {
    		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
    		ue.printStackTrace();
    	}catch(JAXBException je) {
    		java.lang.System.err.println("JAXBException caught!");
    		je.printStackTrace();
    	}
    	
		return sr.getWriter().toString();
	}
	
	public static Document convertToDocument(String xmlStr){
		
		Document doc = null;
		
		try{
			SAXBuilder inp = new SAXBuilder();
			inp.setExpandEntities(true);
			doc = inp.build( new StringReader( xmlStr ));
		}
		catch ( JDOMException e ) {
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		return doc;
	}
	
	public static Document convertToDocument(ROP rop){
		return convertToDocument( convertToXMLString( rop ) );
	}
	
	
	/*-------------------------------------------------------------------------
	 * 
	 * Copy functions
	 * 
	 *-------------------------------------------------------------------------*/
	
	public static ROP copy(final ROP rop){
		
		final Object o = loader.openROP( convertToXMLString( rop ) );
		
		if(o instanceof ROP){
			return (ROP)o;
		}
		
		return null;
	}
	
	public static EOP copy(final EOP eop){
		final Object o = loader.openEOP( convertToXMLString( eop ) );
		
		if(o instanceof EOP){
			return (EOP)o;
		}
		
		return null;
	}
	
	public static IL copy(final IL il){
		
		final Object o = loader.openIL( convertToXMLString( il ) );
		
		if(o instanceof IL){
			return (IL)o;
		}
		
		return null;
	}
}
