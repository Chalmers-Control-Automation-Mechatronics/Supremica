/*
 * MpsUI.java
 *
 * Created on den 14 februari 2008, 16:07
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;
import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

/**
 * This class implements the interface to the freeware milp-solver GLPK.
 * See www.gnu.org/software/glpk/ for more information about GLPK.
 */
public class MpsUI
        implements MilpSolverUI
{
    /** The *.mps file that serves as an input to the Glpk-solver. */
    protected File mpsFile;
    
    /** The *.sol file that stores the solution, i.e. the output of the Glpk-solver. */
    private File solutionFile;
    
    /** The process responsible for the MILP-solver. */
    private Process milpProcess;
    
    /** The pointer to the constructor of MILP-formulation. */
    private Milp milpConstructor = null;
            
    /** The optimal times (for each plant-state) that the GLPK solver returns */
//    private double[][] optimalTimes = null;
    private double[] optimalTimeVarValues = null;
    
    /** The optimal alt. path variables (booleans) for each [plant][start_state][end_state] */
//    private boolean[][][] optimalAltPathVariables = null;
    private boolean[] optimalBinVarValues = null;
    
    Hashtable<String, Integer> binVarIndexMap = new Hashtable<String, Integer>();
    Hashtable<String, Integer> timeVarIndexMap = new Hashtable<String, Integer>();
    ArrayList<double[]>[] varCoeffs;
    ArrayList<Double> bUpper;
    
    /** Creates a new instance of GlpkUI */
    public MpsUI(Milp milpConstructor)
        throws Exception
    {
        this.milpConstructor = milpConstructor;   
    }
    
    public void initialize()
        throws MilpException, IOException
    {
        // Initialize the model file
        mpsFile = File.createTempFile("milp", ".mps");
        mpsFile.deleteOnExit();

        // Initialize the solution file
        solutionFile = File.createTempFile("milp", ".sol");
        solutionFile.deleteOnExit();
        
        milpConstructor.addToMessages("model: " + mpsFile.getPath() + "\n", SchedulingConstants.MESSAGE_TYPE_INFO);
        milpConstructor.addToMessages("solution: " + solutionFile.getPath() + "\n", SchedulingConstants.MESSAGE_TYPE_INFO);
    }
    
    /**
     * Creates the *.mod-file that contains the MILP-formulation used as an input 
     * to the GLPK-solver.
     */
    public void createModelFile()
        throws MilpException, IOException
    {
        BufferedWriter w = new BufferedWriter(new FileWriter(mpsFile));        
        
        // Map the variable names to an index
        for (int[] altPathVar : milpConstructor.getAltPathVaribles())
        {   
            binVarIndexMap.put(makeAltPathsVariableStr(altPathVar), binVarIndexMap.size());
        }
        for (String mutexVar : milpConstructor.getMutexVariables())
        {
            binVarIndexMap.put(mutexVar, binVarIndexMap.size());
        }
        for (String internalPrecVar : milpConstructor.getInternalPrecVariables())
        {
            binVarIndexMap.put(internalPrecVar, binVarIndexMap.size());
        } 
        for (int i = 0; i < milpConstructor.getDeltaTimes().length; i++)
        {
            for (int j = 0; j < milpConstructor.getDeltaTimes()[i].length; j++)
            {
                timeVarIndexMap.put("time[" + i + ", " + j + "]", 
                        timeVarIndexMap.size() + binVarIndexMap.size() + 1);
            }
        }
        
        int constraintCounter = 0;
        bUpper = new ArrayList<Double>();
        varCoeffs = new ArrayList[binVarIndexMap.size() + timeVarIndexMap.size() + 1];
        for (int i = 0; i < varCoeffs.length; i++)
        {
            varCoeffs[i] = new ArrayList<double[]>();
        }
        varCoeffs[binVarIndexMap.size()].add(new double[]{-1, 1}); // The makespan coefficient in the objective function 
        
        // The cycle time constraints
        for (int[] constr : milpConstructor.getCycleTimeConstraints())
        {
            try
            {
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter, 1});
            varCoeffs[binVarIndexMap.size()].add(new double[]{constraintCounter++, -1});
            bUpper.add(new Double(0));
            }
            catch (Exception e)
            {
                System.out.println("var = " + "time[" + constr[0] + ", " + constr[1] + "]");
                System.out.println("res = " + timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]"));
                return;
            }
        }
        
        // The initial (precedence) constraints
        for (int[] constr : milpConstructor.getInitPrecConstraints())
        {
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter++, -1});
            bUpper.add(new Double(-1 * milpConstructor.getDeltaTimes()[constr[0]][constr[1]]));
        }
        
        // The precedence constraints
        for (int[] constr : milpConstructor.getPrecConstraints())
        {
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter, 1});
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[2] + "]")].
                    add(new double[]{constraintCounter++, -1});
            bUpper.add(new Double(-1 * (milpConstructor.getDeltaTimes()[constr[0]][constr[1]] + Milp.EPSILON)));
        }
        
        // The alternative paths constraints
        for (Constraint constr : milpConstructor.getAltPathsConstraints())
        {
            processConstraintString(constr.getBody(), constraintCounter++);
        }
        
        // The mutex constraints
        for (Constraint constr : milpConstructor.getMutexConstraints())
        {           
            processConstraintString(constr.getBody(), constraintCounter++);
        }
        
        // The constraints due to external specifications //TODO... E-row!!!
        for (ArrayList<int[]> xorConstraintsBlock : milpConstructor.getXorConstraints())
        {
                        
            int rhs = 1;
            for (int[] constr : xorConstraintsBlock)
            {
                if (constr.length == 1)
                {
                    rhs -= 1;
                }
                else
                {
                    varCoeffs[binVarIndexMap.get(makeAltPathsVariableStr(constr))].
                            add(new double[]{constraintCounter, 1});
                }
            }
            constraintCounter++;
            
            bUpper.add(new Double(rhs));
        }
        
        // The non-crossbooking constraints
        for (ArrayList<String> currConstraint : milpConstructor.getNonCrossbookingConstraints())
        {
            String constrBody = "";
            for (int i = 0; i < currConstraint.size() - 1; i++)
            {
                constrBody += currConstraint.get(i) + " + ";
            }
            constrBody += currConstraint.get(currConstraint.size() - 1) + " >= 1";
            
            processConstraintString(constrBody, constraintCounter++);
        }

        // Populate the mps-file
        w.write("NAME          DESUPREMICA\n");
        
        w.write("ROWS\n");
        w.write(" N  MAKESP\n");
        int firstLConstrNr = milpConstructor.getCycleTimeConstraints().size() + 
                milpConstructor.getInitPrecConstraints().size() + milpConstructor.getPrecConstraints().size() +
                milpConstructor.getAltPathsConstraints().size() + milpConstructor.getMutexConstraints().size();
        for (int i = 0; i < firstLConstrNr; i++)
        {
            w.write(" L  R" + i + "\n");
        }
        for (int i = firstLConstrNr; i < milpConstructor.getXorConstraints().size(); i++)
        {
            w.write(" E  R" + i + "\n");
        }
        for (int i = firstLConstrNr + milpConstructor.getXorConstraints().size(); i < constraintCounter; i++)
        {
            w.write(" L  R" + i + "\n");
        }
        
        w.write("COLUMNS\n");
        for (int i = 0; i < varCoeffs.length; i++)
        {
            for (int j = 0; j < varCoeffs[i].size(); j++)
            {
                String varName = "C";
                if (i < binVarIndexMap.size())
                {
                    varName = "X" + i;
                }
                else if (i > binVarIndexMap.size())
                {
                    varName =  "T" + i;
                }
                if (Math.IEEEremainder(j, 2) == 0)
                {
                    double[] currCoeffs = varCoeffs[i].get(j);
                    String currColStr = "    " + varName;
                    String valueStr = "" + currCoeffs[1];
                    
                    int currColLength = currColStr.length();
                    for (int k = 0; k < 14 - currColLength; k++) // The values are placed from 15-th char in MPS-format
                    {
                        currColStr += " ";
                    }
                   
                    if (currCoeffs[0] > -1)
                    {
                        currColStr += "R" + (int)currCoeffs[0];
                    }
                    else
                    {
                        currColStr += "MAKESP";
                    }
                    w.write(currColStr);
                    
                    for (int k = 0; k < 36 - currColStr.length() - valueStr.length(); k++)
                    {
                        w.write(" ");
                    }
                    w.write(valueStr);
                    
                    if (j == varCoeffs[i].size() - 1)
                    {
                        w.write("\n");
                    }
                }
                else
                {
                    double[] currCoeffs = varCoeffs[i].get(j); 
                    String currColStr = "   R" + (int)currCoeffs[0];
                    String valueStr = "" + currCoeffs[1];
                    
                    w.write(currColStr);
                    for (int k = 0; k < 25 - currColStr.length() - valueStr.length(); k++) 
                    {
                        w.write(" ");
                    }
                    w.write(valueStr + "\n");
                }
            }
        }
        
        w.write("RHS\n");
        for (int i = 0; i < bUpper.size(); i++)
        {
            String currRhsStr = "    RHS1      R" + i;
            String valueStr = "" + bUpper.get(i);
            
            w.write(currRhsStr);
            for (int j = 0; j < 36 - currRhsStr.length() - valueStr.length(); j++)
            {
                w.write(" ");
            }
            w.write(valueStr + "\n");
        }
        
        w.write("BOUNDS\n");
        for (int i = 0; i < binVarIndexMap.size(); i++)
        {
            w.write(" BV BND1      X" + i + "\n");
        }
        
        w.write("ENDATA");

        w.flush();
        w.close();
    }
    
    private void processConstraintString(String str, int constraintCounter)
    {            
        double currBUpper = 0;
        int multiplier = -1;
        int tokenSign = 1;
        int storedMultiplier = -1;
        boolean bigMMultiplication = false;
        boolean withinParenthesis = false;

        StringTokenizer tokenizer = new StringTokenizer(str);
        String varToken = "";
        while (tokenizer.hasMoreTokens())
        {
            String currToken = tokenizer.nextToken();

            if ((! bigMMultiplication) && currToken.contains("bigM*"))
            {
                storedMultiplier = multiplier;
                multiplier *= Milp.BIG_M_VALUE * tokenSign;
                tokenSign = 1;
                bigMMultiplication = true;

                currToken = currToken.substring(currToken.indexOf("bigM") + 5);
            }

            if (currToken.startsWith("("))
            {
                withinParenthesis = true;
                if (!bigMMultiplication)
                {
                    bigMMultiplication = true;
                    storedMultiplier = multiplier;
                    multiplier *= tokenSign;
                }

                currToken = currToken.substring(1).trim();
            }

            if (currToken.contains("["))
            {
                varToken = currToken.trim();
            }
            else if (currToken.contains("]"))
            {
                varToken += " " + currToken.trim();

                if (varToken.contains(";"))
                {
                    varToken = varToken.substring(0, varToken.indexOf(";"));
                }

                if (varToken.contains("delta"))
                {
                    int plantIndex = (new Integer(varToken.substring(
                            varToken.indexOf("[") + 1, varToken.indexOf(",")).trim())).intValue();
                    int stateIndex = (new Integer(varToken.substring(
                            varToken.indexOf(",") + 1, varToken.indexOf("]")).trim())).intValue();
                    currBUpper += -1 * multiplier * tokenSign * milpConstructor.getDeltaTimes()[plantIndex][stateIndex];
                }
                else
                {
                    int timeVarIndex = timeVarIndexMap.get(varToken).intValue();

                    varCoeffs[timeVarIndex].add(new double[]{constraintCounter, multiplier * tokenSign});
                }
            }
            else if (currToken.contains(">="))
            {
                multiplier = 1;
                if (str.contains("non_cross_0"))
                {
                    System.out.println("resetting multiplier to " + multiplier);
                }
                tokenSign = 1;
            }
            else if (currToken.contains("-"))
            {
                tokenSign = -1; 
            }
            else if (currToken.contains("+"))
            {
                tokenSign = 1; 
            }
            else if (currToken.contains("epsilon"))
            {
                currBUpper += -1 * tokenSign * multiplier * Milp.EPSILON;
            }
            else if (currToken.trim().equals("c"))
            {
                varCoeffs[binVarIndexMap.size()].add(new double[]{constraintCounter, multiplier * tokenSign});
            }
            else
            {
                try
                {
                    if (currToken.contains(")"))
                    {
                        currToken = currToken.substring(0, currToken.indexOf(")"));
                        withinParenthesis = false;
                    }
                    else if (currToken.contains(";"))
                    {
                        currToken = currToken.substring(0, currToken.indexOf(";"));
                    }

                    Integer binVarIndex = binVarIndexMap.get(currToken.trim());
                    if (binVarIndex != null)
                    {                                    
                        varCoeffs[binVarIndex].add(new double[]{constraintCounter, multiplier * tokenSign});
                    }
                    else
                    {                                                  
                        Double constVal = new Double(currToken);
                        currBUpper += -1 * tokenSign * multiplier * constVal;
                    }
                }
                catch (NumberFormatException e){}
            }   

            if (bigMMultiplication && !withinParenthesis)
            {
                multiplier = storedMultiplier;
                bigMMultiplication = false;
            }
        }
        bUpper.add(currBUpper);
    }
    
    /**
     * Launches the GLPK-solver (glpsol.exe must be included in the path).
     */
    public void launchMilpSolver()
        throws MilpException, IOException
    {
        launchMilpSolver(mpsFile);
    }
    
    /**
     * Launches the GLPK-solver (glpsol.exe must be included in the path).
     * This method-header allows to choose the model file manually (instead of 
     * a temporary file that would be created by the system automatically).
     */
    private void launchMilpSolver(File mpsFile)
        throws MilpException, IOException
    {               
        try
        {
            // Launches the MILP-solver
            milpProcess = Runtime.getRuntime().exec(new String[]{"cbc"});
            BufferedWriter commandWriter = new BufferedWriter(
                    new OutputStreamWriter(new DataOutputStream(milpProcess.getOutputStream())));
            commandWriter.write("import " + mpsFile.getAbsolutePath() + "\n");
            commandWriter.write("solve\n");
            commandWriter.write("solution " + solutionFile.getAbsolutePath() + "\n");
            commandWriter.write("quit\n");
            commandWriter.flush();
            commandWriter.close();
        }
        catch (IOException milpNotFoundException)
        {
            milpConstructor.addToMessages("The CBC-solver 'cbc.exe' not found. " +
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
            if (milpEchoStr.contains("Result"))
            {
                System.out.println("milpecho = " + milpEchoStr);
                
                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("objective") + 10).trim();
                String objValue = milpEchoStr.substring(0, milpEchoStr.indexOf("after")).trim();
                
                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("after") + 6).trim();
                String nrNodes = milpEchoStr.substring(0, milpEchoStr.indexOf("node")).trim();
                
                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("and") + 4).trim();
                String nrIters = milpEchoStr.substring(0, milpEchoStr.indexOf("iteration")).trim();
                
                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("took") + 5).trim();
                String runTime = milpEchoStr.substring(0, milpEchoStr.indexOf("sec")).trim();
                
                milpConstructor.addToMessages("\tOptimization time = " + runTime, 
                        SchedulingConstants.MESSAGE_TYPE_INFO); 
                milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " + objValue, 
                        SchedulingConstants.MESSAGE_TYPE_INFO);
                milpConstructor.addToMessages("\t Nr of nodes = " + nrNodes +"; nr of iterations = " + nrIters, 
                        SchedulingConstants.MESSAGE_TYPE_INFO);
            }
        }
