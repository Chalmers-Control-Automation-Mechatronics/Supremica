
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

import org.supremica.log.*;
import org.supremica.automata.*;
import java.io.*;
import java.util.*;

public class AutomataToSattLineSFC
	extends AutomataToControlBuilderSFC
{
	private static Logger logger = LoggerFactory.createLogger(AutomataToSattLineSFC.class);

	public AutomataToSattLineSFC(Project theProject)
	{
		this(theProject, (SattLineHelper) SattLineHelper.getInstance());
	}

	public AutomataToSattLineSFC(Project theProject, IEC61131Helper theHelper)
	{
		super(theProject, theHelper);
	}

	public void serialize(String filename)
	{    // Empty
	}

	public void serialize(PrintWriter pw)
	{    // Empty
	}

	public void serialize_s(File theFile, String filename)
		throws Exception
	{
		PrintWriter pw = new PrintWriter(new FileWriter(theFile));

		/*// Start of file header
		Date theDate = new Date();

		//logger.info(theDate.toString());
		pw.println("\"Syntax version 2.19, date: 2001-08-10-10:42:24.724 N\"");
		pw.println("\"Original file date: ---\"");
		pw.print("\"Program date: 2001-08-10-10:42:24.724, name: ");    // Should perhaps get current date and time

		if (theProject.getName() != null)
		{
				pw.println(" " + theProject.getName().replace('.', '_') + " \"");
		}
		else
		{
				pw.println("\"");
		}

		pw.println("(* This program unit was created by Supremica. *)");
		pw.println("");*/
		theHelper.printBeginProgram(pw, filename);

		// End of file header

		/*// Start of BasePicture Invocation
		pw.println("BasePicture Invocation");
		pw.println("   ( 0.0 , 0.0 , 0.0 , 1.0 , 1.0 ");
		pw.println("    ) : MODULEDEFINITION DateCode_ 492916896 ( GroupConn = ProgStationData.");    // Don't know importance of DateCode
		pw.println("GroupProgFast )\n");*/
		((SattLineHelper) theHelper).printBasePictureInvocation(pw);

		// Start of variable declarations
		/*pw.println("LOCALVARIABLES");*/
		((SattLineHelper) theHelper).printBeginLocalVariables(pw);

		// Output local variables
		Alphabet unionAlphabet = null;

		try
		{
			unionAlphabet = AlphabetHelpers.getUnionAlphabet(theProject);
		}
		catch (Exception ex)
		{
			logger.error("Failed getting union of alphabets of the selected automata. Code generation aborted. " + ex);
			logger.debug(ex.getStackTrace());

			return;
		}

		// . is not allowed in simple variable names, replaced with _
		// #"@|*: Max line length = 140, max identfier length (variable, step name etc) = 20.
		// Too lazy to fix this now. Issue warning instead...
		boolean firstEvent = true;
		int lineLength = 0;

		for (Iterator alphaIt = unionAlphabet.iterator(); alphaIt.hasNext(); )
		{
			LabeledEvent currEvent = (LabeledEvent) alphaIt.next();

			if (currEvent.getLabel().length() > 20)
			{
				logger.warn("Event label " + currEvent.getLabel() + " too long. SattLine's maximum identifier length is 20. (Please rename event label yourself.)");
			}

			if (firstEvent)
			{
				firstEvent = false;

				pw.print(currEvent.getLabel().replace('.', '_'));
			}
			else
			{
				pw.print(", " + currEvent.getLabel().replace('.', '_'));
			}

			lineLength = lineLength + currEvent.getLabel().length() + 2;

			if (lineLength > 80)
			{
				pw.print("\n");

				lineLength = 0;
			}
		}

		pw.println(": boolean;");

		// End of output of local variables
		//pw.println("ProgStationData: ProgStationData;\n");
		((SattLineHelper) theHelper).printEndLocalVariables(pw);

		// End of variable declarations.
		// Start of submodule invocations

		/*pw.println("SUBMODULES");
		pw.println("ProgStationControl1 Invocation");
		pw.println("( 1.18 , 0.72 , 0.0 , 0.1 , 0.1 ) : ProgStationControl;");*/
		((SattLineHelper) theHelper).printSubModules(pw);

		// End of submodule invocations
		// Start of Module definition.

		/*pw.println("ModuleDef");
		pw.println("ClippingBounds = ( -10.0 , -10.0 ) ( 10.0 , 10.0 )");
		pw.println("ZoomLimits = 0.0 0.01\n");

		// End of Module definition
		// Start of Module code.
		pw.println("ModuleCode\n");*/
		((SattLineHelper) theHelper).printBeginModuleDefinition(pw);

		// Here comes the automata, the tricky part.
		automatonConverter(theProject, pw);

		// Event Monitors should be generated here.
		generateEventMonitors(theProject, pw);

		// End of Module code
		//pw.println("ENDDEF (*BasePicture*);");
		theHelper.printEndProgram(pw);

		// End of BasePicture
		pw.close();
	}

	public void serialize_g(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

		((SattLineHelper) theHelper).printGFile(theWriter, filename);
		theWriter.close();
	}

	public void serialize_p(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

		((SattLineHelper) theHelper).printLFile(theWriter, filename);
		theWriter.close();
	}

	public void serialize_l(File theFile, String filename)
		throws Exception
	{
		PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

		((SattLineHelper) theHelper).printPFile(theWriter, filename);
		theWriter.close();
	}
}
