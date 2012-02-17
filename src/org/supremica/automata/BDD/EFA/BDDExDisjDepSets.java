package org.supremica.automata.BDD.EFA;

import java.util.Random;
import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntObjectHashMap;
import java.util.List;
import net.sf.javabdd.BDD;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomataIndexMap;
import org.supremica.automata.ExtendedAutomaton;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.image;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.preImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.preImage2;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedPreImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedPreImage2;

/**
 * The abstract class for partitioning EFAs in BDD
 * 
 * @author zhennan
 * @version 1.0 
 */
public abstract class BDDExDisjDepSets implements BDDExDisjAlgorithms {

    /** A reference to bddExautmata object. */
    BDDExtendedAutomata bddExAutomata;
    /** A reference to orgAutomata object, which is the explicit version of bddExAutomata. */
    ExtendedAutomata orgAutomata;
    /** A list of automaton */
    List<ExtendedAutomaton> theExAutomata;
    /** A reference to manager. */
    BDDExtendedManager manager;
    /** A reference to theIndexMap, used to locate the index of an event or an location. */
    ExtendedAutomataIndexMap theIndexMap;
    /** Size of the depSets, used to initialize and reset the work set*/
    int size;
    /** A work set is responsible to pick an useful component for the fix point computation. */
    BDDExDisjAbstractWorkSet workset;

    /** Construct an instance of BDDExDisjDepSets from bddAutomata. */
    public BDDExDisjDepSets(BDDExtendedAutomata bddExAutomata) {
        this.bddExAutomata = bddExAutomata;
        this.orgAutomata = bddExAutomata.orgExAutomata;
        this.theExAutomata = bddExAutomata.theExAutomata;
        this.manager = bddExAutomata.manager;
        this.theIndexMap = bddExAutomata.theIndexMap;
    }

    /** Initialization */
    protected abstract void initialize();

    /** Return a map where the key is the component index while the value is the component complete transition */
    public abstract TIntObjectHashMap<BDD> getComponentToComponentTransMap();

    /** For the forward reachability, the method returns a map where the key is the component index while the value 
     *  is another map representing forward influenced components and their values. 
     */
    protected abstract TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap();

    /** For the backward reachability, the method returns a map where the key the component index while the value 
     *  is another map representing backward influenced components and their values. 
     */
    protected abstract TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap();

    /** Return a set of component indices of which BDD partitions are qualified as the initial component candidates */
    protected abstract TIntHashSet getInitialComponentCandidates();

    /** Return a set of component indices of which BDD partitions are qualified as the marked component candidates */
    protected abstract TIntHashSet getMarkedComponentCandidates();

    /** Return a set of component indices of which BDD partitions are qualified as the uncontrollable component candidates */
    protected abstract TIntHashSet getUncontrollableComponentCandidates();

    /** Return a BDD which represents all of the transitions labeled by uncontrollable events. */
    protected abstract BDD getUncontrollableTransitionRelationBDD();

    /** Set an work set instance. */
    public void setWorkSet(BDDExDisjAbstractWorkSet workset) {
        this.workset = workset;
    }

    protected abstract TIntObjectHashMap<TIntHashSet> getForwardDependentComponentMap();

    protected abstract TIntObjectHashMap<TIntHashSet> getBackwardDependentComponentMap();

    public int getSize() {
        return size;
    }

