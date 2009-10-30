package org.supremica.automata.BDD.EFA;

/**
 *
 * @author sajed
 */

import java.util.*;

import net.sf.javabdd.*;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;

import org.supremica.automata.BDD.*;
import org.supremica.log.*;
import org.supremica.automata.*;

public class BDDExtendedAutomata implements Iterable<BDDExtendedAutomaton>{

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);

    BDDExtendedManager manager;
    List<ExtendedAutomaton> theExAutomata;
    ExtendedAutomata orgExAutomata;
    List<BDDExtendedAutomaton> theBDDAutomataList = new LinkedList<BDDExtendedAutomaton>();
    Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap = new HashMap<ExtendedAutomaton, BDDExtendedAutomaton>();
    public Map<Integer, String> bddVar2AutName = new HashMap<Integer, String>();
    public HashMap<String,HashSet<Integer>> enablingSigmaMap;

    ExtendedAutomataIndexMap theIndexMap;
    List<EventDeclProxy> unionAlphabet;

    BDDEdges bddEdges = null;

    BDDDomain eventDomain;
    BDDDomain constantDomain;

    BDDDomain[] sourceStateDomains = null;
    BDDDomain[] destStateDomains = null;

    BDDVarSet sourceStateVariables = null;
    BDDVarSet destStateVariables = null;

    BDDDomain[] tempLocationDomains = null;
    BDDDomain[] sourceLocationDomains = null;
    BDDDomain[] destLocationDomains = null;

    BDDVarSet sourceLocationVariables = null;
    BDDVarSet destLocationVariables = null;

    BDDVarSet sourceVariablesVarSet = null;
    BDDVarSet destVariablesVarSet = null;

    BDDVarSet constantsVarSet = null;

    BDDDomain[] tempVarDomains = null;
    BDDDomain[] sourceVarDomains = null;
    BDDDomain[] destVarDomains = null;

    BDDPairing destToSourceLocationPairing = null;
    BDDPairing destToSourceVariablePairing = null;

    BDDPairing sourceToTempLocationPairing = null;
    BDDPairing tempToDestLocationPairing = null;

    BDDPairing sourceToTempVariablePairing = null;
    BDDPairing tempToDestVariablePairing = null;


    BDD initialLocationsBDD = null;
    BDD markedLocationsBDD = null;
    BDD forbiddenLocationsBDD = null;
    BDD uncontrollableStatesBDD = null;

    BDD reachableStatesBDD = null;
    BDD coreachableStatesBDD = null;
    BDD reachableAndCoreachableStatesBDD = null;
    BDD safeStatesBDD = null;

    BDD uncontrollableEventsBDD = null;

    BDD plantsForwardTransitions = null;
    BDD specsForwardTransitions = null;

    BDD plantsBackwardTransitions = null;
    BDD specsBackwardTransitions = null;

    BDD plantsUncontrollableEvents = null;
    BDD specsUncontrollableEvents = null;

    double nbrOfReachableStates = -1;
    double nbrOfCoreachableStates = -1;
    double nbrOfReachableAndCoreachableStates = -1;
    double nbrOfBlockingStates = -1;
    double nbrOfSafeStates = -1;

