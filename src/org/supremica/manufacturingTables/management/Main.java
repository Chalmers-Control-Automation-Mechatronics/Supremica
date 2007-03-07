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
 * Created: Tue Nov  25 13:49:32 2005
 *
 * @author Oscar
 * @version 1.0
 */
package org.supremica.manufacturingTables.management;
import org.supremica.manufacturingTables.controlsystemdata.*;
import org.supremica.manufacturingTables.controlsystemimplementation.*;
import org.supremica.manufacturingTables.xsd.factory.*;
import org.supremica.manufacturingTables.xsd.rop.*;

import java.io.*;

public class Main
{
    // Reads input arguments from the command line.
    // The first argument is the xml file containing the factory description and the second argument is the file
    // path/directory.
    // All eop and cop files in this directory that (".eop" or ".cop" files) are read.
    public static void main(String[] args)
    	throws Exception
    {
	try
	{
	    System.err.println("main function entered");
	    String path = null;
	    if (args.length >= 3)
	    {
		path = args[2];
	    }
	    if (args.length >=2 && ( args[0].equals("-Java") || args[0].equals("-IEC61499") ) )
	    {
		String fileName = args[1];
		Loader loader = new Loader();
		Factory factory = (Factory) loader.loadFactory(path, fileName);
		//AutomationObjectsPLCProgramBuilder plcProgramBuilder = new AutomationObjectsPLCProgramBuilder();
		//plcProgramBuilder.buildPLCProgram(factory);
		ControlSystemDataBuilder plcDataBuilder = new ControlSystemDataBuilder();
		plcDataBuilder.buildPLCData(factory);
		
		String[] fileNames = (new File(path)).list();
		for (int i = 0; i < fileNames.length; i++)
		{
		    if (fileNames[i].toLowerCase().startsWith("eop"))
		    {
			System.out.println("File: " + fileNames[i]);
			org.supremica.manufacturingTables.xsd.eop.Operation eop = (org.supremica.manufacturingTables.xsd.eop.Operation) loader.loadEOP(path, fileNames[i]);
			plcDataBuilder.buildEOP(eop);
		    }
		    else if (fileNames[i].toLowerCase().startsWith("cop"))
		    {
			System.out.println("File: " + fileNames[i]);
			ROP cop = loader.loadROP(path, fileNames[i]);
			plcDataBuilder.buildCOP(cop);
		    }
		}
		
		
		ManufacturingCell cell = plcDataBuilder.getManufacturingCell();
		
		ControlSystemImplementationBuilder PLCProgramBuilder = null;
		if (args[0].equals("-Java"))
		{
		    PLCProgramBuilder = new JavaControlSystemImplementationBuilder();
		}
		else if (args[0].equals("-IEC61499"))
		{
		    PLCProgramBuilder = new IEC61499ControlSystemImplementationBuilder();
		}
		
		if (PLCProgramBuilder != null)
		{
		    PLCProgramBuilder.createNewPLCProgram(cell);
		    System.err.println(args[0].substring(1) + " PLCProgram created");
		}
		else 
		{
		    System.err.println("Unknown implementation language!");
		}
		if (args[0].equals("-Java"))
		{
		    PLCProgram plcProgram = PLCProgramBuilder.getPLCProgram();
		    System.err.println("Time to run the PLCProgram!");
		    plcProgram.run(); // (The IEC61499 application is started from the Fuber runtime)
		}
	    }
	    else
	    {
		System.err.println("You must specify implementation language (-Java, -IEC61499 ...), a fileName and optionally a path!");
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	
    }
}