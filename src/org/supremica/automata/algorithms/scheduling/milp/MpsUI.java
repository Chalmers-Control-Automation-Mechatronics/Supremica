/*
 * MpsUI.java
 *
 * Created on den 14 februari 2008, 16:07
 * @author Avenir Kobetski
 */

package org.supremica.automata.algorithms.scheduling.milp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.StringTokenizer;

import org.supremica.automata.algorithms.scheduling.SchedulingConstants;

/**
 * This class constructs MILP problem files of MPS-format. The launchMilpSolver()-
 * and processSolutionFile()-methods are solver specific and thus left as abstract,
 * to be implemented by the MILP solvers that accept the MPS-format.
 */
public abstract class MpsUI
        implements MilpSolverUI
{
    /** The *.mps file that serves as an input to the Glpk-solver. */
    protected File mpsFile;

    /** The *.sol file that stores the solution, i.e. the output of the Glpk-solver. */
    protected File solutionFile;

    /** The process responsible for the MILP-solver. */
    protected Process milpProcess;

    /** The pointer to the constructor of MILP-formulation. */
    protected Milp milpConstructor = null;

    /** The optimal times (for each plant-state) that the GLPK solver returns */
    protected double[][] optimalTimes = null;

    /** The optimal alt. path variables (booleans) for each [plant][start_state][end_state] */
    protected boolean[][][] optimalAltPathVariables = null;

    Hashtable<String, Integer> binVarIndexMap = new Hashtable<String, Integer>();
    Hashtable<String, Integer> timeVarIndexMap = new Hashtable<String, Integer>();
    ArrayList<double[]>[] varCoeffs;
    ArrayList<Double> bUpper;
    ArrayList<Integer> equalityRowIndices;

    /** Creates a new instance of GlpkUI */
    public MpsUI(final Milp milpConstructor)
        throws Exception
    {
        this.milpConstructor = milpConstructor;
    }

    @Override
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
     * Creates the *.mps-file that contains the MILP-formulation used as an input
     * to the MILP-solver.
     */
    @Override
    @SuppressWarnings("unchecked")
    public void createModelFile()
        throws MilpException, IOException
    {
        final BufferedWriter w = new BufferedWriter(new FileWriter(mpsFile));

        // Map the variable names to an index
        for (final int[] altPathVar : milpConstructor.getAltPathVaribles())
        {
            binVarIndexMap.put(makeAltPathsVariableStr(altPathVar), binVarIndexMap.size());
        }
        for (final String mutexVar : milpConstructor.getMutexVariables())
        {
            binVarIndexMap.put(mutexVar, binVarIndexMap.size());
        }
        for (final String internalPrecVar : milpConstructor.getInternalPrecVariables())
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
        for (final int[] constr : milpConstructor.getCycleTimeConstraints())
        {
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter, 1});
            varCoeffs[binVarIndexMap.size()].add(new double[]{constraintCounter++, -1});
            bUpper.add(0.0);
        }

        // The initial (precedence) constraints
        for (final int[] constr : milpConstructor.getInitPrecConstraints())
        {
            varCoeffs[timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]")].
                    add(new double[]{constraintCounter++, -1});
            bUpper.add(-1 * milpConstructor.getDeltaTimes()[constr[0]][constr[1]]);
        }

        // The precedence constraints
        for (final int[] constr : milpConstructor.getPrecConstraints())
        {
            final int precedingVarIndex = timeVarIndexMap.get("time[" + constr[0] + ", " + constr[1] + "]");
            final int followingVarIndex = timeVarIndexMap.get("time[" + constr[0] + ", " + constr[2] + "]");

            double rhs = -1 * (milpConstructor.getDeltaTimes()[constr[0]][constr[2]] + SchedulingConstants.EPSILON);
            //bUpper.add(new Double(-1 * (milpConstructor.getDeltaTimes()[constr[0]][constr[2]] + SchedulingConstants.EPSILON)));

            // if this is a state where several paths converge, add a constraint,
            // containing an upstreams alt.paths variable for each converging path...
            if (constr.length > 3)
            {
                rhs += SchedulingConstants.BIG_M_VALUE;
                for (final int[] altPathVar : milpConstructor.getActiveAltPathVars(new int[]{constr[0], constr[1], constr[3]}))
                {
                    varCoeffs[precedingVarIndex].add(new double[]{constraintCounter, 1});
                    varCoeffs[followingVarIndex].add(new double[]{constraintCounter, -1});
                    varCoeffs[binVarIndexMap.get(milpConstructor.makeAltPathsVariable(altPathVar[0], altPathVar[1], altPathVar[2]))].
                            add(new double[]{constraintCounter, SchedulingConstants.BIG_M_VALUE});
                    bUpper.add(rhs);

                    constraintCounter++;
                }
            }
            else
            {
                varCoeffs[precedingVarIndex].add(new double[]{constraintCounter, 1});
                varCoeffs[followingVarIndex].add(new double[]{constraintCounter, -1});
                bUpper.add(rhs);

                constraintCounter++;
            }

            //TODO: Kolla om man vill lägga till aktiva altpathvars till varje prec.constraint, minskar detta LP-komplexiteten?

//            //test
//            java.util.Collection<int[]> activeAltVars = milpConstructor.getActiveAltPathVars(new int[]{constr[0], constr[1], constr[3]});
//            for (int[] altVar : activeAltVars)
//            {
//                int binVarIndex = binVarIndexMap.get(milpConstructor.makeAltPathsVariable(altVar[0], altVar[1], altVar[2]));
//                System.out.println("counter = " + constraintCounter + "; found x" + binVarIndex);
//                varCoeffs[binVarIndex].add(new double[]{constraintCounter, SchedulingConstants.BIG_M_VALUE});
//            }
//            if (activeAltVars.size() > 0)
//            {
//                System.out.println(SchedulingConstants.BIG_M_VALUE + " added to rhs");
//                rhs += SchedulingConstants.BIG_M_VALUE;
//            }
        }

        // The alternative paths constraints
        for (final ArrayList<int[]> constrBlock : milpConstructor.getNewAltPathsConstraints())
        {
            for (final int[] constr : constrBlock)
            {
                varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                        constr[0], constr[1]))].add(new double[]{constraintCounter, 1});
                varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                        constr[0], constr[2]))].add(new double[]{constraintCounter, -1});
                varCoeffs[binVarIndexMap.get(milpConstructor.makeAltPathsVariable(
                        constr[0], constr[1], constr[2]))].add(
                        new double[]{constraintCounter, SchedulingConstants.BIG_M_VALUE});
                bUpper.add(SchedulingConstants.BIG_M_VALUE - SchedulingConstants.EPSILON -
                           milpConstructor.getDeltaTimes()[constr[0]][constr[2]]);
                constraintCounter++;
            }
            for (final int[] constr : constrBlock)
            {
                varCoeffs[binVarIndexMap.get(milpConstructor.makeAltPathsVariable(
                        constr[0], constr[1], constr[2]))].add(
                        new double[]{constraintCounter, 1});
            }
            bUpper.add(1.0);
            equalityRowIndices.add(constraintCounter++);
        }


        // The mutex constraints