//    BDD selfLoopsBDD;
    BDD plantsSelfLoopsBDD;
    BDD specsSelfLoopsBDD;
    HashMap<String,BDDBitVector> BDDBitVecSourceVarsMap;
    HashMap<String,BDDBitVector> BDDBitVecTargetVarsMap;
    int maxBitsRequired = 0;

    HashSet<String> initValuesStringSet = null;
    BDD initValuesBDD = null;

    BDD[][] forwardTransWhereVisUpdated;
    BDD[][] forwardTransAndNextValsForV;

    BDD[][] backwardTransWhereVisUpdated;
    BDD[][] backwardTransAndNextValsForV;


    public BDDExtendedAutomata(ExtendedAutomata orgExAutomata)
    {
        this.orgExAutomata = orgExAutomata;
        theIndexMap = new ExtendedAutomataIndexMap(orgExAutomata);
        AutomataSorter automataSorter = new PCGAutomataSorter();

        Automata theAutomata = automataSorter.sortAutomata(removeGuardsActionsFromEFAs(orgExAutomata));
        theExAutomata = new ArrayList<ExtendedAutomaton>();
        for(Automaton automaton:theAutomata)
        {
            theExAutomata.add(theIndexMap.getExAutomatonWithName(automaton.getName()));
        }

        BDDBitVecSourceVarsMap = new HashMap<String, BDDBitVector>(orgExAutomata.getVars().size());
        BDDBitVecTargetVarsMap = new HashMap<String, BDDBitVector>(orgExAutomata.getVars().size());

        manager = new BDDExtendedManager();

        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();
        
        initialLocationsBDD = manager.getOneBDD();
        markedLocationsBDD = manager.getOneBDD();
        forbiddenLocationsBDD = manager.getZeroBDD();

        uncontrollableStatesBDD = manager.getZeroBDD();
        uncontrollableEventsBDD = manager.getZeroBDD();

        plantsUncontrollableEvents = manager.getZeroBDD();
        specsUncontrollableEvents = manager.getZeroBDD();

        plantsForwardTransitions = manager.getZeroBDD();
        specsForwardTransitions = manager.getZeroBDD();

        plantsBackwardTransitions = manager.getZeroBDD();
        specsBackwardTransitions = manager.getZeroBDD();

//        selfLoopsBDD = manager.getZeroBDD();
        plantsSelfLoopsBDD = manager.getZeroBDD();
        specsSelfLoopsBDD = manager.getZeroBDD();

        initialize();
    }

    void initialize()
    {
        unionAlphabet = orgExAutomata.getUnionAlphabet();
        eventDomain = manager.createDomain(unionAlphabet.size());
//        System.out.println("event variables: "+eventDomain.set().toString());
        eventDomain.setName("Events");

        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();

        sourceLocationVariables = manager.createEmptyVarSet();
        destLocationVariables = manager.createEmptyVarSet();

        sourceVariablesVarSet = manager.createEmptyVarSet();
        destVariablesVarSet = manager.createEmptyVarSet();

        sourceStateDomains = new BDDDomain[theExAutomata.size()];
        destStateDomains = new BDDDomain[theExAutomata.size()];

        sourceLocationDomains = new BDDDomain[theExAutomata.size()];
        destLocationDomains = new BDDDomain[theExAutomata.size()];
        tempLocationDomains = new BDDDomain[theExAutomata.size()];

        sourceVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        destVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        tempVarDomains = new BDDDomain[orgExAutomata.getVars().size()];


        maxBitsRequired = (int)Math.ceil(Math.log(orgExAutomata.getMaxRangeOfVars())/Math.log(2));

        constantDomain = manager.createDomain(orgExAutomata.getMaxRangeOfVars());
        manager.setConstantDomain(constantDomain);
//        System.out.println("constant variables: "+constantDomain.set().toString());
        constantsVarSet = constantDomain.set();

        forwardTransAndNextValsForV = new BDD[theExAutomata.size()][orgExAutomata.getVars().size()];
        forwardTransWhereVisUpdated = new BDD[theExAutomata.size()][orgExAutomata.getVars().size()];

        backwardTransAndNextValsForV = new BDD[theExAutomata.size()][orgExAutomata.getVars().size()];
        backwardTransWhereVisUpdated = new BDD[theExAutomata.size()][orgExAutomata.getVars().size()];

        for(int i = 0; i<theExAutomata.size();i++)
        {
            for(int j = 0; j<orgExAutomata.getVars().size();j++)
            {
                forwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                forwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();

                backwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                backwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();
            }
        }

        for(VariableComponentProxy var:orgExAutomata.getVars())
        {
            int varIndex = theIndexMap.getVariableIndex(var);
            BDDDomain tempVarDomain = manager.createDomain(orgExAutomata.getMaxRangeOfVars());
            BDDDomain sourceVarDomain = manager.createDomain(orgExAutomata.getMaxRangeOfVars());
//            System.out.println(var.getName()+" source variables: "+sourceVarDomain.set().toString());
            BDDBitVector bddBitVecVar = manager.getFactory().buildVector (sourceVarDomain);
            BDDBitVecSourceVarsMap.put(var.getName(), bddBitVecVar);
            sourceVariablesVarSet.unionWith(sourceVarDomain.set());

            BDDDomain destVarDomain = manager.createDomain(orgExAutomata.getMaxRangeOfVars());
//            System.out.println(var.getName()+" destination variables: "+destVarDomain.set().toString());
            bddBitVecVar = manager.getFactory().buildVector (destVarDomain);
            BDDBitVecTargetVarsMap.put(var.getName(), bddBitVecVar);
            destVariablesVarSet.unionWith(destVarDomain.set());

            sourceVarDomains[varIndex] = sourceVarDomain;
            sourceVarDomains[varIndex].setName(var.getName());
            destVarDomains[varIndex] = destVarDomain;
            destVarDomains[varIndex].setName(var.getName());
            tempVarDomains[varIndex] = tempVarDomain;
        }
        manager.setVariableStringToIndexMap(theIndexMap.variableStringToIndexMap);

        destToSourceVariablePairing = manager.makePairing(destVarDomains, sourceVarDomains);

        sourceToTempVariablePairing = manager.makePairing(sourceVarDomains, tempVarDomains);
        tempToDestVariablePairing = manager.makePairing(tempVarDomains, destVarDomains);

        BDD currUnconEvents = null;
        
        initValuesBDD = manager.getZeroBDD();
        initValuesStringSet = new HashSet<String>();
        computeInitValues();

        for (ExtendedAutomaton automaton : theExAutomata)
        {
            int autIndex = theIndexMap.getExAutomatonIndex(automaton);
            int nbrOfStates = automaton.nbrOfNodes();
            BDDDomain tempLocationDomain = manager.createDomain(nbrOfStates);
            BDDDomain sourceLocationDomain = manager.createDomain(nbrOfStates);
//            System.out.println(automaton.getName()+" source location variables: "+sourceLocationDomain.set().toString());
            BDDDomain destLocationDomain = manager.createDomain(nbrOfStates);
//            System.out.println(automaton.getName()+" destination location variables: "+destLocationDomain.set().toString());

            sourceLocationVariables.unionWith(sourceLocationDomain.set());
            destLocationVariables.unionWith(destLocationDomain.set());
            sourceLocationDomains[autIndex] = sourceLocationDomain;
            sourceLocationDomains[autIndex].setName(automaton.getName());
            destLocationDomains[autIndex] = destLocationDomain;
            destLocationDomains[autIndex].setName(automaton.getName());
            tempLocationDomains[autIndex] = tempLocationDomain;

            BDDExtendedAutomaton bddExAutomaton = new BDDExtendedAutomaton(this, automaton);

            int[] vars = sourceLocationDomain.vars();
            int nbrOfVars = vars.length;
            for(int h=0;h<nbrOfVars;h++)
            {
                bddVar2AutName.put(vars[h], automaton.getName());
            }
            
            bddExAutomaton.initialize();            

            currUnconEvents = bddExAutomaton.getUncontrollableEvents();
            uncontrollableEventsBDD = uncontrollableEventsBDD.or(currUnconEvents);

//            selfLoopsBDD = selfLoopsBDD.or(bddAutomaton.getSelfLoopsBDD());

            if(automaton.getKind() == ComponentKind.PLANT)
            {
//                bddAutomaton.getTransitionForwardBDD().exist(bddAutomaton.getDestStateDomain().set()).printDot();
                plantsForwardTransitions = plantsForwardTransitions.or(bddExAutomaton.getEdgeForwardBDD());
                plantsBackwardTransitions = plantsBackwardTransitions.or(bddExAutomaton.getEdgeBackwardBDD());
                plantsUncontrollableEvents = plantsUncontrollableEvents.or(currUnconEvents);                

                plantsSelfLoopsBDD = plantsSelfLoopsBDD.or(bddExAutomaton.getSelfLoopsBDD());
//                bddAutomaton.getTransitionForwardBDD().and(bddAutomaton.getSelfLoopsBDD().not()).printDot();
            }
            else if(automaton.getKind() == ComponentKind.SPEC)
            {
                specsForwardTransitions = specsForwardTransitions.or(bddExAutomaton.getEdgeForwardBDD());
                specsBackwardTransitions = specsBackwardTransitions.or(bddExAutomaton.getEdgeBackwardBDD());
                specsUncontrollableEvents = specsUncontrollableEvents.or(currUnconEvents);

                specsSelfLoopsBDD = specsSelfLoopsBDD.or(bddExAutomaton.getSelfLoopsBDD());
            }

//            System.out.println(automaton.getName());
//            bddExAutomaton.getEdgeForwardBDD().printDot();

            add(bddExAutomaton);
        }

        sourceStateVariables.unionWith(sourceLocationVariables);
        sourceStateVariables.unionWith(sourceVariablesVarSet);

        destStateVariables.unionWith(destLocationVariables);
        destStateVariables.unionWith(destVariablesVarSet);
       
        sourceToTempLocationPairing = manager.makePairing(sourceLocationDomains, tempLocationDomains);
        tempToDestLocationPairing = manager.makePairing(tempLocationDomains, destLocationDomains);

        destToSourceLocationPairing = manager.makePairing(destLocationDomains, sourceLocationDomains);

        bddEdges = new BDDEdgeFactory(this).createEdges();

//        System.out.println("number of transitions: "+((BDDMonolithicTransitions)bddTransitions).transitionForwardBDD.pathCount());
    }   

    public BDDDomain getSourceLocationDomain(ExtendedAutomaton automaton)
    {
        return sourceLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
    }

    public BDDDomain getDestLocationDomain(ExtendedAutomaton automaton)
    {
        return destLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
    }

    public BDDVarSet getSourceVariablesVarSet()
    {
        return sourceVariablesVarSet;
    }

    public BDDVarSet getDestVariablesVarSet()
    {
        return destVariablesVarSet;
    }

    public BDDVarSet getSourceStatesVarSet()
    {
        return sourceStateVariables;
    }

    public BDDVarSet getDestStatesVarSet()
    {
        return destStateVariables;
    }

    public BDDBitVector getBDDBitVecSource(String name)
    {
        return BDDBitVecSourceVarsMap.get(name);
    }

    public BDDBitVector getBDDBitVecTarget(String name)
    {
        return BDDBitVecTargetVarsMap.get(name);
    }

    public HashMap<String, BDDBitVector> getBDDBitVecSourceMap()
    {
        return BDDBitVecSourceVarsMap;
    }

    public HashMap<String, BDDBitVector> getBDDBitVecTargetMap()
    {
        return BDDBitVecTargetVarsMap;
    }

    public ExtendedAutomata getExtendedAutomata()
    {
        return orgExAutomata;
    }

    public BDD getForbiddenLocations()
    {
        return forbiddenLocationsBDD;
    }

    public void addForbiddenLocations(BDD forbiddenLocations)
    {
        forbiddenLocationsBDD = forbiddenLocationsBDD.or(forbiddenLocations);
    }

    public void addUncontrollableStates(BDD uncontrollableStates)
    {
        uncontrollableStatesBDD = uncontrollableStatesBDD.and(uncontrollableStates);
    }

    public double numberOfReachableStates()
    {
        if (nbrOfReachableStates < 0)
        {
            getReachableStates();
        }
        return nbrOfReachableStates;
    }

    public double numberOfCoreachableStates()
    {
        if (nbrOfCoreachableStates < 0)
        {
            getCoreachableStates();
        }
        return nbrOfCoreachableStates;
    }

    public double numberOfBlockingStates()
    {
        if (nbrOfBlockingStates < 0)
        {
            getReachableAndCoreachableStates();
        }
        return nbrOfBlockingStates;
    }

    public double numberOfReachableAndCoreachableStates()
    {
        if (nbrOfReachableAndCoreachableStates < 0)
        {
            getReachableAndCoreachableStates();
        }
        return nbrOfReachableAndCoreachableStates;
    }

    public boolean isNonblocking()
    {
        BDD reachableStatesBDD = getReachableStates();
        BDD coreachableStatesBDD = getCoreachableStates();
        BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
        return impBDD.equals(manager.getOneBDD());
    }

    public BDD getReachableStates()
    {
        if (reachableStatesBDD == null)
        {
            reachableStatesBDD = BDDExtendedManager.reachableStates(getInitialState(),bddEdges, sourceStateVariables, eventDomain.set(), destToSourceLocationPairing, destToSourceVariablePairing);
            nbrOfReachableStates = reachableStatesBDD.satCount(sourceStateVariables);
        }
        return reachableStatesBDD;
    }

