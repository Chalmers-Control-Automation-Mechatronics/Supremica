package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi
 */
import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;

public class BDDMonolithicEdges
        implements BDDEdges {

    private final BDDExtendedAutomata bddExAutomata;
    private final BDDExtendedManager manager;
    private BDD edgesForwardBDD = null;
    private BDD forcibleEdgesForwardBDD = null;

//    public BDD forcibleEdgesForwardBDDCopy = null;

    private BDD edgesForwardWithoutDestBDD = null;
    private BDD edgesBackwardBDD = null;
    private BDD edgesForwardWithEventsBDD = null;
    private BDD edgesBackwardWithEventsBDD = null;
    private BDD uncontrollableEdgesForwardBDD = null;
    private BDD uncontrollableEdgesBackwardBDD = null;
    private BDD plantUncontrollableEdgesForwardBDD = null;
    private BDD specUncontrollableEdgesForwardBDD = null;
    private BDD forwardClocksWithTheSameRate = null;
    private BDD backwardClocksWithTheSameRate = null;
    private BDD oneStepTickBDD = null;
    private BDD forcibleSpecEdgesForwardBDD = null;

    private BDD plantEdgesForwardBDD = null;
    private BDD specEdgesForwardBDD = null;

    private BDD deadlockStatesBDD = null;

    private BDD currentFrwdGoodTransBDD = null;


//    private BDD plantUncontrollableEdgesBackwardBDD = null;
//    private BDD specUncontrollableEdgesBackwardBDD = null;
    /** Creates a new instance of BDDMonolithicEdges */
    public BDDMonolithicEdges(final BDDExtendedAutomata bddExAutomata) {
        this.bddExAutomata = bddExAutomata;
        manager = bddExAutomata.getManager();
        edgesForwardBDD = manager.getOneBDD();
        edgesForwardWithoutDestBDD = manager.getOneBDD();
        edgesBackwardBDD = manager.getOneBDD();
        uncontrollableEdgesForwardBDD = manager.getZeroBDD();
        uncontrollableEdgesBackwardBDD = manager.getZeroBDD();
        plantUncontrollableEdgesForwardBDD = manager.getOneBDD();
        specUncontrollableEdgesForwardBDD = manager.getOneBDD();
        forwardClocksWithTheSameRate = manager.getOneBDD();
        backwardClocksWithTheSameRate = manager.getOneBDD();
        oneStepTickBDD = manager.getOneBDD();
        forcibleEdgesForwardBDD = manager.getZeroBDD();
        forcibleSpecEdgesForwardBDD = manager.getZeroBDD();

        plantEdgesForwardBDD = manager.getOneBDD();
        specEdgesForwardBDD = manager.getOneBDD();


//        edgesBackwardBDD = manager.getOneBDD();
//        uncontrollableEdgesBackwardBDD = manager.getZeroBDD();


//        edgesBackwardBDD = manager.getOneBDD();
//        uncontrollableEdgesBackwardBDD = manager.getZeroBDD();
//        plantUncontrollableEdgesBackwardBDD = manager.getZeroBDD();
//        specUncontrollableEdgesBackwardBDD = manager.getZeroBDD();

        System.err.println("Start generating BDDs for forward transitions...");

        for (final BDDExtendedAutomaton currAutomaton : bddExAutomata) {
//            System.out.println(currAutomaton.getExAutomaton().getName());
//            if(currAutomaton.getExAutomaton().getName().equals("P1"))
//                currAutomaton.getEdgeForwardBDD().printDot();
            edgesForwardBDD = edgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());

            //Used when the input model is a Resource Allocation System
            edgesForwardWithoutDestBDD = edgesForwardWithoutDestBDD.and(currAutomaton.getEdgeForwardWithoutDestBDD());

            if (currAutomaton.getExAutomaton().isSpecification()) {

                specEdgesForwardBDD = specEdgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());
            } else {
                plantEdgesForwardBDD = plantEdgesForwardBDD.and(currAutomaton.getEdgeForwardBDD());
            }
        }
        if (bddExAutomata.getExtendedAutomata().modelHasNoPlants()) {
            plantUncontrollableEdgesForwardBDD = manager.getZeroBDD();
        }

        if (bddExAutomata.getExtendedAutomata().modelHasNoSpecs()) {
            specUncontrollableEdgesForwardBDD = manager.getZeroBDD();
        }

        System.err.println("Synchronizing actions...");
        final BDD actionsBDD = synchronizedActions(bddExAutomata.forwardTransWhereVisUpdated, bddExAutomata.forwardTransAndNextValsForV);
        System.err.println("Done.");


        edgesForwardBDD = edgesForwardBDD.and(actionsBDD);

//        edgesForwardBDD.printDot();

        specEdgesForwardBDD = specEdgesForwardBDD.and(actionsBDD);
        plantEdgesForwardBDD = plantEdgesForwardBDD.and(actionsBDD);



        specUncontrollableEdgesForwardBDD = specEdgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
        plantUncontrollableEdgesForwardBDD = plantEdgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);



