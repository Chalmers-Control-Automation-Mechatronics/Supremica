
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
package org.supremica.automata.IO;

import java.util.*;

public class FileFormats
{
	private static List collection = new LinkedList();
	private static List inputs = new LinkedList();
	private static List outputs = new LinkedList();

	// Both input and output
	public static final FileFormats XML = new FileFormats(true, true, "XML", ".xml", "XML files (*.xml)");
	public static final FileFormats SP = new FileFormats(true, true, "SP", ".sp", "Supremica Project files (*.sp)");
	public static final FileFormats CLASS = new FileFormats(true, true, "CLASS", ".class", "Java Bytecode files (*.class)");

	// Output only
	public static final FileFormats RCP = new FileFormats(false, true, "RCP", ".rcp", "RCP files (*.rcp)");
	public static final FileFormats DSX = new FileFormats(false, true, "DSX", ".dsx", "Desco files (*.dsx)");
	public static final FileFormats DOT = new FileFormats(false, true, "DOT", ".dot", "Graphviz files (*.dot)");
	public static final FileFormats EPS = new FileFormats(false, true, "EPS", ".eps", "Encapsulated Postscript (*.eps)");
	public static final FileFormats PNG = new FileFormats(false, true, "PNG", ".png", "PNG files (*.png)");
	public static final FileFormats SVG = new FileFormats(false, true, "SVG", ".svg", "SVG files (*.svg)");
	public static final FileFormats GIF = new FileFormats(false, true, "GIF", ".gif", "GIF files (*.gif)");
	public static final FileFormats MIF = new FileFormats(false, true, "MIF", ".mif", "MIF files (*.mif)");
	public static final FileFormats S = new FileFormats(false, true, "S", ".s", "SattLine files (*.s)");
	public static final FileFormats PRJ = new FileFormats(false, true, "PRJ", ".prj", "Control Builder Project files (*.prj)");
	public static final FileFormats AUT = new FileFormats(false, true, "AUT", ".aut", "Aldebaran files (*.aut)");
	public static final FileFormats ST = new FileFormats(false, true, "ST", ".st", "IEC-1131 Structured Text files (*.st)");
	public static final FileFormats IL = new FileFormats(false, true, "IL", ".il", "IEC-1131 Instruction List files (*.il)");
	public static final FileFormats NQC = new FileFormats(false, true, "NQC", ".nqc", "Mindstorm NQC files (*.nqc)");

	// Input only
	public static final FileFormats VPRJ = new FileFormats(true, false, "VPRJ", ".vprj", "Valid Project files (*.vprj)");
	public static final FileFormats VMOD = new FileFormats(true, false, "VMOD", ".vmod", "Valid Module files (*.vmod)");
	public static final FileFormats DGRF = new FileFormats(true, false, "DGRF", ".dgrf", "Valid Graph files (*.dgrf)");

	// Neither input nor output
	public static final FileFormats Directory = new FileFormats(false, false, "Directory", "", "Directory");

	private String identifier;
	private String extension;
	private String description;
	private boolean input = false;
	private boolean output = false;

	private FileFormats(boolean input, boolean output, String identifier, String extension, String description)
	{
		collection.add(this);
		if (input)
		{
			inputs.add(this);
		}
		if (output)
		{
			outputs.add(this);
		}
		this.input = input;
		this.output = output;
		this.identifier = identifier;
		this.extension = extension;
		this.description = description;
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public static Iterator inputsIterator()
	{
		return inputs.iterator();
	}

	public static Iterator outputsIterator()
	{
		return outputs.iterator();
	}

	public String toString()
	{
		return identifier;
	}

	public String getExtension()
	{
		return extension;
	}

	public String getDescription()
	{
		return description;
	}

}