    // TEST: randomly select a partial transition relation for the forward reachability.
    public BDD randomForwardWorkSet(BDD initialStates) {
        System.err.println("Using random forward workset [for testing only]");
        BDD[] relationSet = new BDD[getComponentToComponentTransMap().size()];
        int[] eventIndexAsKeys = getComponentToComponentTransMap().keys();

        for (int i = 0; i < eventIndexAsKeys.length; i++) {
            int eventIndex = eventIndexAsKeys[i];
            relationSet[eventIndex] = getComponentToComponentTransMap().get(eventIndex);
        }


        boolean[] activeFlags = new boolean[relationSet.length];
        for (int i = 0; i < activeFlags.length; i++) {
            activeFlags[i] = true;
        }

        int workCount = relationSet.length;

        BDD currentReachableStatesBDD = initialStates;

        BDD previousReachablestatesBDD = null;

        Random random = new Random();
        while (workCount != 0) {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            int comIndex = 0;
            do {
                comIndex = random.nextInt(relationSet.length);
            } while (!activeFlags[comIndex]);
            BDD currentTransitionRelation = (BDD) relationSet[comIndex];
            activeFlags[comIndex] = false;
            workCount--;

            currentReachableStatesBDD = image(bddExAutomata, currentReachableStatesBDD, currentTransitionRelation);

            if (!previousReachablestatesBDD.equals(currentReachableStatesBDD)) {
                TIntHashSet dependentEventIndexSet = getForwardDependentComponentMap().get(comIndex);
                int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                for (int j = 0; j < dependentEventIndexArray.length; j++) {
                    if (!activeFlags[dependentEventIndexArray[j]]) {
                        activeFlags[dependentEventIndexArray[j]] = true;
                        workCount++;
                    }
                }
            }
        }
        return currentReachableStatesBDD;
    }

    // TEST: randomly select a partial transition relation for the backward reachability.
    public BDD randomBackwardWorkSet(BDD markedStates) {
        System.err.println("Using random backward workset [for testing only]");
        BDD[] relationSet = new BDD[getComponentToComponentTransMap().size()];
        int[] eventIndexAsKeys = getComponentToComponentTransMap().keys();

        for (int i = 0; i < eventIndexAsKeys.length; i++) {
            int eventIndex = eventIndexAsKeys[i];
            relationSet[eventIndex] = getComponentToComponentTransMap().get(eventIndex);
        }

        boolean[] activeFlags = new boolean[relationSet.length];
        for (int i = 0; i < activeFlags.length; i++) {
            activeFlags[i] = true;
        }

        int workCount = relationSet.length;

        BDD currentCoreachableStatesBDD = markedStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD previousCoreachablestatesBDD = null;

        Random random = new Random();
        while (workCount != 0) {
            previousCoreachablestatesBDD = currentCoreachableStatesBDD.id();
            int comIndex = 0;
            do {
                comIndex = random.nextInt(relationSet.length);
            } while (!activeFlags[comIndex]);
            BDD currentTransitionRelation = (BDD) relationSet[comIndex];
            activeFlags[comIndex] = false;
            workCount--;

            currentCoreachableStatesBDD = preImage(bddExAutomata, currentCoreachableStatesBDD, currentTransitionRelation);

            if (!previousCoreachablestatesBDD.equals(currentCoreachableStatesBDD)) {
                TIntHashSet dependentEventIndexSet = getBackwardDependentComponentMap().get(comIndex);
                int[] dependentEventIndexArray = dependentEventIndexSet.toArray();
                for (int j = 0; j < dependentEventIndexArray.length; j++) {
                    if (!activeFlags[dependentEventIndexArray[j]]) {
                        activeFlags[dependentEventIndexArray[j]] = true;
                        workCount++;
                    }
                }
            }
        }
        return currentCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing).replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    /* Implement all the methods defined in BDDExDisjAlgorithms. 
       Note that all states as parameters are "source states" and the return values are "source states". 
       But during backward search, states should be "target states".
     */
    @Override
    public BDD forwardWorkSetAlgorithm(BDD initialStates) {

        /* Reset the workset */
        workset.reset();
        workset.setStart();

        BDD currentReachableStatesBDD = initialStates;
        BDD previousReachablestatesBDD = null;

        boolean forward = true;

        while (!workset.empty()) {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentReachableStatesBDD = image(bddExAutomata, currentReachableStatesBDD, currentTransitionRelation);
            workset.advance(choice,!previousReachablestatesBDD.equals(currentReachableStatesBDD));
        }

        //System.err.println("The maximal number of nodes for PA is " + ReachabilityUtilities.maxNbrBDDnodes);
        //ReachabilityUtilities.maxNbrBDDnodes = 0;
        return currentReachableStatesBDD;
    }

