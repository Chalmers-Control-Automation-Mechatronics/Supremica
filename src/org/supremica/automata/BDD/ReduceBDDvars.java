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
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import org.supremica.automata.*;
import org.supremica.util.ActionTimer;

/**
 *
 * @author Sajed
 */
public class ReduceBDDvars {
    ExtendedAutomaton automaton;
    Set<ArrayList<NodeProxy>> paths;
    HashSet<ArrayList<NodeProxy>> optimalPaths;
    Map<NodeProxy, HashSet<ArrayList<NodeProxy>>> state2pathsMap;
    Map<ArrayList<NodeProxy>, Integer> path2index;
    Map<Integer, ArrayList<NodeProxy>> index2path;
    Map<Integer, Integer> pathIndex2cost;
    int pathIndex = 0;

    public ReduceBDDvars(final ExtendedAutomaton automaton)
    {
        this.automaton = automaton;
        paths = new HashSet<ArrayList<NodeProxy>>();
        optimalPaths = new HashSet<ArrayList<NodeProxy>>();
        state2pathsMap = new HashMap<NodeProxy, HashSet<ArrayList<NodeProxy>>>(automaton.nbrOfNodes());
        path2index = new HashMap<ArrayList<NodeProxy>, Integer>(automaton.nbrOfNodes());
        index2path = new HashMap<Integer, ArrayList<NodeProxy>>(automaton.nbrOfNodes());
        pathIndex2cost = new HashMap<Integer, Integer>(automaton.nbrOfNodes());

        for(final NodeProxy state:automaton.getNodes())
            state2pathsMap.put(state,new HashSet<ArrayList<NodeProxy>>());
    }

    public int cost(final ArrayList<NodeProxy> path)
    {
        return (int)Math.ceil(Math.log(2*path.size()+1)/Math.log(2));
    }

    public HashSet<ArrayList<NodeProxy>> getOptimalSetOfPaths()
    {
        return optimalPaths;
    }

    @SuppressWarnings("unchecked")
    public void fillState2PathMap(final ArrayList<NodeProxy> path)
    {
        for(final NodeProxy node:path)
            state2pathsMap.get(node).add((ArrayList<NodeProxy>)path.clone());
    }

    public boolean areTwoPathsEqual(final ArrayList<NodeProxy> path1, final ArrayList<NodeProxy> path2)
    {
        final boolean equal = true;
        if(path1.size()==path2.size())
        {
            for(int i = 0;i<path1.size();i++)
            {
                if(!path1.get(i).equals(path2.get(i)))
                    return false;
            }
        }
        else
            return false;

        return equal;
    }

    public boolean isPathVisited(final ArrayList<NodeProxy> path, final Set<ArrayList<NodeProxy>> ps)
    {
        final boolean visited = false;
        for(final ArrayList<NodeProxy> p:ps)
        {
            if(areTwoPathsEqual(p, path))
                return true;
        }
        return visited;
    }

    public void generateAllPaths()
    {
        for(final NodeProxy node:automaton.getNodes())
        {
            if(automaton.getLocationToIngoingEdgesMap().get(node).size()==0)
            {
                DFS(node, new ArrayList<NodeProxy>());
            }
        }

        final Set<ArrayList<NodeProxy>> clonePaths = new HashSet<ArrayList<NodeProxy>>(paths);
        for(final ArrayList<NodeProxy> path:clonePaths)
        {
            final ArrayList<NodeProxy> p = new ArrayList<NodeProxy>(path);
            final int pLength = p.size()-1;
            for(int i=0;i<pLength;i++)
            {
                p.remove(0);
                final ArrayList<NodeProxy> newPath = new ArrayList<NodeProxy>(p);
                final int sizeBefore = paths.size();
                paths.add(newPath);
                final int sizeAfter = paths.size();
                if(sizeBefore!=sizeAfter)
                {
                    pathIndex2cost.put(pathIndex, cost(newPath));
                    index2path.put(pathIndex, newPath);
                    fillState2PathMap(newPath);
//                    paths.add(newPath);
                    path2index.put(newPath, pathIndex++);
                }
            }
        }
    }

    // Some nodes will be visited more than once
    @SuppressWarnings("unchecked")
    public void DFS(final NodeProxy state, final ArrayList<NodeProxy> soFarPath)
    {
        soFarPath.add(state);
        final ArrayList<NodeProxy> newPath = new ArrayList<NodeProxy>(soFarPath);
        final int sizeBefore = paths.size();
        paths.add(newPath);
        final int sizeAfter = paths.size();
        if(sizeBefore!=sizeAfter)
        {
            pathIndex2cost.put(pathIndex, cost(newPath));
            index2path.put(pathIndex, newPath);
            fillState2PathMap(newPath);
            path2index.put(newPath, pathIndex++);
        }
        for(final EdgeProxy arc:automaton.getLocationToOutgoingEdgesMap().get(state))
            DFS(arc.getTarget(),(ArrayList<NodeProxy>)soFarPath.clone());
    }

    public void computeOptimalPaths()
    {
        final ActionTimer timer = new ActionTimer();
        timer.start();
        System.err.println("Start generating the paths...");
        generateAllPaths();
        System.err.println("Number of paths: "+paths.size());

//        for(NodeProxy state:automaton.getNodes())
//            System.err.println(state.getName()+": "+state2pathsMap.get(state));

        System.err.println("Start generating the IP model...");
        final SolverFactory factory = new SolverFactorySAT4J();
        factory.setParameter(Solver.VERBOSE, 0);
        factory.setParameter(Solver.TIMEOUT, 100); // set timeout to 100 seconds

        final Problem problem = new Problem();

        Linear linear = new Linear();
        for(int i=0;i<paths.size();i++)
        {
//            System.err.println(i+": "+index2path.get(i));
            linear.add(pathIndex2cost.get(i), "d"+i);
        }
        System.err.println("The objective function is modeled.");


        problem.setObjective(linear, OptType.MIN);

        for(final NodeProxy node:automaton.getNodes())
        {
            linear = new Linear();
            for(final ArrayList<NodeProxy> path:state2pathsMap.get(node))
            {
                linear.add(1, "d"+(path2index.get(path)));
            }

            problem.add(linear, "=", 1);
        }
        System.err.println("The constraints are modeled.");

        for(int i=0;i<paths.size();i++)
        {
            problem.setVarType("d"+i, Boolean.class);
        }

        System.err.println("Start solving the IP model...");
        final Solver solver = factory.get(); // you should use this solver only once for one problem
        final Result result = solver.solve(problem);
        timer.stop();
        for(int i=0;i<paths.size();i++)
        {
            if(result.get("d"+i).equals(1))
            {
                optimalPaths.add(index2path.get(i));
//                System.out.println(index2path.get(i));
            }
        }
        System.out.println("Time: "+timer.toString());


    }

}
