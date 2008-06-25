/*
 * GlpkUI.java
 *
 * Created on den 23 oktober 2007, 12:17
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

/**
 * This class implements the interface to the freeware milp-solver GLPK.
 * See www.gnu.org/software/glpk/ for more information about GLPK.
 */
public class GlpkUI
        implements MilpSolverUI
{
    /** The *.mod file that serves as an input to the Glpk-solver. */
    protected File modelFile;

    /** The *.sol file that stores the solution, i.e. the output of the Glpk-solver. */
    private File solutionFile;

    /** The process responsible for the MILP-solver. */
    private Process milpProcess;

    /** The pointer to the constructor of MILP-formulation. */
    private Milp milpConstructor = null;

    /** The optimal times (for each plant-state) that the GLPK solver returns */
    private double[][] optimalTimes = null;

    /** The optimal alt. path variables (booleans) for each [plant][start_state][end_state] */
    private boolean[][][] optimalAltPathVariables = null;

    /** Creates a new instance of GlpkUI */
    public GlpkUI(Milp milpConstructor)
        throws Exception
    {
        this.milpConstructor = milpConstructor;
    }

    public void initialize()
        throws MilpException, IOException
    {
        // Initialize the model file
        modelFile = File.createTempFile("milp", ".mod");
        modelFile.deleteOnExit();

        // Initialize the solution file
        solutionFile = File.createTempFile("milp", ".sol");
        solutionFile.deleteOnExit();

        milpConstructor.addToMessages("model: " + modelFile.getPath() + "\n", SchedulingConstants.MESSAGE_TYPE_INFO);
        milpConstructor.addToMessages("solution: " + solutionFile.getPath() + "\n", SchedulingConstants.MESSAGE_TYPE_INFO);
    }

    /**
     * Creates the *.mod-file that contains the MILP-formulation used as an input
     * to the GLPK-solver.
     */
    public void createModelFile()
        throws MilpException, IOException
    {
        BufferedWriter w = new BufferedWriter(new FileWriter(modelFile));

        // Definitions of parameters
        w.write("param nrOfPlants >= 0;");
        w.newLine();
        w.write("param nrOfZones >= 0;");
        w.newLine();
        w.write("param maxTic >= 0;");
        w.newLine();
        w.write("param bigM;");
        w.newLine();
        w.write("param epsilon >= 0;");
        w.newLine();

        // Definitions of sets
        w.newLine();
        w.write("set Plants := 0..nrOfPlants;");
        w.newLine();
        w.write("set Zones := 0..nrOfZones;");
        w.newLine();
        w.write("set Tics := 0..maxTic;");
        w.newLine();

        // Definitions of parameters, using sets as their input (must be in this order to avoid GLPK-complaints)
        w.newLine();
        w.write("param deltaTime{r in Plants, t in Tics};");
        w.newLine();

        // Definitions of variables
        w.newLine();
        w.write("var time{r in Plants, t in Tics};"); // >= 0;");
        w.newLine();
        w.write("var c;");
        w.newLine();

        // Write the definition of alernative-path variables to the model file
        for (int[] altPathVar : milpConstructor.getAltPathVaribles())
        {
            w.write("var " + makeAltPathsVariableStr(altPathVar) + ", binary;\n");
        }
        // Write the definition of mutex variables to the model file
        ArrayList<String> mutexVariables = milpConstructor.getMutexVariables();
        for (String mutexVar : mutexVariables)
        {
            w.write("var " + mutexVar + ", binary;\n");
        }
        // Write the definition of the so called internal precedence variables to the model file
        ArrayList<String> internalPrecVariables = milpConstructor.getInternalPrecVariables();
        for (String internalPrecVar : internalPrecVariables)
        {
            w.write("var " + internalPrecVar + ", binary;\n");
        }
        w.newLine();

        // The objective function
        w.newLine();
        w.write("minimize makespan: c;");
        w.newLine();

        // The constraints section
        w.newLine();
        w.write("subject to");
        w.newLine();

        // The cycle time constraints
        w.newLine();
        for (int[] constr : milpConstructor.getCycleTimeConstraints())
        {
             w.write("cycle_time_" + "r" + constr[0] + " : c >= " + "time[" + constr[0] +
                     ", " + constr[1] + "];\n");
        }

        // The initial (precedence) constraints
        w.newLine();
        for (int[] constr : milpConstructor.getInitPrecConstraints())
        {           
             w.write("initial_" + "r" + constr[0] + "_" + constr[1] + " : time[" +
                     constr[0] + ", " + constr[1] + "] >= deltaTime[" + constr[0] + ", " + constr[1] + "];\n");
        }

        // The precedence constraints
        w.newLine();
        for (int[] constr : milpConstructor.getPrecConstraints())
        {
            int counter = 0;
            
            if (constr.length > 3)
            {
                for (int[] altVar : milpConstructor.getActiveAltPathVars(new int[]{constr[0], constr[1], constr[3]}))
                {
                    w.write("prec_" + "r" + constr[0] + "_" + constr[1] + "_" + constr[2] + "_" + counter++ + " : " + 
                            "time[" + constr[0] + ", " + constr[2] + "] >= " + 
                            " time[" + constr[0] + ", " + constr[1] + "] + " + 
                            "deltaTime[" + constr[0] + ", " + constr[2] + "]" + " - bigM*(1 - " +
                            milpConstructor.makeAltPathsVariable(altVar[0], altVar[1], altVar[2]) +
                            ") + epsilon;\n");
                }
            }
            else
            {
                w.write("prec_" + "r" + constr[0] + "_" + constr[1] + "_" + constr[2] + " : time[" +
                        constr[0] + ", " + constr[2] + "] >= time[" + constr[0] + ", " + constr[1] +
                        "] + deltaTime[" + constr[0] + ", " + constr[2] + "] + epsilon;\n");
            }
        }

        // The alternative paths constraints
        w.newLine();
        for (Constraint constr : milpConstructor.getAltPathsConstraints())
        {
            w.write("alt_paths_");

            int[] constrId = constr.getId();
            String body = constr.getBody(); //temp
            if (constrId.length > 2)
            {
                w.write(milpConstructor.makeAltPathsVariable(constrId[0], constrId[1], constrId[2]) +
                        " : " + constr.getBody() + ";\n");
            }
            else
            {
                w.write("r" + constrId[0] + "_" + constrId[1] + "_TOT : " + constr.getBody() + ";\n");
            }
        }

        // The mutex constraints
        w.newLine();       
        for (int[] constr : milpConstructor.getMutexConstraints())
        {
            // Find all active alt path variables for this b1u1-b2u2-pair
            String allActiveAltVarsStr = "";
            int convergingStatesCounter = 0;
            for (int i=0; i<6; i+=5)
            {
                for (int j=i+1; j<i+5; j+=2)
                {
                    java.util.Collection<int[]> currActiveVars = milpConstructor.getActiveAltPathVars(
                            new int[]{constr[i], constr[j], constr[j+1]});
                    if (currActiveVars.size() > 0)
                    {
                        convergingStatesCounter++;                      
                        for (int[] altVar : currActiveVars)
                        {
                            allActiveAltVarsStr += " - " + milpConstructor.makeAltPathsVariable(altVar[0], altVar[1], altVar[2]);
                        }
                    }
                }
            }

            w.write("mutex_z" + constr[10] + "_r" + constr[0] + "_r" + constr[5] + "_var" + constr[11] + " : " + 
                    milpConstructor.makeTimeVariable(constr[0], constr[1]) + " >= " +
                    milpConstructor.makeTimeVariable(constr[5], constr[8]) + " + epsilon - bigM * (" +
                    convergingStatesCounter + allActiveAltVarsStr + " + " + 
                    milpConstructor.makeMutexVariable(constr[0], constr[5], constr[10], constr[11]) + ");\n");
            w.write("dual_mutex_z" + constr[10] + "_r" + constr[0] + "_r" + constr[5] + "_var" + constr[11] + " : " + 
                    milpConstructor.makeTimeVariable(constr[5], constr[6]) + " >= " +
                    milpConstructor.makeTimeVariable(constr[0], constr[3]) + " + epsilon - bigM * (" +
                    (1 + convergingStatesCounter) + allActiveAltVarsStr + " - " + 
                    milpConstructor.makeMutexVariable(constr[0], constr[5], constr[10], constr[11]) + ");\n");
            if (convergingStatesCounter > 0)
            {
                w.write("mutex_var_limit_z" + constr[10] + "_r" + constr[0] + "_r" + constr[5] + "_var" + constr[11] + " : " + 
                        "-" + milpConstructor.makeMutexVariable(constr[0], constr[5], constr[10], constr[11]) + " >= " + 
                        1.0 / convergingStatesCounter + " * (" + allActiveAltVarsStr + ");\n");
            }
        }

        // The constraints due to external specifications
        w.newLine();
        int counter = 0;
        for (ArrayList<int[]> xorConstraintsBlock : milpConstructor.getXorConstraints())
        {
            w.write("xor_" + counter++ + " : ");

            String tempStr = "";
            for (int[] constr : xorConstraintsBlock)
            {
                if (constr.length == 1)
                {
                    tempStr += "1 + ";
                }
                else
                {
                    tempStr += makeAltPathsVariableStr(constr) + " + ";
                }
            }
            tempStr = tempStr.substring(0, tempStr.lastIndexOf("+")).trim();

            w.write(tempStr + " = 1;\n");
        }

//TODO: w.write(externalPrecConstraints) - don't forget this!
//        w.write(externalConstraints);
//        w.newLine();

        // The constraints on the events that are shared between the robots
        counter = 0;
        for (ArrayList<ArrayList<ArrayList<int[]>>> sharedEventConstraintsBlock : milpConstructor.getSharedEventConstraints())
        {
            for (int i1 = 0; i1 < sharedEventConstraintsBlock.size() - 1; i1++)
            {
                ArrayList<ArrayList<int[]>> allSharedTimeVarsInFirstPlant = sharedEventConstraintsBlock.get(i1);
//                String allAltPathVarsInFirstPlantStr = "";

                for (int i2 = i1 + 1; i2 < sharedEventConstraintsBlock.size(); i2++)
                {
                     ArrayList<ArrayList<int[]>> allSharedTimeVarsInSecondPlant = sharedEventConstraintsBlock.get(i2);
//                     String allAltPathVarsInSecondPlantStr = "";

                     for (int j1 = 0; j1 < allSharedTimeVarsInFirstPlant.size(); j1++)
                     {
                         ArrayList<int[]> currSharedTimeVarsInFirstPlant = allSharedTimeVarsInFirstPlant.get(j1);
                         String constraintTailFirstPlant = "";
                         String allAltPathVarsInFirstPlantStr = "";
                         for (int k1 = 1; k1 < currSharedTimeVarsInFirstPlant.size(); k1++)
                         {
                             constraintTailFirstPlant += " - " + makeAltPathsVariableStr(currSharedTimeVarsInFirstPlant.get(k1));
                             allAltPathVarsInFirstPlantStr += " + " + makeAltPathsVariableStr(currSharedTimeVarsInFirstPlant.get(k1));
                         }
                         if (currSharedTimeVarsInFirstPlant.size() == 1) // If there is no alt. path up to the shared event (i.e. if the event must occur in this plant)
                         {
                             allAltPathVarsInFirstPlantStr += " + 1";
                         }
                         // Remove the first ' + ' of the altPathString
                         allAltPathVarsInFirstPlantStr = allAltPathVarsInFirstPlantStr.substring(3);

                         for (int j2 = 0; j2 < allSharedTimeVarsInSecondPlant.size(); j2++)
                         {
                             String allAltPathVarsInSecondPlantStr = "";

                             ArrayList<int[]> currSharedTimeVarsInSecondPlant = allSharedTimeVarsInSecondPlant.get(j2);
                             String primalConstraint = "shared_event_" + counter + " : time[" + currSharedTimeVarsInFirstPlant.get(0)[0] + ", " +
                                     currSharedTimeVarsInFirstPlant.get(0)[1] + "] >= time[" + currSharedTimeVarsInSecondPlant.get(0)[0] +
                                     ", " + currSharedTimeVarsInSecondPlant.get(0)[1] + "]";
                             String dualConstraint = "shared_event_dual_" + counter + " : time[" + currSharedTimeVarsInSecondPlant.get(0)[0] + ", " +
                                     currSharedTimeVarsInSecondPlant.get(0)[1] + "] >= time[" + currSharedTimeVarsInFirstPlant.get(0)[0] +
                                     ", " + currSharedTimeVarsInFirstPlant.get(0)[1] + "]";

                             String constraintTail = "";
                             int currNrAltPathVars = currSharedTimeVarsInFirstPlant.size() + currSharedTimeVarsInSecondPlant.size() - 2;
                             // At most two variables can be active here, one for each plant
                             if (currNrAltPathVars > 2)
                             {
                                 currNrAltPathVars = 2;
                             }
                             if (currNrAltPathVars > 0)
                             {
                                 constraintTail = " - bigM*(" + currNrAltPathVars + constraintTailFirstPlant;

                                 for (int k2 = 1; k2 < currSharedTimeVarsInSecondPlant.size(); k2++)
                                 {
                                     constraintTail += " - " + makeAltPathsVariableStr(currSharedTimeVarsInSecondPlant.get(k2));
                                     allAltPathVarsInSecondPlantStr += " + " + makeAltPathsVariableStr(currSharedTimeVarsInSecondPlant.get(k2));
                                 }
                                 constraintTail += ")";
                             }
                             constraintTail += ";\n";

                             if (currSharedTimeVarsInSecondPlant.size() == 1) // If there is no alt. path up to the shared event (i.e. if the event must occur in this plant)
                             {
                                 allAltPathVarsInSecondPlantStr += " + 1";
                             }
                             // Remove the first ' + ' of the altPathStrings
                             allAltPathVarsInSecondPlantStr = allAltPathVarsInSecondPlantStr.substring(3);

                             w.write(primalConstraint + constraintTail);
                             w.write(dualConstraint + constraintTail);
                             w.write("shared_event_tot_" + counter++ + " : " +
                                     allAltPathVarsInFirstPlantStr + " = " + allAltPathVarsInSecondPlantStr + ";\n");
                         }
                     }
//                     w.write("shared_event_equal_occurrence_" + counter++ + " : " +
//                             allAltPathVarsInFirstPlantStr + " = " + allAltPathVarsInSecondPlantStr + ";\n");
                }
            }
        }
        w.newLine();

        // The constraints representing deadlocks (found as possible circular wait
        // in connected components graph) and their antidots, unfeasible combinations
        // of booking variables. In many cases (if there is a buffer within a potential
        // "circular wait", only unfeasible constraints are added.
        counter = 0;
        for (CircularWaitConstraintBlock currConstraint : milpConstructor.getCircularWaitConstraints())
        {
            boolean bufferInCycle = false;
            String circWaitConstrStr = "circ_wait_" + counter + " : ";
            String unfeasConstrStr = "unfeas_" + counter++ + " : ";

            for (int i = 0; i < currConstraint.size(); i++)
            {
                // Retrieve the plant-zone-state-information about the current circular wait constraint part
                int zone = currConstraint.get(i)[0];
                int plant1 = currConstraint.get(i)[1];
                int plant2 = currConstraint.get(i)[2];
                int tic1 = currConstraint.get(i)[3];
                int tic2 = currConstraint.get(i)[4];
 
                try
                {
                    if (plant1 < plant2)
                    {
                        int mutexVarIndex = milpConstructor.getMutexVarCounterMap().get(new int[]{
                            zone, plant1, plant2, tic1, tic2}).intValue();

                        unfeasConstrStr += "(1 - r" + plant1 + "_books_z" + zone + "_before_r" +
                                plant2 + "_var" + mutexVarIndex + ") + ";
                        circWaitConstrStr += "r" + plant1 + "_books_z" + zone + "_before_r" +
                                plant2 + "_var" + mutexVarIndex + " + ";
                    }
                    else
                    {
                        int mutexVarIndex = milpConstructor.getMutexVarCounterMap().get(new int[]{
                            zone, plant2, plant1, tic2, tic1}).intValue();

                        unfeasConstrStr += "r" + plant2 + "_books_z" + zone + "_before_r" +
                               plant1 + "_var" + mutexVarIndex + " + ";
                        circWaitConstrStr += "(1 - r" + plant2 + "_books_z" + zone + "_before_r" +
                               plant1 + "_var" + mutexVarIndex + ") + ";
                    }
               }
               catch (NullPointerException ex)
               {
                   milpConstructor.addToMessages("Mutex variable with key = {" + zone + ", " +
                           plant1 + ", " + plant2 + ", " + tic1 + ", " + tic2 +
                           "} not found in the variable map.", SchedulingConstants.MESSAGE_TYPE_ERROR);
                   throw ex;
               }
            }
            // If there is a real constraint, add it to the mod.file
            // Note that the unfeasability constraints are always valid, while
            // deadlocks can only occur if there is no buffer within the current cycle
            if (!unfeasConstrStr.trim().endsWith(":"))
            {
                w.write(unfeasConstrStr.substring(0, unfeasConstrStr.lastIndexOf("+")).trim() + " >= 1;\n");
            }
            if (!currConstraint.hasBuffer() && !circWaitConstrStr.trim().endsWith(":"))
            {
                w.write(circWaitConstrStr.substring(0, circWaitConstrStr.lastIndexOf("+")).trim() + " >= 1;\n");
            }
        }
        w.newLine();

        // The end of the model-section and the beginning of the data-section
        w.newLine();
        w.write("data;");
        w.newLine();

        // The numbers of plants resp. zones are inserted into the GLPK-model
        double[][] deltaTimes = milpConstructor.getDeltaTimes();
        int maxNrOfStates = 0;
        for (int i = 0; i < deltaTimes.length; i++)
        {
            if (maxNrOfStates < deltaTimes[i].length)
            {
                maxNrOfStates = deltaTimes[i].length;
            }
        }
        w.newLine();
        w.write("param nrOfPlants := " + (deltaTimes.length - 1) + ";");
        w.newLine();
        w.write("param nrOfZones := " + (milpConstructor.getNrOfZones() - 1) + ";");
        w.newLine();
        w.write("param bigM := " + SchedulingConstants.BIG_M_VALUE + ";");
        w.newLine();
        w.write("param maxTic := " + (maxNrOfStates - 1) + ";");
        w.newLine();
        w.write("param epsilon := " + SchedulingConstants.EPSILON + ";");

        w.newLine();
        w.write("param deltaTime default 0\n:");
        // Construction of deltaTime-header
        for (int i=0; i<maxNrOfStates; i++)
        {
            w.write("\t\t" + i);
        }
        w.write(" :=\n");
        for (int i = 0; i < deltaTimes.length; i++)
        {
            // Add the minimal state times to the model file. The GLPK-solver
            // requires these values to be added as a table with straight geometry
            // (i.e. each a_{i,j} should be written exactly below a_{i-1,j}. For
            // this sake, a position adjustment "\t" or "\t\t" is needed.
            w.write(i + "\t\t" + deltaTimes[i][0]);
            for (int j=1; j<deltaTimes[i].length; j++)
            {
                String prevDeltaTimeStr = "" + deltaTimes[i][j-1];
                if (prevDeltaTimeStr.length() > 5)
                {
                    w.write("\t" + deltaTimes[i][j]);
                }
                else
                {
                    w.write("\t\t" + deltaTimes[i][j]);
                }
            }

            // If the number of states of the current automaton is less
            // than max_nr_of_states, the deltaTime-matrix is filled with points
            // representing zero values.
            for (int j=deltaTimes[i].length; j<maxNrOfStates; j++)
            {
                w.write("\t\t.");
            }

            // The last row of a matrix must end with a semicolumn
            if (i == deltaTimes.length - 1)
            {
                w.write(";");
            }

            // Jump to the next row
            w.write("\n");
        }
        w.newLine();

        // Close the writing session
        w.newLine();
        w.write("end;");
        w.flush();
    }

    /**
     * Launches the GLPK-solver (glpsol.exe must be included in the path).
     */
    public void launchMilpSolver()
        throws MilpException, IOException
    {
        launchMilpSolver(modelFile);
    }

    /**
     * Launches the GLPK-solver (glpsol.exe must be included in the path).
     * This method-header allows to choose the model file manually (instead of
     * a temporary file that would be created by the system automatically).
     */
    private void launchMilpSolver(File currModelFile)
        throws MilpException, IOException
    {
        // Defines the name of the .exe-file that will launch the GLPK-solver,
        // as well the arguments that are sent as input to the solve (e.g. *.mod and *.sol file names)
        String[] cmds = new String[5];
        cmds[0] = "glpsol";
        cmds[1] = "-m";
        cmds[2] = currModelFile.getAbsolutePath();
        cmds[3] = "-o";
        cmds[4] = solutionFile.getAbsolutePath();

        try
        {
            // Launches the MILP-solver with the arguments defined above
            milpProcess = Runtime.getRuntime().exec(cmds);
        }
        catch (IOException milpNotFoundException)
        {
            milpConstructor.addToMessages("The GLPK-solver 'glpsol.exe' not found. " +
                    "Make sure that it is registered in your path.", SchedulingConstants.MESSAGE_TYPE_ERROR);

            throw new MilpException(milpNotFoundException.getMessage());
        }

        // Listens for the output of MILP (that is the input to this application)...
        BufferedReader milpEcho = new BufferedReader(
                new InputStreamReader(new DataInputStream(milpProcess.getInputStream())));

        // ...and prints it to stdout
        String milpEchoStr = "";
        String totalMilpEchoStr = "";
        String totalIterationCount = "";
        String lpIterationCount = "";
        while ((milpEchoStr = milpEcho.readLine()) != null)
        {
            totalMilpEchoStr += milpEchoStr + "\n";

            if (milpEchoStr.contains("+") && milpEchoStr.contains(":") &&
                    milpEchoStr.contains("mip") && milpEchoStr.contains(">="))
            {
                if (lpIterationCount.equals("") && totalIterationCount.equals(""))
                {
                    lpIterationCount = milpEchoStr.substring(milpEchoStr.indexOf("+") + 1, milpEchoStr.indexOf(":")).trim();
                }
                totalIterationCount = milpEchoStr.substring(milpEchoStr.indexOf("+") + 1, milpEchoStr.indexOf(":")).trim();
            }
            else if (milpEchoStr.contains("objval"))
            {
                lpIterationCount = milpEchoStr.substring(0, milpEchoStr.indexOf(":")).trim();
            }

//             if (milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND") || milpEchoStr.contains("Time") || milpEchoStr.contains("Memory"))
//             {

//                 // 				logger.info(milpEchoStr);

//                 // 				if (!milpEchoStr.contains("INTEGER OPTIMAL SOLUTION FOUND"))
//                 // 				{
//                 // 					outputStr += "\t" + milpEchoStr + "\n";
//                 // 				}
//             }
            else if (milpEchoStr.contains("NO") && milpEchoStr.contains("FEASIBLE SOLUTION"))
            {
                throw new MilpException(milpEchoStr + " (specifications should be relaxed if possible).");
            }
            else if (milpEchoStr.contains("error"))
            {
                throw new MilpException(totalMilpEchoStr);
            }
        }

        milpConstructor.addToMessages("\tNr of GLPK-iterations = " + totalIterationCount + " (incl. " +
                lpIterationCount + " LP-iterations)\n", SchedulingConstants.MESSAGE_TYPE_INFO);
    }

    /**
     * Processes the output from the GLPK-solver, transforming it into a sequence
     * of event firing times.
     */
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException
    {
        optimalTimes = new double[milpConstructor.getDeltaTimes().length][];
        for (int i=0; i<optimalTimes.length; i++)
        {
            optimalTimes[i] = new double[milpConstructor.getDeltaTimes()[i].length];
        }

        optimalAltPathVariables = new boolean[milpConstructor.getDeltaTimes().length][][];
        for (int i=0; i<optimalAltPathVariables.length; i++)
        {
            optimalAltPathVariables[i] = new boolean[milpConstructor.getDeltaTimes()[i].length]
                    [milpConstructor.getDeltaTimes()[i].length];
        }

        BufferedReader r = new BufferedReader(new FileReader(solutionFile));
        String str = r.readLine();

        // Go through the solution file and extract the suggested optimal times for each state
        while (str != null)
        {
            StringTokenizer tokenizer = new StringTokenizer(str);
            String tokenStr = "";
            if (tokenizer.hasMoreTokens())
                tokenStr = tokenizer.nextToken();

            if (str.indexOf(" time[") > -1)
            {
                String strPlantIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
                String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();

                double cost = -1;
                boolean valFound = false;
                while ((tokenStr = tokenizer.nextToken()) != null)
                {
                    try
                    {
                        cost = Double.parseDouble(tokenStr.trim());
                        valFound = true;
                        break;
                    }
                    catch (NumberFormatException ex){}
                }
                if (! valFound)
                {
                    throw new MilpException("Cost not found in '" + str + "' (GlpkUI.processSolutionFile())");
                }
//                String strCost = str.substring(str.indexOf("]") + 1).trim();

                int plantIndex = (new Integer(strPlantIndex)).intValue();
                int stateIndex = (new Integer(strStateIndex)).intValue();
//                double cost = (new Double(strCost)).doubleValue();

                optimalTimes[plantIndex][stateIndex] = cost;
            }
            else if (str.indexOf("c ") >  -1) // Print out the makespan of the system
            {
//                String strMakespan = str.substring(str.indexOf("c") + 1).trim();
                double makespan = -1;
                boolean valFound = false;
                while ((tokenStr = tokenizer.nextToken()) != null)
                {
                    try
                    {
                        makespan = Double.parseDouble(tokenStr.trim());
                        valFound = true;
                        break;
                    }
                    catch (NumberFormatException ex){}
                }
                if (! valFound)
                {
                    throw new MilpException("Makespan not found in '" + str + "' (GlpkUI.processSolutionFile())");
                }

                makespan = milpConstructor.removeEpsilons(makespan);
                milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " + makespan + ".............................\n",
                    SchedulingConstants.MESSAGE_TYPE_INFO);
            }
            else if (str.indexOf(" prec_") > -1)
            {
                str = str.substring(str.indexOf("_r") + 2);
                String strplantIndex = str.substring(0, str.indexOf("_"));
                str = str.substring(str.indexOf("_") + 1);
                String strStartStateIndex = str.substring(0, str.indexOf("_"));
                str = str.substring(str.indexOf("_") + 1);
                String strEndStateIndex = str;
                int counterIndex = str.indexOf("_");
                if (counterIndex > -1)
                {
                    strEndStateIndex = str.substring(0, counterIndex);
                }
                
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }

                int plantIndex = (new Integer(strplantIndex)).intValue();
                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
                int endStateIndex = (new Integer(strEndStateIndex)).intValue();

//                optimalAltPathVariables[plantIndex][startStateIndex][endStateIndex] = true;
            }
            else if (str.indexOf("_from") > -1 && str.indexOf("alt_paths") < 0)
            {
                String strplantIndex = str.substring(str.indexOf("r") + 1, str.indexOf("_"));
                str = str.substring(str.indexOf("_from_") + 6);
                String strStartStateIndex = str.substring(0, str.indexOf("_"));
                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
                if (strEndStateIndex.indexOf(" ") > -1)
                {
                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
                }

                int plantIndex = (new Integer(strplantIndex)).intValue();
                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
                int endStateIndex = (new Integer(strEndStateIndex)).intValue();

                if (str.indexOf(" 1") < 0)
                {
                    str = r.readLine();
                }

                if (str.indexOf(" 0") == str.lastIndexOf(" 0"))
                {
                    optimalAltPathVariables[plantIndex][startStateIndex][endStateIndex] = true;
                }
            }

            str = r.readLine();
        }
    }

    /**
     * Deletes the temporary files that were created to interact with the MILP solver.
     * Called by the main class in case of emergency (e.g. undeleted temporary files
     * in case of exception).
     */
    public void cleanUp()
    {
        try
        {
            if (milpProcess != null)
            {
                milpProcess.destroy();
                milpProcess = null;
            }
            if (modelFile != null)
            {
                    //modelFile.delete();
            }
            if (solutionFile != null)
            {
                solutionFile.delete();
            }
        }
        catch (Exception ex)
        {
            milpConstructor.addToMessages("Cleaning up of the GLPK-solver failed.",
                    SchedulingConstants.MESSAGE_TYPE_ERROR);
        }
    }

    /** Returns the optimal event occurrence times for each plant-state. */
    public double[][] getOptimalTimes()
    {
        return optimalTimes;
    }

    /** Returns the optimal alt. path variable choices. */
    public boolean[][][] getOptimalAltPathVariables()
    {
        return optimalAltPathVariables;
    }

    /**
     * Combines info about an alt.paths variable,
     * stored as int[plantIndex, fromStateIndex, toStateIndex],
     * into string representation.
     */
    public String makeAltPathsVariableStr(int[] altPathsVariable)
    {
        return "r" + altPathsVariable[0] + "_from_" + altPathsVariable[1] + "_to_" + altPathsVariable[2];
    }
}