    /* For the backward reachability algorithm, there is no backward transition BDD any more. 
       All it just does is to change the variable and location domains to perfrom the backward 
       reachability search. 
     */
    @Override
    public BDD backwardWorkSetAlgorithm(BDD markedStates) {

        workset.reset();
        workset.setStart();
        /* From the computation from the bddAutomaton, the markedStates are "source states". 
           To perform the backward search, we change the markedStates as the target states 
           by exchanging the location and variable domains. 
         */
        BDD currentCoreachableStatesBDD = markedStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD previousCoreachableStatesBDD = null;

        boolean forward = false;

        while (!workset.empty()) {
            previousCoreachableStatesBDD = currentCoreachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            if (choice == Integer.MAX_VALUE) {
                return currentCoreachableStatesBDD
                        .replaceWith(bddExAutomata.destToSourceLocationPairing)
                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
            } else {
                BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
                currentCoreachableStatesBDD = preImage(bddExAutomata, currentCoreachableStatesBDD, currentTransitionRelation);
                workset.advance(choice, !previousCoreachableStatesBDD.equals(currentCoreachableStatesBDD));
            }
        }
        return currentCoreachableStatesBDD
                .replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates) {

        workset.reset();
        workset.setStart();

        BDD reachableStatesAsTargetStates = reachableStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD markedStatesAsTargetStates = markedStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD currentReachableAndCoreachableStatesBDD = markedStatesAsTargetStates.and(reachableStatesAsTargetStates);

        BDD previousReachableAndCoreachableStatesBDD = null;

        boolean forward = false;

        while (!workset.empty()) {
            previousReachableAndCoreachableStatesBDD = currentReachableAndCoreachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            if (choice == Integer.MAX_VALUE) {
                return currentReachableAndCoreachableStatesBDD
                        .replaceWith(bddExAutomata.destToSourceLocationPairing)
                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
            } else {
                BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
                currentReachableAndCoreachableStatesBDD = preImage2(bddExAutomata, currentReachableAndCoreachableStatesBDD, 
                        currentTransitionRelation, reachableStates);
                workset.advance(choice, !previousReachableAndCoreachableStatesBDD.equals(currentReachableAndCoreachableStatesBDD));
            }
        }
        return currentReachableAndCoreachableStatesBDD
                .replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {

        BDD currentReachableStatesBDD = initialStates;//.and(forbiddenStates.not());
        BDD previousReachablestatesBDD = null;
        
        do {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            currentReachableStatesBDD = internalFRWA(currentReachableStatesBDD, forbiddenStates);
        } while (!previousReachablestatesBDD.equals(currentReachableStatesBDD));
        
        return currentReachableStatesBDD;
    }
    
    private BDD internalFRWA (BDD reachableStates, BDD forbiddenStates)
    {
        workset.reset();
        
        BDD currentReachableStates = reachableStates;
        BDD previousReachableStates = null;
        
        boolean forward = true;

        while (!workset.empty()) {
            previousReachableStates = currentReachableStates.id();
            int choice = workset.pickOne(forward);
            BDD currTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentReachableStates = restrictedImage(bddExAutomata, currentReachableStates, forbiddenStates, currTransitionRelation);
            workset.advance(choice, !previousReachableStates.equals(currentReachableStates));
        }
        
        return currentReachableStates;
    }

    @Override
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates) {
        
        BDD markedStatesAsTargetStates = markedStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD forbiddenStatesAsTargetStates = forbiddenStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD currentCoreachableStatesBDD = markedStatesAsTargetStates;//.and(forbiddenStatesAsTargetStates.not());
        BDD previousCoreachableStatesBDD = null;

        do {
            previousCoreachableStatesBDD = currentCoreachableStatesBDD.id();
            currentCoreachableStatesBDD = internalBRWA(currentCoreachableStatesBDD, forbiddenStatesAsTargetStates);
        } while (!previousCoreachableStatesBDD.equals(currentCoreachableStatesBDD));
        
        return currentCoreachableStatesBDD
                .replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }
    
