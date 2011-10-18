package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */

import gnu.trove.TIntArrayList;
import gnu.trove.TIntObjectHashMap;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.math.BigInteger;
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
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomataIndexMap;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.BDD.BDDAutomata;
import org.supremica.automata.BDD.SupremicaBDDBitVector.SupremicaBDDBitVector;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


public class BDDExtendedAutomata implements Iterable<BDDExtendedAutomaton>{

    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);

    public BDDExtendedManager manager;
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

    BDDVarSet sourceStateVariables = null;
    BDDVarSet destStateVariables = null;

    BDDDomain[] tempLocationDomains = null;
    BDDDomain[] sourceLocationDomains = null;
    BDDDomain[] destLocationDomains = null;

    BDDVarSet sourceLocationVariables = null;
    BDDVarSet destLocationVariables = null;

    BDDVarSet sourceVariablesVarSet = null;
    BDDVarSet destVariablesVarSet = null;

//    BDDVarSet constantsVarSet = null;

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

    long nbrOfReachableStates = -1;
    long nbrOfCoreachableStates = -1;
    long nbrOfNonblockingStates = -1;
    long nbrOfBlockingStates = -1;
    long nbrOfNonblockingControllableStates = -1;
    long nbrOfControllableStates = -1;

    HashMap<String,SupremicaBDDBitVector> BDDBitVecSourceVarsMap;
    HashMap<String,SupremicaBDDBitVector> BDDBitVecTargetVarsMap;

    BDD initValuesBDD = null;
    BDD markedValuesBDD = null;

    BDD[][] forwardTransWhereVisUpdated;
    BDD[][] forwardTransAndNextValsForV;

    BDD[][] backwardTransWhereVisUpdated;
    BDD[][] backwardTransAndNextValsForV;

    String locaVarSuffix;

    EditorSynthesizerOptions options;
    SynthesisAlgorithm synType;

    List<ExtendedAutomaton> plants;
    List<ExtendedAutomaton> specs;
    
    TIntArrayList plantUncontrollableEventIndexList;
    TIntArrayList specUncontrollableEventIndexList;
    
    TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;

    String pathRoot = "C:/Users/sajed/Desktop/MDD_files/";

    int BDDBitVectoryType = 0;

    int numberOfUsedBDDVariables = 0;

    List<Object> variableOrdering = null;
    List<String> variableOrderingNames;

    BDDExDisjDepSets depSets;

    public BDDExtendedAutomata(final ExtendedAutomata orgExAutomata, final  EditorSynthesizerOptions options)
    {
        this.orgExAutomata = orgExAutomata;
        locaVarSuffix = orgExAutomata.getlocVarSuffix();
        theIndexMap = new ExtendedAutomataIndexMap(orgExAutomata);
        theExAutomata = new PCGExtendedAutomataSorter().sortAutomata(orgExAutomata.getExtendedAutomataList());

        BDDBitVectoryType = orgExAutomata.isNegativeValuesIncluded()? 1 : 0;

//        theExAutomata = orgExAutomata.getExtendedAutomataList();

        BDDBitVecSourceVarsMap = new HashMap<String, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        BDDBitVecTargetVarsMap = new HashMap<String, SupremicaBDDBitVector>(orgExAutomata.getVars().size());

        variableOrderingNames = new ArrayList<String>();
        variableOrdering = new ArrayList<Object>();

        manager = new BDDExtendedManager();

        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();

        initialLocationsBDD = manager.getOneBDD();
        markedLocationsBDD = manager.getOneBDD();
        forbiddenLocationsBDD = manager.getZeroBDD();
        plantifiedBlockedLocationsBDD = manager.getZeroBDD();

        uncontrollableStatesBDD = manager.getZeroBDD();
        uncontrollableEventsBDD = manager.getZeroBDD();
        plantAlphabetBDD = manager.getZeroBDD();

        this.options = options;
        this.synType = options.getSynthesisAlgorithm();

        this.plants = new ArrayList<ExtendedAutomaton>();
        this.specs = new ArrayList<ExtendedAutomaton>();

        this.plantUncontrollableEventIndexList = new TIntArrayList();
        this.specUncontrollableEventIndexList = new TIntArrayList();

        initialize();
    }

    @SuppressWarnings("unchecked")
    void initialize()
    {
        unionAlphabet = orgExAutomata.getUnionAlphabet();
        if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD)) {
            event2AutomatonsEdges = new TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>(unionAlphabet.size());
        }

        sourceStateVariables = manager.createEmptyVarSet();
        destStateVariables = manager.createEmptyVarSet();

        sourceLocationVariables = manager.createEmptyVarSet();
        destLocationVariables = manager.createEmptyVarSet();

        sourceVariablesVarSet = manager.createEmptyVarSet();
        destVariablesVarSet = manager.createEmptyVarSet();

        sourceLocationDomains = new BDDDomain[orgExAutomata.size()];
        destLocationDomains = new BDDDomain[orgExAutomata.size()];
        tempLocationDomains = new BDDDomain[orgExAutomata.size()];

        sourceVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        destVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        tempVarDomains = new BDDDomain[orgExAutomata.getVars().size()];

        forwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        forwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

        backwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        backwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

