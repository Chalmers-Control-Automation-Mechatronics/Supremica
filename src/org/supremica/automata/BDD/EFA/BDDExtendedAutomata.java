package org.supremica.automata.BDD.EFA;

/**
 *
 * @author Sajed Miremadi, Zhennan Fei
 */
import gnu.trove.TIntArrayList;
import gnu.trove.TIntHashSet;
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
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
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
import org.supremica.automata.BDD.SupremicaBDDBitVector.TCSupremicaBDDBitVector;
import org.supremica.automata.FlowerEFABuilder;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.SynthesisAlgorithm;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;

public class BDDExtendedAutomata implements Iterable<BDDExtendedAutomaton> {

    private static Logger logger = LoggerFactory.createLogger(BDDAutomata.class);
    private final BDDExtendedManager manager;
    List<ExtendedAutomaton> theExAutomata;
    ExtendedAutomata orgExAutomata;
    List<BDDExtendedAutomaton> theBDDAutomataList = new LinkedList<BDDExtendedAutomaton>();
    Map<ExtendedAutomaton, BDDExtendedAutomaton> automatonToBDDAutomatonMap = new HashMap<ExtendedAutomaton, BDDExtendedAutomaton>();
    Map<Integer, String> bddVar2AutVarName = new HashMap<Integer, String>();
    public Map<String, HashSet<Integer>> enablingSigmaMap;
    Set<Integer> sourceLocationVars = new HashSet<Integer>();
    ExtendedAutomataIndexMap theIndexMap;
    List<EventDeclProxy> unionAlphabet;
    BDDEdges bddEdges = null;
    private BDDDomain eventDomain;
    @SuppressWarnings("unused")
    private BDDDomain constantDomain;
    private BDDVarSet sourceStateVarSet = null;
    private BDDVarSet destStateVarSet = null;
    //Related to locations
    private BDDDomain[] tempLocationDomains = null;
    private BDDDomain[] sourceLocationDomains = null;
    private BDDDomain[] destLocationDomains = null;
    private BDDVarSet sourceLocationVarSet = null;
    private BDDVarSet destLocationVarSet = null;
    /// For RAS models
    private BDDVarSet sourceStagesVarSet = null;
    private BDDVarSet sourceResourceVarSet = null;
    public BDD loadEventsBDD = null;
    //Related to all variables including regular variables and clocks
    BDDDomain[] tempVarDomains = null;
    BDDDomain[] sourceVarDomains = null;
    BDDDomain[] destVarDomains = null;
    BDDDomain[] tempClockDomains1 = null;
    BDDDomain[] tempClockDomains2 = null;
    private BDDVarSet sourceVariablesVarSet = null;
    private BDDVarSet destVariablesVarSet = null;
    private BDDVarSet sourceClockVarSet = null;
    private BDDVarSet destClockVarSet = null;
    BDDVarSet tempClock1Varset = null;
    private BDDPairing sourceToDestLocationPairing = null;
    private BDDPairing destToSourceLocationPairing = null;
    private BDDPairing sourceToDestVariablePairing = null;
    private BDDPairing destToSourceVariablePairing = null;
    private final BDDPairing destToSourceClockPairing = null;
    BDDPairing tempToSourceClockPairing = null;
    BDDPairing tempToDestClockPairing = null;
    BDDPairing sourceToTempLocationPairing = null;
    BDDPairing tempToDestLocationPairing = null;
    BDDPairing sourceToTempVariablePairing = null;
    BDDPairing tempToSourceVariablePairing = null;
    BDDPairing tempToDestVariablePairing = null;
    BDDPairing[] tempClocki2ToTempClocki1Pairing = null;
    //This pairing will be perfromed manually for each BDD variable
    BDDPairing tempClock1ToDestClockPairing = null;
//    BDDPairing sourceToTempClockPairing = null;
//    BDDPairing tempToDestClockPairing = null;
    BDD initialLocationsBDD = null;
    BDD markedLocationsBDD = null;
    BDD forbiddenLocationsBDD = null;
    BDD forbiddenValuesBDD = null;
    BDD forbiddenStatesBDD = null;
    BDD plantifiedBlockedLocationsBDD = null;
    BDD uncontrollableStatesBDD = null;
    private BDD reachableStatesBDD = null;
    private BDD coreachableStatesBDD = null;
    private BDD nonblockingStatesBDD = null;
    private BDD nonblockingControllableStatesBDD = null;
    private BDD controllableStatesBDD = null;
    BDD uncontrollableEventsBDD = null;
    BDD forcibleEventsBDD = null;
    BDD plantAlphabetBDD = null;
    long nbrOfReachableStates = -1;
    long nbrOfCoreachableStates = -1;
    long nbrOfNonblockingStates = -1;
    long nbrOfBlockingStates = -1;
    long nbrOfNonblockingControllableStates = -1;
    long nbrOfControllableStates = -1;
    long nbrOfUnsafeStates = -1; // for RAS models
    private final HashMap<Integer, SupremicaBDDBitVector> BDDBitVecSourceVarsMap;
    private final HashMap<Integer, SupremicaBDDBitVector> BDDBitVecTargetVarsMap;
    private final HashMap<Integer, SupremicaBDDBitVector> BDDBitVecTempVarsMap;
    Map<String, Integer> variableToMinSourceBDDVar;
    BDD initValuesBDD = null;
    BDD markedValuesBDD = null;
    BDD markedStatesBDD = null;
    BDD initClocksBDD = null;
    BDD locationInvariantsBDD = null;
    BDD[][] forwardTransWhereVisUpdated;
    BDD[] allForwardTransWhereVisUpdated;
    BDD[] allBackwardTransWhereVisUpdated;
    BDD[][] forwardTransWhereVHasAppearedInGuard;
    BDD[][] forwardTransAndNextValsForV;
    private BDD[] forwardClockExtensionBDD;
    private BDD forwardClocksExtensionBDD;
//    TIntObjectHashMap<TIntArrayList> reversedDestVarOrderings;
//    TIntObjectHashMap<TIntArrayList> reversedSourceVarOrderings;
//    TIntObjectHashMap<TIntArrayList> reversedTempVarOrderings;
    private BDD[] backwardClockExtensionBDD;
    private BDD backwardClocksExtensionBDD;
    BDD[][] backwardTransWhereVisUpdated;
    BDD[][] backwardTransAndNextValsForV;
    BDD[][] transWhereVisUpdatedWithoutDestClocks;
    BDD[][] transAndNextValsForVWithoutDestClocks;
    String locaVarSuffix;
    EditorSynthesizerOptions options;
    SynthesisAlgorithm synType;
    List<ExtendedAutomaton> plants;
    List<ExtendedAutomaton> specs;
    TIntArrayList plantUncontrollableEventIndexList;
    TIntArrayList specUncontrollableEventIndexList;
    TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>> event2AutomatonsEdges;
    //String pathRoot = "C:/Users/sajed/Desktop/MDD_files/";
    String pathRoot = "/Users/sajed/Dropbox/Documents/Papers/Supremica_Models/MDD_files";
    int BDDBitVectoryType = 0;
    int numberOfUsedBDDVariables = 0;
    List<Object> variableOrdering = null;
    List<String> variableOrderingNames;
    BDDExDisjDepSets depSets;
    
    private long optimalTime = -1;
    private BDD optimalTimeBDD = null;
    
    public Integer minValueOfVar = null;
    public BDD minValueOfVarBDD = null;    
    private String optVarName = null;

    public BDDExtendedAutomata(final ExtendedAutomata orgExAutomata, final EditorSynthesizerOptions options) {
        this.orgExAutomata = orgExAutomata;
        locaVarSuffix = orgExAutomata.getlocVarSuffix();
        theIndexMap = new ExtendedAutomataIndexMap(orgExAutomata);
        theExAutomata = new PCGExtendedAutomataSorter().sortAutomata(orgExAutomata.getExtendedAutomataList());

        BDDBitVectoryType = orgExAutomata.isNegativeValuesIncluded() ? 1 : 0;

//        theExAutomata = orgExAutomata.getExtendedAutomataList();

        BDDBitVecSourceVarsMap = new HashMap<Integer, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        BDDBitVecTargetVarsMap = new HashMap<Integer, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        BDDBitVecTempVarsMap = new HashMap<Integer, SupremicaBDDBitVector>(orgExAutomata.getVars().size());
        variableToMinSourceBDDVar = new HashMap<String, Integer>(orgExAutomata.getVars().size());

        variableOrderingNames = new ArrayList<String>();
        variableOrdering = new ArrayList<Object>();

        manager = new BDDExtendedManager();

        enablingSigmaMap = new HashMap<String, HashSet<Integer>>();

        initialLocationsBDD = manager.getOneBDD();
        markedLocationsBDD = manager.getOneBDD();        
        forbiddenLocationsBDD = manager.getZeroBDD();
        forbiddenValuesBDD = manager.getZeroBDD();
        forbiddenStatesBDD = manager.getZeroBDD();
        plantifiedBlockedLocationsBDD = manager.getZeroBDD();
        locationInvariantsBDD = manager.getOneBDD();

        uncontrollableStatesBDD = manager.getZeroBDD();

        uncontrollableEventsBDD = manager.getZeroBDD();
        forcibleEventsBDD = manager.getZeroBDD();
        plantAlphabetBDD = manager.getZeroBDD();

        loadEventsBDD = manager.getZeroBDD();

        this.options = options;
        this.synType = options.getSynthesisAlgorithm();

        this.plants = new ArrayList<ExtendedAutomaton>();
        this.specs = new ArrayList<ExtendedAutomaton>();

        this.plantUncontrollableEventIndexList = new TIntArrayList();
        this.specUncontrollableEventIndexList = new TIntArrayList();
        
        if(!options.getOptVaribale().isEmpty())
        {
            optVarName = options.getOptVaribale();
        }

        initialize();
    }

