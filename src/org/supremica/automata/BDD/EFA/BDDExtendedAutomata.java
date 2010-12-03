package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.javabdd.BDD;
import net.sf.javabdd.BDDDomain;
import net.sf.javabdd.BDDPairing;
import net.sf.javabdd.BDDVarSet;

import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.base.ListSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.Arc;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.AutomatonType;
import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomataIndexMap;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.LabeledEvent;
import org.supremica.automata.State;
import org.supremica.automata.BDD.BDDAutomata;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


public class BDDExtendedAutomata implements Iterable<BDDExtendedAutomaton>{

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);

    BDDExtendedManager manager;
    List<ExtendedAutomaton> theExAutomata;
    ExtendedAutomata orgExAutomata;
    List<BDDExtendedAutomaton> theBDDAutomataList = new LinkedList<BDDExtendedAutomaton>();
    Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap = new HashMap<ExtendedAutomaton, BDDExtendedAutomaton>();
    Map<Integer, String> bddVar2AutVarName = new HashMap<Integer, String>();
    public Map<String,HashSet<Integer>> enablingSigmaMap;
    Map<Integer,BDD> sourceBDDVar2BDD = new HashMap<Integer, BDD>();
    Set<Integer> sourceLocationVars = new HashSet<Integer>();

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

    BDDPairing sourceToDestLocationPairing = null;
    BDDPairing sourceToDestVariablePairing = null;

    BDDPairing destToSourceLocationPairing = null;
    BDDPairing destToSourceVariablePairing = null;

    BDDPairing sourceToTempLocationPairing = null;
    BDDPairing tempToDestLocationPairing = null;

    BDDPairing sourceToTempVariablePairing = null;
    BDDPairing tempToDestVariablePairing = null;

    BDD initialLocationsBDD = null;
    BDD markedLocationsBDD = null;
    BDD forbiddenLocationsBDD = null;
    BDD plantifiedBlockedLocationsBDD = null;
    BDD uncontrollableStatesBDD = null;

    private BDD reachableStatesBDD = null;
    BDD coreachableStatesBDD = null;
    BDD nonblockingStatesBDD = null;
    BDD nonblockingControllableStatesBDD = null;
    BDD reachableNonblockingControllableStatesBDD = null;
    BDD controllableStatesBDD = null;

    BDD uncontrollableEventsBDD = null;
    BDD plantAlphabetBDD = null;

    BDD forwardOverflows = null;
    BDD backwardOverflows = null;

    double nbrOfReachableStates = -1;
    double nbrOfCoreachableStates = -1;
    double nbrOfNonblockingStates = -1;
    double nbrOfBlockingStates = -1;
    double nbrOfSafeStates = -1;
    double nbrOfNonblockingControllableStates = -1;
    double nbrOfControllableStates = -1;

    HashMap<String,SupremicaBDDBitVector> BDDBitVecSourceVarsMap;
    HashMap<String,SupremicaBDDBitVector> BDDBitVecTargetVarsMap;

    BDD initValuesBDD = null;
    BDD markedValuesBDD = null;

    BDD[][] forwardTransWhereVisUpdated;
    BDD[][] forwardTransAndNextValsForV;

    BDD[][] backwardTransWhereVisUpdated;
    BDD[][] backwardTransAndNextValsForV;

    HashSet<String> varNames;
    String locaVarSuffix;

    public BDDExtendedAutomata(final ExtendedAutomata orgExAutomata)
    {
        this.orgExAutomata = orgExAutomata;
        locaVarSuffix = orgExAutomata.getlocVarSuffix();
        theIndexMap = new ExtendedAutomataIndexMap(orgExAutomata);
        theExAutomata = new PCGExtendedAutomataSorter().sortAutomata(orgExAutomata.getExtendedAutomataList());
 /*       theExAutomata = new ArrayList<ExtendedAutomaton>();
        theExAutomata.add(theIndexMap.getExAutomatonWithName("CC"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("CL"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("CR"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("CT"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("Hiss1"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("Hiss2"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("R1"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("R2"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("Puck1"));
        theExAutomata.add(theIndexMap.getExAutomatonWithName("Puck3"));
        for(ExtendedAutomaton aut: theExAutomata)
        {
            System.err.println(aut.getName());
        }

 */
/*        AutomataSorter automataSorter = new PCGAutomataSorter();
        Automata theAutomata = automataSorter.sortAutomata(removeGuardsActionsFromEFAs(orgExAutomata));

        theExAutomata = new ArrayList<ExtendedAutomaton>();
        for(Automaton automaton:theAutomata)
        {
            theExAutomata.add(theIndexMap.getExAutomatonWithName(automaton.getName()));
        }
*/
//        theExAutomata = orgExAutomata.getExtendedAutomataList();

        BDDBitVecSourceVarsMap = new HashMap<String, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        BDDBitVecTargetVarsMap = new HashMap<String, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        varNames = new HashSet<String>(orgExAutomata.getVars().size());

        manager = new BDDExtendedManager();

        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();

        initialLocationsBDD = manager.getOneBDD();
        markedLocationsBDD = manager.getOneBDD();
        forbiddenLocationsBDD = manager.getZeroBDD();
        plantifiedBlockedLocationsBDD = manager.getZeroBDD();

        uncontrollableStatesBDD = manager.getZeroBDD();
        uncontrollableEventsBDD = manager.getZeroBDD();
        plantAlphabetBDD = manager.getZeroBDD();

        forwardOverflows = manager.getZeroBDD();
        backwardOverflows = manager.getZeroBDD();

        initialize();
    }

    void initialize()
    {
        unionAlphabet = orgExAutomata.getUnionAlphabet();
        eventDomain = manager.createDomain(unionAlphabet.size());
/*        System.out.println("event variables: "+eventDomain.set().toString());
        for(EventDeclProxy e: orgExAutomata.unionAlphabet)
        {
            System.out.println(e.getName()+": "+getEventIndex(e));
        }
*/
        for(final EventDeclProxy event:unionAlphabet)
        {
            final int currEventIndex = getEventIndex(event);
            final BDD eventBDD = manager.createBDD(currEventIndex, eventDomain);
            if(orgExAutomata.getPlantAlphabet().contains(event))
            {
                plantAlphabetBDD = plantAlphabetBDD.or(eventBDD);
            }

            if(event.getKind() != EventKind.CONTROLLABLE)
            {
                uncontrollableEventsBDD.orWith(eventBDD);
            }
        }

        eventDomain.setName("Events");

        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();

        sourceLocationVariables = manager.createEmptyVarSet();
        destLocationVariables = manager.createEmptyVarSet();

        sourceVariablesVarSet = manager.createEmptyVarSet();
        destVariablesVarSet = manager.createEmptyVarSet();

        sourceStateDomains = new BDDDomain[orgExAutomata.size()];
        destStateDomains = new BDDDomain[orgExAutomata.size()];

        sourceLocationDomains = new BDDDomain[orgExAutomata.size()];
        destLocationDomains = new BDDDomain[orgExAutomata.size()];
        tempLocationDomains = new BDDDomain[orgExAutomata.size()];

        sourceVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        destVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        tempVarDomains = new BDDDomain[orgExAutomata.getVars().size()];

        if(orgExAutomata.getDomain() > 0 )
        {
            constantDomain = manager.createDomain(orgExAutomata.getDomain());
//        System.out.println("constant variables: "+constantDomain.set().toString());
            manager.setConstantDomain(constantDomain);
            constantsVarSet = constantDomain.set();
        }

        forwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        forwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

        backwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        backwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

        for(int i = 0; i<orgExAutomata.size();i++)
        {
            for(int j = 0; j<orgExAutomata.getVars().size();j++)
            {
                forwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                forwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();

                backwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                backwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();
            }
        }

//        System.out.println("domain: "+orgExAutomata.getDomain());


        final ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(new PCGVariableSorter(orgExAutomata).sortVars(orgExAutomata.getVars()));
//        ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(orgExAutomata.getVars());

        for(final ExtendedAutomaton automaton:theExAutomata){

//            System.err.println(automaton.getName());
            final int autIndex = theIndexMap.getExAutomatonIndex(automaton.getName());
            final int nbrOfStates = automaton.nbrOfNodes();
            final BDDDomain tempLocationDomain = manager.createDomain(nbrOfStates);
//            VariableComponentProxy var = theIndexMap.getCorrepondentVariable(automaton);
//            BDDDomain tempLocationDomain = tempVarDomains[theIndexMap.getVariableIndex(var)];

            final BDDDomain sourceLocationDomain = manager.createDomain(nbrOfStates);
//            BDDDomain sourceLocationDomain = sourceVarDomains[theIndexMap.getVariableIndex(var)];
            final int[] sourceVars = sourceLocationDomain.vars();
            for(int i = 0; i<sourceVars.length; i++)
            {
                final int[] sourceVar = new int[1];
                sourceVar[0] = sourceVars[i];
                sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
                sourceLocationVars.add(sourceVars[i]);
                bddVar2AutVarName.put(sourceVars[i], automaton.getName());
            }

//            System.err.println("sourceLocation variables: "+sourceLocationDomain.set().toString());
            final BDDDomain destLocationDomain = manager.createDomain(nbrOfStates);
//            BDDDomain destLocationDomain = destVarDomains[theIndexMap.getVariableIndex(var)];
/*            System.out.println("destLocation variables: "+destLocationDomain.set().toString());
            for(NodeProxy loc: automaton.getNodes())
            {
                System.out.println(loc.getName()+": "+getLocationIndex(automaton, loc));
            }
*/
            sourceLocationVariables.unionWith(sourceLocationDomain.set());
            destLocationVariables.unionWith(destLocationDomain.set());
            sourceLocationDomains[autIndex] = sourceLocationDomain;
            sourceLocationDomains[autIndex].setName(automaton.getName());
            destLocationDomains[autIndex] = destLocationDomain;
            destLocationDomains[autIndex].setName(automaton.getName());

            tempLocationDomains[autIndex] = tempLocationDomain;

            //Place the variables that are related to this automaton (the variables that are updated in this automaton)
            for(final VariableComponentProxy varRelatedToAutomaton:automaton.getUsedTargetVariables())
            {
                if(sortedVarList.contains(varRelatedToAutomaton))
                {
                    initializeVariable(varRelatedToAutomaton);
                }
            }
            sortedVarList.removeAll(automaton.getUsedTargetVariables());

        }

        //Manual variable ordering
/*      sortedVarList = new ArrayList<VariableComponentProxy>();

        sortedVarList.add(orgExAutomata.getVariableByName("R1_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("R2_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("CL_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("CR_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("CT_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("CC_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("Hiss1_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("Hiss2_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("Puck1_state"));
        sortedVarList.add(orgExAutomata.getVariableByName("Puck3_state"));

        sortedVarList = new ArrayList<VariableComponentProxy>(orgExAutomata.getVars());
*/


        for(final VariableComponentProxy var:sortedVarList)
        {
            initializeVariable(var);
        }

        manager.setVariableStringToIndexMap(theIndexMap.variableStringToIndexMap);

        destToSourceVariablePairing = manager.makePairing(destVarDomains, sourceVarDomains);
        sourceToTempVariablePairing = manager.makePairing(sourceVarDomains, tempVarDomains);
        tempToDestVariablePairing = manager.makePairing(tempVarDomains, destVarDomains);

        sourceToDestVariablePairing = manager.makePairing(sourceVarDomains,destVarDomains);

        initValuesBDD = manager.getZeroBDD();
        markedValuesBDD = manager.getOneBDD();

        manager.setBDDExAutomata(this);

        computeInitValues();
        computeMarkedValues();

        for(final ExtendedAutomaton automaton:theExAutomata)
        {
            final BDDExtendedAutomaton bddExAutomaton = new BDDExtendedAutomaton(this, automaton);

            bddExAutomaton.initialize();

            add(bddExAutomaton);
        }


/*        BDD t1 = getBDDExAutomaton(theExAutomata.get(0)).getEdgeForwardBDD();
        BDD t2 = getBDDExAutomaton(theExAutomata.get(1)).getEdgeForwardBDD();

        t1.biimp(t2).printDot();
*/
        sourceStateVariables = sourceStateVariables.union(sourceLocationVariables);
        sourceStateVariables = sourceStateVariables.union(sourceVariablesVarSet);

        destStateVariables = destStateVariables.union(destLocationVariables);
        destStateVariables = destStateVariables.union(destVariablesVarSet);

        sourceToTempLocationPairing = manager.makePairing(sourceLocationDomains, tempLocationDomains);
        tempToDestLocationPairing = manager.makePairing(tempLocationDomains, destLocationDomains);
        destToSourceLocationPairing = manager.makePairing(destLocationDomains, sourceLocationDomains);

        sourceToDestLocationPairing = manager.makePairing(sourceLocationDomains,destLocationDomains);

        bddEdges = new BDDEdgeFactory(this).createEdges();

//        System.out.println("number of transitions: "+((BDDMonolithicTransitions)bddTransitions).transitionForwardBDD.pathCount());
    }

    public void initializeVariable(final VariableComponentProxy var)
    {
        varNames.add(var.getName());
        final int varIndex = theIndexMap.getVariableIndex(var);
//            System.out.println("variable name: "+var.getName());


        final BDDDomain tempVarDomain = manager.createDomain(orgExAutomata.getDomain());
//            System.out.println("tempVar variables: "+tempVarDomain.set().toString());
        final BDDDomain sourceVarDomain = manager.createDomain(orgExAutomata.getDomain());
        final int[] sourceVars = sourceVarDomain.vars();
        for(int i = 0; i<sourceVars.length; i++)
        {
            final int[] sourceVar = new int[1];
            sourceVar[0] = sourceVars[i];
            sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
            bddVar2AutVarName.put(sourceVars[i], var.getName());
        }

//            System.out.println("sourceVar variables: "+sourceVarDomain.set().toString());
        BDDBitVecSourceVarsMap.put(var.getName(), new SupremicaBDDBitVector(manager.getFactory(),sourceVarDomain));
        sourceVariablesVarSet.unionWith(sourceVarDomain.set());

        final BDDDomain destVarDomain = manager.createDomain(orgExAutomata.getDomain());
//            System.out.println("destVar variables: "+destVarDomain.set().toString());
        BDDBitVecTargetVarsMap.put(var.getName(), new SupremicaBDDBitVector(manager.getFactory(),destVarDomain));
        destVariablesVarSet.unionWith(destVarDomain.set());

        sourceVarDomains[varIndex] = sourceVarDomain;
        sourceVarDomains[varIndex].setName(var.getName());
        destVarDomains[varIndex] = destVarDomain;
        destVarDomains[varIndex].setName(var.getName());
        tempVarDomains[varIndex] = tempVarDomain;
    }

    public boolean isSourceLocationVar(final int var)
    {
        if(sourceLocationVars.contains(var))
            return true;
        else
            return false;
    }

    public String getLocVarSuffix()
    {
        return locaVarSuffix;
    }

    public String getAutVarName(final int var)
    {
        return bddVar2AutVarName.get(var);
    }

    public BDD getBDDforSourceBDDVar(final int bddVar)
    {
        return sourceBDDVar2BDD.get(bddVar);
    }

    public BDDDomain getSourceLocationDomain(final String automaton)
    {
        return sourceLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
    }

    public BDDDomain getDestLocationDomain(final String automaton)
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

    public SupremicaBDDBitVector getBDDBitVecSource(final String name)
    {
        return BDDBitVecSourceVarsMap.get(name);
    }

    public SupremicaBDDBitVector getBDDBitVecTarget(final String name)
    {
        return BDDBitVecTargetVarsMap.get(name);
    }

    public HashMap<String, SupremicaBDDBitVector> getBDDBitVecSourceMap()
    {
        return BDDBitVecSourceVarsMap;
    }

    public HashMap<String, SupremicaBDDBitVector> getBDDBitVecTargetMap()
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

    public BDD getPlantifiedBlockedLocations()
    {
        return plantifiedBlockedLocationsBDD;
    }

    public void addForbiddenLocations(final BDD forbiddenLocations)
    {
        forbiddenLocationsBDD = forbiddenLocationsBDD.or(forbiddenLocations);
    }

    public void addPlantifiedBlockedLocations(final BDD forbiddenUnconLocations)
    {
        plantifiedBlockedLocationsBDD = plantifiedBlockedLocationsBDD.or(forbiddenUnconLocations);
    }

    public double numberOfReachableStates()
    {
        if (nbrOfReachableStates < 0)
        {
            getReachableStates();
        }
        return nbrOfReachableStates;
    }

    public double numberOfControllableStates(final boolean reachable)
    {
        if (nbrOfControllableStates < 0)
        {
            getControllableStates(reachable);
        }
        return nbrOfControllableStates;
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
            getNonblockingStates();
        }
        return nbrOfBlockingStates;
    }

    public double numberOfNonblockingStates()
    {
        if (nbrOfNonblockingStates < 0)
        {
            getNonblockingStates();
        }
        return nbrOfNonblockingStates;
    }

    public double numberOfNonblockingControllableStates(final boolean reachable)
    {
        if (nbrOfNonblockingControllableStates < 0)
        {
            getNonblockingControllableStates(reachable);
        }
        return nbrOfNonblockingControllableStates;
    }

    public boolean isNonblocking()
    {
        final BDD reachableStatesBDD = getReachableStates();
        final BDD coreachableStatesBDD = getCoreachableStates();
        final BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
        return impBDD.equals(manager.getOneBDD());
    }

    public void done()
    {
        if (manager != null)
            manager.done();
    }

    public BDD getInitialState()
    {
//        return (initValuesBDD);
        return initialLocationsBDD.and(initValuesBDD);
    }

    public BDD getMarkedStates()
    {

//        return (markedValuesBDD);
        return markedLocationsBDD.and(markedValuesBDD);
    }

    public void computeInitValues()
    {
        initValuesBDD = manager.getOneBDD();
        for(final VariableComponentProxy var:orgExAutomata.getVars())
        {
            final int initValue = theIndexMap.getInitValueofVar(var.getName());
            initValuesBDD.andWith(getConstantBDD(var.getName(),initValue));
        }
    }

    public void computeMarkedValues()
    {
        markedValuesBDD = manager.getOneBDD();
        for(final VariableComponentProxy var:orgExAutomata.getVars())
        {
            BDD markedVals = manager.getZeroBDD();
            for(final VariableMarkingProxy vmp: theIndexMap.getMarkedPredicatesofVar(var.getName()))
            {
                markedVals.orWith(manager.guard2BDD(vmp.getPredicate()));
            }
            if(theIndexMap.getMarkedPredicatesofVar(var.getName()).size() == 0)
                markedVals = manager.getOneBDD();

            markedValuesBDD.andWith(markedVals);
        }
    }

    public SupremicaBDDBitVector getMaxBDDBitVecOf(final String variable)
    {
        return new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),orgExAutomata.getMaxValueofVar(variable));
    }

    public SupremicaBDDBitVector getMinBDDBitVecOf(final String variable)
    {
        return new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),orgExAutomata.getMinValueofVar(variable));
    }

    public BDD getConstantBDD(final String varName, final int cons)
    {
        final SupremicaBDDBitVector c = new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),cons);

        final BDD result = manager.getOneBDD();
        for(int i=0;i<c.size();i++)
        {
            result.andWith(getBDDBitVecSource(varName).getBit(i).biimp(c.getBit(i)));
        }

        return result;
    }

    public BDD getZeroBDD()
    {
        return manager.getZeroBDD();
    }

    public BDD getOneBDD()
    {
        return manager.getOneBDD();
    }

    public BDD getReachableStates()
    {
        if (reachableStatesBDD == null)
        {
            reachableStatesBDD = manager.restrictedForward(manager.getZeroBDD());
            nbrOfReachableStates = nbrOfStatesBDD(reachableStatesBDD);
        }
        return reachableStatesBDD;
    }

    public double getNbrOfRecahableStates()
    {
        getReachableStates();
        return nbrOfReachableStates;
    }

    BDD getCoreachableStates()
    {
        if (coreachableStatesBDD == null)
        {
            coreachableStatesBDD = manager.restrictedBackward(manager.getZeroBDD());
            nbrOfCoreachableStates = nbrOfStatesBDD(coreachableStatesBDD);
        }

        return coreachableStatesBDD;
    }

    public BDD getNonblockingControllableStates(final boolean reachable)
    {

        if(nonblockingControllableStatesBDD == null)
        {
            nonblockingControllableStatesBDD = manager.nonblockingControllable(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()),reachable);
            nbrOfNonblockingControllableStates = nbrOfStatesBDD(nonblockingControllableStatesBDD);
        }

        return nonblockingControllableStatesBDD;
    }

    public BDD getControllableStates(final boolean reachable)
    {
        if(controllableStatesBDD == null)
        {
            final BDD uncontrollableStates = manager.getInitiallyUncontrollableStates().or(getForbiddenLocations());
//            BDD uncontrollableStates = manager.uncontrollableBackward(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()));
            if(reachable)
                controllableStatesBDD = (manager.restrictedForward(uncontrollableStates));
            else
                controllableStatesBDD = uncontrollableStates.not();

            nbrOfControllableStates = nbrOfStatesBDD(controllableStatesBDD);
        }

        return controllableStatesBDD;
    }

    public BDD getNonblockingStates()
    {
        if (nonblockingStatesBDD == null)
        {
            reachableStatesBDD = getReachableStates();
//            reachableStatesBDD.printDot();
            coreachableStatesBDD = getCoreachableStates();

//            coreachableStatesBDD.printDot();

            nonblockingStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);

            nbrOfNonblockingStates = nbrOfStatesBDD(nonblockingStatesBDD);
            nbrOfBlockingStates = nbrOfReachableStates - nbrOfNonblockingStates;
        }

        return nonblockingStatesBDD;
    }

    public ArrayList<String> getComplementValues(final String varName, final ArrayList<String> vals)
    {
        final ArrayList<String> output = new ArrayList<String>();
        for(int i= getExtendedAutomata().getMinValueofVar(varName);i<=getExtendedAutomata().getMaxValueofVar(varName);i++)
            if(!vals.contains(i+""))
                output.add(i+"");

        return output;
    }

    public double nbrOfStatesBDD(final BDD bdd)
    {
        return bdd.satCount(sourceStateVariables);
    }

    public BDDExtendedAutomaton getBDDExAutomaton(final String autName)
    {
        final ExtendedAutomaton efa = theIndexMap.getExAutomatonWithName(autName);
        return (efa==null?null:automatonToBDDAutomatonMap.get(efa));
    }

    public List<EventDeclProxy> getInverseAlphabet(final ExtendedAutomaton exAutomaton)
    {
        return orgExAutomata.getInverseAlphabet(exAutomaton);
    }


    public int getLocationIndex(final ExtendedAutomaton theAutomaton, final NodeProxy theLocation)
    {
        return theIndexMap.getLocationIndex(theAutomaton.getName(), theLocation.getName());
    }

    public int getEventIndex(final EventDeclProxy theEvent)
    {
        return theIndexMap.getEventIndex(theEvent);
    }

    public int getEventIndex(final String theEvent)
    {
        return theIndexMap.getEventIndex(theIndexMap.eventIdToProxy(theEvent));
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

    public void addInitialLocations(final BDD initialLocations)
    {
        initialLocationsBDD = initialLocationsBDD.and(initialLocations);
    }

    public void addMarkedLocations(final BDD markedLocations)
    {
        markedLocationsBDD = markedLocationsBDD.and(markedLocations);
    }

    public BDDExtendedManager getBDDManager()
    {
        return manager;
    }

    public Automaton removeGuardsActionsFromEFA(final SimpleComponentSubject component)
    {
        final Automaton automaton = new Automaton(component.getName());

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

        for(final EdgeSubject edge : component.getGraph().getEdgesModifiable())
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

            final ListSubject<AbstractSubject> eventList = edge.getLabelBlock().getEventListModifiable();
            for(final AbstractSubject e:eventList)
            {
//                EventDeclSubject eventSubject = (EventDeclSubject)e;
//                SimpleComponentSubject eventSubject = (SimpleComponentSubject)e;
                final SimpleIdentifierSubject eventSubject = (SimpleIdentifierSubject)e;
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
                    final GuardActionBlockSubject gab = new GuardActionBlockSubject();
                    edge.setGuardActionBlock(gab);
                }

                final Arc currArc = new Arc(fromState, toState, event);
                automaton.addArc(currArc);
            }

        }
        return automaton;
    }

    public ExtendedAutomataIndexMap getIndexMap()
    {
        return theIndexMap;
    }

    public Automata removeGuardsActionsFromEFAs(final ExtendedAutomata exAutomata)
    {
        final ArrayList<AbstractSubject> components = exAutomata.getComponents();
        final Automata automata = new Automata();
        final HashSet<SimpleComponentSubject> autComps = new HashSet<SimpleComponentSubject>();

        for(final AbstractSubject component : components)
        {
            if(component.toString().contains("NODES"))// && component.toString().contains("EDGES"))
                autComps.add((SimpleComponentSubject)component);
        }


        for(final SimpleComponentSubject autComp: autComps)
        {
            automata.addAutomaton(removeGuardsActionsFromEFA(autComp));
        }

        return automata;
    }

    public BDD[] getForwardTransAndNextValsForV(final BDDExtendedAutomaton aut)
    {
        return forwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getForwardTransWhereVisUpdated(final BDDExtendedAutomaton aut)
    {
        return forwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransAndNextValsForV(final BDDExtendedAutomaton aut)
    {
        return backwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransWhereVisUpdated(final BDDExtendedAutomaton aut)
    {
        return backwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD getMarkedLocations()
    {
        return markedLocationsBDD;
    }

    public BDD getForwardOverflows()
    {
        return forwardOverflows;
    }

    public BDD getBackwardOverflows()
    {
        return backwardOverflows;
    }

    public void setBackwardOverflows(final BDD overflows)
    {
        backwardOverflows = overflows;
    }

    public BDDPairing getDest2SourceLocationPairing()
    {
        return destToSourceLocationPairing;
    }

    public BDDPairing getDest2SourceVariablePairing()
    {
        return destToSourceVariablePairing;
    }

    public BDDPairing getSource2DestLocationPairing()
    {
        return sourceToDestLocationPairing;
    }

    public BDDPairing getSource2DestVariablePairing()
    {
        return sourceToDestVariablePairing;
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

    void add(final BDDExtendedAutomaton bddExAutomaton)
    {
        theBDDAutomataList.add(bddExAutomaton);
        automatonToBDDAutomatonMap.put(bddExAutomaton.getExAutomaton(), bddExAutomaton);
    }

    public Iterator<BDDExtendedAutomaton> iterator()
    {
        return theBDDAutomataList.iterator();
    }

    public BDDExtendedAutomaton getBDDExAutomaton(final ExtendedAutomaton theAutomaton)
    {
        return automatonToBDDAutomatonMap.get(theAutomaton);
    }

    public BDDExtendedAutomaton getBDDExAutomatonAt(final int i)
    {
        return automatonToBDDAutomatonMap.get(theIndexMap.getExAutomatonAt(i));
    }

    public HashSet<String> generateStates(final BDD states)
    {
        final BDD.BDDIterator satIt = states.iterator(sourceStateVariables);

        final HashSet<String> output = new HashSet<String>();
        while(satIt.hasNext())
        {
            final BDD truePathBDD = satIt.nextBDD();
            HashSet<String> subOutput = new HashSet<String>();
            boolean firstTime = true;
            for(final ExtendedAutomaton aut:orgExAutomata)
            {
                final HashSet<String> tempOutput = new HashSet<String>();
                for(final NodeProxy state:aut.getNodes())
                {
                    final int stateIndex = getLocationIndex(aut, state);
                    final BDD stateBDD = manager.getFactory().buildCube(stateIndex, getSourceLocationDomain(aut.getName()).vars());
                    if(truePathBDD.and(stateBDD).nodeCount() > 0)
                    {
                        if(!firstTime)
                        {
                            for(final String element:subOutput)
                            {
                                final String temp = element + "." + state.getName();
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
            firstTime = true;
            final HashSet<String> subVarOutput = new HashSet<String>();
            for(final VariableComponentProxy var:orgExAutomata.getVars())
            {
                final HashSet<String> tempOutput = new HashSet<String>();
                for(int val = theIndexMap.getInitValueofVar(var.getName());val<=orgExAutomata.getMaxValueofVar(var.getName());val++)
                {
                    final BDD varBDD = getConstantBDD(var.getName(), val);
                    if(truePathBDD.and(varBDD).nodeCount() > 0)
                    {
                        if(!firstTime)
                        {
                            for(final String element:subVarOutput)
                            {
                                final String temp = element + "," + val;
                                tempOutput.add(temp);
                            }
                        }
                        else
                        {
                            subVarOutput.add(""+val);
                        }
                    }
                }
                if(!firstTime)
                    subOutput = tempOutput;

                firstTime = false;
            }

            output.addAll(subOutput);
        }

        for(final String element:output)
        {
            System.out.println(element);
        }

        return output;
    }

}
