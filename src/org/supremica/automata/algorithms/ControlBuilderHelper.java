
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
package org.supremica.automata.algorithms;

import org.supremica.log.*;
import java.io.*;
import java.util.*;

public class ControlBuilderHelper
{

	public static void serializeAppBegin(PrintWriter pw, String fileName)
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

	public static void serializeAppEnd(PrintWriter pw)
	{
		// End of Program code
		pw.println("END_PROGRAM;\n");

		pw.println("ModuleDef");
		pw.println("ClippingBounds := ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits := 0.0 0.01\n");
		// End of Module definition

		pw.println("END_MODULE");

	}

	public static void serializeVarBegin(PrintWriter pw)
	{
		pw.println("VAR");
	}

	public static void serializeVarEnd(PrintWriter pw)
	{
		pw.println("END_VAR\n");
	}

	public static void serializeILBegin(PrintWriter pw)
	{
		pw.println("      CODEBLOCK Code COORD 0.0, 0.0 OBJSIZE 0.0, 0.0 :");
		pw.println("         INSTRUCTIONLIST");
	}

	public static void serializeILEnd(PrintWriter pw)
	{
		pw.println("\n         END_CODEBLOCK\n");
	}

	public static void serializeSTBegin(PrintWriter pw)
	{
		pw.println("      CODEBLOCK Code COORD 0.0, 0.0 OBJSIZE 0.0, 0.0 :");
		pw.println("         STRUCTUREDTEXT");
	}

	public static void serializeSTEnd(PrintWriter pw)
	{
		pw.println("\n         END_CODEBLOCK\n");
	}

	public static void serializePrj(PrintWriter pw, String fileName)
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
}
