package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDD.BDDIterator;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.properties.Config;

public class BDDExtendedManager extends BDDAbstractManager {

    //Variabes used for RAS models
    BDD globalLargerBDD = null;
    //The BDD representing the forward transition relation, where the dest variables are removed.  
    BDD frwdTrans = null;
    BDD completeFrwdTrans = null;
    //The BDD representing the backward transition relation
    BDD bckwdTransEvents = null;
    BDD frwdLoadingTrans = null;
    BDD globalOneStepLargerBDD = null;
    BDD feasibleSourceStates = null;
    BDD unreachableStates = null;
    TIntObjectHashMap<BDD> event2ParitionBDD = null;
    //BDD[] feasiableStatesBDDs = null; // represent the feasiable state set as a set of BDDs
    BDD[] partitionBDDs = null;
    BDD[] deltaTransBDDs = null;
    int componentSize;
    BDD minimalBoundaryUnsafeStates = null;
    TIntArrayList stageVarIndexList = null;
    List<BDDVarSet> varSetsTobeExisted = null;
    List<BDD> largerVarValues = null;
    List<BDD> smallerVarValues = null;
    Map<BDD, Set<String>> eventToStageVars;
    Map<BDD, BDDVarSet> eventToStageVarSet;
    Map<BDD, BDDVarSet> eventToOtherVarSet;
    //BDD eventToStageComBDD;
    HashMap<BDD, BDD> eventBDDToTransitionBDD = null;
    
    public int maxBDDSizeClassic = 0;
    public int maxBDDSizeAlternative = 0;
    public int maxBDDSizeMinimization = 0;

    public BDDExtendedManager() {
        this(BDDLibraryType.fromDescription(Config.BDD2_BDDLIBRARY.getAsString()));
    }

    public BDDExtendedManager(final BDDLibraryType bddpackage) {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }

    public BDDExtendedManager(final BDDLibraryType bddpackage, final int nodenum, final int cachesize) {
        if (factory == null) {
            factory = BDDFactory.init(bddpackage.getLibraryname(), nodenum, cachesize);
            factory.setMaxIncrease(Config.BDD2_MAXINCREASENODES.get());
            factory.setIncreaseFactor(Config.BDD2_INCREASEFACTOR.get());
            factory.setCacheRatio(Config.BDD2_CACHERATIO.get());
        }
    }

    /**
     * Return a set of initial uncontrollable states.
     */
    public BDD getInitiallyUncontrollableStates() {
        final BDDMonolithicEdges edges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());