//        System.out.println("number of nodes in the forward BDD: "+edgesForwardBDD.nodeCount());
        edgesBackwardBDD = bddExAutomata.sourceTooTdest(edgesForwardBDD);
        //edgesBackwardBDD.printDot();
        if (!bddExAutomata.orgExAutomata.getClocks().isEmpty()) {
            final BDD[] clocksWithTheSameRate = computeClocksExtension();
            System.err.println("Clock extenstion computed.");
            forwardClocksWithTheSameRate = saturateForwardClocks(clocksWithTheSameRate[0]);
            backwardClocksWithTheSameRate = clocksWithTheSameRate[1];
            oneStepTickBDD = saturateForwardClocks(clocksWithTheSameRate[2]);
//            BDD c1 = manager.createBDD((2), bddExAutomata.destVarDomains[0]);
//            BDD c2 = manager.createBDD((2), bddExAutomata.destVarDomains[1]);
//            BDD c3 = manager.createBDD((2), bddExAutomata.destVarDomains[2]);
//            (backwardClocksWithTheSameRate.and(c1).and(c2).and(c3)).printDot();

            final BDD sourceInvariant = bddExAutomata.getSourceLocationInvariants();
            final BDD destInvariant = bddExAutomata.getDestLocationInvariants();

//            edgesForwardBDD = edgesForwardBDD.and(forwardClocksWithTheSameRate);
//            edgesForwardBDD = edgesForwardBDD.exist(bddExAutomata.getDestClockVarSet());
//            edgesForwardBDD = edgesForwardBDD.replace(bddExAutomata.tempClock1ToDestClockPairing).exist(bddExAutomata.tempClock1Varset);
            edgesForwardBDD = edgesForwardBDD.and(sourceInvariant);
            edgesForwardBDD = edgesForwardBDD.and(destInvariant);

//            edgesBackwardBDD = edgesBackwardBDD.and(backwardClocksWithTheSameRate);
//            edgesBackwardBDD = edgesBackwardBDD.exist(bddExAutomata.getDestClockVarSet());
//            edgesBackwardBDD = edgesBackwardBDD.replace(bddExAutomata.tempClock1ToDestClockPairing).exist(bddExAutomata.tempClock1Varset);
            edgesBackwardBDD = edgesBackwardBDD.and(sourceInvariant);
            edgesBackwardBDD = edgesBackwardBDD.and(destInvariant);

            specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.and(sourceInvariant);
            plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.and(sourceInvariant);

            System.err.println("Timing stuffs created.");

        }


        //edgesBackwardBDD.printDot();
        //edgesForwardBDD.printDot();
        uncontrollableEdgesForwardBDD = edgesForwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
        uncontrollableEdgesForwardBDD = uncontrollableEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());

        uncontrollableEdgesBackwardBDD = edgesBackwardBDD.and(bddExAutomata.uncontrollableEventsBDD);
        uncontrollableEdgesBackwardBDD = uncontrollableEdgesBackwardBDD.exist(bddExAutomata.getEventVarSet());

        forcibleEdgesForwardBDD = edgesForwardBDD.and(bddExAutomata.forcibleEventsBDD);
        forcibleSpecEdgesForwardBDD = specEdgesForwardBDD.and(bddExAutomata.forcibleEventsBDD);
//        forcibleEdgesForwardBDD.printDot();
        forcibleSpecEdgesForwardBDD = forcibleSpecEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());
        forcibleEdgesForwardBDD = forcibleEdgesForwardBDD.exist(bddExAutomata.getEventVarSet());

//        if(!uncontrollableEdgesForwardBDD.isZero())
//        {
//            uncontrollableEdgesBackwardBDD = sourceTooTdest(uncontrollableEdgesForwardBDD);
//        }

