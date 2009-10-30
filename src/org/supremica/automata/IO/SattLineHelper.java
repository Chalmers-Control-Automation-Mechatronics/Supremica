
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
package org.supremica.automata.IO;

import java.io.PrintWriter;


public class SattLineHelper
	extends ControlBuilderHelper
{
	private static SattLineHelper theHelper;

	protected SattLineHelper() {}

	public static IEC61131Helper getInstance()
	{
		if (theHelper == null)
		{
			theHelper = new SattLineHelper();
		}

		return theHelper;
	}

	/**
	 * Helper functions for producing SattLine Code
	 *
	 * @param pw does this and that
	 * @see org.supremica.gui.Supremica
	 */
	public void printBeginProgram(PrintWriter pw, String fileName)
	{
		printFileHeader(pw, fileName);
	}

	public void printEndProgram(PrintWriter pw)
	{
		printEndModuleDefinition(pw);
	}

	public void printFileHeader(PrintWriter pw, String fileName)
	{
		pw.println("\" Syntax version 2.19, date: 2004-01-27-20:33:34.140 N \" ");
		pw.println("\"Original file date: ---\"");
		pw.println("\"Program date: 2004-01-27-20:33:34.140, name: " + fileName + "\"");    // Should perhaps get current date and time
		pw.println("(* This program unit was created by Supremica. *)");
		pw.println("");
	}

	public void printBasePictureInvocation(PrintWriter pw)
	{
		pw.println("BasePicture Invocation");
		pw.println("   ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 ");
		pw.println("    ) : MODULEDEFINITION DateCode_ 492916896 ( GroupConn = ProgStationData.");    // Don't know importance of DateCode
		pw.println("GroupProgFast )\n");
	}

	public void printTypeDefinitions(PrintWriter pw)
	{
		// Empty
	}

	public void printBeginLocalVariables(PrintWriter pw)
	{
		pw.println("\nLOCALVARIABLES");
	}

	public void printEndLocalVariables(PrintWriter pw)
	{

		// We need a ProgStationData variable for generic SattLine code
		pw.println("ProgStationData: ProgStationData;\n");

		// End of variable declarations.
	}

	public void printSubModules(PrintWriter pw)
	{

		// Start of submodule invocations
		pw.println("SUBMODULES");
		pw.println("ProgStationControl1 Invocation");
		pw.println("( 1.18 , 0.72 , 0.0 , 0.1 , 0.1 ) : ProgStationControl;");

		// End of submodule invocations
	}

	public void printBeginModuleDefinition(PrintWriter pw)
	{
		pw.println("ModuleDef");
		pw.println("ClippingBounds = ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits = 0.0 0.01\n");

		// End of Module definition
		// Start of Module code.
		pw.println("ModuleCode\n");
	}

	public void printEndModuleDefinition(PrintWriter pw)
	{

		// End of Module code
		pw.println("ENDDEF (*BasePicture*);");

		// End of BasePicture
	}

	public int getIdentifierLengthLimit()
	{
		return 16;
	}

	public String getTransitionConditionPrefix()
	{
		return " WAIT_FOR ";
	}

	public String getTransitionConditionSuffix()
	{
		return "";
	}

	public String getCoord()
	{

		// Should perhaps parameterise COORD
		return " COORD 0.5, 0.5 OBJSIZE 0.5, 0.5";
	}

	public String getActionP1Prefix()
	{
		return "ENTERCODE\n";
	}

	public String getActionP1Suffix()
	{
		return "";
	}

	public String getActionP0Prefix()
	{
		return "EXITCODE\n";
	}

	public String getActionP0Suffix()
	{
		return "";
	}

	public String getAssignmentOperator()
	{
		return " = ";
	}

/*      Might as well use both SeqControl and SeqTimer. Note that SeqControl is default in
		ControlBuilder but not in SattLine.
		public String getSequenceControlString()
		{
				return "";
		}*/
	public String getIdentifierLengthErrorMessage()
	{
		return " is too long. Identifiers are limited to 20 characters in SattLine. The new name is Automaton_";
	}

	protected void printGFile(PrintWriter pw, String filename)
	{
		pw.println("\" Syntax version 2.19, date: 2001-08-10-10:42:24.724 N \"  ");
	}

	protected void printLFile(PrintWriter pw, String filename)
	{
		pw.println("nucleuslib");
	}

	protected void printPFile(PrintWriter pw, String filename)
	{
		pw.println("DistributionData");
		pw.println(" ( Version \"Distributiondata version 1.0\" )");
		pw.println("SourceCodeSystems");
		pw.println(" (  )");
		pw.println("ExecutingSystems");
		pw.println(" (  )");
	}
}
