package org.supremica.automata;

import gnu.trove.TIntObjectHashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.ComponentKind;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 *
 * @author Sajed, Zhennan
 */

public class FlowerEFABuilder {

    private String name;
    private int nbrOfJobs;
    private int nbrOfResources;
    private int[] resourceCapacities;
    private int[] nbrOfTransitionsForJob;
    public  int[] nbrOfStagesForJob;
    private int[][][] demandAtStage;
    private int[][] maxInstancesAtStage;

    private Map<Integer,Set<Pair>> resourceToUsedInStages;
    private Map<Integer, Set<Pair>> jobToTransitions;
    private Map<Integer, Set<Integer>> jobToInitialStages;
    private Map<Integer, Set<Integer>> jobToLastStages;
    public static String STAGE_PREFIX = "s";
    public static String RESOURCE_PREFIX = "r";
    public static String LOAD_EVENT_PREFIX = "load";
    public String feasibleEquation = "";
    public static TIntObjectHashMap<String> resourceToFeasibleEquationMap; // use for ant
    public static TIntObjectHashMap<List<String>> resourceToEventNamesMap; // use for ant
    public ExtendedAutomata exAutomata;
    private ModuleSubject module;
    
    public FlowerEFABuilder(final File rasFile, final ModuleSubject module) {
        this.module = module;

        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(rasFile));
            nbrOfJobs = Integer.parseInt(br.readLine());
            nbrOfTransitionsForJob = new int[nbrOfJobs];
            nbrOfStagesForJob = new int[nbrOfJobs];
            demandAtStage = new int[nbrOfJobs][][];
            maxInstancesAtStage = new int[nbrOfJobs][];
            jobToTransitions = new HashMap<Integer, Set<Pair>>();
            jobToInitialStages = new HashMap<Integer, Set<Integer>>();
            jobToLastStages = new HashMap<Integer, Set<Integer>>();
            nbrOfResources = Integer.parseInt(br.readLine());
            resourceToFeasibleEquationMap = new TIntObjectHashMap<String>(nbrOfResources);
            resourceToEventNamesMap = new TIntObjectHashMap<List<String>>(nbrOfResources);
            resourceToUsedInStages = new HashMap<Integer, Set<Pair>>();
            resourceCapacities = new int[nbrOfResources];
            
            StringTokenizer st = new StringTokenizer(br.readLine());

            int i = 0;
            while (st.hasMoreTokens()) {
                resourceToUsedInStages.put(i, new HashSet<Pair>());
                resourceCapacities[i++] = Integer.parseInt(st.nextToken());
            }

            for (i = 0; i < nbrOfJobs; i++) {           
                nbrOfStagesForJob[i] = Integer.parseInt(br.readLine());
                maxInstancesAtStage[i] = new int[nbrOfStagesForJob[i]];

                demandAtStage[i] = new int[nbrOfStagesForJob[i]][];

                final Set<Integer> initialStages = new HashSet<Integer>();
                final Set<Integer> lastStages = new HashSet<Integer>();
                for (int j = 0; j < nbrOfStagesForJob[i]; j++) {
                    maxInstancesAtStage[i][j] = Integer.MAX_VALUE;
                    initialStages.add(j);
                    lastStages.add(j);
                    demandAtStage[i][j] = new int[nbrOfResources];
                    st = new StringTokenizer(br.readLine());
                    int k = 0;
                    int maxInstances;
                    while (st.hasMoreTokens()) {
                        final int nbrOfNeededResources = Integer.parseInt(st.nextToken());
                        if (nbrOfNeededResources > 0) {
                            maxInstances = resourceCapacities[k]
                                    / nbrOfNeededResources;
                            if (maxInstances < maxInstancesAtStage[i][j]) {
                                maxInstancesAtStage[i][j] = maxInstances;
                            }
                        }

                        demandAtStage[i][j][k] = nbrOfNeededResources;
                        resourceToUsedInStages.get(k).add(new Pair(i, j));
                        k++;
                    }
                }

                nbrOfTransitionsForJob[i] = Integer.parseInt(br.readLine());
                final Set<Pair> trans = new HashSet<Pair>();

                for (int j = 0; j < nbrOfTransitionsForJob[i]; j++) {
                    st = new StringTokenizer(br.readLine());
                    final int sourceStage = Integer.parseInt(st.nextToken());
                    final int targetStage = Integer.parseInt(st.nextToken());
                    
                    trans.add(new Pair(sourceStage, targetStage));
                    initialStages.remove(targetStage);
                    lastStages.remove(sourceStage);
                }
                jobToTransitions.put(i, trans);
                jobToInitialStages.put(i, initialStages);
                jobToLastStages.put(i, lastStages);
            }

