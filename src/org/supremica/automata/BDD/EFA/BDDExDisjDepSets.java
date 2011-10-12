package org.supremica.automata.BDD.EFA;

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
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedImage;
import static org.supremica.automata.BDD.EFA.ReachabilityUtilities.restrictedPreImage;

/**
 * The abstract class for partitioning EFAs in BDD
 * 
 * @author zhennan
 * @version 1.0 
 */

public abstract class BDDExDisjDepSets implements BDDExDisjAlgorithms {

    BDDExtendedAutomata bddExAutomata;
    ExtendedAutomata orgAutomata;
    List<ExtendedAutomaton> theExAutomata;
    BDDExtendedManager manager;
    ExtendedAutomataIndexMap theIndexMap;
    BDDExDisjAbstractWorkSet workset;
    /** Size of the depSets, used to initialize and reset the work set*/
    int size;

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
    
    /** For the forward reachability, the method returns a map where the key is the component index while the value is another map 
     *   representing forward influenced components and their values. */
    protected abstract TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap();

    /** For the backward reachability, the method returns a map where the key the component index while the value is another map 
     *   representing backward influenced components and their values. */
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

    /** Return the size. 
     *   1. For event-based partitioning, size is equal to the size of alphabet;
     *   2. For automaton-based partitioning, size is equal to the size of automata;
     *   3. For variable-based partitioning, size is equal to the size of variables.
     */
    public int getSize() {
        return size;
    }

    /* Implement all the methods defined in BDDExDisjAlgorithms. 
        Note that all states as parameters are "source states" and the return values are "source states". 
        But during backward search, states should be "target states".
     */
    @Override
    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
        
        /* Reset the workset */
        workset.reset(); 
        
        BDD currentReachableStatesBDD = initialStates;
        BDD previousReachablestatesBDD = null;
        
        boolean forward = true;
        
        while (!workset.empty()) {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentReachableStatesBDD = image(bddExAutomata, currentReachableStatesBDD, currentTransitionRelation);
            workset.advance(choice, !previousReachablestatesBDD.equals(currentReachableStatesBDD));
        }
        
        return currentReachableStatesBDD;
    }

    /* For the backward reachability algorithm, there is no backward transition BDD any more. 
        All it just does is to change the variable and location domains to perfrom the backward 
        reachability search. 
     */
    @Override
    public BDD backwardWorkSetAlgorithm(BDD markedStates) {
                
        workset.reset();
        
        /* From the computation from the bddAutomaton, the markedStates are  "source states". 
            To perform the backward search, we change the markedStates as the target states 
            by exchanging the location and variable domains. 
         */
        BDD currentCoreachableStatesBDD = markedStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD previousCoreachableStatesBDD = null;
        
        boolean forward = false;
        
        while (!workset.empty()) {
            previousCoreachableStatesBDD = currentCoreachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            if (choice == Integer.MAX_VALUE) {
                return currentCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
            } else {
                BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
                currentCoreachableStatesBDD = preImage(bddExAutomata, currentCoreachableStatesBDD, currentTransitionRelation);
                workset.advance(choice, !previousCoreachableStatesBDD.equals(currentCoreachableStatesBDD));
            }
        }
        return currentCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD reachableBackwardWorkSetAlgorithm(BDD markedStates, BDD reachableStates) {
        
        workset.reset();
        
        BDD reachableStatesAsTargetStates = reachableStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD markedStatesAsTargetStates = markedStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD currentReachableAndCoreachableStatesBDD = markedStatesAsTargetStates.and(reachableStatesAsTargetStates);
        BDD previousReachableAndCoreachableStatesBDD = null;
        
        boolean forward = false;
        
        while (!workset.empty()) {
            previousReachableAndCoreachableStatesBDD = currentReachableAndCoreachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            if (choice == Integer.MAX_VALUE) {
                return currentReachableAndCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
            } else {
                BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
                currentReachableAndCoreachableStatesBDD = reachableStatesAsTargetStates.and(preImage(bddExAutomata, currentReachableAndCoreachableStatesBDD, currentTransitionRelation));
                workset.advance(choice, !previousReachableAndCoreachableStatesBDD.equals(currentReachableAndCoreachableStatesBDD));
            }
        }
        return currentReachableAndCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD forwardRestrictedWorkSetAlgorithm(BDD initialStates, BDD forbiddenStates) {
                
        workset.reset();
        
        BDD currentReachableStatesBDD = initialStates;//.and(forbiddenStates.not());
        BDD previousReachablestatesBDD = null;
        
        boolean forward = true;
        
        while (!workset.empty()) {
            previousReachablestatesBDD = currentReachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
            currentReachableStatesBDD = restrictedImage(bddExAutomata, currentReachableStatesBDD, forbiddenStates, currentTransitionRelation);
            workset.advance(choice, !previousReachablestatesBDD.equals(currentReachableStatesBDD));
        }
        return currentReachableStatesBDD;
    }

    @Override
    public BDD backwardRestrictedWorkSetAlgorithm(BDD markedStates, BDD forbiddenStates) {
        
        workset.reset();
        
        BDD markedStatesAsTargetStates = markedStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD forbiddenStatesAsTargetStates = forbiddenStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing).replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD currentCoreachableStatesBDD = markedStatesAsTargetStates;//.and(forbiddenStatesAsTargetStates.not());
        BDD previousCoreachableStatesBDD = null;
        
        boolean forward = false;
        
        while (!workset.empty()) {
            previousCoreachableStatesBDD = currentCoreachableStatesBDD.id();
            int choice = workset.pickOne(forward);
            if (choice == Integer.MAX_VALUE) {
                return currentCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
            } else {
                BDD currentTransitionRelation = getComponentToComponentTransMap().get(choice);
                currentCoreachableStatesBDD = restrictedPreImage(bddExAutomata, currentCoreachableStatesBDD, forbiddenStatesAsTargetStates, currentTransitionRelation);
                workset.advance(choice, !previousCoreachableStatesBDD.equals(currentCoreachableStatesBDD));
            }
        }
        return currentCoreachableStatesBDD.replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }

    @Override
    public BDD uncontrollableBackwardWorkSetAlgorithm(BDD forbiddenStates) {
        
        BDD uncontrollableTransitionRelationBDD = bddExAutomata.getDepSets().getUncontrollableTransitionRelationBDD();
        
        BDD forbiddenStatesAsTargetstates = forbiddenStates.id().replaceWith(bddExAutomata.sourceToDestLocationPairing)
                .replaceWith(bddExAutomata.sourceToDestVariablePairing);
        BDD currentForbiddenStates = forbiddenStatesAsTargetstates;
        BDD previousForbiddenStates = null;
        
        do{
            previousForbiddenStates = currentForbiddenStates.id();
            BDD nextStatesAsSourceStates = currentForbiddenStates.and(uncontrollableTransitionRelationBDD).exist(bddExAutomata.destStateVariables);
            BDD nextStates = nextStatesAsSourceStates.replaceWith(bddExAutomata.sourceToDestLocationPairing)
                                                     .replaceWith(bddExAutomata.sourceToDestVariablePairing);
            currentForbiddenStates.orWith(nextStates);
        }while(!currentForbiddenStates.equals(previousForbiddenStates));
        
        return currentForbiddenStates.replaceWith(bddExAutomata.destToSourceLocationPairing)
                .replaceWith(bddExAutomata.destToSourceVariablePairing);
    }
}
