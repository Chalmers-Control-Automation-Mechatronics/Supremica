package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */

import gnu.trove.TIntArrayList;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDPairing;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.SynthesisAlgorithm;


public class BDDExtendedAutomaton {

    ExtendedAutomaton theExAutomaton;
    BDDExtendedAutomata bddExAutomata;
    BDDExtendedManager manager;

    BDDDomain sourceLocationDomain;
    BDDDomain destLocationDomain;

    BDDPairing sourceToDestPairing;
    BDDPairing destToSourcePairing;
    BDD edgeForwardBDD;
    BDD edgeBackwardBDD;
    BDD edgeForwardDisjunctiveBDD;
    BDD edgeBackwardDisjunctiveBDD;
    BDD forbiddenStateSet;
    BDD allowedStateSet;

    HashMap<Integer,String> bddIndex2SourceStateName;
    public HashMap<String,HashSet<Integer>> enablingSigmaMap;

    BDD selfLoopsBDD;
    TIntArrayList caredEventsIndex;
    BDDExDisjunctiveDependentSet dependentSet;

    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    String EQUALS = " = ";
    String NEQUALS = " != ";

    BDD initialLocation;
    BDD markedLocations;
    BDD forbiddenLocations;

    int nbrOfTerms;
    public boolean allwFrbdnChosen = false;

    public BDDExtendedAutomaton(final BDDExtendedAutomata bddExAutomata, final ExtendedAutomaton theExAutomaton)
    {
        this.bddExAutomata = bddExAutomata;
        this.theExAutomaton = theExAutomaton;
        this.sourceLocationDomain = bddExAutomata.getSourceLocationDomain(theExAutomaton.getName());
        this.destLocationDomain = bddExAutomata.getDestLocationDomain(theExAutomaton.getName());

        this.manager = bddExAutomata.getBDDManager();
        this.bddExAutomata = bddExAutomata;
        this.theExAutomaton = theExAutomaton;

        bddIndex2SourceStateName = new HashMap<Integer,String>();
        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();

        sourceToDestPairing = manager.makePairing(sourceLocationDomain, destLocationDomain);
        destToSourcePairing = manager.makePairing(destLocationDomain, sourceLocationDomain);

        edgeForwardBDD = manager.getZeroBDD();
        edgeBackwardBDD = manager.getZeroBDD();

        edgeForwardDisjunctiveBDD = manager.getZeroBDD();
        edgeBackwardDisjunctiveBDD = manager.getZeroBDD();

        selfLoopsBDD = manager.getZeroBDD();
        caredEventsIndex = new TIntArrayList();
        dependentSet = null;
    }

    public void initialize()
    {
        initialLocation = manager.getZeroBDD();
        markedLocations = manager.getZeroBDD();
        forbiddenLocations = manager.getZeroBDD();
        final BDD plantifiedBlockedLocation = manager.getZeroBDD();

        final BDD tempMarkedLocations = manager.getZeroBDD();

        final List<EventDeclProxy> inverseAlphabet = bddExAutomata.getInverseAlphabet(theExAutomaton);

        final HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap = theExAutomaton.getLocationToOutgoingEdgesMap();

        boolean anyMarkedLocation = false;

        int nbrOfEdges = 0;
        int nbrOfGuards = 0;
        for (final NodeProxy currLocation : theExAutomaton.getNodes()) {
            if (bddExAutomata.synType.equals(SynthesisAlgorithm.PARTITIONBDD)) {
                // Add the node proxy in the map
                for (final Iterator<EdgeSubject> edgeIt = locationToOutgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext();) {
                    final EdgeSubject currEdge = edgeIt.next();
                        if (currEdge.getGuardActionBlock() != null && currEdge.getGuardActionBlock().getGuards() != null
                        && currEdge.getGuardActionBlock().getGuards().size() > 0) {
                            nbrOfGuards++;
                        }
                    addNodeProxy(currEdge);
                    nbrOfEdges++;
                }
                // Construct "keep BDD"
                addKeep(currLocation);
            } else {
                // First create all edges in this automaton
                for (final Iterator<EdgeSubject> edgeIt = locationToOutgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext();) {
                    final EdgeSubject currEdge = edgeIt.next();
                    addEdge(currEdge);
                }

                // Self loop events not in this alphabet
                for (final EventDeclProxy event : inverseAlphabet) {
                    addEdge(currLocation, currLocation, event);
                }
            }

            // Then add state properties
            final int locationIndex = bddExAutomata.getLocationIndex(theExAutomaton, currLocation);
            manager.addLocation(tempMarkedLocations, locationIndex, sourceLocationDomain);
            if (theExAutomaton.isLocationInitial(currLocation))
            {
               manager.addLocation(initialLocation, locationIndex, sourceLocationDomain);
            }
            if (theExAutomaton.isLocationAccepted(currLocation))
            {
                anyMarkedLocation = true;
                manager.addLocation(markedLocations, locationIndex, sourceLocationDomain);
            }
            if (theExAutomaton.isLocationForbidden(currLocation))
            {
               manager.addLocation(forbiddenLocations, locationIndex, sourceLocationDomain);
            }
            if(theExAutomaton.isLocationPlantifiedBlocked(currLocation))
            {
               manager.addLocation(plantifiedBlockedLocation, locationIndex, sourceLocationDomain);
            }
        }
        if(!anyMarkedLocation)
        {
            markedLocations = tempMarkedLocations.id();
        }

        bddExAutomata.addInitialLocations(initialLocation);
        bddExAutomata.addMarkedLocations(markedLocations);
        bddExAutomata.addForbiddenLocations(forbiddenLocations);
        bddExAutomata.addPlantifiedBlockedLocations(plantifiedBlockedLocation);

        if (bddExAutomata.synType.equals(SynthesisAlgorithm.PARTITIONBDD)) {
            bddExAutomata.automaton2nbrEdges.put(theExAutomaton, nbrOfEdges);
            bddExAutomata.automaton2nbrGuards.put(theExAutomaton, nbrOfGuards);
        }
    }

