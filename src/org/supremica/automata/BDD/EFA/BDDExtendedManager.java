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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.analysis.bdd.BDDPackage;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.properties.Config;

public class BDDExtendedManager extends BDDAbstractManager {

    private static Logger logger = LogManager.getLogger(BDDExtendedManager.class);

    BDD globalLargerBDD = null;
    //The BDD representing the forward transition relation, where the destination variables are removed.
    BDD frwdTrans = null;
    //The BDD representing the backward transition relation
    BDD bckwdTransEvents = null;
    BDD frwdLoadingTrans = null;
    BDD globalOneStepLargerBDD = null;
    BDD feasibleSourceStates = null;
    BDD unreachableStates = null;
    TIntObjectHashMap<BDD> event2ParitionBDD = null;
    BDD[] partitionBDDs = null;
    BDD[] accuLocalUnsafeStatesBDDs = null;
    BDD[] accuTransToGlobalUnsafeStates = null;
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

    public int maxNbrNodes = 0;

    public BDDExtendedManager() {
        this(Config.BDD2_BDDLIBRARY.get());
    }

    public BDDExtendedManager(final BDDPackage bddpackage) {
        this(bddpackage, Config.BDD2_INITIALNODETABLESIZE.get(), Config.BDD2_CACHESIZE.get());
    }

