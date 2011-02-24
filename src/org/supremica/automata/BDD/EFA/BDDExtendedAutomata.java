package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi
 */

import java.util.*;
import java.util.ArrayList;

import net.sf.javabdd.*;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;
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


    public BDDExtendedAutomata(ExtendedAutomata orgExAutomata)
    {
        this.orgExAutomata = orgExAutomata;
        locaVarSuffix = orgExAutomata.getlocVarSuffix();
        theIndexMap = new ExtendedAutomataIndexMap(orgExAutomata);
        theExAutomata = new PCGExtendedAutomataSorter().sortAutomata(orgExAutomata.getExtendedAutomataList());

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
        for(EventDeclProxy event:unionAlphabet)
        {
            int currEventIndex = getEventIndex(event);
            BDD eventBDD = manager.createBDD(currEventIndex, eventDomain);
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


        ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(new PCGVariableSorter(orgExAutomata).sortVars(orgExAutomata.getVars()));
//        ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(orgExAutomata.getVars());

        for(ExtendedAutomaton automaton:theExAutomata){ 

//            System.out.println(automaton.getName());
            int autIndex = theIndexMap.getExAutomatonIndex(automaton.getName());
            int nbrOfStates = automaton.nbrOfNodes();
            BDDDomain tempLocationDomain = manager.createDomain(nbrOfStates);

            BDDDomain sourceLocationDomain = manager.createDomain(nbrOfStates);
            int[] sourceVars = sourceLocationDomain.vars();
            for(int i = 0; i<sourceVars.length; i++)
            {
                int[] sourceVar = new int[1];
                sourceVar[0] = sourceVars[i];
                sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
                sourceLocationVars.add(sourceVars[i]);
                bddVar2AutVarName.put(sourceVars[i], automaton.getName());
            }

//            System.out.println("sourceLocation variables: "+sourceLocationDomain.set().toString());
            BDDDomain destLocationDomain = manager.createDomain(nbrOfStates);
//            System.out.println("destLocation variables: "+destLocationDomain.set().toString());
/*            for(NodeProxy loc: automaton.getNodes())
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
            for(VariableComponentProxy varRelatedToAutomaton:automaton.getUsedTargetVariables())
            {
                if(sortedVarList.contains(varRelatedToAutomaton))
                {
                    initializeVariable(varRelatedToAutomaton);
                }
            }
            sortedVarList.removeAll(automaton.getUsedTargetVariables());

        }

        for(VariableComponentProxy var:sortedVarList)
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


        for(ExtendedAutomaton automaton:theExAutomata)
        {
            BDDExtendedAutomaton bddExAutomaton = new BDDExtendedAutomaton(this, automaton);

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

        bddEdges = new BDDEdgeFactory(this).createEdges();

//        System.out.println("number of transitions: "+((BDDMonolithicTransitions)bddTransitions).transitionForwardBDD.pathCount());
    }

    public void initializeVariable(VariableComponentProxy var)
    {
        varNames.add(var.getName());
        int varIndex = theIndexMap.getVariableIndex(var);
//        System.out.println("variable name: "+var.getName());

        BDDDomain tempVarDomain = manager.createDomain(orgExAutomata.getDomain());
//            System.out.println("tempVar variables: "+tempVarDomain.set().toString());
        BDDDomain sourceVarDomain = manager.createDomain(orgExAutomata.getDomain());

        int[] sourceVars = sourceVarDomain.vars();
        for(int i = 0; i<sourceVars.length; i++)
        {
            int[] sourceVar = new int[1];
            sourceVar[0] = sourceVars[i];
            sourceBDDVar2BDD.put(sourceVars[i], manager.getFactory().buildCube(1, sourceVar));
            bddVar2AutVarName.put(sourceVars[i], var.getName());
        }

//        System.out.println("sourceVar variables: "+sourceVarDomain.set().toString());
        BDDBitVecSourceVarsMap.put(var.getName(), new SupremicaBDDBitVector(manager.getFactory(),sourceVarDomain));
        sourceVariablesVarSet.unionWith(sourceVarDomain.set());

        BDDDomain destVarDomain = manager.createDomain(orgExAutomata.getDomain());
//            System.out.println("destVar variables: "+destVarDomain.set().toString());
        BDDBitVecTargetVarsMap.put(var.getName(), new SupremicaBDDBitVector(manager.getFactory(),destVarDomain));
        destVariablesVarSet.unionWith(destVarDomain.set());

        sourceVarDomains[varIndex] = sourceVarDomain;
        sourceVarDomains[varIndex].setName(var.getName());
        destVarDomains[varIndex] = destVarDomain;
        destVarDomains[varIndex].setName(var.getName());
        tempVarDomains[varIndex] = tempVarDomain;
    }

    public boolean isSourceLocationVar(int var)
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

    public String getAutVarName(int var)
    {
        return bddVar2AutVarName.get(var);
    }

    public BDD getBDDforSourceBDDVar(int bddVar)
    {
        return sourceBDDVar2BDD.get(bddVar);
    }

    public BDDDomain getSourceLocationDomain(String automaton)
    {
        return sourceLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
    }

    public BDDDomain getDestLocationDomain(String automaton)
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

    public SupremicaBDDBitVector getBDDBitVecSource(String name)
    {
        return BDDBitVecSourceVarsMap.get(name);
    }

    public SupremicaBDDBitVector getBDDBitVecTarget(String name)
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

    public void addForbiddenLocations(BDD forbiddenLocations)
    {
        forbiddenLocationsBDD = forbiddenLocationsBDD.or(forbiddenLocations);
    }

    public void addPlantifiedBlockedLocations(BDD forbiddenUnconLocations)
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

    public double numberOfControllableStates(boolean reachable)
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

    public double numberOfNonblockingControllableStates(boolean reachable)
    {
        if (nbrOfNonblockingControllableStates < 0)
        {
            getNonblockingControllableStates(reachable);
        }
        return nbrOfNonblockingControllableStates;
    }

    public boolean isNonblocking()
    {
        BDD reachableStatesBDD = getReachableStates();
        BDD coreachableStatesBDD = getCoreachableStates();
        BDD impBDD = reachableStatesBDD.imp(coreachableStatesBDD);
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
        for(VariableComponentProxy var:orgExAutomata.getVars())
        {
            int initValue = theIndexMap.getInitValueofVar(var.getName());
            initValuesBDD.andWith(getConstantBDD(var.getName(),initValue));
        }
    }

    public void computeMarkedValues()
    {
        markedValuesBDD = manager.getOneBDD();
        for(VariableComponentProxy var:orgExAutomata.getVars())
        {
            BDD markedVals = manager.getZeroBDD();
            for(VariableMarkingProxy vmp: theIndexMap.getMarkedPredicatesofVar(var.getName()))
            {
                markedVals.orWith(manager.guard2BDD(vmp.getPredicate()));
            }
            if(theIndexMap.getMarkedPredicatesofVar(var.getName()).size() == 0)
            {

                String markedPredicate = var.getName()+"==0";
                int range = orgExAutomata.getMaxValueofVar(var.getName())-orgExAutomata.getMinValueofVar(var.getName());
                for(int i=1;i<=range;i++)
                {
                    markedPredicate += ("|"+var.getName()+"=="+i);
                }
                try
                {
                    ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
                    markedVals.orWith(manager.guard2BDD((SimpleExpressionSubject)(parser.parse(markedPredicate,Operator.TYPE_BOOLEAN))));
                }
                catch(final ParseException pe)
                {
                    System.err.println(pe);
                    break;
                }
 
 
//                markedVals = manager.getOneBDD();
            }

            markedValuesBDD.andWith(markedVals);
        }
    }

    public SupremicaBDDBitVector getMaxBDDBitVecOf(String variable)
    {
        return new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),orgExAutomata.getMaxValueofVar(variable));
    }

    public SupremicaBDDBitVector getMinBDDBitVecOf(String variable)
    {
        return new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),orgExAutomata.getMinValueofVar(variable));
    }

    public BDD getConstantBDD(String varName, int cons)
    {
        SupremicaBDDBitVector c = new SupremicaBDDBitVector(manager.getFactory(),constantDomain.varNum(),cons);

        BDD result = manager.getOneBDD();
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
//            logger.info("Number of reachable states in the closed-loop system: "+nbrOfReachableStates);
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

    public BDD getNonblockingControllableStates(boolean reachable)
    {

        if(nonblockingControllableStatesBDD == null)
        {
            nonblockingControllableStatesBDD = manager.nonblockingControllable(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()),reachable);
            nbrOfNonblockingControllableStates = nbrOfStatesBDD(nonblockingControllableStatesBDD);
        }

        return nonblockingControllableStatesBDD;
    }

    public BDD getControllableStates(boolean reachable)
    {
        if(controllableStatesBDD == null)
        {
            BDD uncontrollableStates = manager.getInitiallyUncontrollableStates().or(getForbiddenLocations());
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
            System.err.println("Marked states: "+nbrOfStatesBDD(getMarkedStates()));
            reachableStatesBDD = getReachableStates();
//            reachableStatesBDD.printDot();
            coreachableStatesBDD = getCoreachableStates();

//            coreachableStatesBDD.printDot();

//            nonblockingStatesBDD = reachableStatesBDD.and(coreachableStatesBDD);
            nonblockingStatesBDD = coreachableStatesBDD;

            nbrOfNonblockingStates = nbrOfStatesBDD(nonblockingStatesBDD);
            nbrOfBlockingStates = nbrOfReachableStates - nbrOfNonblockingStates;            
        }

        return nonblockingStatesBDD;
    }

    public ArrayList<String> getComplementValues(String varName, ArrayList<String> vals)
    {
        ArrayList<String> output = new ArrayList<String>();
        for(int i= getExtendedAutomata().getMinValueofVar(varName);i<=getExtendedAutomata().getMaxValueofVar(varName);i++)
            if(!vals.contains(i+""))
                output.add(i+"");

        return output;
    }

    public double nbrOfStatesBDD(BDD bdd)
    {
        return bdd.satCount(sourceStateVariables);
    }

    public BDDExtendedAutomaton getBDDExAutomaton(String autName)
    {
        ExtendedAutomaton efa = theIndexMap.getExAutomatonWithName(autName);
        return (efa==null?null:automatonToBDDAutomatonMap.get(efa));
    }

    public List<EventDeclProxy> getInverseAlphabet(ExtendedAutomaton exAutomaton)
    {
        return orgExAutomata.getInverseAlphabet(exAutomaton);
    }

    public int getLocationIndex(ExtendedAutomaton theAutomaton, NodeProxy theLocation)
    {
        return theIndexMap.getLocationIndex(theAutomaton.getName(), theLocation.getName());
    }

    public int getEventIndex(EventDeclProxy theEvent)
    {
        return theIndexMap.getEventIndex(theEvent);
    }

    public int getEventIndex(String theEvent)
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

    public ExtendedAutomataIndexMap getIndexMap()
    {
        return theIndexMap;
    }

    public BDD[] getForwardTransAndNextValsForV(BDDExtendedAutomaton aut)
    {
        return forwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getForwardTransWhereVisUpdated(BDDExtendedAutomaton aut)
    {
        return forwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransAndNextValsForV(BDDExtendedAutomaton aut)
    {
        return backwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransWhereVisUpdated(BDDExtendedAutomaton aut)
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

    public void setBackwardOverflows(BDD overflows)
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
        return automatonToBDDAutomatonMap.get(theIndexMap.getExAutomatonAt(i));
    }

}