    @SuppressWarnings("unchecked")
    private void initialize() {
        unionAlphabet = orgExAutomata.getUnionAlphabet();

        if (!(synType.equals(SynthesisAlgorithm.MONOLITHICBDD) || synType.equals(SynthesisAlgorithm.MINIMALITY))) {
            event2AutomatonsEdges = new TIntObjectHashMap<HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>>(unionAlphabet.size());
        }

        sourceStateVarSet = manager.createEmptyVarSet();
        destStateVarSet = manager.createEmptyVarSet();

        sourceLocationVarSet = manager.createEmptyVarSet();
        destLocationVarSet = manager.createEmptyVarSet();

        sourceClockVarSet = manager.createEmptyVarSet();
        destClockVarSet = manager.createEmptyVarSet();
        tempClock1Varset = manager.createEmptyVarSet();

        sourceVariablesVarSet = manager.createEmptyVarSet();
        destVariablesVarSet = manager.createEmptyVarSet();

        sourceStagesVarSet = manager.createEmptyVarSet();
        sourceResourceVarSet = manager.createEmptyVarSet();

        sourceLocationDomains = new BDDDomain[orgExAutomata.size()];
        destLocationDomains = new BDDDomain[orgExAutomata.size()];
        tempLocationDomains = new BDDDomain[orgExAutomata.size()];

        tempVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        sourceVarDomains = new BDDDomain[orgExAutomata.getVars().size()];
        destVarDomains = new BDDDomain[orgExAutomata.getVars().size()];

        tempClockDomains1 = new BDDDomain[orgExAutomata.getVars().size()];
        tempClockDomains2 = new BDDDomain[orgExAutomata.getVars().size()];
        tempClocki2ToTempClocki1Pairing = new BDDPairing[orgExAutomata.getVars().size()];

        forwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        forwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        forwardTransWhereVHasAppearedInGuard = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        allForwardTransWhereVisUpdated = new BDD[orgExAutomata.getVars().size()];
        allBackwardTransWhereVisUpdated = new BDD[orgExAutomata.getVars().size()];

        forwardClockExtensionBDD = new BDD[orgExAutomata.getVars().size()];
        forwardClocksExtensionBDD = manager.getOneBDD();

        backwardClockExtensionBDD = new BDD[orgExAutomata.getVars().size()];
        backwardClocksExtensionBDD = manager.getOneBDD();
//        reversedDestVarOrderings = new TIntObjectHashMap<TIntArrayList>(orgExAutomata.getVars().size());
//        reversedSourceVarOrderings = new TIntObjectHashMap<TIntArrayList>(orgExAutomata.getVars().size());
//        reversedTempVarOrderings = new TIntObjectHashMap<TIntArrayList>(orgExAutomata.getVars().size());


        backwardTransAndNextValsForV = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        backwardTransWhereVisUpdated = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

        transWhereVisUpdatedWithoutDestClocks = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];
        transAndNextValsForVWithoutDestClocks = new BDD[orgExAutomata.size()][orgExAutomata.getVars().size()];

        /*
        if(orgExAutomata.getDomain() > 0 )
        {
        constantDomain = manager.createDomain(orgExAutomata.getDomain());
        //        System.out.println("constant variables: "+constantDomain.set().toString());
        }
         */

        for (int i = 0; i < orgExAutomata.size(); i++) {
            for (int j = 0; j < orgExAutomata.getVars().size(); j++) {
                forwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                forwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();
                forwardTransWhereVHasAppearedInGuard[i][j] = manager.getZeroBDD();
                forwardClockExtensionBDD[j] = manager.getZeroBDD();
                backwardClockExtensionBDD[j] = manager.getZeroBDD();

                backwardTransAndNextValsForV[i][j] = manager.getZeroBDD();
                backwardTransWhereVisUpdated[i][j] = manager.getZeroBDD();

                transWhereVisUpdatedWithoutDestClocks[i][j] = manager.getZeroBDD();
                transAndNextValsForVWithoutDestClocks[i][j] = manager.getZeroBDD();

                allForwardTransWhereVisUpdated[j] = manager.getZeroBDD();
                allBackwardTransWhereVisUpdated[j] = manager.getZeroBDD();

            }
        }

//        System.out.println("domain: "+orgExAutomata.getDomain());

        setVariableOrdering();

        //Manually set the variable ordering. In this case, tha bove setVariableOrdering() should be commented out.
//        manuallySetVariableOrderingNames();
//        setVariableOrdering(variableOrderingNames);

        for (final Object obj : variableOrdering) {
            if (obj instanceof List<?> && !((List<?>) obj).isEmpty()) {
                eventDomain = manager.createDomain(((List<EventDeclProxy>) obj).size());
                eventDomain.setName("Events");
                numberOfUsedBDDVariables += eventDomain.varNum();

                for (final EventDeclProxy event : ((List<EventDeclProxy>) obj)) {
                    final int currEventIndex = getEventIndex(event);
                    if (!(synType.equals(SynthesisAlgorithm.MONOLITHICBDD) || synType.equals(SynthesisAlgorithm.MINIMALITY))) {
                        event2AutomatonsEdges.put(currEventIndex, new HashMap<ExtendedAutomaton, ArrayList<EdgeProxy>>());
                    }
                    final BDD eventBDD = manager.createBDD(currEventIndex, eventDomain);
                    if (event.getName().contains(FlowerEFABuilder.LOAD_EVENT_PREFIX)) {
                        loadEventsBDD = loadEventsBDD.or(eventBDD);
                    }

                    if (orgExAutomata.getPlantAlphabet().contains(event)) {
                        plantAlphabetBDD = plantAlphabetBDD.or(eventBDD);
                    }

                    if (event.getKind() != EventKind.CONTROLLABLE) {
                        uncontrollableEventsBDD = uncontrollableEventsBDD.or(eventBDD);
                    }

                    if (orgExAutomata.isEventForcible(event)) {
                        forcibleEventsBDD = forcibleEventsBDD.or(eventBDD);
                    }
                }
            }

            if (obj instanceof ExtendedAutomaton) {
                final String automatonName = ((ExtendedAutomaton) obj).getName();

                System.err.println(automatonName);
                final int autIndex = theIndexMap.getExAutomatonIndex(automatonName);
                final int nbrOfStates = ((ExtendedAutomaton) obj).nbrOfNodes();
                final BDDDomain tempLocationDomain = manager.createDomain(nbrOfStates);

                final BDDDomain sourceLocationDomain = manager.createDomain(nbrOfStates);
                numberOfUsedBDDVariables += sourceLocationDomain.varNum();

                final int[] sourceVars = sourceLocationDomain.vars();
                for (int i = 0; i < sourceVars.length; i++) {
                    sourceLocationVars.add(sourceVars[i]);
                    bddVar2AutVarName.put(sourceVars[i], automatonName);
                }

                System.err.println("sourceLocation variables: " + sourceLocationDomain.set().toString());
                final BDDDomain destLocationDomain = manager.createDomain(nbrOfStates);
                numberOfUsedBDDVariables += destLocationDomain.varNum();

                System.err.println("destLocation variables: " + destLocationDomain.set().toString());

                sourceLocationVarSet.unionWith(sourceLocationDomain.set());
                destLocationVarSet.unionWith(destLocationDomain.set());
                sourceLocationDomains[autIndex] = sourceLocationDomain;
                sourceLocationDomains[autIndex].setName(automatonName);
                destLocationDomains[autIndex] = destLocationDomain;
                destLocationDomains[autIndex].setName(automatonName);

                tempLocationDomains[autIndex] = tempLocationDomain;
            }



            if (obj instanceof VariableComponentProxy) {
                initializeVariable(((VariableComponentProxy) obj));
            }

        }

        destToSourceLocationPairing = manager.makePairing(destLocationDomains, sourceLocationDomains);
        sourceToDestLocationPairing = manager.makePairing(sourceLocationDomains, destLocationDomains);

        manager.setVariableStringToIndexMap(theIndexMap.variableStringToIndexMap);

//        sourceToTempClockPairing = manager.makePairing(sourceClockDomains, tempClockDomains);
//        tempToDestClockPairing = manager.makePairing(tempClockDomains, destClockDomains);

        sourceToTempVariablePairing = manager.makePairing(sourceVarDomains, tempVarDomains);
        tempToSourceVariablePairing = manager.makePairing(tempVarDomains, sourceVarDomains);
        tempToDestVariablePairing = manager.makePairing(tempVarDomains, destVarDomains);

        sourceToDestVariablePairing = manager.makePairing(sourceVarDomains, destVarDomains);
        destToSourceVariablePairing = manager.makePairing(destVarDomains, sourceVarDomains);