/*    public BDD getReachableStatesForAutomaton(BDDExtendedAutomaton bddAut)
    {
        BDD statesBDD = manager.reachableStates(bddAut.getEdgeForwardBDD(),getInitialStateForAutomaton(bddAut),bddEdges, sourceStateVariables, eventDomain.set(), destToSourceLocationPairing, destToSourceVariablePairing);
        return statesBDD;
    }
*/
    public BDD getInitialState()
    {
        return initialLocationsBDD.and(initValuesBDD);
    }

    public BDD getInitialStateForAutomaton(BDDExtendedAutomaton bddAut)
    {
        return bddAut.getInitialLocation().and(initValuesBDD);
    }

    public void computeInitValues()
    {
        if(!orgExAutomata.isInitValuesContradicting())
        {
            initValuesBDD = manager.getOneBDD();
            for(VariableComponentProxy var:orgExAutomata.getVars())
            {
                int initValue = orgExAutomata.getInitValueofVar(var.getName());
                initValuesStringSet.add(var.getName()+"="+initValue);
                initValuesBDD.andWith(getConstantBDD(var.getName(),initValue));
            }
        }
    }

    public BDD getConstantBDD(String varName, int cons)
    {
        BDDBitVector c = manager.getFactory().buildVector(constantDomain);
        // c.initialize(cons);
	// ++ cannot find symbol
	// ++ symbol  : method initialize(int)
	// ++ location: class net.sf.javabdd.BDDBitVector

        BDD result = manager.getOneBDD();
        for(int i=0;i<c.size();i++)
        {
            result.andWith(getBDDBitVecSource(varName).getBit(i).biimp(c.getBit(i)));
        }

        return result;
    }

    public HashSet<String> getInitValuesStringSet()
    {
        return initValuesStringSet;
    }

    BDD getCoreachableStates()
    {
        if (coreachableStatesBDD == null)
        {
            coreachableStatesBDD = BDDExtendedManager.coreachableStates(markedLocationsBDD, bddEdges, sourceStateVariables, eventDomain.set(), destToSourceLocationPairing, destToSourceVariablePairing);
            nbrOfCoreachableStates = coreachableStatesBDD.satCount(sourceStateVariables);
        }

        return coreachableStatesBDD;
    }

    public BDD getReachableAndCoreachableStates()
    {
        if (reachableAndCoreachableStatesBDD == null)
        {
            BDD reachableStatesBDD = getReachableStates();
            BDD coreachableStatesBDD = getCoreachableStates();

            reachableAndCoreachableStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);

            nbrOfReachableAndCoreachableStates = reachableAndCoreachableStatesBDD.satCount(sourceStateVariables);
            nbrOfBlockingStates = nbrOfReachableStates - nbrOfReachableAndCoreachableStates;
        }
    
        return reachableAndCoreachableStatesBDD;
    }

    public BDDExtendedAutomaton getBDDExAutomaton(String autName)
    {
        for(ExtendedAutomaton aut: theExAutomata)
            if(aut.getName().equals(autName))
                return automatonToBDDAutomatonMap.get(aut);
        return null;
    }

    public BDD getPlantsSelfLoopsBDD()
    {
        return plantsSelfLoopsBDD;
    }

    public List<EventDeclProxy> getInverseAlphabet(ExtendedAutomaton exAutomaton)
    {
        return orgExAutomata.getInverseAlphabet(exAutomaton);
    }

    public int getLocationIndex(ExtendedAutomaton theAutomaton, NodeProxy theState)
    {
        return theIndexMap.getLocationIndex(theAutomaton, theState);
    }

    public int getEventIndex(EventDeclProxy theEvent)
    {
        return theIndexMap.getEventIndex(theEvent);
    }

    public BDDVarSet getEventVarSet()
    {
        return eventDomain.set();
    }

    public BDDDomain getEventDomain()
    {
        return eventDomain;
    }

    public BDDEdges getBDDEdges()
    {
        return bddEdges;
    }

    public void addInitialLocations(BDD initialLocations)
    {
        initialLocationsBDD = initialLocationsBDD.and(initialLocations);
    }

    public void addMarkedLocations(BDD markedLocations)
    {
        markedLocationsBDD = markedLocationsBDD.and(markedLocations);
    }

    public BDDExtendedManager getBDDManager()
    {
        return manager;
    }

    public Automaton removeGuardsActionsFromEFA(SimpleComponentSubject component)
    {
        Automaton automaton = new Automaton(component.getName());

        if(component.getKind() == ComponentKind.PLANT)
            automaton.setType(AutomatonType.PLANT);
        if(component.getKind() == ComponentKind.SPEC)
            automaton.setType(AutomatonType.SPECIFICATION);
        if(component.getKind() == ComponentKind.SUPERVISOR)
            automaton.setType(AutomatonType.SUPERVISOR);
        if(component.getKind() == ComponentKind.PROPERTY)
            automaton.setType(AutomatonType.PROPERTY);

        State fromState , toState;
        LabeledEvent event;
        boolean initialFlag = true;
        for(EdgeSubject edge : component.getGraph().getEdgesModifiable())
        {
            fromState = automaton.getStateWithName(edge.getSource().getName());
            if(fromState == null)
            {
                fromState = new State(edge.getSource().getName());
                if(initialFlag && edge.getSource().toString().contains("initial"))
                {
                    fromState.setInitial(true);
                    initialFlag = false;
                }
                if(edge.getSource().toString().contains("accepting"))
                {
                    fromState.setAccepting(true);
                }
                automaton.addState(fromState);
                if(fromState.isInitial())
                {
                    automaton.setInitialState(fromState);
                }
            }
            toState = automaton.getStateWithName(edge.getTarget().getName());
            if(toState == null)
            {
                toState = new State(edge.getTarget().getName());
                if(initialFlag && edge.getTarget().toString().contains("initial"))
                {
                    toState.setInitial(true);
                    initialFlag = false;
                }
                if(edge.getTarget().toString().contains("accepting"))
                {
                    toState.setAccepting(true);
                }
                automaton.addState(toState);
                if(toState.isInitial())
                {
                    automaton.setInitialState(toState);
                }
            }

            ListSubject<AbstractSubject> eventList = edge.getLabelBlock().getEventListModifiable();
            for(AbstractSubject e:eventList)
            {
//                EventDeclSubject eventSubject = (EventDeclSubject)e;
//                SimpleComponentSubject eventSubject = (SimpleComponentSubject)e;
                SimpleIdentifierSubject eventSubject = (SimpleIdentifierSubject)e;
                event = automaton.getAlphabet().getEvent(eventSubject.getName());
                if(event == null)
                {
                    event = new LabeledEvent(eventSubject.getName());
                    automaton.getAlphabet().add(event);
                }
/*                if(eventSubject.getKind() == EventKind.CONTROLLABLE)
                {
                    event.setControllable(true);
                }
                else
                {
                    event.setControllable(false);
                }
 */
                if(edge.getGuardActionBlock() == null)
                {
                    GuardActionBlockSubject gab = new GuardActionBlockSubject();
                    edge.setGuardActionBlock(gab);
                }

                Arc currArc = new Arc(fromState, toState, event);
                automaton.addArc(currArc);
            }

        }

        return automaton;
    }

    public Automata removeGuardsActionsFromEFAs(ExtendedAutomata exAutomata)
    {
        ArrayList<AbstractSubject> components = exAutomata.getComponents();
        Automata automata = new Automata();
        HashSet<SimpleComponentSubject> autComps = new HashSet<SimpleComponentSubject>();

        for(AbstractSubject component : components)
            if(component.toString().contains("NODES") && component.toString().contains("EDGES"))
                autComps.add((SimpleComponentSubject)component);


        for(SimpleComponentSubject autComp: autComps)
        {
            automata.addAutomaton(removeGuardsActionsFromEFA(autComp));
        }

        return automata;
    }

    public BDD[] getForwardTransAndNextValsForV(BDDExtendedAutomaton aut)
    {
        return forwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton())];
    }
    
    public BDD[] getForwardTransWhereVisUpdated(BDDExtendedAutomaton aut)
    {
        return forwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton())];
    }

    public BDD[] getBackwardTransAndNextValsForV(BDDExtendedAutomaton aut)
    {
        return backwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton())];
    }

    public BDD[] getBackwardTransWhereVisUpdated(BDDExtendedAutomaton aut)
    {
        return backwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton())];
    }

    public BDD getMarkedLocations()
    {
        return markedLocationsBDD;
    }

    public BDDPairing getDest2SourcePairing()
    {
        return destToSourceLocationPairing;
    }

    public BDDVarSet getSourceLocationVariables()
    {
        return sourceLocationVariables;
    }

    public BDDVarSet getDestLocationVariables()
    {
        return destLocationVariables;
    }

    public BDD getUncontrollableEvents()
    {
        return uncontrollableEventsBDD;
    }

    public BDD getPlantsForwardTransitions()
    {
        return plantsForwardTransitions;
    }

    public BDD getSpecsForwardTransitions()
    {
        return specsForwardTransitions;
    }

    public BDD getPlantsBackwardTransitions()
    {
        return plantsBackwardTransitions;
    }

    public BDD getSpecsBackwardTransitions()
    {
        return specsBackwardTransitions;
    }

    public BDD getPlantsUncontrollableEvents()
    {
        return plantsUncontrollableEvents;
    }

    public BDD getSpecsUncontrollableEvents()
    {
        return specsUncontrollableEvents;
    }

    void add(BDDExtendedAutomaton bddExAutomaton)
    {
        theBDDAutomataList.add(bddExAutomaton);
        automatonToBDDAutomatonMap.put(bddExAutomaton.getExAutomaton(), bddExAutomaton);
    }

    public Iterator<BDDExtendedAutomaton> iterator()
    {
        return theBDDAutomataList.iterator();
    }
    
    public BDDExtendedAutomaton getBDDExAutomaton(ExtendedAutomaton theAutomaton)
    {
        return automatonToBDDAutomatonMap.get(theAutomaton);
    }

    public BDDExtendedAutomaton getBDDExAutomatonAt(int i)
    {
        return automatonToBDDAutomatonMap.get(theIndexMap.getAutomatonAt(i));
    }

    public HashSet<String> generateStates(BDD states)
    {
        BDD.BDDIterator satIt = states.iterator(sourceLocationVariables);

        HashSet<String> output = new HashSet<String>();
        while(satIt.hasNext())
        {
            BDD truePathBDD = satIt.nextBDD();
            HashSet<String> subOutput = new HashSet<String>();
            boolean firstTime = true;
            for(ExtendedAutomaton aut:orgExAutomata)
            {
                HashSet<String> tempOutput = new HashSet<String>();
                for(NodeProxy state:aut.getNodes())
                {
                    int stateIndex = getLocationIndex(aut, state);
                    BDD stateBDD = manager.getFactory().buildCube(stateIndex, getSourceLocationDomain(aut).vars());
                    if(truePathBDD.and(stateBDD).nodeCount() > 0)
                    {
                        if(!firstTime)
                        {
                            for(String element:subOutput)
                            {
                                String temp = element + "." + state.getName();
                                tempOutput.add(temp);
                            }
                        }
                        else
                        {
                            subOutput.add(state.getName());
                        }
                    }
                }
                if(!firstTime)
                    subOutput = tempOutput;

                firstTime = false;
            }
            output.addAll(subOutput);
        }

        for(String element:output)
        {
            System.out.println(element);
        }

        return output;
    }

}
