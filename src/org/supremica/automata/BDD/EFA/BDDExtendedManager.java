package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import gnu.trove.set.hash.TIntHashSet;

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
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.properties.Config;

public class BDDExtendedManager extends BDDAbstractManager {

    //Variabes used for RAS models
    BDD globalLargerBDD = null;
    //The BDD representing the forward transition relation, where the dest variables are removed.
    BDD frwdTrans = null;
//    BDD completeFrwdTrans = null;
    //The BDD representing the backward transition relation
    BDD bckwdTransEvents = null;
    BDD frwdLoadingTrans = null;
    BDD globalOneStepLargerBDD = null;
    BDD feasibleSourceStates = null;
    BDD unreachableStates = null;
    TIntObjectHashMap<BDD> event2ParitionBDD = null;
    BDD[] partitionBDDs = null;
    BDD[] possUnsafeStatesBDDs = null;
    BDD[] unsafeStatesTargetTrans = null;
    int componentSize;
    BDD minimalBoundaryUnsafeStates = null;
    TIntArrayList stageVarIndexList = null;
    List<BDDVarSet> varSetsTobeExisted = null;
    List<BDD> largerVarValues = null;
    List<BDD> smallerVarValues = null;
    Map<BDD, Set<String>> eventToStageVars;
    Map<BDD, BDDVarSet> eventToStageVarSet;
    Map<BDD, BDDVarSet> eventToOtherVarSet;
    HashMap<BDD, BDD> eventBDDToTransitionBDD = null;

    private boolean iddSatCount = false;
    private long minStatesIDDSatCountDenominator = 1L;
    public long nbrMinBoundUnsafeStates = 0L;

    public int maxBDDSizeClassic = 0;
    public int maxBDDSizeAlternative = 0;
    public int maxBDDSizeMinimization = 0;
    public int maxBDDSizePartitioning = 0;

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
        BDD uncontrollableStates = getZeroBDD();
        if (bddExAutomata.orgExAutomata.modelHasNoPlants()
                || bddExAutomata.orgExAutomata.modelHasNoSpecs()) {
            return getZeroBDD();
        } else {
            final BDD t1 = edges.getPlantMonolithicUncontrollableEdgesForwardBDD();
            final BDD t2 = edges.getSpecMonolithicUncontrollableEdgesForwardBDD().and(t1).exist(bddExAutomata.getDestStatesVarSet());
            uncontrollableStates =  t1.and(t2.not()).exist(bddExAutomata.getDestStatesVarSet()).exist(bddExAutomata.getEventVarSet());
        }
//        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
//            uncontrollableStates = uncontrollableStates.or(getInitiallyTimedUncontrollableStates());
//        }