        if (!orgExAutomata.getClocks().isEmpty()) {
            tempClock1ToDestClockPairing = manager.getFactory().makePair();
        }
        for (final VariableComponentProxy clockComponent : orgExAutomata.getClocks()) {
            final int clockIndex = theIndexMap.getVariableIndex(clockComponent);
            final BDDDomain bddDomainTemp = tempClockDomains1[clockIndex];
            final BDDDomain bddDomainDest = destVarDomains[clockIndex];

            for (int i = 0; i < bddDomainDest.varNum(); i++) {
                tempClock1ToDestClockPairing.set((bddDomainTemp.vars())[bddDomainTemp.varNum() - 1 - i], (bddDomainDest.vars())[bddDomainDest.varNum() - 1 - i]);
            }
        }

        for (final EventDeclProxy event : unionAlphabet) {
            System.err.println(event.getName() + " index: " + theIndexMap.getEventIndex(event));
        }


        initValuesBDD = manager.getOneBDD();
        initClocksBDD = manager.getOneBDD();

        manager.setBDDExAutomata(this);

        computeInitVariableValues();
        computeInitClockValues();

        for (final ExtendedAutomaton automaton : theExAutomata) {
            final BDDExtendedAutomaton bddExAutomaton = new BDDExtendedAutomaton(this, automaton);

            if (automaton.isSpecification()) {
                specs.add(automaton);
            } else {
                plants.add(automaton);
            }

            bddExAutomaton.initialize();

            add(bddExAutomaton);
            locationInvariantsBDD = locationInvariantsBDD.and(bddExAutomaton.getLocationInvariants());
        }

//        BDD bdd1 = manager.getFactory().buildCube(0, sourceVarDomains[0].vars()).and(manager.getFactory().buildCube(1, sourceVarDomains[1].vars()));
//        BDD bdd2 = manager.getFactory().buildCube(1, sourceVarDomains[0].vars()).and(manager.getFactory().buildCube(2, sourceVarDomains[1].vars()));
//        BDD bdd3 = manager.getFactory().buildCube(2, sourceVarDomains[0].vars()).and(manager.getFactory().buildCube(3, sourceVarDomains[1].vars()));
//        BDD bdd4 = manager.getFactory().buildCube(3, sourceVarDomains[0].vars()).and(manager.getFactory().buildCube(4, sourceVarDomains[1].vars()));
//
//        BDD bddTest = bdd1.or(bdd2).or(bdd3).or(bdd4);
//
//        BDD bdd5 = manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(), sourceVarDomains[1]).gth(
//                manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(), sourceVarDomains[1].varNum(),2));
//        BDD bdd6 = bddTest.and(bdd5).and(manager.getFactory().buildCube(2, tempVarDomains[1].vars()));
//        BDD bdd7 = bdd6.exist(sourceVarDomains[1].set()).replace(manager.makePairing(tempVarDomains[1], sourceVarDomains[1]));
//        bddTest = bddTest.and(bdd5.not()).or(bdd7);
//        bddTest.printDot();

        sourceStateVarSet = sourceStateVarSet.union(sourceLocationVarSet);
        sourceStateVarSet = sourceStateVarSet.union(sourceVariablesVarSet);

        destStateVarSet = destStateVarSet.union(destLocationVarSet);
        destStateVarSet = destStateVarSet.union(destVariablesVarSet);

        sourceToTempLocationPairing = manager.makePairing(sourceLocationDomains, tempLocationDomains);
        tempToDestLocationPairing = manager.makePairing(tempLocationDomains, destLocationDomains);

//        for(VariableComponentProxy var:orgExAutomata.getVars())
//        {
//            int varIndex = theIndexMap.getVariableIndex(var);
//            //allBackwardTransWhereVisUpdated[varIndex] = sourceTooTdest(allForwardTransWhereVisUpdated[varIndex]);
//            allBackwardTransWhereVisUpdated[varIndex] = allForwardTransWhereVisUpdated[varIndex].replace(sourceToTempLocationPairing);
//            allBackwardTransWhereVisUpdated[varIndex] = allBackwardTransWhereVisUpdated[varIndex].replace(destToSourceLocationPairing);
//            allBackwardTransWhereVisUpdated[varIndex] = allBackwardTransWhereVisUpdated[varIndex].replace(tempToDestLocationPairing);
//        }

        if (options.getSynthesisAlgorithm().equals(SynthesisAlgorithm.MONOLITHICBDD)
                || options.getSynthesisAlgorithm().equals(SynthesisAlgorithm.MINIMALITY)) {
            computeBDDEdges();
        }

