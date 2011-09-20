package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import gnu.trove.TIntObjectProcedure;
import gnu.trove.TObjectIntHashMap;
import java.util.HashSet;
import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.module.VariableComponentProxy;

/**
 * Variable-based disjunctive partitioning
 *
 * @author zhennan
 * @version 1.0
 *
 */
public class BDDExDisjVariableDepSets extends BDDExDisjDepSetsDecorator{

    /** A map from names of variables to their indices. */
    private final TObjectIntHashMap<String> variableNameToIndex;

    /** A map from variable indices to variable names. */
    private final TIntObjectHashMap<String> variableIndexToName;

    /** A map from variable indices to their partitioned BDD expressions. */
    private final TIntObjectHashMap<BDD> variableToBDD;

    /** A map from variable indices to a map where the key is the influenced component index while
     *   the value is the influenced value.
     */
    @SuppressWarnings("unused")
    private final TIntObjectHashMap<TIntIntHashMap> variableToForwardInfluencedVariables;

    @SuppressWarnings("unused")
    private final TIntObjectHashMap<TIntIntHashMap> variableToBackwardInfluencedVariables;

    /** A variable can appear in several arcs where events are enabled.
     *   After filling this field, the BDDs corresponding to all the events for one variable will be used to construct the BDD
     *   expression of a variable partition.
     */
    private final TIntObjectHashMap<TIntHashSet> variableToEvents;


    public BDDExDisjVariableDepSets(final BDDExtendedAutomata bddExtendAutomata, final BDDExDisjEventDepSets eventDepSets) {
        super(bddExtendAutomata, eventDepSets);
        this.size = orgAutomata.getVars().size();
        this.variableNameToIndex = new TObjectIntHashMap<String>(size);
        this.variableIndexToName = new TIntObjectHashMap<String>(size);
        this.variableToBDD = new TIntObjectHashMap<BDD>(size);
        this.variableToForwardInfluencedVariables = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.variableToBackwardInfluencedVariables = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.variableToEvents = new TIntObjectHashMap<TIntHashSet>(size);

        initialize();
    }