        if (bddExAutomata.orgExAutomata.modelHasNoPlants()
                || bddExAutomata.orgExAutomata.modelHasNoSpecs()) {
            return getZeroBDD();
        } else {
            final BDD t1 = bddExAutomata.getReachableStates().and(edges.getPlantMonolithicUncontrollableEdgesForwardBDD());
            final BDD t2 = edges.getSpecMonolithicUncontrollableEdgesForwardBDD().and(t1).exist(bddExAutomata.getDestStatesVarSet());
            return t1.and(t2.not()).exist(bddExAutomata.getDestStatesVarSet()).exist(bddExAutomata.getEventVarSet());
        }

    }

    public BDD uncontrollableBackward(final BDD forbidden) {
        System.err.println("UncontrollableBackward entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD t_u = bddEdges.getMonolithicUncontrollableEdgesBackwardBDD();
        final BDD backwardTime = bddEdges.getBackwardClocksWithTheSameRate();
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();

//        System.out.println("forbidden");
//        forbidden.printDot();
        BDD Qk = null;
        BDD newUCstates = null;
        BDD newCstates = null;
        BDD Qkn = forbidden.id();
        if (!bddExAutomata.orgExAutomata.getClocks().isEmpty()) {
            Qkn = timeEvolSource(Qkn, backwardTime);
            newCstates = Qkn.and(bddEdges.getMonolithicForcibleEdgesForwardBDD().exist(bddExAutomata.getDestStatesVarSet()));
            newCstates = timeEvolSource(newCstates, backwardTime);
            Qkn = Qkn.and(newCstates.not());
            Qkn = timeEvolSource(Qkn, forwardTime);

//        System.out.println("Qknnnnnnn: "+(Qkn.isOne()?"one":""));
//        Qkn.printDot();

            Qkn = Qkn.or(forbidden);
            bddEdges.removeFromMonolithicForcibleEdgesForwardBDD(Qkn);
        }

        do {
//            System.out.println("UBackward: "+iteration++);
            Qk = Qkn.id();
            newUCstates = image_preImage(Qk, t_u);
            Qkn = Qk.or(newUCstates);
            BDD ucDueToTime = getZeroBDD();
            if (!bddExAutomata.orgExAutomata.getClocks().isEmpty()) {
                ucDueToTime = timeEvolSource(newUCstates, backwardTime).and(newUCstates.not());
                newCstates = ucDueToTime.and(bddEdges.getMonolithicForcibleEdgesForwardBDD().exist(bddExAutomata.getDestStatesVarSet()));
                newCstates = timeEvolSource(newCstates, backwardTime);
                ucDueToTime = ucDueToTime.and(newCstates.not());
                ucDueToTime = timeEvolSource(ucDueToTime, forwardTime);
                bddEdges.removeFromMonolithicForcibleEdgesForwardBDD(ucDueToTime);
            }
            Qkn = Qkn.or(ucDueToTime);
        } while (!Qkn.equals(Qk));

        System.err.println("UncontrollableBackward exited.");
        return Qkn;
    }

    public BDD restrictedBackward(final BDD markedStates, final BDD forbidden) {
        System.err.println("RestrictedBackward entered.");
        final BDD delta_all = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesBackwardBDD();
        final BDD clocks = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getBackwardClocksWithTheSameRate();

        BDD Qkn = markedStates.and(forbidden.not());
        BDD Qk = null;
        BDD Qm = null;
        /*
         FileWriter fstream = null;
         try
         {
         fstream = new FileWriter("C:/Users/sajed/Documents/My Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RB.txt");
         //           fstream = new FileWriter("/Users/sajed/Dropbox/Documents/Papers/Supremica_Models/FisherThompson/BDDstatistics_RB.txt");

         } catch (final Exception e){}
         final BufferedWriter out = new BufferedWriter(fstream);
         */

        do {
//            System.out.println("RBackward "+iteration++);
/*            try
             {
             out.write((iteration++) + "\t" + Qkn.nodeCount());
             out.newLine();
             out.close();
             } catch (final Exception e){}
             */
            Qk = Qkn.id();
            Qm = image_preImage(Qk, delta_all, clocks).and(bddExAutomata.getReachableStates());

//            BDD clockBDD = Qm.exist(bddExAutomata.sourceLocationVarSet);
//            String nameOfClock = bddExAutomata.getAutVarName(clockBDD.var());
//
//            int minClockValue = getMinimalValue(clockBDD);
//            SupremicaBDDBitVector minClockValueBDDVec = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
//                    bddExAutomata.getSourceVariableDomain(nameOfClock).size().intValue(),minClockValue);
//            SupremicaBDDBitVector clockBDDVec = bddExAutomata.getBDDBitVecSource(nameOfClock);
//
//            BDD extendedClockBDD = clockBDDVec.lth(minClockValueBDDVec);
//            Qm = Qm.or(extendedClockBDD);


            Qkn = ((Qk.or(Qm)).and(forbidden.not()));
            //           iteration++;
        } while (!Qkn.equals(Qk));

//        System.err.println("number of iterations in restrictedBackward: "+iteration);

//        try{out.close();}catch (final Exception e){}
        System.err.println("RestrictedBackward exited.");

        return Qkn;
    }

    public BDD restrictedForward(final BDD initialStates, final BDD forbidden) {
        System.err.println("RestrictedForward entered.");
        final BDD trans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardBDD();
        final BDD clocks = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getForwardClocksWithTheSameRate();

//        System.out.println("restrictedForward");

        BDD Qkn = initialStates.and(forbidden.not());
        BDD Qk = null;
        BDD Qm = null;


//        FileWriter fstream = null;
//        try
//        {
//            fstream = new FileWriter("/Users/sajed/Desktop/fxdPoint.txt");
//        } catch (final Exception e){}
//        out = new BufferedWriter(fstream);

        do {
//            System.err.println("RForward "+(iteration++) + "\t" + Qkn.nodeCount());

//            try
//            {
//                out.write((iteration++) + "\t" + Qkn.nodeCount());
//                out.newLine();
//            } catch (final Exception e){}
//
//            try
//            {
//                out.write((iteration++) + "\t");
//            } catch (final Exception e){}

            Qk = Qkn.id();
            Qm = image_preImage(Qk, trans, clocks);

//            BDD clockBDD = Qm.exist(bddExAutomata.sourceLocationVarSet);
//            String nameOfClock = bddExAutomata.getAutVarName(clockBDD.var());
//
//            int minClockValue = getMinimalValue(clockBDD);
//            SupremicaBDDBitVector minClockValueBDDVec = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, factory,
//                    bddExAutomata.getSourceVariableDomain(nameOfClock).size().intValue(),minClockValue);
//            SupremicaBDDBitVector clockBDDVec = bddExAutomata.getBDDBitVecSource(nameOfClock);
//
//            BDD extendedClockBDD = clockBDDVec.gte(minClockValueBDDVec);
//            Qm = Qm.or(extendedClockBDD);
//            iteration++;
            Qkn = (Qk.or(Qm)).and(forbidden.not());

        } while (!Qkn.equals(Qk));

//        System.err.println("number of iterations in restrictedForward: "+iteration);

        System.err.println("RestrictedForward exited.");
        return Qkn;
    }

    public BDD nonblockingControllable(final BDD forbidden, final boolean reachable) {
        System.err.println("NonblockingControllable entered.");
        final BDD clocks = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getForwardClocksWithTheSameRate();

        BDD Qkn = forbidden;
        BDD Qk = null;
        BDD Q1 = null;
        BDD Q2 = null;

        do {
//            System.out.println("nbc: "+(iteration++));
            Qk = Qkn.id();
            Q1 = restrictedBackward(bddExAutomata.getMarkedStates(), Qk);
            BDD forbiddenStates = Q1.not().and(bddExAutomata.getReachableStates());
            if (!bddExAutomata.orgExAutomata.getClocks().isEmpty()) {
                forbiddenStates = bddExAutomata.fitIntoClockDomains(forbiddenStates).and(
                        (timeEvolSource(bddExAutomata.getMarkedStates(), clocks)).not());
            }
            Q2 = uncontrollableBackward(forbiddenStates);
//            Q2 =  Q2.and((timeEvolSource(bddExAutomata.getMarkedStates(),clocks)).not());
            Qkn = Qk.or(Q2);
        } while ((!Qkn.equals(Qk)));

        System.err.println("NonblockingControllable exited.");

        if (reachable) {
            return restrictedForward(bddExAutomata.getInitialState(), Qkn);
        } else {
            return Qkn.not();
        }
    }

    BDD getDisjunctiveInitiallyUncontrollableStates() {

        if (bddExAutomata.plants.isEmpty() && bddExAutomata.specs.isEmpty()) {
            return getZeroBDD();
        } else {

            final TIntHashSet plantUncontrollableEvents = bddExAutomata.plantUncontrollableEventIndexList;
            final TIntHashSet specUncontrollableEvents = bddExAutomata.specUncontrollableEventIndexList;

            TIntHashSet sharedUncontrollableEvents = new TIntHashSet(specUncontrollableEvents.toArray());
            sharedUncontrollableEvents.retainAll(plantUncontrollableEvents.toArray());

            final TIntObjectHashMap<BDD> plantsEnabledStates =
                    new BDDPartitionUncontSetEve(bddExAutomata, bddExAutomata.plants, plantUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final TIntObjectHashMap<BDD> specEnabledStates =
                    new BDDPartitionUncontSetEve(bddExAutomata, bddExAutomata.specs, specUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final BDD uncontrollableStates = getZeroBDD();

            for (TIntIterator itr = sharedUncontrollableEvents.iterator(); itr.hasNext();) {
                int unConEventIndex = itr.next();
                BDD statesEnabledByPlants = plantsEnabledStates.get(unConEventIndex).and(bddExAutomata.getReachableStates());
                BDD statesEnabledBySpecs = specEnabledStates.get(unConEventIndex).and(bddExAutomata.getReachableStates());
                uncontrollableStates.orWith(statesEnabledByPlants.and(statesEnabledBySpecs.not()));
            }
            return uncontrollableStates.and(bddExAutomata.getReachableStates());
        }
    }

    BDD disjunctiveNonblockingControllable(final BDD forbiddenStates, final boolean reachable) {

        BDD previousForbidenStates = null;
        BDD tmpCoreachableStates = null;
        BDD currentForbidenStates = forbiddenStates;

        boolean flag = false;
        do {
            previousForbidenStates = currentForbidenStates.id();
            currentForbidenStates = bddExAutomata.getParAlgoWorker().uncontrollableBackwardWorkSetAlgorithm(currentForbidenStates);
            if (flag && currentForbidenStates.equals(previousForbidenStates)) {
                break;
            } else {
                // TEST!!
                //parAlgoWorker = new BDDPartitionAlgoWorkerRan(eventPartitions, eventCoordinator);
                tmpCoreachableStates = bddExAutomata.getParAlgoWorker()
                        .reachableBackwardRestrictedWorkSetAlgorithm(bddExAutomata.getMarkedStates(),
                        currentForbidenStates, bddExAutomata.getReachableStates());
                currentForbidenStates = tmpCoreachableStates.not();
                flag = true;
            }
        } while (!previousForbidenStates.equals(currentForbidenStates));

        BDD nonblockingControllableStates = null;
        if (reachable) {
            nonblockingControllableStates = bddExAutomata.getParAlgoWorker()
                    .forwardRestrictedWorkSetAlgorithm(bddExAutomata.getInitialState(), currentForbidenStates);
        } else {
            nonblockingControllableStates = currentForbidenStates.not();
        }
        return nonblockingControllableStates;
    }

    // ##################################################################################
    // ################ THE MAIN ALGORITHM 1 AND 2 (SEE THE TASE PAPER) #################
    // ### Zhennan
    // Compute the boundary unsafe states with the extension of the SCT algorithm
    public BDD computeBoundaryUnsafeStatesClassic() {
        
        buildHelpers();

        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardBDD()
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));
        
        BDD initialStateBDD = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());
     
        BDD Qkn = initialStateBDD;
        BDD Qk = null;
        BDD Qm = null;

        
        do {
            Qk = Qkn.id();
            
            BDD tmp = frwdTrans.and(Qkn);
            if (maxBDDSizeClassic < tmp.nodeCount()) {
                maxBDDSizeClassic = tmp.nodeCount();
            }         
        
            Qm = tmp.exist(bddExAutomata.getSourceVariablesVarSet());

            Qm.replaceWith(bddExAutomata.getDestToSourceVariablePairing());

            Qkn = Qk.or(Qm);
            if (maxBDDSizeClassic < Qkn.nodeCount()) {
                maxBDDSizeClassic = Qkn.nodeCount();
            }

        } while (!Qkn.equals(Qk));
        
        BDD reachableStatesBDD = Qkn.id();
        System.out.println("The number of reachable states is " + 
                reachableStatesBDD.satCount(bddExAutomata.getSourceVariablesVarSet()));
        
        Qkn = initialStateBDD;
        Qk = null;
        Qm = null;
        
        do {
            Qk = Qkn.id();
            
            BDD tmp = frwdTrans.and(Qkn
                    .replaceWith(bddExAutomata.getSourceToDestVariablePairing()));
            
            if (maxBDDSizeClassic < tmp.nodeCount()) {
                maxBDDSizeClassic = tmp.nodeCount();
            }
            
            Qm = tmp.exist(bddExAutomata.getDestVariablesVarSet());

            Qkn = Qk.or(Qm);
            if (maxBDDSizeClassic < Qkn.nodeCount()) {
                maxBDDSizeClassic = Qkn.nodeCount();
            }          
            
        } while (!Qkn.equals(Qk));
        
        BDD coreachableStatesBDD = Qkn.id();
        
        BDD safeStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
        
        BDD boundaryUnsafeStatesBDD = safeStatesBDD.and(frwdTrans)
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        if (maxBDDSizeClassic < boundaryUnsafeStatesBDD.nodeCount()) {
            maxBDDSizeClassic = boundaryUnsafeStatesBDD.nodeCount();
        }
        
        boundaryUnsafeStatesBDD = boundaryUnsafeStatesBDD.and(coreachableStatesBDD.not());
        if (maxBDDSizeClassic < boundaryUnsafeStatesBDD.nodeCount()) {
            maxBDDSizeClassic = boundaryUnsafeStatesBDD.nodeCount();
        }
        
        System.out.println("The maximal size of intermediate BDDs is "
                + maxBDDSizeClassic);

        System.out.println("The number of reachable boundary unsafe states is " +  
                + boundaryUnsafeStatesBDD
                .satCount(bddExAutomata.getSourceVariablesVarSet()));
        
        boundaryUnsafeStatesBDD = boundaryUnsafeStatesBDD
                .exist(bddExAutomata.getSourceResourceVarSet());
        
        return boundaryUnsafeStatesBDD;
    }
    
    // Compute boundary unsafe states with the alternative algorithm
    public BDD computeBoundaryUnsafeStatesAlternative() {

        // compute some helper BDDs and maps
        buildHelpers();

        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardWithEventsBDD()
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));
        
        frwdLoadingTrans = frwdTrans.relprod(bddExAutomata.loadEventsBDD,
                bddExAutomata.getEventVarSet());

        BDD deadlocks = getDeadlocks();
        System.out.println("The number of deadlocks is: "
                + deadlocks.satCount(bddExAutomata.getSourceVariablesVarSet()));

        BDD boundaryUnsafeStates  = computeBoundaryUnsafeStates(deadlocks);
        System.out.println("the number of boundary states is " + 
                boundaryUnsafeStates.satCount(bddExAutomata.getSourceVariablesVarSet()));

        boundaryUnsafeStates = boundaryUnsafeStates
                .exist(bddExAutomata.getSourceResourceVarSet());
        return boundaryUnsafeStates;
    }

    // Backtrack deadlock states to find all boundary unsafe states
    private BDD computeBoundaryUnsafeStates(BDD deadlocks) {

        BDD newUnsafeStates = deadlocks.id();

        BDD unsafeStates = deadlocks.id();

        BDD tTrans = getZeroBDD();

        do {

            BDD fTransToUnsafeStates = newUnsafeStates
                    .replace(bddExAutomata.getSourceToDestVariablePairing())
                    .and(frwdTrans);
            if (maxBDDSizeAlternative < fTransToUnsafeStates.nodeCount()) {
                maxBDDSizeAlternative = fTransToUnsafeStates.nodeCount();
            }

            BDD possibleUnsafeStates = fTransToUnsafeStates
                    .exist(bddExAutomata.getDestVariablesVarSet());

            BDD transFromPossUnStates = possibleUnsafeStates.and(frwdTrans);
            if (maxBDDSizeAlternative < transFromPossUnStates.nodeCount()) {
                maxBDDSizeAlternative = transFromPossUnStates.nodeCount();
            }

            BDD nonUnsafeStates = transFromPossUnStates.and(tTrans.not());
            if (maxBDDSizeAlternative < nonUnsafeStates.nodeCount()) {
                maxBDDSizeAlternative = nonUnsafeStates.nodeCount();
            }


            nonUnsafeStates = nonUnsafeStates.relprod(fTransToUnsafeStates.not(),
                    bddExAutomata.getDestVariablesVarSet());
            if (maxBDDSizeAlternative < nonUnsafeStates.nodeCount()) {
                maxBDDSizeAlternative = nonUnsafeStates.nodeCount();
            }

            newUnsafeStates = possibleUnsafeStates.and(nonUnsafeStates.not());

            newUnsafeStates.andWith(unsafeStates.not());
            unsafeStates = unsafeStates.or(newUnsafeStates);

            tTrans.orWith(fTransToUnsafeStates).and(newUnsafeStates.not());
            if (maxBDDSizeAlternative < tTrans.nodeCount()) {
                maxBDDSizeAlternative = tTrans.nodeCount();
            }

        } while (!newUnsafeStates.isZero());       
        
        BDD boundaryUnsafeStates = unsafeStates
                .replace(bddExAutomata.getSourceToDestVariablePairing())
                .andWith(completeFrwdTrans).andWith(unsafeStates.not())
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replace(bddExAutomata.getDestToSourceVariablePairing());
        
        if (maxBDDSizeAlternative < boundaryUnsafeStates.nodeCount()) {
            maxBDDSizeAlternative = boundaryUnsafeStates.nodeCount();
        }

        System.out.println("The maximal size of intermediate BDDs is " 
                + maxBDDSizeAlternative);
                  
        return boundaryUnsafeStates;
    }

    // Compute all deadlock states 
    private BDD getDeadlocks() {

        BDD potentialDeadlockStates = frwdTrans
                .exist(bddExAutomata.getEventVarSet()
                .union(bddExAutomata.getSourceVariablesVarSet()))
                .replace(bddExAutomata.getDestToSourceVariablePairing());

        completeFrwdTrans = frwdTrans.exist(bddExAutomata.getEventVarSet());
        
        frwdTrans = frwdTrans.relprod(bddExAutomata.loadEventsBDD.not(),
                bddExAutomata.getEventVarSet());
        
        BDD notDeadlockStates = frwdTrans.exist(bddExAutomata.getDestVariablesVarSet());

        BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());

        BDD deadlocks = potentialDeadlockStates
                .andWith(notDeadlockStates.not()).andWith(initv.not());

        // a series of BDDs together representing the feasible states
