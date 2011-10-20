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
 * The class groups an amount of event based BDD partitions, in order to make
 * the fix-point iteration algorithm efficient.
 *
 * @author zhennan
 * @version 1.0
 */

public final class BDDExDisjEventClusters extends BDDExDisjDepSetsDecorator{

    private final List<EventsCluster> clusterList;
    private final TIntHashSet initialComponentCandidates;
    private final TIntHashSet markedComponentCandidates;
    private final TIntHashSet uncontrollableComponentCandidates;
    private final  TIntObjectHashMap<BDD> clusterToBDD;
    TIntObjectHashMap<TIntIntHashMap> clusterToForwardInfluencedClusters;
    TIntObjectHashMap<TIntIntHashMap> clusterToBackwardinfluencedClusters;

    public BDDExDisjEventClusters(final BDDExtendedAutomata bddExAutomata, final BDDExDisjEventDepSets eventParDepSets) {
        super(bddExAutomata, eventParDepSets);
        clusterList = new ArrayList<EventsCluster>();
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

        final TIntObjectHashMap<BDD> event2BDD = eventParDepSets.getComponentToComponentTransMap();
        final int[] eventIndexAsKeys =event2BDD .keys();

        // Get the first eventIndex and construct a cluster
        int index = 0;
        final EventsCluster firstCluster = new EventsCluster(index);
        firstCluster.add(eventIndexAsKeys[index]);
        clusterList.add(firstCluster);

        boolean mergedIntoCluster = false;
        for( index = 1; index < eventIndexAsKeys.length; index++){
            mergedIntoCluster = false;
            final int currEventIndex = eventIndexAsKeys[index];
            final BDD currBDD = event2BDD.get(currEventIndex);
            for(final EventsCluster anEvCluster: clusterList){
                if(anEvCluster.eventsBDD.or(currBDD).nodeCount() <= Options.max_partition_size){
                     anEvCluster.add(currEventIndex);
                     mergedIntoCluster = true;
                     break;
                }
            }
            if(!mergedIntoCluster){
                // Create a new cluster and set the id
                final EventsCluster ec = new EventsCluster(clusterList.size());
                ec.add(currEventIndex);
                clusterList.add(ec);
            }
        }

        // set size
        size = clusterList.size();

        // build the map from component to BDD
        for(final EventsCluster ec: clusterList){
            clusterToBDD.put(ec.getId(), ec.getEventsBDD());
        }

        // Two heuristic maps
        for (int i = 0; i < clusterList.size(); i++) {
            final EventsCluster ec_i = clusterList.get(i);
            final TIntIntHashMap forwardInfluencedEventValueMap = ec_i.getForwradInfluencedEventValueMap();
            final TIntIntHashMap backwardInfluencedEventValueMap = ec_i.getBackwardInfluencedEventValueMap();
            final TIntIntHashMap forwardInfluencedClusterValueMap = new TIntIntHashMap();
            final TIntIntHashMap backwardInfluencedClusterValueMap = new TIntIntHashMap();
            for (int j = 0; j < clusterList.size(); j++) {
                if (j != i) {
                    final EventsCluster ec_j = clusterList.get(j);
                    final TIntHashSet clusterEvents = ec_j.getEventIndexSet();
                    int clusterForwardValue = 0;
                    int clusterBackwardValue = 0;
                    for (final TIntIterator itr = clusterEvents.iterator(); itr.hasNext();) {
                        final int currInfluencedEventIndex = itr.next();
                        if (forwardInfluencedEventValueMap.containsKey(currInfluencedEventIndex)) {
                            clusterForwardValue += forwardInfluencedEventValueMap.get(currInfluencedEventIndex);
                        }
                        if (backwardInfluencedEventValueMap.containsKey(currInfluencedEventIndex)) {
                            clusterBackwardValue += backwardInfluencedEventValueMap.get(currInfluencedEventIndex);
                        }
                    }
                    forwardInfluencedClusterValueMap.put(ec_j.getId(), clusterForwardValue);
                    backwardInfluencedClusterValueMap.put(ec_j.getId(), clusterBackwardValue);
                }
            }
            clusterToForwardInfluencedClusters.put(ec_i.getId(), forwardInfluencedClusterValueMap);
            clusterToBackwardinfluencedClusters.put(ec_i.getId(), backwardInfluencedClusterValueMap);
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
        return eventParDepSets.getUncontrollableTransitionRelationBDD();
    }

    class EventsCluster{

        private final TIntHashSet eventIndexSet;
        private BDD eventsBDD;
        private final int id;
        private final TIntIntHashMap forwradInfluencedEventValueMap;
        private final TIntIntHashMap backwardInfluencedEventValueMap;

        public EventsCluster(final int id){
            this.id = id;
            this.eventIndexSet = new TIntHashSet();
            this.eventsBDD = eventParDepSets.manager.getZeroBDD();
            this.forwradInfluencedEventValueMap = new TIntIntHashMap(eventParDepSets.size);
            this.backwardInfluencedEventValueMap = new TIntIntHashMap(eventParDepSets.size);
        }

        // Add a component. EventIndex will be added into the eventIndexSet, coressponding BDD will be merged.
        public void add(final int eventIndex){

            // Expand the cared alphabet set
            eventIndexSet.add(eventIndex);

            // BDD "or" with the corresponding BDD
            eventsBDD = eventsBDD.or(eventParDepSets.getComponentToComponentTransMap().get(eventIndex));

            // Update the qualified components
            if(eventParDepSets.getInitialComponentCandidates().contains(eventIndex) && !initialComponentCandidates.contains(id))
                initialComponentCandidates.add(id);
            if(eventParDepSets.getMarkedComponentCandidates().contains(eventIndex) && !markedComponentCandidates.contains(id))
                markedComponentCandidates.add(id);
            if(eventParDepSets.getUncontrollableComponentCandidates().contains(eventIndex) && !uncontrollableComponentCandidates.contains(id))
                uncontrollableComponentCandidates.add(id);

            // Expand two heuristic maps.
            final TIntIntHashMap eventIndexForwradInfluencedEventValueMap
                    = eventParDepSets.getForwardComponentToInfluencedComponentMap().get(eventIndex);
            final TIntIntHashMap eventIndexBackwardInfluencedEventValueMap
                    = eventParDepSets.getBackwardComponentToInfluencedComponentMap().get(eventIndex);

            // Update the forwradInfluencedEventValueMap and backwradInfluencedEventValueMap
            eventIndexForwradInfluencedEventValueMap.forEachEntry(new TIntIntProcedure() {
                @Override
                public boolean execute(final int forInfluencedEventIndex, final int value) {
                    forwradInfluencedEventValueMap
                            .adjustOrPutValue(forInfluencedEventIndex, forwradInfluencedEventValueMap.get(forInfluencedEventIndex) + value, value);
                    return true;
                }
            });

            eventIndexBackwardInfluencedEventValueMap.forEachEntry(new TIntIntProcedure() {
                @Override
                public boolean execute(final int backInfluencedEventIndex, final int value) {
                    backwardInfluencedEventValueMap
                            .adjustOrPutValue(backInfluencedEventIndex, backwardInfluencedEventValueMap.get(backInfluencedEventIndex) + value, value);
                    return true;
                }
            });
        }

        public TIntIntHashMap getBackwardInfluencedEventValueMap() {
            return backwardInfluencedEventValueMap;
        }

        public TIntHashSet getEventIndexSet() {
            return eventIndexSet;
        }

        public BDD getEventsBDD() {
            return eventsBDD;
        }

        public TIntIntHashMap getForwradInfluencedEventValueMap() {
            return forwradInfluencedEventValueMap;
        }

        public int getId() {
            return id;
        }
    }
}
