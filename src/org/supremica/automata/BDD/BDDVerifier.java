
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
import java.io.*;
import org.supremica.automata.*;
import org.supremica.automata.IO.*;

public class BDDVerifier
{
    BDDAutomata bddAutomata;
    
    public BDDVerifier(Automata theAutomata)
    {
        bddAutomata = new BDDAutomata(theAutomata);
    }
    
    public double numberOfReachableStates()
    {
        return bddAutomata.numberOfReachableStates();
    }
    
    public double numberOfCoreachableStates()
    {
        return bddAutomata.numberOfCoreachableStates();
    }
    
    public double numberOfReachableAndCoreachableStates()
    {
        return bddAutomata.numberOfReachableAndCoreachableStates();
    }
 
    public double numberOfBlockingStates()
    {
        return bddAutomata.numberOfBlockingStates();
    }
        
    public boolean isNonblocking()
    {
        return bddAutomata.isNonblocking();
    }
    
    public boolean isControllable()
    {
        return false;
    }
    
    public boolean isNonblockingAndControllable()
    {
        return false;
    }
    
    public static void main(String[] args)
    throws Exception
    {
        System.err.println("Loading: " + args[0]);
        
        ProjectBuildFromXml builder = new ProjectBuildFromXml();
        Project theProject = builder.build(new File(args[0]));
        
        BDDVerifier bddVerifier = new BDDVerifier(theProject);
        
        long startTime = System.currentTimeMillis();
        double nbrOfReachableStates = bddVerifier.numberOfReachableStates();
        long stopTime = System.currentTimeMillis();
        long compTime = stopTime - startTime;
        
        System.err.println("Computation time (ms): " + compTime);
        System.err.println("Reachable states: " + nbrOfReachableStates);
        
        startTime = System.currentTimeMillis();
        double nbrOfCoreachableStates = bddVerifier.numberOfCoreachableStates();
        stopTime = System.currentTimeMillis();
        compTime = stopTime - startTime;
        
        System.err.println("Computation time (ms): " + compTime);
        System.err.println("Coreachable states: " + nbrOfCoreachableStates);
        
        startTime = System.currentTimeMillis();
        double nbrOfReachableAndCoreachableStates = bddVerifier.numberOfReachableAndCoreachableStates();
        stopTime = System.currentTimeMillis();
        compTime = stopTime - startTime;
        
        System.err.println("Computation time (ms): " + compTime);
        System.err.println("ReachableAndCoreachable states: " + nbrOfReachableAndCoreachableStates);
        
        System.err.println("isNonblocking: " + bddVerifier.isNonblocking());
    }
    
}

