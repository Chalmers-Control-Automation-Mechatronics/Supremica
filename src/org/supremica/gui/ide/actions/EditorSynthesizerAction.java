//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorSynthesizerAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import gnu.trove.TIntArrayList;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import net.sf.javabdd.BDD;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.module.VariableComponentProxy;

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import org.supremica.automata.BDD.BDDSynthesizer;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
import org.supremica.automata.BDD.EFA.BDDMonolithicEdges;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.gui.EditorSynthesizerDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class EditorSynthesizerAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    public EditorSynthesizerAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Symbolic Synthesis/Optimization on TEFAs...");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Synthesize a modular supervisor by adding guards to the original automata");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));
    }

    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    public void doAction()
    {
        //QMC minimizer test
/*
        List<String> variables = new ArrayList<String>();
        variables.add("vR1");
        variables.add("vR2");

        List<Integer> minTerms = new ArrayList<Integer>();
        minTerms.add(0);
        minTerms.add(1);
        List<Integer> dontCareTerms = new ArrayList<Integer>();
        QMCMinimizerSupremica qmc = new QMCMinimizerSupremica(variables, minTerms, dontCareTerms);
        System.err.println(qmc.minimize());
*/

        final ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getModuleSubject();

        final int nbrOfComponents = module.getComponentList().size();
        if(nbrOfComponents == 0)
            return;

        // Synchronize EFAs
/*        SynchronizationOptions so = new SynchronizationOptions();
        AutomataSynchronizer as = new AutomataSynchronizer(module.getComponentListModifiable(),so);
        SimpleComponentProxy synchedEA = as.getSynchronizedComponent();
        ide.getActiveDocumentContainer().getEditorPanel().addComponent(synchedEA);
        System.out.println(synchedEA.getGraph().getNodes().size());
*/

        final EditorSynthesizerOptions options = new EditorSynthesizerOptions();

        final Vector<String> eventNames = new Vector<String>();

        for(final EventDeclSubject sigmaS:  module.getEventDeclListModifiable())
        {
            if(sigmaS.getKind() == EventKind.CONTROLLABLE)// || sigmaS.getKind() == EventKind.UNCONTROLLABLE)
            {
                eventNames.add(sigmaS.getName());
            }
        }
        
        final Vector<String> variableNamesForBox = new Vector<String>();
        variableNamesForBox.add("No variable selected");
        for (final Proxy sub : module.getComponentList()) {
            if (sub instanceof VariableComponentProxy) {
                variableNamesForBox.add(((VariableComponentProxy)sub).getName());
            }                    
        }

        final Vector<String> eventNamesForBox = new Vector<String>(eventNames);
        eventNamesForBox.add(0,"Generate guards for ALL controllable events");

        final EditorSynthesizerDialog synthesizerDialog = new EditorSynthesizerDialog(ide.getFrame(), nbrOfComponents, options, eventNamesForBox, variableNamesForBox);
        synthesizerDialog.show();

        if (!options.getDialogOK())
        {
            return;
        }

        ExtendedAutomata exAutomata = options.getOptimization()?new ExtendedAutomata(module, (int)options.getGlobalClockDomain()):new ExtendedAutomata(module);


    //        ReduceBDDvars rBDDv = new ReduceBDDvars(exAutomata.getExtendedAutomataList().get(0));
    //        rBDDv.computeOptimalPaths();

        final BDDExtendedSynthesizer bddSynthesizer = new BDDExtendedSynthesizer(exAutomata,options);
        if(logger.isDebugEnabled())
            logger.info("Number of used BDD variables: "+bddSynthesizer.bddAutomata.getNumberOfUsedBDDVariables());
/*
        //Create a naive PCG graph
        int[][] weight = new int[exAutomata.size()][exAutomata.size()];
        int[] efaDegree = new int[exAutomata.size()];
        boolean[][] weightComputed = new boolean[exAutomata.size()][exAutomata.size()];
        for(ExtendedAutomaton efa1:exAutomata)
        {
            efaDegree[exAutomata.getExAutomatonIndex(efa1)] = 0;
            for(ExtendedAutomaton efa2:exAutomata)
            {
                boolean weightComp = false;
                if(exAutomata.getExAutomatonIndex(efa1) == exAutomata.getExAutomatonIndex(efa2))
                        weightComp = true;

                weightComputed[exAutomata.getExAutomatonIndex(efa1)][exAutomata.getExAutomatonIndex(efa2)] = weightComp;
            }
        }

        for(ExtendedAutomaton efa1:exAutomata)
        {
            for(ExtendedAutomaton efa2:exAutomata)
            {
                int index1 = exAutomata.getExAutomatonIndex(efa1);
                int index2 = exAutomata.getExAutomatonIndex(efa2);
                if(!weightComputed[index1][index2])
                {
                    ArrayList<EventDeclProxy> commonEvents = new ArrayList<EventDeclProxy>(efa1.getAlphabet());
                    commonEvents.retainAll(efa2.getAlphabet());
                    int commonVarsSize = 0;
                    for(EventDeclProxy event:commonEvents)
                    {
                        if(efa1.getGuardVariables(event) != null && efa2.getGuardVariables(event) != null)
                        {
                            HashSet<VariableComponentProxy> gVars = new HashSet<VariableComponentProxy>(efa1.getGuardVariables(event));
                            gVars.retainAll(efa2.getGuardVariables(event));
                            commonVarsSize += (gVars.size());
                        }
                    }
                    int w = commonEvents.size()+commonVarsSize;
                    weight[index1][index2] = w;
                    efaDegree[index1] += w;
                    efaDegree[index2] += w;
                    weightComputed[index1][index2] = true;
                    weightComputed[index2][index1] = true;
                }
            }
        }
*/
//        System.err.println("SIZE: "+exAutomata.nbrOfEFAsVars);
//        System.err.println("Number of controllable events: "+exAutomata.controllableAlphabet.size());
//        System.err.println("Number of theoretical reachable states: "+((double)exAutomata.theoNbrOfReachableStates));

//        double LD = (double)max(efaDegree)/(double)exAutomata.nbrOfEFAsVars;
//        System.err.println("LD: "+ LD);

        
//        int i = 1;
//        final BDD bdd =  bddSynthesizer.bddAutomata.getMarkedLocations().and(bddSynthesizer.bddAutomata.getMarkedValuations()).and(bddSynthesizer.bddAutomata.getReachableStates());            
//        final TIntArrayList valuations = bddSynthesizer.bddAutomata.BDD2valuations(bdd, options.getOptVaribale());
        
//        do
//        {
//            System.err.println("cheking "+valuations.get(i)+"...");
//            bddSynthesizer.bddAutomata.minValueOfVar = valuations.get(i++);
//            bddSynthesizer.bddAutomata.minValueOfVarBDD = bdd.and(
//                    bddSynthesizer.bddAutomata.getManager().getFactory().buildCube((int) bddSynthesizer.bddAutomata.minValueOfVar, bddSynthesizer.bddAutomata.getSourceVariableDomain(
//                    bddSynthesizer.bddAutomata.getIndexMap().getVariableIndexByName(options.getMinVaribale())).vars()));              
//            
//            bddSynthesizer.bddAutomata.nonblockingControllableStatesBDD = null;
//            bddSynthesizer.bddAutomata.nbrOfNonblockingControllableStates = -1;            
//            ((BDDMonolithicEdges) bddSynthesizer.bddAutomata.getBDDEdges()).forcibleEdgesForwardBDD = ((BDDMonolithicEdges) bddSynthesizer.bddAutomata.getBDDEdges()).forcibleEdgesForwardBDDCopy.id();          
//                
//            bddSynthesizer.bddAutomata.markedStatesBDD = bddSynthesizer.bddAutomata.getMarkedLocations().and(bddSynthesizer.bddAutomata.getMarkedValuations()).and(bddSynthesizer.bddAutomata.minValueOfVarBDD);
//                
//            bddSynthesizer.synthesize(options);
//            System.err.println("number of supervisor states: "+bddSynthesizer.nbrOfStates());
//        }while(bddSynthesizer.nbrOfStates() <= 0);                                                       
                

        bddSynthesizer.synthesize(options);
        
        
        logger.info("Synthesis completed after "+bddSynthesizer.getSynthesisTimer().toString()+".");
        logger.info("Number of reachable states: "+bddSynthesizer.bddAutomata.numberOfReachableStates());
        if(options.getOptimization())
            logger.info("The minimum time to reach a marked state from the initial state: "+bddSynthesizer.bddAutomata.getOptimalTime()+".");       
        
        if(!options.getOptVaribale().isEmpty())
        {
            logger.info("The minimum value of variable "+ options.getOptVaribale()+" among the reachable marked states is: "+bddSynthesizer.bddAutomata.getMinValueOfVar()+"."); 
        }
        
        logger.info("The "+options.getSynthesisType().toString()+" supervisor consists of "+(double)bddSynthesizer.nbrOfStates()+" states.");

        List<VariableComponentProxy> pars = bddSynthesizer.bddAutomata.getExtendedAutomata().getParameters();
        if(!pars.isEmpty())
            logger.info("The feasible values for the parameters are:");
        for(VariableComponentProxy variable:pars)
        {
            logger.info(bddSynthesizer.getFeasibleValues(variable.getName()));
        }


//        System.err.println("Number of nodes in the safe BDD: "+bddSynthesizer.getResult().nodeCount());

        if(bddSynthesizer.nbrOfStates()>0 && (options.getSaveInFile() || options.getSaveIDDInFile() ||
                options.getPrintGuard() || options.getAddGuards()))
        {
            boolean guardsGenerated = false;
            HashMap<String,BDDExtendedGuardGenerator> event2guard = null;
/*            //
            int minGuardSize = Integer.MAX_VALUE;
            int maxGuardSize = Integer.MIN_VALUE;
            double aveGuardSize = 0;
            for(final String event:event2guard.keySet())
            {
                final int guardSize = event2guard.get(event).getNbrOfTerms();
                if(guardSize < minGuardSize)
                    minGuardSize = guardSize;
                if(guardSize > maxGuardSize)
                    maxGuardSize = guardSize;
                aveGuardSize += guardSize;

            }
            System.err.println("Min guard: "+minGuardSize);
            System.err.println("Max guard: "+maxGuardSize);
            System.err.println("Average guard: "+aveGuardSize/exAutomata.controllableAlphabet.size());
*/

            if(options.getSaveInFile() || options.getSaveIDDInFile())
            {
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                chooser.setAcceptAllFileFilterUsed(false);
                final int returnVal = chooser.showOpenDialog(ide.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    final String path = chooser.getSelectedFile().getAbsolutePath();
                    Config.FILE_SAVE_PATH.set(path);

                    if(!guardsGenerated)
                    {
                        bddSynthesizer.generateGuard(eventNames, options);
                        event2guard = bddSynthesizer.getEventGuardMap();
                        guardsGenerated = true;
                    }

                    if(options.getSaveInFile())
                    {
                        String name = module.getName();
                        if(name.isEmpty())
                            name = "guard_event_list";

                        final File file = new File(path+"/"+name+".xls");
                        try
                        {
                            final FileWriter fstream = new FileWriter(file);
                            final BufferedWriter out = new BufferedWriter(fstream);
                            out.write("Event" + "\t" + "Guard size" +"\t"+"# Complement Heuristic was applied"+"\t"+"# Independent Heuristic was applied"+"\t"+"Guard expression");
                            out.newLine();
                            out.newLine();
                            for(final String event:event2guard.keySet())
                            {
                                final BDDExtendedGuardGenerator bddegg = event2guard.get(event);
                                String guard = bddegg.getGuard();
                                if(guard.equals(bddegg.FALSE))
                                    guard = "False";
                                if(guard.equals(bddegg.TRUE))
                                    guard = "True";
                                out.write(event + "\t" + bddegg.getNbrOfTerms() +"\t"+ bddegg.getNbrOfCompHeuris()+"\t"+bddegg.getNbrOfIndpHeuris()+"\t"+guard);
                                out.newLine();
                                out.newLine();
                            }
                            out.close();
                        }

                        catch (final Exception e)
                        {
                           logger.error("Could not save the event-guard pairs in the file: " + e.getMessage());
                        }
                    }
                }
            }

            if(options.getPrintGuard())
            {
                if(!guardsGenerated)
                {
                    bddSynthesizer.generateGuard(eventNames, options);
                    event2guard = bddSynthesizer.getEventGuardMap();
                    guardsGenerated = true;
                }
                for(final String event:event2guard.keySet())
                {
                    final BDDExtendedGuardGenerator bddgg = event2guard.get(event);
                    String TF =bddgg.getGuard();
                    if(TF.equals(bddgg.TRUE))
                        TF = "This event is always ENABLED by the supervisor.";
                    else if(TF.equals(bddgg.FALSE))
                        TF = "This event is always DISABLED by the supervisor"+(bddgg.isEventBlocked()?" (Blocked in the synchronization process).":".");

                    logger.info(bddgg.getBestStateSet()+" guard for event "+event+": "+TF);

                    logger.info("Number of terms in the expression: "+bddgg.getNbrOfTerms());
                }
                logger.info("The guards were generated in "+bddSynthesizer.getGuardTimer().toString()+".");
            }

            if(options.getAddGuards())
            {
                if(!guardsGenerated)
                {
                    bddSynthesizer.generateGuard(eventNames, options);
                    event2guard = bddSynthesizer.getEventGuardMap();
                    guardsGenerated = true;
                }
                bddSynthesizer.addGuardsToAutomata(module);
            }
        }
        else if(options.getAddGuards() || options.getSaveIDDInFile() || options.getSaveInFile() || options.getPrintGuard())
            logger.info("No guards can be generated when there does not exist any supervisor.");

        bddSynthesizer.done();
//            upperboundOfTime ++;
//        }

    }

}
