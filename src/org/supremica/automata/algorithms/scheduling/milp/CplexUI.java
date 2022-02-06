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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author Avenir Kobetski
 */
public class CplexUI
        extends MpsUI
{
    public CplexUI(final Milp milpConstructor)
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
            // Launches the CPLEX-solver
            milpProcess = Runtime.getRuntime().exec(new String[]{"cplex"});
            // Create a command-line writer
            commandWriter = new BufferedWriter(
                    new OutputStreamWriter(new DataOutputStream(milpProcess.getOutputStream())));
            // Read the MILP-model
            commandWriter.write("read " + mpsFile.getAbsolutePath() + "\n");
            // Optimize
            commandWriter.write("optimize\n");
            // Write the results of optimization to a solution-file
            commandWriter.write("write " + solutionFile.getAbsolutePath() + "\n");
            // Authorize overwriting of the existing (temporary) solution file
            commandWriter.write("y\n");
            // Quit Cplex
            commandWriter.write("quit\n");
            // Close the command-line writer
            commandWriter.flush();
            commandWriter.close();
        }
        catch (final IOException milpNotFoundException)
        {
            milpConstructor.addToMessages("The Cplex-solver 'cplex.exe' not found. " +
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
//            System.out.println("echo: " + milpEchoStr);

            if (milpEchoStr.contains("time ="))
            {
                milpConstructor.addToMessages(milpEchoStr, SchedulingConstants.MESSAGE_TYPE_INFO);
            }
            else if (milpEchoStr.contains("Infeasibility"))
            {
                milpConstructor.addToMessages(milpEchoStr, SchedulingConstants.MESSAGE_TYPE_WARN);
            }
            else if (milpEchoStr.contains("No solution exists"))
            {
                if (milpEchoStr.contains("CPLEX Error"))
                {
                    milpEchoStr = milpEchoStr.substring(milpEchoStr.indexOf("CPLEX Error"));
                }
                throw new MilpException("\n" + milpEchoStr + " (specifications should be relaxed if possible).");
            }
        }
    }

    /**
     * Calls CplexSolutionParser to parse the xml-file containing the optimal MILP-solution.
     * These values are then sent to fillOptimalVarArrays()-method, common to all MPS-format solvers.
     */
    @Override
    public void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException
    {
        final CplexSolutionParser parser = new CplexSolutionParser(milpConstructor, solutionFile);
        parser.parseSolutionFile();

        fillOptimalVarArrays(parser.getOptimalTimeVarValues(), parser.getOptimalBinVarValues());
    }
}

class CplexSolutionParser extends DefaultHandler
{
    Milp milpConstructor;
    File solFile;

    Hashtable<Integer, Double> optimalTimeVarValues = new Hashtable<Integer, Double>();
    Hashtable<Integer, Integer> optimalBinVarValues = new Hashtable<Integer, Integer>();

    public CplexSolutionParser(final Milp milpConstructor, final File solFile)
    {
        this.milpConstructor = milpConstructor;
        this.solFile = solFile;
    }

    public void parseSolutionFile()
    {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        try
        {
            final SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(solFile, this);

        }
        catch (final Throwable t)
        {
            t.printStackTrace();
        }

    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final org.xml.sax.Attributes attr)
            throws SAXException
    {
        if (qName.equals("header"))
        {
            milpConstructor.addToMessages("\t\tOPTIMAL MAKESPAN: " +
                    milpConstructor.removeEpsilons((new Double(attr.getValue("objectiveValue"))).doubleValue()),
                        SchedulingConstants.MESSAGE_TYPE_INFO);
            milpConstructor.addToMessages("\tNr of nodes = " + attr.getValue("MIPNodes") +
                    "; nr of iterations = " + attr.getValue("MIPIterations"),
                    SchedulingConstants.MESSAGE_TYPE_INFO);
        }
        else if (qName.equals("variable"))
        {
            String varName = attr.getValue("name");
            if (varName.contains("X")) // This is a boolean variable
            {
                varName = varName.substring(1); //Remove X from the name (which leaves only the index of X in the optimalBinVarValues-map)
                optimalBinVarValues.put(new Integer(varName), new Integer((int)Math.round(new Double(attr.getValue("value"))))); // The rounding is added due to numerical precission errors (for example when 0 is represented by 1e-17)
            }
            else if (varName.contains("T")) // This is a continuous time variable
            {
                varName = varName.substring(1); //Remove T from the name (which leaves only the index of T in the optimalTimeVarValues-map)
                optimalTimeVarValues.put(new Integer(varName), new Double(attr.getValue("value")));
            }
        }
    }

    public Hashtable<Integer, Double> getOptimalTimeVarValues()
    {
        return optimalTimeVarValues;
    }
    public Hashtable<Integer, Integer> getOptimalBinVarValues()
    {
        return optimalBinVarValues;
    }
}