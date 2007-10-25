/*
 * MilpSolverUI.java
 *
 * Created on den 23 oktober 2007, 11:58
 *
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * This interface defines the methods that the classes connecting Supremica and
 * MILP-solvers should contain.
 */
public interface MilpSolverUI
{
    /**
     * Creates the MILP-file that serves as an input to the solver.
     */
    public void createModelFile()
        throws MilpException, IOException;
    
    /**
     * Launches the (external) MILP-solver.
     */
    public void launchMilpSolver()
        throws MilpException, IOException;
    
    /**
     * Processes the output from the solver, transforming it into a sequence 
     * of event firing times.
     */
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException;
    
    /**
     * Deletes the temporary files that were created to interact with the MILP solver
     * and destroys the external MILP-process. Called by the main class in case 
     * of emergency (e.g. undeleted temporary files in case of exception).
     */
    public void cleanUp();
        
    /** Returns the optimal event occurrence times for each plant-state. */
    public double[][] getOptimalTimes();
    
    /** Returns the optimal alt. path variable choices. */
    public boolean[][][] getOptimalAltPathVariables();
}