        computeMarkedValues();

//                try
//                {
//                    final ExpressionParser parser = new ExpressionParser(ModuleSubjectFactory.getInstance(), CompilerOperatorTable.getInstance());
//                    manager.guard2BDD((SimpleExpressionSubject)(parser.parse("c1-c2==2",Operator.TYPE_BOOLEAN))).printDot();
//                }
//                catch(final ParseException pe)
//                {
//                    System.err.println(pe);
//                }
    }

    //Set variable ordering based on PCG sorters
    void setVariableOrdering() {
        variableOrdering.add(unionAlphabet);
        variableOrderingNames.add("Events");

        final List<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(new PCGVariableSorter(orgExAutomata).sortVars(orgExAutomata.getVars()));
//        ArrayList<VariableComponentProxy> sortedVarList = new ArrayList<VariableComponentProxy>(orgExAutomata.getVars());

        for (final ExtendedAutomaton automaton : theExAutomata) {

            variableOrdering.add(automaton);
            variableOrderingNames.add(automaton.getName());

            //Place the variables that are related to this automaton (the variables that are updated in this automaton)
            for (final VariableComponentProxy varRelatedToAutomaton : automaton.getUsedTargetVariables()) {
                if (sortedVarList.contains(varRelatedToAutomaton)) {
                    variableOrdering.add(varRelatedToAutomaton);
                    variableOrderingNames.add(varRelatedToAutomaton.getName());
                }
            }

            sortedVarList.removeAll(automaton.getUsedTargetVariables());

        }

        for (final VariableComponentProxy var : sortedVarList) {
            variableOrdering.add(var);
            //if(!var.getName().equals(orgExAutomata.getGlobalClockName()))
            variableOrderingNames.add(var.getName());
        }

        variableOrderingNames.add("1");

    }

    void setVariableOrdering(final List<String> varOrderingNames) {
        for (final String varName : varOrderingNames) {
            if (!varName.equals("1")) {
                if (varName.equals("Events")) {
                    variableOrdering.add(unionAlphabet);
                } else {

                    final int isAutomaton = theIndexMap.isStringEFAorVar(varName);
                    if (isAutomaton == 0) {
                        variableOrdering.add(theIndexMap.getExAutomatonWithName(varName));
                    } else if (isAutomaton == 1) {
                        variableOrdering.add(theIndexMap.getVariableAt(theIndexMap.getVariableIndexByName(varName)));
                    }
                }
            }
        }
    }

    void manuallySetVariableOrderingNames() {
        variableOrderingNames = new ArrayList<String>();
        variableOrderingNames.add("Events");
        variableOrderingNames.add("Machine");
        variableOrderingNames.add("MachLoc");       
        variableOrderingNames.add("Scheduler");
        variableOrderingNames.add("time");                 
        variableOrderingNames.add("V");                
        variableOrderingNames.add("V_acc");
        variableOrderingNames.add("clock_t");        
        variableOrderingNames.add("i");
        variableOrderingNames.add("Pump");
        variableOrderingNames.add("clock_w");                
        variableOrderingNames.add("PumpLoc");        
        variableOrderingNames.add("clock_z");        
        variableOrderingNames.add("clock_y");        
        variableOrderingNames.add("1");
//        variableOrderingNames.add("Events");
//        variableOrderingNames.add("Clock");
//        variableOrderingNames.add("time");
//        variableOrderingNames.add("m0");
//        variableOrderingNames.add("m1");
//        variableOrderingNames.add("m2");
//        variableOrderingNames.add("m3");
//        variableOrderingNames.add("m4");
//        variableOrderingNames.add("m5");
//        variableOrderingNames.add("P2");
//        variableOrderingNames.add("c2");
//        variableOrderingNames.add("P5");
//        variableOrderingNames.add("c5");
//        variableOrderingNames.add("P6");
//        variableOrderingNames.add("c6");
//        variableOrderingNames.add("P4");
//        variableOrderingNames.add("c4");
//        variableOrderingNames.add("P3");
//        variableOrderingNames.add("c3");
//        variableOrderingNames.add("P1");
//        variableOrderingNames.add("c1");
//        variableOrderingNames.add("1");
    }

    public void setPathRoot(final String pr) {
        pathRoot = pr;
    }

    public void initializeVariable(final VariableComponentProxy var) {
        final String varName = var.getName();
        final int varIndex = theIndexMap.getVariableIndex(var);

        System.err.println("variable name: " + varName);
//        int domain = orgExAutomata.getDomain();
        final int domain = orgExAutomata.getVarDomain(varName);

        if (orgExAutomata.getClocks().contains(var)) {
            tempClockDomains1[varIndex] = manager.createDomain(orgExAutomata.getLargestClockDomain() * 2);
            System.err.println("tempClock1 variables: " + tempClockDomains1[varIndex].set().toString());
            tempClockDomains2[varIndex] = manager.createDomain(orgExAutomata.getLargestClockDomain() * 2);
            System.err.println("tempClock2 variables: " + tempClockDomains2[varIndex].set().toString());
        }

        final BDDDomain tempDomain = manager.createDomain(domain);
        BDDBitVecTempVarsMap.put(theIndexMap.getVariableIndex(var),
                manager.createSupremicaBDDBitVector(BDDBitVectoryType,
                orgExAutomata.getMinValueofVar(varName) < 0,
                tempDomain));
//        int[] reversedTempVarOrdering = manager.partialReverseVarOrdering(tempDomain.vars());
//        reversedTempVarOrderings.put(varIndex, new TIntArrayList(reversedTempVarOrdering));
        tempVarDomains[varIndex] = tempDomain;
        System.err.println("tempVar variables: " + tempDomain.set().toString());

        final BDDDomain sourceDomain = manager.createDomain(domain);
        //manager.getFactory().buildCube(1, sourceDomain.vars()).printDot();
        variableToMinSourceBDDVar.put(varName, sourceDomain.vars()[0]);
//        int[] reversedSourceVarOrdering = manager.partialReverseVarOrdering(sourceDomain.vars());
//        reversedSourceVarOrderings.put(varIndex, new TIntArrayList(reversedSourceVarOrdering));
        System.err.println("sourceVar variables: " + sourceDomain.set().toString());

        numberOfUsedBDDVariables += sourceDomain.varNum();

        final int[] sourceVars = sourceDomain.vars();
        for (int i = 0; i < sourceVars.length; i++) {
            final int[] sourceVar = new int[1];
            sourceVar[0] = sourceVars[i];
            bddVar2AutVarName.put(sourceVars[i], varName);
        }

//        System.err.println("sourceVar variables: "+sourceVarDomain.set().toString());
        BDDBitVecSourceVarsMap.put(theIndexMap.getVariableIndex(var),
                manager.createSupremicaBDDBitVector(BDDBitVectoryType,
                orgExAutomata.getMinValueofVar(varName) < 0,
                sourceDomain));
        if (orgExAutomata.getClocks().contains(var)) {
            sourceClockVarSet.unionWith(sourceDomain.set());

            tempClocki2ToTempClocki1Pairing[varIndex] = manager.makePairing(tempClockDomains2[varIndex], tempClockDomains1[varIndex]);
            tempClock1Varset = tempClock1Varset.union(tempClockDomains1[varIndex].set());
        }

        sourceVariablesVarSet.unionWith(sourceDomain.set());

        final BDDDomain destDomain = manager.createDomain(domain);
//        int[] reversedDestVarOrdering = manager.partialReverseVarOrdering(destDomain.vars());
//        reversedDestVarOrderings.put(varIndex, new TIntArrayList(reversedDestVarOrdering));

        if (orgExAutomata.getStageVars().contains(var)) {
            sourceStagesVarSet.unionWith(sourceDomain.set());
        } else {
            sourceResourceVarSet.unionWith(sourceDomain.set());
        }

        numberOfUsedBDDVariables += destDomain.varNum();
        System.err.println("destVar variables: " + destDomain.set().toString());
        BDDBitVecTargetVarsMap.put(theIndexMap.getVariableIndex(var),
                manager.createSupremicaBDDBitVector(BDDBitVectoryType,
                orgExAutomata.getMinValueofVar(varName) < 0,
                destDomain));

        //Create the BDD that will be used to extend the clocks
//        if(orgExAutomata.getClocks().contains(var))
//        {
//            final BDDDomain tempClockDomain = manager.createDomain(domain);
//            System.err.println("tempClock variables: "+tempClockDomain.set().toString());
////
//            tempClockDomains[theIndexMap.getClockIndex(var)] = tempClockDomain;
//            tempClockDomains[theIndexMap.getClockIndex(var)].setName(var.getName());
//
//            SupremicaBDDBitVector clockBDDBitVector =  manager.createSupremicaBDDBitVector(BDDBitVectoryType, manager.getFactory(),tempClockDomain);
//
//            for(int i = 0; i < domain ; i++)
//            {
//                //BDD iSourceBDD = manager.getFactory().buildCube(i, reversedSourceVarOrdering);
//                BDD iTargetBDD = manager.getFactory().buildCube(i, reversedDestVarOrdering);
//
//                BDD largerThanIBDD = clockBDDBitVector.gte(manager.createSupremicaBDDBitVector(
//                        BDDBitVectoryType, manager.getFactory(), tempClockDomain.size().intValue(), i));
//
//                BDD lessThanIBDD = clockBDDBitVector.lte(manager.createSupremicaBDDBitVector(
//                        BDDBitVectoryType, manager.getFactory(), sourceDomain.size().intValue(), i));
//                BDD forwardClockExtension = iTargetBDD.and(largerThanIBDD);
//                BDD backwardClockExtension = iTargetBDD.and(lessThanIBDD);
//                forwardClockExtensionBDD[varIndex] = forwardClockExtensionBDD[varIndex].or(forwardClockExtension);
//                backwardClockExtensionBDD[varIndex] = backwardClockExtensionBDD[varIndex].or(backwardClockExtension);
//            }
//
//            forwardClocksExtensionBDD = forwardClocksExtensionBDD.and(forwardClockExtensionBDD[varIndex]);
//            backwardClocksExtensionBDD = backwardClocksExtensionBDD.and(backwardClockExtensionBDD[varIndex]);
//        }

        if (orgExAutomata.getClocks().contains(var)) {
            destClockVarSet.unionWith(destDomain.set());
        }

        destVariablesVarSet.unionWith(destDomain.set());

        sourceVarDomains[varIndex] = sourceDomain;
        sourceVarDomains[varIndex].setName(var.getName());
        destVarDomains[varIndex] = destDomain;
        destVarDomains[varIndex].setName(var.getName());
    }

    public BDD sourceTooTdest(final BDD bdd) {
        final BDD newBDD = bdd.id();

        newBDD.replaceWith(sourceToTempLocationPairing);
        newBDD.replaceWith(destToSourceLocationPairing);
        newBDD.replaceWith(tempToDestLocationPairing);
        newBDD.replaceWith(sourceToTempVariablePairing);
        newBDD.replaceWith(destToSourceVariablePairing);
        newBDD.replaceWith(tempToDestVariablePairing);

        return newBDD;
    }

    public BDD getForwardClocksExtension() {
        return forwardClocksExtensionBDD;
    }

    public BDD getBackwardClocksExtension() {
        return backwardClocksExtensionBDD;
    }

    public int getMinSourceBDDVar(final String varName) {
        return variableToMinSourceBDDVar.get(varName);
    }

    public int getNumberOfUsedBDDVariables() {
        return numberOfUsedBDDVariables;
    }

    public boolean isSourceLocationVar(final int var) {
        if (sourceLocationVars.contains(var)) {
            return true;
        } else {
            return false;
        }
    }

    public BDD getLocationInvariants() {
        return locationInvariantsBDD;
    }

    public String getLocVarSuffix() {
        return locaVarSuffix;
    }

    public String getAutVarName(final int var) {
        return bddVar2AutVarName.get(var);
    }

    public BDDDomain getTempLocationDomain(final String automaton) {
        if (theIndexMap.getExAutomatonIndex(automaton) != null) {
            return tempLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
        } else {
            return null;
        }
    }

    public BDDDomain getSourceLocationDomain(final String automaton) {
        if (theIndexMap.getExAutomatonIndex(automaton) != null) {
            return sourceLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
        } else {
            return null;
        }
    }

    public BDDDomain getDestLocationDomain(final String automaton) {
        if (theIndexMap.getExAutomatonIndex(automaton) != null) {
            return destLocationDomains[theIndexMap.getExAutomatonIndex(automaton)];
        } else {
            return null;
        }
    }

    public BDDDomain getTempVariableDomain(final int varIndex) {
        if (varIndex <= tempVarDomains.length) {
            return tempVarDomains[varIndex];
        } else {
            return null;
        }
    }

    public BDDDomain getSourceVariableDomain(final int varIndex) {
        if (varIndex <= sourceVarDomains.length) {
            return sourceVarDomains[varIndex];
        } else {
            return null;
        }
    }

    public BDDDomain getDestVariableDomain(final int varIndex) {
        if (varIndex <= destVarDomains.length) {
            return destVarDomains[varIndex];
        } else {
            return null;
        }
    }

    public BDDVarSet getSourceResourceVarSet() {
        return sourceResourceVarSet;
    }

    public BDDVarSet getSourceStagesVarSet() {
        return sourceStagesVarSet;
    }

    public BDDVarSet getSourceClockVarSet() {
        return sourceClockVarSet;
    }

    public BDDVarSet getDestClockVarSet() {
        return destClockVarSet;
    }

    public BDDVarSet getSourceVariablesVarSet() {
        return sourceVariablesVarSet;
    }

    public BDDVarSet getDestVariablesVarSet() {
        return destVariablesVarSet;
    }

    public BDDVarSet getSourceStatesVarSet() {
        return sourceStateVarSet;
    }

    public BDDVarSet getDestStatesVarSet() {
        return destStateVarSet;
    }

    public BDDVarSet MinusVarSet(final BDDVarSet b1, final BDDVarSet b2) {
        final TIntHashSet varSet1 = new TIntHashSet(b1.toArray());
        varSet1.removeAll(b2.toArray());
        return manager.getFactory().makeSet(varSet1.toArray());
    }

    public SupremicaBDDBitVector getBDDBitVecSource(final int index) {
        return BDDBitVecSourceVarsMap.get(index);
    }

    public SupremicaBDDBitVector getBDDBitVecTarget(final int index) {
        return BDDBitVecTargetVarsMap.get(index);
    }

    public SupremicaBDDBitVector getBDDBitVecTemp(final int index) {
        return BDDBitVecTempVarsMap.get(index);
    }

    public ExtendedAutomata getExtendedAutomata() {
        return orgExAutomata;
    }

    public BDD getForbiddenLocations() {
        return forbiddenLocationsBDD;
    }

