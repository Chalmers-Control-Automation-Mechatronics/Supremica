
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.util.BDD.Options;
import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.solvers.OrderingSolver;


public class PCGAutomataSorter
    implements AutomataSorter
{
    public PCGAutomataSorter()
    {
    }

    public Automata sortAutomata(final Automata oorgAutomata)
    {
        Options.ordering_algorithm = Options.AO_HEURISTIC_BFS;
        final ArrayList<PCGNode> pcgNodeList = new ArrayList<PCGNode>();
        //Alphabetic sorting - so that the variable ordering of the corresponding BDDs become the same in every run
        final List<String> automataNames = new ArrayList<String>();

        for(final Automaton a:oorgAutomata)
            automataNames.add(a.getName());

        Collections.sort(automataNames);
        final Automata orgAutomata = new Automata();

        for(final String an:automataNames)
            orgAutomata.addAutomaton(oorgAutomata.getAutomaton(an));

//        orgAutomata = oorgAutomata.clone();

        for (final Automaton currAutomaton : orgAutomata)
        {
            pcgNodeList.add(new DefaultPCGNode(currAutomaton.getName(), currAutomaton.nbrOfStates()));
        }
        //PCG pcg = new PCG(new Vector<PCGNode>(pcgNodeList));

        final int[][] weightMatrix = getCommunicationMatrix(orgAutomata);
        //Code for finding the (min,max,avg) cardinality of the level-1 dependency set of an automaton
        double[] degree = new double[orgAutomata.size()];
        double minLD = Double.MAX_VALUE;
        double maxLD = Double.MIN_VALUE;
        double avgLD = 0;
        for (int i=0; i<orgAutomata.size();i++)
        {
            degree[i] = 1;
            for (int j=0; j<orgAutomata.size();j++)
            {
                if(i!=j)
                {
                    if(weightMatrix[i][j] != 0)
                       degree[i] ++;
                }
            }
            double LD = degree[i] / orgAutomata.size();
            if(LD < minLD)
            {
                minLD = LD;
            }
            if(LD > maxLD)
            {
                maxLD = LD;
            }
            
            avgLD += (LD/orgAutomata.size());
        }
        System.err.println("minLD: "+minLD);
        System.err.println("maxLD: "+maxLD);
        System.err.println("avgLD: "+avgLD);

        final OrderingSolver orderingSolver = new OrderingSolver(orgAutomata.size());

        int i = 0;
        for (@SuppressWarnings("unused") final Automaton currAutomaton : orgAutomata)
        {
            orderingSolver.addNode(pcgNodeList.get(i), weightMatrix[i], i - 1);
            i++;
        }

        final int[] order = orderingSolver.getGoodOrder();

        final Automata sortedAutomata = new Automata();
        for (i = 0; i < order.length; i++)
        {
            sortedAutomata.addAutomaton(new Automaton(orgAutomata.getAutomatonAt(order[i])));
        }
        return sortedAutomata;
    }

    static int[][] getCommunicationMatrix(final Automata theAutomata)
    {
        final int nbrOfAutomata = theAutomata.size();
        final int[][] communicationMatrix = new int[nbrOfAutomata][nbrOfAutomata];

        for (int i = 0; i < nbrOfAutomata; i++)
        {
            final Automaton firstAutomaton = theAutomata.getAutomatonAt(i);

            communicationMatrix[i][i] = getCommunicationComplexity(firstAutomaton, firstAutomaton);

            for (int j = 0; j < i; j++)
            {
                final Automaton secondAutomaton = theAutomata.getAutomatonAt(j);
                final int complexity = getCommunicationComplexity(firstAutomaton, secondAutomaton);
                communicationMatrix[i][j] = communicationMatrix[j][i] = complexity;
            }
        }

        return communicationMatrix;
    }

    static int getCommunicationComplexity(final Automaton firstAutomaton, final Automaton secondAutomaton)
    {
        final Alphabet firstAlphabet = new Alphabet(firstAutomaton.getAlphabet());
        final Alphabet secondAlphabet = secondAutomaton.getAlphabet();
        firstAlphabet.intersect(secondAlphabet);        
        return firstAlphabet.size();
    }

}