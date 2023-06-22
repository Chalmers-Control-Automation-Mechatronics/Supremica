/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

/**
 *
 * @author Avenir Kobetski
 */
public class CbcUI
        extends MpsUI
{
    public CbcUI(final Milp milpConstructor)
            throws Exception
    {
        super(milpConstructor);
    }

    @Override
    public void launchMilpSolver(final File mpsFile)
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
        catch (final IOException milpNotFoundException)
        {
            milpConstructor.addToMessages("The CBC-solver 'cbc.exe' not found. " +
                    "Make sure that it is registered in your path.", SchedulingConstants.MESSAGE_TYPE_ERROR);

            throw new MilpException(milpNotFoundException.getMessage());
        }

        // Listens for the output of MILP (that is the input to this application)...
        final BufferedReader milpEcho = new BufferedReader(
                new InputStreamReader(new DataInputStream(milpProcess.getInputStream())));

        // ...and prints it to stdout
        String milpEchoStr = "";
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
                final String objValue = milpEchoStr.substring(0, milpEchoStr.indexOf("after")).trim();

                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("after") + 6).trim();
                final String nrNodes = milpEchoStr.substring(0, milpEchoStr.indexOf("node")).trim();

                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("and") + 4).trim();
                final String nrIters = milpEchoStr.substring(0, milpEchoStr.indexOf("iteration")).trim();

                milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("took") + 5).trim();
                final String runTime = milpEchoStr.substring(0, milpEchoStr.indexOf("sec")).trim();

                milpConstructor.addToMessages("\tOptimization time = " + runTime + "ms",
                        SchedulingConstants.MESSAGE_TYPE_INFO);
                milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " + objValue,
                        SchedulingConstants.MESSAGE_TYPE_INFO);
                milpConstructor.addToMessages("\tNr of nodes = " + nrNodes +"; nr of iterations = " + nrIters,
                        SchedulingConstants.MESSAGE_TYPE_INFO);
            }
        }
    }

    @Override
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException
    {
      final Hashtable<Integer, Double> optimalTimeVarValues = new Hashtable<Integer, Double>();
      final Hashtable<Integer, Integer> optimalBinVarValues = new Hashtable<Integer, Integer>();
      final BufferedReader r = new BufferedReader(new FileReader(solutionFile));
      try {
        // Go through the solution file and extract the suggested optimal times for each state
        String str;
        while ((str = r.readLine()) != null)
        {
            int cutIndex = str.indexOf("X");
            if (cutIndex > 0)
            {
                str = str.substring(cutIndex + 1);
                cutIndex = str.indexOf(" ");
                final Integer varIndex = Integer.parseInt(str.substring(0, cutIndex).trim());

                str = str.substring(cutIndex).trim();
                cutIndex = str.indexOf(" ");
                final Integer varValue = Integer.parseInt(str.substring(0, cutIndex).trim());

                optimalBinVarValues.put(varIndex, varValue);
            }
            else
            {
                cutIndex = str.indexOf("T");
                if (cutIndex > 0)
                {
                    str = str.substring(cutIndex + 1);
                    cutIndex = str.indexOf(" ");
                    final Integer varIndex = Integer.parseInt(str.substring(0, cutIndex).trim());

                    str = str.substring(cutIndex).trim();
                    cutIndex = str.indexOf(" ");
                    final Double varValue = Double.parseDouble(str.substring(0, cutIndex).trim());

                    optimalTimeVarValues.put(varIndex, varValue);
                }
            }
        }
      } finally {
        r.close();
      }
      fillOptimalVarArrays(optimalTimeVarValues, optimalBinVarValues);
    }
}

