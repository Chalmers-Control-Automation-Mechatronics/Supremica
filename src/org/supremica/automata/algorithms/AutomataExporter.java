
/*
 *  Supremica Software License Agreement
 *
 *  The Supremica software is not in the public domain
 *  However, it is freely available without fee for education,
 *  research, and non-profit purposes.  By obtaining copies of
 *  this and other files that comprise the Supremica software,
 *  you, the Licensee, agree to abide by the following
 *  conditions and understandings with respect to the
 *  copyrighted software:
 *
 *  The software is copyrighted in the name of Supremica,
 *  and ownership of the software remains with Supremica.
 *
 *  Permission to use, copy, and modify this software and its
 *  documentation for education, research, and non-profit
 *  purposes is hereby granted to Licensee, provided that the
 *  copyright notice, the original author's names and unit
 *  identification, and this permission notice appear on all
 *  such copies, and that no charge be made for such copies.
 *  Any entity desiring permission to incorporate this software
 *  into commercial products or to use it for commercial
 *  purposes should contact:
 *
 *  Knut Akesson (KA), knut@supremica.org
 *  Supremica,
 *  Haradsgatan 26A
 *  431 42 Molndal
 *  SWEDEN
 *
 *  to discuss license terms. No cost evaluation licenses are
 *  available.
 *
 *  Licensee may not use the name, logo, or any other symbol
 *  of Supremica nor the names of any of its employees nor
 *  any adaptation thereof in advertising or publicity
 *  pertaining to the software without specific prior written
 *  approval of the Supremica.
 *
 *  SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 *  SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 *  IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 *  Supremica or KA shall not be liable for any damages
 *  suffered by Licensee from the use of this software.
 *
 *  Supremica is owned and represented by KA.
 */
package org.supremica.automata.algorithms;

import java.io.*;
import java.util.*;
import org.supremica.automata.Automata;

class AutomataExporter
{
	final static int TO_XML = 1;
	final static int TO_DOT = 2;
	final static int TO_DSX = 3;
	final static int SYSTEM = 10;
	final static int FILE = 11;
	final static int TOPDOWN = 20;
	final static int LEFTRIGHT = 21;

	public static void main(String args[])
		throws Exception
	{
		String inputFileName = "";
		String outputFileName = "";
		int outputType = TO_XML;
		int outputMedia = SYSTEM;
		int outputDirection = TOPDOWN;
		int inputMedia = SYSTEM;
		boolean withLabel = true;
		boolean validate = false;
		int automatonToExport = -1;

		// Get filename
		if (args.length == 0)
		{
			System.out.print("Usage: AutomataExporter options (inputFileName.xml | -)");
			System.out.println("[(-t | --type) (xml|dot|dsx)] : The output format");
			System.out.println("[(-o | -output) filename] : The output fileName");
			System.out.println("[(-a | -automaton) (0|1|...)] : The automaton to export");
			System.out.println("[(-p | -parameter) (leftright|topdown|nostatename|validate)] : parameter to the exporter");

			return;
		}

		int i = 0;

		while (i < args.length)
		{
			if (args[i].equals("--type") || args[i].equals("-t"))
			{
				i++;

				if (args[i].equals("xml"))
				{
					outputType = TO_XML;
				}
				else if (args[i].equals("dot"))
				{
					outputType = TO_DOT;
				}
				else if (args[i].equals("dsx"))
				{
					outputType = TO_DSX;
				}
			}
			else if (args[i].equals("--object") || args[i].equals("-o"))
			{
				i++;

				outputMedia = FILE;
				outputFileName = args[i];
			}
			else if (args[i].equals("--automaton") || args[i].equals("-a"))
			{
				i++;

				automatonToExport = (new Integer(args[i])).intValue();
			}
			else if (args[i].equals("--parameter") || args[i].equals("-p"))
			{
				i++;

				if (args[i].equals("leftright"))
				{
					outputDirection = LEFTRIGHT;
				}
				else if (args[i].equals("topdown"))
				{
					outputDirection = TOPDOWN;
				}
				else if (args[i].equals("nostatename"))
				{
					withLabel = false;
				}
				else if (args[i].equals("validate"))
				{
					validate = true;
				}
			}
			else if (args[i].equals("-"))
			{
				inputMedia = SYSTEM;
			}
			else
			{
				inputFileName = args[i];
				inputMedia = FILE;
			}

			i++;
		}

		// AutomataBuildFromXml builder = new AutomataBuildFromXml();
		Automata theAutomata;

		if (inputMedia == FILE)
		{
			theAutomata = AutomataBuildFromXml.build(inputFileName, validate);
		}
		else
		{
			theAutomata = AutomataBuildFromXml.build(new FileInputStream(FileDescriptor.in), validate);
		}

		PrintWriter pw;

		if (outputMedia == FILE)
		{
			pw = new PrintWriter(new FileWriter(outputFileName));
		}
		else
		{
			pw = new PrintWriter(System.out);
		}

		if (outputType == TO_XML)
		{
			AutomataToXml serializer;

			if (automatonToExport == -1)
			{

				// Export all automata
				serializer = new AutomataToXml(theAutomata);
			}
			else
			{
				serializer = new AutomataToXml(theAutomata.getAutomatonAt(automatonToExport));
			}

			serializer.serialize(pw);
		}
		else if (outputType == TO_DOT)
		{
			if (automatonToExport == -1)
			{

				// Export the first automaton
				automatonToExport = 0;
			}

			AutomatonToDot serializer = new AutomatonToDot(theAutomata.getAutomatonAt(automatonToExport));

			serializer.setLeftToRight((outputDirection == LEFTRIGHT)
									  ? true
									  : false);
			serializer.setWithLabels(withLabel);
			serializer.serialize(pw);
		}
		else if (outputType == TO_DSX)
		{
			if (automatonToExport == -1)
			{

				// Export the first automaton
				automatonToExport = 0;
			}

			AutomatonToDsx serializer = new AutomatonToDsx(theAutomata.getAutomatonAt(automatonToExport));

			serializer.serialize(pw);
		}
	}
}
