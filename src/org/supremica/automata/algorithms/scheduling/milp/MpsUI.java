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
    private double[][] optimalTimes = null;
    
    /** The optimal alt. path variables (booleans) for each [plant][start_state][end_state] */
    private boolean[][][] optimalAltPathVariables = null;
    
    Hashtable<String, Integer> binVarIndexMap = new Hashtable<String, Integer>();
    Hashtable<String, Integer> timeVarIndexMap = new Hashtable<String, Integer>();
    ArrayList<double[]>[] varCoeffs;
    ArrayList<Double> bUpper;
    ArrayList<Integer> equalityRowIndices;
    
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
        equalityRowIndices = new ArrayList<Integer>();
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
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter, 1});
            varCoeffs[binVarIndexMap.size()].add(new double[]{constraintCounter++, -1});
            bUpper.add(new Double(0));
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
            bUpper.add(new Double(-1 * (milpConstructor.getDeltaTimes()[constr[0]][constr[2]] + Milp.EPSILON)));
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
  
// TODO: Add circWaitConstraints to the mps-files        
//        // The non-crossbooking constraints
//        for (ArrayList<String> currConstraint : milpConstructor.getNonCrossbookingConstraints())
//        {
//            String constrBody = "";
//            for (int i = 0; i < currConstraint.size() - 1; i++)
//            {
//                constrBody += currConstraint.get(i) + " + ";
//            }
//            constrBody += currConstraint.get(currConstraint.size() - 1) + " >= 1";
//            
//            processConstraintString(constrBody, constraintCounter++);
//        }
        
        // The shared events constraints (a block of constrains for each shared event)
        for (ArrayList<ArrayList<ArrayList<int[]>>> eventBlock : milpConstructor.getSharedEventConstraints())
        {
            for (int i1 = 0; i1 < eventBlock.size() - 1; i1++)
            {
                ArrayList<ArrayList<int[]>> allSharedTimeVarsInFirstPlant = eventBlock.get(i1);
                
                for (int i2 = i1+1; i2 < eventBlock.size(); i2++)
                {
                    ArrayList<ArrayList<int[]>> allSharedTimeVarsInSecondPlant = eventBlock.get(i2);
                    
                    // The hashtables are needed to ensure that the event occurs as many times in both plants
                    Hashtable<Integer, Integer> firstPlantFrequencyMap = new Hashtable<Integer, Integer>();
                    Hashtable<Integer, Integer> secondPlantFrequencyMap = new Hashtable<Integer, Integer>();
                    // Forced occurrences of an event (no path choices upstreams) are stored at index -1
                    firstPlantFrequencyMap.put(-1, 0);
                    secondPlantFrequencyMap.put(-1, 0);
                    
                    for (int j1 = 0; j1 < allSharedTimeVarsInFirstPlant.size(); j1++)
                    {
                        ArrayList<int[]> currSharedTimeVarsInFirstPlant = allSharedTimeVarsInFirstPlant.get(j1);
                        int[] firstPlantState = currSharedTimeVarsInFirstPlant.get(0);
                        
                        // Update the frequency of forced event occurrence if there are no altPath variables for this plant-state
                        if (currSharedTimeVarsInFirstPlant.size() == 1)
                        {
                            int forcedFrequency = firstPlantFrequencyMap.get(-1);
                            firstPlantFrequencyMap.put(-1, forcedFrequency + 1);
                        }
                        
                        for (int j2 = 0; j2 < allSharedTimeVarsInFirstPlant.size(); j2++)
                        {
                            ArrayList<int[]> currSharedTimeVarsInSecondPlant = allSharedTimeVarsInSecondPlant.get(j2);                            
                            int[] secondPlantState = currSharedTimeVarsInSecondPlant.get(0);
                            
                            // Update the frequency of forced event occurrence if there are no altPath variables for this plant-state
                            if (currSharedTimeVarsInSecondPlant.size() == 1)
                            {
                                int forcedFrequency = secondPlantFrequencyMap.get(-1);
                                secondPlantFrequencyMap.put(-1, forcedFrequency + 1);
                            }
                            
                            // Creating two constraints for each plant-state-pair
                            varCoeffs[timeVarIndexMap.get("time[" + secondPlantState[0] + ", " + 
                                    secondPlantState[1] + "]")].add(new double[]{constraintCounter, 1});
                            varCoeffs[timeVarIndexMap.get("time[" + secondPlantState[0] + ", " + 
                                    secondPlantState[1] + "]")].add(new double[]{constraintCounter + 1, -1});
                            varCoeffs[timeVarIndexMap.get("time[" + firstPlantState[0] + ", " + 
                                    firstPlantState[1] + "]")].add(new double[]{constraintCounter, -1});
                            varCoeffs[timeVarIndexMap.get("time[" + firstPlantState[0] + ", " + 
                                    firstPlantState[1] + "]")].add(new double[]{constraintCounter + 1, 1});
                            
                            // The occurrence frequency of each altPathVariable is important 
                            int altPathCounter = 0;
                            Hashtable<Integer, Integer> currFrequencyMap = new Hashtable<Integer, Integer>();
                            for (int k1 = 1; k1 < currSharedTimeVarsInFirstPlant.size(); k1++)
                            {
                                int altPathIndex = binVarIndexMap.get(makeAltPathsVariableStr(
                                        currSharedTimeVarsInFirstPlant.get(k1)));
                                
                                Integer currFrequency = currFrequencyMap.get(new Integer(altPathIndex));
                                if (currFrequency == null)
                                {
                                    currFrequency = new Integer(0);
                                }
                                currFrequencyMap.put(altPathIndex, currFrequency.intValue() + 1);
                                
                                Integer firstPlantFrequency = firstPlantFrequencyMap.get(new Integer(altPathIndex));
                                if (firstPlantFrequency == null)
                                {
                                    firstPlantFrequency = new Integer(0);
                                }
                                firstPlantFrequencyMap.put(altPathIndex, firstPlantFrequency.intValue() + 1);
                                
                                altPathCounter++;
                            }
                            for (int k2 = 1; k2 < currSharedTimeVarsInSecondPlant.size(); k2++)
                            {
                                int altPathIndex = binVarIndexMap.get(makeAltPathsVariableStr(
                                        currSharedTimeVarsInSecondPlant.get(k2)));
                                
                                Integer currFrequency = currFrequencyMap.get(new Integer(altPathIndex));
                                if (currFrequency == null)
                                {
                                    currFrequency = new Integer(0);
                                }
                                currFrequencyMap.put(altPathIndex, currFrequency.intValue() + 1);
                                
                                Integer secondPlantFrequency = secondPlantFrequencyMap.get(new Integer(altPathIndex));
                                if (secondPlantFrequency == null)
                                {
                                    secondPlantFrequency = new Integer(0);
                                }
                                secondPlantFrequencyMap.put(altPathIndex, secondPlantFrequency.intValue() + 1);
                                
                                altPathCounter++;
                            }
                            
                            // The occurrence frequencys are added to the coefficients of current row for each variable
                            for (Integer altPathIndex : currFrequencyMap.keySet())
                            {
                                varCoeffs[altPathIndex].add(new double[]{constraintCounter, 
                                    Milp.BIG_M_VALUE * currFrequencyMap.get(altPathIndex)});
                                varCoeffs[altPathIndex].add(new double[]{constraintCounter + 1, 
                                    Milp.BIG_M_VALUE * currFrequencyMap.get(altPathIndex)});
                            }
                            
                            // Two right-hand-side-expressions are needed
                            bUpper.add(new Double(Milp.BIG_M_VALUE * altPathCounter));
                            bUpper.add(new Double(Milp.BIG_M_VALUE * altPathCounter));
                            
                            constraintCounter += 2;
                        }
                    }
                    
                    // The last constraint ensures that the number of event occurrences is equal in both plants 
                    for (Integer altPathIndex : firstPlantFrequencyMap.keySet())
                    {
                        if (altPathIndex > -1)
                        {
                            varCoeffs[altPathIndex].add(new double[]{
                                constraintCounter, -1 * firstPlantFrequencyMap.get(altPathIndex)});
                        }
                    }
                    for (Integer altPathIndex : secondPlantFrequencyMap.keySet())
                    {
                        if (altPathIndex > -1)
                        {
                            varCoeffs[altPathIndex].add(new double[]{
                                constraintCounter, secondPlantFrequencyMap.get(altPathIndex)});
                        }
                    }
                    // The right-hand-side is generally = 0, but here it contains the numbers of
                    // forces event occurrences (no alt. paths upstreams)
                    bUpper.add(new Double(firstPlantFrequencyMap.get(-1) - secondPlantFrequencyMap.get(-1)));
                    
                    // The list of equality row indices is updated
                    equalityRowIndices.add(constraintCounter);
                    constraintCounter++;
                }
            }
        }

        // Populate the mps-file
        w.write("NAME          DESUPREMICA\n");
        
        w.write("ROWS\n");
        w.write(" N  MAKESP\n");
