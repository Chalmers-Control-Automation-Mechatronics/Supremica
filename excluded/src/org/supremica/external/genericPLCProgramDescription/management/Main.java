/*
 * This file is external to the Supremica tool and developed by 
 * Oscar Ljungkrantz. Contact:
 *
 * Oscar Ljungkrantz 
 * oscar.ljungkrantz@chalmers.se
 * +46 (0)708-706278
 * SWEDEN
 *
 * for technical discussions about this file.
 * For questions about Supremica license or other technical discussions,
 * contact Supremica according to License Agreement below.
 */

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
 * Main file to load XML files for generic descriptions on PLC
 * Programs, based on extended PLCOpen XML schema. 
 *
 *
 * Created: Thu Apr  03 11:15 2008
 *
 * @author Oscar
 * @version 1.0
 */

package org.supremica.external.genericPLCProgramDescription.management;

import org.supremica.external.genericPLCProgramDescription.xsd.*;

import java.io.*;

public class Main
{
    // Reads input arguments from the command line.
    // The first argument is the xml file containing generic PLC Program and the second argument is the file
    // path/directory.
    public static void main(String[] args)
    	throws Exception
    {
	try
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
		Project plcProject = (Project) loader.loadPLCProject(path, fileName);
		System.out.println("Project " + plcProject.getContentHeader().getName() + " is opened");
		
 		
	 	//ManufacturingCell cell = plcDataBuilder.getManufacturingCell();
		
		//ControlSystemImplementationBuilder PLCProgramBuilder = null;
		//if (args[0].equals("-Java"))
		//{
		//    PLCProgramBuilder = new JavaControlSystemImplementationBuilder();
		//}
		//else if (args[0].equals("-IEC61499"))
		//{
		//    PLCProgramBuilder = new IEC61499ControlSystemImplementationBuilder();
		//}
		
		//if (PLCProgramBuilder != null)
		//{
		//   PLCProgramBuilder.createNewPLCProgram(cell);
		//    System.err.println(args[0].substring(1) + " PLCProgram created");
		//}
		//else 
		//{
		//    System.err.println("Unknown implementation language!");
		//}
	    }
	    else
	    {
		System.err.println("You must specify a fileName and optionally a path!");
	    }
	}
	catch (Exception e)
	{
	    e.printStackTrace();
	}
	
    }
}