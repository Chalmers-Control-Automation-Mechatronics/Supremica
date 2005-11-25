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
 * Haradsgatan 26A
 * 431 42 Molndal
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

import java.io.File;
import java.io.FileInputStream;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import javax.xml.bind.*;
import org.supremica.manufacturingTables.xsd.factory.*;

import org.supremica.properties.SupremicaProperties;


public class Loader
{
    private JAXBContext jaxbContext;
    private Unmarshaller u;

    public Loader()
    {	
	try
	    {
		System.err.println("Entered the Loader constructor.");
		//		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory", this.getClass().getClassLoader());
		jaxbContext = JAXBContext.newInstance("org.supremica.manufacturingTables.xsd.factory");
		System.err.println("jaxbcontext skapat");
		u = jaxbContext.createUnmarshaller();
		System.err.println("unmarshaller skapat");
		// enable validation
		u.setValidating( true );
		System.err.println("validation satt till true");

		// We will allow the Unmarshaller's default
		// ValidationEventHandler to receive notification of warnings 
		// and errors which will be sent to System.out.  The default
		// ValidationEventHandler will cause the unmarshal operation
		// to fail with an UnmarshalException after encountering the
		// first error or fatal error.
	    }
	catch(JAXBException je)
	    {
		je.printStackTrace();
	    }
    }
    
    public void load(String path, String fileName)
    {
	try
	    {
		File theFile = getFile(path, fileName);
		System.err.println("filen inläst");

		if(theFile!=null)
		    {
			// Unmarshall from the file with the fileName
			Object o = u.unmarshal(theFile);
			System.err.println("filen unmarshallad");

			//return (FactoryType) o;
			buildPLCProgram((FactoryType) o);
		    }
		else
		    {
			System.err.println("Problems reading the file!");
		    }
	    }
	catch(UnmarshalException ue)
	    {
		System.out.println("Invalid XML code (UnmarshalException)" );
		ue.printStackTrace();
	    }
	catch(JAXBException je)
	    {
		System.err.println("JAXBException caucht!");
		je.printStackTrace();
	    }
	return;
	//	for (Iterator resIter = theDevice.getResource().iterator();resIter.hasNext();)
	
    }
    
    private void buildPLCProgram(FactoryType factory)
    {
	System.err.println("<Factory name=\"" + factory.getName() + ">");
	if(factory.getDescription()!=null)
	    {
		System.err.println("<Description>" + factory.getDescription() + "</Description>");
	    }
	StationsType stations = factory.getStations();
	System.err.println(" <Stations>");
	List stationsList = stations.getStation();
	for (Iterator stationIter = stationsList.iterator();stationIter.hasNext();)
	    {
		StationType currentStation = (StationType) stationIter.next();
		System.err.println("  <Station name=\"" + currentStation.getName() + ">");
		if(currentStation.getDescription()!=null)
		    {
			System.err.println("<Description>" + currentStation.getDescription() + "</Description>");
		    }
		CellsType cells = currentStation.getCells();
		System.err.println("   <Cells>");
		
		
	    }
	
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
			System.err.println("The specified directory is invalid!");
			return null;
		    }
	    }
	if (theFile.isFile())
	    {
		return theFile;
	    }
	else
	    {
		System.err.println("The file does not exist in the specified directory!");
		return null;
	    }
    }
}