//        for (BDD feasiableSet : feasiableStatesBDDs) {
//            deadlocks = deadlocks.and(feasiableSet);
//        }
        
        deadlocks.andWith(feasibleSourceStates);

        return deadlocks;
    }

    // subroutine: remove the larger states from unsafeStates
    public BDD removeLargerStates(BDD boundaryStates) {

        BDDIterator itr = boundaryStates.iterator(bddExAutomata.getSourceStagesVarSet());

        BDD largerStates = getZeroBDD();

        while (itr.hasNext()) {

            BDD anUnsafeState = itr.nextBDD();

            if (anUnsafeState.and(largerStates).isZero()) {

                BDD tempUnsafeState = anUnsafeState
                        .replace(bddExAutomata.sourceToTempVariablePairing);

                BDD possibleLargerStates = boundaryStates.and(anUnsafeState.not());

                BDD localLargerStates = getOneBDD();

                for (int i = 0; i < stageVarIndexList.size(); i++) {

                    BDD partialLargerState =
                            tempUnsafeState.exist(varSetsTobeExisted.get(i))
                            .relprod(largerVarValues.get(i),
                            bddExAutomata.tempVariablesVarSet);

                    localLargerStates = localLargerStates.and(partialLargerState);

                    if (maxBDDSizeMinimization < localLargerStates.nodeCount()) {
                        maxBDDSizeMinimization = localLargerStates.nodeCount();
                    }
                }
                largerStates.orWith(localLargerStates.andWith(possibleLargerStates));
                if (maxBDDSizeMinimization < largerStates.nodeCount()) {
                    maxBDDSizeMinimization = largerStates.nodeCount();
                }
            }
        }

        minimalBoundaryUnsafeStates = boundaryStates.and(largerStates.not());
        System.out.println("The maximal size of BDDs during the minimization is: "
                + maxBDDSizeMinimization);
        System.out.println("The number of minimal boundary unsafe states is: " 
                + minimalBoundaryUnsafeStates
                .satCount(bddExAutomata.getSourceStagesVarSet()));
        
        return minimalBoundaryUnsafeStates;
    }

    // Build auxiliary BDDs used for computing the boundary unsafe states
    private void buildHelpers() {

        int stageVarSize = bddExAutomata.orgExAutomata.getStageVars().size();

        stageVarIndexList = new TIntArrayList(stageVarSize);

        varSetsTobeExisted = new ArrayList<BDDVarSet>(stageVarSize);

        largerVarValues = new ArrayList<BDD>(stageVarSize);

        List<VariableComponentProxy> stageVars = bddExAutomata.orgExAutomata.getStageVars();

        for (VariableComponentProxy sv : stageVars) {

            int svIndex = bddExAutomata.theIndexMap.getVariableIndex(sv);

            stageVarIndexList.add(svIndex);

            // put BDDVarSets of other stage vars together
            BDDVarSet otherVarsBDDVarSets = createEmptyVarSet();

            for (VariableComponentProxy osv : stageVars) {

                if (!sv.equals(osv)) {

                    otherVarsBDDVarSets = otherVarsBDDVarSets
                            .union(bddExAutomata.stageVar2BDDTempVarSetMap.get(osv));
                }
            }

            varSetsTobeExisted.add(otherVarsBDDVarSets);

            // construct the BDD representing larger values of this stage var
            BDD largerValues = getZeroBDD();

            int stageDomain = bddExAutomata.orgExAutomata.getVarDomain(sv.getName());

            BDDDomain stageSourceDomain = bddExAutomata.sourceVarDomains[svIndex];

            SupremicaBDDBitVector stageBDDBitVectorSource =
                    createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                    bddExAutomata.orgExAutomata.getMinValueofVar(sv.getName()) < 0,
                    stageSourceDomain);

            for (int i = 0; i < stageDomain; i++) {

                BDD tmp = createBDD(i, bddExAutomata.getTempVariableDomain(svIndex));

                SupremicaBDDBitVector vector_j =
                        createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        stageSourceDomain.varNum(), i);

                BDD greaterThanJBDD = stageBDDBitVectorSource.gte(vector_j);

                largerValues = largerValues.or(tmp.andWith(greaterThanJBDD));
            }

            largerVarValues.add(largerValues);
        }
        
        // compute the feasiable state set BDD
        feasibleSourceStates = getFeasibleSourceStatesBDD();
    }
    
    // Build the BDD of feasible states from the resource invariants (module comments)
    private BDD getFeasibleSourceStatesBDD() {

        if (feasibleSourceStates == null) {

            final ExpressionParser parser =
                    new ExpressionParser(ModuleSubjectFactory.getInstance(),
                    CompilerOperatorTable.getInstance());

            SimpleExpressionSubject feasibleEquation = null;

            try {

                feasibleEquation = (SimpleExpressionSubject) 
                        (parser.parse(bddExAutomata.orgExAutomata.getModule().getComment(),
                        Operator.TYPE_BOOLEAN));
            } catch (final ParseException pe) {
                System.err.println(pe);
            }

            feasibleSourceStates = guard2BDD(feasibleEquation);

            return feasibleSourceStates;
        }

        return feasibleSourceStates;
    }
    
    //####### END #######################################################################
    
    //####### UNDER DEVELOPMENT #########################################################
    public BDD computeBoundaryUnsafeStatesEventPartitioning() {

        setupPartitioningBDDs();

        System.err.println("Computing the deadlock states with the event-based "
                + "partitioning approach.");
        BDD deadlocks = getDeadlocksWithPartitions();
        System.err.println("The number of deadlocks is: "
                + deadlocks.satCount(bddExAutomata.getSourceVariablesVarSet()));

        System.err.println("Computing the boundary unsafe states with the event-based "
                + "partitioning approach. ");
        
        BDD boundaryUnsafeStates =
                computeBoundaryUnsafeStatesWithEventPartitions(deadlocks);

        return boundaryUnsafeStates.exist(bddExAutomata.getSourceResourceVarSet());
    }

    // Get the event partial transition realtion BDD 
    private void setupPartitioningBDDs() {

        BDDPartitionAlgoWorker worker = bddExAutomata.getParAlgoWorker();

        BDDPartitionSet partitionSet = worker.partitions;

        event2ParitionBDD = partitionSet.getCompIndexToCompBDDMap();

        componentSize = event2ParitionBDD.size();

        partitionBDDs = new BDD[componentSize];
        partitionBDDs = event2ParitionBDD.getValues(partitionBDDs);

        for (int i = 0; i < componentSize; i++) {
            partitionBDDs[i] = partitionBDDs[i]
                    .exist(bddExAutomata.getSourceLocationVarSet()
                    .union(bddExAutomata.getDestLocationVarSet()));
        }

        partitionBDDs = new BDD[componentSize];

        frwdLoadingTrans = getZeroBDD();

        event2ParitionBDD.forEachEntry(new TIntObjectProcedure<BDD>() {
            BDDVarSet locationVars = bddExAutomata.getSourceLocationVarSet()
                    .union(bddExAutomata.getDestLocationVarSet());

            public boolean execute(int eventIndex, BDD partition) {

                String eventName = bddExAutomata.theIndexMap
                        .getEventAt(eventIndex).getName();

                partition = partition.exist(locationVars);

                if (!eventName.contains(FlowerEFABuilder.LOAD_EVENT_PREFIX)) {

                    partitionBDDs[eventIndex] = partition;

                } else {
                    partitionBDDs[eventIndex] = getZeroBDD();
                    frwdLoadingTrans = frwdLoadingTrans.or(partition);
                }
                return true;
            }
        });

        deltaTransBDDs = new BDD[componentSize];

        for (int i = 0; i < componentSize; i++) {
            deltaTransBDDs[i] = getZeroBDD();
        }

        buildHelpers();

        feasibleSourceStates = getFeasibleSourceStatesBDD();
    }

    // Compute all deadlock states using partitioning techniques
    private BDD getDeadlocksWithPartitions() {

        BDD nonDeadlocks = getZeroBDD();

        for (BDD component : partitionBDDs) {

            nonDeadlocks.orWith(component
                    .exist(bddExAutomata.getDestVariablesVarSet()));
        }

        BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());

        BDD deadlocks = frwdLoadingTrans.exist(bddExAutomata.getSourceVariablesVarSet())
                .replace(bddExAutomata.getDestToSourceVariablePairing())
                .and(nonDeadlocks.not()).and(feasibleSourceStates);

        for (BDD component : partitionBDDs) {

            BDD possResourceDeadlockStates = component
                    .exist(bddExAutomata.getSourceVariablesVarSet())
                    .replace(bddExAutomata.getDestToSourceVariablePairing());

            deadlocks.orWith(possResourceDeadlockStates
                    .and(nonDeadlocks.not())
                    .and(feasibleSourceStates));
        }

        deadlocks = deadlocks.andWith(initv.not());
        return deadlocks;
    }
    
    // BUGGY!
    // Backtrack deadlock states to find all boundary unsafe states from partition
    private BDD computeBoundaryUnsafeStatesWithEventPartitions(BDD deadlocks) {

        BDD newFoundUnsafeStates = deadlocks;

        BDD unsafeStates = deadlocks.id();

        BDD[] notUnsafeStates = new BDD[componentSize];
        BDD[] possiableUnsafeStates = new BDD[componentSize];

        do {

            for (int i = 0; i < componentSize; i++) {

                BDD partition = partitionBDDs[i];

                BDD transToUnsafeStates = newFoundUnsafeStates
                        .replace(bddExAutomata.getSourceToDestVariablePairing())
                        .and(partition);
                
                possiableUnsafeStates[i] = transToUnsafeStates
                        .exist(bddExAutomata.getDestVariablesVarSet());
                
                deltaTransBDDs[i] = deltaTransBDDs[i].or(possiableUnsafeStates[i]);

                notUnsafeStates[i] = partition
                        .exist(bddExAutomata.getDestVariablesVarSet())
                        .and(deltaTransBDDs[i].not());
            }

            newFoundUnsafeStates = getZeroBDD();

            for (int j1 = 0; j1 < componentSize; j1++) {

                BDD localNewUnStates = possiableUnsafeStates[j1];

                for (int j2 = 0; j2 < componentSize; j2++) {
                    if (j1 != j2) {
                        localNewUnStates.andWith(notUnsafeStates[j2].not());
                    }
                }

                BDD nonBoundaryStates = localNewUnStates.relprod(deltaTransBDDs[j1], 
                        bddExAutomata.getSourceVariablesVarSet())
                        .replace(bddExAutomata.getDestToSourceVariablePairing());

                minimalBoundaryUnsafeStates.andWith(nonBoundaryStates.not());

                deltaTransBDDs[j1].andWith(localNewUnStates.not());
                newFoundUnsafeStates = newFoundUnsafeStates.or(localNewUnStates);
            }

            unsafeStates = unsafeStates.or(newFoundUnsafeStates);

            minimalBoundaryUnsafeStates =
                    minimalBoundaryUnsafeStates.or(newFoundUnsafeStates);

        } while (!newFoundUnsafeStates.isZero());

        return unsafeStates;
    }
    
    // Compute a series of BDDs representing the feasiable states