    public BDDExtendedManager(final BDDPackage bddpackage, final int nodenum, final int cachesize) {
        if (factory == null) {
            factory = BDDFactory.init(bddpackage.getBDDPackageName(), nodenum, cachesize);
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
        if (!bddExAutomata.orgExAutomata.modelHasNoPlants() // model has both plants and specifications
                && !bddExAutomata.orgExAutomata.modelHasNoSpecs()) {
            final BDD t1 = edges.getPlantMonolithicUncontrollableEdgesForwardBDD();
            final BDD t2 = edges.getSpecMonolithicUncontrollableEdgesForwardBDD().and(t1).exist(bddExAutomata.getDestStatesVarSet());
            uncontrollableStates =  t1.and(t2.not()).exist(bddExAutomata.getDestStatesVarSet()).exist(bddExAutomata.getEventVarSet());

            if (bddExAutomata.trackPeakBDD) {
              maxNbrNodes = t1.nodeCount() > maxNbrNodes ? t1.nodeCount() : maxNbrNodes;
              maxNbrNodes = t2.nodeCount() > maxNbrNodes ? t2.nodeCount() : maxNbrNodes;
            }
        }

        if (bddExAutomata.trackPeakBDD)
          maxNbrNodes = uncontrollableStates.nodeCount() > maxNbrNodes ? uncontrollableStates.nodeCount() : maxNbrNodes;

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
        logger.debug("UncontrollableBackward entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD t_u = bddEdges.getMonolithicUncontrollableEdgesBackwardBDD();

        if (bddExAutomata.trackPeakBDD)
          maxNbrNodes = t_u.nodeCount() > maxNbrNodes ? t_u.nodeCount() : maxNbrNodes;

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
            logger.debug("ub: "+(i++));
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
            if (bddExAutomata.trackPeakBDD) {
              maxNbrNodes = newUCstates.nodeCount() > maxNbrNodes ? newUCstates.nodeCount() : maxNbrNodes;
              maxNbrNodes = Qkn.nodeCount() > maxNbrNodes ? Qkn.nodeCount() : maxNbrNodes;
            }
        } while (!Qkn.equals(Qk));

        logger.debug("UncontrollableBackward exited.");
        return Qkn;
    }

    public BDD restrictedBackward(final BDD markedStates, final BDD forb) {
        logger.debug("RestrictedBackward entered.");

        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD trans = bddEdges.getMonolithicEdgesBackwardBDD();

        if (bddExAutomata.trackPeakBDD)
          maxNbrNodes = trans.nodeCount() > maxNbrNodes ? trans.nodeCount() : maxNbrNodes;

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

        int i = 0;
        do {
            logger.debug("rb "+(i++));

            Qk = Qkn.id();
            Qm = image_preImage(Qk, trans, backwardTime);//.and(bddExAutomata.getReachableStates());

            Qkn = ((Qk.or(Qm)).and(forbidden.not()));

            if (bddExAutomata.trackPeakBDD) {
              maxNbrNodes = Qm.nodeCount() > maxNbrNodes ? Qm.nodeCount() : maxNbrNodes;
              maxNbrNodes = Qkn.nodeCount() > maxNbrNodes ? Qkn.nodeCount() : maxNbrNodes;
            }

        } while (!Qkn.equals(Qk));

        logger.debug("RestrictedBackward exited.");
        return Qkn;
    }

    public BDD restrictedForward(final BDD initialStates, final BDD forb) {
        logger.debug("RestrictedForward entered.");
        final BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD trans = bddEdges.getMonolithicEdgesForwardBDD();

        if (bddExAutomata.trackPeakBDD)
          maxNbrNodes = trans.nodeCount() > maxNbrNodes ? trans.nodeCount() : maxNbrNodes;

        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();

        final BDD forbidden = forb.id();

        BDD Qkn = initialStates.and(forbidden.not());
        BDD Qk;
        BDD Qm;

        int iteration = 0;

        do {
            logger.debug("RForward "+(iteration++) + "\t" + Qkn.nodeCount());

            Qk = Qkn.id();
            Qm = image_preImage(Qk, trans, forwardTime);

            Qkn = (Qk.or(Qm)).and(forbidden.not());

            if (bddExAutomata.trackPeakBDD) {
              maxNbrNodes = Qm.nodeCount() > maxNbrNodes ? Qm.nodeCount() : maxNbrNodes;
              maxNbrNodes = Qkn.nodeCount() > maxNbrNodes ? Qkn.nodeCount() : maxNbrNodes;
            }

        } while (!Qkn.equals(Qk));

        logger.debug("RestrictedForward exited.");
        return Qkn;
    }

    public BDD nonblockingControllable(final BDD forb, final boolean reachable) {
        logger.debug("NonblockingControllable entered.");
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
            logger.debug("nbc i: "+(i++));
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

        logger.debug("NonblockingControllable exited.");

        if (reachable) {
            return restrictedForward(bddExAutomata.getInitialState(), Qkn);
        } else {
            return Qkn.not();
        }
    }

    BDD getDisjunctiveInitiallyUncontrollableStates() {

        if (bddExAutomata.plants.isEmpty() || bddExAutomata.specs.isEmpty()) {
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

            for(final TIntIterator itr = sharedUncontrollableEvents.iterator(); itr.hasNext();)
            {
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
            logger.debug("i: "+i);
            Qk = Qkn.id();
            nextTrans = frwdTrans.and(Qk);
//            localFrwdTrans = localFrwdTrans.or(nextTrans);

            /*
             * Image operator
             */
            logger.debug("Performing the image operator...");
            nextStates = nextTrans.exist(bddExAutomata.getSourceStatesVarSet());
            Qm = bddExAutomata.destToSource(nextStates);
            Qkn = Qk.or(Qm);
            logger.debug("Image computed.");

            /*
             * Perform a local analysis (synthesis):
             *     - find all blocking and uncontrollable states starting
             *       from the localBadStates
             *     - find states that are on the edge between the locally good
             *       and bad states
             */
            logger.debug("Procedure: finding new blocking states...");
            localBadStates = getBlockingStates(frwdTrans, Qm);
            logger.debug("Procedure done.");
            localBadStatesExt = getZeroBDD();
            localUnconStates = getZeroBDD();
            j = 0;
            while(!localBadStates.equals(localBadStatesExt))
            {
                logger.debug("j: "+j);
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
                logger.debug("compute local uncon states...");
                localUnconStates = coreachability(bkwdUnconTrans, localBadStates);
                localUnconStates = localUnconStates.and(localBadStates.not());
                logger.debug("local uncon states computed.");

                localBadStates = localBadStates.or(localUnconStates);
//                localBadStates.printDot();

                logger.debug("local prune...");
                pruneTrans(frwdTrans, localBadStates);
//                localFrwdTrans = localFrwdTrans.and(bddExAutomata.sourceToDest(localBadStates).not());

//                unconStates = unconStates.or(localUnconStates);

                Qkn = Qkn.and(localBadStates.not());
                logger.debug("computing next local bad states...");
                nextBlockingStates = getBlockingStates(frwdTrans, Qkn);
                logger.debug("next local bad states comoputed.");
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
            logger.debug("unnVS: "+i);
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

    // ############################################################################################
    // ################ THE MAIN ALGORITHM 1 AND 2 (SEE THE TASE PAPER) ###########################

    // compute the boundary unsafe states with the extension of the SCT algorithm (ALGORITHM 1)
    public BDD computeBoundaryUnsafeStatesClassic() {

        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardBDD()
                .exist(bddExAutomata.getSourceLocationVarSet() // flower structure has one location
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

            iddSatCount = true; // to deal with the overflow of executing satCount method

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

        System.out.println("The maximal size of intermediate BDDs is " + maxBDDSizeClassic);

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

    // compute boundary unsafe states (some are not reachable) with ALGORITHM 2
    public BDD computeBoundaryUnsafeStatesAlternative() {

        feasibleSourceStates = getFeasibleSourceStatesBDD();

        frwdTrans = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardWithEventsBDD()
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));

        // compute the transitions that are labeled by loading events
        frwdLoadingTrans = frwdTrans.and(bddExAutomata.loadEventsBDD)
                .exist(bddExAutomata.getEventVarSet());

        final BDD deadlocks = getDeadlocks();

        BDD boundaryUnsafeStates  = computeBoundaryUnsafeStates(deadlocks);

        long nbrOfBoundUnsafeStates = 0;

        iddSatCount = true; // to get the node sizes of IDDs

        if (iddSatCount) {
            // convert the BDD into the corresponding IDD
            final IDD boundUnsafeStatesIDD = bddExAutomata
              .generateIDD(boundaryUnsafeStates, boundaryUnsafeStates);

            nbrOfBoundUnsafeStates = bddExAutomata
              .nbrOfStatesIDD(boundUnsafeStatesIDD).longValue();

            // remove the resource variables from boundary unsafe states BDD
            final BDD boundUnsafeStatesNoResourceVarBDD = boundaryUnsafeStates
              .exist(bddExAutomata.getSourceResourceVarSet());

            // convert it to the corresponding IDD
            final IDD boundUnsafeStatesNoResourceVarIDD = bddExAutomata
              .generateIDD(boundUnsafeStatesNoResourceVarBDD, boundUnsafeStatesNoResourceVarBDD);

            // print the number of nodes in the IDD
            System.out.println("The number of nodes of the IDD representing the boundary unsafe "
              + "states is " + boundUnsafeStatesNoResourceVarIDD.nbrOfNodes());

            boundUnsafeStatesNoResourceVarBDD.free();

        } else {
            nbrOfBoundUnsafeStates = (long) boundaryUnsafeStates
                    .satCount(bddExAutomata.getSourceVariablesVarSet());
        }

        System.out.println("The number of boundary states is " + nbrOfBoundUnsafeStates);

        boundaryUnsafeStates = boundaryUnsafeStates.exist(bddExAutomata.getSourceResourceVarSet());

        frwdLoadingTrans.free();
        frwdTrans.free();

        return boundaryUnsafeStates;
    }

    //#### Auxiliary methods used for computing the boundary unsafe states for ALGORITHM 2
    // compute all deadlock states
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

    // backtrack deadlock states to find all boundary unsafe states
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
    //##########################################END################################################

    //######################### ALGORITHM 3 IN THE TASE PAPER #####################################
    //#### Zhennan

    // compute the minimal boundary unsafe states (ALGORITHM 3 in the TASE paper).
    public BDD removeLargerStates (final BDD boundaryStates) {

      getGlobalStrictLargerBDD();

      final BDD boundaryTempStates = boundaryStates
        .replace(bddExAutomata.sourceToTempVariablePairing);

      final BDD largerStatePairs = globalLargerBDD.and(boundaryTempStates);
      boundaryTempStates.free();

      if (maxBDDSizeMinimization < largerStatePairs.nodeCount()) {
        maxBDDSizeMinimization = largerStatePairs.nodeCount();
      }

      final BDD largerStates = largerStatePairs.exist(bddExAutomata.tempVariablesVarSet);
      largerStatePairs.free();

      if (maxBDDSizeMinimization < largerStates.nodeCount()) {
        maxBDDSizeMinimization = largerStates.nodeCount();
      }

      minimalBoundaryUnsafeStates = boundaryStates.andWith(largerStates.not());
      largerStates.free();

      globalLargerBDD.free();

      System.out.println("The maximal size of BDDs during the minimization is: "
        + maxBDDSizeMinimization);

      iddSatCount = true; // to get the node size of IDDs

      if (iddSatCount) {
        computeMinStatesIDDSatCountDenominator();
        final IDD minBoundUnsafeStatesIDD = bddExAutomata.generateIDD(minimalBoundaryUnsafeStates,
                                                                      minimalBoundaryUnsafeStates);
        nbrMinBoundUnsafeStates = bddExAutomata.nbrOfStatesIDD(minBoundUnsafeStatesIDD).longValue()
            / minStatesIDDSatCountDenominator;

        // print the number of nodes in the IDD representing the minimal boundary unsafe states
        System.out.println("The number of nodes of the IDD representing the minimal boundary "
          + "unsafe states is " + minBoundUnsafeStatesIDD.nbrOfNodes());

      } else {
        nbrMinBoundUnsafeStates = (long) minimalBoundaryUnsafeStates
          .satCount(bddExAutomata.getSourceStagesVarSet());
      }

      System.out.println("The number of minimal boundary unsafe states is: "
                       + nbrMinBoundUnsafeStates);

      return minimalBoundaryUnsafeStates;
    }

    //#### Auxiliary methods used for ALGORITHM 3
    private BDD getGlobalStrictLargerBDD() {

      final BDD equalsBDD = getOneBDD();
      globalLargerBDD = getOneBDD();

      final List<VariableComponentProxy> stageVars = bddExAutomata.orgExAutomata.getStageVars();

      for (final VariableComponentProxy stage: stageVars)
      {
        final int stageIndex = bddExAutomata.theIndexMap.getVariableIndex(stage);
        final int stageDomain = bddExAutomata.orgExAutomata.getVarDomain(stage.getName());
        final BDDDomain stageSourceDomain = bddExAutomata.sourceVarDomains[stageIndex];
        final BDDDomain stageTempDomain = bddExAutomata.tempVarDomains[stageIndex];

        final SupremicaBDDBitVector stageBDDBitVectorSource =
          createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                              bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                              stageSourceDomain);

        final SupremicaBDDBitVector stageBDDBitVectorTemp =
          createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                              bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                              stageTempDomain);

        final BDD stageLargerBDD = getZeroBDD();

        for (int j = 0; j < stageDomain; j++) {

          final BDD jTemp = createBDD(j, bddExAutomata.getTempVariableDomain(stageIndex));
          final SupremicaBDDBitVector vector_j =
            createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, stageTempDomain.varNum(), j);
          final BDD greaterThanJBDD = stageBDDBitVectorSource.gte(vector_j);
          stageLargerBDD.orWith(jTemp.andWith(greaterThanJBDD));
          vector_j.free();
        }

        equalsBDD.andWith(stageBDDBitVectorTemp.equ(stageBDDBitVectorSource));
        globalLargerBDD.andWith(stageLargerBDD);
        stageBDDBitVectorSource.free();
        stageBDDBitVectorTemp.free();
      }

      globalLargerBDD.andWith(equalsBDD.not());
      equalsBDD.free();
      return globalLargerBDD;
    }

    // build the BDD of feasible states from the resource invariants
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
                logger.error(pe);
            }

            feasibleSourceStates = getOneBDD();
            for (final SimpleExpressionProxy fe: feasibleEquationExpressions) {
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
    //###########################################END###############################################


    //################# THE ALGORITHM USING PARTITIONING TECHNIQUES (ALGORITHM D)##################
    //#### Zhennan

    // compute the boundary unsafe states using partitioning techniques
    public BDD computeBoundaryUnsafeStatesEventPartitioning() {

        setupPartitioningBDDs();

        logger.debug("Computing the deadlock states with the event-based "
                + "partitioning approach.");

        final BDD deadlocks = getDeadlocksWithPartitions();

        final BDD boundaryUnsafeStates = computeBoundaryUnsafeStatesWithEventPartitions(deadlocks);

        long nbrOfBoundUnsafeStates = 0L;

        iddSatCount = true; // to get the number of nodes for IDD

        if (iddSatCount) {
            final IDD boundUnsafeStatesIDD = bddExAutomata
                    .generateIDD(boundaryUnsafeStates, boundaryUnsafeStates);

            nbrOfBoundUnsafeStates = bddExAutomata
                    .nbrOfStatesIDD(boundUnsafeStatesIDD).longValue();

            // remove the resource variables from boundary unsafe states BDD
            final BDD boundUnsafeStatesNoResourceVarBDD = boundaryUnsafeStates
              .exist(bddExAutomata.getSourceResourceVarSet());

            // convert it to the corresponding IDD
            final IDD boundUnsafeStatesNoResourceVarIDD = bddExAutomata
              .generateIDD(boundUnsafeStatesNoResourceVarBDD, boundUnsafeStatesNoResourceVarBDD);

            // print the number of nodes in the IDD
            System.out.println("The number of nodes of the IDD representing the boundary unsafe "
              + "states is " + boundUnsafeStatesNoResourceVarIDD.nbrOfNodes());

            boundUnsafeStatesNoResourceVarBDD.free();

        } else {
            nbrOfBoundUnsafeStates = (long) boundaryUnsafeStates
                    .satCount(bddExAutomata.getSourceVariablesVarSet());
        }

        System.out.println("The number of boundary unsafe states is " + nbrOfBoundUnsafeStates);

        final BDD boundaryUnStatesWithoutResourceVar = boundaryUnsafeStates
          .exist(bddExAutomata.getSourceResourceVarSet());

        // clean up
        boundaryUnsafeStates.free();

        return boundaryUnStatesWithoutResourceVar;
    }

    //#### Auxiliary methods used for ALGORTIHM D

    // get the event partial transition relation BDD
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
            public boolean execute(final int eventIndex, final BDD partition) {

                final String eventName = bddExAutomata.theIndexMap
                        .getEventAt(eventIndex).getName();

                final BDD partitionNoLocation = partition.exist(locationVars);
                partition.free();

                if (!eventName.contains(FlowerEFABuilder.LOAD_EVENT_PREFIX)) {

                    partitionBDDs[eventIndex] = partitionNoLocation;

                } else {
                    partitionBDDs[eventIndex] = getZeroBDD();
                    frwdLoadingTrans.orWith(partitionNoLocation.id());
                }
                return true;
            }
        });

