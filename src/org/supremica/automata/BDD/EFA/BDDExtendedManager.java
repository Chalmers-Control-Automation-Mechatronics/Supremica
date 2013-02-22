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
    /* Implementeation for Resource Allocation Systems*/
    public BDD uComputeUnsafeStates() {
        
        frwdTransEvents = ((BDDMonolithicEdges) bddExAutomata.getBDDEdges())
                .getMonolithicEdgesForwardWithEventsBDD();
        
        frwdTransEvents = frwdTransEvents
                .exist(bddExAutomata.getSourceLocationVarSet()
                .union(bddExAutomata.getDestLocationVarSet()));

        // compute the feasible states
        frwdTransEvents = frwdTransEvents.and(getFeasibleSourceStates())
                                         .and(getFeasibleDestStates());
        
        frwdTransEvents = frwdTransEvents
                .exist(bddExAutomata.getSourceResourceVarSet())
                .exist(bddExAutomata.getDestResourceVarSet());
        

        bckwdTransEvents = frwdTransEvents.id();
        
        frwdTransEvents = frwdTransEvents.exist(bddExAutomata.getDestStatesVarSet());

        bckwdTransEvents = bckwdTransEvents
                .replace(bddExAutomata.sourceToTempVariablePairing)
                .replace(bddExAutomata.getDestToSourceVariablePairing())
                .replace(bddExAutomata.tempToDestVariablePairing);

        System.err.println("Computing the deadlock states...");
        BDD unsafeStates = getDeadlocks_U();
        System.err.println("Done.");
         
        System.err.println("Computing the unsafe states...");
        // Transitions under loading events are not considered.
        bckwdTransEvents = bckwdTransEvents.and(bddExAutomata.loadEventsBDD.not());
        
        // state-event pairs where states can reach existing unsafe states via events.
        BDD tauTrans = getZeroBDD(); 
         
        BDD upUnsafeStates = unsafeStates.id(); // unprocessed unsafe states
        
        BDD[] pair = null; 
        
        do {
            
            pair = computeElements_U(upUnsafeStates);
            
            BDD newIneUnsafeStates1 = pair[0]; 
            
            BDD newTauTrans1 = pair[1];
            
            tauTrans = tauTrans.and(newIneUnsafeStates1.not());
            
            tauTrans = tauTrans.or(newTauTrans1);
            
            System.err.println("the bottleneck");
            pair = extractUnsafeStatesFromLargerTau_U(tauTrans);
            
            BDD newIneUnsafeStates2 = pair[0];
            
            BDD newTauTrans2 = pair[1];
            
            tauTrans = tauTrans.and(newIneUnsafeStates2.not());
            
            tauTrans = tauTrans.or(newTauTrans2);
            //tauTrans = newTauTrans2;
            
            upUnsafeStates = newIneUnsafeStates1.or(newIneUnsafeStates2);
            
            unsafeStates = unsafeStates.or(upUnsafeStates);
            
        } while(!upUnsafeStates.isZero());
        
        // ### The following code is used to check the correctness of the algorithm
        // ### with the benchamrk example results from Ahmed's paper.
        System.err.println("Minimizing unsafe states...");
        unsafeStates = unsafeStates
                .and(getLarger(unsafeStates.id(), unsafeStates.id()).not());
        
        
        unsafeStates = unsafeStates.and(bddExAutomata.getReachableStates()
                .exist(bddExAutomata.getSourceLocationVarSet()))
                .exist(bddExAutomata.getSourceResourceVarSet());
        System.err.println("Done.");
        
        BDD reachableStates = bddExAutomata.getReachableStates()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());
        
        BDD nonblockingStates = bddExAutomata.getNonblockingStates()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());
        
        BDD blockingStates = reachableStates.and(nonblockingStates.not());
        blockingStates = blockingStates.and(getLarger(blockingStates.id(), 
                blockingStates.id()).not());
        
        System.err.println(unsafeStates.equals(blockingStates));
        
        return unsafeStates;
    }
    
    
    private BDD[] computeElements_U(BDD upUnsafeStates) {
                
        BDD bTransFromUnsafe = upUnsafeStates.and(bckwdTransEvents);
        
        bckwdTransEvents = bckwdTransEvents.and(bTransFromUnsafe.not());
        
        BDD fTrans = bTransFromUnsafe
                .exist(bddExAutomata.getSourceVariablesVarSet())
                .replace(bddExAutomata.getDestToSourceVariablePairing());
        
        frwdTransEvents = frwdTransEvents.and(fTrans.not());
        
        BDD nonUnsafeStates = frwdTransEvents.exist(bddExAutomata.getEventVarSet());
        
        BDD inevitableStates = fTrans.exist(bddExAutomata.getEventVarSet())
                .and(nonUnsafeStates.not());
        
        frwdTransEvents = frwdTransEvents.and(inevitableStates.not());
        
        BDD tauTrans = nonUnsafeStates.and(fTrans);
        
        BDD[] elements = {inevitableStates, tauTrans};
        
        return elements;
    }
    
    private BDD[] extractUnsafeStatesFromLargerTau_U(BDD tauTrans) {
        
        // find larger tau trans in forward transitions
        BDD largerTau = getLarger(tauTrans.id(), tauTrans.id());
        
        frwdTransEvents = frwdTransEvents.and(largerTau.not());
        
        BDD nonUnsafeStates = frwdTransEvents
                .exist(bddExAutomata.getEventVarSet());
        
        BDD newInevitableStates = largerTau.exist(bddExAutomata.getEventVarSet())
                .and(nonUnsafeStates.not());
        
        BDD newTauTrans = nonUnsafeStates.and(largerTau);
        
        BDD[] elements = {newInevitableStates, newTauTrans};
        
        return elements;
    }
    

    
    private BDD getDeadlocks_U() {

        frwdTransEvents = frwdTransEvents.and(bddExAutomata.loadEventsBDD.not());
        
        BDD notDeadlockStates = frwdTransEvents.exist(bddExAutomata.getEventVarSet());        

        BDD potentialDeadlockStates = bckwdTransEvents
                .exist(bddExAutomata.getEventVarSet())
                .exist(bddExAutomata.getDestVariablesVarSet());
        
        BDD initv = bddExAutomata.getInitialState()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet());
        
        BDD deadlocks = potentialDeadlockStates.and(notDeadlockStates.not())
                .and(initv.not());
        
        // ### TEST
        // # To test results according to Ahmed's report, we compare the number of 
        // # minimal deadlock states. But for the symbolic computation, all deadlocks 
        // # including minimal deadlocks are fine.
        // # Note that if we consider loading events, minimal deadlock states are not 
        // # deadlock states but these states lead to deadlock states only through 
        // # loading events.
        
        //deadlocks = deadlocks.and(getLarger(deadlocks.id(), deadlocks.id()).not());
        
        /*bddExAutomata.getReachableStates()
                .exist(bddExAutomata.getSourceLocationVarSet())
                .exist(bddExAutomata.getSourceResourceVarSet())
                .and(deadlocks).printDot();*/
        
        return deadlocks;
    }

    /**
     * Computation of unsafe states including all minimal unsafe states.
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
            
            final List<VariableComponentProxy> stageVars 
                    = bddExAutomata.orgExAutomata.getStageVars();
            
            for (final VariableComponentProxy stage : stageVars) {
                
                final int stageIndex = bddExAutomata.theIndexMap.getVariableIndex(stage);
                
                final int stageDomain 
                        = bddExAutomata.orgExAutomata.getVarDomain(stage.getName());
                
                final BDDDomain stageSourceDomain 
                        = bddExAutomata.sourceVarDomains[stageIndex];
                
                final BDDDomain stageTempDomain = bddExAutomata.tempVarDomains[stageIndex];
                
                final SupremicaBDDBitVector stageBDDBitVectorSource 
                        = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageSourceDomain);
                
                final SupremicaBDDBitVector stageBDDBitVectorTemp 
                        = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType,
                        bddExAutomata.orgExAutomata.getMinValueofVar(stage.getName()) < 0,
                        stageTempDomain);
                
                BDD stageLargerBDD = getZeroBDD();
                
                for (int j = 0; j < stageDomain; j++) {
                   
                    final BDD jSource 
                            = createBDD(j, bddExAutomata.getSourceVariableDomain(stageIndex));
                    
                    final SupremicaBDDBitVector vector_j 
                            = createSupremicaBDDBitVector(bddExAutomata.BDDBitVectoryType, 
                            stageTempDomain.varNum(), j);
                    
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

    // No caller. Why is it here?
    BDD getInitialUnreachableStates(final BDD localBckwdTransEvents) {
        final BDD notInitialFeasibleStates = getFeasibleSourceStates().and(localBckwdTransEvents);
        final BDD initialFeasibleStates = getFeasibleSourceStates().and(notInitialFeasibleStates.exist(
                bddExAutomata.getEventVarSet()).exist(
                bddExAutomata.getDestStatesVarSet()).not());
        return initialFeasibleStates.and(bddExAutomata.getInitialState().exist(bddExAutomata.getSourceLocationVarSet()).not());
    }
}
