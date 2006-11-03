/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

/**
 * The Loader class uses JAXB to load a Factory
 * application into a PLC program structure.
 *
 *
 * Created: Mon Dec  05 13:49:32 2005
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

//import java.util.Iterator;
//import java.util.List;
import java.io.*;
import javax.xml.bind.*;
//import net.sourceforge.fuber.xsd.libraryelement.*;


public class XMLCreator
{
    private JAXBContext jaxbContext;
    private Marshaller marshaller;

    public XMLCreator()
    {
	try
	    {
		//jaxbContext = JAXBContext.newInstance("net.sourceforge.fuber.xsd.libraryelement");
		jaxbContext = JAXBContext.newInstance("org.supremica.automationobjects.xsd.libraryelement");
		marshaller = jaxbContext.createMarshaller();
		//You can tell the Marshaller to format the resulting XML data with line breaks and indentation. The following statement turns this output format property on -- line breaks and indentation will appear in the output format:
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,new Boolean(true));
	    }
	catch(JAXBException je)
	    {
		je.printStackTrace();
	    }
    }

    public void createXMLFile(Object o, String path, String fileName)
    {
	try
	    {
		File file = new File(path, fileName);
		marshaller.marshal(o,new FileOutputStream(file));

		//Validation is not performed as part of the marshalling operation. In other words, unlike the case for unmarshalling, there is no setValidating method for marshalling. Instead, when marshalling data, you use the Validator class that is a part of the binding framework to validate a content tree against a schema. For example:

		Validator validator = jaxbContext.createValidator();
		validator.validate(o);
	    }
	catch(PropertyException pe)
	    {
		java.lang.System.err.println("Couldn´t set the desired marshal properties!");
		pe.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		java.lang.System.err.println("JAXBException caught!");
		je.printStackTrace();
	    }
	catch(FileNotFoundException fe)
	    {
		java.lang.System.err.println("Couldn´t create the file with the given path and filename!");
		fe.printStackTrace();
	    }
    }
}

