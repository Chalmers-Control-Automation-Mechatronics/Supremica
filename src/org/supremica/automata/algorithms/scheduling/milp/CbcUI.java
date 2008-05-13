/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.*;
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

}