//        int firstLConstrNr = milpConstructor.getCycleTimeConstraints().size() + 
//                milpConstructor.getInitPrecConstraints().size() + milpConstructor.getPrecConstraints().size() +
//                milpConstructor.getAltPathsConstraints().size() + milpConstructor.getMutexConstraints().size();
//        for (int i = 0; i < firstLConstrNr; i++)
//        {
//            w.write(" L  R" + i + "\n");
//        }
//        for (int i = firstLConstrNr; i < milpConstructor.getXorConstraints().size(); i++)
//        {
//            w.write(" E  R" + i + "\n");
//        }
//        for (int i = firstLConstrNr + milpConstructor.getXorConstraints().size(); i < constraintCounter; i++)
//        {
//            w.write(" L  R" + i + "\n");
//        }
        for (int i = 0; i < constraintCounter; i++)
        {
            if (equalityRowIndices.contains(i))
            {
                w.write(" E  R" + i + "\n");
            }
            else
            {
                w.write(" L  R" + i + "\n");
            }
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
        BufferedWriter commandWriter = null;
        try
        {
            // Launches the MILP-solver
            milpProcess = Runtime.getRuntime().exec(new String[]{"cbc"});
            commandWriter = new BufferedWriter(
                    new OutputStreamWriter(new DataOutputStream(milpProcess.getOutputStream())));
            commandWriter.write("import " + mpsFile.getAbsolutePath() + "\n");
            commandWriter.write("solve\n");
            commandWriter.write("directory c:\n"); // A needed fix to cope with unix-file-finding
            String solutionPath = solutionFile.getAbsolutePath();
            solutionPath = solutionPath.substring(3);
            commandWriter.write("solution " + solutionPath + "\n");
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
            if (milpEchoStr.contains("infeasible"))
            {
                throw new MilpException(milpEchoStr + " (specifications should be relaxed if possible).");
            }
            
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
                
                milpConstructor.addToMessages("\tOptimization time = " + runTime + "ms", 
                        SchedulingConstants.MESSAGE_TYPE_INFO); 
                milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " + objValue, 
                        SchedulingConstants.MESSAGE_TYPE_INFO);
                milpConstructor.addToMessages("\tNr of nodes = " + nrNodes +"; nr of iterations = " + nrIters, 
                        SchedulingConstants.MESSAGE_TYPE_INFO);
            }
        }
    }
    
    /**
     * Processes the output from the GLPK-solver, transforming it into a sequence 
     * of event firing times.
     */
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException
    {       
        Hashtable<Integer, Double> optimalTimeVarValues = new Hashtable<Integer, Double>();
        Hashtable<Integer, Integer> optimalBinVarValues = new Hashtable<Integer, Integer>();
        
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
                Integer varIndex = new Integer(str.substring(0, cutIndex).trim());
                
                str = str.substring(cutIndex).trim();
                cutIndex = str.indexOf(" ");
                Integer varValue = new Integer(str.substring(0, cutIndex).trim());
                
                optimalBinVarValues.put(new Integer(varIndex), varValue);
            }
            else 
            {
                cutIndex = str.indexOf("T");
                if (cutIndex > 0)
                {
                    str = str.substring(cutIndex + 1);
                    cutIndex = str.indexOf(" ");
                    Integer varIndex = new Integer(str.substring(0, cutIndex).trim());

                    str = str.substring(cutIndex).trim();
                    cutIndex = str.indexOf(" ");
                    Double varValue = new Double(str.substring(0, cutIndex).trim());

                    optimalTimeVarValues.put(varIndex, varValue);
                }
            }
        }
        
        // Initialize the arrays of optimal variable values
        optimalTimes = new double[milpConstructor.getDeltaTimes().length][];
        optimalAltPathVariables = new boolean[milpConstructor.getDeltaTimes().length][][];
        for (int i = 0; i < optimalTimes.length; i++)
        {
            optimalTimes[i] = new double[milpConstructor.getDeltaTimes()[i].length];
            optimalAltPathVariables[i] = new boolean[milpConstructor.getDeltaTimes()[i].length]
                    [milpConstructor.getDeltaTimes()[i].length];
        }
        
        // Fill the arrays of optimal times with appropriate values
        for (String varKey : timeVarIndexMap.keySet())
        {
            int plantIndex = (new Integer(varKey.substring(varKey.indexOf("[") + 1, varKey.indexOf(",")).trim())).intValue();
            int stateIndex = (new Integer(varKey.substring(varKey.indexOf(",") + 1, varKey.indexOf("]")).trim())).intValue();
            
            Double optimalVarValue = optimalTimeVarValues.get(timeVarIndexMap.get(varKey));
            if (optimalVarValue != null)
            {
                optimalTimes[plantIndex][stateIndex] = optimalVarValue.doubleValue();
            }
        }
        // Fill the arrays of optimal alt path variables with appropriate values
        for (String varKey : binVarIndexMap.keySet())
        {
            // If this is an alt.path variable, then store it. Otherwise, do nothing.
            if (varKey.contains("_from_") && varKey.contains("_to_"))
            {
                int[] plantStateIndices = unmakeAltPathsVariableStr(varKey);
                Integer optimalVarValue = optimalBinVarValues.get(binVarIndexMap.get(varKey));
                if (optimalVarValue != null)
                {
                    optimalAltPathVariables[plantStateIndices[0]][plantStateIndices[1]][plantStateIndices[2]] = 
                            (optimalVarValue.intValue() == 1);
                }
                else
                {
                    optimalAltPathVariables[plantStateIndices[0]][plantStateIndices[1]][plantStateIndices[2]] = false;
                }
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
    
    private int[] unmakeAltPathsVariableStr(String varStr)
    {
        int[] varIndices = new int[3];
        
        varIndices[0] = (new Integer(varStr.substring(1, varStr.indexOf("_")))).intValue();
        varIndices[1] = (new Integer(varStr.substring(varStr.indexOf("om_") + 3, varStr.indexOf("_to")))).intValue();
        varIndices[2] = (new Integer(varStr.substring(varStr.indexOf("to_") + 3, varStr.length()))).intValue();
        
        return varIndices;
    }
}
