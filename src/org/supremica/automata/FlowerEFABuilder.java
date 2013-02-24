package org.supremica.automata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.xsd.base.EventKind;

/**
 *
 * @author Sajed
 *
 * The class builds a set of EFAs (one location plus several self-loops, aka
 * flower structure).
 * The structure was firstly introduced in CASE11 authored by
 * Z. Fei, S. Miremadi and K. ?kesson.
 */

public class FlowerEFABuilder {

    private int nbrOfJobs;
    private int nbrOfResources;

    private int[] resourceCapacities;
    private int[] nbrOfTransitionsForJob;
    public static int[] nbrOfStagesForJob;
    private int[][][] demandAtStage;
    private int[][] maxInstancesAtStage;

    private Map<Integer,Set<Pair>> resourceToUsedInStages;
    private Map<Integer, Set<Pair>> jobToTransitions;
    private Map<Integer, Set<Integer>> jobToInitialStages;
    private Map<Integer, Set<Integer>> jobToLastStages;

    public static String STAGE_PREFIX = "s";
    public static String RESOURCE_PREFIX = "r";
    public static String LOAD_EVENT_PREFIX = "load";
    public static String feasibleEquation = "";

    private ExtendedAutomata exAutomata;
    private final ModuleSubject module;

    public FlowerEFABuilder (final File rasFile, final ModuleSubject module)
    {
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
        for(i = 0; i < nbrOfResources; i++)
        {
            String resourceGuard = "";
            for(final Pair jobStage:resourceToUsedInStages.get(i))
            {
                final String plus = resourceGuard.isEmpty() ? "(" : " + ";
                if(demandAtStage[jobStage.p1][jobStage.p2][i] > 0 &&
                        !jobToLastStages.get(jobStage.p1).contains(jobStage.p2))
                {
                    final String coefficient =
                            demandAtStage[jobStage.p1][jobStage.p2][i] > 1 ?
                                demandAtStage[jobStage.p1][jobStage.p2][i]+"*" :
                                "";
                    resourceGuard = resourceGuard + plus +
                                    coefficient + STAGE_PREFIX +
                                    jobStage.p1 + jobStage.p2;
                }
            }

            if(!resourceGuard.isEmpty())
            {
                resourceGuard = resourceGuard + " + " + RESOURCE_PREFIX + i +
                                ") == " + resourceCapacities[i];
                final String and = feasibleEquation.isEmpty() ? "" : " & ";
                feasibleEquation = feasibleEquation + and + resourceGuard;
            }

        }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            try{
                if(br != null) 
                    br.close();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        System.err.println(feasibleEquation);
    }

    public void buildEFA()
    {
        exAutomata = new ExtendedAutomata(module);
        final ModuleSubjectFactory factory = ModuleSubjectFactory.getInstance();
        module.getEventDeclListModifiable().add(
                        factory.createEventDeclProxy(
                                    factory.createSimpleIdentifierProxy(
                                        EventDeclProxy.DEFAULT_MARKING_NAME),
                                        EventKind.PROPOSITION));

        int i;
        for(i = 0; i < nbrOfResources; i++)
        {
            exAutomata.addIntegerVariable(RESOURCE_PREFIX+i,
                                          0,
                                          resourceCapacities[i],
                                          resourceCapacities[i],
                                          resourceCapacities[i]);
        }
        
        for(i = 0; i < nbrOfJobs; i++)
        {
            @SuppressWarnings("deprecation")
            final
            ExtendedAutomaton efa = new ExtendedAutomaton("Job"+i,
                                                          exAutomata,
                                                          true);
            efa.addState("J"+i, false, true, false);

            exAutomata.addEvent(LOAD_EVENT_PREFIX+i);

            for(final Integer init : jobToInitialStages.get(i))
            {
                final String targetStageVar = STAGE_PREFIX+i+init;

                String guard = "";
                String action = targetStageVar+"+=1";;

                for(int r = 0; r < nbrOfResources; r++)
                {
                    final int targetDemand = demandAtStage[i][init][r];
                    if(targetDemand > 0)
                    {
                        final String resourceVar = RESOURCE_PREFIX+r;
                        final String and = guard.isEmpty() ? "" : " & ";
                        guard = guard + and + resourceVar + ">=" +
                                        targetDemand;

                        action = action + ";" + resourceVar+ "-=" +
                                                targetDemand + ";";
                    }

                }

                efa.addTransition("J"+i,
                                  "J"+i,
                                  LOAD_EVENT_PREFIX+i+";",
                                  guard,
                                  action);
            }

            for(final Pair tran:jobToTransitions.get(i))
            {
                String guard = "";
                String action = "";

                final String sourceStageVar = STAGE_PREFIX+i+tran.p1;
                final String targetStageVar = STAGE_PREFIX+i+tran.p2;

                action = sourceStageVar + "-=1";
                if(!jobToLastStages.get(i).contains(tran.p2))
                    action = action + ";" + targetStageVar + "+=1";

                guard = sourceStageVar + ">0";

                for(int r = 0; r < nbrOfResources; r++)
                {
                    final int sourceDemand = demandAtStage[i][tran.p1][r];
                    if(sourceDemand > 0)
                    {
                        final String resourceVar = RESOURCE_PREFIX+r;

                        action = action + ";" + resourceVar+ "+=" +
                                          sourceDemand + ";";
                    }

                    final int targetDemand = demandAtStage[i][tran.p2][r];
                    if(targetDemand > 0)
                    {
                        final String resourceVar = RESOURCE_PREFIX+r;
                        guard = guard + " & " + resourceVar + ">=" +
                                        targetDemand;

                        if(!jobToLastStages.get(i).contains(tran.p2))
                        {
                            action = action + ";" + resourceVar+"-=" +
                                              targetDemand + ";";
                        }
                    }

                }


                exAutomata.addEvent(sourceStageVar + targetStageVar);
                efa.addTransition("J"+i,
                                  "J"+i,
                                  (sourceStageVar + targetStageVar)+";",
                                  guard,
                                  action);
            }

            exAutomata.addAutomaton(efa);

            for(int j = 0; j < nbrOfStagesForJob[i]-1; j++)
            {
                exAutomata.addIntegerVariable(STAGE_PREFIX+i+j,
                                              0,
                                              maxInstancesAtStage[i][j],
                                              0,
                                              0);
            }
        }
        
    }

    class Pair
    {
        private final int p1;
        private final int p2;
        Pair(final int p1, final int p2)
        {
            this.p1 = p1;
            this.p2 = p2;
        }

    }
}
