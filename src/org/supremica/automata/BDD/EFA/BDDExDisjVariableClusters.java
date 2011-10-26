package org.supremica.automata.BDD.EFA;

import gnu.trove.TIntHashSet;
import gnu.trove.TIntIntHashMap;
import gnu.trove.TIntIntProcedure;
import gnu.trove.TIntIterator;
import gnu.trove.TIntObjectHashMap;
import java.util.ArrayList;
import java.util.List;
import net.sf.javabdd.BDD;
import org.supremica.util.BDD.Options;

/**
 * Group several variable BDDs to make the fix-point iteration efficient.
 * 
 * @author zhennan
 * @version 1.0
 * 
 */

public class BDDExDisjVariableClusters extends BDDExDisjDepSetsDecorator {

    private BDDExDisjVariableDepSets variableDepSet;
    
    private final List<VariablesCluster> clusterList;
    
    private final TIntHashSet initialComponentCandidates;
    private final TIntHashSet markedComponentCandidates;
    private final TIntHashSet uncontrollableComponentCandidates;
    
    private final  TIntObjectHashMap<BDD> clusterToBDD;
    TIntObjectHashMap<TIntIntHashMap> clusterToForwardInfluencedClusters;
    TIntObjectHashMap<TIntIntHashMap> clusterToBackwardinfluencedClusters;

    public BDDExDisjVariableClusters(final BDDExtendedAutomata bddExAutomata, final BDDExDisjEventDepSets eventParDepSets,
            final BDDExDisjVariableDepSets variableDepSet) {
        super(bddExAutomata, eventParDepSets);
        this.variableDepSet = variableDepSet;
        this.clusterList = new ArrayList<VariablesCluster>();
        
        initialComponentCandidates = new TIntHashSet();
        markedComponentCandidates = new TIntHashSet();
        uncontrollableComponentCandidates = new TIntHashSet();
        
        clusterToBDD = new TIntObjectHashMap<BDD>();
        clusterToForwardInfluencedClusters = new TIntObjectHashMap<TIntIntHashMap>();
        clusterToBackwardinfluencedClusters = new TIntObjectHashMap<TIntIntHashMap>();
        initialize();
    }

    @Override
    protected void initialize() {
        TIntObjectHashMap<BDD> varIndex2BDD = variableDepSet.getComponentToComponentTransMap();
        int[] varIndexAsKeys = varIndex2BDD.keys();
        
        // Get the first varIndex and construct a variable cluster
        int index = 0;
        VariablesCluster firstCluster = new VariablesCluster(index);
        firstCluster.add(varIndexAsKeys[index]);
        clusterList.add(firstCluster);
        
        boolean mergedIntoCluster = false;
        for(index=1; index < varIndexAsKeys.length; index++){
            mergedIntoCluster = false;
            int currVarIndex = varIndexAsKeys[index];
            BDD currBDD = varIndex2BDD.get(currVarIndex);
            
            for(VariablesCluster vc: clusterList){
                if(vc.varTransBDD.or(currBDD).nodeCount() <= Options.max_partition_size){
                    vc.add(currVarIndex);
                    mergedIntoCluster = true;
                    break;
                }
            }
            
            if(!mergedIntoCluster){
                VariablesCluster mvc = new VariablesCluster(clusterList.size());
                mvc.add(currVarIndex);
                clusterList.add(mvc);
            }
        }
        
        // set size 
        size = clusterList.size();
        
         // build the map from component to BDD
        for(final VariablesCluster vc: clusterList){
            clusterToBDD.put(vc.getId(), vc.getVarTransBDD());
        }
        
        // Two heuristic maps
        for (int i = 0; i < clusterList.size(); i++) {
            final VariablesCluster vc_i = clusterList.get(i);
            final TIntIntHashMap forwardInfluencedVarValueMap = vc_i.getForwradInfluencedVarValueMap();
            final TIntIntHashMap backwardInfluencedVarValueMap = vc_i.getBackwardInfluencedVarValueMap();
            final TIntIntHashMap forwardInfluencedClusterValueMap = new TIntIntHashMap();
            final TIntIntHashMap backwardInfluencedClusterValueMap = new TIntIntHashMap();
            for (int j = 0; j < clusterList.size(); j++) {
                if (j != i) {
                    final VariablesCluster vc_j = clusterList.get(j);
                    final TIntHashSet clusterVars = vc_j.getVarIndexSet();
                    int clusterForwardValue = 0;
                    int clusterBackwardValue = 0;
                    for (final TIntIterator itr = clusterVars.iterator(); itr.hasNext();) {
                        final int currInfluencedVarIndex = itr.next();
                        if (forwardInfluencedVarValueMap.containsKey(currInfluencedVarIndex)) {
                            clusterForwardValue += forwardInfluencedVarValueMap.get(currInfluencedVarIndex);
                        }
                        if (backwardInfluencedVarValueMap.containsKey(currInfluencedVarIndex)) {
                            clusterBackwardValue += backwardInfluencedVarValueMap.get(currInfluencedVarIndex);
                        }
                    }
                    forwardInfluencedClusterValueMap.put(vc_j.getId(), clusterForwardValue);
                    backwardInfluencedClusterValueMap.put(vc_j.getId(), clusterBackwardValue);
                }
            }
            clusterToForwardInfluencedClusters.put(vc_i.getId(), forwardInfluencedClusterValueMap);
            clusterToBackwardinfluencedClusters.put(vc_i.getId(), backwardInfluencedClusterValueMap);
        }
        
    }

