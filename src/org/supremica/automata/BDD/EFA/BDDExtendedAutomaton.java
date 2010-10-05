package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import java.math.BigInteger;
import java.util.ArrayList;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
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
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;


import org.supremica.automata.ExtendedAutomaton;

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

    public BDDExtendedAutomaton(BDDExtendedAutomata bddExAutomata, ExtendedAutomaton theExAutomaton)
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
    }

    public void initialize()
    {
        initialLocation = manager.getZeroBDD();
        markedLocations = manager.getZeroBDD();
        forbiddenLocations = manager.getZeroBDD();
        BDD plantifiedBlockedLocation = manager.getZeroBDD();

        BDD tempMarkedLocations = manager.getZeroBDD();

        List<EventDeclProxy> inverseAlphabet = bddExAutomata.getInverseAlphabet(theExAutomaton);

        HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap = theExAutomaton.getLocationToOutgoingEdgesMap();

        boolean anyMarkedLocation = false;
        for (NodeProxy currLocation : theExAutomaton.getNodes())
        {
            // First create all edges in this automaton
            for (Iterator<EdgeSubject> edgeIt = locationToOutgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext(); )
            {
                EdgeSubject currEdge = edgeIt.next();
                addEdge(currEdge);
            }

            // Self loop events not in this alphabet
            for (EventDeclProxy event : inverseAlphabet)
            {
               addEdge(currLocation, currLocation, event);
            }

            // Then add state properties
            int locationIndex = bddExAutomata.getLocationIndex(theExAutomaton, currLocation);
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

    void addEdge(EdgeProxy theEdge)
    {
        NodeProxy sourceLocation = theEdge.getSource();
        NodeProxy destLocation = theEdge.getTarget();
//        ListSubject<AbstractSubject> theEvent = theEdge.getLabelBlock().getEventListModifiable();
        Iterator<Proxy> eventIterator = theEdge.getLabelBlock().getEventList().iterator();
        while(eventIterator.hasNext())
        {
//            String eventName = ((SimpleIdentifierSubject)theEdge.getLabelBlock().getEventList().iterator().next()).getName();
            String eventName = ((SimpleIdentifierSubject)eventIterator.next()).getName();
            EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);


            // Add all states that could be reach by only unobservable events including the destState
            for (NodeProxy epsilonState : epsilonClosure(destLocation,true))
            {
                if(theEdge.getGuardActionBlock() != null)
                    addEdge(sourceLocation, epsilonState, theEvent, theEdge.getGuardActionBlock().getGuards(),theEdge.getGuardActionBlock().getActions());
                else
                    addEdge(sourceLocation, epsilonState, theEvent);
            }
        }
    }

    void addEdge(NodeProxy sourceLocation, NodeProxy destLocation, EventDeclProxy theEvent)
    {
        addEdge(sourceLocation, destLocation, theEvent, null,null);
    }

    void addEdge(NodeProxy sourceLocation, NodeProxy destLocation, EventDeclProxy theEvent, List<SimpleExpressionProxy> guards, List<BinaryExpressionProxy> actions)
    {
//        System.out.println("Edge belonging to "+theExAutomaton.getName()+": "+sourceLocation.getName()+"  "+theEvent.getName()+"  "+destLocation.getName());
        int sourceLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, sourceLocation);
        int destLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, destLocation);
        int eventIndex = bddExAutomata.getEventIndex(theEvent);

        BDD sourceBDD = manager.getFactory().buildCube(sourceLocationIndex, sourceLocationDomain.vars());

        Integer bddIndex = -1;
        if(!bddIndex2SourceStateName.containsValue(sourceLocation.getName()))
        {

            BDD.BDDIterator satIt = new BDD.BDDIterator(sourceBDD, sourceLocationDomain.set());
            BigInteger[] currSat = satIt.nextTuple();
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

        manager.addEdge(edgeForwardBDD, bddExAutomata.getForwardTransWhereVisUpdated(this), bddExAutomata.getForwardTransAndNextValsForV(this), sourceLocationIndex, sourceLocationDomain, destLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions,
                sourceLocation.getName(), destLocation.getName());
//        BDDExtendedManager.addEdge(edgeBackwardBDD, bddExAutomata.getBackwardTransWhereVisUpdated(this), bddExAutomata.getBackwardTransAndNextValsForV(this), destLocationIndex, sourceLocationDomain, sourceLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions, bddExAutomata.BDDBitVecTargetVarsMap, bddExAutomata.BDDBitVecSourceVarsMap,bddExAutomata.orgExAutomata.getMinMaxValMap());
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

    public ArrayList<String> getComplementLocationNames(ArrayList<String> locationNames)
    {
        ArrayList<String> output = new ArrayList<String>();
        for(NodeProxy location: getExAutomaton().getNodes())
        {
            if(!locationNames.contains(location.getName()))
                output.add(location.getName());
        }
        return output;
    }

    public Set<NodeProxy> epsilonClosure(NodeProxy thisLocation, boolean includeSelf)
    {
        Set<NodeProxy> result = new TreeSet<NodeProxy>();

        // Include self?
        if (includeSelf)
        {
            result.add(thisLocation);
        }

        // Examine states
        LinkedList<NodeProxy> statesToExamine = new LinkedList<NodeProxy>();
        statesToExamine.add(thisLocation);
        while (statesToExamine.size() != 0)
        {
            NodeProxy currLocation = statesToExamine.removeFirst();

            HashMap<NodeProxy,ArrayList<EdgeSubject>> outgoingEdgesMap = theExAutomaton.getLocationToOutgoingEdgesMap();

            for (Iterator<EdgeSubject> edgeIt = outgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext(); )
            {
                EdgeSubject currEdge = edgeIt.next();
                NodeProxy state = currEdge.getTarget();

                // Is this an epsilon event that we care about?
                String eventName = ((SimpleIdentifierSubject)currEdge.getLabelBlock().getEventListModifiable().iterator().next()).getName();
                EventDeclProxy currEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);

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
