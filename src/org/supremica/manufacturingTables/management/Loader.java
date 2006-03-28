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
 * Created: Tue Nov  23 13:49:32 2005
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;

import java.util.Iterator;
import java.util.List;
import java.io.*;
import javax.xml.bind.*;
import org.supremica.manufacturingTables.xsd.factory.*;
import org.supremica.functionblocks.xsd.libraryelement.*;
import org.supremica.functionblocks.xsd.libraryelement.impl.*;


public class Loader
{
    private JAXBContext jaxbContext;
    private Unmarshaller u;


    public Loader()
    {
	try
	    {
		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory");
		//System.err.println("jaxbcontext created");
		u = jaxbContext.createUnmarshaller();
		//System.err.println("unmarshaller created");
		// enable validation
		u.setValidating( true );
		System.err.println("validation is set to true");

		// We will allow the Unmarshaller's default
		// ValidationEventHandler to receive notification of warnings
		// and errors which will be sent to java.lang.System.err.  The default
		// ValidationEventHandler will cause the unmarshal operation
		// to fail with an UnmarshalException after encountering the
		// first error or fatal error.
	    }
	catch(JAXBException je)
	    {
		je.printStackTrace();
	    }
    }

    public Object load(String path, String fileName)
    {
	try
	    {
		File theFile = getFile(path, fileName);

		if(theFile!=null)
		    {
			// Unmarshall from the file
			Object o = u.unmarshal(theFile);
			java.lang.System.err.println("The file is unmarshalled");

			//return (FactoryType) o;
			return o;
			//buildPLCProgram((FactoryType) o);
		    }
		else
		    {
			java.lang.System.err.println("Problems reading the file!");
		    }
	    }
	catch(UnmarshalException ue)
	    {
		java.lang.System.err.println("Invalid XML code (UnmarshalException)" );
		ue.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		java.lang.System.err.println("JAXBException caught!");
		je.printStackTrace();
	    }
	return null;
    }


    private File getFile(String path, String fileName)
    {
	File theFile = new File(fileName);
	if (path !=null)
	    {
		File pathFile = new File(path);
		if (pathFile.isDirectory())
		    {
			theFile = new File(path, fileName);
		    }
		else
		    {
			java.lang.System.err.println("The specified directory is invalid!");
			return null;
		    }
	    }
	if (theFile.isFile())
	    {
		return theFile;
	    }
	else
	    {
		java.lang.System.err.println("The file does not exist in the specified directory!");
		return null;
	    }
    }
}