//    public BDD getForbiddenValues()
//    {
//        return manager.createBDD(theIndexMap.getIndexOfVal("4"), getSourceVariableDomain(theIndexMap.getVariableIndexByName("clock_c1"))).or(
//                manager.createBDD(theIndexMap.getIndexOfVal("4"), getSourceVariableDomain(theIndexMap.getVariableIndexByName("clock_c2"))));
//    }
    public BDD getPlantifiedBlockedLocations() {
        return plantifiedBlockedLocationsBDD;
    }

    public void addForbiddenLocations(final BDD forbiddenLocations) {
        forbiddenLocationsBDD = forbiddenLocationsBDD.or(forbiddenLocations);
    }

    public void addPlantifiedBlockedLocations(final BDD forbiddenUnconLocations) {
        plantifiedBlockedLocationsBDD = plantifiedBlockedLocationsBDD.or(forbiddenUnconLocations);
    }

    public double numberOfReachableStates() {
        if (nbrOfReachableStates < 0) {
            computeReachableStates();
        }
        return nbrOfReachableStates;
    }

    public double numberOfControllableStates(final boolean reachable) {
        if (nbrOfControllableStates < 0) {
            getControllableStates(reachable);
        }
        return nbrOfControllableStates;
    }

    public double numberOfCoreachableStates() {
        if (nbrOfCoreachableStates < 0) {
            computeCoreachableStates();
        }
        return nbrOfCoreachableStates;
    }

    public double numberOfBlockingStates() {
        if (nbrOfBlockingStates < 0) {
            getNonblockingStates();
        }
        return nbrOfBlockingStates;
    }

    public double numberOfNonblockingStates() {
        if (nbrOfNonblockingStates < 0) {
            getNonblockingStates();
        }
        return nbrOfNonblockingStates;
    }

    public double numberOfNonblockingControllableStates(final boolean reachable) {
        if (nbrOfNonblockingControllableStates < 0) {
            getNonblockingControllableStates(reachable);
        }
        return nbrOfNonblockingControllableStates;
    }

    public boolean isNonblocking() {
        final BDD impBDD = getReachableStates().imp(getCoreachableStates());
        return impBDD.isOne();
    }

    public void done() {
        if (manager != null) {
            manager.done();
        }
    }

    public BDD getInitialState() {
        final BDD initLocationInvariants = locationInvariantsBDD.and(initialLocationsBDD);
        return initLocationInvariants.and(initValuesBDD).and(initClocksBDD);
    }
        
    public BDD getMarkedStates() {
   
        if(markedStatesBDD == null)
        {
            markedStatesBDD = markedLocationsBDD.and(getMarkedValuations());

            if (options.getOptimization()) {
                markedStatesBDD = markedStatesBDD.and(getOptimalTimeBDD());
            }

            if (optVarName != null && !optVarName.isEmpty()) {
                markedStatesBDD = markedStatesBDD.and(getMinValueOfBDD());
            }
        }
        
        return markedStatesBDD;
    }

    public BDD getMarkedValuations() {
        if(markedValuesBDD == null)
            computeMarkedValues();
        
        return markedValuesBDD;
    }

    void computeInitVariableValues() {
        initValuesBDD = manager.getOneBDD();

        for (final VariableComponentProxy var : orgExAutomata.getVars()) {
            if (!orgExAutomata.getClocks().contains(var) && !orgExAutomata.getParameters().contains(var)) {
                final int initValue = theIndexMap.getInitValueofVar(var.getName());
                BDD initBDD = manager.getOneBDD();
                if (initValue >= 0) {
                    initBDD = getConstantBDD(var.getName(), initValue);
                } else {
                    final int varIndex = theIndexMap.getVariableIndex(var);
                    final TCSupremicaBDDBitVector tcs = ((TCSupremicaBDDBitVector) manager.createSupremicaBDDBitVector(1,
                            getSourceVariableDomain(varIndex).varNum(),
                            initValue));
                    initBDD = getBDDBitVecSource(varIndex).equ(tcs);
                }
                initValuesBDD = initValuesBDD.and(initBDD);
            }
        }

    }

    void computeInitClockValues() {
        int clockIndex = -1;
        if (orgExAutomata.getClocks().size() > 0) {
            clockIndex = theIndexMap.getVariableIndex(orgExAutomata.getClocks().get(0));
        }
        // Create initial values of clocks with the same rate
        for (int i = 1; i < orgExAutomata.getClocks().size(); i++) {
            final int newClockIndex = theIndexMap.getVariableIndex(orgExAutomata.getClocks().get(i));

            initClocksBDD = initClocksBDD.and(getBDDBitVecSource(clockIndex).equ(getBDDBitVecSource(newClockIndex)));

            clockIndex = newClockIndex;
        }
    }

    void computeMarkedValues() {
        markedValuesBDD = manager.getOneBDD();
        for (final VariableComponentProxy var : orgExAutomata.getVars()) {
            BDD markedVals = manager.getOneBDD();
            if (theIndexMap.getMarkedPredicatesofVar(var.getName()).size() > 0) {
                markedVals = manager.getZeroBDD();
                for (final VariableMarkingProxy vmp : theIndexMap.getMarkedPredicatesofVar(var.getName())) {
                    markedVals = markedVals.or(manager.guard2BDD(vmp.getPredicate()));
                }
            }

            markedValuesBDD.andWith(markedVals);
        }
    }

    public SupremicaBDDBitVector getMaxBDDBitVecOf(final int varIndex) {
        return manager.createSupremicaBDDBitVector(BDDBitVectoryType,
                getBDDBitVecSource(varIndex).length(), orgExAutomata.getMaxValueofVar(theIndexMap.getVariableAt(varIndex).getName()));
    }

    public SupremicaBDDBitVector getMinBDDBitVecOf(final int varIndex) {
        return manager.createSupremicaBDDBitVector(BDDBitVectoryType,
                getBDDBitVecSource(varIndex).length(), orgExAutomata.getMinValueofVar(theIndexMap.getVariableAt(varIndex).getName()));
    }

    public BDD getConstantBDD(final String varName, final int cons) {
        final SupremicaBDDBitVector c = manager.createSupremicaBDDBitVector(
                BDDBitVectoryType, getBDDBitVecSource(theIndexMap.getVariableIndexByName(varName)).length(), cons);

        final BDD result = manager.getOneBDD();
        for (int i = 0; i < c.length(); i++) {
            result.andWith(getBDDBitVecSource(theIndexMap.getVariableIndexByName(varName)).getBit(i).biimp(c.getBit(i)));
        }

        return result;
    }

    public void computeOptimalTimeBDD() {
        if (optimalTimeBDD == null) {
            final BDD bdd = markedStatesBDD.and(getReachableStates());
            final TIntArrayList valuations = BDD2valuations(bdd, orgExAutomata.getGlobalClockName());
            if (valuations.isEmpty()) {
                logger.error("Your guess for the time domain is too small to compute the minimum time. Please try a larger time domain.");
                optimalTimeBDD = manager.getOneBDD();
            } else {
                optimalTime = valuations.get(0);
                optimalTimeBDD = bdd.and(
                        manager.getFactory().buildCube((int) optimalTime, getSourceVariableDomain(
                        theIndexMap.getVariableIndexByName(orgExAutomata.getGlobalClockName())).vars()));
            }
        }
    }
    
    public BDD getOptimalTimeBDD() {
        computeOptimalTimeBDD();
        return optimalTimeBDD;
    }
    
    public void computeMinValueOfVar()
    {
        if(minValueOfVarBDD == null)
        {
            final BDD bdd = markedStatesBDD.and(getReachableStates());
            final TIntArrayList valuations = BDD2valuations(bdd, optVarName);
            minValueOfVar = valuations.get((options.getTypeOfVarOpt()?0:valuations.size()-1));
                minValueOfVarBDD = bdd.and(
                        manager.getFactory().buildCube((int) minValueOfVar, getSourceVariableDomain(
                        theIndexMap.getVariableIndexByName(optVarName)).vars()));                        
        }
    }    
    
    public BDD getMinValueOfBDD() {
        computeMinValueOfVar();        
        return minValueOfVarBDD;
    }    

    private void computeReachableStates() {
        if (reachableStatesBDD == null) {
            System.err.println("Computing reachable states...");
            if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD)) {
                //reachableStatesBDD = BDDExDisjunctiveReachabilityAlgorithms.restrictedForwardWorkSetAlgorithm(this, getInitialState(), manager.getZeroBDD());
                //reachableStatesBDD = BDDExDisjunctiveHeuristicReachabilityAlgorithms.forwardWorkSetAlgorithm(this, getInitialState(), manager.getZeroBDD());
                getDepSets();
                reachableStatesBDD = depSets.forwardWorkSetAlgorithm(getInitialState());
            } else {
                reachableStatesBDD = manager.restrictedForward(getInitialState(), forbiddenStatesBDD);
            }

            //System.err.println("Reachable states computed!" + ": " + reachableStatesBDD.satCount(sourceStateVarSet));

            //nbrOfReachableStates = nbrOfStatesBDD(reachableStatesBDD);
            