        accuLocalUnsafeStatesBDDs = new BDD[componentSize];
        accuTransToGlobalUnsafeStates = new BDD[componentSize];

        for (int i = 0; i < componentSize; i++) {
            accuLocalUnsafeStatesBDDs[i] = getZeroBDD();
            accuTransToGlobalUnsafeStates[i] = getZeroBDD();
        }

        feasibleSourceStates = getFeasibleSourceStatesBDD();
        getGlobalStrictLargerBDD();
    }

    // compute all deadlock states using partitioning techniques
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

        deadlocks.andWith(feasibleSourceStates.id()).andWith(initv.not());

        // cleanup
        nonDeadlocks.free();
        initv.free();

        // compute the number of deadlock states
        long nbrOfDeadlockStates = (long) deadlocks.satCount(bddExAutomata.getSourceVariablesVarSet());

        if (nbrOfDeadlockStates <= 1L) {
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

        final BDD[] partitionSourceStates = new BDD[componentSize];
        final BDD[] notUnsafeStates = new BDD[componentSize];
        final BDD[] currLocalUnsafeStates = new BDD[componentSize];

        for (int i = 0; i < componentSize; i++)
            partitionSourceStates[i] = partitionBDDs[i].exist(bddExAutomata.getDestVariablesVarSet());

        do {

            final BDD newFoundUnsafeTargetStates = newFoundUnsafeStates
              .replaceWith(bddExAutomata.getSourceToDestVariablePairing());

            for (int i = 0; i < componentSize; i++) {

                final BDD partition = partitionBDDs[i];

                final BDD transToUnsafeStates = newFoundUnsafeTargetStates.and(partition);

                currLocalUnsafeStates[i] = transToUnsafeStates.exist(bddExAutomata.getDestVariablesVarSet());

                accuLocalUnsafeStatesBDDs[i] = accuLocalUnsafeStatesBDDs[i].or(currLocalUnsafeStates[i]);

                if (accuLocalUnsafeStatesBDDs[i].nodeCount() > maxBDDSizePartitioning)
                  maxBDDSizePartitioning = accuLocalUnsafeStatesBDDs[i].nodeCount();

                final BDD notPartitionUnsafeStates = accuLocalUnsafeStatesBDDs[i].not();

                notUnsafeStates[i] = partitionSourceStates[i].and(notPartitionUnsafeStates);

                notPartitionUnsafeStates.free();

                accuTransToGlobalUnsafeStates[i].orWith(transToUnsafeStates);

                if (accuTransToGlobalUnsafeStates[i].nodeCount() > maxBDDSizePartitioning)
                  maxBDDSizePartitioning = accuTransToGlobalUnsafeStates[i].nodeCount();
            }

            newFoundUnsafeTargetStates.free();

            newFoundUnsafeStates = getZeroBDD();

            for (int j1 = 0; j1 < componentSize; j1++) {

                final BDD currLocalUnStates = currLocalUnsafeStates[j1];

                for (int j2 = 0; j2 < componentSize; j2++) {
                    if (j1 != j2) {
                        currLocalUnStates.andWith(notUnsafeStates[j2].not());
                    }
                }

                newFoundUnsafeStates.orWith(currLocalUnStates);
                if (newFoundUnsafeStates.nodeCount() > maxBDDSizePartitioning)
                    maxBDDSizePartitioning = newFoundUnsafeStates.nodeCount();
            }

            for (int j = 0; j < componentSize; j++) {

              accuTransToGlobalUnsafeStates[j].andWith(newFoundUnsafeStates.not());

              if (accuTransToGlobalUnsafeStates[j].nodeCount() > maxBDDSizePartitioning) {
                  maxBDDSizePartitioning = accuTransToGlobalUnsafeStates[j].nodeCount();
              }

              notUnsafeStates[j].free();
            }

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
          accuLocalUnsafeStatesBDDs[i].free();

          final BDD partitionBoundaryUnsafeStates = accuTransToGlobalUnsafeStates[i]
            .exist(bddExAutomata.getSourceVariablesVarSet());

          accuTransToGlobalUnsafeStates[i].free();

          boundaryUnsafeStates.orWith(partitionBoundaryUnsafeStates
                                         .replaceWith(bddExAutomata.getDestToSourceVariablePairing()));

          if (boundaryUnsafeStates.nodeCount() > maxBDDSizePartitioning)
            maxBDDSizePartitioning = boundaryUnsafeStates.nodeCount();
        }

        if (boundaryUnsafeStates.nodeCount() > maxBDDSizePartitioning)
          maxBDDSizePartitioning = boundaryUnsafeStates.nodeCount();

        System.out.println("The maximal size of intermediate BDDs is "
          + maxBDDSizePartitioning);

        return boundaryUnsafeStates;
    }
    //######################################END####################################################
}

