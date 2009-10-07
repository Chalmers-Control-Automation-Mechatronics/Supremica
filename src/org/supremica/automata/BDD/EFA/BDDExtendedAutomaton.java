package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import java.math.BigInteger;
import net.sf.javabdd.*;
import java.util.*;
import java.util.ArrayList;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.BinaryExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;

import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.*;

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
    BDD selfLoopsBDD;
    BDD uncontrollableEventsBDD;

    HashMap<Integer,String> bddIndex2SourceStateName;
    public HashMap<String,HashSet<Integer>> enablingSigmaMap;

    String OR = " | ";
    String AND = " & ";
    String O_PAR = "(";
    String C_PAR = ")";
    String EQUALS = " = ";
    String NEQUALS = " != ";

    boolean isTransSelfLoop = false;

    BDD initialLocation;
    BDD markedLocations;
    BDD forbiddenLocations;

    int nbrOfTerms;
    public boolean allwFrbdnChosen = false;

    public BDDExtendedAutomaton(BDDExtendedAutomata bddExAutomata, ExtendedAutomaton theExAutomaton)
    {
        this.bddExAutomata = bddExAutomata;
        this.theExAutomaton = theExAutomaton;
        this.sourceLocationDomain = bddExAutomata.getSourceLocationDomain(theExAutomaton);
        this.destLocationDomain = bddExAutomata.getDestLocationDomain(theExAutomaton);

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

        uncontrollableEventsBDD = manager.getZeroBDD();
        selfLoopsBDD = manager.getZeroBDD();
    }

    public void initialize()
    {
        initialLocation = manager.getZeroBDD();
        markedLocations = manager.getZeroBDD();
        forbiddenLocations = manager.getZeroBDD();

        BDD tempMarkedLocations = manager.getZeroBDD();

        List<EventDeclProxy> inverseAlphabet = bddExAutomata.getInverseAlphabet(theExAutomaton);

        HashMap<NodeProxy,ArrayList<EdgeSubject>> locationToOutgoingEdgesMap = theExAutomaton.getLocationToOutgoingEdgesMap();

        boolean anyMarkedLocation = false;
        for (NodeProxy currLocation : theExAutomaton.getNodes())
        {
            isTransSelfLoop = false;
            // First create all edges in this automaton
            for (Iterator<EdgeSubject> edgeIt = locationToOutgoingEdgesMap.get(currLocation).iterator(); edgeIt.hasNext(); )
            {
                EdgeSubject currEdge = edgeIt.next();
                addEdge(currEdge);
            }

            isTransSelfLoop = true;
            // Self loop events not in this alphabet
            for (EventDeclProxy event : inverseAlphabet)
            {
               addEdge(currLocation, currLocation, event, null,null);
            }

            // Then add state properties
            int locationIndex = bddExAutomata.getLocationIndex(theExAutomaton, currLocation);
            BDDExtendedManager.addLocation(tempMarkedLocations, locationIndex, sourceLocationDomain);
            if (theExAutomaton.isLocationInitial(currLocation))
            {
               BDDExtendedManager.addLocation(initialLocation, locationIndex, sourceLocationDomain);
            }
            if (theExAutomaton.isLocationAccepted(currLocation))
            {
                anyMarkedLocation = true;
                BDDExtendedManager.addLocation(markedLocations, locationIndex, sourceLocationDomain);
            }
            if (theExAutomaton.isLocationForbidden(currLocation))
            {
               BDDExtendedManager.addLocation(forbiddenLocations, locationIndex, sourceLocationDomain);
            }
        }
        if(!anyMarkedLocation)
        {
            markedLocations = tempMarkedLocations.id();
        }

        Iterator <EventDeclProxy> eventItr = theExAutomaton.getAlphabet().iterator();
        BDD sigmaBDD;
        int currEventIndex;
        while(eventItr.hasNext())
        {
            EventDeclProxy event = eventItr.next();
            currEventIndex = bddExAutomata.getEventIndex(event);
            sigmaBDD = manager.createBDD(currEventIndex, bddExAutomata.getEventDomain());
            if(event.getKind() != EventKind.CONTROLLABLE)
                uncontrollableEventsBDD.orWith(sigmaBDD);
        }

        bddExAutomata.addInitialLocations(initialLocation);
        bddExAutomata.addMarkedLocations(markedLocations);
        bddExAutomata.addForbiddenLocations(forbiddenLocations);
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
        String eventName = ((SimpleIdentifierSubject)theEdge.getLabelBlock().getEventList().iterator().next()).getName();
        EventDeclProxy theEvent = bddExAutomata.getExtendedAutomata().eventIdToProxy(eventName);


        // Add all states that could be reach by only unobservable events including the destState
        for (NodeProxy epsilonState : epsilonClosure(destLocation,true))
        {
            addEdge(sourceLocation, epsilonState, theEvent, theEdge.getGuardActionBlock().getGuards(),theEdge.getGuardActionBlock().getActions());
        }
    }

    void addEdge(NodeProxy sourceLocation, NodeProxy destLocation, EventDeclProxy theEvent, List<SimpleExpressionProxy> guards, List<BinaryExpressionProxy> actions)
    {
        int sourceLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, sourceLocation);
        int destLocationIndex = bddExAutomata.getLocationIndex(theExAutomaton, destLocation);
        int eventIndex = bddExAutomata.getEventIndex(theEvent);

