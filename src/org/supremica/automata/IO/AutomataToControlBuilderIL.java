
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

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

import org.supremica.automata.Project;


public class AutomataToControlBuilderIL
{
	private AutomataToIEC1131 theExporter = null;
	private ControlBuilderHelper theHelper = null;

	public AutomataToControlBuilderIL(final Project theProject)
		throws Exception
	{
		theHelper = (ControlBuilderHelper) ControlBuilderHelper.getInstance();
		theExporter = new AutomataToIEC1131(theProject, theHelper);
	}

	public void serializeApp(final File theFile, final String filename)
		throws Exception
	{
		final PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

		theExporter.serializeInstructionList(theWriter);
		theWriter.close();
	}

	public void serializePrj(final File theFile, final String filename)
		throws Exception
	{
		final PrintWriter theWriter = new PrintWriter(new FileWriter(theFile));

		theHelper.printPrj(theWriter, filename);
		theWriter.close();
	}
}
