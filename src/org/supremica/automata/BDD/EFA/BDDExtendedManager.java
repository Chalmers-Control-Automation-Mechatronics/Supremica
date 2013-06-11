package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    //The BDD representing the backward transition relation
    BDD bckwdTransEvents = null;
    
    BDD frwdLoadingTrans = null;
    
    BDD globalOneStepLargerBDD = null;
    
    BDD feasibleSourceStates = null;
    BDD feasibleDestStates = null;
    BDD unreachableStates = null;
    
    TIntArrayList stageVarIndexList = null;
    List<BDDVarSet> varSetsTobeExisted = null;
    List<BDD> largerVarValues = null;
    TObjectIntHashMap<BDD> eventBDDToSourceStageVarIndexMap = null;
    
    Map<BDD, Set<String>> eventToStageVars;
    Map<BDD, BDDVarSet> eventToStageVarSet;
    Map<BDD, BDDVarSet> eventToOtherVarSet;
    //BDD eventToStageComBDD;
    
    HashMap<BDD, BDD> eventBDDToTransitionBDD = null;

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
    // ################ THE MAIN ALGORITHM ##############################################
    // ### Zhennan
    public BDD computeUnsafeStates() {
        
        // get forward transition relations from the monolithic approach
        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardWithEventsBDD();
        
        // remove variables of locations
        frwdTrans = frwdTrans
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));

        // remove the states in transitions that are non-feasible
        frwdTrans = frwdTrans.and(getFeasibleSourceStates())
                             .and(getFeasibleDestStates());
        
        // remove resource variables
        frwdTrans = frwdTrans
                .exist(bddExAutomata.getSourceResourceVarSet())
                .exist(bddExAutomata.getDestResourceVarSet());
        
        // switch variables to build the backward transition relation
        bckwdTransEvents = frwdTrans
                .replace(bddExAutomata.sourceToTempVariablePairing)
                .replace(bddExAutomata.getDestToSourceVariablePairing())
                .replace(bddExAutomata.tempToDestVariablePairing);
        
        // used to find minimal deadlock states
        /*frwdLoadingTrans = frwdTrans
                .and(bddExAutomata.loadEventsBDD)
                .exist(bddExAutomata.getEventVarSet());*/
        
        // use relprod instead of AND + exists
        frwdLoadingTrans = frwdTrans.relprod(bddExAutomata.loadEventsBDD,
                bddExAutomata.getEventVarSet());
        
        // frwdTrans does not have loading transitions and target states
        /*frwdTrans = frwdTrans.and(bddExAutomata.loadEventsBDD.not())
                .exist(bddExAutomata.getDestVariablesVarSet());*/
        
        // use relprod instead of AND + exists
        frwdTrans = frwdTrans.relprod(bddExAutomata.loadEventsBDD.not(), 
                bddExAutomata.getDestVariablesVarSet());
        
        //System.err.println("Computing the minimal deadlock states...");
        BDD unsafeStates = getMinimalDeadlocks();
        //System.err.println(unsafeStates.satCount(bddExAutomata.getSourceStagesVarSet())
        //        + " minimal deadlock states are computed.");
        
        // remove the loading transitions from the backward transition relation
        bckwdTransEvents.andWith(bddExAutomata.loadEventsBDD.not());
        
        //System.err.println("Computing the minimal unsafe states...");
        
        BDD[] elements = null; // collect \chi_{\hat{U}} and \Delta_{\cal T}
       
        BDD newUnsafeStates = unsafeStates;
        
        do {

            elements = computeElements(unsafeStates);

            unsafeStates.orWith(elements[0]);
            
            unsafeStates = removeLargerStates(unsafeStates);
            
            newUnsafeStates = extractLargerUnsafeStates(unsafeStates, elements[1]); 
            
            if (!newUnsafeStates.isZero()) {
                unsafeStates = unsafeStates.or(newUnsafeStates);
                unsafeStates = removeLargerStates(unsafeStates);
            }    
            
        } while (!newUnsafeStates.isZero());
        
        unsafeStates = removeLargerStates(unsafeStates);
        
        
        // TEST
        // get reachable states from the monolithic BDD algorithm
        /*BDD reachableStates = bddExAutomata.getReachableStates()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());

        // temporary: make unsafeStates reachable to test the correctness
        unsafeStates = unsafeStates.and(reachableStates);
        
        // get safe states from the monolithic BDD algorithm
        BDD nonblockingStates = bddExAutomata.getNonblockingStates()
                .and(getFeasibleSourceStates())
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());
        
        BDD minimalUnsafeStates = removeLargerStates(reachableStates
                .and(nonblockingStates.not()));
        
        System.err.println("#unsafeStates computed should equal #minimalUnsafeStates " 
                + unsafeStates.satCount(bddExAutomata.getSourceStagesVarSet()) + "; " 
                + minimalUnsafeStates.satCount(bddExAutomata.getSourceStagesVarSet()) + " "
                + unsafeStates.and(minimalUnsafeStates).equals(unsafeStates));*/
        
      
        return unsafeStates;
    }
    
    // subroutine: remove the larger states from unsafeStates
    private BDD removeLargerStates(BDD unsafeStates) {
        
        //System.err.println("minimizing...");
        
        BDDIterator itr = unsafeStates.iterator(bddExAutomata.getSourceStagesVarSet());

        BDD largerStates = getZeroBDD();

        while (itr.hasNext()) {

            BDD anUnsafeState = itr.nextBDD();

            if (anUnsafeState.and(largerStates).isZero()) {

                BDD tempUnsafeState = anUnsafeState
                        .replace(bddExAutomata.sourceToTempVariablePairing);

                BDD possibleLargerStates = unsafeStates.and(anUnsafeState.not());

                for (int i = 0; i < stageVarIndexList.size(); i++) {

                    /*BDD partialLargerState = tempUnsafeState.exist(
                            varSetsTobeExisted.get(i))
                            .and(largerVarValues.get(i))
                            .exist(bddExAutomata.tempVariablesVarSet);*/
                    
                    // use relprod 
                    BDD partialLargerState = 
                            tempUnsafeState.exist(varSetsTobeExisted.get(i))
                            .relprod(largerVarValues.get(i), 
                            bddExAutomata.tempVariablesVarSet);

                    possibleLargerStates = possibleLargerStates.and(partialLargerState);
                    
                    if(possibleLargerStates.isZero())
                        break;
                }

                largerStates = largerStates.or(possibleLargerStates);
            }
        }

        return unsafeStates.and(largerStates.not());
    }

    private BDD getMinimalDeadlocks() {

        BDD notDeadlockStates = frwdTrans.exist(bddExAutomata.getEventVarSet());

        BDD potentialDeadlockStates = bckwdTransEvents
                .exist(bddExAutomata.getEventVarSet())
                .exist(bddExAutomata.getDestVariablesVarSet());

        BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());

        BDD deadlocks = potentialDeadlockStates
                .andWith(notDeadlockStates.not()).andWith(initv.not());

        // exlcude states reached by loading events since they are larger 
        /*deadlocks = deadlocks.and(deadlocks.and(frwdLoadingTrans)
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replace(bddExAutomata.getDestToSourceVariablePairing()).not());*/
        
        //use relprod 
        deadlocks = deadlocks.and(
                deadlocks.relprod(frwdLoadingTrans, bddExAutomata.getSourceVariablesVarSet())
                .replace(bddExAutomata.getDestToSourceVariablePairing()).not());
        
        // compute some helper BDDs and maps
        buildHelpers();
        
        BDD minimalDeadlocks = removeLargerStates(deadlocks);
        /*
        // TEST
        // get reachable states from the monolithic BDD algorithm
        BDD reachableStates = bddExAutomata.getReachableStates()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());

        // get safe states from the monolithic BDD algorithm
        BDD nonblockingStates = bddExAutomata.getNonblockingStates()
                .and(getFeasibleSourceStates())
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());

        BDD blockingStates = reachableStates.and(nonblockingStates.not());
        
        blockingStates = blockingStates.and(blockingStates.and(frwdTrans)
                .exist(bddExAutomata.getEventVarSet()).not());
        
        blockingStates = removeLargerStates(blockingStates);
        
        System.err.println("Test minimal deadlock states");
        System.err.println(blockingStates.satCount(bddExAutomata.getSourceStagesVarSet()));
        System.err.println(blockingStates.equals(minimalDeadlocks.and(reachableStates)));
        
        return blockingStates;*/
        return minimalDeadlocks;
    }
    
    // index is the stage var index, value is the union of var set of other stage vars
    private void buildHelpers() {

        int stageVarSize = bddExAutomata.orgExAutomata.getStageVars().size();

        stageVarIndexList = new TIntArrayList(stageVarSize);
        
        varSetsTobeExisted = new ArrayList<BDDVarSet>(stageVarSize);

        largerVarValues = new ArrayList<BDD>(stageVarSize);
        
        eventBDDToSourceStageVarIndexMap = new TObjectIntHashMap<BDD>();
        
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
        
        HashMap<String, String> eventNameToSourceStageVarName 
                = FlowerEFABuilder.eventIndexToSourceStageVar;
        
        for (Map.Entry<String, String> entry : eventNameToSourceStageVarName.entrySet()) {

            int eventIndex = bddExAutomata.theIndexMap.getEventIndex(
                    bddExAutomata.theIndexMap.eventIdToProxy(entry.getKey()));

            BDD eventBDD = factory.buildCube(eventIndex,
                    bddExAutomata.getEventDomain().vars());
            
            int sourceStageVarIndex = bddExAutomata.theIndexMap
                    .getVariableIndexByName(entry.getValue());
            
            eventBDDToSourceStageVarIndexMap.put(eventBDD, sourceStageVarIndex);
        }

    }
     
    private BDD[] computeElements(BDD unsafeStates) {

        BDD newlyFoundUnsafeStates = unsafeStates;
        
        BDD inevitableUnsafeStates = unsafeStates;
        
        BDD tTrans = getZeroBDD();
        
        do {
            BDD bTransFromUnsafe = newlyFoundUnsafeStates.and(bckwdTransEvents);
            
            bckwdTransEvents = bckwdTransEvents.and(bTransFromUnsafe.not());

            BDD fTrans = bTransFromUnsafe
                    .exist(bddExAutomata.getSourceVariablesVarSet())
                    .replace(bddExAutomata.getDestToSourceVariablePairing());

            frwdTrans = frwdTrans.and(fTrans.not());

            // inevitably unsafe states + unsure states
            BDD possibleUnsafeStates = fTrans.exist(bddExAutomata.getEventVarSet());
            
            // unsure states
            /*BDD nonUnsafeStates = possibleUnsafeStates.and(frwdTrans)
                    .exist(bddExAutomata.getEventVarSet());*/
            
            // use relprod 
            BDD nonUnsafeStates = possibleUnsafeStates
                    .relprod(frwdTrans, bddExAutomata.getEventVarSet());

            // inevitably unsafe states - unsafe states found before
            newlyFoundUnsafeStates = possibleUnsafeStates
                    .and(nonUnsafeStates.not()).and(unsafeStates.not());
            
            inevitableUnsafeStates = inevitableUnsafeStates.or(newlyFoundUnsafeStates);
            
            // unsure states + inevitable events 
            tTrans.andWith(newlyFoundUnsafeStates.not())
                    .orWith(nonUnsafeStates.andWith(fTrans));

        } while (!newlyFoundUnsafeStates.isZero());
        
        BDD[] elements = {inevitableUnsafeStates, tTrans};
        
        return elements;
    }
    
    private BDD extractLargerUnsafeStates(BDD unsafeStates, BDD tTrans) {
        
        //System.err.println("enter this methed");
        //computedEventRelatedMaps();
        
        BDDIterator tSourceStateItr = tTrans
                .iterator(bddExAutomata.getSourceStagesVarSet());
        
        BDD partialTTrans = getZeroBDD(); // \Delta_P
        
        while (tSourceStateItr.hasNext()) {

            BDD tState = tSourceStateItr.nextBDD(); // \varrho 
            
            /*BDD enabledEvents = tState.and(tTrans) // \Gamma_{\varrho}
                    .exist(bddExAutomata.getSourceStagesVarSet());*/
            
            // use relprod 
            BDD enabledEvents = tState
                    .relprod(tTrans, bddExAutomata.getSourceStagesVarSet());

            BDDIterator sigmaItr = enabledEvents.iterator(bddExAutomata.getEventVarSet());
            
            BDD partialStates = getOneBDD(); // \varrho':= 1
            
            while (sigmaItr.hasNext()) {
                
                BDD sigma = sigmaItr.nextBDD();
                
                int retainedStageVarIndex = eventBDDToSourceStageVarIndexMap.get(sigma);
                
                BDD g = tState.exist(varSetsTobeExisted
                        .get(stageVarIndexList.indexOf(retainedStageVarIndex)))
                        .replace(bddExAutomata.sourceToTempVariablePairing);
                
                /*g = g.and(largerVarValues
                        .get(stageVarIndexList.indexOf(retainedStageVarIndex)))
                        .exist(bddExAutomata.tempVariablesVarSet);*/
                
                // use relprod
                g = g.relprod(largerVarValues
                        .get(stageVarIndexList.indexOf(retainedStageVarIndex)), 
                        bddExAutomata.tempVariablesVarSet);
                
                //partialTTrans.orWith(g.and(sigma));
                partialStates.andWith(g);    
            }
                                
            partialTTrans.orWith(partialStates.and(enabledEvents));
        }
        
        BDD guTrans = partialTTrans.and(frwdTrans); // \Delta_{GU}
        
        BDDIterator tTransItr = tTrans.iterator(bddExAutomata.getSourceStagesVarSet()
                .union(bddExAutomata.getEventVarSet()));
        
        BDD LargerGUTrans = getZeroBDD();
        
        while (tTransItr.hasNext()) {
            
            BDD aTTran = tTransItr.nextBDD();
            
            BDD tTempStateEvent = aTTran.replace(bddExAutomata.sourceToTempVariablePairing);

            BDD possibleLargerGUTrans = guTrans;
            
            for (int i = 0; i < stageVarIndexList.size(); i++) {

                /*BDD partialLargerStateEvent = tTempStateEvent.exist(
                        varSetsTobeExisted.get(i))
                        .and(largerVarValues.get(i))
                        .exist(bddExAutomata.tempVariablesVarSet);*/
                
                // use relprod 
                BDD partialLargerStateEvent = tTempStateEvent.exist(
                        varSetsTobeExisted.get(i))
                        .relprod(largerVarValues.get(i), bddExAutomata.tempVariablesVarSet);

                possibleLargerGUTrans = possibleLargerGUTrans.and(partialLargerStateEvent);

                if (possibleLargerGUTrans.isZero()) {
                    break;
                }
            }
            
            LargerGUTrans = LargerGUTrans.or(possibleLargerGUTrans);
        }
          
        frwdTrans.andWith(LargerGUTrans.not());
        bckwdTransEvents.andWith(LargerGUTrans
                .replace(bddExAutomata.getSourceToDestVariablePairing()).not());
                
        // extract unsafe states
        BDD possibleUnsafeStates = LargerGUTrans
                .exist(bddExAutomata.getEventVarSet());
        
        /*BDD nonUnsafeStates = possibleUnsafeStates.and(frwdTrans)
                .exist(bddExAutomata.getEventVarSet());*/
        
        // use relprod
        BDD nonUnsafeStates = possibleUnsafeStates.relprod(frwdTrans, 
                bddExAutomata.getEventVarSet());
        
        BDD newUnsafeStates = possibleUnsafeStates
                .and(nonUnsafeStates.not())
                .and(unsafeStates.not());
  
        return newUnsafeStates;
    }

    private BDD getFeasibleSourceStates() {
        
        if (feasibleSourceStates == null) {
            
            final ExpressionParser parser = 
                    new ExpressionParser(ModuleSubjectFactory.getInstance(), 
                    CompilerOperatorTable.getInstance());

            SimpleExpressionSubject feasibleEquation = null;
            
            try {
                feasibleEquation = (SimpleExpressionSubject) 
                        (parser.parse(FlowerEFABuilder.feasibleEquation, 
                        Operator.TYPE_BOOLEAN));
            } catch (final ParseException pe) {
                System.err.println(pe);
            }

            feasibleSourceStates = guard2BDD(feasibleEquation);

            return feasibleSourceStates;
        }
        
        return feasibleSourceStates;
    }

    BDD getFeasibleDestStates() {
        
        if (feasibleDestStates == null) {
            
            feasibleDestStates = getFeasibleSourceStates()
                    .replace(bddExAutomata.getSourceToDestVariablePairing());
            
            return feasibleDestStates;
        }

        return feasibleDestStates;
    }
    
    //####### END #######################################################################
    //###################################################################################
    
    // This method might be useful when partititioning the state-space
    /*private void computedEventRelatedMaps() {
        
        eventToStageVars = new HashMap<BDD, Set<String>>();
        eventToStageVarSet = new HashMap<BDD, BDDVarSet>();
        eventToOtherVarSet = new HashMap<BDD, BDDVarSet>();
        //eventToStageComBDD = getZeroBDD();

        final ExpressionParser parser =
                new ExpressionParser(ModuleSubjectFactory.getInstance(),
                CompilerOperatorTable.getInstance());

        for (Map.Entry<String, Integer> entry :
                FlowerEFABuilder.eventToResourceBlocked.entrySet()) {

            int eventIndex = bddExAutomata.theIndexMap.getEventIndex(
                    bddExAutomata.theIndexMap.eventIdToProxy(entry.getKey()));

            BDD eventBDD = factory.buildCube(eventIndex,
                    bddExAutomata.getEventDomain().vars());
            
            

            BDDVarSet relatedVarSet = createEmptyVarSet();

            BDDVarSet otherVarSet = createEmptyVarSet();

            Set<String> relatedVars = FlowerEFABuilder.resourceToBlockedStageVars
                    .get(entry.getValue());

            eventToStageVars.put(eventBDD, relatedVars);

            for (String var : relatedVars) {

                relatedVarSet = relatedVarSet
                        .union(bddExAutomata.getSourceVariableDomain(
                        bddExAutomata.theIndexMap.getVariableIndexByName(var)).set());
            }

            eventToStageVarSet.put(eventBDD, relatedVarSet);

            for (VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {

                if (!relatedVars.contains(var.getName())) {

                    otherVarSet = otherVarSet
                            .union(bddExAutomata.getSourceVariableDomain(
                            bddExAutomata.theIndexMap.getVariableIndex(var)).set());
                }
            }

            eventToOtherVarSet.put(eventBDD, otherVarSet);

            SimpleExpressionSubject blockEquation = null;

            try {
                blockEquation = (SimpleExpressionSubject) (parser
                        .parse(FlowerEFABuilder.resourceIndexToBlockEquation
                        .get(entry.getValue()), Operator.TYPE_BOOLEAN));
            } catch (final ParseException pe) {
                System.err.println(pe);
            }

            eventToStageComBDD.orWith(eventBDD.and(guard2BDD(blockEquation)));
        }
    }*/
}