//        System.out.println("state name: "+sourceState.getName());
//        System.out.println("sourceStateIndex: "+sourceStateIndex);

        BDD sourceBDD = manager.factory.buildCube(sourceLocationIndex, sourceLocationDomain.vars());

        Integer bddIndex = -1;
//        String varsBits = "";
        if(!bddIndex2SourceStateName.containsValue(sourceLocation.getName()))
        {
//            sourceBDD.printDot();

            BDD.BDDIterator satIt = new BDD.BDDIterator(sourceBDD, sourceLocationDomain.set());
            BigInteger[] currSat = satIt.nextTuple();
//            System.out.println("currSat: " + ArrayHelper.arrayToString(currSat));
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

        if(isTransSelfLoop)
        {
              BDDExtendedManager.addEdge(selfLoopsBDD, bddExAutomata.getForwardTransWhereVisUpdated(this), bddExAutomata.getForwardTransAndNextValsForV(this), sourceLocationIndex, sourceLocationDomain, destLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions, bddExAutomata.BDDBitVecSourceVarsMap, bddExAutomata.BDDBitVecTargetVarsMap);
        }
        
        BDDExtendedManager.addEdge(edgeForwardBDD, bddExAutomata.getForwardTransWhereVisUpdated(this), bddExAutomata.getForwardTransAndNextValsForV(this), sourceLocationIndex, sourceLocationDomain, destLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions, bddExAutomata.BDDBitVecSourceVarsMap, bddExAutomata.BDDBitVecTargetVarsMap);
//        BDDExtendedManager.addEdge(edgeBackwardBDD, bddExAutomata.getBackwardTransWhereVisUpdated(this), bddExAutomata.getBackwardTransAndNextValsForV(this), destLocationIndex, sourceLocationDomain, sourceLocationIndex, destLocationDomain, eventIndex, bddExAutomata.getEventDomain(), guards, actions, bddExAutomata.BDDBitVecTargetVarsMap, bddExAutomata.BDDBitVecSourceVarsMap);
    }

    public BDD getSelfLoopsBDD(){
        return selfLoopsBDD;
    }

    public BDD getForbiddenStateSet()
    {
        return forbiddenStateSet;
    }

    public BDD getAllowedStateSet()
    {
        return allowedStateSet;
    }

    public BDD getUncontrollableEvents()
    {
        return uncontrollableEventsBDD;
    }

    public BDD getEdgeForwardBDD()
    {
        return edgeForwardBDD;
    }
    
    public BDD getEdgeBackwardBDD()
    {
        return edgeBackwardBDD;
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
        LinkedList statesToExamine = new LinkedList();
        statesToExamine.add(thisLocation);
        while (statesToExamine.size() != 0)
        {
            NodeProxy currLocation = (NodeProxy) statesToExamine.removeFirst();

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
