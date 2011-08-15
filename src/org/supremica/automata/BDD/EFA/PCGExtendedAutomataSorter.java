/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import org.supremica.automata.BDD.DefaultPCGNode;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.util.BDD.Options;
import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.solvers.OrderingSolver;


public class PCGExtendedAutomataSorter
{
    public PCGExtendedAutomataSorter(){}

    public List<ExtendedAutomaton> sortAutomata(final List<ExtendedAutomaton> oorgAutomata)
    {
        Options.ordering_algorithm = Options.AO_HEURISTIC_BFS;
        final ArrayList<PCGNode> pcgNodeList = new ArrayList<PCGNode>();
        //Alphabetic sorting - so that the variable ordering of the corresponding BDDs become the same in every run
        final List<String> automataNames = new ArrayList<String>();

        for(final ExtendedAutomaton a:oorgAutomata)
            automataNames.add(a.getName());

        Collections.sort(automataNames);
        final List<ExtendedAutomaton> orgAutomata = new ArrayList<ExtendedAutomaton>();

        for(final String an:automataNames)
        {
innerLoop:  for(final ExtendedAutomaton efa:oorgAutomata)
                if(efa.getName().equals(an))
                {
                    orgAutomata.add(efa);
                    break innerLoop;
                }
        }

//        orgAutomata = oorgAutomata.clone();

        for (final ExtendedAutomaton currAutomaton : orgAutomata)
        {
            pcgNodeList.add(new DefaultPCGNode(currAutomaton.getName(), currAutomaton.nbrOfNodes()));
        }
        //PCG pcg = new PCG(new Vector<PCGNode>(pcgNodeList));

        final int[][] weightMatrix = getCommunicationMatrix(orgAutomata);
        final OrderingSolver orderingSolver = new OrderingSolver(orgAutomata.size());


        for (int i=0;i<orgAutomata.size();i++)
        {
            orderingSolver.addNode(pcgNodeList.get(i), weightMatrix[i], i - 1);
        }

        int i = 0;
        final int[] order = orderingSolver.getGoodOrder();

        final List<ExtendedAutomaton> sortedAutomata = new ArrayList<ExtendedAutomaton>();
        for (i = 0; i < order.length; i++)
        {
            sortedAutomata.add(orgAutomata.get(order[i]));
        }
        return sortedAutomata;
    }

    int[][] getCommunicationMatrix(final List<ExtendedAutomaton> theAutomata)
    {
        final int nbrOfAutomata = theAutomata.size();
        final int[][] communicationMatrix = new int[nbrOfAutomata][nbrOfAutomata];

        for (int i = 0; i < nbrOfAutomata; i++)
        {
            final ExtendedAutomaton firstAutomaton = theAutomata.get(i);

            communicationMatrix[i][i] = getCommunicationComplexity(firstAutomaton, firstAutomaton);

            for (int j = 0; j < i; j++)
            {
                final ExtendedAutomaton secondAutomaton = theAutomata.get(j);
                final int complexity = getCommunicationComplexity(firstAutomaton, secondAutomaton);
                communicationMatrix[i][j] = communicationMatrix[j][i] = complexity;
            }
        }

        return communicationMatrix;
    }

    int getCommunicationComplexity(final ExtendedAutomaton firstAutomaton, final ExtendedAutomaton secondAutomaton)
    {
        final List<EventDeclProxy> firstAlphabet = new ArrayList<EventDeclProxy>(firstAutomaton.getAlphabet());
        final List<EventDeclProxy> secondAlphabet = new ArrayList<EventDeclProxy>(secondAutomaton.getAlphabet());
        firstAlphabet.retainAll(secondAlphabet);
        for(final EventDeclProxy event:firstAlphabet)
        {
            Set<VariableComponentProxy> guardVars1 = new HashSet<VariableComponentProxy>();
            if(firstAutomaton.getGuardVariables(event)!=null)
                    guardVars1 = new HashSet<VariableComponentProxy>((firstAutomaton.getGuardVariables(event)));

            Set<VariableComponentProxy> actionVars1 = new HashSet<VariableComponentProxy>();
            if(firstAutomaton.getActionVariables(event)!=null)
                    actionVars1 = new HashSet<VariableComponentProxy>(firstAutomaton.getActionVariables(event));

            final Set<VariableComponentProxy> guardVars2 = new HashSet<VariableComponentProxy>();
            if(secondAutomaton.getGuardVariables(event)!=null)
                    guardVars1 = new HashSet<VariableComponentProxy>((secondAutomaton.getGuardVariables(event)));

            final Set<VariableComponentProxy> actionVars2 = new HashSet<VariableComponentProxy>();
            if(secondAutomaton.getActionVariables(event)!=null)
                    actionVars1 = new HashSet<VariableComponentProxy>((secondAutomaton.getActionVariables(event)));

            guardVars1.retainAll(guardVars2);
            actionVars1.retainAll(actionVars2);
        }

        Set<VariableComponentProxy> guardVars1_allEvents = new HashSet<VariableComponentProxy>();
        if(firstAutomaton.getUsedSourceVariables() != null)
            guardVars1_allEvents = new HashSet<VariableComponentProxy>(firstAutomaton.getUsedSourceVariables());

        Set<VariableComponentProxy> actionVars1_allEvents = new HashSet<VariableComponentProxy>();
        if(firstAutomaton.getUsedSourceVariables() != null)
            actionVars1_allEvents = new HashSet<VariableComponentProxy>(firstAutomaton.getUsedTargetVariables());


        Set<VariableComponentProxy> guardVars2_allEvents = new HashSet<VariableComponentProxy>();
        if(secondAutomaton.getUsedSourceVariables() != null)
            guardVars2_allEvents = new HashSet<VariableComponentProxy>(secondAutomaton.getUsedSourceVariables());

        Set<VariableComponentProxy> actionVars2_allEvents = new HashSet<VariableComponentProxy>();
        if(secondAutomaton.getUsedSourceVariables() != null)
            actionVars2_allEvents = new HashSet<VariableComponentProxy>(secondAutomaton.getUsedTargetVariables());


        guardVars1_allEvents.retainAll(actionVars2_allEvents);
        guardVars2_allEvents.retainAll(actionVars1_allEvents);
//      System.err.println(firstAutomaton.getName()+"-"+secondAutomaton.getName()+": "+weight);
        return firstAlphabet.size();
    }

    int getCommunicationComplexity2(final ExtendedAutomaton firstAutomaton, final ExtendedAutomaton secondAutomaton)
    {
        final List<EventDeclProxy> firstAlphabet = new ArrayList<EventDeclProxy>(firstAutomaton.getAlphabet());
        final List<EventDeclProxy> secondAlphabet = new ArrayList<EventDeclProxy>(secondAutomaton.getAlphabet());
        firstAlphabet.retainAll(secondAlphabet);
        // return weight;
        return firstAlphabet.size();
    }


}