//        edgesBackwardBDD = sourceTooTdest(edgesForwardBDD);

        edgesForwardWithEventsBDD = edgesForwardBDD.id();

        edgesBackwardWithEventsBDD = edgesBackwardBDD.id();

        edgesForwardBDD = edgesForwardBDD.exist(bddExAutomata.getEventVarSet());
        currentFrwdGoodTransBDD = edgesForwardBDD.id();
        System.err.println("BDD represeting forward edges without events is created.");
        System.err.println("The number of nodes in the forward edge BDD: " + edgesForwardBDD.nodeCount());
        edgesBackwardBDD = edgesBackwardBDD.exist(bddExAutomata.getEventVarSet());
        System.err.println("BDD represeting backward edges without events is created.");
        System.err.println("The number of nodes in the backward edge BDD: " + edgesBackwardBDD.nodeCount());
    }

    private BDD synchronizedActions(final BDD[][] inTransWhereVisUpdated, final BDD[][] inTransAndNextValsForV) {
        final BDD[] autTransWhereVisUpdated = inTransWhereVisUpdated[0];
        final BDD[] autTransAndNextValsForV = inTransAndNextValsForV[0];

        if (bddExAutomata.orgExAutomata.size() == 1) {
            for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);

                final BDD noneUpdateVar = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));
                autTransAndNextValsForV[varIndex] = autTransWhereVisUpdated[varIndex].ite(autTransAndNextValsForV[varIndex], noneUpdateVar);
            }
        }

        for (int i = 1; i < (bddExAutomata.orgExAutomata.size()); i++) {
            final BDD[] currAutTransWhereVisUpdated = inTransWhereVisUpdated[i];
            final BDD[] currAutTransAndNextValsForV = inTransAndNextValsForV[i];

            for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
                final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
                final BDD tUpdate = autTransWhereVisUpdated[varIndex];
                final BDD ctUpdate = currAutTransWhereVisUpdated[varIndex];
                final BDD tNextVal = autTransAndNextValsForV[varIndex];
                final BDD ctNextVal = currAutTransAndNextValsForV[varIndex];

                final BDD firstUpdated = tUpdate.and(ctUpdate.not());
                final BDD nextTransAndNextValsForV_01 = firstUpdated.and(tNextVal);

                final BDD secondUpdated = tUpdate.not().and(ctUpdate);
                final BDD nextTransAndNextValsForV_10 = secondUpdated.and(ctNextVal);

                final BDD bothUpdated = ctNextVal.and(tNextVal);

                final BDD conflicting = (bothUpdated.exist(bddExAutomata.getDestVariablesVarSet().union(bddExAutomata.getDestClockVarSet()))).not();
                final BDD noneUpdatedOrConfliction = (ctUpdate.and(tUpdate).and(conflicting)).or(ctUpdate.not().and(tUpdate.not()));
                //If you want a transition to be prevented when there is a conflicting situation, then "uncomment" the below line and "comment" the two line above.
                //BDD noneUpdatedOrConfliction = ctUpdate.not().and(tUpdate.not());
                final BDD noChange = bddExAutomata.getBDDBitVecTarget(varIndex).equ(bddExAutomata.getBDDBitVecSource(varIndex));

                final BDD noneUpdateVar = noneUpdatedOrConfliction.and(noChange);

                autTransWhereVisUpdated[varIndex] = tUpdate.or(ctUpdate);
                autTransAndNextValsForV[varIndex] = nextTransAndNextValsForV_01.or(nextTransAndNextValsForV_10).or(bothUpdated).or(noneUpdateVar);
            }
        }

        final BDD newEdgesBDD = manager.getOneBDD();
        for (final VariableComponentProxy var : bddExAutomata.orgExAutomata.getVars()) {
            final int varIndex = bddExAutomata.theIndexMap.getVariableIndex(var);
            if (!bddExAutomata.orgExAutomata.getClocks().contains(var)) {
                autTransAndNextValsForV[varIndex] = autTransAndNextValsForV[varIndex].and(bddExAutomata.getBDDBitVecSource(varIndex).lte(bddExAutomata.getMaxBDDBitVecOf(varIndex)));
                autTransAndNextValsForV[varIndex] = autTransAndNextValsForV[varIndex].and(bddExAutomata.getBDDBitVecTarget(varIndex).lte(bddExAutomata.getMaxBDDBitVecOf(varIndex)));
                autTransAndNextValsForV[varIndex] = autTransAndNextValsForV[varIndex].and(bddExAutomata.getBDDBitVecSource(varIndex).gte(bddExAutomata.getMinBDDBitVecOf(varIndex)));
                autTransAndNextValsForV[varIndex] = autTransAndNextValsForV[varIndex].and(bddExAutomata.getBDDBitVecTarget(varIndex).gte(bddExAutomata.getMinBDDBitVecOf(varIndex)));
            }

            newEdgesBDD.andWith(autTransAndNextValsForV[varIndex]);
        }
        return newEdgesBDD;
    }

    @SuppressWarnings("unused")
    private void computeDeadlocks()
    {
        System.err.println("Compute deadlocks...");
        final BDD notDeadlocks = edgesForwardBDD.exist(bddExAutomata.getDestStatesVarSet());
        deadlockStatesBDD = notDeadlocks.not();
        System.err.println("Deadlocks computed.");
    }

    public BDD getDeadlockStatesBDD()
    {
        return deadlockStatesBDD;
    }

    public void pruneCurrentGoodFrwdTransBDD(final BDD badStates)
    {
        BDD targetBadStates = badStates.replace(bddExAutomata.getSourceToDestLocationPairing());
        targetBadStates = targetBadStates.replace(bddExAutomata.getSourceToDestVariablePairing());
        currentFrwdGoodTransBDD = currentFrwdGoodTransBDD.and(targetBadStates.not());
    }

    public BDD getCurrentGoodFrwdTransBDD()
    {
        return currentFrwdGoodTransBDD;
    }

    public void makeTheEdgesReachable()
    {
        System.err.println("Start making the edges reachable...");

        edgesForwardBDD = edgesForwardBDD.and(bddExAutomata.getReachableStates());
        System.err.println("1");
//        edgesForwardWithEventsBDD = edgesForwardWithEventsBDD.and(bddExAutomata.getReachableStates());
        forcibleEdgesForwardBDD = forcibleEdgesForwardBDD.and(bddExAutomata.getReachableStates());
//        forcibleEdgesForwardBDDCopy = forcibleEdgesForwardBDD.id();
        System.err.println("2");
//        plantUncontrollableEdgesForwardBDD = plantUncontrollableEdgesForwardBDD.and(bddExAutomata.getReachableStates());
//        specUncontrollableEdgesForwardBDD = specUncontrollableEdgesForwardBDD.and(bddExAutomata.getReachableStates());

        BDD destReachableStates = bddExAutomata.getReachableStates().replace(bddExAutomata.getSourceToDestLocationPairing());
        destReachableStates = destReachableStates.replace(bddExAutomata.getSourceToDestVariablePairing());
        edgesBackwardBDD = edgesBackwardBDD.and(destReachableStates);
        System.err.println("3");

//        edgesBackwardWithEventsBDD = edgesBackwardWithEventsBDD.and(destReachableStates);
        uncontrollableEdgesBackwardBDD = uncontrollableEdgesBackwardBDD.and(destReachableStates);

        System.err.println("The edges only including reachable states are computed.");
    }

