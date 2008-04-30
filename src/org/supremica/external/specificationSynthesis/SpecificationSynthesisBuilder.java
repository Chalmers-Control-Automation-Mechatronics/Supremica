/*
 * 
 * Test class for the specification system input builder class
 * 
 * 
 * 
 * 
 * 
 */

package org.supremica.external.specificationSynthesis;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.*;

import java.util.*;

import org.jdom.Document;
import org.jdom.output.XMLOutputter;
import org.supremica.external.specificationSynthesis.algorithms.*;
import org.supremica.external.specificationSynthesis.gui.*;

import org.supremica.external.processeditor.xml.Loader;

import org.supremica.manufacturingTables.xsd.eop.EOP;
import org.supremica.manufacturingTables.xsd.il.IL;

public class SpecificationSynthesisBuilder{

	private static final String PATH = "C://david//JAXB//";
	
	private static String testEOPfile = "eop_33.xml";
	private static String testILfile = "il_151Y13.xml";
	
	private static String outFile = "out.xml";
	
	//empty constructor
    public SpecificationSynthesisBuilder() {}

    
    public static void saveDoc(Document d)
	{
		Document doc = d;
		
		try
		{
			XMLOutputter outp = new XMLOutputter();
			outp.setFormat(org.jdom.output.Format.getPrettyFormat());

			FileOutputStream fileStream = new FileOutputStream(PATH + outFile);

			outp.output(doc, fileStream);
		}
		catch (FileNotFoundException e) {
			System.out.println("No file");
		}
		catch (IOException sxe) {

		}
	}
    
	public static void main(String[] args) {
		
		SpecificationSynthesInputBuilder builder;
		builder = new SpecificationSynthesInputBuilder();
		
		Loader loader = new Loader();
		
		Object o;
		
		o = loader.openEOP(new File(PATH + testEOPfile));
		builder.addEOP( (EOP)o );
		
		o = loader.openIL(new File(PATH + testILfile));
		builder.addIL( (IL)o );
		
		saveDoc(builder.getDoc());
    }
}