    @Override
    protected final void initialize() {

        for (final VariableComponentProxy variable : orgAutomata.getVars()) {/*Fill these fields*/
            final String variableName = variable.getName();
            final int variableIndex = theIndexMap.getVariableIndex(variable);
            variableNameToIndex.put(variableName, variableIndex);
            variableIndexToName.put(variableIndex, variableName);
            variableToEvents.put(variableIndex, new TIntHashSet());
            variableToBDD.put(variableIndex, manager.getZeroBDD());
        }

        final TIntObjectHashMap<HashSet<String>> eventIndex2GuardVariables = eventParDepSets.getEventIndex2GuardVariables();

        eventIndex2GuardVariables.forEachEntry(new TIntObjectProcedure<HashSet<String>>() {

            @Override
            public boolean execute(final int eventIndex, final HashSet<String> guardVariables) {
                if(!guardVariables.isEmpty()){
                    for(final String aguardVariable: guardVariables){
                        variableToEvents.get(variableNameToIndex.get(aguardVariable)).add(eventIndex);
                    }
                }else{
                    final int[] autIndicesAsKeys = variableToEvents.keys();
                    for(int i = 0; i < autIndicesAsKeys.length; i++){
                        variableToEvents.get(autIndicesAsKeys[i]).add(eventIndex);
                    }
                }
                return true;
            }
        });

        buildVariableToBDD();
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap() {
         return variableToBDD;
    }

    // Get a map which is the form of [variableIndex [influencedVariableIndex influencedValue]]
    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap() {
//        int[] variableIndices = variableToEvents.keys();
//        for (int i = 0; i < variableIndices.length; i++) {
//            int variableIndex = variableIndices[i];
//            TIntIntHashMap influencedVariable2Value = new TIntIntHashMap();
//            for (TIntIterator itr = variableToEvents.get(variableIndex).iterator(); itr.hasNext();) {
//                HashSet<String> updatedVariablesByTheEvent = eventParDepSets.getEventIndex2UpdatedVariables().get(itr.next());
//                for (String aUpdatedVariable : updatedVariablesByTheEvent) {
//                    int influencedVariableIndex = variableNameToIndex.get(aUpdatedVariable);
//                    if (influencedVariableIndex != variableIndex) {
//                        influencedVariable2Value.adjustOrPutValue(influencedVariableIndex, influencedVariable2Value.get(influencedVariableIndex) + 1,1);
//                    }
//                }
//                for(VariableComponentProxy aVariable: orgAutomata.getVars()){
//                    int notInfluencedVariableIndex = theIndexMap.getVariableIndex(aVariable);
//                    if(!updatedVariablesByTheEvent.contains(aVariable.getName()) && notInfluencedVariableIndex != variableIndex)
//                        influencedVariable2Value.put(notInfluencedVariableIndex, 0);
//                }
//            }
//            variableToForwardInfluencedVariables.put(variableIndex, influencedVariable2Value);
//        }
//        return variableToForwardInfluencedVariables;
        return eventParDepSets.getForwardComponentToInfluencedComponentMap();
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap() {
//        int[] variableIndices = variableToEvents.keys();
//        for (int i = 0; i < variableIndices.length; i++) {
//            int variableIndex = variableIndices[i];
//            TIntIntHashMap influencedVariable2Value = new TIntIntHashMap();
//            for (TIntIterator itr = variableToEvents.get(variableIndex).iterator(); itr.hasNext();) {
//                HashSet<String> guardVariablesByTheEvent = eventParDepSets.getEventIndex2GuardVariables().get(itr.next());
//                for (String aguardVariable : guardVariablesByTheEvent) {
//                    int influencedVariableIndex = variableNameToIndex.get(aguardVariable);
//                    if (influencedVariableIndex != variableIndex) {
//                        influencedVariable2Value.adjustOrPutValue(influencedVariableIndex, influencedVariable2Value.get(influencedVariableIndex) + 1, 1);
//                    }
//                }
//                for (VariableComponentProxy aVariable : orgAutomata.getVars()) {
//                    int notInfluencedVariableIndex = theIndexMap.getVariableIndex(aVariable);
//                    if (!guardVariablesByTheEvent.contains(aVariable.getName()) && notInfluencedVariableIndex != variableIndex) {
//                        influencedVariable2Value.put(notInfluencedVariableIndex, 0);
//                    }
//                }
//            }
//            variableToBackwardInfluencedVariables.put(variableIndex, influencedVariable2Value);
//        }
//        return variableToBackwardInfluencedVariables;
        return eventParDepSets.getBackwardComponentToInfluencedComponentMap();
    }
    /** Based on the events associated with each variable, build the BDD expression of it. */
    private void buildVariableToBDD() {
        final int[] variableIndicesAsKeys = variableToEvents.keys();
        for(int i = 0; i < variableIndicesAsKeys.length; i++){
                BDD variableTransitions = manager.getZeroBDD();
                final TIntHashSet includedEvents = variableToEvents.get(variableIndicesAsKeys[i]);
                for(final TIntIterator itr = includedEvents.iterator(); itr.hasNext();){
                    variableTransitions = variableTransitions.or(eventParDepSets.getComponentToComponentTransMap().get(itr.next()));
                }
                variableToBDD.get(variableIndicesAsKeys[i]).orWith(variableTransitions);
        }
    }

    @Override
    protected TIntHashSet getInitialComponentCandidates() {
        return eventParDepSets.getInitialComponentCandidates();
    }

    @Override
    protected TIntHashSet getMarkedComponentCandidates() {
        return eventParDepSets.getMarkedComponentCandidates();
    }

    @Override
    protected TIntHashSet getUncontrollableComponentCandidates() {
        return eventParDepSets.getUncontrollableComponentCandidates();
    }

    @Override
    protected BDD getUncontrollableTransitionRelationBDD() {
        return eventParDepSets.getUncontrollableTransitionRelationBDD();
    }

    /* Another idea: disjunctive (event trans BDD without actions)+ selective conjunctive (variable update)*/
//    @Override
//    public BDD forwardWorkSetAlgorithm(BDD initialStates) {
//
//        System.err.println("Start variable based c/d partitioning forward reachability search: ");
//        workset.reset();
//
//        BDD[] compensateBDDArray = new BDD[orgAutomata.getVars().size()];
//        for (VariableComponentProxy var : orgAutomata.getVars()) {
//            int varIndex = theIndexMap.getVariableIndex(var);
//            compensateBDDArray[varIndex] = bddExAutomata.BDDBitVecTargetVarsMap.get(var.getName()).equ(bddExAutomata.BDDBitVecSourceVarsMap.get(var.getName()));
//        }
//
//       BDD currentReachableStatesBDD = initialStates;
//       BDD previousReachablestatesBDD = null;
//
//       /* Use the intuitive way to test */
//        do {
//
//            int[] eventIndexAsKeys = eventParDepSets.eventToTransitionBDDwithoutActions.keys();
//
//            previousReachablestatesBDD = currentReachableStatesBDD.id();
//
//            for (int i = 0; i < eventIndexAsKeys.length; i++) {
//
//                int currEventIndex = eventIndexAsKeys[i];
//
//                BDD currentTransition = eventParDepSets.getEventToTransitionBDDwithoutActions().get(currEventIndex);
//
//                BDD caredTransition = currentReachableStatesBDD.and(currentTransition);
//
//                BDD nextStatesWithCaredValues = manager.getOneBDD();
//
//                HashSet<String> updatedVariablesNames = eventParDepSets.eventIndex2UpdatedVariables.get(currEventIndex);
//
//                for (String updatedVariableName : updatedVariablesNames) {
//                    int varIndex = theIndexMap.getVariableIndexByName(updatedVariableName);
//
//                    BDD t = caredTransition.and(eventParDepSets.getVariableUpdateBDD()[varIndex])
//                            .exist(bddExAutomata.getEventVarSet()).exist(bddExAutomata.sourceStateVariables);
//
//                    nextStatesWithCaredValues.andWith(t);
//                }
//
//                nextStatesWithCaredValues.replaceWith(bddExAutomata.destToSourceLocationPairing)
//                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
//
//                //Add compensate (v:=v)
//                BDD compensate = manager.getOneBDD();
//                for (int j = 0; j < compensateBDDArray.length; j++) {
//                    if (!updatedVariablesNames.contains(theIndexMap.getVariableAt(j).getName())) {
//                        compensate = compensate.and(compensateBDDArray[j]);
//                    }
//                }
//
//                BDD dontCaredValues = caredTransition.and(compensate).exist(bddExAutomata.getEventVarSet())
//                        .exist(bddExAutomata.sourceVariablesVarSet)
//                        .exist(bddExAutomata.destLocationVariables)
//                        .replaceWith(bddExAutomata.destToSourceVariablePairing);
//
//                currentReachableStatesBDD.orWith(nextStatesWithCaredValues.and(dontCaredValues));
//            }
//        } while (!currentReachableStatesBDD.equals(previousReachablestatesBDD));
//
//
////        boolean forward = true;
////
////        while (!workset.empty()) {
////
////            previousReachablestatesBDD = currentReachableStatesBDD.id();
////            int choice = workset.pickOne(forward);
////
////            BDD currentTransition = eventParDepSets.getEventToTransitionBDDwithoutActions().get(choice);
////
////            BDD caredTransition = currentReachableStatesBDD.and(currentTransition);
////
////            BDD updatedVariablesBDD = manager.getOneBDD();
////            HashSet<String> updatedVariablesNames = eventParDepSets.eventIndex2UpdatedVariables.get(choice);
////            for(String updatedVariableName: updatedVariablesNames){
////                int varIndex = theIndexMap.getVariableIndexByName(updatedVariableName);
////                BDD t = caredTransition
////                        .and(eventParDepSets.getVariableUpdateBDD()[varIndex]).exist(bddExAutomata.getEventVarSet())
////                        .exist(bddExAutomata.sourceStateVariables);
////                updatedVariablesBDD.andWith(t);
////            }
////
////            //Add compensate (v:=v)
////            BDD compensate = manager.getOneBDD();
////            for(int i = 0; i < compensateBDDArray.length; i++){
////                if(!updatedVariablesNames.contains(theIndexMap.getVariableAt(i).getName()))
////                    compensate = compensate.and(compensateBDDArray[i]);
////            }
////
////            compensate = currentReachableStatesBDD.exist(bddExAutomata.sourceLocationVariables).and(compensate).exist(bddExAutomata.sourceVariablesVarSet);
////
////             currentReachableStatesBDD.orWith(updatedVariablesBDD.and(compensate).replaceWith(bddExAutomata.destToSourceLocationPairing).replaceWith(bddExAutomata.destToSourceVariablePairing));
////
////            workset.advance(choice, !previousReachablestatesBDD.equals(currentReachableStatesBDD));
////        }
//
//        return currentReachableStatesBDD;
//    }

}
