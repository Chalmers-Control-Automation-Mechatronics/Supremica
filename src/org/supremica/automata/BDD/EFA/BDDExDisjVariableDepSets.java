package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
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
    private final TIntObjectHashMap<TIntIntHashMap> variableToForwardInfluencedVariables;

    private final TIntObjectHashMap<TIntIntHashMap> variableToBackwardInfluencedVariables;

    /** A variable can appear in several arcs where events are enabled.
     *   After filling this field, the BDDs corresponding to all the events for one variable will be used to construct the BDD
     *   expression of a variable partition.
     */
    private final TIntObjectHashMap<TIntHashSet> variableToEvents;
    
    
   private TIntHashSet initialComponentCandidates;
    
    private TIntHashSet markedComponentCandidates;
    
    private TIntHashSet uncontrollableComponentCandidates;


    public BDDExDisjVariableDepSets(final BDDExtendedAutomata bddExtendAutomata, final BDDExDisjEventDepSets eventDepSets) {
        
        super(bddExtendAutomata, eventDepSets);
        this.size = orgAutomata.getVars().size();
        this.variableNameToIndex = new TObjectIntHashMap<String>(size);
        this.variableIndexToName = new TIntObjectHashMap<String>(size);
        this.variableToBDD = new TIntObjectHashMap<BDD>(size);
        this.variableToForwardInfluencedVariables = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.variableToBackwardInfluencedVariables = new TIntObjectHashMap<TIntIntHashMap>(size);
        this.variableToEvents = new TIntObjectHashMap<TIntHashSet>(size);

        this.initialComponentCandidates = new TIntHashSet();
        this.markedComponentCandidates = new TIntHashSet();
        this.uncontrollableComponentCandidates = new TIntHashSet();

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
        final TIntObjectHashMap<HashSet<String>> eventIndex2UpdatedVariables = eventParDepSets.getEventIndex2UpdatedVariables();

        int[] eventIndicesAsKeys = eventIndex2GuardVariables.keys();

        for (int i = 0; i < eventIndicesAsKeys.length; i++) {

            int currEventIndex = eventIndicesAsKeys[i];

            HashSet<String> guardVariablesForCurrEventIndex = eventIndex2GuardVariables.get(currEventIndex);
            HashSet<String> updatedVariablesForCurrEventIndex = eventIndex2UpdatedVariables.get(currEventIndex);

            if (guardVariablesForCurrEventIndex.isEmpty() && updatedVariablesForCurrEventIndex.isEmpty()) {
                final int[] variableIndicesAsKeys = variableToEvents.keys();
                for (int j = 0; i < variableIndicesAsKeys.length; j++) {
                    variableToEvents.get(variableIndicesAsKeys[j]).add(currEventIndex);
                }
            } else {
                for (String aGuardVariable : guardVariablesForCurrEventIndex) {
                    variableToEvents.get(variableNameToIndex.get(aGuardVariable)).add(currEventIndex);
                }

                for (String aUpdatedVariable : updatedVariablesForCurrEventIndex) {
                    if (!guardVariablesForCurrEventIndex.contains(aUpdatedVariable)) {
                        variableToEvents.get(variableNameToIndex.get(aUpdatedVariable)).add(currEventIndex);
                    }
                }
            }
        }
        
        buildVariableToBDD();
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap() {
         return variableToBDD;
    }

    // Get a map which is the form of [variableIndex [influencedVariableIndex influencedValue]]
    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap() {
        
        int[] variableIndices = variableToEvents.keys();
        
        for (int i = 0; i < variableIndices.length; i++) {
            
            int variableIndex = variableIndices[i];
            TIntIntHashMap influencedVariable2Value = new TIntIntHashMap();
            
            for (TIntIterator itr = variableToEvents.get(variableIndex).iterator(); itr.hasNext();) {
                
                HashSet<String> updatedVariablesByTheEvent = eventParDepSets.getEventIndex2UpdatedVariables().get(itr.next());
                
                for (String aUpdatedVariable : updatedVariablesByTheEvent) {
                    int influencedVariableIndex = variableNameToIndex.get(aUpdatedVariable);
                    if (influencedVariableIndex != variableIndex) {
                        influencedVariable2Value.adjustOrPutValue(influencedVariableIndex, influencedVariable2Value.get(influencedVariableIndex) + 1,1);
                    }
                }
                
                for(VariableComponentProxy aVariable: orgAutomata.getVars()){
                    int notInfluencedVariableIndex = theIndexMap.getVariableIndex(aVariable);
                    if(!updatedVariablesByTheEvent.contains(aVariable.getName()) && notInfluencedVariableIndex != variableIndex)
                        influencedVariable2Value.put(notInfluencedVariableIndex, 0);
                }
            }
            variableToForwardInfluencedVariables.put(variableIndex, influencedVariable2Value);
        }
        return variableToForwardInfluencedVariables;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap() {
        
        int[] variableIndices = variableToEvents.keys();
        
        for (int i = 0; i < variableIndices.length; i++) {
           
            int variableIndex = variableIndices[i];
            TIntIntHashMap influencedVariable2Value = new TIntIntHashMap();
            
            for (TIntIterator itr = variableToEvents.get(variableIndex).iterator(); itr.hasNext();) {
            
                HashSet<String> guardVariablesByTheEvent = eventParDepSets.getEventIndex2GuardVariables().get(itr.next());
                
                for (String aguardVariable : guardVariablesByTheEvent) {
                    int influencedVariableIndex = variableNameToIndex.get(aguardVariable);
                    if (influencedVariableIndex != variableIndex) {
                        influencedVariable2Value.adjustOrPutValue(influencedVariableIndex, influencedVariable2Value.get(influencedVariableIndex) + 1, 1);
                    }
                }
               
                for (VariableComponentProxy aVariable : orgAutomata.getVars()) {
                    int notInfluencedVariableIndex = theIndexMap.getVariableIndex(aVariable);
                    if (!guardVariablesByTheEvent.contains(aVariable.getName()) && notInfluencedVariableIndex != variableIndex) {
                        influencedVariable2Value.put(notInfluencedVariableIndex, 0);
                    }
                }
            }
            variableToBackwardInfluencedVariables.put(variableIndex, influencedVariable2Value);
        }
        return variableToBackwardInfluencedVariables;
    }
    
    /** Based on the events associated with each variable, build the BDD expression of it. */
    private void buildVariableToBDD() {
        
        final int[] variableIndicesAsKeys = variableToEvents.keys();
        for(int i = 0; i < variableIndicesAsKeys.length; i++){
            
                BDD variableTransitions = manager.getZeroBDD();
                int currVariableIndex = variableIndicesAsKeys[i];
                final TIntHashSet includedEvents = variableToEvents.get(currVariableIndex);
                
            for (final TIntIterator itr = includedEvents.iterator(); itr.hasNext();) {

                int anEventIndex = itr.next();
                variableTransitions = variableTransitions.or(eventParDepSets.getComponentToComponentTransMap().get(anEventIndex));

                if (eventParDepSets.getInitialComponentCandidates().contains(anEventIndex)) {
                    initialComponentCandidates.add(currVariableIndex);
                }

                if (eventParDepSets.getMarkedComponentCandidates().contains(anEventIndex)) {
                    markedComponentCandidates.add(currVariableIndex);
                }

                if (eventParDepSets.getUncontrollableComponentCandidates().contains(anEventIndex)) {
                    uncontrollableComponentCandidates.add(currVariableIndex);
                }
            }
            variableToBDD.get(variableIndicesAsKeys[i]).orWith(variableTransitions);
        }
    }

    @Override
    protected TIntHashSet getInitialComponentCandidates() {
        return initialComponentCandidates;
    }

    @Override
    protected TIntHashSet getMarkedComponentCandidates() {
        return markedComponentCandidates;
    }

    @Override
    protected TIntHashSet getUncontrollableComponentCandidates() {
        return uncontrollableComponentCandidates;
    }

    @Override
    protected BDD getUncontrollableTransitionRelationBDD() {
        return eventParDepSets.getUncontrollableTransitionRelationBDD();
    }
}
