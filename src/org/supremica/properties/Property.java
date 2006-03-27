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
package org.supremica.properties;

import java.util.*;

public abstract class Property
{
	private static List<Property> collection = new LinkedList<Property>();

	private PropertyType type;
	private final boolean immutable;
	private final String key;
	private final String comment;

	public Property(PropertyType type, String key, String comment, boolean immutable)
	{
		assert(type != null);
		this.key = key;
		this.type = type;
		this.comment = comment;
		this.immutable = immutable;
		collection.add(this);
	}

	public PropertyType getPropertyType()
	{
		return type;
	}

	public String getKey()
	{
		return key;
	}

	public String getComment()
	{
		return comment;
	}

	public boolean isImmutable()
	{
		return immutable;
	}

	static Iterator<Property> iterator()
	{
		return collection.iterator();
	}

	public final String toString()
	{
		return getPropertyType().toString() + "." + getKey() + " " + valueToString();
	}

	public String valueToEscapedString()
	{
		return valueToString();
	}

	public abstract void set(String value);

	public abstract String valueToString();

	public abstract boolean currentValueDifferentFromDefaultValue();

}
