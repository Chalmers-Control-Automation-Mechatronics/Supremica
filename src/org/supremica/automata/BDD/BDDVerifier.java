
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

import java.io.File;

import net.sourceforge.waters.model.analysis.AbortRequester;
import net.sourceforge.waters.model.analysis.Abortable;
import net.sourceforge.waters.model.analysis.AnalysisAbortException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.Automata;
import org.supremica.automata.Project;
import org.supremica.automata.IO.ProjectBuildFromXML;


public class BDDVerifier implements Abortable
{
    private static Logger logger = LogManager.getLogger(BDDVerifier.class);
    private boolean isAborting;
    BDDAutomata bddAutomata;

    public BDDVerifier(final Automata theAutomata)
    {
        bddAutomata = new BDDAutomata(theAutomata);
    }

    public double numberOfReachableStates() throws AnalysisAbortException
    {
        return bddAutomata.numberOfReachableStates();
    }

    public double numberOfCoreachableStates() throws AnalysisAbortException
    {
        return bddAutomata.numberOfCoreachableStates();
    }

    public double numberOfReachableAndCoreachableStates() throws AnalysisAbortException
    {
        return bddAutomata.numberOfReachableAndCoreachableStates();
    }

    public double numberOfBlockingStates() throws AnalysisAbortException
    {
        return bddAutomata.numberOfBlockingStates();
    }

    public boolean isNonblocking() throws AnalysisAbortException
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

    public void done()
    {
        if (bddAutomata != null)
        {
            bddAutomata.done();
            bddAutomata = null;
        }
    }

    public static void main(final String[] args)
    throws Exception
    {
        System.err.println("Loading: " + args[0]);

        final ProjectBuildFromXML builder = new ProjectBuildFromXML();
        final Project theProject = builder.build(new File(args[0]));

        final BDDVerifier bddVerifier = new BDDVerifier(theProject);

        long startTime = System.currentTimeMillis();
        final double nbrOfReachableStates = bddVerifier.numberOfReachableStates();
        long stopTime = System.currentTimeMillis();
        long compTime = stopTime - startTime;

        System.err.println("Computation time (ms): " + compTime);
        System.err.println("Reachable states: " + nbrOfReachableStates);

        startTime = System.currentTimeMillis();
        final double nbrOfCoreachableStates = bddVerifier.numberOfCoreachableStates();
        stopTime = System.currentTimeMillis();
        compTime = stopTime - startTime;

        System.err.println("Computation time (ms): " + compTime);
        System.err.println("Coreachable states: " + nbrOfCoreachableStates);

        startTime = System.currentTimeMillis();
        final double nbrOfReachableAndCoreachableStates = bddVerifier.numberOfReachableAndCoreachableStates();
        stopTime = System.currentTimeMillis();
        compTime = stopTime - startTime;

        System.err.println("Computation time (ms): " + compTime);
        System.err.println("ReachableAndCoreachable states: " + nbrOfReachableAndCoreachableStates);

        System.err.println("isNonblocking: " + bddVerifier.isNonblocking());
    }

    @Override
    public void requestAbort(final AbortRequester sender)
    {
      logger.debug("BDDVerifier is requested to stop.");
      isAborting = true;
      bddAutomata.requestAbort(sender);
    }

    @Override
    public boolean isAborting()
    {
      return isAborting;
    }

    @Override
    public void resetAbort()
    {
      isAborting = false;
    }

}

