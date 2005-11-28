
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

import java.io.*;
import java.util.*;

public class ControlBuilderHelper
	extends IEC61131Helper
{
	private static ControlBuilderHelper theHelper;

	protected ControlBuilderHelper() {}

	public static IEC61131Helper getInstance()
	{
		if (theHelper == null)
		{
			theHelper = new ControlBuilderHelper();
		}

		return theHelper;
	}

	public void printBeginProgram(PrintWriter pw, String fileName)
	{

		// Start of file header
		Date theDate = new Date();

		// Should perhaps get current date and time, but how do I format it?
		//logger.info(theDate.toString());
		pw.println("HEADER SyntaxVersion_ '3.1' ChangedDate_ '2002-01-25-22:20:41.631'");
		pw.println("OfficialDate_ '2002-01-25-22:20:41.631'");
		pw.println("ProductVersion_ '2.2-0'");
		pw.println("FileName_ ''");
		pw.println("FileHistory_");
		pw.println("(* This source code unit was created 2002-01-25 22:20 by Supremica. *)");
		pw.println("ENDDEF");

		// End of file header
		// Start of Program invocation
		pw.println(fileName);
		pw.println("Invocation ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 )");
		pw.println(": ROOT_MODULE");

		// Use generic Program1 for now
		pw.println("PROGRAM Program1 : SINGLE_PROGRAM");
	}

	public void printEndProgram(PrintWriter pw)
	{

		// End of Program code
		pw.println("\nEND_PROGRAM;\n");
		pw.println("ModuleDef");
		pw.println("ClippingBounds := ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits := 0.0 0.01\n");

		// End of Module definition
		pw.println("END_MODULE");
	}

	public void printBooleanVariableDeclaration(PrintWriter pw, String variableName, String comment)
	{
		pw.print("\t\t" + variableName + " : bool; ");

		if (comment != null)
		{

			//pw.print(comment);
		}

		pw.println();
	}

	public void printBeginVariables(PrintWriter pw)
	{
		pw.println("\tVAR");
	}

	public void printEndVariables(PrintWriter pw)
	{
		pw.println("\tEND_VAR\n");
	}

	public void printILBegin(PrintWriter pw)
	{
		pw.println("      CODEBLOCK Code COORD 0.0, 0.0 OBJSIZE 0.0, 0.0 :");
		pw.println("         INSTRUCTIONLIST");
	}

	public void printILEnd(PrintWriter pw)
	{
		pw.println("\n         END_CODEBLOCK\n");
	}

	public void printSTBegin(PrintWriter pw)
	{
		pw.println("      CODEBLOCK Code COORD 0.0, 0.0 OBJSIZE 0.0, 0.0 :");
		pw.println("         STRUCTUREDTEXT");
	}

	public void printSTEnd(PrintWriter pw)
	{
		pw.println("\n         END_CODEBLOCK\n");
	}

	public void printILComment(PrintWriter pw, String comment)
	{
		printILStatement(pw, null, null, null, comment);
	}

	public void printPrj(PrintWriter pw, String fileName)
	{
		pw.println("'2002-01-11-16:24:38.775'");
		pw.println("Header");
		pw.println(" ( SyntaxVersion '3.0'");
		pw.println("   SavedDate '2002-01-11-16:47:35.825'");
		pw.println("   ChangedDate '2002-01-11-16:24:38.775'");
		pw.println("   FileName '" + fileName + "'\n\n  )");
		pw.println("FileUnits");
		pw.println(" ( Application");
		pw.println("    ( Name '" + fileName + "'");
		pw.println("      Directory '' ) )");
		pw.println("ControlSystem");
		pw.println(" ( Name\n Directory '' )");
		pw.println("ColorTable");
		pw.println(" ( ColorModel HLS\n )");
	}

	public String getActionP1Prefix()
	{
		return "ENTERCODEBLOCK STRUCTUREDTEXT\n";
	}

	public String getActionP1Suffix()
	{
		return "\nEND_CODEBLOCK";
	}

	public String getActionP0Prefix()
	{
		return "EXITCODEBLOCK STRUCTUREDTEXT\n";
	}

	public String getActionP0Suffix()
	{
		return "\nEND_CODEBLOCK";
	}

	public String getAssignmentOperator()
	{
		return " := ";
	}

	public String getSequenceControlString()
	{
		return "  (SeqControl,SeqTimer)";
	}

	public String getIdentifierLengthErrorMessage()
	{
		return " is too long. Identifiers are limited to 32 characters in ControlBuilder. The new name is Automaton_";
	}

	public int getIdentifierLengthLimit()
	{
		return 28;
	}

	public String getTransitionConditionPrefix()
	{
		return " TRANSITIONCODEBLOCK\nSTRUCTUREDTEXT\n";
	}

	public String getTransitionConditionSuffix()
	{
		return "\nEND_CODEBLOCK";
	}

	public String getCoord()
	{
		return " COORD 0.0, 0.0 OBJSIZE 1.0, 1.0";
	}
}