            //Compute the equations the represents the feasible states in the model
            feasibleEquation = "";
            for (i = 0; i < nbrOfResources; i++) {
                
                String resourceGuard = "";
                for (final Pair jobStage : resourceToUsedInStages.get(i)) {
                    if (demandAtStage[jobStage.p1][jobStage.p2][i] > 0
                            && !jobToLastStages.get(jobStage.p1).contains(jobStage.p2)) {

                        String stage = "";
                        for (int k = 1; k <=  demandAtStage[jobStage.p1][jobStage.p2][i]; 
                                                                                    k++){
                            stage = stage + " + " + STAGE_PREFIX + jobStage.p1 + jobStage.p2;
                        }
                        
                        resourceGuard = resourceGuard + stage;
                    }
                }

                    resourceGuard = "(" + RESOURCE_PREFIX + i + resourceGuard 
                            + ") == " + resourceCapacities[i];
                    final String and = feasibleEquation.isEmpty() ? "" : " & ";
                    feasibleEquation = feasibleEquation + and + resourceGuard;
                    resourceToFeasibleEquationMap.put(i, resourceGuard);
                
                resourceToEventNamesMap.put(i, new ArrayList<String>());
            }
        } catch (IOException e) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (IOException e) {
            }
        }
        
    }
    
    public void buildEFA() {     
         
        ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        
        factory.createSimpleIdentifierProxy(name);
        
        final SimpleIdentifierProxy ident = factory
                .createSimpleIdentifierProxy(EventDeclProxy.DEFAULT_MARKING_NAME);
        module.getEventDeclListModifiable()
                .add(factory.createEventDeclProxy(ident, EventKind.PROPOSITION));

        exAutomata = new ExtendedAutomata(module);
        int i;
        for (i = 0; i < nbrOfResources; i++) {
            exAutomata.addIntegerVariable(RESOURCE_PREFIX + i,
                    0,
                    resourceCapacities[i],
                    resourceCapacities[i],
                    resourceCapacities[i]);
        }
        
        for (i = 0; i < nbrOfJobs; i++) {
            
            final ExtendedAutomaton efa = new ExtendedAutomaton("Job" + i, ComponentKind.PLANT);
            efa.addState("J" + i, false, true, false);

            efa.addEvent(LOAD_EVENT_PREFIX + i, "CONTROLLABLE");

            for (final Integer init : jobToInitialStages.get(i)) {
                final String targetStageVar = STAGE_PREFIX + i + init;

                String guard = "";
                String action = targetStageVar + "+=1";;

                for (int r = 0; r < nbrOfResources; r++) {
                    final int targetDemand = demandAtStage[i][init][r];
                    if (targetDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;
                        final String and = guard.isEmpty() ? "" : " & ";
                        guard = guard + and + resourceVar + ">="
                                + targetDemand;

                        action = action + ";" + resourceVar + "-="
                                + targetDemand + ";";                      
                        resourceToEventNamesMap.get(r).add(LOAD_EVENT_PREFIX + i);
                    }

                }

                efa.addTransition("J" + i,
                        "J" + i,
                        LOAD_EVENT_PREFIX + i + ";",
                        guard,
                        action);
            }

            for (final Pair tran : jobToTransitions.get(i)) {
                String guard = "";
                String action = "";

                final String sourceStageVar = STAGE_PREFIX + i + tran.p1;
                final String targetStageVar = STAGE_PREFIX + i + tran.p2;

                action = sourceStageVar + "-=1";
                if (!jobToLastStages.get(i).contains(tran.p2)) {
                    action = action + ";" + targetStageVar + "+=1";
                }

                guard = sourceStageVar + ">0";

                for (int r = 0; r < nbrOfResources; r++) {
                    final int sourceDemand = demandAtStage[i][tran.p1][r];
                    if (sourceDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;

                        action = action + ";" + resourceVar + "+="
                                + sourceDemand + ";";
                    }

                    final int targetDemand = demandAtStage[i][tran.p2][r];
                    if (targetDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;
                        guard = guard + " & " + resourceVar + ">="
                                + targetDemand;

                        if (!jobToLastStages.get(i).contains(tran.p2)) {
                            action = action + ";" + resourceVar + "-="
                                    + targetDemand + ";";
                        }
                        resourceToEventNamesMap.get(r)
                                .add(sourceStageVar + targetStageVar);
                    }

                }

                efa.addEvent(sourceStageVar + targetStageVar, "CONTROLLABLE");
                efa.addTransition("J" + i,
                        "J" + i,
                        (sourceStageVar + targetStageVar) + ";",
                        guard,
                        action);            
            }

            for (int j = 0; j < nbrOfStagesForJob[i] - 1; j++) {
                exAutomata.addIntegerVariable(STAGE_PREFIX + i + j,
                        0,
                        maxInstancesAtStage[i][j],
                        0,
                        0);
            }
            
            for (int j = 0; j < nbrOfStagesForJob[i] - 1; j++) {

                String stageVarName = STAGE_PREFIX + i + j;

                VariableComponentProxy stageVar = VariableHelper.createIntegerVariable(
                        stageVarName,
                        0,
                        maxInstancesAtStage[i][j],
                        0,
                        0);
                exAutomata.getVars().add(stageVar);
                exAutomata.getStageVars().add(stageVar);

                if (!exAutomata.var2MinMaxValMap.containsKey(stageVarName)) {

                    ExtendedAutomata.MinMax minMax = exAutomata
                            .new MinMax(0, maxInstancesAtStage[i][j]);

                    exAutomata.var2MinMaxValMap.put(stageVarName, minMax);
                }

                if (!exAutomata.var2domainMap.containsKey(stageVarName)) {
                    exAutomata.var2domainMap.
                            put(stageVarName, maxInstancesAtStage[i][j] + 1);
                }
            }
                      
            exAutomata.addAutomaton(efa);
        }
        
        module.setComment(feasibleEquation);
    }

    class Pair {

        private final int p1;
        private final int p2;

        Pair(final int p1, final int p2) {
            this.p1 = p1;
            this.p2 = p2;
        }
    }
    
    // Builds an ExtendedAutomata object directly from RAS (ant task)
    public ExtendedAutomata getEFAforRAS() {
        
        Map<String, VariableComponentProxy> varName2VariableMap = 
                new HashMap<String, VariableComponentProxy>();
        
        Map<String, List<String>> varName2relatedVarNamesMap = 
                new HashMap<String, List<String>>();
        
        int domain = 0;
             
        // create an empty extended automata
        exAutomata = new ExtendedAutomata();
        
        // added the resource variables
        for (int r = 0; r < nbrOfResources; r++) {

            String varName = RESOURCE_PREFIX + r;
            
            VariableComponentProxy resourceVar = VariableHelper.createIntegerVariable(
                    varName,
                    0,
                    resourceCapacities[r],
                    resourceCapacities[r],
                    resourceCapacities[r]);

            exAutomata.getVars().add(resourceVar);

            if (!exAutomata.var2MinMaxValMap.containsKey(varName)) {

                ExtendedAutomata.MinMax minMax = exAutomata.new MinMax(0, resourceCapacities[r]);

                exAutomata.var2MinMaxValMap.put(varName, minMax);
            }

            if (!exAutomata.var2domainMap.containsKey(varName)) {
                exAutomata.var2domainMap.put(varName, resourceCapacities[r] + 1);
            }
            
            if (!varName2VariableMap.containsKey(varName)) {
                varName2VariableMap.put(varName, resourceVar);
            }
            
            if (domain < resourceCapacities[r] + 1)
                domain = resourceCapacities[r] + 1;
        }
        
        // create automata coppesponding to jobs and instance variables
        for (int i = 0; i < nbrOfJobs; i++) {

            // create an empty automaton
            final ExtendedAutomaton efa = new ExtendedAutomaton("Job" + i, ComponentKind.PLANT);
            efa.addState("J" + i, false, true, false);

            efa.addEvent(LOAD_EVENT_PREFIX + i, "CONTROLLABLE");

            // loading transitions
            for (final Integer init : jobToInitialStages.get(i)) {

                List<String> relatedVars = new ArrayList<String>();
                
                final String targetStageVar = STAGE_PREFIX + i + init;

                String guard = "";
                String action = targetStageVar + "+=1";;

                for (int r = 0; r < nbrOfResources; r++) {
                    final int targetDemand = demandAtStage[i][init][r];
                    if (targetDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;
                        relatedVars.add(resourceVar);
                        final String and = guard.isEmpty() ? "" : " & ";
                        guard = guard + and + resourceVar + ">="
                                + targetDemand;

                        action = action + ";" + resourceVar + "-="
                                + targetDemand + ";";
                        resourceToEventNamesMap.get(r).add(LOAD_EVENT_PREFIX + i);
                        
                    }

                }

                efa.addTransition("J" + i,
                        "J" + i,
                        LOAD_EVENT_PREFIX + i + ";",
                        guard,
                        action);
                
                for(String var: relatedVars) {
                    List<String> tmp = new ArrayList<String>(relatedVars);
                    tmp.remove(var);
                    if(!varName2relatedVarNamesMap.containsKey(var)) {
                        varName2relatedVarNamesMap.put(var, tmp);
                    } else {
                        varName2relatedVarNamesMap.get(var).addAll(tmp);
                    } 
                }
            }

            // process advancement transitions
            for (final Pair tran : jobToTransitions.get(i)) {
                String guard = "";
                String action = "";

                final String sourceStageVar = STAGE_PREFIX + i + tran.p1;
                final String targetStageVar = STAGE_PREFIX + i + tran.p2;

                action = sourceStageVar + "-=1";
                if (!jobToLastStages.get(i).contains(tran.p2)) {
                    action = action + ";" + targetStageVar + "+=1";
                }

                guard = sourceStageVar + ">0";
                
                List<String> relatedVarNames = new ArrayList<String>();
                relatedVarNames.add(sourceStageVar);

                for (int r = 0; r < nbrOfResources; r++) {
                    final int sourceDemand = demandAtStage[i][tran.p1][r];
                    if (sourceDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;

                        action = action + ";" + resourceVar + "+="
                                + sourceDemand + ";";
                    }

                    final int targetDemand = demandAtStage[i][tran.p2][r];
                    if (targetDemand > 0) {
                        final String resourceVar = RESOURCE_PREFIX + r;
                        guard = guard + " & " + resourceVar + ">="
                                + targetDemand;

                        if (!jobToLastStages.get(i).contains(tran.p2)) {
                            action = action + ";" + resourceVar + "-="
                                    + targetDemand + ";";
                        }
                        resourceToEventNamesMap.get(r)
                                .add(sourceStageVar + targetStageVar);
                        relatedVarNames.add(resourceVar);
                    }

                }

                efa.addEvent(sourceStageVar + targetStageVar, "CONTROLLABLE");
                efa.addTransition("J" + i,
                        "J" + i,
                        (sourceStageVar + targetStageVar) + ";",
                        guard,
                        action);
                
                for (String var : relatedVarNames) {
                    List<String> tmp = new ArrayList<String>(relatedVarNames);
                    tmp.remove(var);
                    if (!varName2relatedVarNamesMap.containsKey(var)) {
                        varName2relatedVarNamesMap.put(var, tmp);
                    } else {
                        varName2relatedVarNamesMap.get(var).addAll(tmp);
                    }
                }

            }

            // instance variables
            for (int j = 0; j < nbrOfStagesForJob[i] - 1; j++) {

                String stageVarName = STAGE_PREFIX + i + j;

                VariableComponentProxy stageVar = VariableHelper.createIntegerVariable(
                        stageVarName,
                        0,
                        maxInstancesAtStage[i][j],
                        0,
                        0);
                exAutomata.getVars().add(stageVar);
                exAutomata.getStageVars().add(stageVar);

                if (!exAutomata.var2MinMaxValMap.containsKey(stageVarName)) {

                    ExtendedAutomata.MinMax minMax = exAutomata
                            .new MinMax(0, maxInstancesAtStage[i][j]);

                    exAutomata.var2MinMaxValMap.put(stageVarName, minMax);
                }

                if (!exAutomata.var2domainMap.containsKey(stageVarName)) {
                    exAutomata.var2domainMap.
                            put(stageVarName, maxInstancesAtStage[i][j] + 1);
                }
                
                if (!varName2VariableMap.containsKey(stageVarName)) {
                    varName2VariableMap.put(stageVarName, stageVar);
                }
                
                if(domain < maxInstancesAtStage[i][j] + 1)
                    domain = maxInstancesAtStage[i][j] + 1;
            }
            
            // set the domain to exAutomata 
            exAutomata.setDomain(domain);
            
            // adding efa into the exAutomata

            exAutomata.getExtendedAutomataList().add(efa);
            exAutomata.setNbrOfExAutomata(exAutomata.getNbrExAutomata() + 1);
            exAutomata.getStringToExAutomaton().put(efa.getName(), efa);

            for (final EventDeclProxy event : efa.getAlphabet()) {
                
                if (exAutomata.getEventIdToProxyMap().get(event.getName()) == null) {
                    exAutomata.getEventIdToProxyMap().put(event.getName(), event);
                    exAutomata.unionAlphabet.add(event);
                    if (event.getKind().value().equals("controllable") || 
                            event.getKind().equals(EventKind.CONTROLLABLE)) {
                        exAutomata.controllableAlphabet.add(event);
                    } else if (event.getKind().value().equals("uncontrollable") || 
                            event.getKind().equals(EventKind.UNCONTROLLABLE)) {
                        exAutomata.uncontrollableAlphabet.add(event);
                    }
                }
            }
        }
        
        for(VariableComponentProxy var: exAutomata.getVars()) {
           
            exAutomata.var2relatedVarsMap.put(var, new ArrayList<VariableComponentProxy>());
            String varName = var.getName();
            
            if (varName2relatedVarNamesMap.containsKey(varName)) {
                for(String rVarName: varName2relatedVarNamesMap.get(varName)) {
                    exAutomata.var2relatedVarsMap.get(var)
                            .add(varName2VariableMap.get(rVarName));
                }
            }  
        }        
        return exAutomata;
    }
}