//    private BDD synchronizeForwardClocks()
//    {
//        BDD newEdgesBDD = manager.getOneBDD();
//
//        for(VariableComponentProxy gclock:bddExAutomata.orgExAutomata.getClocks())
//        {
//            int gclockIndex = bddExAutomata.theIndexMap.getVariableIndex(gclock);
//
//            BDD otherClocks1 = manager.getOneBDD();
//            BDD otherClocks2 = manager.getOneBDD();
//
//            for(VariableComponentProxy clock:bddExAutomata.orgExAutomata.getClocks())
//            {
//                int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clock);
//                BDD clockGreaterThanVarTarget = bddExAutomata.getBDDBitVecTarget(clockIndex).gte(bddExAutomata.getBDDBitVecTarget(gclockIndex));
//                BDD varGreaterThanClockTarget = bddExAutomata.getBDDBitVecTarget(gclockIndex).gte(bddExAutomata.getBDDBitVecTarget(clockIndex));
//                BDD clockGreaterThanVar = bddExAutomata.getBDDBitVecTarget(clockIndex).gte(bddExAutomata.getBDDBitVecTarget(gclockIndex)).and(
//                        bddExAutomata.getBDDBitVecSource(clockIndex).gte(bddExAutomata.getBDDBitVecSource(gclockIndex)));
//                BDD varGreaterThanClock = bddExAutomata.getBDDBitVecTarget(gclockIndex).gte(bddExAutomata.getBDDBitVecTarget(clockIndex)).and(
//                        bddExAutomata.getBDDBitVecSource(gclockIndex).gte(bddExAutomata.getBDDBitVecSource(clockIndex)));
//
////                bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
////                        bddExAutomata.getBDDBitVecSource(clockIndex)).printDot();
//                BDD currClock1 = bddExAutomata.allForwardTransWhereVisUpdated[clockIndex].ite(
//                        bddExAutomata.getBDDBitVecTarget(gclockIndex).equ(bddExAutomata.getBDDBitVecTarget(clockIndex)),
//                        bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
//                        bddExAutomata.getBDDBitVecSource(clockIndex)).and(clockGreaterThanVarTarget));
//
//                otherClocks1 = otherClocks1.and(currClock1);
//
//                BDD currClock2 = bddExAutomata.allForwardTransWhereVisUpdated[clockIndex].ite(
//                        bddExAutomata.getBDDBitVecTarget(gclockIndex).sub(bddExAutomata.getBDDBitVecTarget(clockIndex)).equ(
//                        bddExAutomata.getBDDBitVecSource(gclockIndex)).and(varGreaterThanClockTarget),
//                        bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
//                        bddExAutomata.getBDDBitVecSource(clockIndex).sub(bddExAutomata.getBDDBitVecSource(gclockIndex))).and(clockGreaterThanVar).or
//                        (bddExAutomata.getBDDBitVecTarget(gclockIndex).sub(bddExAutomata.getBDDBitVecTarget(clockIndex)).equ(
//                        bddExAutomata.getBDDBitVecSource(gclockIndex).sub(bddExAutomata.getBDDBitVecSource(clockIndex))).and(varGreaterThanClock)));
//                otherClocks2 = otherClocks2.and(currClock2);
//            }
//
//            BDD gClockBDD  = bddExAutomata.allForwardTransWhereVisUpdated[gclockIndex].ite(otherClocks1, otherClocks2);
//
//            //fit to domain
//            gClockBDD.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//            gClockBDD.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//
//            newEdgesBDD = newEdgesBDD.and(gClockBDD);
//        }
//
//        return newEdgesBDD;
//    }
//    BDD synchronizeBackwardClocks()
//    {
//        BDD newEdgesBDD = manager.getOneBDD();
//
//        int i = 0;
//        for(VariableComponentProxy gclock:bddExAutomata.orgExAutomata.getClocks())
//        {
//            if(i++ > 0)
//                continue;
//
//            System.out.println("name: "+gclock.getName());
//
//            int gclockIndex = bddExAutomata.theIndexMap.getVariableIndex(gclock);
//
//            BDD transSourceGClock = bddExAutomata.allBackwardTransWhereVisUpdated[gclockIndex];
//
//            BDD gClockBDD = manager.getOneBDD();
//
//            for(VariableComponentProxy clock:bddExAutomata.orgExAutomata.getClocks())
//            {
//                if(!clock.equals(gclock))
//                {
//                    int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clock);
//
//                    BDD transSourceClock = bddExAutomata.allBackwardTransWhereVisUpdated[clockIndex];
//
//                    BDD clockTargetLessThanSource = bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getBDDBitVecSource(clockIndex));
//                    BDD gclockTargetLessThanSource = bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getBDDBitVecSource(gclockIndex));
//                    BDD clockGreaterThanGclock = bddExAutomata.getBDDBitVecTarget(clockIndex).gte(bddExAutomata.getBDDBitVecTarget(gclockIndex)).and(
//                            bddExAutomata.getBDDBitVecSource(clockIndex).gte(bddExAutomata.getBDDBitVecSource(gclockIndex)));
//                    BDD gclockGreaterThanClock = bddExAutomata.getBDDBitVecTarget(gclockIndex).gte(bddExAutomata.getBDDBitVecTarget(clockIndex)).and(
//                            bddExAutomata.getBDDBitVecSource(gclockIndex).gte(bddExAutomata.getBDDBitVecSource(clockIndex)));
//
////                   BDD gclockZero = manager.createBDD(bddExAutomata.theIndexMap.getIndexOfVal("0"), bddExAutomata.getSourceClockDomain(gclockIndex));
////                   BDD clockZero = manager.createBDD(bddExAutomata.theIndexMap.getIndexOfVal("0"), bddExAutomata.getSourceClockDomain(clockIndex));
//
//                    BDD noneResetTrans = transSourceGClock.not().and(transSourceClock.not());
//                    BDD noneReset1 = noneResetTrans.and(
//                            bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(clockIndex).sub(bddExAutomata.getBDDBitVecSource(gclockIndex)))).and(
//                            clockGreaterThanGclock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    noneReset1.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    noneReset1.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    noneReset1.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    noneReset1.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//
//                    BDD noneReset2 = noneResetTrans.and(
//                            bddExAutomata.getBDDBitVecTarget(gclockIndex).sub(bddExAutomata.getBDDBitVecTarget(clockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(gclockIndex).sub(bddExAutomata.getBDDBitVecSource(clockIndex)))).and(
//                            gclockGreaterThanClock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    noneReset2.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    noneReset2.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    noneReset2.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    noneReset2.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//
//                    BDD noneReset =  noneReset1.or(noneReset2);
//
//                    noneReset.printDot();
//
//
//
//                    BDD gclockReset1 = transSourceGClock.and(transSourceClock.not()).and(
//                            bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(clockIndex).sub(bddExAutomata.getBDDBitVecSource(gclockIndex)))).and(
//                            clockGreaterThanGclock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    gclockReset1.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    gclockReset1.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    gclockReset1.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    gclockReset1.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//                    gclockReset1 = gclockReset1.exist(bddExAutomata.getSourceVariableDomain(gclockIndex).set());
//
//
//                    BDD gclockReset2 = transSourceGClock.and(transSourceClock.not()).and(
//                            bddExAutomata.getBDDBitVecTarget(gclockIndex).sub(bddExAutomata.getBDDBitVecTarget(clockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(gclockIndex).sub(bddExAutomata.getBDDBitVecSource(clockIndex)))).and(
//                            gclockGreaterThanClock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    gclockReset2.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    gclockReset2.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    gclockReset2.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    gclockReset2.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//                    gclockReset2 = gclockReset2.exist(bddExAutomata.getSourceVariableDomain(gclockIndex).set());
//
//
//                    BDD gclockReset =  gclockReset1.or(gclockReset2);
//
//
//                    BDD clockReset1 = transSourceGClock.not().and(transSourceClock).and(
//                            bddExAutomata.getBDDBitVecTarget(gclockIndex).sub(bddExAutomata.getBDDBitVecTarget(clockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(gclockIndex).sub(bddExAutomata.getBDDBitVecSource(clockIndex)))).and(
//                            gclockGreaterThanClock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    clockReset1.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    clockReset1.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    clockReset1.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    clockReset1.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//                    clockReset1 = clockReset1.exist(bddExAutomata.getSourceVariableDomain(clockIndex).set());
//
//                    BDD clockReset2 = transSourceGClock.not().and(transSourceClock).and(
//                            bddExAutomata.getBDDBitVecTarget(clockIndex).sub(bddExAutomata.getBDDBitVecTarget(gclockIndex)).equ(
//                            bddExAutomata.getBDDBitVecSource(clockIndex).sub(bddExAutomata.getBDDBitVecSource(gclockIndex)))).and(
//                            clockGreaterThanGclock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//
//                    clockReset2.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    clockReset2.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    clockReset2.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    clockReset2.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//                    clockReset2 = clockReset2.exist(bddExAutomata.getSourceVariableDomain(clockIndex).set());
//
//
//                    BDD clockReset =  clockReset1.or(clockReset2);
//
//                    BDD bothReset =  transSourceGClock.and(transSourceClock).and(clockTargetLessThanSource).and(gclockTargetLessThanSource);
//                    bothReset.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    bothReset.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//                    bothReset.andWith(bddExAutomata.getBDDBitVecSource(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//                    bothReset.andWith(bddExAutomata.getBDDBitVecTarget(clockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(clockIndex)));
//
//                    bothReset = bothReset.exist(bddExAutomata.getSourceVariableDomain(clockIndex).set()).exist(
//                            bddExAutomata.getSourceVariableDomain(gclockIndex).set());
//
//
//                    gClockBDD = gClockBDD.and(noneReset.or(gclockReset).or(clockReset).or(bothReset));
//                }
//            }
//
//            //fit to domain
//            gClockBDD.andWith(bddExAutomata.getBDDBitVecSource(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//            gClockBDD.andWith(bddExAutomata.getBDDBitVecTarget(gclockIndex).lte(bddExAutomata.getMaxBDDBitVecOf(gclockIndex)));
//
//
//            newEdgesBDD = newEdgesBDD.and(gClockBDD);
//
//        }
//
//        return newEdgesBDD;
//    }
    private BDD saturateForwardClocks(final BDD forwardClocksWithTheSameRate) {
        BDD satForwardClocksWithTheSameRate = forwardClocksWithTheSameRate.id();
        for (final VariableComponentProxy clockComponent : bddExAutomata.orgExAutomata.getClocks()) {
            final int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clockComponent);
            final int clockDomain = bddExAutomata.orgExAutomata.getVarDomain(clockComponent.getName());
            final BDD largestValue = manager.getFactory().buildCube(clockDomain - 1, bddExAutomata.tempClockDomains2[clockIndex].vars());
            final BDD largerThanLargest = manager.createSupremicaBDDBitVector(
                    bddExAutomata.BDDBitVectoryType, bddExAutomata.orgExAutomata.getMinValueofVar(clockComponent.getName()) < 0, bddExAutomata.tempClockDomains1[clockIndex]).gth(
                    manager.createSupremicaBDDBitVector(
                    bddExAutomata.BDDBitVectoryType, bddExAutomata.tempClockDomains1[clockIndex].size().intValue(), clockDomain - 1));

            final BDD satForwardClocksWithTheSameRateCopy = satForwardClocksWithTheSameRate.id();
            satForwardClocksWithTheSameRate = satForwardClocksWithTheSameRate.and(largerThanLargest.not());

            if (!clockComponent.getName().equals(bddExAutomata.orgExAutomata.getGlobalClockName())) {
                BDD bdd = satForwardClocksWithTheSameRateCopy.and(largestValue).and(largerThanLargest).exist(bddExAutomata.tempClockDomains1[clockIndex].set());
                bdd = bdd.replace(bddExAutomata.tempClocki2ToTempClocki1Pairing[clockIndex]);

                satForwardClocksWithTheSameRate = satForwardClocksWithTheSameRate.or(bdd);
            }
        }

        return satForwardClocksWithTheSameRate;
    }

    // A new function to extend the clocks at the same rate
    private BDD[] computeClocksExtension() {
        System.err.println("Synchronizining clocks...");
        BDD frwdClocksWithTheSameRate = manager.getOneBDD();
        BDD bckwdClocksWithTheSameRate = manager.getOneBDD();
        BDD oneTickForward = manager.getOneBDD();
        final VariableComponentProxy gClockComponent = bddExAutomata.orgExAutomata.getClocks().get(0);
        final int gclockIndex = bddExAutomata.theIndexMap.getVariableIndex(gClockComponent);

        final int gClockDomain = bddExAutomata.orgExAutomata.getVarDomain(gClockComponent.getName()); // Get the domain of this clock

        final BDDDomain gclockTempDomain = bddExAutomata.tempClockDomains1[gclockIndex]; // temporary BDD domain of this clock

        final SupremicaBDDBitVector gclockBDDBitVector = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                bddExAutomata.orgExAutomata.getMinValueofVar(gClockComponent.getName()) < 0,
                gclockTempDomain); // BDDBit Vector for this clock with temporary domain
        if (bddExAutomata.BDDBitVectoryType == 1) {
            gclockBDDBitVector.setBit(gclockBDDBitVector.length() - 1, manager.getZeroBDD());
        }

        BDD upperBoundBDD = manager.getOneBDD();
        if (bddExAutomata.orgExAutomata.getClocks().size() == 1) {
            BDD clockExForward = manager.getZeroBDD();
            BDD clockExBack = manager.getZeroBDD();

            for (int i = 0; i < gClockDomain; i++) {
                final BDD igcTarget = manager.createBDD(i, bddExAutomata.getDestVariableDomain(gclockIndex));
                final SupremicaBDDBitVector vector_i = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, gclockTempDomain.varNum(), i);

                final BDD greaterThanIBDD = gclockBDDBitVector.gte(vector_i);
                final BDD lessThanIBDD = gclockBDDBitVector.lte(vector_i);

                clockExForward = clockExForward.or(igcTarget.and(greaterThanIBDD));
                clockExBack = clockExBack.or(igcTarget.and(lessThanIBDD));
            }
            frwdClocksWithTheSameRate = frwdClocksWithTheSameRate.and(clockExForward);
            bckwdClocksWithTheSameRate = bckwdClocksWithTheSameRate.and(clockExBack);
        } else {
            for (final VariableComponentProxy clockComponent : bddExAutomata.orgExAutomata.getClocks()) {
                System.err.println(clockComponent.getName());
                final int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clockComponent);
                final int clockDomain = bddExAutomata.orgExAutomata.getVarDomain(clockComponent.getName());
                final BDDDomain clockTempDomain = bddExAutomata.tempClockDomains1[clockIndex];
                final SupremicaBDDBitVector clockBDDBitVector = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(clockComponent.getName()) < 0,
                        clockTempDomain);
                if (bddExAutomata.BDDBitVectoryType == 1) {
                    clockBDDBitVector.setBit(clockBDDBitVector.length() - 1, manager.getZeroBDD());
                }

                if (clockIndex != gclockIndex) {
                    BDD clockExForward = manager.getZeroBDD();
                    BDD clockExBack = manager.getZeroBDD();

                    for (int i = 0; i < gClockDomain; i++) {
                        final BDD igcTarget = manager.createBDD(i, bddExAutomata.getDestVariableDomain(gclockIndex));
                        // Create a constant BDDBitVector to represent the value i
                        final SupremicaBDDBitVector vector_i = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, gclockTempDomain.varNum(), i);

                        final BDD greaterThanIBDD = gclockBDDBitVector.gte(vector_i);
                        final BDD lessThanIBDD = gclockBDDBitVector.lte(vector_i);

                        for (int j = 0; j < clockDomain; j++) {
                            final BDD jcTarget = manager.createBDD(j, bddExAutomata.getDestVariableDomain(clockIndex));
                            // Create a constant BDDBitVector to rrepresent the value j
                            final SupremicaBDDBitVector vector_j = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, clockTempDomain.varNum(), j);
                            final BDD lessThanJBDD = clockBDDBitVector.lte(vector_j);

                            final BDD greaterThanJBDD = clockBDDBitVector.gte(vector_j);

                            BDD diff;
                            if (i >= j) {
                                diff = gclockBDDBitVector.sub(clockBDDBitVector).equ(vector_i.sub(vector_j)).and(gclockBDDBitVector.gte(clockBDDBitVector));
                            } else {
                                diff = clockBDDBitVector.sub(gclockBDDBitVector).equ(vector_j.sub(vector_i)).and(clockBDDBitVector.gth(gclockBDDBitVector));
                            }

                            final BDD tForward = greaterThanIBDD.and(greaterThanJBDD).and(diff);
                            final BDD lessThanJplus1BDD = clockBDDBitVector.lte( manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, clockTempDomain.varNum(), j+1));
                            oneTickForward = tForward.and(lessThanJplus1BDD);

                            BDD tBackward = lessThanIBDD.and(lessThanJBDD).and(diff);

                            if (i == (gClockDomain - 1)) {
                                final BDD slessThanIBDD = gclockBDDBitVector.lth(vector_i);
                                for (int k = 0; k < j; k++) {
                                    final SupremicaBDDBitVector vector_k = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, clockTempDomain.varNum(), k);

                                    final BDD iANDk = manager.createBDD((i + k + 1), gclockTempDomain).and(manager.createBDD(k, clockTempDomain));

                                    // vector_i: defined before
                                    BDD bInner;
                                    if (i >= k) {
                                        bInner = gclockBDDBitVector.sub(clockBDDBitVector).equ(vector_i.sub(vector_k)).and(gclockBDDBitVector.gte(clockBDDBitVector));
                                    } else {
                                        bInner = clockBDDBitVector.sub(gclockBDDBitVector).equ(vector_k.sub(vector_i)).and(clockBDDBitVector.gth(gclockBDDBitVector));
                                    }

                                    tBackward = tBackward.or((bInner.and(slessThanIBDD)).or(iANDk));

                                    if (j == (clockDomain - 1)) {
                                        final BDD upperBoundclock = manager.createBDD(j, clockTempDomain);
                                        tBackward = tBackward.or(gclockBDDBitVector.gth(vector_i).and(upperBoundclock));
                                    }


                                }
                            }
                            if (j == (clockDomain - 1)) {
                                for (int k = 0; k < i; k++) {
                                    final SupremicaBDDBitVector vector_k = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, gclockTempDomain.varNum(), k);

                                    final BDD lessThanKBDD = gclockBDDBitVector.lte(vector_k);
                                    BDD bInner;
                                    if (j >= k) {
                                        bInner = clockBDDBitVector.sub(gclockBDDBitVector).equ(vector_j.sub(vector_k)).and(clockBDDBitVector.gte(gclockBDDBitVector));
                                    } else {
                                        bInner = gclockBDDBitVector.sub(clockBDDBitVector).equ(vector_k.sub(vector_j)).and(gclockBDDBitVector.gth(clockBDDBitVector));
                                    }

                                    tBackward = tBackward.or(bInner.and(lessThanKBDD).and(lessThanIBDD));
                                }

                            }

                            clockExForward = clockExForward.or(igcTarget.and(jcTarget).and(tForward));
                            clockExBack = clockExBack.or(igcTarget.and(jcTarget).and(tBackward));
                        }
                    }

                    frwdClocksWithTheSameRate = frwdClocksWithTheSameRate.and(clockExForward);
                    bckwdClocksWithTheSameRate = bckwdClocksWithTheSameRate.and(clockExBack);
                }

                final SupremicaBDDBitVector vector_upperbound = manager.createSupremicaBDDBitVector(
                        bddExAutomata.BDDBitVectoryType, clockTempDomain.varNum(), clockDomain - 1);
                upperBoundBDD = upperBoundBDD.and(
                        manager.createBDD(clockDomain - 1, bddExAutomata.getDestVariableDomain(clockIndex))).and(
                        clockBDDBitVector.lte(vector_upperbound));
            }
            bckwdClocksWithTheSameRate = bckwdClocksWithTheSameRate.or(upperBoundBDD);
        }

        final BDD[] output = new BDD[3];