/*
        if(orgExAutomata.getDomain() > 0 )
        {
            constantDomain = manager.createDomain(orgExAutomata.getDomain());
//        System.out.println("constant variables: "+constantDomain.set().toString());
        }
*/

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
        setVariableOrdering();
        // Set after the IDD bug is fixed.
//        if(synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
//            setVariableOrdering();
//        else{
//            FORCEAutomatonVariableSorter forceSorter = new FORCEAutomatonVariableSorter(orgExAutomata);
//            forceSorter.sort();
//
//            variableOrdering.addAll(forceSorter.getVariableOrdering());
//            variableOrdering.add(unionAlphabet);
//
//            variableOrderingNames.addAll(forceSorter.getVariableOrderingNames());
//            variableOrderingNames.add("Events");
//        }

        //Manually set the variable ordering
//        manuallySetVariableOrderingNames();
//        setVariableOrdering(variableOrderingNames);

        for(final Object obj:variableOrdering)
        {
            if(obj instanceof List<?>)
            {
                eventDomain = manager.createDomain(((List<EventDeclProxy>) obj).size());
                eventDomain.setName("Events");
                numberOfUsedBDDVariables += eventDomain.varNum();
//                System.err.println("event variables: "+eventDomain.set().toString());
        /*        for(EventDeclProxy e: orgExAutomata.unionAlphabet)
                {
                    System.out.println(e.getName()+": "+getEventIndex(e));
                }
        */
                for(final EventDeclProxy event:((List<EventDeclProxy>) obj))
                {
                    final int currEventIndex = getEventIndex(event);
                    if(!synType.equals(SynthesisAlgorithm.MONOLITHICBDD)){
                        event2AutomatonsEdges.put(currEventIndex, new HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>());
                    }
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
            }

            if(obj instanceof ExtendedAutomaton)
            {
                final String automatonName = ((ExtendedAutomaton)obj).getName();


    //            System.err.println(automaton.getName());
                final int autIndex = theIndexMap.getExAutomatonIndex(automatonName);
                final int nbrOfStates = ((ExtendedAutomaton)obj).nbrOfNodes();
                final BDDDomain tempLocationDomain = manager.createDomain(nbrOfStates);

                final BDDDomain sourceLocationDomain = manager.createDomain(nbrOfStates);
                numberOfUsedBDDVariables += sourceLocationDomain.varNum();

                final int[] sourceVars = sourceLocationDomain.vars();
                for(int i = 0; i<sourceVars.length; i++)
                {
                    final int[] sourceVar = new int[1];
                    sourceVar[0] = sourceVars[i];
                    sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
                    sourceLocationVars.add(sourceVars[i]);
                    bddVar2AutVarName.put(sourceVars[i], automatonName);
                }

//                System.err.println("sourceLocation variables: "+sourceLocationDomain.set().toString());
                final BDDDomain destLocationDomain = manager.createDomain(nbrOfStates);
                numberOfUsedBDDVariables += destLocationDomain.varNum();

    //            System.err.println("destLocation variables: "+destLocationDomain.set().toString());
    /*            for(NodeProxy loc: automaton.getNodes())
                {
                    System.out.println(loc.getName()+": "+getLocationIndex(automaton, loc));
                }
    */
                sourceLocationVariables.unionWith(sourceLocationDomain.set());
                destLocationVariables.unionWith(destLocationDomain.set());
                sourceLocationDomains[autIndex] = sourceLocationDomain;
                sourceLocationDomains[autIndex].setName(automatonName);
                destLocationDomains[autIndex] = destLocationDomain;
                destLocationDomains[autIndex].setName(automatonName);

                tempLocationDomains[autIndex] = tempLocationDomain;
            }

            if(obj instanceof VariableComponentProxy)
            {
                initializeVariable(((VariableComponentProxy)obj));
            }

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

            if (automaton.isSpecification()) {
                specs.add(automaton);
            } else {
                plants.add(automaton);
            }

            bddExAutomaton.initialize();

            add(bddExAutomaton);
        }

        sourceStateVariables = sourceStateVariables.union(sourceLocationVariables);
        sourceStateVariables = sourceStateVariables.union(sourceVariablesVarSet);

        destStateVariables = destStateVariables.union(destLocationVariables);
        destStateVariables = destStateVariables.union(destVariablesVarSet);

        sourceToTempLocationPairing = manager.makePairing(sourceLocationDomains, tempLocationDomains);
        tempToDestLocationPairing = manager.makePairing(tempLocationDomains, destLocationDomains);
        destToSourceLocationPairing = manager.makePairing(destLocationDomains, sourceLocationDomains);

        sourceToDestLocationPairing = manager.makePairing(sourceLocationDomains,destLocationDomains);

        if (options.getSynthesisAlgorithm().equals(SynthesisAlgorithm.MONOLITHICBDD))
        {
            bddEdges = new BDDEdgeFactory(this).createEdges();
        }

        System.err.println("Variable ordering: "+variableOrderingNames);
//        for(int i = 0; i < manager.getFactory().getVarOrder().length; i++)
//            System.out.println(i+": "+manager.getFactory().getVarOrder()[i]);
//        System.out.println("number of transitions: "+((BDDMonolithicTransitions)bddTransitions).transitionForwardBDD.pathCount());
    }

    //Set variable ordering based on PCG sorters
    void setVariableOrdering()
    {
        variableOrdering.add(unionAlphabet);
        variableOrderingNames.add("Events");

        final List<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(new PCGVariableSorter(orgExAutomata).sortVars(orgExAutomata.getVars()));
//        ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(orgExAutomata.getVars());

        for(final ExtendedAutomaton automaton:theExAutomata)
        {

            variableOrdering.add(automaton);
            variableOrderingNames.add(automaton.getName());

            //Place the variables that are related to this automaton (the variables that are updated in this automaton)
            for(final VariableComponentProxy varRelatedToAutomaton:automaton.getUsedTargetVariables())
            {
                if(sortedVarList.contains(varRelatedToAutomaton))
                {
                    variableOrdering.add(varRelatedToAutomaton);
                    variableOrderingNames.add(varRelatedToAutomaton.getName());
                }
            }

            sortedVarList.removeAll(automaton.getUsedTargetVariables());

        }

        for(final VariableComponentProxy var:sortedVarList)
        {
            variableOrdering.add(var);
            variableOrderingNames.add(var.getName());
        }

        variableOrderingNames.add("1");

    }

    void setVariableOrdering(final List<String> varOrderingNames)
    {
        for(final String varName:varOrderingNames)
        {
            if(!varName.equals("1"))
            {
                if(varName.equals("Events"))
                {
                    variableOrdering.add(unionAlphabet);
                }
                else
                {

                    final int isAutomaton = theIndexMap.isStringEFAorVar(varName);
                    if(isAutomaton == 0)
                    {
                        variableOrdering.add(theIndexMap.getExAutomatonWithName(varName));
                    }
                    else if(isAutomaton == 1)
                    {
                        variableOrdering.add(theIndexMap.getVariableAt(theIndexMap.getVariableIndexByName(varName)));
                    }
                }
            }
        }
    }

    void manuallySetVariableOrderingNames()
    {
        variableOrderingNames = new ArrayList<String>();
        variableOrderingNames.add("Events");
        variableOrderingNames.add("Clock");
        variableOrderingNames.add("time");
        variableOrderingNames.add("m0");
        variableOrderingNames.add("m1");
        variableOrderingNames.add("m2");
        variableOrderingNames.add("m3");
        variableOrderingNames.add("m4");
        variableOrderingNames.add("m5");
        variableOrderingNames.add("P2");
        variableOrderingNames.add("c2");
        variableOrderingNames.add("P5");
        variableOrderingNames.add("c5");
        variableOrderingNames.add("P6");
        variableOrderingNames.add("c6");
        variableOrderingNames.add("P4");
        variableOrderingNames.add("c4");
        variableOrderingNames.add("P3");
        variableOrderingNames.add("c3");
        variableOrderingNames.add("P1");
        variableOrderingNames.add("c1");
        variableOrderingNames.add("1");
    }

    public void setPathRoot(final String pr)
    {
        pathRoot = pr;
    }

    public void initializeVariable(final VariableComponentProxy var)
    {
        final String varName = var.getName();
        final int varIndex = theIndexMap.getVariableIndex(var);
//        System.err.println("variable name: "+varName);

//        int domain = orgExAutomata.getDomain();
        final int domain = orgExAutomata.getVarDomain(varName);

        final BDDDomain tempVarDomain = manager.createDomain(domain);
        manager.partialReverseVarOrdering(tempVarDomain.vars());
//        System.err.println("tempVar variables: "+tempVarDomain.set().toString());
        final BDDDomain sourceVarDomain = manager.createDomain(domain);
        manager.partialReverseVarOrdering(sourceVarDomain.vars());

        numberOfUsedBDDVariables += sourceVarDomain.varNum();

        final int[] sourceVars = sourceVarDomain.vars();
        for(int i = 0; i<sourceVars.length; i++)
        {
            final int[] sourceVar = new int[1];
            sourceVar[0] = sourceVars[i];
            sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
            bddVar2AutVarName.put(sourceVars[i], varName);
        }

//        System.err.println("sourceVar variables: "+sourceVarDomain.set().toString());
        BDDBitVecSourceVarsMap.put(var.getName(), manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),sourceVarDomain));
        sourceVariablesVarSet.unionWith(sourceVarDomain.set());

        final BDDDomain destVarDomain = manager.createDomain(domain);
        manager.partialReverseVarOrdering(destVarDomain.vars());

        numberOfUsedBDDVariables += destVarDomain.varNum();
