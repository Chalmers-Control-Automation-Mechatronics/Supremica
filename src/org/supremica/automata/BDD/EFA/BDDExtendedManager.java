package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.*;
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
import org.supremica.automata.BDD.BDDLibraryType;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.FlowerEFABuilder;
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
        System.err.println("Entering uncontrollableBackward...");
        BDDMonolithicEdges bddEdges = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges());
        final BDD t_u = bddEdges.getMonolithicUncontrollableEdgesBackwardBDD();
        final BDD backwardTime = bddEdges.getBackwardClocksWithTheSameRate();
        final BDD forwardTime = bddEdges.getForwardClocksWithTheSameRate();

//        System.out.println("forbidden");
//        forbidden.printDot();
        BDD Qk = null;
        BDD newUCstates = null;
        BDD newCstates = null;
        BDD Qkn = forbidden.id();
        if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
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
        int iteration = 0;
        do {
//            System.out.println("UBackward: "+iteration++);
            Qk = Qkn.id();
            newUCstates = image_preImage(Qk, t_u, backwardTime);
            if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
                //remove the states from newUCstates that are not uncontrollable due to forcible events
                newCstates = newUCstates.and(bddEdges.getMonolithicForcibleEdgesForwardBDD().exist(bddExAutomata.getDestStatesVarSet()));
                newCstates = timeEvolSource(newCstates, backwardTime);
                newUCstates = newUCstates.and(newCstates.not());
                newUCstates = timeEvolSource(newUCstates, forwardTime);
                bddEdges.removeFromMonolithicForcibleEdgesForwardBDD(newUCstates);
            }
            Qkn = Qk.or(newUCstates);
        } while (!Qkn.equals(Qk));

        System.err.println("Exiting uncontrollableBackward...");
        return Qkn;
    }

    public BDD restrictedBackward(final BDD markedStates, final BDD forbidden) {
        System.err.println("Entering restrictedBackward...");
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
        int iteration = 0;

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
        System.err.println("Exiting restrictedBackward...");

        return Qkn;
    }

    public BDD restrictedForward(BDD initialStates, final BDD forbidden) {
        System.err.println("Entering restrictedForward...");
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

        int iteration = 0;

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

        System.err.println("Exiting restrictedForward...");
        return Qkn;
    }

    public BDD nonblockingControllable(final BDD forbidden, final boolean reachable) {
        System.err.println("Entering nonblockingControllable...");
        final BDD clocks = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges()).getForwardClocksWithTheSameRate();

        BDD Qkn = forbidden;
        BDD Qk = null;
        BDD Q1 = null;
        BDD Q2 = null;

        int iteration = 0;
        do {
//            System.out.println("nbc: "+(iteration++));
            Qk = Qkn.id();
            Q1 = restrictedBackward(bddExAutomata.getMarkedStates(), Qk);
            BDD forbiddenStates = Q1.not().and(bddExAutomata.getReachableStates());
            if(!bddExAutomata.orgExAutomata.getClocks().isEmpty()){
                forbiddenStates = bddExAutomata.fitIntoClockDomains(forbiddenStates).and(
                    (timeEvolSource(bddExAutomata.getMarkedStates(), clocks)).not());
            }
            Q2 = uncontrollableBackward(forbiddenStates);
//            Q2 =  Q2.and((timeEvolSource(bddExAutomata.getMarkedStates(),clocks)).not());
            Qkn = Qk.or(Q2);
        } while ((!Qkn.equals(Qk)));

        System.err.println("Exiting nonblockingControllable...");

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

            final TIntArrayList plantUncontrollableEvents = bddExAutomata.plantUncontrollableEventIndexList;
            final TIntArrayList specUncontrollableEvents = bddExAutomata.specUncontrollableEventIndexList;

            final TIntObjectHashMap<BDD> plantsEnabledStates =
                    new UncontrollableEventDepSets(bddExAutomata, bddExAutomata.plants, plantUncontrollableEvents).getUncontrollableEvents2EnabledStates();
            final TIntObjectHashMap<BDD> specEnabledStates =
                    new UncontrollableEventDepSets(bddExAutomata, bddExAutomata.specs, specUncontrollableEvents).getUncontrollableEvents2EnabledStates();

            final BDD uncontrollableStates = getZeroBDD();

            for (int i = 0; i < specUncontrollableEvents.size(); i++) {
                if (plantUncontrollableEvents.contains(specUncontrollableEvents.get(i))) {
                    final int eventIndex = specUncontrollableEvents.get(i);
                    uncontrollableStates.orWith(plantsEnabledStates.get(eventIndex).and(specEnabledStates.get(eventIndex).not()));
                }
            }

            return uncontrollableStates;
        }
    }

    BDD disjunctiveNonblockingControllable(final BDD forbiddenStates, final boolean reachable) {

        BDD previousForbidenStates = null;
        BDD tmpCoreachableStates = null;
        BDD currentForbidenStates = forbiddenStates;

        boolean flag = false;
        do {
            previousForbidenStates = currentForbidenStates.id();
            currentForbidenStates = bddExAutomata.getDepSets().uncontrollableBackwardWorkSetAlgorithm(currentForbidenStates);
            if (flag && currentForbidenStates.equals(previousForbidenStates)) {
                break;
            } else {
                tmpCoreachableStates = bddExAutomata.getDepSets().backwardRestrictedWorkSetAlgorithm(bddExAutomata.getMarkedStates(), currentForbidenStates);
                currentForbidenStates = tmpCoreachableStates.not();
                flag = true;
            }
        } while (!previousForbidenStates.equals(currentForbidenStates));

        BDD nonblockingControllableStates = null;
        if (reachable) {
            nonblockingControllableStates = bddExAutomata.getDepSets().
                    forwardRestrictedWorkSetAlgorithm(bddExAutomata.getInitialState(), currentForbidenStates);
        } else {
            nonblockingControllableStates = currentForbidenStates.not();
        }
        return nonblockingControllableStates;
    }

    /* Implementeation for Resource Allocation Systems*/
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
        int i = 0;
        do {
//            System.err.println("computeUnsafeStates: "+(i++));
            BDD[] elements = computeElements(upUnsafeStates);
            upUnsafeStates = elements[0].id();

//            System.err.println("Performing the larger operator...");
            BDD newTauTrans = elements[1].or(getLarger(sigmaTrans.and(elements[1].not()), elements[1])).and(getFeasibleSourceStates());
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
    private BDD[] computeElements(BDD unsafeStates) {
        BDD[] elements = new BDD[2];
        BDD upUnsafeStates = unsafeStates.id();
        BDD inevitableStates = getZeroBDD();
        BDD tauTrans = getZeroBDD();
        int i = 0;
        do {
//            System.err.println("computeElements: "+(i++));
            BDD tauSuperBckwdTrans = upUnsafeStates.and(bckwdTransEvents);
            BDD tauSuperFrwdTrans = tauSuperBckwdTrans.exist(bddExAutomata.getSourceStatesVarSet()).replace(
                    bddExAutomata.getDestToSourceVariablePairing());
            BDD boundaryOtherStates = frwdTransEvents.and(tauSuperFrwdTrans.not()).exist(bddExAutomata.getEventVarSet());
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
    private BDD extractNewUnsafeStates(BDD trans) {
        return (trans.exist(bddExAutomata.getEventVarSet()).and(
                frwdTransEvents.and(trans.not()).exist(bddExAutomata.getEventVarSet()).not()));
    }

    private BDD getLarger(BDD trans1, BDD trans2) {
        BDD tmpTrans1 = trans1.replace(bddExAutomata.sourceToTempVariablePairing);
        BDD pairs = trans2.and(tmpTrans1);

        BDD larger = (pairs.and(getGlobalLargerBDD()).exist(bddExAutomata.getSourceStatesVarSet()));
        return larger.replace(bddExAutomata.tempToSourceVariablePairing);
    }

    private BDD getGlobalLargerBDD() {
        if (globalLargerBDD == null) {
            BDD equalsBDD = getOneBDD();
            globalLargerBDD = getOneBDD();
            List<VariableComponentProxy> stageVars = bddExAutomata.orgExAutomata.getStageVars();
            for (VariableComponentProxy stage : stageVars) {
                int stageIndex = bddExAutomata.theIndexMap.getVariableIndex(stage);
                int stageDomain = bddExAutomata.orgExAutomata.getVarDomain(stage.getName());
                BDDDomain stageSourceDomain = bddExAutomata.sourceVarDomains[stageIndex];
                BDDDomain stageTempDomain = bddExAutomata.tempVarDomains[stageIndex];
                SupremicaBDDBitVector stageBDDBitVectorSource = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageSourceDomain);
                SupremicaBDDBitVector stageBDDBitVectorTemp = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageTempDomain);
                BDD stageLargerBDD = getZeroBDD();
                for (int j = 0; j < stageDomain; j++) {
                    BDD jSource = createBDD(j, bddExAutomata.getSourceVariableDomain(stageIndex));
                    SupremicaBDDBitVector vector_j = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, stageTempDomain.varNum(), j);
                    BDD greaterThanJBDD = stageBDDBitVectorTemp.gte(vector_j);
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
        BDD feasibleTrans = frwdTransEvents.and(getFeasibleSourceStates());
        BDD feasSourceStates = feasibleTrans.exist(bddExAutomata.getEventVarSet());
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
            ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());

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

    BDD getInitialUnreachableStates(BDD localBckwdTransEvents) {
        BDD notInitialFeasibleStates = getFeasibleSourceStates().and(localBckwdTransEvents);
        BDD initialFeasibleStates = getFeasibleSourceStates().and(notInitialFeasibleStates.exist(
                bddExAutomata.getEventVarSet()).exist(
                bddExAutomata.getDestStatesVarSet()).not());
        return initialFeasibleStates.and(bddExAutomata.getInitialState().exist(bddExAutomata.getSourceLocationVarSet()).not());
    }
}
