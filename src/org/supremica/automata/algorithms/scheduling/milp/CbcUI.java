/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.*;
import java.util.Hashtable;
import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

/**
 *
 * @author Avenir Kobetski
 */
public class CbcUI 
        extends MpsUI
{
    public CbcUI(Milp milpConstructor)
            throws Exception
    {
        super(milpConstructor);
    }
    
    public void launchMilpSolver(File mpsFile)
            throws MilpException, IOException
    {      
        BufferedWriter commandWriter = null;
        try
        {
            // Launches the CBC-solver
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
}
