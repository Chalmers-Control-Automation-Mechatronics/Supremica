
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
package org.supremica.automata;

import java.util.*;

public class AutomatonType
{
	private static List collection = new LinkedList();
	public static final AutomatonType Undefined = new AutomatonType("Undefined", true);
	public static final AutomatonType Plant = new AutomatonType("Plant", true);
	public static final AutomatonType Specification = new AutomatonType("Specification", true);
	public static final AutomatonType Supervisor = new AutomatonType("Supervisor", true);
	public static final AutomatonType Interface = new AutomatonType("Interface", false);

	private String identifier;

	private AutomatonType(String identifier, boolean add)
	{
		if (add)
		{
			collection.add(this);
			this.identifier = identifier;
		}
	}

	public static Iterator iterator()
	{
		return collection.iterator();
	}

	public String toString()
	{
		return identifier;
	}

	public static AutomatonType toType(String type)
	{
		if (equalType(Plant, type))
		{
			return Plant;
		}

		if (equalType(Specification, type))
		{
			return Specification;
		}

		if (equalType(Supervisor, type))
		{
			return Supervisor;
		}

		if (equalType(Interface, type))
		{
			return Interface;
		}

		return Undefined;
	}

	public static Object[] toArray()
	{
		return collection.toArray();
	}

	private static boolean equalType(AutomatonType type, String ident)
	{
		if (type == null || ident == null)
		{
			return false;
		}
		return ident.toLowerCase().equals(type.toString().toLowerCase());
	}

}
