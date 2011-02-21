/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.BDD;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.sf.javailp.*;
import org.supremica.automata.*;

/**
 *
 * @author Sajed
 */
public class ReduceBDDvars {
    Automaton automaton;
    Set<ArrayList<State>> paths;
    HashSet<ArrayList<State>> optimalPaths;
    Map<State, HashSet<ArrayList<State>>> state2pathsMap;

    public ReduceBDDvars(Automaton automaton)
    {
        this.automaton = automaton;
        paths = new HashSet<ArrayList<org.supremica.automata.State>>();
        optimalPaths = new HashSet<ArrayList<org.supremica.automata.State>>();
        state2pathsMap = new HashMap<org.supremica.automata.State, HashSet<ArrayList<org.supremica.automata.State>>>(automaton.nbrOfStates());
        for(State state:automaton.getStateSet())
            state2pathsMap.put(state,new HashSet<ArrayList<org.supremica.automata.State>>());
    }

    public HashSet<ArrayList<State>> getOptimalSetOfPaths()
    {
        return optimalPaths;
    }

    public void generateAllpaths(State state, ArrayList<State> soFarPath)
    {
        ArrayList<State> singlePath = new ArrayList<org.supremica.automata.State>();
        singlePath.add(state);
        paths.add((ArrayList<State>)singlePath.clone());
        state2pathsMap.get(state).add((ArrayList<State>)singlePath.clone());
        soFarPath.add(state);
        paths.add((ArrayList<State>)soFarPath.clone());
        state2pathsMap.get(state).add((ArrayList<State>)soFarPath.clone());
        for(Arc arc:state.getOutgoingArcs())
        {
            generateAllpaths(arc.getTarget(),(ArrayList<State>)soFarPath.clone());
            generateAllpaths(arc.getTarget(),new ArrayList<State>());
        }
    }

    public void printAllPaths()
    {
/*        generateAllpaths(automaton.getInitialState(), new ArrayList<State>());
        System.err.println(paths.size());
//        for(ArrayList<State> path:paths)
//            System.err.println(path);
        
        for(State state:automaton.getStateSet())
            System.out.println(state.getName()+": "+state2pathsMap.get(state));
*/
        SolverFactory factory = new SolverFactoryGLPK();
        factory.setParameter(Solver.VERBOSE, 0);
        factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds

        /**
        * Constructing a Problem:
        * Maximize: 143x+60y
        * Subject to:
        * 120x+210y <= 15000
        * 110x+30y <= 4000
        * x+y <= 75
        *
        * With x,y being integers
        *
        */
        Problem problem = new Problem();

        Linear linear = new Linear();
        linear.add(143, "x");
        linear.add(60, "y");

        problem.setObjective(linear, OptType.MAX);

        linear = new Linear();
        linear.add(120, "x");
        linear.add(210, "y");

        problem.add(linear, "<=", 15000);

        linear = new Linear();
        linear.add(110, "x");
        linear.add(30, "y");

        problem.add(linear, "<=", 4000);

        linear = new Linear();
        linear.add(1, "x");
        linear.add(1, "y");

        problem.add(linear, "<=", 75);

        problem.setVarType("x", Integer.class);
        problem.setVarType("y", Integer.class);

        Solver solver = factory.get(); // you should use this solver only once for one problem
        Result result = solver.solve(problem);

        System.out.println(result);

        /**
        * Extend the problem with x <= 16 and solve it again
        */
        problem.setVarUpperBound("x", 16);

        solver = factory.get();
        result = solver.solve(problem);

        System.out.println(result);
        

    }

}