        return uncontrollableStates;

    }

    public BDD getInitiallyTimedUncontrollableStates()
    {
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD statesEnablingForc = (bddEdges.getMonolithicForcibleSpecEdgesForwardBDD()).exist(bddExAutomata.getDestStatesVarSet());
        return statesEnablingForc.not().and(
                bddEdges.getStatesTickDisabled(bddEdges.getPlantMonolithicEdgesForwardBDD(), bddExAutomata.getPlantSourceLocationInvariants()).not()).and(
                bddEdges.getStatesTickDisabled(bddEdges.getSpecMonolithicEdgesForwardBDD(), bddExAutomata.getSpecSourceLocationInvariants()));
    }

    public BDD uncontrollableBackward(final BDD forbidden) {
        System.err.println("UncontrollableBackward entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD t_u = bddEdges.getMonolithicUncontrollableEdgesBackwardBDD();
        @SuppressWarnings("unused")
        final BDD backwardTime = bddEdges.getBackwardClocksWithTheSameRate();
        @SuppressWarnings("unused")
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();


//        System.out.println("forbidden");
//        forbidden.printDot();
        BDD Qk = null;
        BDD newUCstates = null;
        @SuppressWarnings("unused")
        final
        BDD newCstates = null;
        BDD Qkn = forbidden.id();
//        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
//            BDD ucDueToTime = timeEvolSource(Qkn, backwardTime).and(Qkn.not());
//            newCstates = Qkn.and(bddEdges.getMonolithicForcibleEdgesForwardBDD().exist(bddExAutomata.getDestStatesVarSet()));
//            newCstates = timeEvolSource(newCstates, backwardTime);
//            ucDueToTime = ucDueToTime.and(newCstates.not());
////            Qkn = timeEvolSource(Qkn, forwardTime);
//
////        System.out.println("Qknnnnnnn: "+(Qkn.isOne()?"one":""));
////        Qkn.printDot();
//
//            Qkn = Qkn.or(ucDueToTime);
/////            bddEdges.removeFromMonolithicForcibleEdgesForwardBDD(Qkn);
//        }
        int i = 0;
        do {
            System.err.println("ub: "+(i++));
            Qk = Qkn.id();
            newUCstates = image_preImage(Qk, t_u);
            Qkn = Qk.or(newUCstates);
//            Qkn = timeEvolSource(Qkn, backwardTime);
//            Qkn = timeEvolSource(Qkn, forwardTime);
//            Qkn = timeEvolSource(bddExAutomata.getReachableStates().and(Qkn), forwardTime);

//            if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
//                BDD ucDueToTime = timeEvolSource(Qkn, backwardTime).and(Qkn.not());
//                newCstates = ucDueToTime.and(bddEdges.getMonolithicForcibleEdgesForwardBDD().exist(bddExAutomata.getDestStatesVarSet()));
//                newCstates = timeEvolSource(newCstates, backwardTime);
//                ucDueToTime = ucDueToTime.and(newCstates.not());
////                ucDueToTime = timeEvolSource(ucDueToTime, forwardTime);
/////                bddEdges.removeFromMonolithicForcibleEdgesForwardBDD(ucDueToTime);
//                Qkn = Qkn.or(ucDueToTime);
//            }
        } while (!Qkn.equals(Qk));

        System.err.println("UncontrollableBackward exited.");
        return Qkn;
    }

    public BDD restrictedBackward(final BDD markedStates, final BDD forb) {
        System.err.println("RestrictedBackward entered.");

        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD trans = bddEdges.getMonolithicEdgesBackwardBDD();
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();
        final BDD backwardTime = bddEdges.getBackwardClocksWithTheSameRate();

        BDD forbidden = forb.id();
        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty())
        {
           forbidden = timeEvolSource(forb,forwardTime);
        }

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
        int i = 0;
        do {
            System.err.println("rb "+(i++));
/*            try
             {
             out.write((iteration++) + "\t" + Qkn.nodeCount());
             out.newLine();
             out.close();
             } catch (final Exception e){}
             */
            Qk = Qkn.id();
            Qm = image_preImage(Qk, trans, backwardTime);//.and(bddExAutomata.getReachableStates());

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

    public BDD restrictedForward(final BDD initialStates, final BDD forb) {
        System.err.println("RestrictedForward entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD trans = bddEdges.getMonolithicEdgesForwardBDD();
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();

//        System.out.println("restrictedForward");

        final BDD forbidden = forb.id();
//        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty())
//        {
//           forbidden = timeEvolSource(forb,forwardTime);
//        }

        BDD Qkn = initialStates.and(forbidden.not());
        BDD Qk;
        BDD Qm;


//        FileWriter fstream = null;
//        try
//        {
//            fstream = new FileWriter("/Users/sajed/Desktop/fxdPoint.txt");
//        } catch (final Exception e){}
//        out = new BufferedWriter(fstream);
        int iteration = 0;

        do {
            System.err.println("RForward "+(iteration++) + "\t" + Qkn.nodeCount());

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

//            System.err.println("("+(iteration++)+","+Qkn.nodeCount()+")");
            Qk = Qkn.id();
            Qm = image_preImage(Qk, trans, forwardTime);

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

//        System.out.println("RestrictedForward exited.");
        System.err.println("RestrictedForward exited.");
        return Qkn;
    }

    public BDD nonblockingControllable(final BDD forb, final boolean reachable) {
        System.err.println("NonblockingControllable entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();

        bddExAutomata.computeReachableStates();

        BDD forbidden = forb.id();

        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty())
        {
           forbidden = timeEvolSource(forb,forwardTime);
        }

        BDD Qkn = forbidden;
        BDD Qk;
        BDD Q1;
        BDD Q2;
        int i = 0;
        do {
            System.err.println("nbc i: "+(i++));
            Qk = Qkn.id();
            Q1 = restrictedBackward(bddExAutomata.getMarkedStates(), Qk);
            BDD forbiddenStates = Q1.not();//.and(bddExAutomata.getReachableStates());
            if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
                forbiddenStates = bddExAutomata.fitIntoClockDomains(forbiddenStates).and(bddExAutomata.getMarkedStates().not());
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

            final TIntHashSet sharedUncontrollableEvents = new TIntHashSet(specUncontrollableEvents.toArray());
            sharedUncontrollableEvents.retainAll(plantUncontrollableEvents.toArray());

            final TIntObjectHashMap<BDD> plantsEnabledStates =
                    new BDDPartitionUncontSetEve(bddExAutomata, bddExAutomata.plants, plantUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final TIntObjectHashMap<BDD> specEnabledStates =
                    new BDDPartitionUncontSetEve(bddExAutomata, bddExAutomata.specs, specUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final BDD uncontrollableStates = getZeroBDD();

            for(final TIntIterator itr = sharedUncontrollableEvents.iterator(); itr.hasNext();) {
                final int unConEventIndex = itr.next();
                final BDD statesEnabledByPlants = plantsEnabledStates.get(unConEventIndex).and(bddExAutomata.getReachableStates());
                    final BDD statesEnabledBySpecs = specEnabledStates.get(unConEventIndex).and(bddExAutomata.getReachableStates());
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

    /*
     * On-the-fly synthesis based on forward reachability
     */
    public BDD onTheFlySynthesis(final BDD initialStates, final BDD forb) {
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD frwdTrans = bddEdges.getMonolithicEdgesForwardBDD().id();
        final BDD bkwdUnconTrans = bddEdges.getMonolithicUncontrollableEdgesBackwardBDD();
        final BDD forbidden = forb.id().or(coreachability(bkwdUnconTrans, forb.id()));
        pruneTrans(frwdTrans, forbidden);
//        pruneTrans(bkwdUnconTrans, forbidden);

        /*
         * The transition relations that are incrementally created during the
         * fixed point computations
         */
//        BDD localFrwdTrans = getZeroBDD();

        BDD localBadStates;
        BDD localBadStatesExt;
        BDD localUnconStates;
        @SuppressWarnings("unused")
        final
        BDD unconStates = getZeroBDD();

        BDD Qkn = initialStates.and(forbidden.not());
        BDD Qk;
        BDD Qm = Qkn.id();

        BDD nextStates;
        BDD nextTrans;
        BDD nextBlockingStates;
        int i = 0;
        int j;

//        BDD backTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesBackwardBDD();
//        System.err.println("computing coreachable states...");
//        BDD coreachable = coreachability(backTrans, bddExAutomata.getMarkedStates());
//        System.err.println("coreachable states computed.");

//        frwdTrans = frwdTrans.and(coreachable);

        do {
            System.err.println("i: "+i);
            Qk = Qkn.id();
            nextTrans = frwdTrans.and(Qk);
//            localFrwdTrans = localFrwdTrans.or(nextTrans);

            /*
             * Image operator
             */
            System.err.println("Performing the image operator...");
            nextStates = nextTrans.exist(bddExAutomata.getSourceStatesVarSet());
            Qm = bddExAutomata.destToSource(nextStates);
            Qkn = Qk.or(Qm);
            System.err.println("Image computed.");

            /*
             * Perform a local analysis (synthesis):
             *     - find all blocking and uncontrollable states starting
             *       from the localBadStates
             *     - find states that are on the edge between the locally good
             *       and bad states
             */
            System.err.println("Procedure: finding new blocking states...");
            localBadStates = getBlockingStates(frwdTrans, Qm);
            System.err.println("Procedure done.");
            localBadStatesExt = getZeroBDD();
            localUnconStates = getZeroBDD();
            j = 0;
            while(!localBadStates.equals(localBadStatesExt))
            {
                System.err.println("j: "+j);
                localBadStatesExt = localBadStates.id();
                /*
                 * Compute the local uncontrollable states
                 */

//                //Initialize the uncontrollable backward transitions
//                if(j == 0)
//                {
//                    localUnconBkwdTrans = bddExAutomata.sourceTooTdest(localFrwdTrans.and(
//                    bddEdges.getMonolithicEdgesForwardWithEventsBDD()).and(
//                    bddExAutomata.uncontrollableEventsBDD)).exist(bddExAutomata.getEventVarSet());
//
//                }
                System.err.println("compute local uncon states...");
                localUnconStates = coreachability(bkwdUnconTrans, localBadStates);
                localUnconStates = localUnconStates.and(localBadStates.not());
                System.err.println("local uncon states computed.");

                localBadStates = localBadStates.or(localUnconStates);
//                localBadStates.printDot();

                System.err.println("local prune...");
                pruneTrans(frwdTrans, localBadStates);
//                localFrwdTrans = localFrwdTrans.and(bddExAutomata.sourceToDest(localBadStates).not());

//                unconStates = unconStates.or(localUnconStates);

                Qkn = Qkn.and(localBadStates.not());
                System.err.println("computing next local bad states...");
                nextBlockingStates = getBlockingStates(frwdTrans, Qkn);
                System.err.println("next local bad states comoputed.");
                localBadStates = localBadStates.or(nextBlockingStates);
                j++;
            }

            /*
             * Remove unnecessary visited states, visited thorugh local
             * uncontrollable states and do not continue traversing from those states
             */
            /*
            if(!unconStates.isZero())
            {
                Qkn = Qkn.and(unnecessaryVisitedStates(localFrwdTrans,
                                                   frwdTrans,
                                                   unconStates).not());

            }
            */
            i++;
        } while (!Qkn.equals(Qk));


//        Qkn.printDot();

        return Qkn;
    }

    /*
     * Remove transitions that includden forbidden states
     */
    private void pruneTrans(final BDD trans, final BDD forbiddenStates)
    {
        trans.andWith(forbiddenStates.not());
        trans.andWith(bddExAutomata.sourceToDest(forbiddenStates).not());
    }

    /*
     * Perform a forward reachability from each state in unconStates. For a state
     * in unconStates, the traversing is stopped when a state is reached that has
     * at least an incoming transition
     */
    @SuppressWarnings("unused")
    private BDD unnecessaryVisitedStates(final BDD localFrwdTrans, final BDD frwdTrans, final BDD states)
    {
        BDD Qkn = states.id();
        BDD Qk;
        BDD incStates;
        BDD tmpLocalFrwdTrans = localFrwdTrans.id();
        BDD tmpFrwdTrans = frwdTrans.id();
        BDD nextStates;
        /*
         * Remove the states that have incoming transitions
         */
        int i = 0;
        do
        {
            System.err.println("unnVS: "+i);
            Qk = Qkn.id();
            nextStates = image_preImage(Qkn, tmpLocalFrwdTrans);
            tmpFrwdTrans = tmpFrwdTrans.and(Qkn.not());
            incStates = statesWithIncomingTrans(nextStates, tmpFrwdTrans);
            nextStates = nextStates.and(incStates.not());
            tmpLocalFrwdTrans = tmpLocalFrwdTrans.and(bddExAutomata.sourceToDest(incStates).not());
            Qkn = Qkn.or(nextStates);
            i++;
        }while(!Qkn.equals(Qk));

        return Qkn;
    }

    private BDD statesWithIncomingTrans(final BDD states, final BDD frwdTrans)
    {
        final BDD incomingTrans = frwdTrans.and(bddExAutomata.sourceToDest(states));
        return bddExAutomata.destToSource(incomingTrans.exist(bddExAutomata.getSourceStatesVarSet()));
    }

    private BDD coreachability(final BDD trans, final BDD states) {

        BDD Qk;
        BDD Qkn = states.id();
        do {
            Qk = Qkn.id();
            Qkn = Qk.or(image_preImage(Qk, trans));
        } while (!Qkn.equals(Qk));

        return Qkn;
    }

    private BDD getBlockingStates(final BDD forwardTrans, final BDD states)
    {
        final BDD notBlocking = forwardTrans.and(states).exist(bddExAutomata.getDestStatesVarSet());
        return (states.and(bddExAutomata.getMarkedStates().not()).and(notBlocking.not()));
    }
    // ##################################################################################
    // ################ THE MAIN ALGORITHM 1 AND 2 (SEE THE TASE PAPER) #################
    // ### Zhennan

    // Compute the boundary unsafe states with the extension of the SCT algorithm
    public BDD computeBoundaryUnsafeStatesClassic() {

        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardBDD()
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));

        final BDD initialStateBDD = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());

        BDD Qkn = initialStateBDD;
        BDD Qk = null;
        BDD Qm = null;


        do {
            Qk = Qkn.id();

            final BDD tmp = frwdTrans.and(Qkn);
            if (maxBDDSizeClassic < tmp.nodeCount()) {
                maxBDDSizeClassic = tmp.nodeCount();
            }

            Qm = tmp.exist(bddExAutomata.getSourceVariablesVarSet());

            Qm.replaceWith(bddExAutomata.getDestToSourceVariablePairing());

            Qkn = Qk.or(Qm);
            if (maxBDDSizeClassic < Qkn.nodeCount()) {
                maxBDDSizeClassic = Qkn.nodeCount();
            }

            tmp.free();
            Qm.free();

        } while (!Qkn.equals(Qk));

        final BDD reachableStatesBDD = Qkn.id();

        Qk.free();
        Qkn.free();

        long nbrOfReachableStates = (long) reachableStatesBDD
                .satCount(bddExAutomata.getSourceVariablesVarSet());

        if (nbrOfReachableStates <= 1L) {

            iddSatCount = true;

            final IDD reachableStatesIDD = bddExAutomata
                .generateIDD(reachableStatesBDD, reachableStatesBDD);

            nbrOfReachableStates = bddExAutomata
                    .nbrOfStatesIDD(reachableStatesIDD).longValue();
        }

        System.out.println("The number of reachable states is " + nbrOfReachableStates);

        Qkn = initialStateBDD;
        Qk = null;
        Qm = null;

        do {
            Qk = Qkn.id();

            final BDD tmp = frwdTrans.and(Qkn
                    .replaceWith(bddExAutomata.getSourceToDestVariablePairing()));

            if (maxBDDSizeClassic < tmp.nodeCount()) {
                maxBDDSizeClassic = tmp.nodeCount();
            }

            Qm = tmp.exist(bddExAutomata.getDestVariablesVarSet());

            Qkn = Qk.or(Qm);
            if (maxBDDSizeClassic < Qkn.nodeCount()) {
                maxBDDSizeClassic = Qkn.nodeCount();
            }

            tmp.free();
            Qm.free();

        } while (!Qkn.equals(Qk));

        final BDD coreachableStatesBDD = Qkn.id();

        Qkn.free();
        Qk.free();

        final BDD safeStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);

        reachableStatesBDD.free();
        coreachableStatesBDD.free();

        BDD boundaryUnsafeStatesBDD = safeStatesBDD.and(frwdTrans)
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        if (maxBDDSizeClassic < boundaryUnsafeStatesBDD.nodeCount()) {
            maxBDDSizeClassic = boundaryUnsafeStatesBDD.nodeCount();
        }

        boundaryUnsafeStatesBDD = boundaryUnsafeStatesBDD.and(safeStatesBDD.not());
        if (maxBDDSizeClassic < boundaryUnsafeStatesBDD.nodeCount()) {
            maxBDDSizeClassic = boundaryUnsafeStatesBDD.nodeCount();
        }

        safeStatesBDD.free();
        frwdTrans.free();

        System.out.println("The maximal size of intermediate BDDs is "
                + maxBDDSizeClassic);

        long nbrBoundaryUnsafeStates = 0;

        if (iddSatCount) {
            final IDD boundaryUnsafeStatesIDD = bddExAutomata
                    .generateIDD(boundaryUnsafeStatesBDD, boundaryUnsafeStatesBDD);
            nbrBoundaryUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(boundaryUnsafeStatesIDD).longValue();
        } else {
            nbrBoundaryUnsafeStates = (long) boundaryUnsafeStatesBDD
                .satCount(bddExAutomata.getSourceVariablesVarSet());
        }

        System.out.println("The number of reachable boundary unsafe states is " +
                + nbrBoundaryUnsafeStates);

        boundaryUnsafeStatesBDD = boundaryUnsafeStatesBDD
                .exist(bddExAutomata.getSourceResourceVarSet());

        return boundaryUnsafeStatesBDD;
    }

    // Compute boundary unsafe states with the alternative algorithm
    public BDD computeBoundaryUnsafeStatesAlternative() {

      feasibleSourceStates = getFeasibleSourceStatesBDD();

      frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardWithEventsBDD()
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));

        frwdLoadingTrans = frwdTrans.and(bddExAutomata.loadEventsBDD)
                .exist(bddExAutomata.getEventVarSet());

        final BDD deadlocks = getDeadlocks();

        BDD boundaryUnsafeStates  = computeBoundaryUnsafeStates(deadlocks);

        long nbrOfBoundUnsafeStates = 0;
        if (iddSatCount) {
            final IDD boundUnsafeStatesIDD = bddExAutomata
                    .generateIDD(boundaryUnsafeStates, boundaryUnsafeStates);

            nbrOfBoundUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(boundUnsafeStatesIDD).longValue();
        } else {
            nbrOfBoundUnsafeStates = (long) boundaryUnsafeStates
                    .satCount(bddExAutomata.getSourceVariablesVarSet());
        }

        System.out.println("The number of boundary states is " + nbrOfBoundUnsafeStates);

        boundaryUnsafeStates = boundaryUnsafeStates
                .exist(bddExAutomata.getSourceResourceVarSet());

        frwdLoadingTrans.free();
        frwdTrans.free();

        return boundaryUnsafeStates;
    }

    // Backtrack deadlock states to find all boundary unsafe states
    private BDD computeBoundaryUnsafeStates(final BDD deadlocks) {

        BDD newUnsafeStates = deadlocks.id();

        BDD unsafeStates = deadlocks.id();

        final BDD tTrans = getZeroBDD();

        do {

            final BDD fTransToUnsafeStates = newUnsafeStates
                    .replace(bddExAutomata.getSourceToDestVariablePairing())
                    .and(frwdTrans);
            if (maxBDDSizeAlternative < fTransToUnsafeStates.nodeCount()) {
                maxBDDSizeAlternative = fTransToUnsafeStates.nodeCount();
            }

            final BDD possibleUnsafeStates = fTransToUnsafeStates
                    .exist(bddExAutomata.getDestVariablesVarSet());

            final BDD transFromPossUnStates = possibleUnsafeStates.and(frwdTrans);
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

            tTrans.orWith(fTransToUnsafeStates).andWith(newUnsafeStates.not());

            newUnsafeStates.andWith(unsafeStates.not());

            unsafeStates = unsafeStates.or(newUnsafeStates);

            if (maxBDDSizeAlternative < tTrans.nodeCount()) {
                maxBDDSizeAlternative = tTrans.nodeCount();
            }

            possibleUnsafeStates.free();
            transFromPossUnStates.free();
            nonUnsafeStates.free();

        } while (!newUnsafeStates.isZero());

        BDD boundaryUnsafeStates = unsafeStates
                .replace(bddExAutomata.getSourceToDestVariablePairing())
                .and(frwdLoadingTrans);
        if (maxBDDSizeAlternative < boundaryUnsafeStates.nodeCount()) {
            maxBDDSizeAlternative = boundaryUnsafeStates.nodeCount();
        }


        boundaryUnsafeStates.andWith(unsafeStates.not());
        if (maxBDDSizeAlternative < boundaryUnsafeStates.nodeCount()) {
            maxBDDSizeAlternative = boundaryUnsafeStates.nodeCount();
        }

        boundaryUnsafeStates = boundaryUnsafeStates
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        if (maxBDDSizeAlternative < boundaryUnsafeStates.nodeCount()) {
            maxBDDSizeAlternative = boundaryUnsafeStates.nodeCount();
        }

        boundaryUnsafeStates.orWith(tTrans.exist(bddExAutomata.getSourceVariablesVarSet())
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing()));
        if (maxBDDSizeAlternative < boundaryUnsafeStates.nodeCount()) {
            maxBDDSizeAlternative = boundaryUnsafeStates.nodeCount();
        }

        System.out.println("The maximal size of intermediate BDDs is "
                + maxBDDSizeAlternative);

        // cleanup
        newUnsafeStates.free();
        unsafeStates.free();
        tTrans.free();

        return boundaryUnsafeStates;
    }

    // Compute all deadlock states
    private BDD getDeadlocks() {

        final BDD potentialDeadlockStates = frwdTrans
                .exist(bddExAutomata.getEventVarSet()
                .union(bddExAutomata.getSourceVariablesVarSet()))
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        frwdTrans = frwdTrans.relprod(bddExAutomata.loadEventsBDD.not(),
                bddExAutomata.getEventVarSet());

        final BDD notDeadlockStates = frwdTrans.exist(bddExAutomata.getDestVariablesVarSet());

        final BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());

        final BDD deadlocks = potentialDeadlockStates.and(notDeadlockStates.not()).and(initv.not());

        deadlocks.andWith(feasibleSourceStates);

        long nbrOfDeadlockStates = (long) deadlocks
                .satCount(bddExAutomata.getSourceVariablesVarSet());
        if (nbrOfDeadlockStates <= 1L) {

            iddSatCount = true;

            final IDD deadlockStatesIDD = bddExAutomata
                    .generateIDD(deadlocks, deadlocks);
            nbrOfDeadlockStates = bddExAutomata
                    .nbrOfStatesIDD(deadlockStatesIDD).longValue();
        }

        System.out.println("The number of feasible deadlock states is: " +
                nbrOfDeadlockStates);

        // cleanup
        potentialDeadlockStates.free();
        notDeadlockStates.free();
        initv.free();

        return deadlocks;
    }

    // subroutine: remove the larger states from unsafeStates
    public BDD removeLargerStates(final BDD boundaryStates) {

        buildHelpers();

        final BDDIterator itr = boundaryStates.iterator(bddExAutomata.getSourceStagesVarSet());

        final BDD largerStates = getZeroBDD();

        while (itr.hasNext()) {

            final BDD anUnsafeState = itr.nextBDD();

            if (anUnsafeState.and(largerStates).isZero()) {

                final BDD tempUnsafeState = anUnsafeState
                        .replace(bddExAutomata.sourceToTempVariablePairing);

                final BDD possibleLargerStates = boundaryStates.and(anUnsafeState.not());

                final BDD localLargerStates = getOneBDD();

                for (int i = 0; i < stageVarIndexList.size(); i++) {

                    final BDD partialLargerTempStates = tempUnsafeState.exist(varSetsTobeExisted.get(i));

                    final BDD partialLargerState = partialLargerTempStates
                            .relprod(largerVarValues.get(i),bddExAutomata.tempVariablesVarSet);

                    localLargerStates.andWith(partialLargerState);

                    if (maxBDDSizeMinimization < localLargerStates.nodeCount()) {
                        maxBDDSizeMinimization = localLargerStates.nodeCount();
                    }

                    partialLargerTempStates.free();
                }
                largerStates.orWith(localLargerStates.andWith(possibleLargerStates));
                if (maxBDDSizeMinimization < largerStates.nodeCount()) {
                    maxBDDSizeMinimization = largerStates.nodeCount();
                }
                tempUnsafeState.free();
            }

            anUnsafeState.free();
        }

        minimalBoundaryUnsafeStates = boundaryStates.and(largerStates.not());
        System.out.println("The maximal size of BDDs during the minimization is: "
                + maxBDDSizeMinimization);

        if (iddSatCount) {
            computeMinStatesIDDSatCountDenominator();
            final IDD minBoundUnsafeStatesIDD = bddExAutomata
                    .generateIDD(minimalBoundaryUnsafeStates, minimalBoundaryUnsafeStates);
            nbrMinBoundUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(minBoundUnsafeStatesIDD)
                    .longValue() / minStatesIDDSatCountDenominator;
        } else {
            nbrMinBoundUnsafeStates = (long) minimalBoundaryUnsafeStates
                .satCount(bddExAutomata.getSourceStagesVarSet());
        }

        System.out.println("The number of minimal boundary unsafe states is: "
                + nbrMinBoundUnsafeStates);

        //cleanup
        boundaryStates.free();
        largerStates.free();

        for(final BDD largValue: largerVarValues)
            largValue.free();

        return minimalBoundaryUnsafeStates;
    }

    // an alternative way to remove non-minimal RAS states
    public BDD removeLargerStatesAlternative(final BDD boundaryStates) {

        buildHelpers();

        final BDDIterator itr = boundaryStates.iterator(bddExAutomata.getSourceStagesVarSet());

        final BDD largerStates = getZeroBDD();

        while (itr.hasNext()) {

            final BDD anUnsafeState = itr.nextBDD();

            if (anUnsafeState.and(largerStates).isZero()) {

                final BDD tempUnsafeState = anUnsafeState
                        .replace(bddExAutomata.sourceToTempVariablePairing);

                final BDD possibleLargerStates = boundaryStates.and(anUnsafeState.not());

                for (int i = 0; i < stageVarIndexList.size(); i++) {

                    final BDD partialLargerTempStates = tempUnsafeState.exist(varSetsTobeExisted.get(i));

                    final BDD partialLargerState = partialLargerTempStates
                            .relprod(largerVarValues.get(i), bddExAutomata.tempVariablesVarSet);

                    possibleLargerStates.andWith(partialLargerState);

                    if (maxBDDSizeMinimization < possibleLargerStates.nodeCount()) {
                        maxBDDSizeMinimization = possibleLargerStates.nodeCount();
                    }

                    partialLargerTempStates.free();

                    if (possibleLargerStates.isZero()) {
                        break;
                    }

                }

                largerStates.orWith(possibleLargerStates);

                if (maxBDDSizeMinimization < largerStates.nodeCount()) {
                    maxBDDSizeMinimization = largerStates.nodeCount();
                }

                tempUnsafeState.free();
            }

            anUnsafeState.free();
        }

        minimalBoundaryUnsafeStates = boundaryStates.and(largerStates.not());
        System.out.println("The maximal size of BDDs during the minimization is: "
                + maxBDDSizeMinimization);

        if (iddSatCount) {
            computeMinStatesIDDSatCountDenominator();
            final IDD minBoundUnsafeStatesIDD = bddExAutomata
                    .generateIDD(minimalBoundaryUnsafeStates, minimalBoundaryUnsafeStates);
            nbrMinBoundUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(minBoundUnsafeStatesIDD)
                    .longValue() / minStatesIDDSatCountDenominator;
        } else {
            nbrMinBoundUnsafeStates = (long) minimalBoundaryUnsafeStates
                    .satCount(bddExAutomata.getSourceStagesVarSet());
        }

        System.out.println("The number of minimal boundary unsafe states is: "
                + nbrMinBoundUnsafeStates);

        //cleanup
        boundaryStates.free();
        largerStates.free();

        for (final BDD largValue : largerVarValues) {
            largValue.free();
        }

        return minimalBoundaryUnsafeStates;
    }

    // Build auxiliary BDDs used for computing the boundary unsafe states
    private void buildHelpers() {

        final int stageVarSize = bddExAutomata.orgExAutomata.getStageVars().size();

        stageVarIndexList = new TIntArrayList(stageVarSize);

        varSetsTobeExisted = new ArrayList<BDDVarSet>(stageVarSize);

        largerVarValues = new ArrayList<BDD>(stageVarSize);

        final List<VariableComponentProxy> stageVars = bddExAutomata.orgExAutomata.getStageVars();

        for (final VariableComponentProxy sv : stageVars) {

            final int svIndex = bddExAutomata.theIndexMap.getVariableIndex(sv);

            stageVarIndexList.add(svIndex);

            // put BDDVarSets of other stage vars together
            BDDVarSet otherVarsBDDVarSets = createEmptyVarSet();

            for (final VariableComponentProxy osv : stageVars) {

                if (!sv.equals(osv)) {

                    otherVarsBDDVarSets = otherVarsBDDVarSets
                            .union(bddExAutomata.stageVar2BDDTempVarSetMap.get(osv));
                }
            }

            varSetsTobeExisted.add(otherVarsBDDVarSets);

            // construct the BDD representing larger values of this stage var
            final BDD largerValues = getZeroBDD();

            final int stageDomain = bddExAutomata.orgExAutomata.getVarDomain(sv.getName());

            final BDDDomain stageSourceDomain = bddExAutomata.sourceVarDomains[svIndex];

            final SupremicaBDDBitVector stageBDDBitVectorSource =
                    createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                    bddExAutomata.orgExAutomata.getMinValueofVar(sv.getName()) < 0,
                    stageSourceDomain);

            for (int i = 0; i < stageDomain; i++) {

                final BDD tmp = createBDD(i, bddExAutomata.getTempVariableDomain(svIndex));

                final SupremicaBDDBitVector vector_j =
                        createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        stageSourceDomain.varNum(), i);

                final BDD greaterThanJBDD = stageBDDBitVectorSource.gte(vector_j);

                largerValues.orWith(tmp.andWith(greaterThanJBDD));
            }

            largerVarValues.add(largerValues);
        }
    }

    // Build the BDD of feasible states from the resource invariants (module comments)
    private BDD getFeasibleSourceStatesBDD() {

        if (feasibleSourceStates == null) {

            String[] feasibleEquationStrings = null;

            if (bddExAutomata.orgExAutomata.getModule() != null) {
              feasibleEquationStrings = bddExAutomata.orgExAutomata
                .getModule().getComment().split("&");
            } else {
              feasibleEquationStrings = bddExAutomata.orgExAutomata.feasiableEquation.split("&");
            }

            final SimpleExpressionProxy[] feasibleEquationExpressions =
              new SimpleExpressionProxy[feasibleEquationStrings.length];;

            final ExpressionParser parser =
                    new ExpressionParser(ModuleSubjectFactory.getInstance(),
                    CompilerOperatorTable.getInstance());

            try {
                for (int i = 0; i < feasibleEquationStrings.length; i++) {
                  feasibleEquationExpressions[i] = (parser.parse(feasibleEquationStrings[i], Operator.TYPE_BOOLEAN));
                }
            } catch (final ParseException pe) {
                System.err.println(pe);
            }

            feasibleSourceStates = getOneBDD();
            for (final SimpleExpressionProxy fe: feasibleEquationExpressions) {
              System.err.println("hh");
              feasibleSourceStates.andWith(guard2BDD(fe));
            }

            return feasibleSourceStates;
        }

        return feasibleSourceStates;
    }

    private void computeMinStatesIDDSatCountDenominator() {

        final ExtendedAutomata exAutomata = bddExAutomata.orgExAutomata;

        for(final VariableComponentProxy var: exAutomata.getVars()) {

            final String varName = var.getName();

            if (varName.contains(FlowerEFABuilder.RESOURCE_PREFIX)) {

                minStatesIDDSatCountDenominator *= exAutomata.getVarDomain(varName);
            }
        }
    }

    //####### END #######################################################################

    // ################ THE ALGORITHM USING PARTITIONING TECHNIQUES (TO BE WRITTEN)######
    // ### Zhennan

    public BDD computeBoundaryUnsafeStatesEventPartitioning() {

        setupPartitioningBDDs();

        System.err.println("Computing the deadlock states with the event-based "
                + "partitioning approach.");

        final BDD deadlocks = getDeadlocksWithPartitions();

        final BDD boundaryUnsafeStates = computeBoundaryUnsafeStatesWithEventPartitions(deadlocks);

        long nbrOfBoundUnsafeStates = 0L;
        if (iddSatCount) {
            final IDD boundUnsafeStatesIDD = bddExAutomata
                    .generateIDD(boundaryUnsafeStates, boundaryUnsafeStates);

            nbrOfBoundUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(boundUnsafeStatesIDD).longValue();
        } else {
            nbrOfBoundUnsafeStates = (long) boundaryUnsafeStates
                    .satCount(bddExAutomata.getSourceVariablesVarSet());
        }

        System.out.println("The number of boundary unsafe states is " + nbrOfBoundUnsafeStates);

        return boundaryUnsafeStates.exist(bddExAutomata.getSourceResourceVarSet());
    }

    // Get the event partial transition relation BDD
    private void setupPartitioningBDDs() {

        final BDDPartitionAlgoWorker worker = bddExAutomata.getParAlgoWorker();

        final BDDPartitionSet partitionSet = worker.partitions;

        event2ParitionBDD = partitionSet.getCompIndexToCompBDDMap();

        componentSize = event2ParitionBDD.size();

        partitionBDDs = new BDD[componentSize];

        frwdLoadingTrans = getZeroBDD();

        event2ParitionBDD.forEachEntry(new TIntObjectProcedure<BDD>() {

            BDDVarSet locationVars = bddExAutomata.getSourceLocationVarSet()
                    .union(bddExAutomata.getDestLocationVarSet());

            @Override
            public boolean execute(final int eventIndex, BDD partition) {

                final String eventName = bddExAutomata.theIndexMap
                        .getEventAt(eventIndex).getName();

                partition = partition.exist(locationVars);

                if (!eventName.contains(FlowerEFABuilder.LOAD_EVENT_PREFIX)) {

                    partitionBDDs[eventIndex] = partition;

                } else {
                    partitionBDDs[eventIndex] = getZeroBDD();
                    final BDD tmp = frwdLoadingTrans;
                    frwdLoadingTrans = frwdLoadingTrans.or(partition);
                    tmp.free();
                }
                return true;
            }
        });

        possUnsafeStatesBDDs = new BDD[componentSize];
        unsafeStatesTargetTrans = new BDD[componentSize];

        for (int i = 0; i < componentSize; i++) {
            possUnsafeStatesBDDs[i] = getZeroBDD();
            unsafeStatesTargetTrans[i] = getZeroBDD();
        }

        buildHelpers();

        feasibleSourceStates = getFeasibleSourceStatesBDD();
    }

    // Compute all deadlock states using partitioning techniques
    private BDD getDeadlocksWithPartitions() {

        final BDD nonDeadlocks = getZeroBDD();

        for (final BDD component : partitionBDDs) {

            nonDeadlocks.orWith(component
                    .exist(bddExAutomata.getDestVariablesVarSet()));
        }

        final BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet());

        final BDD deadlocks = frwdLoadingTrans.exist(bddExAutomata.getSourceVariablesVarSet())
                .replaceWith(bddExAutomata.getDestToSourceVariablePairing())
                .andWith(nonDeadlocks.not());

        for (final BDD component : partitionBDDs) {

            final BDD possResourceDeadlockStates = component
                    .exist(bddExAutomata.getSourceVariablesVarSet())
                    .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

            deadlocks.orWith(possResourceDeadlockStates
                    .andWith(nonDeadlocks.not()));
        }

        deadlocks.andWith(feasibleSourceStates).andWith(initv.not());

        // cleanup
        nonDeadlocks.free();
        initv.free();

        // compute the number of deadlock states
        long nbrOfDeadlockStates = (long) deadlocks.satCount(bddExAutomata.getSourceVariablesVarSet());

        if (nbrOfDeadlockStates <= 1) {
          iddSatCount = true;
          final IDD deadlockStatesIDD = bddExAutomata.generateIDD(deadlocks, deadlocks);
          nbrOfDeadlockStates = bddExAutomata.nbrOfStatesIDD(deadlockStatesIDD).longValue();
        }

        System.out.println("The number of feasiable deadlock states is: " + nbrOfDeadlockStates);

        return deadlocks;
    }

    // Backtrack deadlock states to find all boundary unsafe states from partition
    private BDD computeBoundaryUnsafeStatesWithEventPartitions(final BDD deadlocks) {

        BDD newFoundUnsafeStates = deadlocks;

        BDD unsafeStates = deadlocks.id();

        final BDD[] notUnsafeStates = new BDD[componentSize];
        final BDD[] partitionSourceStates = new BDD[componentSize];
        final BDD[] possiableUnsafeStates = new BDD[componentSize];

        for (int i = 0; i < componentSize; i++)
            partitionSourceStates[i] = partitionBDDs[i].exist(bddExAutomata.getDestVariablesVarSet());

        do {

            final BDD newFoundUnsafeTargetStates = newFoundUnsafeStates
              .replace(bddExAutomata.getSourceToDestVariablePairing());

            newFoundUnsafeStates.free();

            for (int i = 0; i < componentSize; i++) {

                final BDD partition = partitionBDDs[i];

                final BDD transToUnsafeStates = newFoundUnsafeTargetStates.and(partition);

                possiableUnsafeStates[i] = transToUnsafeStates.exist(bddExAutomata.getDestVariablesVarSet());

                possUnsafeStatesBDDs[i] = possUnsafeStatesBDDs[i].or(possiableUnsafeStates[i]);

                final BDD notPartitionUnsafeStates = possUnsafeStatesBDDs[i].not();

                notUnsafeStates[i] = partitionSourceStates[i].and(notPartitionUnsafeStates);

                unsafeStatesTargetTrans[i].orWith(transToUnsafeStates);

                notPartitionUnsafeStates.free();
            }

            newFoundUnsafeTargetStates.free();

            newFoundUnsafeStates = getZeroBDD();

            for (int j1 = 0; j1 < componentSize; j1++) {

                final BDD localNewUnStates = possiableUnsafeStates[j1];

                for (int j2 = 0; j2 < componentSize; j2++) {
                    if (j1 != j2) {
                        localNewUnStates.andWith(notUnsafeStates[j2].not());
                    }
                }

                newFoundUnsafeStates.orWith(localNewUnStates);
                if (newFoundUnsafeStates.nodeCount() > maxBDDSizePartitioning)
                    maxBDDSizePartitioning = newFoundUnsafeStates.nodeCount();
            }

            for (int j = 0; j < componentSize; j++) {

              unsafeStatesTargetTrans[j].andWith(newFoundUnsafeStates.not());

              if (unsafeStatesTargetTrans[j].nodeCount() > maxBDDSizePartitioning) {
                  maxBDDSizePartitioning = unsafeStatesTargetTrans[j].nodeCount();
              }

              notUnsafeStates[j].free();
            }

            newFoundUnsafeStates.andWith(unsafeStates.not());
            if (newFoundUnsafeStates.nodeCount() > maxBDDSizePartitioning)
                    maxBDDSizePartitioning = newFoundUnsafeStates.nodeCount();

            unsafeStates = unsafeStates.or(newFoundUnsafeStates);
            if (unsafeStates.nodeCount() > maxBDDSizePartitioning)
                    maxBDDSizePartitioning = unsafeStates.nodeCount();

        } while (!newFoundUnsafeStates.isZero());

        final BDD loadingToBoundaryUnsafeStates = unsafeStates
          .replace(bddExAutomata.getSourceToDestVariablePairing())
          .andWith(frwdLoadingTrans).andWith(unsafeStates.not());

        if (loadingToBoundaryUnsafeStates.nodeCount() > maxBDDSizePartitioning)
          maxBDDSizePartitioning = loadingToBoundaryUnsafeStates.nodeCount();

        final BDD boundaryUnsafeStates = loadingToBoundaryUnsafeStates
          .exist(bddExAutomata.getSourceVariablesVarSet())
          .replaceWith(bddExAutomata.getDestToSourceVariablePairing());

        if (boundaryUnsafeStates.nodeCount() > maxBDDSizePartitioning)
          maxBDDSizePartitioning = boundaryUnsafeStates.nodeCount();

        // clean up while collect the boundary unsafe states
        unsafeStates.free();
        loadingToBoundaryUnsafeStates.free();

        for (int i = 0; i < componentSize; i++) {

          partitionSourceStates[i].free();
          partitionBDDs[i].free();
          possUnsafeStatesBDDs[i].free();

          final BDD partitionBoundaryUnsafeStates = unsafeStatesTargetTrans[i]
            .exist(bddExAutomata.getSourceVariablesVarSet());

          unsafeStatesTargetTrans[i].free();

          boundaryUnsafeStates.orWith(partitionBoundaryUnsafeStates
                                         .replaceWith(bddExAutomata.getDestToSourceVariablePairing()));

        }

        if (boundaryUnsafeStates.nodeCount() > maxBDDSizePartitioning)
          maxBDDSizePartitioning = boundaryUnsafeStates.nodeCount();

        System.out.println("The maximal size of intermediate BDDs is "
          + maxBDDSizePartitioning);

        return boundaryUnsafeStates;
    }
}

