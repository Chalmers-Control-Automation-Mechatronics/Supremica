
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

import org.supremica.util.SupremicaException;
import java.util.*;
import org.supremica.automata.*;

import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.PCG;
import org.supremica.util.BDD.Options;

import org.supremica.util.BDD.solvers.OrderingSolver;

public class PCGAutomataSorter
    implements AutomataSorter
{
    public PCGAutomataSorter()
    {
    }
    
    public Automata sortAutomata(Automata orgAutomata)
    {
        Options.ordering_algorithm = Options.AO_HEURISTIC_BFS;
        ArrayList<PCGNode> pcgNodeList = new ArrayList<PCGNode>();
        for (Automaton currAutomaton : orgAutomata)
        {
            pcgNodeList.add(new DefaultPCGNode(currAutomaton.getName(), currAutomaton.nbrOfStates()));
        }
        //PCG pcg = new PCG(new Vector<PCGNode>(pcgNodeList));
        
        int[][] weightMatrix = getCommunicationMatrix(orgAutomata);
        OrderingSolver orderingSolver = new OrderingSolver(orgAutomata.size());
        
        int i = 0;
        for (Automaton currAutomaton : orgAutomata)
        {
            orderingSolver.addNode(pcgNodeList.get(i), weightMatrix[i], i - 1);
            i++;
        }
        
        int[] order = orderingSolver.getGoodOrder();
        
        Automata sortedAutomata = new Automata();
        for (i = 0; i < order.length; i++)
        {
            sortedAutomata.addAutomaton(new Automaton(orgAutomata.getAutomatonAt(order[i])));
        }
        return sortedAutomata;
    }
    
    static int[][] getCommunicationMatrix(Automata theAutomata)
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
    
}