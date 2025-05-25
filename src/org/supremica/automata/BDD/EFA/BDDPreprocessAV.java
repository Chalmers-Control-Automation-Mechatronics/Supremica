//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 1999-2023 Knut Akesson, Martin Fabian, Robi Malik
//###########################################################################
//# This file is part of Waters/Supremica IDE.
//# Waters/Supremica IDE is free software: you can redistribute it and/or
//# modify it under the terms of the GNU General Public License as published
//# by the Free Software Foundation, either version 2 of the License, or
//# (at your option) any later version.
//# Waters/Supremica IDE is distributed in the hope that it will be useful,
//# but WITHOUT ANY WARRANTY; without even the implied warranty of
//# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
//# Public License for more details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters/Supremica IDE statically or dynamically with other modules
//# is making a combined work based on Waters/Supremica IDE. Thus, the terms
//# and conditions of the GNU General Public License cover the whole
//# combination.
//# In addition, as a special exception, the copyright holders of
//# Waters/Supremica IDE give you permission to combine Waters/Supremica IDE
//# with code included in the standard release of Supremica under the
//# Supremica Software License Agreement (or modified versions of such code,
//# with unchanged license). You may copy and distribute such a system
//# following the terms of the GNU GPL for Waters/Supremica IDE and the
//# licenses of the other code concerned.
//# Note that people who make modified versions of Waters/Supremica IDE are
//# not obligated to grant this special exception for their modified versions;
//# it is their choice whether to do so. The GNU General Public License gives
//# permission to release a modified version without this exception; this
//# exception also makes it possible to release a modified version which
//# carries forward this exception.
//###########################################################################

package org.supremica.automata.BDD.EFA;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ComponentProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.GuardActionBlockProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/***
 * Currently (May 2025) the BDD-based synthesis for EFA cannot handle automaton
 * variables, see issue #132. A work-around is to turn the automaton variables
 * expressions into ordinary variable-value comparisons before invoking the
 * BDD-based synthesis. That's what this pre-processing does.
 *
 * @author: M Fabian
***/
public class BDDPreprocessAV
{
	private final java.util.Map<String, GraphProxy> nameGraphMap = new java.util.HashMap<>();
	private final java.util.Map<String, VariableComponentProxy> nameVarMap = new java.util.HashMap<>();

	// We search for patterns like "Efsm_1 != loc_1" or "efsm_2 == loc_x"
	private final Pattern pattern = Pattern.compile("(\\w+)\\s*[!=]=\\s*(\\w+)");

	public BDDPreprocessAV(java.util.List<Proxy> components)
	{
		// Build the nameGraphMap and nameVarMap (does such maps not exist already?)
		for(final Proxy proxy : components)
		{
			final ComponentProxy component = (ComponentProxy)proxy;
			final String name = component.getName();

			if(component instanceof SimpleComponentProxy)
			{
				final SimpleComponentProxy scp = (SimpleComponentProxy)component;
				// System.err.println(name + " is simple component of kind " + scp.getKind().toString());
				nameGraphMap.put(name, scp.getGraph());
			}
			else if(component instanceof VariableComponentProxy)
			{
				final VariableComponentProxy vcp = (VariableComponentProxy)component;
				nameVarMap.put(name, vcp);
			}
/*			else
			{
				System.err.println("Unknown component proxy type");
			}
*/
		}
	}
	private static final boolean FIND_ONE = true;
	private static final boolean FIND_ALL = false;

	/***
	 * Check if there is at least one guard that includes an automaton variables expression,
	 * that is, an expression like "A == q" (or "A != q"), where A is the name of an automaton
	 * and q is the label of a location in that automaton.
	 *
	***/
	public boolean checkAutomatonVariableGuards()
	{
		// nameGraphMap.forEach((name, graph) -> {if (checkGraph(graph)) { return true; }});
		for (final String name : nameGraphMap.keySet())
		{
			// System.err.println("Checking " + name);
			if(checkGraph(nameGraphMap.get(name)))
				return true; // Automaton variable expression found
		}
		return false;
	}