    @Override
    public TIntObjectHashMap<BDD> getComponentToComponentTransMap() {
        return clusterToBDD;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getForwardComponentToInfluencedComponentMap() {
        return clusterToForwardInfluencedClusters;
    }

    @Override
    protected TIntObjectHashMap<TIntIntHashMap> getBackwardComponentToInfluencedComponentMap() {
        return clusterToBackwardinfluencedClusters;
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
        return getEventParDepSets().getUncontrollableTransitionRelationBDD();
    }

    class VariablesCluster {

        private final TIntHashSet varIndexSet;
        private BDD varTransBDD;
        private final int id;
        private final TIntIntHashMap forwardInfluencedVarValueMap;
        private final TIntIntHashMap backwardInfluencedVarValueMap;

        public VariablesCluster(final int id) {
            this.id = id;
            this.varIndexSet = new TIntHashSet();
            this.varTransBDD = variableDepSet.manager.getZeroBDD();
            this.forwardInfluencedVarValueMap = new TIntIntHashMap(variableDepSet.size);
            this.backwardInfluencedVarValueMap = new TIntIntHashMap(variableDepSet.size);
        }

        // Add a component. EventIndex will be added into the eventIndexSet, coressponding BDD will be merged.
        public void add(final int varIndex) {

            // Expand the cared variable set
            varIndexSet.add(varIndex);

            // BDD "or" with the corresponding BDD
            varTransBDD = varTransBDD.or(variableDepSet.getComponentToComponentTransMap().get(varIndex));

            // Update the qualified components
            if (variableDepSet.getInitialComponentCandidates().contains(varIndex) && !initialComponentCandidates.contains(id)) {
                initialComponentCandidates.add(id);
            }
            if (variableDepSet.getMarkedComponentCandidates().contains(varIndex) && !markedComponentCandidates.contains(id)) {
                markedComponentCandidates.add(id);
            }
            if (variableDepSet.getUncontrollableComponentCandidates().contains(varIndex) && !uncontrollableComponentCandidates.contains(id)) {
                uncontrollableComponentCandidates.add(id);
            }

            // Expand two heuristic maps.
            final TIntIntHashMap varIndexForwradInfluencedVarValueMap = variableDepSet.getForwardComponentToInfluencedComponentMap().get(varIndex);
            final TIntIntHashMap varIndexBackwardInfluencedVarValueMap = variableDepSet.getBackwardComponentToInfluencedComponentMap().get(varIndex);

            varIndexForwradInfluencedVarValueMap.forEachEntry(new TIntIntProcedure() {

                @Override
                public boolean execute(final int forInfluencedVarIndex, final int value) {
                    forwardInfluencedVarValueMap.adjustOrPutValue(forInfluencedVarIndex, forwardInfluencedVarValueMap.get(forInfluencedVarIndex) + value, value);
                    return true;
                }
            });

            varIndexBackwardInfluencedVarValueMap.forEachEntry(new TIntIntProcedure() {

                @Override
                public boolean execute(final int backInfluencedVarIndex, final int value) {
                    backwardInfluencedVarValueMap.adjustOrPutValue(backInfluencedVarIndex, backwardInfluencedVarValueMap.get(backInfluencedVarIndex) + value, value);
                    return true;
                }
            });
        }

        public TIntIntHashMap getBackwardInfluencedVarValueMap() {
            return backwardInfluencedVarValueMap;
        }

        public TIntHashSet getVarIndexSet() {
            return varIndexSet;
        }

        public BDD getVarTransBDD() {
            return varTransBDD;
        }

        public TIntIntHashMap getForwradInfluencedVarValueMap() {
            return forwardInfluencedVarValueMap;
        }

        public int getId() {
            return id;
        }
    }
}