//        System.err.println("destVar variables: "+destVarDomain.set().toString());
        BDDBitVecTargetVarsMap.put(var.getName(), manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),destVarDomain));
        destVariablesVarSet.unionWith(destVarDomain.set());

        sourceVarDomains[varIndex] = sourceVarDomain;
        sourceVarDomains[varIndex].setName(var.getName());
        destVarDomains[varIndex] = destVarDomain;
        destVarDomains[varIndex].setName(var.getName());
        tempVarDomains[varIndex] = tempVarDomain;
    }

    public int getNumberOfUsedBDDVariables()
    {
        return numberOfUsedBDDVariables;
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

    public BDDDomain getSourceVariableDomain(final String variable)
    {
        return sourceVarDomains[theIndexMap.getVariableIndexByName(variable)];
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
        final BDD impBDD = getReachableStates().imp(getCoreachableStates());
        return impBDD.isOne();
    }

    public void done()
    {
        if (manager != null)
            manager.done();
    }

    public BDD getInitialState()
    {
 //       return (initialLocationsBDD);
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
            BDD markedVals = manager.getOneBDD();
            if(theIndexMap.getMarkedPredicatesofVar(var.getName()).size() > 0)
            {
                markedVals = manager.getZeroBDD();
                for(final VariableMarkingProxy vmp: theIndexMap.getMarkedPredicatesofVar(var.getName()))
                {
                    markedVals = markedVals.or(manager.guard2BDD(vmp.getPredicate()));
                }
            }
/*            if(theIndexMap.getMarkedPredicatesofVar(var.getName()).size() == 0)
            {
                String markedPredicate = var.getName()+"==0";
                final int range = orgExAutomata.getMaxValueofVar(var.getName())-orgExAutomata.getMinValueofVar(var.getName());
                for(int i=1;i<=range;i++)
                {
                    markedPredicate += ("|"+var.getName()+"=="+i);
                }
                try
                {
                    final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
                    markedVals.orWith(manager.guard2BDD((SimpleExpressionSubject)(parser.parse(markedPredicate,Operator.TYPE_BOOLEAN))));
                }
                catch(final ParseException pe)
                {
                    System.err.println(pe);
                    break;
                }
            }
*/
            markedValuesBDD.andWith(markedVals);
        }
    }

    public SupremicaBDDBitVector getMaxBDDBitVecOf(final String variable)
    {
        return manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),
                getBDDBitVecSource(variable).length(),orgExAutomata.getMaxValueofVar(variable));
    }

    public SupremicaBDDBitVector getMinBDDBitVecOf(final String variable)
    {
        return manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),
                getBDDBitVecSource(variable).length(),orgExAutomata.getMinValueofVar(variable));
    }

    public BDD getConstantBDD(final String varName, final int cons)
    {
        final SupremicaBDDBitVector c = manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),getBDDBitVecSource(varName).length(),cons);

        final BDD result = manager.getOneBDD();
        for(int i=0;i<c.length();i++)
        {
            result.andWith(getBDDBitVecSource(varName).getBit(i).biimp(c.getBit(i)));
        }

        return result;
    }

    public BDD getReachableStates()
    {
        if (reachableStatesBDD == null)
        {
            if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
            {
               //reachableStatesBDD = BDDExDisjunctiveReachabilityAlgorithms.restrictedForwardWorkSetAlgorithm(this, getInitialState(), manager.getZeroBDD());
               //reachableStatesBDD = BDDExDisjunctiveHeuristicReachabilityAlgorithms.forwardWorkSetAlgorithm(this, getInitialState(), manager.getZeroBDD());
               getDepSets();
               reachableStatesBDD = depSets.forwardWorkSetAlgorithm(getInitialState());
            }
            else
            {
                reachableStatesBDD = manager.restrictedForward(manager.getZeroBDD());
            }

            System.err.println("Reachable states computed!");
            
            //nbrOfReachableStates = nbrOfStatesBDD(reachableStatesBDD);
            final IDD idd = generateIDD(reachableStatesBDD, reachableStatesBDD);
            nbrOfReachableStates = nbrOfStatesIDD(idd).longValue();
//            nbrOfReachableStates = -1;


//            logger.info("Number of reachable states in the closed-loop system: "+nbrOfReachableStates);
        }
        return reachableStatesBDD;
    }

    public double getNbrOfRecahableStates()
    {
        if(nbrOfReachableStates == -1)
            getReachableStates();

        return nbrOfReachableStates;
    }

    BDD getCoreachableStates()
    {
        if (coreachableStatesBDD == null)
        {
            if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
            {
                //coreachableStatesBDD = getDepSets().reachableBackwardWorkSetAlgorithm(getMarkedStates(), getReachableStates()); 
                coreachableStatesBDD = getDepSets().backwardWorkSetAlgorithm(getMarkedStates());
            }
            else
            {
                coreachableStatesBDD = manager.restrictedBackward(manager.getZeroBDD());
            }

            System.err.println("Coreachable states computed!");
            //nbrOfCoreachableStates = nbrOfStatesBDD(coreachableStatesBDD);
            final IDD idd = generateIDD(coreachableStatesBDD, coreachableStatesBDD);
            nbrOfCoreachableStates = nbrOfStatesIDD(idd).longValue();
//            nbrOfCoreachableStates = 1;
        }

        return coreachableStatesBDD;
    }

    public BDD getNonblockingControllableStates(final boolean reachable)
    {

        if(nonblockingControllableStatesBDD == null)
        {
            if(synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
            {
                nonblockingControllableStatesBDD = manager.nonblockingControllable(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()),reachable);
            }
            else
            {
                nonblockingControllableStatesBDD = manager.disjunctiveNonblockingControllable
                        (manager.getDisjunctiveInitiallyUncontrollableStates().or(getForbiddenLocations()), reachable);
            }

            System.err.println("Nonblocking and controllable states computed!");
            //nbrOfNonblockingControllableStates = nbrOfStatesBDD(nonblockingControllableStatesBDD);

            final IDD idd = generateIDD(nonblockingControllableStatesBDD, nonblockingControllableStatesBDD);
            nbrOfNonblockingControllableStates = nbrOfStatesIDD(idd).longValue();
        }

        return nonblockingControllableStatesBDD;
    }

    public BDD getControllableStates(final boolean reachable)
    {
        if(controllableStatesBDD == null)
        {
            if (synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
            {
                final BDD uncontrollableStates = manager.getInitiallyUncontrollableStates().or(getForbiddenLocations());
//             BDD uncontrollableStates = manager.uncontrollableBackward(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()));
                if (reachable) {
                    controllableStatesBDD = (manager.restrictedForward(uncontrollableStates));

                } else {
                    controllableStatesBDD = uncontrollableStates.not();
                }
            }
            else
            {
                uncontrollableStatesBDD = manager.getDisjunctiveInitiallyUncontrollableStates().or(getForbiddenLocations());
                if (reachable) {
                     controllableStatesBDD = getDepSets().forwardRestrictedWorkSetAlgorithm(getInitialState(), uncontrollableStatesBDD);
                } else {
                    controllableStatesBDD = uncontrollableStatesBDD.not();
                }
            }

            System.err.println("Controllable states computed!");

            //nbrOfControllableStates = nbrOfStatesBDD(controllableStatesBDD);
            final IDD idd = generateIDD(controllableStatesBDD, controllableStatesBDD);
            nbrOfControllableStates = nbrOfStatesIDD(idd).longValue();
        }

        return controllableStatesBDD;
    }

    public BDD getNonblockingStates()
    {
        if (nonblockingStatesBDD == null)
        {
//            System.err.println("Marked states: "+nbrOfStatesBDD(getMarkedStates()));
            System.err.println("Start computing reachable states...");
            reachableStatesBDD = getReachableStates();
            System.err.println("Start computing coreachable states...");
//            reachableStatesBDD.printDot();
            coreachableStatesBDD = getCoreachableStates();

//            coreachableStatesBDD.printDot();

//            nonblockingStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
            if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD))
            {
                nonblockingStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
            }
            else
            {
                nonblockingStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
//                final IDD idd = generateIDD(nonblockingStatesBDD, nonblockingStatesBDD);
//                nbrOfNonblockingStates = nbrOfStatesIDD(idd, new HashMap<IDDNode, BigInteger>()).longValue();
            }


//            BDD2IDD2PS(nonblockingStatesBDD, nonblockingStatesBDD, "nonblockingStates");
            System.err.println("Nonblocking states computed!");
            final IDD idd = generateIDD(nonblockingStatesBDD, nonblockingStatesBDD);
            nbrOfNonblockingStates = nbrOfStatesIDD(idd).longValue();
//            nbrOfNonblockingStates = 1;

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

    public IDD generateIDD(final BDD bdd, final BDD validStatesBDD)
    {
        System.err.println("generating IDD...");
        final HashMap<Integer, IDD> visitedNodes = new HashMap<Integer, IDD>();
        visitedNodes.put(1, new IDD(new IDDNode("1", "1")));
        IDD idd = null;
        if(bdd.isZero())
            idd = new IDD(new IDDNode("0","0"));

        if(bdd.isOne())
            idd = new IDD(new IDDNode("1","1"));

        if(bdd.nodeCount() > 0)
        {
            final IDDNode root = new IDDNode(""+bdd.hashCode(),getAutVarName(bdd.var()));
            idd = new IDD(root);
            final BDD varBDD = sourceBDDVar2BDD.get(bdd.var());

            BDD2IDD(bdd.low(), varBDD.not(), idd, visitedNodes, validStatesBDD);
            BDD2IDD(bdd.high(), varBDD, idd, visitedNodes, validStatesBDD);
        }

        System.err.println("IDD generated.");

        return idd;
    }

    public void BDD2IDD(final BDD bdd, final BDD autStatesBDD, final IDD idd, final HashMap<Integer, IDD> visitedNodes, final BDD validStatesBDD)
    {
//        System.err.println("BDD2IDD()");
        if(!bdd.isZero())
        {
            if(bdd.isOne() || !getAutVarName(bdd.var()).equals(idd.getRoot().getName()))
            {
                final ArrayList<String> states = bdd2automatonStates(autStatesBDD,validStatesBDD);

                IDD nextIDD = visitedNodes.get(bdd.hashCode());

                //if 'node' has not been visited
                if(nextIDD == null)
                {
                    final IDDNode node = new IDDNode(""+bdd.hashCode(),getAutVarName(bdd.var()));
                    nextIDD = new IDD(node);

                    final BDD varBDD = getBDDforSourceBDDVar(bdd.var());
                    BDD2IDD(bdd.low(), varBDD.not(), nextIDD, visitedNodes, validStatesBDD);
                    BDD2IDD( bdd.high(), varBDD, nextIDD, visitedNodes, validStatesBDD);

                    if(!states.isEmpty())
                        idd.addChild(nextIDD, states);

                    visitedNodes.put(bdd.hashCode(), nextIDD);
                }
                else
                {
                    if(!states.isEmpty())
                    {
                        if(idd.labelOfChild(nextIDD) != null)//'idd' has a child with root 'node'
                            idd.labelOfChild(nextIDD).addAll(states);
                        else
                            idd.addChild(nextIDD, states);
                    }
                }
            }
            else
            {
                final BDD varBDD = getBDDforSourceBDDVar(bdd.var());

                BDD2IDD(bdd.low(),  autStatesBDD.and(varBDD.not()), idd, visitedNodes, validStatesBDD);
                BDD2IDD(bdd.high(), autStatesBDD.and(varBDD), idd, visitedNodes, validStatesBDD);
            }
        }
    }

    ArrayList<String> bdd2automatonStates(final BDD autStatesBDD, final BDD validStatesBDD)
    {
//        System.err.println("bdd2automatonStates()");
        final ArrayList<String> output = new ArrayList<String>();
        final int var = autStatesBDD.var();

        if(isSourceLocationVar(var))
        {
            final ExtendedAutomaton exAut = theIndexMap.getExAutomatonWithName(getAutVarName(var));
            for(final NodeProxy location:exAut.getNodes())
            {
                final int locationIndex = getLocationIndex(exAut, location);
                final BDD locationBDD = manager.getFactory().buildCube(locationIndex, getSourceLocationDomain(exAut.getName()).vars());
                if(!autStatesBDD.and(locationBDD).isZero() && !locationBDD.and(validStatesBDD).isZero())
                {
                    output.add(location.getName());
                }
            }
        }
        else
        {
            final int maxValue = getExtendedAutomata().getMaxValueofVar(getAutVarName(var));
            final int minValue = getExtendedAutomata().getMinValueofVar(getAutVarName(var));
            for(int i=minValue;i<=maxValue;i++)
            {
                final BDD valueBDD = getConstantBDD(getAutVarName(var), i);
                if(!autStatesBDD.and(valueBDD).isZero() && !valueBDD.and(validStatesBDD).isZero())
                {
                    output.add(""+getIndexMap().getValOfIndex(i));
                }
            }

        }

        return output;
    }

    public void BDD2IDD2PS(final BDD bdd, final BDD validStatesBDD, final String fileName)
    {
        final String absPathDot = pathRoot+standardizePathAddress(fileName)+".dot";
        final String absPathPs = pathRoot+standardizePathAddress(fileName)+".ps";
        generateDOT(generateIDD(bdd, validStatesBDD), absPathDot);
        final Runtime rt = Runtime.getRuntime();
        try{
            final Process proc1 = rt.exec("dot -Tps "+absPathDot+" -o "+absPathPs);
            proc1.waitFor();
            proc1.exitValue();
            final Process proc2 = rt.exec("cmd /C del "+absPathDot);
            proc2.waitFor();
            proc2.exitValue();
        }catch(final Exception e) {System.out.println(e);}

    }

    public void IDD2DOT(final BufferedWriter out, final IDD idd, final HashSet<IDD> visited)
    {
        final IDDNode root = idd.getRoot();
        try
        {
            if(!visited.contains(idd))
            {
                out.write(""+root.getID()+" [label=\""+root.getName()+"\"];");
                out.newLine();
                visited.add(idd);

                for(final IDD child:idd.getChildren())
                {
                    String temp = ""+root.getID()+" -> "+child.getRoot().getID()+" [label=\"";
                    final ArrayList<String> label = idd.labelOfChild(child);
                    if(label.size() > 0)
                        temp += label.get(0);
                    for(int i = 1;i < label.size(); i++)
                    {
                        temp += ("|" + label.get(i));
                    }

                    out.write(temp+"\"];");
                    out.newLine();
                    IDD2DOT(out, child, visited);

                }
            }
        }
        catch (final Exception e)
        {
            logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public String standardizePathAddress(final String path)
    {
        String sPath = path.replace('/' , '_');
        sPath = sPath.replace('\\', '_');
        sPath = sPath.replace(':', '_');
        sPath = sPath.replace('*', '_');
        sPath = sPath.replace('"', '_');
        sPath = sPath.replace('<', '_');
        sPath = sPath.replace('>', '_');
        sPath = sPath.replace('|', '_');
        return sPath;
    }

    public void generateDOT(final IDD idd, final String path)
    {
        try
        {
            final FileWriter fstream = new FileWriter(path);
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("digraph G {");
            out.newLine();
            out.write("size = \"7.5,10\"");
            out.newLine();
//            out.write("0 [shape=box, label=\"0\", style=filled, shape=box, height=0.3, width=0.3];");
//            out.newLine();
            out.write("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");
            out.newLine();
            final HashSet<IDD> visited = new HashSet<IDD>();
            if(idd.nbrOfNodes() > 1)
            {
                IDD2DOT(out, idd, visited);
            }
            out.write("}");
            out.close();
        }
        catch (final Exception e)
        {
           logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public BigInteger nbrOfStatesIDD(final IDD idd)
    {
        //iterate through the idd-variables and find the first one that corresponds to a varibale or automaton
        if(idd.getRoot().getName().equals("0"))
            return BigInteger.ZERO;
        
        String firstVarAut = "";
        for(Object va:variableOrdering)
        {
            if(va instanceof ExtendedAutomaton)
            {
                firstVarAut = ((ExtendedAutomaton)va).getName();
                break;
            }
            if(va instanceof VariableComponentProxy)
            {
                firstVarAut = ((VariableComponentProxy)va).getName();
                break;
            }
        }
        int indexVar1 = variableOrderingNames.indexOf(firstVarAut);
        int indexVar2 = variableOrderingNames.indexOf(idd.getRoot().getName());
        return nbrOfStatesIDD(idd,new HashMap<IDDNode,BigInteger>()).multiply(statesBetweenVars(indexVar1,indexVar2));
    }


    public BigInteger nbrOfStatesIDD(final IDD idd, final HashMap<IDDNode,BigInteger> cache)
    {
        BigInteger nbrOfStates = BigInteger.ZERO;
        if(idd.isOneTerminal())
        {
            return BigInteger.ONE;
        }
        else
        {
            for(final IDD child: idd.getChildren())
            {
                int indexVar1 = variableOrderingNames.indexOf(idd.getRoot().getName());
                int indexVar2 = variableOrderingNames.indexOf(child.getRoot().getName());
                final BigInteger statesBetweenChildren = statesBetweenVars(indexVar1+1,indexVar2);
                final BigInteger currStates = BigInteger.valueOf(idd.labelOfChild(child).size());
                BigInteger newNbrOfStates = cache.get(child.getRoot());
                if(newNbrOfStates == null)
                {
                    newNbrOfStates = nbrOfStatesIDD(child, cache);
                }
                nbrOfStates = nbrOfStates.add(statesBetweenChildren.multiply(newNbrOfStates.multiply(currStates)));
            }
        }
        cache.put(idd.getRoot(), nbrOfStates);
        return nbrOfStates;

    }

    public BigInteger statesBetweenVars(final int indexVar1, final int indexVar2)
    {
        BigInteger output = BigInteger.ONE;
        if(!(variableOrderingNames.get(indexVar1)).equals("1"))
        {
            for(final String var:variableOrderingNames.subList(indexVar1, indexVar2))
            {
                final int isAutomaton = theIndexMap.isStringEFAorVar(var);
                if(isAutomaton == 0)
                    output = output.multiply(BigInteger.valueOf(theIndexMap.getExAutomatonWithName(var).getNodes().size()));
                else if(isAutomaton == 1)
                    output = output.multiply(BigInteger.valueOf(orgExAutomata.getVarDomain(var)));
            }
        }
        return output;
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

    public ExtendedAutomataIndexMap getIndexMap()
    {
        return theIndexMap;
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

    public SynthesisAlgorithm getSynthAlg() {
       return synType;
    }
    
    public BDDExDisjDepSets getDepSets() {
        if(depSets == null)
            depSets = BDDExDisjPartitioningTypeFactory.getDepSets(this, synType);
        return depSets;
    }
}