	private boolean checkGraph(final GraphProxy graph)
	{
		final java.util.Collection<EdgeProxy> edgeSet = graph.getEdges();
		for (final EdgeProxy edge : edgeSet)
		{
			final GuardActionBlockProxy gaBlock = edge.getGuardActionBlock();
			if (gaBlock != null)
			{
				final java.util.List<SimpleExpressionProxy> guards = gaBlock.getGuards();
				for (SimpleExpressionProxy sep : guards)
				{
					if (matchPattern(sep.toString()))
					{
						return true;
					}
				}
			}
		}
		return false;	// No automaton variable expression found
	}

	/*
	 * From https://docs.oracle.com/javase/8/docs/api/java/util/Collection.html
	 * the specification for the contains(Object o) method says: "returns true if and only if
	 * this collection contains at least one element e such that (o==null ? e==null : o.equals(e))."
	 * This specification should not be construed to imply that invoking Collection.contains
	 * with a non-null argument o will cause o.equals(e) to be invoked for any element e.
	 *
	 * So... linear search it is then!
	 */
/*	private class locationComparator
	{
		final String locationName;
		public locationComparator(final String loc)
		{
			this.locationName = loc;
		}
		public boolean equals(NodeProxy node)
		{
			System.err.println("Comparing " + locationName + " to " + node.getName());
			return locationName.equals(node.getName());
		}
	}
*/
	private boolean matchPattern(final String str)
	{
		final Matcher matcher = pattern.matcher(str);
		while(matcher.find())
		{
			final String automatonName = matcher.group(1);	// Might not be automaton name
			final String automatonLocation = matcher.group(2);	// Might not be location name

			final GraphProxy graph = nameGraphMap.get(automatonName);
			if (graph != null) // then this was the name of an automaton
			{
				if(searchForLocation(automatonLocation, graph.getNodes()))
				{
					// We found an expression "A == q" or "A != q" where A is the
					// name of an automaton, and q is the name of a location of A
					return true;
				}
				/**
				 * If "A" is the name of an automaton but "q" is not a location of that automaton,
				 * then we have not really found and automaton variable expression. But we have
				 * found an illformed expression that should not be possible.
				NO, NOT TRUE! q can be a variable that has location labels in its domain,
				and then A == q is a proper AV expression, only "dynamically" so, not directly.
				So this is more complicated than initially expected...
				**/
				final VariableComponentProxy vcp = nameVarMap.get(automatonLocation);
				if (vcp != null) // then this was the name of an EFSM variable
				{
					// Found something that looks like "A == q" or "A != q", but where
					// q is not the name of a location, but of a variable. This variable
					// does not have to have location names in its domain, still we consider
					// this to be an automaton variable expression.
					return true;
					/***
					 * It appears that "automaton variables" simply means that the atomaton names
					 * can be used as (implicit) variables that have the set of location names
					 * as domain. It is even possible to have guards like "A' == q", which evaluates
					 * to true if the automaton A is in its location q after the transition.
					***/
				}
				else // q is neither a location of the automaton, nor the name of an EFSM variable
				{
					// Unclear what to do, this should not be possible
				}
			}
		}
		return false; // No automaton variable expression found
	}
	private boolean searchForLocation(final String location, final java.util.Set<NodeProxy> nodes)
	{
		for (final NodeProxy node : nodes)
		{
			if (location.equals(node.getName()))
			{
				// We found an expression "A == q" or "A != q" where A is the
				// name of an automaton, and q is the name of a location of A
				return true;
			}
		}
		return false;
	}

	private boolean collectAutomatonLocationPairs()
	{
		return false;
	}
	/***
	 * This function collects all <automaton, state> pairs of all automaton variable expressions
	 *
	***/


	public boolean resolveAutomatonVariables()
	{
		return false;
	}
}