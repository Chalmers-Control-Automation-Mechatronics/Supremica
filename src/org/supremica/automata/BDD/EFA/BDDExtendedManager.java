package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.set.hash.TIntHashSet;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.map.hash.TIntObjectHashMap;

import java.util.List;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDFactory;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.properties.Config;

public class BDDExtendedManager extends BDDAbstractManager {

    //Variabes used for RAS models
    BDD globalLargerBDD = null;
    //The BDD representing the forward transition relation, where the dest variables are removed.
    BDD frwdTransEvents = null;
    //The BDD representing the backward transition relation
    BDD bckwdTransEvents = null;
    BDD feasibleSourceStates = null;
    BDD feasibleDestStates = null;
    BDD unreachableStates = null;

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

    /** Return a set of initial uncontrollable states. */
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



    /* Implementation for Resource Allocation Systems*/
    /** Computation of unsafe states including all minimal unsafe states.
     *
     * @return
     */
    public BDD computeUnsafeStates() {
        frwdTransEvents = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardWithEventsBDD();
        frwdTransEvents = frwdTransEvents.exist(bddExAutomata.getSourceLocationVarSet().union(bddExAutomata.getDestLocationVarSet()));
        frwdTransEvents = frwdTransEvents.and(bddExAutomata.loadEventsBDD.not());
        frwdTransEvents = frwdTransEvents.exist(bddExAutomata.getDestStatesVarSet());

        bckwdTransEvents = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getMonolithicEdgesBackwardWithEventsBDD();
        /*The following exist operator can be avoided by only doing exist once on
         * the forward transition relation and then construct the backward transition relation.
         */
        bckwdTransEvents = bckwdTransEvents.exist(bddExAutomata.getSourceLocationVarSet().union(bddExAutomata.getDestLocationVarSet()));

        //Remove the load transitions
        bckwdTransEvents = bckwdTransEvents.and(bddExAutomata.loadEventsBDD.not());

        System.err.println("Computing the feasible states...");
        frwdTransEvents = frwdTransEvents.and(getFeasibleSourceStates());
        System.err.println("Done.");

        bckwdTransEvents = bckwdTransEvents.and(getFeasibleSourceStates()).and(getFeasibleDestStates());


        BDD tauTrans = getZeroBDD();
        BDD sigmaTrans = frwdTransEvents.id();
        System.err.println("Computing the deadlock states...");
        BDD unsafeStates = getMinimalDeadlocks();
        System.err.println("Done.");


        BDD upUnsafeStates = unsafeStates.id();
        do {
//            System.err.println("computeUnsafeStates: "+(i++));
            final BDD[] elements = computeElements(upUnsafeStates);
            upUnsafeStates = elements[0].id();

//            System.err.println("Performing the larger operator...");
            final BDD newTauTrans = elements[1].or(getLarger(sigmaTrans.and(elements[1].not()), elements[1])).and(getFeasibleSourceStates());
//            System.err.println("Done.");
            sigmaTrans = sigmaTrans.and(newTauTrans.not());
            tauTrans = tauTrans.or(newTauTrans);
//            upUnsafeStates = upUnsafeStates.or(extractNewUnsafeStates(tauTrans.and(
//                    newTauTrans.exist(bddExAutomata.getEventVarSet()).not())));
            upUnsafeStates = upUnsafeStates.or(extractNewUnsafeStates(tauTrans));

            unsafeStates = unsafeStates.or(upUnsafeStates);
        } while (!upUnsafeStates.isZero());

        return unsafeStates;
    }

    /**
     *
     * @param unsafeStates
     * @return The new inevitable states and $\tau$Trans
     */
    private BDD[] computeElements(final BDD unsafeStates) {
        final BDD[] elements = new BDD[2];
        BDD upUnsafeStates = unsafeStates.id();
        BDD inevitableStates = getZeroBDD();
        BDD tauTrans = getZeroBDD();
        do {
//            System.err.println("computeElements: "+(i++));
            final BDD tauSuperBckwdTrans = upUnsafeStates.and(bckwdTransEvents);
            final BDD tauSuperFrwdTrans = tauSuperBckwdTrans.exist(bddExAutomata.getSourceStatesVarSet()).replace(
                    bddExAutomata.getDestToSourceVariablePairing());
            final BDD boundaryOtherStates = frwdTransEvents.and(tauSuperFrwdTrans.not()).exist(bddExAutomata.getEventVarSet());
//            upUnsafeStates = (tauSuperFrwdTrans.and(boundaryOtherStates.not())).exist(bddExAutomata.getEventVarSet());
            upUnsafeStates = (tauSuperFrwdTrans.exist(bddExAutomata.getEventVarSet()).and(boundaryOtherStates.not())).and(getFeasibleSourceStates());
            inevitableStates = inevitableStates.or(upUnsafeStates);
            tauTrans = tauTrans.or(boundaryOtherStates.and(tauSuperFrwdTrans));
        } while (!upUnsafeStates.isZero());

        elements[0] = inevitableStates;
        elements[1] = tauTrans;

        return elements;
    }

