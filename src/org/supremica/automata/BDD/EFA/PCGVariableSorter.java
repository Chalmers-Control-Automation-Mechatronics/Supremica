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
import java.util.List;

import net.sourceforge.waters.model.module.VariableComponentProxy;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.DefaultPCGNode;
import org.supremica.properties.Config;
import org.supremica.util.BDD.OrderingAlgorithm;
import org.supremica.util.BDD.PCGNode;
import org.supremica.util.BDD.solvers.OrderingSolver;


public class PCGVariableSorter
{
    ExtendedAutomata efa;
    public PCGVariableSorter(final ExtendedAutomata efa)
    {
        this.efa = efa;
    }

    public List<VariableComponentProxy> sortVars(final List<VariableComponentProxy> vars)
    {
        Config.BDD_ORDER_ALGO.setValue(OrderingAlgorithm.AO_HEURISTIC_BFS);
        final ArrayList<PCGNode> pcgNodeList = new ArrayList<PCGNode>();
        //Alphabetic sorting - so that the variable ordering of the corresponding BDDs become the same in every run
        final List<String> varNames = new ArrayList<String>();

        for(final VariableComponentProxy a:vars)
            varNames.add(a.getName());

        Collections.sort(varNames);
        final List<VariableComponentProxy> orgVar = new ArrayList<VariableComponentProxy>();

        for(final String v:varNames)
        {
innerLoop:  for(final VariableComponentProxy var:vars)
                if(var.getName().equals(v))
                {
                    orgVar.add(var);
                    break innerLoop;
                }
        }

//        orgAutomata = oorgAutomata.clone();

        for (final VariableComponentProxy currVar : orgVar)
        {

            final int max = efa.getMaxValueofVar(currVar.getName());
            final int min = efa.getMinValueofVar(currVar.getName());
            final int size = max - min + 1;

            pcgNodeList.add(new DefaultPCGNode(currVar.getName(), size));
        }
        //PCG pcg = new PCG(new Vector<PCGNode>(pcgNodeList));

        final int[][] weightMatrix = getCommunicationMatrix(orgVar);
        final OrderingSolver orderingSolver = new OrderingSolver(orgVar.size());


        for (int i=0;i<orgVar.size();i++)
        {
            orderingSolver.addNode(pcgNodeList.get(i), weightMatrix[i], i - 1);
        }

        int i = 0;
        final int[] order = orderingSolver.getGoodOrder();

        final List<VariableComponentProxy> sortedVars = new ArrayList<VariableComponentProxy>();
        for (i = 0; i < order.length; i++)
        {
            sortedVars.add(orgVar.get(order[i]));
        }
        return sortedVars;
    }

    int[][] getCommunicationMatrix(final List<VariableComponentProxy> theVars)
    {
        final int nbrOfVars = theVars.size();
        final int[][] communicationMatrix = new int[nbrOfVars][nbrOfVars];

        for (int i = 0; i < nbrOfVars; i++)
        {
            final VariableComponentProxy firstVar = theVars.get(i);

            communicationMatrix[i][i] = getCommunicationComplexity(firstVar, firstVar);

            for (int j = 0; j < i; j++)
            {
                final VariableComponentProxy secondVar = theVars.get(j);
                final int complexity = getCommunicationComplexity(firstVar, secondVar);
                communicationMatrix[i][j] = communicationMatrix[j][i] = complexity;
            }
        }

        return communicationMatrix;
    }

    int getCommunicationComplexity(final VariableComponentProxy firstVar, final VariableComponentProxy secondVar)
    {
        int weight = 0;
        final ArrayList<VariableComponentProxy> relatedVars = new ArrayList<VariableComponentProxy>(efa.getRelatedVars(firstVar));

        if(firstVar.equals(secondVar))
        {
            weight = Integer.MAX_VALUE;
        }
        else
        {
            final ArrayList<VariableComponentProxy> secVar = new ArrayList<VariableComponentProxy>();
            secVar.add(secondVar);
            relatedVars.retainAll(secVar);
            weight = relatedVars.size();
        }

        return weight;
    }


}