    public ExtendedAutomaton getExAutomaton()
    {
        return theExAutomaton;
    }

    public BDD getInitialLocation()
    {
        return initialLocation;
    }

    public BDD getMarkedLocations()
    {
        return markedLocations;
    }

    public BDD getForbiddenLocations()
    {
        return forbiddenLocations;
    }

    void addEdge(final EdgeProxy theEdge)
    {
        final NodeProxy sourceLocation = theEdge.getSource();
        final NodeProxy destLocation = theEdge.getTarget();
//        ListSubject<AbstractSubject> theEvent = theEdge.getLabelBlock().getEventListModifiable();
        final Iterator<Proxy> eventIterator = theEdge.getLabelBlock().getEventList().iterator();
        while(eventIterator.hasNext())
        {
//            String eventName = ((SimpleIdentifierSubject)theEdge.getLabelBlock().getEventList().iterator().next()).getName();
            final String eventName = ((SimpleIdentifierSubject)eventIterator.next()).getName();
            final EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);

            // Add all states that could be reach by only unobservable events including the destState
            for (final NodeProxy epsilonState : epsilonClosure(destLocation,true))
            {
                if(theEdge.getGuardActionBlock() != null)
                    addEdge(sourceLocation, epsilonState, theEvent, theEdge.getGuardActionBlock().getGuards(),theEdge.getGuardActionBlock().getActions());
                else
                    addEdge(sourceLocation, epsilonState, theEvent);
            }
        }
    }

    void addEdge(final NodeProxy sourceLocation, final NodeProxy destLocation, final EventDeclProxy theEvent)
    {
        addEdge(sourceLocation, destLocation, theEvent, null,null);
    }

    void addEdge(final NodeProxy sourceLocation, final NodeProxy destLocation, final EventDeclProxy theEvent, final List<SimpleExpressionProxy> guards, final List<BinaryExpressionProxy> actions)
    {
//        System.out.println("Edge belonging to "+theExAutomaton.getName()+": "+sourceLocation.getName()+"  "+theEvent.getName()+"  "+destLocation.getName());
        final int sourceLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, sourceLocation);
        final int destLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, destLocation);
        final int eventIndex = bddExAutomata.getEventIndex(theEvent);

        final BDD sourceBDD = manager.getFactory().buildCube(sourceLocationIndex, sourceLocationDomain.vars());

        Integer bddIndex = -1;
        if(!bddIndex2SourceStateName.containsValue(sourceLocation.getName()))
        {

            final BDD.BDDIterator satIt = new BDD.BDDIterator(sourceBDD, sourceLocationDomain.set());
            final BigInteger[] currSat = satIt.nextTuple();
            for(int i=0; i<currSat.length;i++)
            {
                if(currSat[i] != null)
                {
                     bddIndex = currSat[i].intValue();
                     break;
                }
            }

            bddIndex2SourceStateName.put(bddIndex,sourceLocation.getName());
        }
	manager.addEdge(edgeForwardBDD, bddExAutomata.getForwardTransWhereVisUpdated(this), bddExAutomata.getForwardTransAndNextValsForV(this), sourceLocationIndex, sourceLocationDomain, destLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions);
    }

    private void addKeep(final NodeProxy location){

        final int locationIndex = bddExAutomata.getLocationIndex(theExAutomaton, location);

        final BDD sourceLocationBDD = manager.getFactory().buildCube(locationIndex, sourceLocationDomain.vars());
        Integer bddIndex = -1;
        if (!bddIndex2SourceStateName.containsValue(location.getName())) {
            final BDD.BDDIterator satIt = new BDD.BDDIterator(sourceLocationBDD, sourceLocationDomain.set());
            final BigInteger[] currSat = satIt.nextTuple();
            for (int i = 0; i < currSat.length; i++) {
                if (currSat[i] != null) {
                    bddIndex = currSat[i].intValue();
                    break;
                }
            }
            bddIndex2SourceStateName.put(bddIndex, location.getName());
        }

        final BDD destLocationBDD = manager.getFactory().buildCube(locationIndex, destLocationDomain.vars());

        sourceLocationBDD.andWith(destLocationBDD);
        selfLoopsBDD.orWith(sourceLocationBDD);
    }


    private void addNodeProxy(final EdgeSubject theEdge) {

        final Iterator<Proxy> eventIterator = theEdge.getLabelBlock().getEventList().iterator();
        while (eventIterator.hasNext()) {

            final String eventName = ((SimpleIdentifierSubject) eventIterator.next()).getName();
            final EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);
            final int eventIndex = bddExAutomata.getEventIndex(theEvent);

            caredEventsIndex.add(eventIndex);

            if(theEvent.getKind() == EventKind.UNCONTROLLABLE){
                if(theExAutomaton.isSpecification())
                    bddExAutomata.specUncontrollableEventIndexList.add(eventIndex);
                else{
                    bddExAutomata.plantUncontrollableEventIndexList.add(eventIndex);
                }
            }

            final HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>> currentEventsAutEdgeMap = bddExAutomata.event2AutomatonsEdges.get(eventIndex);
            if (currentEventsAutEdgeMap.containsKey(theExAutomaton)) {
                currentEventsAutEdgeMap.get(theExAutomaton).add(theEdge);
            } else {
                final ArrayList<EdgeProxy> edgeList = new ArrayList<EdgeProxy>();
                edgeList.add(theEdge);
                currentEventsAutEdgeMap.put(theExAutomaton, edgeList);
            }
        }
    }

    public TIntArrayList getCaredEventsIndex() {
        return caredEventsIndex;
    }

    public BDD getSelfLoopsBDD() {
        return selfLoopsBDD;
    }

    public BDDExDisjunctiveDependentSet getDependentSet() {
        if (dependentSet == null) {
            dependentSet = new BDDExDisjunctiveDependentSet(manager, this);
        }
        return dependentSet;
    }

    public BDD getForbiddenStateSet()
    {
        return forbiddenStateSet;
    }

    public BDD getAllowedStateSet()
    {
        return allowedStateSet;
    }

    public BDD getEdgeForwardBDD()
    {
        return edgeForwardBDD;
    }

    public BDD getEdgeBackwardBDD()
    {
        return edgeBackwardBDD;
    }

    public ArrayList<String> getComplementLocationNames(final ArrayList<String> locationNames)
    {
        final ArrayList<String> output = new ArrayList<String>();
        for(final NodeProxy location: theExAutomaton.getNodes())
        {
            if(!locationNames.contains(location.getName()))
                output.add(location.getName());
        }
        return output;
    }

    public Set<NodeProxy> epsilonClosure(final NodeProxy thisLocation, final boolean includeSelf)
    {
        final Set<NodeProxy> result = new TreeSet<NodeProxy>();

        // Include self?
        if (includeSelf)
        {
            result.add(thisLocation);
        }

        // Examine states
        final LinkedList<NodeProxy> statesToExamine = new LinkedList<NodeProxy>();
        statesToExamine.add(thisLocation);
        while (statesToExamine.size() != 0)
        {
            final NodeProxy currLocation = statesToExamine.removeFirst();

            final HashMap<NodeProxy,ArrayList<EdgeSubject>> outgoingEdgesMap = theExAutomaton.getLocationToOutgoingEdgesMap();

            for (final Iterator<EdgeSubject> edgeIt = outgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext(); )
            {
                final EdgeSubject currEdge = edgeIt.next();
                final NodeProxy state = currEdge.getTarget();

                // Is this an epsilon event that we care about?
                final String eventName = ((SimpleIdentifierSubject)currEdge.getLabelBlock().getEventListModifiable().iterator().next()).getName();
                final EventDeclProxy currEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);

                if (!currEvent.isObservable() && (currEdge.getSource() != currEdge.getTarget()) && !result.contains(state) )
                {
                    statesToExamine.add(state);
                    result.add(state);
                }
            }
        }

        return result;
    }

}
