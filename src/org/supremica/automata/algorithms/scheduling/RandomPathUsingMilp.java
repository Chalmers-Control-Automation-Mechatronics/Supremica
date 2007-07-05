/*
 * RandomPathUsingMilp.java
 *
 * Created on den 4 juli 2007, 17:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import org.supremica.automata.Automata;
import org.supremica.gui.ScheduleDialog;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.util.ActionTimer;

/**
 *
 * @author Avenir Kobetski
 */
public class RandomPathUsingMilp
        extends Milp
{
    private static Logger logger = LoggerFactory.createLogger(RandomPathUsingMilp.class);
    
    /** Creates a new instance of RandomPathUsingMilp */
    public RandomPathUsingMilp(Automata theAutomata, boolean buildSchedule, ScheduleDialog scheduleDialog)
        throws Exception
    {
        super(theAutomata, buildSchedule, scheduleDialog);
    }
        
    protected void callMilpSolver()
            throws Exception
    {
        File suboptimalModelFile = File.createTempFile("milp", ".mod");
        logger.info("Creating suboptimal model file... " + suboptimalModelFile.getName());
        
        ArrayList<String> boolVarList = null;
        int[] currBoolCombination = null;
        
        // The number of times to try random variable values. If a solution is not found randomly,
        // all variable combinations are searched through in a systematic manner to be able to know
        // that no solution exists. (Obs: Don't initialize this variable with a negative number!)
        int nrOfRandomTrials = 10;
        while (true)
        {
            BufferedReader r = new BufferedReader(new FileReader(modelFile));
            BufferedWriter w = new BufferedWriter(new FileWriter(suboptimalModelFile));
            
            boolean boolVarsAlreadyFound = (boolVarList == null) ? false : true;
            if (!boolVarsAlreadyFound)
            {
                boolVarList = new ArrayList<String>();
            }

            String str;
            int counter = 1;
            while ((str = r.readLine()) != null)
            {
                if (!boolVarsAlreadyFound && str.contains("binary"))
                {
                    boolVarList.add(str.substring(4, str.indexOf(",")));
                }
                else if (str.contains("data;"))
                {
                    if (nrOfRandomTrials > 0) // Set boolean variables randomly
                    {
                        nrOfRandomTrials--;
                        if (currBoolCombination == null)
                        {
                            currBoolCombination = new int[boolVarList.size()];
                        }
                        
                        for (int i = 0; i < currBoolCombination.length; i++)
                        {
                            currBoolCombination[i] = (int) Math.round(Math.random());
                        }
                    }
                    else if (nrOfRandomTrials == 0) // Set all boolean variables to 0
                    {
                        nrOfRandomTrials--;
                        if (currBoolCombination == null)
                        {
                            currBoolCombination = new int[boolVarList.size()];
                        }
                        
                        for (int i = 0; i < currBoolCombination.length; i++)
                        {
                            currBoolCombination[i] = 0;
                        }
                    }
                    else // Update boolean variables 
                    {
                        for (int i = 0; i < currBoolCombination.length; i++)
                        {
                            if (currBoolCombination[i] == 0)
                            {
                                currBoolCombination[i] = 1;
                                break;
                            }
                            else
                            {
                                currBoolCombination[i] = 0;
                            }

                            if ((i == currBoolCombination.length - 1) && (currBoolCombination[i] == 0))
                            {
                                throw new Exception("All combinations of boolean variables were tested, no path found.");
                            }
                        }
                    }

                    for (int i = 0; i < currBoolCombination.length; i++)
                    {
                        w.write("random_path_" + counter++ + " : " + boolVarList.get(i) + " = " + currBoolCombination[i] + ";\n");
                    }

                    w.newLine();
                }

                w.write(str + "\n");
            }
            w.flush();
            w.close();

            try
            {
                super.callMilpSolver(suboptimalModelFile);
                logger.info("A path was found!!!");
                return;
            }
            catch (Exception ex)
            {
                if (ex.getMessage().contains("NO") && ex.getMessage().contains("FEASIBLE SOLUTION"))
                {
                    logger.info("No path with current variable values");
//                    logger.error(ex.getMessage());
//                    throw ex;
                }
            }
        }
    }
}