//        forwardClocksWithTheSameRate = bddExAutomata.fitIntoClockDomains(forwardClocksWithTheSameRate);
        output[0] = frwdClocksWithTheSameRate;
//        backwardClocksWithTheSameRate = bddExAutomata.fitIntoClockDomains(backwardClocksWithTheSameRate);
        output[1] = bckwdClocksWithTheSameRate;

        output[2] = oneTickForward;

        return output;
    }

//    // A new function to extend the clocks backward at the same rate -- zhennan
//    private BDD getBackRateClocksExtension() {
//
//        BDD localBackwardClocksWithTheSameRate = manager.getOneBDD();
//
//        VariableComponentProxy gClockComponent = bddExAutomata.orgExAutomata.getClocks().get(0);
//        int gclockIndex = bddExAutomata.theIndexMap.getVariableIndex(gClockComponent);
//
//        int gClockDomain = bddExAutomata.orgExAutomata.getVarDomain(gClockComponent.getName()); // Get the domain of this clock
//
//        BDDDomain gclockTempDomain = bddExAutomata.tempClockDomains[bddExAutomata.theIndexMap.getClockIndex(gClockComponent)]; // temporary BDD domain of this clock
//
//        SupremicaBDDBitVector gclockBDDBitVector =  manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(),gclockTempDomain); // BDDBit Vector for this clock with temporary domain
//
//        for (VariableComponentProxy clockComponent : bddExAutomata.orgExAutomata.getClocks()) {
//
//            if(!clockComponent.getName().equals(gClockComponent.getName())){
//
//                int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clockComponent);
//                BDDDomain clockTempDomain = bddExAutomata.tempClockDomains[bddExAutomata.theIndexMap.getClockIndex(clockComponent)];
//                SupremicaBDDBitVector  clockBDDBitVector =  manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), clockTempDomain);
//                int clockDomain = bddExAutomata.orgExAutomata.getVarDomain(clockComponent.getName());
//
//               BDD clockEx = manager.getZeroBDD();
//
//                for (int i = 0; i < gClockDomain; i++) {
//
//                    // reversedDestVarOrderings is a new variable in bddExAutomata. Just a map which the key is varIndex and the value is reversedDestVarOrdering.
//                    // Remember that we had a problem before when constructing forward clock extension BDD, where the variable domain should be reversed. -- Zhennan
//                    BDD igcTarget = manager.getFactory().buildCube(i, bddExAutomata.reversedDestVarOrderings.get(gclockIndex).toNativeArray());
//                    // Create a constant BDDBitVector to rrepresent the value i
//                    SupremicaBDDBitVector vector_i = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), gclockTempDomain.size().intValue(), i);
//                    BDD lessThanIBDD =  gclockBDDBitVector.lte(vector_i);
//
//                    for (int j = 0; j < clockDomain; j++) {
//                        BDD icTarget = manager.getFactory().buildCube(j,  bddExAutomata.reversedDestVarOrderings.get(clockIndex).toNativeArray());
//                        // Create a constant BDDBitVector to rrepresent the value j
//                        SupremicaBDDBitVector vector_j = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), clockTempDomain.size().intValue(), j);
//                        BDD lessThanJBDD = clockBDDBitVector.lte(vector_j);
//
//                        BDD t = lessThanIBDD.and(lessThanJBDD);
//
//                        if (i >= j) {
//                            t = gclockBDDBitVector.sub(clockBDDBitVector).equ(vector_i.sub(vector_j)).and(gclockBDDBitVector.gte(clockBDDBitVector)).and(t);
//                        } else {
//                            t = clockBDDBitVector.sub(gclockBDDBitVector).equ(vector_j.sub(vector_i)).and(clockBDDBitVector.gth(gclockBDDBitVector)).and(t);
//                        }
//
//                        clockEx = clockEx.or(igcTarget.and(icTarget).and(t));
//                    }
//                }
//
//                localBackwardClocksWithTheSameRate = localBackwardClocksWithTheSameRate.and(clockEx);
//            }
//        }
//
//        return localBackwardClocksWithTheSameRate;
//    }
//    private BDD getForwardRateClocksExtension() {
//
//        BDD localForwardClocksWithTheSameRate = manager.getOneBDD();
//        VariableComponentProxy gClockComponent = bddExAutomata.orgExAutomata.getClocks().get(0); // the first clock
//        int gclockIndex = bddExAutomata.theIndexMap.getVariableIndex(gClockComponent); // index of the first clock
//
//        int gClockDomain = bddExAutomata.orgExAutomata.getVarDomain(gClockComponent.getName()); // Get the domain of this clock
//
//        BDDDomain gclockTempDomain = bddExAutomata.tempClockDomains[bddExAutomata.theIndexMap.getClockIndex(gClockComponent)]; // temporary BDD domain of this clock
//
//        // BDDBit Vector for this clock with temporary domain
//        SupremicaBDDBitVector gclockBDDBitVector =  manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(),gclockTempDomain);
//
//        // iterate each clock which is not the first one
//        for (VariableComponentProxy clockComponent : bddExAutomata.orgExAutomata.getClocks()) {
//
//            if(!clockComponent.getName().equals(gClockComponent.getName())){
//
//                int clockIndex = bddExAutomata.theIndexMap.getVariableIndex(clockComponent); // index
//                BDDDomain clockTempDomain = bddExAutomata.tempClockDomains[bddExAutomata.theIndexMap.getClockIndex(clockComponent)]; // temporary domain
//                // BDDBit Vector for this clock with temporary domain
//                SupremicaBDDBitVector  clockBDDBitVector =  manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), clockTempDomain);
//                int clockDomain = bddExAutomata.orgExAutomata.getVarDomain(clockComponent.getName()); // clock domain
//                BDD clockEx = manager.getZeroBDD();
//
//                for (int i = 0; i < gClockDomain; i++) {
//
//                    BDD igcTarget = manager.getFactory().buildCube(i, bddExAutomata.reversedDestVarOrderings.get(gclockIndex).toNativeArray());
//                    // Create a constant BDDBitVector to represent the value i
//                    SupremicaBDDBitVector vector_i = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), gclockTempDomain.size().intValue(), i);
//                    BDD greaterThenIBDD =  gclockBDDBitVector.gte(vector_i);
//
//                    for (int j = 0; j < clockDomain; j++) {
//                        BDD icTarget = manager.getFactory().buildCube(j,  bddExAutomata.reversedDestVarOrderings.get(clockIndex).toNativeArray());
//                        // Create a constant BDDBitVector to rrepresent the value j
//                        SupremicaBDDBitVector vector_j = manager.createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, manager.getFactory(), clockTempDomain.size().intValue(), j);
//                        BDD greaterThanJBDD = clockBDDBitVector.gte(vector_j);
//
//                        BDD t = greaterThenIBDD.and(greaterThanJBDD);
//
//                        if (i >= j) {
//                            t = gclockBDDBitVector.sub(clockBDDBitVector).equ(vector_i.sub(vector_j)).and(gclockBDDBitVector.gte(clockBDDBitVector)).and(t);
//                        } else {
//                            t = clockBDDBitVector.sub(gclockBDDBitVector).equ(vector_j.sub(vector_i)).and(clockBDDBitVector.gth(gclockBDDBitVector)).and(t);
//                        }
//
//                        clockEx = clockEx.or(igcTarget.and(icTarget).and(t));
//                    }
//                }
//
//                localForwardClocksWithTheSameRate = localForwardClocksWithTheSameRate.and(clockEx);
//            }
//        }
//
//        return localForwardClocksWithTheSameRate;
//    }
    SupremicaBDDBitVector maxBitVec(final int varIndex, final int sourceTargetTemp) {
        SupremicaBDDBitVector output = null;
        switch (sourceTargetTemp) {
            case 0:
                output = bddExAutomata.getBDDBitVecSource(varIndex).copy();
                break;
            case 1:
                output = bddExAutomata.getBDDBitVecTarget(varIndex).copy();
                break;
            case 2:
                output = bddExAutomata.getBDDBitVecTemp(varIndex).copy();
                break;
        }

        for (int i = 0; i < output.length(); i++) {
            output.setBit(i, manager.getOneBDD());
        }
        return output;
    }

    public BDD getStatesTickDisabled(final BDD transitions, final BDD invariants)
    {
        final BDD B1 = transitions.exist(bddExAutomata.getEventVarSet()).and(invariants);
        final BDD Bstt = B1.and(oneStepTickBDD);
        final BDD Btt = Bstt.exist(bddExAutomata.getSourceStatesVarSet());
        BDD B1tt = Btt.replace(bddExAutomata.getDestToSourceVariablePairing());
        B1tt = B1tt.replace(bddExAutomata.getDestToSourceLocationPairing());
        final BDD B2 = B1tt.replace(bddExAutomata.tempClock1ToDestClockPairing).exist(bddExAutomata.tempClock1Varset);
        final BDD B1Dest = B1.exist(bddExAutomata.getSourceStatesVarSet());
        return (B2.and(B1Dest.not())).exist(bddExAutomata.getDestStatesVarSet());
    }

    public BDD getForwardClocksWithTheSameRate() {
        return forwardClocksWithTheSameRate;
    }

    public BDD getBackwardClocksWithTheSameRate() {
        return backwardClocksWithTheSameRate;
    }

    public BDD getOneStepTickBDD() {
        return oneStepTickBDD;
    }

    public BDD getMonolithicEdgesForwardWithoutDestBDD() {
        return edgesForwardWithoutDestBDD;
    }

    public BDD getMonolithicEdgesForwardBDD() {
        return edgesForwardBDD;
    }

    public BDD getMonolithicEdgesBackwardBDD() {
        return edgesBackwardBDD;
    }

    public BDD getMonolithicEdgesForwardWithEventsBDD() {
        return edgesForwardWithEventsBDD;
    }

    public BDD getMonolithicEdgesBackwardWithEventsBDD() {
        return edgesBackwardWithEventsBDD;
    }

    public BDD getMonolithicUncontrollableEdgesBackwardBDD() {
        return uncontrollableEdgesBackwardBDD;
    }

    public BDD getMonolithicUncontrollableEdgesForwardBDD() {
        return uncontrollableEdgesForwardBDD;
    }

    public BDD getPlantMonolithicUncontrollableEdgesForwardBDD() {
        return plantUncontrollableEdgesForwardBDD;
    }

    public BDD getSpecMonolithicUncontrollableEdgesForwardBDD() {
        return specUncontrollableEdgesForwardBDD;
    }

    public BDD getPlantMonolithicEdgesForwardBDD() {
        return plantEdgesForwardBDD;
    }

    public BDD getSpecMonolithicEdgesForwardBDD() {
        return specEdgesForwardBDD;
    }

    public BDD getMonolithicForcibleEdgesForwardBDD() {
        return forcibleEdgesForwardBDD;
    }

    public BDD getMonolithicForcibleSpecEdgesForwardBDD() {
        return forcibleSpecEdgesForwardBDD;
    }

    public void removeFromMonolithicForcibleEdgesForwardBDD(final BDD sourceStates) {
        BDD destStates = sourceStates.replace(bddExAutomata.getSourceToDestLocationPairing());
        destStates = destStates.replace(bddExAutomata.getSourceToDestVariablePairing());
        forcibleEdgesForwardBDD = forcibleEdgesForwardBDD.and(destStates.not());
    }
//    public BDD getPlantMonolithicUncontrollableEdgesBackwardBDD()
//    {
//        return plantUncontrollableEdgesBackwardBDD;
//    }
//
//    public BDD getSpecMonolithicUncontrollableEdgesBackwardBDD()
//    {
//        return specUncontrollableEdgesBackwardBDD;
//    }
}