    /**
     *
     * @param trans
     * @return The new found unsafe states
     */
    private BDD extractNewUnsafeStates(final BDD trans) {
        return (trans.exist(bddExAutomata.getEventVarSet()).and(
                frwdTransEvents.and(trans.not()).exist(bddExAutomata.getEventVarSet()).not()));
    }

    private BDD getLarger(final BDD trans1, final BDD trans2) {
        final BDD tmpTrans1 = trans1.replace(bddExAutomata.sourceToTempVariablePairing);
        final BDD pairs = trans2.and(tmpTrans1);

        final BDD larger = (pairs.and(getGlobalLargerBDD()).exist(bddExAutomata.getSourceStatesVarSet()));
        return larger.replace(bddExAutomata.tempToSourceVariablePairing);
    }

    private BDD getGlobalLargerBDD() {
        if (globalLargerBDD == null) {
            BDD equalsBDD = getOneBDD();
            globalLargerBDD = getOneBDD();
            final List<VariableComponentProxy> stageVars = bddExAutomata.orgExAutomata.getStageVars();
            for (final VariableComponentProxy stage : stageVars) {
                final int stageIndex = bddExAutomata.theIndexMap.getVariableIndex(stage);
                final int stageDomain = bddExAutomata.orgExAutomata.getVarDomain(stage.getName());
                final BDDDomain stageSourceDomain = bddExAutomata.sourceVarDomains[stageIndex];
                final BDDDomain stageTempDomain = bddExAutomata.tempVarDomains[stageIndex];
                final SupremicaBDDBitVector stageBDDBitVectorSource = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageSourceDomain);
                final SupremicaBDDBitVector stageBDDBitVectorTemp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageTempDomain);
                BDD stageLargerBDD = getZeroBDD();
                for (int j = 0; j < stageDomain; j++) {
                    final BDD jSource = createBDD(j, bddExAutomata.getSourceVariableDomain(stageIndex));
                    final SupremicaBDDBitVector vector_j = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, stageTempDomain.varNum(), j);
                    final BDD greaterThanJBDD = stageBDDBitVectorTemp.gte(vector_j);
                    stageLargerBDD = stageLargerBDD.or(jSource.and(greaterThanJBDD));
                }
                equalsBDD = equalsBDD.and(stageBDDBitVectorSource.equ(stageBDDBitVectorTemp));
                globalLargerBDD = globalLargerBDD.and(stageLargerBDD);
            }

            globalLargerBDD = globalLargerBDD.and(equalsBDD.not());
        }

        return globalLargerBDD;
    }

    private BDD getMinimalDeadlocks() {
        final BDD feasibleTrans = frwdTransEvents.and(getFeasibleSourceStates());
        final BDD feasSourceStates = feasibleTrans.exist(bddExAutomata.getEventVarSet());
        BDD deadlocks = getFeasibleSourceStates().and(feasSourceStates.not());
        deadlocks = deadlocks.and(bddExAutomata.getInitialState().exist(bddExAutomata.getSourceLocationVarSet()).not());
//       BDD forward = ((BDDMonolithicEdges)bddExAutomata.getBDDEdges()).getMonolithicEdgesForwardWithEventsBDD();
//       forward.exist(bddExAutomata.getEventVarSet()).exist(
//                                bddExAutomata.getDestStatesVarSet()).exist(
//                                    bddExAutomata.getSourceLocationVarSet()).printDot();
        //      deadlocks.printDot();
//       getLarger(deadlocks.id(), deadlocks.id()).printDot();

        return deadlocks.and(getLarger(deadlocks.id(), deadlocks.id()).not());
    }

    private BDD getFeasibleSourceStates() {
        if (feasibleSourceStates == null) {
            final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());

            SimpleExpressionSubject feasibleEquation = null;
            try {
                feasibleEquation = (SimpleExpressionSubject) (parser.parse(FlowerEFABuilder.feasibleEquation, Operator.TYPE_BOOLEAN));
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
            feasibleDestStates = getFeasibleSourceStates().replace(bddExAutomata.getSourceToDestVariablePairing());
            return feasibleDestStates;
        }

        return feasibleDestStates;
    }

    BDD getInitialUnreachableStates(final BDD localBckwdTransEvents) {
        final BDD notInitialFeasibleStates = getFeasibleSourceStates().and(localBckwdTransEvents);
        final BDD initialFeasibleStates = getFeasibleSourceStates().and(notInitialFeasibleStates.exist(
                bddExAutomata.getEventVarSet()).exist(
                bddExAutomata.getDestStatesVarSet()).not());
        return initialFeasibleStates.and(bddExAutomata.getInitialState().exist(bddExAutomata.getSourceLocationVarSet()).not());
    }
}

