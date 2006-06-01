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
import org.supremica.manufacturingTables.controlsystemimplementation.Java.*;
import org.supremica.manufacturingTables.xsd.factory.*;
import org.supremica.manufacturingTables.xsd.eop.*;

import java.io.*;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

public class Main
{
    public static void main(String[] args)
    {
	System.err.println("main function entered");
	String path = null;
	if (args.length >= 2)
	    {
		path = args[1];
	    }
	if (args.length >=1)
	    {
		String fileName = args[0];
		Loader loader = new Loader();
		FactoryType factory = (FactoryType) loader.loadFactory(path, fileName);
		//AutomationObjectsPLCProgramBuilder plcProgramBuilder = new AutomationObjectsPLCProgramBuilder();
		//plcProgramBuilder.buildPLCProgram(factory);
		ControlSystemDataBuilder plcDataBuilder = new ControlSystemDataBuilder();
		plcDataBuilder.buildPLCData(factory);
		
		// build EOPs
		for (int i = 2; i < args.length; i++)
		    {
			OperationType eop = (OperationType) loader.loadEOP(path, args[i]);
			plcDataBuilder.buildEOP(eop);
		    }

		ManufacturingCell cell = plcDataBuilder.getManufacturingCell();
		

		ControlSystemImplementationBuilder javaPLCProgramBuilder = new JavaControlSystemImplementationBuilder();
		javaPLCProgramBuilder.createNewPLCProgram(cell);
		System.err.println("Java PLCProgram created");
		PLCProgram plcProgram = javaPLCProgramBuilder.getPLCProgram();
		System.err.println("Time to run the PLCProgram!");
		plcProgram.run();


		

		

	    }
	else
	    {
		System.err.println("You must enter a fileName and optionally a path!");
	    }
    }
}