//            reachableStatesBDD.printDot();
//            System.err.println("sat number of reachable states: "+reachableStatesBDD.satCount(sourceStateVarSet));
            
//            reachableStatesBDD.printDot();
            
            final IDD idd = generateIDD(reachableStatesBDD, reachableStatesBDD);
            nbrOfReachableStates = nbrOfStatesIDD(idd).longValue();
            
//            ((BDDMonolithicEdges) getBDDEdges()).makeTheEdgesReachable();
            
            System.err.println(nbrOfReachableStates + " reachable states found.");

//            nbrOfReachableStates = -1;


//            logger.info("Number of reachable states in the closed-loop system: "+nbrOfReachableStates);
        }
    }

    private void computeCoreachableStates() {
        if (coreachableStatesBDD == null) {
            System.err.println("Computing coreachable states...");

            if (!synType.equals(SynthesisAlgorithm.MONOLITHICBDD)) {
                //coreachableStatesBDD = getDepSets().reachableBackwardWorkSetAlgorithm(getMarkedStates(), computeReachableStates());
                coreachableStatesBDD = getDepSets().backwardWorkSetAlgorithm(getMarkedStates());
            } else {
                coreachableStatesBDD = manager.restrictedBackward(getMarkedStates(), forbiddenStatesBDD);
            }

//            if(options.getOptimization())
//            {
//                coreachableStatesBDD = coreachableStatesBDD.exist(
//                        getSourceVariableDomain(theIndexMap.getVariableIndexByName(orgExAutomata.getGlobalClockName())).set());
//            }

            //nbrOfCoreachableStates = nbrOfStatesBDD(coreachableStatesBDD);
            final IDD idd = generateIDD(coreachableStatesBDD, coreachableStatesBDD);
            nbrOfCoreachableStates = nbrOfStatesIDD(idd).longValue();
            System.err.println(nbrOfCoreachableStates + " coreachable states found.");
//            nbrOfCoreachableStates = 1;
        }

    }

    public BDD getReachableStates() {
        if (reachableStatesBDD == null) {
            computeReachableStates();
        }

        return reachableStatesBDD;
    }

    public BDD getCoreachableStates() {
        if (coreachableStatesBDD == null) {
            computeCoreachableStates();
        }

        return coreachableStatesBDD;
    }

    public BDD getNonblockingControllableStates(final boolean reachable) {

        if (nonblockingControllableStatesBDD == null) {
            if (synType.equals(SynthesisAlgorithm.MONOLITHICBDD)) {
                nonblockingControllableStatesBDD = manager.nonblockingControllable(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()), reachable);
            } else {
                nonblockingControllableStatesBDD = manager.disjunctiveNonblockingControllable(manager.getDisjunctiveInitiallyUncontrollableStates().or(getForbiddenLocations()), reachable);
            }

            System.err.println("Nonblocking and controllable states computed!");
            

            nonblockingControllableStatesBDD = fitIntoClockDomains(nonblockingControllableStatesBDD);

//            if(options.getOptimization())
//            {
//                nonblockingControllableStatesBDD = nonblockingControllableStatesBDD.exist(
//                        getSourceVariableDomain(theIndexMap.getVariableIndexByName(orgExAutomata.getGlobalClockName())).set());
//            }

            //nbrOfNonblockingControllableStates = nbrOfStatesBDD(nonblockingControllableStatesBDD);

            final IDD idd = generateIDD(nonblockingControllableStatesBDD, nonblockingControllableStatesBDD);
            nbrOfNonblockingControllableStates = nbrOfStatesIDD(idd).longValue();
        }

        return nonblockingControllableStatesBDD;
    }

    public BDD getControllableStates(final boolean reachable) {
        if (controllableStatesBDD == null) {
            if (synType.equals(SynthesisAlgorithm.MONOLITHICBDD)) {
                final BDD uncontrollableStates = manager.getInitiallyUncontrollableStates().or(getForbiddenLocations());
//             BDD uncontrollableStates = manager.uncontrollableBackward(manager.getInitiallyUncontrollableStates().or(getForbiddenLocations()));
                if (reachable) {
                    controllableStatesBDD = manager.restrictedForward(getInitialState(), uncontrollableStates);

                } else {
                    controllableStatesBDD = uncontrollableStates.not();
                }
            } else {
                uncontrollableStatesBDD = manager.getDisjunctiveInitiallyUncontrollableStates().or(getForbiddenLocations());
                if (reachable) {
                    controllableStatesBDD = getDepSets().forwardRestrictedWorkSetAlgorithm(getInitialState(), uncontrollableStatesBDD);
                } else {
                    controllableStatesBDD = uncontrollableStatesBDD.not();
                }
            }

            System.err.println("Controllable states computed!");

            controllableStatesBDD = fitIntoClockDomains(controllableStatesBDD);

//            if(options.getOptimization())
//            {
//                controllableStatesBDD = controllableStatesBDD.exist(
//                        getSourceVariableDomain(theIndexMap.getVariableIndexByName(orgExAutomata.getGlobalClockName())).set());
//            }

            //nbrOfControllableStates = nbrOfStatesBDD(controllableStatesBDD);
            final IDD idd = generateIDD(controllableStatesBDD, controllableStatesBDD);
            nbrOfControllableStates = nbrOfStatesIDD(idd).longValue();
        }

        return controllableStatesBDD;
    }

    public BDD getNonblockingStates() {
//        nonblockingStatesBDD = getOptimalNonblockingStates();
        if (nonblockingStatesBDD == null) {
            nonblockingStatesBDD = getReachableStates().and(getCoreachableStates());

//            getReachableStates().printDot();
            //nonblockingStatesBDD = (getCoreachableStates());
//                final IDD idd = generateIDD(nonblockingStatesBDD, nonblockingStatesBDD);
//                nbrOfNonblockingStates = nbrOfStatesIDD(idd, new HashMap<IDDNode, BigInteger>()).longValue();

//            if(options.getOptimization())
//            {
//                nonblockingStatesBDD = nonblockingStatesBDD.exist(
//                        getSourceVariableDomain(theIndexMap.getVariableIndexByName(orgExAutomata.getGlobalClockName())).set());
//            }

            //BDD2IDD2PS(nonblockingStatesBDD, nonblockingStatesBDD, "C:/Users/sajed/Desktop/IDDsnonblockingStates");
            final IDD idd = generateIDD(nonblockingStatesBDD, nonblockingStatesBDD);
            // printDOT(idd);
            nbrOfNonblockingStates = nbrOfStatesIDD(idd).longValue();
//            System.err.println("---------------------------- "+nonblockingStatesBDD.satCount(sourceStateVarSet));
//            nbrOfNonblockingStates = (long)nonblockingStatesBDD.satCount(sourceStateVarSet);

            nbrOfBlockingStates = (int) numberOfReachableStates() - nbrOfNonblockingStates;
        }

        return nonblockingStatesBDD;
    }

    public BDD getUnsafeStates() {
        manager.setBDDExAutomata(this);
        synType = SynthesisAlgorithm.MONOLITHICBDD;
//        System.err.println("compute reachable states --- begin");
//        computeReachableStates();
//        System.err.println("compute reachable states --- end");
        final BDD unsafeStates = manager.computeUnsafeStates();

//        unsafeStates.printDot();

        //       final IDD idd = generateIDD(unsafeStates, unsafeStates);
        // printDOT(idd);
        nbrOfUnsafeStates = (long) unsafeStates.satCount(sourceVariablesVarSet);
        System.err.println("Number of unsafe states: " + nbrOfUnsafeStates);

        return unsafeStates;
    }

    public TIntArrayList BDD2valuations(final BDD bdd, final String variable) {
        final TIntArrayList output = new TIntArrayList();
        for (int i = 0; i <= orgExAutomata.getMaxValueofVar(variable); i++) {
            final BDD iBDD = manager.getFactory().buildCube(i, getSourceVariableDomain(theIndexMap.getVariableIndexByName(variable)).vars());
            final BDD opBDD = bdd.and(iBDD);
            if (!opBDD.isZero()) {
                output.add(i);
            }
        }
        return output;
    }

    public long getOptimalTime() {
        if (optimalTime == -1) {
            computeOptimalTimeBDD();
        }

        return optimalTime;
    }
    
    public int getMinValueOfVar() {
        if (minValueOfVar == null) {
            computeMinValueOfVar();
        }

        return minValueOfVar;
    }    

    public String intListToFormula(final String variable, final TIntArrayList list) {
        if (list.isEmpty()) {
            return "";
        }

        final String eq = CompilerOperatorTable.getInstance().getEqualsOperator().getName();
        final String leq = CompilerOperatorTable.getInstance().getLessEqualsOperator().getName();
        final String geq = CompilerOperatorTable.getInstance().getGreaterEqualsOperator().getName();
        final String and = CompilerOperatorTable.getInstance().getAndOperator().getName();
        final String or = " " + CompilerOperatorTable.getInstance().getOrOperator().getName() + " ";

        int prevVal = list.get(0);
        if (list.size() == 1) {
            return (variable + eq + prevVal);
        }

        String output = "";
        String temp = ((variable + geq + prevVal) + and);

        int j = 0;
        for (int i = 1; i < list.size(); i++) {
            final int currVal = list.get(i);
            if (currVal == (prevVal + 1)) {
                j++;
            } else {
                if (j == 0) {
                    output += ((variable + eq + prevVal) + or);
                } else {
                    output += temp + ((variable + leq + prevVal) + or);
                }
                temp = ((variable + geq + currVal) + and);

                j = 0;
            }
            if (i == list.size() - 1) {
                if (j == 0) {
                    output += ((variable + eq + currVal) + or);
                } else {
                    output += temp + ((variable + leq + currVal) + or);
                }
            }
            prevVal = currVal;
        }
        output = output.endsWith(or) ? output.substring(0, output.length() - or.length())
                : output;

        return output;
    }

    public BDD fitIntoClockDomains(final BDD states) {
        BDD output = states.id();
        for (final VariableComponentProxy var : orgExAutomata.getVars()) {

            if (orgExAutomata.getClocks().contains(var)) {
                final int varIndex = theIndexMap.getVariableIndex(var);
                output = output.and(getBDDBitVecSource(varIndex).lte(getMaxBDDBitVecOf(varIndex)));
                output = output.and(getBDDBitVecSource(varIndex).gte(
                        manager.createSupremicaBDDBitVector(BDDBitVectoryType, getSourceVariableDomain(varIndex).varNum(), 0)));
            }

        }

        return output;
    }

    public ArrayList<String> getComplementValues(final String varName, final ArrayList<String> vals) {
        final ArrayList<String> output = new ArrayList<String>();
        for (int i = getExtendedAutomata().getMinValueofVar(varName); i <= getExtendedAutomata().getMaxValueofVar(varName); i++) {
            if (!vals.contains(i + "")) {
                output.add(i + "");
            }
        }

        return output;
    }

    public double nbrOfStatesBDD(final BDD bdd) {
        return bdd.satCount(sourceStateVarSet);
    }

    public IDD generateIDD(final BDD bdd, final BDD validStatesBDD) {
//        if(logger.isDebugEnabled())
//            System.err.println("generating IDD...");

        final HashMap<Integer, IDD> visitedNodes = new HashMap<Integer, IDD>();
        visitedNodes.put(1, new IDD(new IDDNode("1", "1")));
        IDD idd = null;
        if (bdd.isZero()) {
            idd = new IDD(new IDDNode("0", "0"));
        }

        if (bdd.isOne()) {
            idd = new IDD(new IDDNode("1", "1"));
        }

        if (bdd.nodeCount() > 0) {
            final IDDNode root = new IDDNode("" + bdd.hashCode(), getAutVarName(bdd.var()));
            idd = new IDD(root);
            final BDD varBDD = manager.getFactory().ithVar(bdd.var());//sourceBDDVar2BDD.get(bdd.var());

            BDD2IDD(bdd.low(), varBDD.not(), idd, visitedNodes, validStatesBDD);
            BDD2IDD(bdd.high(), varBDD, idd, visitedNodes, validStatesBDD);
        }

//        if(logger.isDebugEnabled())
//            System.err.println("IDD generated.");

        return idd;
    }

    public void BDD2IDD(final BDD bdd, final BDD autStatesBDD, final IDD idd, final HashMap<Integer, IDD> visitedNodes, final BDD validStatesBDD) {
//        System.err.println("BDD2IDD()");
        if (!bdd.isZero()) {
            if (bdd.isOne() || !getAutVarName(bdd.var()).equals(idd.getRoot().getName())) {
                final ArrayList<String> states = bdd2automatonStates(autStatesBDD, validStatesBDD);

                IDD nextIDD = visitedNodes.get(bdd.hashCode());

                //if 'node' has not been visited
                if (nextIDD == null) {
                    final IDDNode node = new IDDNode("" + bdd.hashCode(), getAutVarName(bdd.var()));
                    nextIDD = new IDD(node);

                    final BDD varBDD = manager.getFactory().ithVar(bdd.var());//getBDDforSourceBDDVar(bdd.var());
                    BDD2IDD(bdd.low(), varBDD.not(), nextIDD, visitedNodes, validStatesBDD);
                    BDD2IDD(bdd.high(), varBDD, nextIDD, visitedNodes, validStatesBDD);

                    if (!states.isEmpty()) {
                        idd.addChild(nextIDD, states);
                    }

                    visitedNodes.put(bdd.hashCode(), nextIDD);
                } else {
                    if (!states.isEmpty()) {
                        if (idd.labelOfChild(nextIDD) != null)//'idd' has a child with root 'node'
                        {
                            idd.labelOfChild(nextIDD).addAll(states);
                        } else {
                            idd.addChild(nextIDD, states);
                        }
                    }
                }
            } else {
                final BDD varBDD = manager.getFactory().ithVar(bdd.var());//getBDDforSourceBDDVar(bdd.var());

                BDD2IDD(bdd.low(), autStatesBDD.and(varBDD.not()), idd, visitedNodes, validStatesBDD);
                BDD2IDD(bdd.high(), autStatesBDD.and(varBDD), idd, visitedNodes, validStatesBDD);
            }
        }
    }

    ArrayList<String> bdd2automatonStates(final BDD autStatesBDD, final BDD validStatesBDD) {
//        System.err.println("bdd2automatonStates()");
        final ArrayList<String> output = new ArrayList<String>();
        final int var = autStatesBDD.var();

        if (isSourceLocationVar(var)) {
            final ExtendedAutomaton exAut = theIndexMap.getExAutomatonWithName(getAutVarName(var));
            for (final NodeProxy location : exAut.getNodes()) {
                final int locationIndex = getLocationIndex(exAut, location);
                final BDD locationBDD = manager.getFactory().buildCube(locationIndex, getSourceLocationDomain(exAut.getName()).vars());
                if (!autStatesBDD.and(locationBDD).isZero() && !locationBDD.and(validStatesBDD).isZero()) {
                    output.add(location.getName());
                }
            }
        } else {
            final int maxValue = getExtendedAutomata().getMaxValueofVar(getAutVarName(var));
            final int minValue = getExtendedAutomata().getMinValueofVar(getAutVarName(var));
            for (int i = minValue; i <= maxValue; i++) {
                final BDD valueBDD = getConstantBDD(getAutVarName(var), i);
                if (!autStatesBDD.and(valueBDD).isZero() && !valueBDD.and(validStatesBDD).isZero()) {
                    output.add("" + getIndexMap().getValOfIndex(i));
                }
            }

        }

        return output;
    }

    public String standardizePathAddress(final String path) {
        String sPath = path.replace('/', '_');
        sPath = sPath.replace('\\', '_');
        sPath = sPath.replace(':', '_');
        sPath = sPath.replace('*', '_');
        sPath = sPath.replace('"', '_');
        sPath = sPath.replace('<', '_');
        sPath = sPath.replace('>', '_');
        sPath = sPath.replace('|', '_');
        return sPath;
    }

    public void BDD2IDD2PS(final BDD bdd, final BDD validStatesBDD, final String fileName) {
        final String absPathDot = pathRoot + standardizePathAddress(fileName) + ".dot";
        final String absPathPs = pathRoot + standardizePathAddress(fileName) + ".ps";
        generateDOT(generateIDD(bdd, validStatesBDD), absPathDot);
        final Runtime rt = Runtime.getRuntime();
        try {
            final Process proc1 = rt.exec("dot -Tps " + absPathDot + " -o " + absPathPs);
            proc1.waitFor();
            proc1.exitValue();
            final Process proc2 = rt.exec("cmd /C del " + absPathDot);
            proc2.waitFor();
            proc2.exitValue();
        } catch (final Exception e) {
            System.out.println(e);
        }

    }

    public void IDD2DOT(final BufferedWriter out, final IDD idd, final HashSet<IDD> visited) {
        final IDDNode root = idd.getRoot();
        try {
            if (!visited.contains(idd)) {
                out.write("" + root.getID() + " [label=\"" + root.getName() + "\"];");
                out.newLine();
                visited.add(idd);

                for (final IDD child : idd.getChildren()) {
                    String temp = "" + root.getID() + " -> " + child.getRoot().getID() + " [label=\"";
                    final ArrayList<String> label = idd.labelOfChild(child);
                    if (label.size() > 0) {
                        temp += label.get(0);
                    }
                    for (int i = 1; i < label.size(); i++) {
                        temp += ("|" + label.get(i));
                    }

                    out.write(temp + "\"];");
                    out.newLine();
                    IDD2DOT(out, child, visited);

                }
            }
        } catch (final Exception e) {
            logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public void generateDOT(final IDD idd, final String path) {
        try {
            final FileWriter fstream = new FileWriter(path);
            final BufferedWriter out = new BufferedWriter(fstream);
            out.write("digraph G {");
            out.newLine();
            out.write("size = \"7.5,10\"");
            out.newLine();
            out.write("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");
            out.newLine();

            final HashSet<IDD> visited = new HashSet<IDD>();
            if (idd.nbrOfNodes() > 1) {
                IDD2DOT(out, idd, visited);
            }
            out.write("}");
            out.close();
        } catch (final Exception e) {
            logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public void printDOT(final IDD idd) {

        System.out.println("digraph G {");
        System.out.println("size = \"7.5,10\"");
        System.out.println("1 [shape=box, label=\"1\", style=filled, shape=box, height=0.3, width=0.3];");

        final HashSet<IDD> visited = new HashSet<IDD>();
        if (idd.nbrOfNodes() > 1) {
            IDD2DOTPrint(idd, visited);
        }
        System.out.println("}");

    }

    public void IDD2DOTPrint(final IDD idd, final HashSet<IDD> visited) {
        final IDDNode root = idd.getRoot();
        try {
            if (!visited.contains(idd)) {
                System.out.println("" + root.getID() + " [label=\"" + root.getName() + "\"];");
                visited.add(idd);

                for (final IDD child : idd.getChildren()) {
                    String temp = "" + root.getID() + " -> " + child.getRoot().getID() + " [label=\"";
                    final ArrayList<String> label = idd.labelOfChild(child);
                    if (label.size() > 0) {
                        temp += label.get(0);
                    }
                    for (int i = 1; i < label.size(); i++) {
                        temp += ("|" + label.get(i));
                    }

                    System.out.println(temp + "\"];");
                    IDD2DOTPrint(child, visited);

                }
            }
        } catch (final Exception e) {
            logger.error("IDD to DOT: " + e.getMessage());
        }
    }

    public BigInteger nbrOfStatesIDD(final IDD idd) {
        //iterate through the idd-variables and find the first one that corresponds to a varibale or automaton
        if (idd.getRoot().getName().equals("0")) {
            return BigInteger.ZERO;
        }

        String firstVarAut = "";
        for (final Object va : variableOrdering) {
            if (va instanceof ExtendedAutomaton) {
                firstVarAut = ((ExtendedAutomaton) va).getName();
                break;
            }
            if (va instanceof VariableComponentProxy) {
                firstVarAut = ((VariableComponentProxy) va).getName();
                break;
            }
        }
        final int indexVar1 = variableOrderingNames.indexOf(firstVarAut);
        final int indexVar2 = variableOrderingNames.indexOf(idd.getRoot().getName());
        return nbrOfStatesIDD(idd, new HashMap<IDDNode, BigInteger>()).multiply(statesBetweenVars(indexVar1, indexVar2));
    }

    public BigInteger nbrOfStatesIDD(final IDD idd, final HashMap<IDDNode, BigInteger> cache) {
        BigInteger nbrOfStates = BigInteger.ZERO;
        if (idd.isOneTerminal()) {
            return BigInteger.ONE;
        } else {
            for (final IDD child : idd.getChildren()) {
                final int indexVar1 = variableOrderingNames.indexOf(idd.getRoot().getName());
                final int indexVar2 = variableOrderingNames.indexOf(child.getRoot().getName());
                final BigInteger statesBetweenChildren = statesBetweenVars(indexVar1 + 1, indexVar2);
                final BigInteger currStates = BigInteger.valueOf(idd.labelOfChild(child).size());
                BigInteger newNbrOfStates = cache.get(child.getRoot());
                if (newNbrOfStates == null) {
                    newNbrOfStates = nbrOfStatesIDD(child, cache);
                }
                nbrOfStates = nbrOfStates.add(statesBetweenChildren.multiply(newNbrOfStates.multiply(currStates)));
            }
        }
        cache.put(idd.getRoot(), nbrOfStates);
        return nbrOfStates;

    }

    public BigInteger statesBetweenVars(final int indexVar1, final int indexVar2) {
        BigInteger output = BigInteger.ONE;
        if (!(variableOrderingNames.get(indexVar1)).equals("1")) {
            for (final String var : variableOrderingNames.subList(indexVar1, indexVar2)) {
                final int autVar = theIndexMap.isStringEFAorVar(var);
                if (autVar == 0) {
                    output = output.multiply(BigInteger.valueOf(theIndexMap.getExAutomatonWithName(var).getNodes().size()));
                } else if (autVar == 1) {
                    output = output.multiply(BigInteger.valueOf(orgExAutomata.getVarDomain(var)));
                }
            }
        }
        return output;
    }

    public BDDExtendedAutomaton getBDDExAutomaton(final String autName) {
        final ExtendedAutomaton efa = theIndexMap.getExAutomatonWithName(autName);
        return (efa == null ? null : automatonToBDDAutomatonMap.get(efa));
    }

    public List<EventDeclProxy> getInverseAlphabet(final ExtendedAutomaton exAutomaton) {
        return orgExAutomata.getInverseAlphabet(exAutomaton);
    }

    public int getLocationIndex(final ExtendedAutomaton theAutomaton, final NodeProxy theLocation) {
        return theIndexMap.getLocationIndex(theAutomaton.getName(), theLocation.getName());
    }

    public int getEventIndex(final EventDeclProxy theEvent) {
        return theIndexMap.getEventIndex(theEvent);
    }

    public int getEventIndex(final String theEvent) {
        return theIndexMap.getEventIndex(theIndexMap.eventIdToProxy(theEvent));
    }

    public BDDVarSet getEventVarSet() {
        return eventDomain.set();
    }

    public BDDDomain getEventDomain() {
        return eventDomain;
    }

    public BDDEdges getBDDEdges() {
        computeBDDEdges();
        return bddEdges;
    }

    private void computeBDDEdges() {
        if (bddEdges == null) {
            bddEdges = new BDDEdgeFactory(this).createEdges();
        }
    }

    public void addInitialLocations(final BDD initialLocations) {
        initialLocationsBDD = initialLocationsBDD.and(initialLocations);
    }

    public void addMarkedLocations(final BDD markedLocations) {
        markedLocationsBDD = markedLocationsBDD.and(markedLocations);
    }

    public BDDExtendedManager getManager() {
        return manager;
    }

    public ExtendedAutomataIndexMap getIndexMap() {
        return theIndexMap;
    }

    public BDD[] getForwardTransAndNextValsForV(final BDDExtendedAutomaton aut) {
        return forwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getForwardTransWhereVisUpdated(final BDDExtendedAutomaton aut) {
        return forwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getForwardTransWhereVHasAppearedInGuard(final BDDExtendedAutomaton aut) {
        return forwardTransWhereVHasAppearedInGuard[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransAndNextValsForV(final BDDExtendedAutomaton aut) {
        return backwardTransAndNextValsForV[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getBackwardTransWhereVisUpdated(final BDDExtendedAutomaton aut) {
        return backwardTransWhereVisUpdated[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getTransAndNextValsForVWithoutDestClocks(final BDDExtendedAutomaton aut) {
        return transAndNextValsForVWithoutDestClocks[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD[] getTransWhereVisUpdatedWithoutDestClocks(final BDDExtendedAutomaton aut) {
        return transWhereVisUpdatedWithoutDestClocks[theIndexMap.getExAutomatonIndex(aut.getExAutomaton().getName())];
    }

    public BDD getMarkedLocations() {
        return markedLocationsBDD;
    }

    public BDDPairing getDestToSourceClockPairing() {
        return destToSourceClockPairing;
    }

    public BDDPairing getDestToSourceLocationPairing() {
        return destToSourceLocationPairing;
    }

    public BDDPairing getDestToSourceVariablePairing() {
        return destToSourceVariablePairing;
    }

    public BDDPairing getSourceToDestLocationPairing() {
        return sourceToDestLocationPairing;
    }

    public BDDPairing getSourceToDestVariablePairing() {
        return sourceToDestVariablePairing;
    }

    public BDDPairing getTempToSourceClockPairing() {
        return tempToSourceClockPairing;
    }

    public BDDPairing getTempToDestClockPairing() {
        return tempToDestClockPairing;
    }

    public BDDPairing getTempToDestVariablePairing() {
        return tempToDestVariablePairing;
    }

    public BDDVarSet getSourceLocationVarSet() {
        return sourceLocationVarSet;
    }

    public BDDVarSet getDestLocationVarSet() {
        return destLocationVarSet;
    }

    public BDD getUncontrollableEvents() {
        return uncontrollableEventsBDD;
    }

    void add(final BDDExtendedAutomaton bddExAutomaton) {
        theBDDAutomataList.add(bddExAutomaton);
        automatonToBDDAutomatonMap.put(bddExAutomaton.getExAutomaton(), bddExAutomaton);
    }

    public Iterator<BDDExtendedAutomaton> iterator() {
        return theBDDAutomataList.iterator();
    }

    public BDDExtendedAutomaton getBDDExAutomaton(final ExtendedAutomaton theAutomaton) {
        return automatonToBDDAutomatonMap.get(theAutomaton);
    }

    public BDDExtendedAutomaton getBDDExAutomatonAt(final int i) {
        return automatonToBDDAutomatonMap.get(theIndexMap.getExAutomatonAt(i));
    }

    public SynthesisAlgorithm getSynthAlg() {
        return synType;
    }

    public BDDExDisjDepSets getDepSets() {
        if (depSets == null) {
            depSets = BDDExDisjPartitioningTypeFactory.getDepSets(this, synType);
        }
        return depSets;
    }
}