    private BDD internalBRWA (BDD coreachableStatesAsTargetStates, BDD forbiddenStatesAsTargetStates)
    {
        workset.reset();

        BDD currentCoreachableStatesAsTargetStates = coreachableStatesAsTargetStates;
        BDD previousCoreachableStatesAsTargetStates = null;
        
        boolean forward = false;
        while (!workset.empty()) {
            previousCoreachableStatesAsTargetStates = currentCoreachableStatesAsTargetStates.id();
            int choice = workset.pickOne(forward);
            BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentCoreachableStatesAsTargetStates = restrictedPreImage(bddExAutomata, currentCoreachableStatesAsTargetStates,
                    forbiddenStatesAsTargetStates, currentTransitionRelation);
            workset.advance(choice, !previousCoreachableStatesAsTargetStates.equals(currentCoreachableStatesAsTargetStates));
        }

        return currentCoreachableStatesAsTargetStates;
    }
    
    @Override
    public BDD reachableBackwardRestrictedWorkSetAlgorithm (BDD markedStates, BDD forbiddenStates, BDD reachableStates) 
    {
        BDD markedStatesAsTargetStates = markedStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD forbiddenStatesAsTargetStates = forbiddenStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);
        
        BDD currentCoreachableStatesBDDAsTargetStates = markedStatesAsTargetStates;
        BDD previousCoreachableStatesBDDAsTargetStates = null;
        
        do {
            previousCoreachableStatesBDDAsTargetStates = currentCoreachableStatesBDDAsTargetStates.id();
            currentCoreachableStatesBDDAsTargetStates = 
            internalRBRWA(currentCoreachableStatesBDDAsTargetStates, forbiddenStatesAsTargetStates, reachableStates);
        } while (!previousCoreachableStatesBDDAsTargetStates.equals(currentCoreachableStatesBDDAsTargetStates));
        
        return currentCoreachableStatesBDDAsTargetStates
                .replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }
    
    private BDD internalRBRWA (BDD coreachableStatesBDDAsTargetStates, BDD forbiddenStatesAsTargetStates, BDD reachableStates) {

        workset.reset();

        BDD currentCoreachableStatesBDDAsTargetStates = coreachableStatesBDDAsTargetStates;
        BDD previousCoreachableStatesBDD = null;

        boolean forward = false;

        while (!workset.empty()) {
            previousCoreachableStatesBDD = currentCoreachableStatesBDDAsTargetStates.id();
            int choice = workset.pickOne(forward);
            BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentCoreachableStatesBDDAsTargetStates = restrictedPreImage2(bddExAutomata, currentCoreachableStatesBDDAsTargetStates,
                    forbiddenStatesAsTargetStates, currentTransitionRelation, reachableStates);
            workset.advance(choice, !previousCoreachableStatesBDD.equals(currentCoreachableStatesBDDAsTargetStates));
        }
        return currentCoreachableStatesBDDAsTargetStates;
    }

    @Override
    public BDD uncontrollableBackwardWorkSetAlgorithm(BDD forbiddenStates) {

        BDD uncontrollableTransitionRelationBDD = bddExAutomata.getDepSets().getUncontrollableTransitionRelationBDD();

        BDD forbiddenStatesAsTargetstates = forbiddenStates.id()
                .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);

        BDD currentForbiddenStates = forbiddenStatesAsTargetstates;
        BDD previousForbiddenStates = null;

        do {
            previousForbiddenStates = currentForbiddenStates.id();
            BDD nextStatesAsSourceStates = currentForbiddenStates.and(uncontrollableTransitionRelationBDD)
                    .exist(bddExAutomata.destStateVariables);
            BDD nextStates = nextStatesAsSourceStates
                    .replaceWith(bddExAutomata.sourceToDestLocationPairing)
                    .replaceWith(bddExAutomata.sourceToDestVariablePairing);
            currentForbiddenStates.orWith(nextStates);
        } while (!currentForbiddenStates.equals(previousForbiddenStates));

        return currentForbiddenStates
                .replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }
}
