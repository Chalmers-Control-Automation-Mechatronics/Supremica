
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
package org.supremica.automata.templates;

public class TemplateProperties
{
	private String stateNamePrefix = null;
	private String stateNameSuffix = null;
	private String labelPrefix = null;
	private String labelSuffix = null;
	private String stateNameRegExp = null;
	private String labelRegExp = null;

	public TemplateProperties() {}

	public void setStateNamePrefix(String prefix)
	{
		this.stateNamePrefix = prefix;
	}

	public void setStateNameSuffix(String suffix)
	{
		this.stateNameSuffix = suffix;
	}

	public void setLabelPrefix(String prefix)
	{
		this.labelPrefix = prefix;
	}

	public void setLabelSuffix(String suffix)
	{
		this.labelSuffix = suffix;
	}

	public void setStateNameRegExp(String regExp)
	{
		this.stateNameRegExp = regExp;
	}

	public void setLabelRegExp(String regExp)
	{
		this.labelRegExp = regExp;
	}

	public boolean matchesStateName(String stateName)
	{
		return true;
	}

	public boolean matchesLabel(String label)
	{
		return true;
	}

	public String getNewStateName(String stateName)
	{
		if (!matchesStateName(stateName))
		{
			return stateName;
		}

		StringBuffer sb = new StringBuffer();

		if ((stateNamePrefix != null) &&!stateNamePrefix.equals(""))
		{
			sb.append(stateNamePrefix);
		}

		sb.append(stateName);

		if ((stateNameSuffix != null) &&!stateNameSuffix.equals(""))
		{
			sb.append(stateNameSuffix);
		}

		return sb.toString();
	}

	public String getNewLabel(String label)
	{
		if (!matchesLabel(label))
		{
			return label;
		}

		StringBuffer sb = new StringBuffer();

		if ((labelPrefix != null) &&!labelPrefix.equals(""))
		{
			sb.append(labelPrefix);
		}

		sb.append(label);

		if ((labelSuffix != null) &&!labelSuffix.equals(""))
		{
			sb.append(labelSuffix);
		}

		return sb.toString();
	}
}