//            totalMilpEchoStr += milpEchoStr + "\n";
//            
//            if (milpEchoStr.contains("+") && milpEchoStr.contains(":") && 
//                    milpEchoStr.contains("mip") && milpEchoStr.contains(">="))
//            {
//                if (lpIterationCount.equals("") && totalIterationCount.equals(""))
//                {
//                    lpIterationCount = milpEchoStr.substring(milpEchoStr.indexOf("+") + 1, milpEchoStr.indexOf(":")).trim();
//                }
//                totalIterationCount = milpEchoStr.substring(milpEchoStr.indexOf("+") + 1, milpEchoStr.indexOf(":")).trim();
//            }
//            else if (milpEchoStr.contains("objval"))
//            {
//                lpIterationCount = milpEchoStr.substring(0, milpEchoStr.indexOf(":")).trim();
//            }

//        else if (milpEchoStr.contains("NO") && milpEchoStr.contains("FEASIBLE SOLUTION"))
//            {
//                throw new MilpException(milpEchoStr + " (specifications should be relaxed if possible).");
//            }
//            else if (milpEchoStr.contains("error"))
//            {
//                throw new MilpException(totalMilpEchoStr);
//            }
//        }
//         
//        milpConstructor.addToMessages("\tNr of GLPK-iterations = " + totalIterationCount + " (incl. " + 
//                lpIterationCount + " LP-iterations)\n", SchedulingConstants.MESSAGE_TYPE_INFO);
    }
    
    /**
     * Processes the output from the GLPK-solver, transforming it into a sequence 
     * of event firing times.
     */
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException
    {       
        optimalTimeVarValues = new double[timeVarIndexMap.size()];
        for (int i = 0; i < optimalTimeVarValues.length; i++)
        {
            optimalTimeVarValues[i] = 0;
        }
        optimalBinVarValues = new boolean[binVarIndexMap.size()];
        for (int i = 0; i < optimalBinVarValues.length; i++)
        {
            optimalBinVarValues[i] = false;
        }
        
        BufferedReader r = new BufferedReader(new FileReader(solutionFile));
     
        // Go through the solution file and extract the suggested optimal times for each state
        String str;
        while ((str = r.readLine()) != null)
        {
            int cutIndex = str.indexOf("X");
            if (cutIndex > 0)
            {
                str = str.substring(cutIndex + 1);
                cutIndex = str.indexOf(" ");
                int varIndex = (new Integer(str.substring(0, cutIndex).trim())).intValue();
                
                str = str.substring(cutIndex).trim();
                cutIndex = str.indexOf(" ");
                double varValue = (new Integer(str.substring(0, cutIndex).trim())).intValue();
                
                optimalBinVarValues[varIndex] = (varValue == 1);
            }
            else 
            {
                cutIndex = str.indexOf("T");
                if (cutIndex > 0)
                {
                    str = str.substring(cutIndex + 1);
                    cutIndex = str.indexOf(" ");
                    int varIndex = (new Integer(str.substring(0, cutIndex).trim())).intValue();

                    str = str.substring(cutIndex).trim();
                    cutIndex = str.indexOf(" ");
                    double varValue = (new Double(str.substring(0, cutIndex).trim())).intValue();

                    optimalTimeVarValues[varIndex] = varValue;
                }
            }
            
            
//            if (str.indexOf(" time[") > -1)
//            {
//                String strPlantIndex = str.substring(str.indexOf("[") + 1, str.indexOf(",")).trim();
//                String strStateIndex = str.substring(str.indexOf(",") + 1, str.indexOf("]")).trim();
//                String strCost = str.substring(str.indexOf("]") + 1).trim();
//                
//                int plantIndex = (new Integer(strPlantIndex)).intValue();
//                int stateIndex = (new Integer(strStateIndex)).intValue();
//                double cost = (new Double(strCost)).doubleValue();
//                
//                optimalTimes[plantIndex][stateIndex] = cost;
//            }
//            else if (str.indexOf("c ") >  -1) // Print out the makespan of the system
//            {
//                String strMakespan = str.substring(str.indexOf("c") + 1).trim();
//                double makespan = milpConstructor.removeEpsilons((new Double(strMakespan)).doubleValue());
//                milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " + makespan + ".............................\n",
//                    SchedulingConstants.MESSAGE_TYPE_INFO);
//            }
//            else if (str.indexOf(" prec_") > -1)
//            {
//                str = str.substring(str.indexOf("_r") + 2);
//                String strplantIndex = str.substring(0, str.indexOf("_"));
//                String strStartStateIndex = str.substring(str.indexOf("_") + 1, str.lastIndexOf("_"));
//                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
//                if (strEndStateIndex.indexOf(" ") > -1)
//                {
//                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
//                }
//                
//                int plantIndex = (new Integer(strplantIndex)).intValue();
//                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
//                int endStateIndex = (new Integer(strEndStateIndex)).intValue();
//                
//                optimalAltPathVariables[plantIndex][startStateIndex][endStateIndex] = true;
//            }
//            else if (str.indexOf("_from") > -1 && str.indexOf("alt_paths") < 0)
//            {
//                String strplantIndex = str.substring(str.indexOf("r") + 1, str.indexOf("_"));
//                str = str.substring(str.indexOf("_from_") + 6);
//                String strStartStateIndex = str.substring(0, str.indexOf("_"));
//                String strEndStateIndex = str.substring(str.lastIndexOf("_") + 1);
//                if (strEndStateIndex.indexOf(" ") > -1)
//                {
//                    strEndStateIndex = strEndStateIndex.substring(0, strEndStateIndex.indexOf(" "));
//                }
//                
//                int plantIndex = (new Integer(strplantIndex)).intValue();
//                int startStateIndex = (new Integer(strStartStateIndex)).intValue();
//                int endStateIndex = (new Integer(strEndStateIndex)).intValue();
//                
//                if (str.indexOf(" 1") < 0)
//                {
//                    str = r.readLine();
//                }
//                
//                if (str.indexOf(" 0") == str.lastIndexOf(" 0"))
//                {
//                    optimalAltPathVariables[plantIndex][startStateIndex][endStateIndex] = true;
//                }
//            }
        }
        
        //temp
        String s = "binVars: ";
        for (int i = 0; i < optimalBinVarValues.length; i++)
        {
            s += "[" + i + " -> " + optimalBinVarValues[i] + "], ";
        }
        System.out.println(s);
        s = "timeVArs: ";
        for (int i = 0; i < optimalTimeVarValues.length; i++)
        {
            s += "[" + i + " -> " + optimalTimeVarValues[i] + "], ";
        }
        System.out.println(s);
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
            if (mpsFile != null)
            {
                    //mpsFile.delete();
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
        //TODO...
        //return optimalTimes;
        return null;
    }
    
    /** Returns the optimal alt. path variable choices. */
    public boolean[][][] getOptimalAltPathVariables()
    {
        //TODO...
        //return optimalAltPathVariables;
       return null;
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
