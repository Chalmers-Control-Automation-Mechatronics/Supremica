/*
 * RandomPathUsingMilp.java
 *
 * Created on den 4 juli 2007, 17:12
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import org.supremica.automata.Automata;
import org.supremica.util.ActionTimer;

/**
 *
 * @author Avenir Kobetski
 */
public class RandomPathUsingMilp
        extends Milp
{    
    /** Creates a new instance of RandomPathUsingMilp */
    public RandomPathUsingMilp(Automata theAutomata, boolean buildSchedule, boolean balanceVelocities)
        throws Exception
    {
        super(theAutomata, buildSchedule, balanceVelocities);
    }
        
//  TEMP OUTCOMMENTED DUE TO COMPILATION ERRORS (DUE TO MOVE TO MILP-PACKAGE).  
//    protected void callMilpSolver()
//            throws Exception
//    {
//        File suboptimalModelFile = File.createTempFile("milp", ".mod");
//        infoMsgs += "Creating suboptimal model file... " + suboptimalModelFile.getName();
//        
//        // The hashtable containing internal indices of the mutex variables (with the variable names as keys)
//        Hashtable<String, Integer> boolVarTable = null;
//        
//        // Each value of this hashtable contains all internal indices of the variables that are 
//        // consecutive-booking-coupled (they must have the same value to avoid deadlocks) with the 
//        // variable having the internal index stored in the key
//        Hashtable<Integer, ArrayList<Integer>> consecutiveBookingCouplingTable = null;
//        
//        // The current values of the boolean combinations, ordered by internal indices
//        int[] currBoolCombination = null;
//        
//        // The number of times to try random variable values. If a solution is not found randomly,
//        // all variable combinations are searched through in a systematic manner to be able to know
//        // that no solution exists. (Obs: Don't initialize this variable with a negative number!)
//        int nrOfRandomTrials = 1;
//        
//        outerLoop: while (true)
//        {
//            //temp
//            if (nrOfRandomTrials < 1)
//            {
//                break outerLoop;
//            }
//            
//            BufferedReader r = new BufferedReader(new FileReader(modelFile));
//            BufferedWriter w = new BufferedWriter(new FileWriter(suboptimalModelFile));
//            
//            boolean boolVarsAlreadyFound = (boolVarTable == null) ? false : true;
//            if (!boolVarsAlreadyFound)
//            {
//                boolVarTable = new Hashtable<String, Integer>();
//                consecutiveBookingCouplingTable = new Hashtable<Integer, ArrayList<Integer>>();
//            }
//
//            //The internal index of the boolean variables 
//            int internalVarIndex = 0;
//            
//            // Copy the MILP-file until the end of "model"-section, i.e. until the beginning of "data"-section.
//            // Store the boolean variables that this file contains if not already done. 
//            String str;
//            while (!(str = r.readLine()).contains(("data;")))
//            {
//                if (!boolVarsAlreadyFound)
//                {
//                    // Add the names of the boolean variables to boolVarTable
//                    if (str.contains("binary"))
//                    {
//                        boolVarTable.put(str.substring(4, str.indexOf(",")), internalVarIndex++);
//                        //temp
//                        infoMsgs += "adding " + str.substring(4, str.indexOf(",")) + " to the var-table (index = " + (internalVarIndex-1) + ")";
//                    }
//                    // Add the variable indices for all consecutive-bookings of the zones to consecutiveBookingCouplingTable
//                    else if (str.contains("consecutive_booking"))
//                    {
//                        // Extract the names of the boolean variables that represent non-cross-booked zones
//                        String varLeft = str.substring(str.indexOf(":") + 1, str.indexOf(">=")).trim();
//                        String varRight = str.substring(str.indexOf(">=") + 2).trim();
//                        if (varRight.contains(" - "))
//                        {
//                            varRight = varRight.substring(0, varRight.indexOf(" - ")).trim();
//                        }
//                        
//                        // Retrieve the internal indices for the current variables
//                        Integer varLeftIndex = boolVarTable.get(varLeft);
//                        Integer varRightIndex = boolVarTable.get(varRight);
//                        
//                        // If no cross-coupling for the current key is found, 
//                        // create new list of corresponing consecutive-booking-indices
//                        ArrayList<Integer> consecutiveBookingCouplingEntry = consecutiveBookingCouplingTable.get(varLeftIndex);
//                        if (consecutiveBookingCouplingEntry == null)
//                        {
//                            consecutiveBookingCouplingEntry = new ArrayList<Integer>();
//                        }
//                        
//                        // If no cross-coupling between the current key-value-pair is found,
//                        // add the value-index to the current consecutive-booking-list
//                        if (!consecutiveBookingCouplingEntry.contains(varRightIndex))
//                        {
//                            consecutiveBookingCouplingEntry.add(varRightIndex);
//                        }
//
//                        // Update the table of consecutive-booking-indices
//                        consecutiveBookingCouplingTable.put(varLeftIndex, consecutiveBookingCouplingEntry);
//                    }
//                }
//                
//                w.write(str + "\n");
//            }
//            
//            // If the nr of random trials is positive, set boolean variables randomly
//            if (nrOfRandomTrials > 0) 
//            {
//                nrOfRandomTrials--;
//                
//                // Initialize the variable array if not already done
//                if (currBoolCombination == null)
//                {
//                    currBoolCombination = new int[boolVarTable.size()];
//                }
//                
//                // At each new attempt, initialize all variable values to -1
//                for (int i = 0; i < currBoolCombination.length; i++)
//                {
//                    currBoolCombination[i] = -1;
//                }
//
//                // Assign variable values randomly if not already done (i.e. if the values are equal to -1) 
//                for (int i = 0; i < currBoolCombination.length; i++)
//                {
//                    if (currBoolCombination[i] == -1)
//                    {
//                        currBoolCombination[i] = (int) Math.round(Math.random());
//                        
//                        // Update all cross-coupled variables to have the same value as currBoolCombination[i].
//                        // Note that the whole coupling-chain should be updated, i.e. if alpha_1 is connected
//                        // with alpha_2, while alpha_2 -> alpha_3, also alpha_3 should be updated in this step.
//                        // (If this is done in several for-steps, conflicting cross-coupling-updates can occur.)
//                        ArrayList<Integer> keyVarList = new ArrayList<Integer>();
//                        keyVarList.add(new Integer(i));                       
//                        while (!keyVarList.isEmpty())
//                        {
//                            Integer keyVarIndex = keyVarList.remove(0);
//                            
//                            ArrayList<Integer> valueVarList = consecutiveBookingCouplingTable.get(new Integer(keyVarIndex));                        
//                            if (valueVarList != null)
//                            {
//                                for (Iterator<Integer> valueIndicesIt = valueVarList.iterator(); valueIndicesIt.hasNext(); )
//                                {
//                                    Integer valueVarIndex = valueIndicesIt.next();
//
//                                    // If the currently examined cross-variable have not been updated previously,
//                                    // set the correct value for it and continue to unroll the cross-coupling chain
//                                    // by examining the currently updated variables couplings to the other variables
//                                    if (currBoolCombination[valueVarIndex] == -1)
//                                    {
//                                        currBoolCombination[valueVarIndex] = currBoolCombination[keyVarIndex];
//                                        keyVarList.add(valueVarIndex);
//
//                                        //temp
//                                        warnMsgs += valueVarIndex + " follows " + keyVarIndex;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//// TEMP: temporarily outcommented, only random trials for the moment.
////            // Else start systematic looping through all combinations of boolean variables 
////            // (the first combination is that all variables are zero).
////            else if (nrOfRandomTrials == 0) // Set all boolean variables to 0
////            {
////                nrOfRandomTrials--;
////                if (currBoolCombination == null)
////                {
////                    currBoolCombination = new int[boolVarTable.size()];
////                }
////
////                for (int i = 0; i < currBoolCombination.length; i++)
////                {
////                    currBoolCombination[i] = 0;
////                }
////            }
////            // Continue the systematic looping through the variable combinations. 
////            else // Update boolean variables 
////            {
////                for (int i = 0; i < currBoolCombination.length; i++)
////                {
////                    if (currBoolCombination[i] == 0)
////                    {
////                        currBoolCombination[i] = 1;
////                        break;
////                    }
////                    else
////                    {
////                        currBoolCombination[i] = 0;
////                    }
////
////                    // If all boolean combinations have been tested, an exception is thrown, terminating the loop.
////                    if ((i == currBoolCombination.length - 1) && (currBoolCombination[i] == 0))
////                    {
////                        throw new Exception("All combinations of boolean variables were tested, no path found.");
////                    }
////                }
////            }
//
//            // Write current values of the boolean variables to the "model"-file
//            for (String varName : boolVarTable.keySet())
//            {
//                int currIndex = boolVarTable.get(varName);
//                w.write("random_path_" + currIndex + " : " + varName + " = " + 
//                        currBoolCombination[currIndex] + ";\n");
//            }
//
//            // Finish the MILP-file by copying the "data"-section
//            w.newLine();
//            w.write("data;\n");
//            while ((str = r.readLine()) != null)
//            {
//                w.write(str + "\n");
//            }
//            
//            // Close the writing stream
//            w.flush();
//            w.close();
//
//            // Call the MILP-solver
//            try
//            {
//                super.callMilpSolver(suboptimalModelFile);
//                infoMsgs += "A path was found!!!";
//                return;
//            }
//            // If there is no solution with current variable values, an exception is thrown. Here it is caught 
//            // and new combination of boolean variable values is tested. 
//            catch (Exception ex)
//            {
//                if (ex.getMessage().contains("NO") && ex.getMessage().contains("FEASIBLE SOLUTION"))
//                {
//                    warnMsgs += "No path with current variable values";
////                    errorMsgs += (ex.getMessage();
////                    throw ex;
//                }
//            }
//        }
//    }
}
