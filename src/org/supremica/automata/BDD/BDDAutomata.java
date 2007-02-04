
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
package org.supremica.automata.BDD;

import net.sf.javabdd.*;
import org.supremica.util.SupremicaException;
import java.util.*;
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;
import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.PCG;
import org.supremica.util.BDD.solvers.OrderingSolver;


public class BDDAutomata
{
	BDDManager manager;
	Automata theAutomata;

	public BDDAutomata(Automata theAutomata)
	{
		this.theAutomata = new Automata(theAutomata);
		sortAutomata(theAutomata);
		manager = new BDDManager(theAutomata);
		manager.initialize();
	}

	void sortAutomata(Automata theAutomata)
	{
		ArrayList<PCGNode> pcgNodeList = new ArrayList<PCGNode>();
		for (Automaton currAutomaton : theAutomata)
		{
			pcgNodeList.add(new DefaultPCGNode(currAutomaton.getName(), currAutomaton.nbrOfStates()));
		}
		PCG pcg = new PCG(pcgNodeList.toArray());

		int[][] weightMatrix = getCommunicationMatrix();
		OrderingSolver orderingSolver = new OrderingSolver(theAutomata.size());

		int i = 0;
		for (Automaton currAutomaton : theAutomata)
		{
			orderingSolver.addNode(pcgNodeList.get(i), weightMatrix[i], i - 1);
			i++;
		}

		int[] order = orderingSolver.getGoodOrder();
	}

	public double numberOfReachableStates()
	{
		return manager.numberOfReachableStates();
	}

	public boolean isNonblocking()
	{
		return false;
	}

	public boolean isControllable()
	{
		return false;
	}

	public boolean isNonblockingAndControllable()
	{
		return false;
	}

	int[][] getCommunicationMatrix()
	{
		int nbrOfAutomata = theAutomata.size();
		int[][] communicationMatrix = new int[nbrOfAutomata][nbrOfAutomata];

		for (int i = 0; i < nbrOfAutomata; i++)
		{
			Automaton firstAutomaton = theAutomata.getAutomatonAt(i);

			communicationMatrix[i][i] = getCommunicationComplexity(firstAutomaton, firstAutomaton);

			for (int j = 0; j < i; j++)
			{
				Automaton secondAutomaton = theAutomata.getAutomatonAt(j);
				int complexity = getCommunicationComplexity(firstAutomaton, secondAutomaton);
				communicationMatrix[i][j] = communicationMatrix[j][i] = complexity;
			}
		}

		return communicationMatrix;
	}

	static int getCommunicationComplexity(Automaton firstAutomaton, Automaton secondAutomaton)
	{
		Alphabet firstAlphabet = new Alphabet(firstAutomaton.getAlphabet());
		Alphabet secondAlphabet = secondAutomaton.getAlphabet();
		firstAlphabet.intersect(secondAlphabet);
		return firstAlphabet.size();
	}

	public static void main(String[] args)
			throws Exception
	{
		System.err.println("Loading: " + args[0]);

		ProjectBuildFromXml builder = new ProjectBuildFromXml();
		Project theProject = builder.build(new File(args[0]));

		BDDAutomata bddAutomata = new BDDAutomata(theProject);

		double nbrOfReachableStates = bddAutomata.numberOfReachableStates();

		System.err.println("Reachable states: " + nbrOfReachableStates);
	}

}