//    public BDD[] computeFeasiableStatesBDDs() {
//
//        if (feasiableStatesBDDs == null) {
//
//            TIntObjectHashMap<String> r2FeaEquations = bddExAutomata.orgExAutomata
//                    .resourceToFeasibleEquationMap;
////              TIntObjectHashMap<String> r2FeaEquations = FlowerEFABuilder
////                      .resourceToFeasibleEquationMap;
//
//            feasiableStatesBDDs = new BDD[r2FeaEquations.size()];
//
//            final ExpressionParser parser =
//                    new ExpressionParser(ModuleSubjectFactory.getInstance(),
//                    CompilerOperatorTable.getInstance());
//
//            r2FeaEquations.forEachEntry(new TIntObjectProcedure<String>() {
//                public boolean execute(int rIndex, String equation) {
//
//                    SimpleExpressionSubject equationExpression = null;
//                    BDD equationBDD = null;
//
//                    if (!equation.isEmpty()) {
//                        try {
//                            equationExpression = (SimpleExpressionSubject) (parser.parse(equation, Operator.TYPE_BOOLEAN));
//                        } catch (final ParseException pe) {
//                            System.err.println(pe);
//                        }
//                        equationBDD = guard2BDD(equationExpression);
//                    } else {
//                        equationBDD = getOneBDD();
//                    }
//
//
//                    feasiableStatesBDDs[rIndex] = equationBDD;
//                    return true;
//                }
//            });
//        }
//
//        return feasiableStatesBDDs;
//    }
}
