
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



import java.util.*;


public class SynthesisAlgorithm
{

	private static Collection types = new LinkedList();
	public static final SynthesisAlgorithm Monolithic = new SynthesisAlgorithm("Monolithic");
	public static final SynthesisAlgorithm Modular = new SynthesisAlgorithm("Modular");
	public static final SynthesisAlgorithm IDD = new SynthesisAlgorithm("IDD", false);
	public static final SynthesisAlgorithm Unknown = new SynthesisAlgorithm("Unknown", false);
	private String description = null;

	private SynthesisAlgorithm(String description)
	{
		this(description, true);
	}

	private SynthesisAlgorithm(String description, boolean selectable)
	{

		if (selectable)
		{
			types.add(this);
		}

		this.description = description;
	}

	public static Iterator iterator()
	{
		return types.iterator();
	}

	public String toString()
	{
		return description;
	}

	public static SynthesisAlgorithm toAlgorithm(String algorithm)
	{

		if (algorithm.equals(Monolithic.toString()))
		{
			return Monolithic;
		}

		if (algorithm.equals(Modular.toString()))
		{
			return Modular;
		}

		if (algorithm.equals(IDD.toString()))
		{
			return IDD;
		}

		return Unknown;
	}

	public static Object[] toArray()
	{
		return types.toArray();
	}
}