//        for (Constraint constr : milpConstructor.getMutexConstraints())
        for (final int[] constr : milpConstructor.getMutexConstraints())
        {
            //test
            // Find all active alt path variables for this b1u1-b2u2-pair
            final Hashtable<Integer, Integer> allActiveAltBinVars = new Hashtable<Integer, Integer>();
            int convergingStatesCounter = 0;
            for (int i=0; i<6; i+=5)
            {
                for (int j=i+1; j<i+5; j+=2)
                {
                    final java.util.Collection<int[]> currActiveVars = milpConstructor.getActiveAltPathVars(
                            new int[]{constr[i], constr[j], constr[j+1]});
                    if (currActiveVars.size() > 0)
                    {
                        convergingStatesCounter++;
                        for (final int[] altVar : currActiveVars)
                        {
                            final Integer currAltVar = binVarIndexMap.get(milpConstructor.makeAltPathsVariable(
                                                                    altVar[0], altVar[1], altVar[2]));
                            final Integer currVarFrequency = allActiveAltBinVars.get(currAltVar);
                            if (currVarFrequency == null)
                            {
                                allActiveAltBinVars.put(currAltVar, 1);
                            }
                            else
                            {
                                allActiveAltBinVars.put(currAltVar, currVarFrequency + 1);
                            }
                        }
                    }
                }
            }

            final int mutexVarIndex = binVarIndexMap.get(milpConstructor.makeMutexVariable(
                                        constr[0], constr[5], constr[10], constr[11]));

            // Use the total alt path variable in the mutex constraint (note that sum(tot_alt_path) <= 4)
            varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                    constr[5], constr[8]))].add(new double[]{constraintCounter, 1});
            varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                    constr[0], constr[1]))].add(new double[]{constraintCounter, -1});
            varCoeffs[mutexVarIndex].add(new double[]{constraintCounter, -1*SchedulingConstants.BIG_M_VALUE});
            for (final Integer altPathVarIndex : allActiveAltBinVars.keySet())
            {
                varCoeffs[altPathVarIndex].add(new double[]{
                    constraintCounter, allActiveAltBinVars.get(altPathVarIndex) * SchedulingConstants.BIG_M_VALUE});
            }
            bUpper.add(convergingStatesCounter * SchedulingConstants.BIG_M_VALUE - SchedulingConstants.EPSILON);
            constraintCounter++;

            // The dual constraint
            varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                    constr[0], constr[3]))].add(new double[]{constraintCounter, 1});
            varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                    constr[5], constr[6]))].add(new double[]{constraintCounter, -1});
            varCoeffs[mutexVarIndex].add(new double[]{constraintCounter, SchedulingConstants.BIG_M_VALUE});
            for (final Integer altPathVarIndex : allActiveAltBinVars.keySet())
            {
                varCoeffs[altPathVarIndex].add(new double[]{
                    constraintCounter, allActiveAltBinVars.get(altPathVarIndex) * SchedulingConstants.BIG_M_VALUE});
            }
            bUpper.add((1 + convergingStatesCounter) * SchedulingConstants.BIG_M_VALUE - SchedulingConstants.EPSILON);
            constraintCounter++;

            // Set mutexVar := 0 if any state involved in the current b/u-choice is unreached
            if (convergingStatesCounter > 0)
            {
                varCoeffs[mutexVarIndex].add(new double[]{constraintCounter, 1});
                for (final Integer altPathVarIndex : allActiveAltBinVars.keySet())
                {
                    varCoeffs[altPathVarIndex].add(new double[]{
                        constraintCounter, -1.0 / convergingStatesCounter});
                }
                bUpper.add(0.0);
                constraintCounter++;
            }
        }

        // The constraints due to external specifications //TODO... E-row!!!
        for (final ArrayList<int[]> xorConstraintsBlock : milpConstructor.getXorConstraints())
        {

            int rhs = 1;
            for (final int[] constr : xorConstraintsBlock)
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

            bUpper.add((double) rhs);
        }

        // The circular wait constraints
        final IntArrayTreeSet unfeasiblePairs = new IntArrayTreeSet(); //test
        for (final CircularWaitConstraintBlock constraint : milpConstructor.getCircularWaitConstraints())
        {
            //test
            final ArrayList<int[]> currUFPair = new ArrayList<int[]>();
            final boolean testReduction = false; // use this flag to override the reduction test

            // The counters of 1's in the rhs of the constraint-to-be
            int uBoundCW = -1;
            int uBoundUF = -1;

            for (final int[] varInfo : constraint)
            {
                final String mutexVar = makeMutexVariableStr(varInfo); // make the mutex-variable string

                int coeffUF = -1;
                // if the current constraint info has correct plant ordering, the
                // unfeasibility constraint should be (1 - var). In such a case, uBoundUF
                // must be increased while the coefficient of 'var' is +1 (since 'var' is move to lhs).
                if (varInfo[1] < varInfo[2])
                {
                    uBoundUF++;
                    coeffUF = 1;
                }
                // Otherwise the coefficient is -1 for 'var' in the unfeasibility constraint
                else
                {
                    uBoundCW++;
                }

                //test
                if (testReduction && constraint.size() == 2)
                {
                    final int ufVarValue = (coeffUF == -1) ? 0 : 1;
                    currUFPair.add(new int[]{binVarIndexMap.get(mutexVar), ufVarValue});
                }
                else
                {
                    // Add the coefficient for 'var' in the unfeasibility constraint
                    varCoeffs[binVarIndexMap.get(mutexVar)].add(new double[]{constraintCounter, coeffUF});
                    if (!constraint.hasBuffer())
                    {
                        // If there is no buffer here, add the coefficient for 'var' in the circular wait constraint.
                        // Note that the coefficient is opposite (in sign) to the UF-coefficient.
                        varCoeffs[binVarIndexMap.get(mutexVar)].add(new double[]{constraintCounter + 1, -1*coeffUF});
                    }
                }
            }

            //test
            if (testReduction && currUFPair.size() > 0)
            {
                boolean similarPairFound = false;

                final java.util.SortedSet<int[]> headSet = unfeasiblePairs.headSet(new int[]{currUFPair.get(0)[0]+1, 0, 0, 0});
                final java.util.SortedSet<int[]> currVarSet = headSet.tailSet(new int[]{currUFPair.get(0)[0], 0, 0, 0});

                if (currVarSet.size() > 0)
                {
                    for (final int[] currPair : currVarSet)
                    {
                        final int[] similarPair = new int[]{currPair[2], Math.abs(currPair[3] - 1),
                                                      currUFPair.get(1)[0], currUFPair.get(1)[1]};
                        if (unfeasiblePairs.contains(similarPair))
                        {
                            similarPairFound = true;
                            break;
                        }
                    }
                }

                if (!similarPairFound)
                {
                    unfeasiblePairs.add(new int[]{currUFPair.get(0)[0], currUFPair.get(0)[1],
                                                  currUFPair.get(1)[0], currUFPair.get(1)[1]});

                    double currCoeff = (currUFPair.get(0)[1] == 0) ? -1 : 1;
                    varCoeffs[currUFPair.get(0)[0]].add(new double[]{constraintCounter, currCoeff});
                    if (!constraint.hasBuffer())
                    {
                        // If there is no buffer here, add the coefficient for 'var' in the circular wait constraint.
                        // Note that the coefficient is opposite (in sign) to the UF-coefficient.
                        varCoeffs[currUFPair.get(0)[0]].add(new double[]{constraintCounter + 1, -1*currCoeff});
                    }
                    currCoeff = (currUFPair.get(1)[1] == 0) ? -1 : 1;
                    varCoeffs[currUFPair.get(1)[0]].add(new double[]{constraintCounter, currCoeff});
                    if (!constraint.hasBuffer())
                    {
                        // If there is no buffer here, add the coefficient for 'var' in the circular wait constraint.
                        // Note that the coefficient is opposite (in sign) to the UF-coefficient.
                        varCoeffs[currUFPair.get(1)[0]].add(new double[]{constraintCounter + 1, -1*currCoeff});
                    }

                    final double uBound = currUFPair.get(0)[1] + currUFPair.get(1)[1] - 1;
                    bUpper.add(uBound);
                    if (!constraint.hasBuffer())
                    {
                        bUpper.add(-uBound);
                        constraintCounter++;
                    }
                }
            }
            else
            {
                // Add the upper bounds for the new constraints
                bUpper.add((double) uBoundUF);
                if (!constraint.hasBuffer())
                {
                    bUpper.add((double) uBoundCW);
                    constraintCounter++;
                }
            }

            constraintCounter++;
        }



        // The shared events constraints (a block of constrains for each shared event)
        for (final ArrayList<ArrayList<ArrayList<int[]>>> eventBlock : milpConstructor.getSharedEventConstraints())
        {
            for (int i1 = 0; i1 < eventBlock.size() - 1; i1++)
            {
                final ArrayList<ArrayList<int[]>> allSharedTimeVarsInFirstPlant = eventBlock.get(i1);

                for (int i2 = i1+1; i2 < eventBlock.size(); i2++)
                {
                    final ArrayList<ArrayList<int[]>> allSharedTimeVarsInSecondPlant = eventBlock.get(i2);

                    // The hashtables are needed to ensure that the event occurs as many times in both plants
                    final Hashtable<Integer, Integer> firstPlantFrequencyMap = new Hashtable<Integer, Integer>();
                    final Hashtable<Integer, Integer> secondPlantFrequencyMap = new Hashtable<Integer, Integer>();
                    // Forced occurrences of an event (no path choices upstreams) are stored at index -1
                    firstPlantFrequencyMap.put(-1, 0);
                    secondPlantFrequencyMap.put(-1, 0);

                    for (int j1 = 0; j1 < allSharedTimeVarsInFirstPlant.size(); j1++)
                    {
                        final ArrayList<int[]> currSharedTimeVarsInFirstPlant = allSharedTimeVarsInFirstPlant.get(j1);
                        final int[] firstPlantState = currSharedTimeVarsInFirstPlant.get(0);

                        // Update the frequency of forced event occurrence if there are no altPath variables for this plant-state
                        if (currSharedTimeVarsInFirstPlant.size() == 1)
                        {
                            final int forcedFrequency = firstPlantFrequencyMap.get(-1);
                            firstPlantFrequencyMap.put(-1, forcedFrequency + 1);
                        }

                        for (int j2 = 0; j2 < allSharedTimeVarsInFirstPlant.size(); j2++)
                        {
                            final ArrayList<int[]> currSharedTimeVarsInSecondPlant = allSharedTimeVarsInSecondPlant.get(j2);
                            final int[] secondPlantState = currSharedTimeVarsInSecondPlant.get(0);

                            // Update the frequency of forced event occurrence if there are no altPath variables for this plant-state
                            if (currSharedTimeVarsInSecondPlant.size() == 1)
                            {
                                final int forcedFrequency = secondPlantFrequencyMap.get(-1);
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
                            final Hashtable<Integer, Integer> currFrequencyMap = new Hashtable<Integer, Integer>();
                            for (int k1 = 1; k1 < currSharedTimeVarsInFirstPlant.size(); k1++)
                            {
                                final int altPathIndex = binVarIndexMap.get(makeAltPathsVariableStr(
                                        currSharedTimeVarsInFirstPlant.get(k1)));

                                Integer currFrequency = currFrequencyMap.get(altPathIndex);
                                if (currFrequency == null)
                                {
                                    currFrequency = 0;
                                }
                                currFrequencyMap.put(altPathIndex, currFrequency.intValue() + 1);

                                Integer firstPlantFrequency = firstPlantFrequencyMap.get(altPathIndex);
                                if (firstPlantFrequency == null)
                                {
                                    firstPlantFrequency = 0;
                                }
                                firstPlantFrequencyMap.put(altPathIndex, firstPlantFrequency.intValue() + 1);

                                altPathCounter++;
                            }
                            for (int k2 = 1; k2 < currSharedTimeVarsInSecondPlant.size(); k2++)
                            {
                                final int altPathIndex = binVarIndexMap.get(makeAltPathsVariableStr(
                                        currSharedTimeVarsInSecondPlant.get(k2)));

                                Integer currFrequency = currFrequencyMap.get(altPathIndex);
                                if (currFrequency == null)
                                {
                                    currFrequency = 0;
                                }
                                currFrequencyMap.put(altPathIndex, currFrequency.intValue() + 1);

                                Integer secondPlantFrequency = secondPlantFrequencyMap.get(altPathIndex);
                                if (secondPlantFrequency == null)
                                {
                                    secondPlantFrequency = 0;
                                }
                                secondPlantFrequencyMap.put(altPathIndex, secondPlantFrequency.intValue() + 1);

                                altPathCounter++;
                            }

                            // The occurrence frequencies are added to the coefficients of current row for each variable
                            for (final Integer altPathIndex : currFrequencyMap.keySet())
                            {
                                varCoeffs[altPathIndex].add(new double[]{constraintCounter,
                                    SchedulingConstants.BIG_M_VALUE * currFrequencyMap.get(altPathIndex)});
                                varCoeffs[altPathIndex].add(new double[]{constraintCounter + 1,
                                    SchedulingConstants.BIG_M_VALUE * currFrequencyMap.get(altPathIndex)});
                            }

                            // Two right-hand-side-expressions are needed
                            bUpper.add((double) SchedulingConstants.BIG_M_VALUE * altPathCounter);
                            bUpper.add((double) SchedulingConstants.BIG_M_VALUE * altPathCounter);

                            constraintCounter += 2;
                        }
                    }

                    // The last constraint ensures that the number of event occurrences is equal in both plants
                    for (final Integer altPathIndex : firstPlantFrequencyMap.keySet())
                    {
                        if (altPathIndex > -1)
                        {
                            varCoeffs[altPathIndex].add(new double[]{
                                constraintCounter, -1 * firstPlantFrequencyMap.get(altPathIndex)});
                        }
                    }
                    for (final Integer altPathIndex : secondPlantFrequencyMap.keySet())
                    {
                        if (altPathIndex > -1)
                        {
                            varCoeffs[altPathIndex].add(new double[]{
                                constraintCounter, secondPlantFrequencyMap.get(altPathIndex)});
                        }
                    }
                    // The right-hand-side is generally = 0, but here it contains the numbers of
                    // forces event occurrences (no alt. paths upstreams)
                    bUpper.add((double) firstPlantFrequencyMap.get(-1) - secondPlantFrequencyMap.get(-1));

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
                    final double[] currCoeffs = varCoeffs[i].get(j);
                    String currColStr = "    " + varName;
                    final String valueStr = "" + currCoeffs[1];

                    final int currColLength = currColStr.length();
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
                    final double[] currCoeffs = varCoeffs[i].get(j);
                    final String currColStr = "   R" + (int)currCoeffs[0];
                    final String valueStr = "" + currCoeffs[1];

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
            final String currRhsStr = "    RHS1      R" + i;
            final String valueStr = "" + bUpper.get(i);

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

    protected void processConstraintString(final String str, final int constraintCounter)
    {
        double currBUpper = 0;
        int multiplier = -1;
        int tokenSign = 1;
        int storedMultiplier = -1;
        boolean bigMMultiplication = false;
        boolean withinParenthesis = false;

        final StringTokenizer tokenizer = new StringTokenizer(str);
        String varToken = "";
        while (tokenizer.hasMoreTokens())
        {
            String currToken = tokenizer.nextToken();

            if ((! bigMMultiplication) && currToken.contains("bigM*"))
            {
                storedMultiplier = multiplier;
                multiplier *= SchedulingConstants.BIG_M_VALUE * tokenSign;
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
                    final int plantIndex = Integer.parseInt(varToken.substring(
                            varToken.indexOf("[") + 1, varToken.indexOf(",")).trim());
                    final int stateIndex = Integer.parseInt(varToken.substring(
                            varToken.indexOf(",") + 1, varToken.indexOf("]")).trim());
                    currBUpper += -1 * multiplier * tokenSign * milpConstructor.getDeltaTimes()[plantIndex][stateIndex];
                }
                else
                {
                    final int timeVarIndex = timeVarIndexMap.get(varToken).intValue();

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
                currBUpper += -1 * tokenSign * multiplier * SchedulingConstants.EPSILON;
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

                    final Integer binVarIndex = binVarIndexMap.get(currToken.trim());
                    if (binVarIndex != null)
                    {
                        varCoeffs[binVarIndex].add(new double[]{constraintCounter, multiplier * tokenSign});
                    }
                    else
                    {
                        final Double constVal = Double.parseDouble(currToken);
                        currBUpper += -1 * tokenSign * multiplier * constVal;
                    }
                }
                catch (final NumberFormatException e){}
            }

            if (bigMMultiplication && !withinParenthesis)
            {
                multiplier = storedMultiplier;
                bigMMultiplication = false;
            }
        }
        bUpper.add(currBUpper);
    }

    protected void fillOptimalVarArrays(final Hashtable<Integer, Double> optimalTimeVarValues, final Hashtable<Integer, Integer> optimalBinVarValues)
    {
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
        for (final String varKey : timeVarIndexMap.keySet())
        {
            final int plantIndex = Integer.parseInt(varKey.substring(varKey.indexOf("[") + 1, varKey.indexOf(",")).trim());
            final int stateIndex = Integer.parseInt(varKey.substring(varKey.indexOf(",") + 1, varKey.indexOf("]")).trim());

            final Double optimalVarValue = optimalTimeVarValues.get(timeVarIndexMap.get(varKey));
            if (optimalVarValue != null)
            {
                optimalTimes[plantIndex][stateIndex] = optimalVarValue.doubleValue();
            }
        }
        // Fill the arrays of optimal alt path variables with appropriate values
        for (final String varKey : binVarIndexMap.keySet())
        {
            // If this is an alt.path variable, then store it. Otherwise, do nothing.
            if (varKey.contains("_from_") && varKey.contains("_to_"))
            {
                final int[] plantStateIndices = unmakeAltPathsVariableStr(varKey);
                final Integer optimalVarValue = optimalBinVarValues.get(binVarIndexMap.get(varKey));
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
    }

    /**
     * Launches the GLPK-solver (glpsol.exe must be included in the path).
     */
    @Override
    public void launchMilpSolver()
        throws MilpException, IOException
    {
        launchMilpSolver(mpsFile);
    }

    /**
     * Launches the appropriate MILP-solver (that should be included in the path).
     * This method-header allows to choose the model file manually (instead of
     * a temporary file that would be created by the system automatically).
     */
    public abstract void launchMilpSolver(File mpsFile)
        throws MilpException, IOException;

    /**
     * Processes the output from the MILP-solver, transforming it into a sequence
     * of event firing times.
     */
    @Override
    public abstract void processSolutionFile()
        throws MilpException, FileNotFoundException, IOException;

    /**
     * Deletes the temporary files that were created to interact with the MILP solver.
     * Called by the main class in case of emergency (e.g. undeleted temporary files
     * in case of exception).
     */
    @Override
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
        catch (final Exception ex)
        {
            milpConstructor.addToMessages("Cleaning up of the GLPK-solver failed.",
                    SchedulingConstants.MESSAGE_TYPE_ERROR);
        }
    }

    /** Returns the optimal event occurrence times for each plant-state. */
    @Override
    public double[][] getOptimalTimes()
    {
        return optimalTimes;
    }

    /** Returns the optimal alt. path variable choices. */
    @Override
    public boolean[][][] getOptimalAltPathVariables()
    {
       return optimalAltPathVariables;
    }

    /**
     * Combines info about an alt.paths variable,
     * stored as int[plantIndex, fromStateIndex, toStateIndex],
     * into string representation.
     */
    public String makeAltPathsVariableStr(final int[] altPathsVariable)
    {
        return "r" + altPathsVariable[0] + "_from_" + altPathsVariable[1] + "_to_" + altPathsVariable[2];
    }

    /**
     * Combines the info about a variable into string representation. If the
     * plant ordering is incorrect (which is used by the CircularWaitConstraintBlocks
     * to create correct logic), it is adjusted before constructing the variable.
     * @return String representation of the variable
     */
    public String makeMutexVariableStr(final int[] mutexVariable)
    {
        int[] localMutexVar;

        if (mutexVariable[1] < mutexVariable[2])
        {
            localMutexVar = mutexVariable;
        }
        else
        {
            localMutexVar = new int[mutexVariable.length];
            localMutexVar[0] = mutexVariable[0];
            localMutexVar[1] = mutexVariable[2];
            localMutexVar[2] = mutexVariable[1];
            localMutexVar[3] = mutexVariable[4];
            localMutexVar[4] = mutexVariable[3];
        }

        final int varIndex = milpConstructor.getMutexVarCounterMap().get(localMutexVar);

        return "r" + localMutexVar[1] + "_books_z" + localMutexVar[0] +
                "_before_r" + localMutexVar[2] + "_var" + varIndex;
    }

    protected int[] unmakeAltPathsVariableStr(final String varStr)
    {
        final int[] varIndices = new int[3];

        varIndices[0] = Integer.parseInt(varStr.substring(1, varStr.indexOf("_")));
        varIndices[1] = Integer.parseInt(varStr.substring(varStr.indexOf("om_") + 3, varStr.indexOf("_to")));
        varIndices[2] = Integer.parseInt(varStr.substring(varStr.indexOf("to_") + 3, varStr.length()));

        return varIndices;
    }

    protected int representMutexConstraint(final int p1, final int st1, final int p2, final int st2,
            final int mutexVarIndex, final int mutexVarCoeff, int constraintCounter)
    {
        final java.util.Collection<int[]> bookingAltPaths = milpConstructor.getActiveAltPathVars(
                new int[]{p1, st1});
        final java.util.Collection<int[]> unbookingAltPaths = milpConstructor.getActiveAltPathVars(
                new int[]{p2, st2});

        if (bookingAltPaths.size() + unbookingAltPaths.size() > 0)
        {
            for (final int[] bAltPathVar : bookingAltPaths)
            {
                final int bAltPathIndex = binVarIndexMap.get(milpConstructor.makeAltPathsVariable(
                        bAltPathVar[0], bAltPathVar[1], bAltPathVar[2]));
                for (final int[] uAltPathVar : unbookingAltPaths)
                {
                    varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                            p2, st2))].add(new double[]{constraintCounter, 1});
                    varCoeffs[timeVarIndexMap.get(milpConstructor.makeTimeVariable(
                            p1, st1))].add(new double[]{constraintCounter, -1});
                    varCoeffs[mutexVarIndex].add(new double[]{constraintCounter, mutexVarCoeff*SchedulingConstants.BIG_M_VALUE});
                    varCoeffs[bAltPathIndex].add(new double[]{constraintCounter, SchedulingConstants.BIG_M_VALUE});
                    varCoeffs[binVarIndexMap.get(milpConstructor.makeAltPathsVariable(
                            uAltPathVar[0], uAltPathVar[1], uAltPathVar[2]))].
                            add(new double[]{constraintCounter++, SchedulingConstants.BIG_M_VALUE});

                    bUpper.add(SchedulingConstants.BIG_M_VALUE *
                               (bookingAltPaths.size() + unbookingAltPaths.size()) - SchedulingConstants.EPSILON);
                }
            }
        }

        return constraintCounter;
    }
}
