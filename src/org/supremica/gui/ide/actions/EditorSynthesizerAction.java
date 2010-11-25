//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerSynthesizerAction
//###########################################################################
//# $Id: AnalyzerSynthesizerAction.java 4750 2009-09-01 00:33:54Z robi $
//###########################################################################

package org.supremica.gui.ide.actions;

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

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
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

        putValue(Action.NAME, "Seamless Synthesize...");
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
        eventNames.add("Generate guards for ALL controllable events");

        for(final EventDeclSubject sigmaS:  module.getEventDeclListModifiable())
        {
            if(sigmaS.getKind() == EventKind.CONTROLLABLE)// || sigmaS.getKind() == EventKind.UNCONTROLLABLE)
            {
                eventNames.add(sigmaS.getName());
            }
        }

        final EditorSynthesizerDialog synthesizerDialog = new EditorSynthesizerDialog(ide.getFrame(), nbrOfComponents, options, eventNames);
        synthesizerDialog.show();

        if (!options.getDialogOK())
        {
            return;
        }

        final ExtendedAutomata exAutomata = new ExtendedAutomata(module);
        final BDDExtendedSynthesizer bddSynthesizer = new BDDExtendedSynthesizer(exAutomata);
        bddSynthesizer.synthesize(options);

        logger.info("Synthesis completed after "+bddSynthesizer.getSynthesisTimer().toString()+".");
        logger.info("The "+options.getSynthesisType().toString()+" supervisor consists of "+bddSynthesizer.nbrOfStates()+" states.");

        if(bddSynthesizer.nbrOfStates()>0)
        {
            boolean isGuardsComputed = false;

            if(options.getSaveInFile() || options.getSaveIDDInFile())
            {
                final JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                final int returnVal = chooser.showOpenDialog(ide.getFrame());
                if (returnVal == JFileChooser.APPROVE_OPTION)
                {
                    final String path = chooser.getSelectedFile().getAbsolutePath();
                    Config.FILE_SAVE_PATH.set(path);
                    if(!isGuardsComputed)
                    {
                        bddSynthesizer.generateGuard(eventNames, options);
                        isGuardsComputed = true;
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
                            final HashMap<String,BDDExtendedGuardGenerator> event2guard = bddSynthesizer.getEventGuardMap();
                            for(final String event:event2guard.keySet())
                            {
                                final BDDExtendedGuardGenerator bddegg = event2guard.get(event);
                                out.write(event + "\t" + bddegg.getNbrOfTerms() +"\t"+ bddegg.getNbrOfCompHeuris()+"\t"+bddegg.getNbrOfIndpHeuris()+"\t"+bddegg.getGuard());
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
                String expressionType = "";
                switch(options.getExpressionType())
                {
                    case(0):
                        expressionType = "Forbidden";
                    break;

                    case(1):
                        expressionType = "Allowed";
                    break;

                    case(2):
                        expressionType = "Adaptive";
                    break;
                }

                if(!isGuardsComputed)
                {
                    bddSynthesizer.generateGuard(eventNames, options);
                    isGuardsComputed = true;
                }
                final HashMap<String,BDDExtendedGuardGenerator> event2guard = bddSynthesizer.getEventGuardMap();
                for(final String event:event2guard.keySet())
                {
                    final BDDExtendedGuardGenerator bddgg = event2guard.get(event);
                    String TF =bddgg.getGuard();
                    if(TF.equals("True"))
                        TF = "This event is always ENABLED by the supervisor.";
                    else if(TF.equals("False"))
                        TF = "This event is always DISABLED by the supervisor.";

                    logger.info(expressionType+" guard for event "+event+": "+TF);

                    logger.info("Number of terms in the expression: "+bddgg.getNbrOfTerms());

                }
                logger.info("The guards were generated in "+bddSynthesizer.getGuardTimer().toString()+".");
            }

            if(options.getAddGuards())
            {
                if(!isGuardsComputed)
                {
                    bddSynthesizer.generateGuard(eventNames, options);
                    isGuardsComputed = true;
                }
                bddSynthesizer.addGuardsToAutomata(module);
            }
        }
        else if(options.getAddGuards() || options.getSaveIDDInFile() || options.getSaveInFile() || options.getPrintGuard())
            logger.info("No guards can be generated when there does not exist any supervisor.");

        bddSynthesizer.done();

    }

}
