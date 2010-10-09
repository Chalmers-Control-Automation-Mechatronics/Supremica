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
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.xsd.base.EventKind;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.BDD.EFA.BDDExtendedSynthesizer;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.gui.EditorSynthesizerDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


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
        eventNames.add("Generate guards for ALL events");

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

        if(options.getGenerateGuard())
        {
            if(bddSynthesizer.nbrOfStates()>0)
            {
                bddSynthesizer.generateGuard(eventNames, options);
                logger.info("The guards were generated in "+bddSynthesizer.getGuardTimer().toString()+".");
                bddSynthesizer.addGuardsToAutomata(module);
            }
            else
                logger.info("No guards can be generated when there does not exist any supervisor.");
        }

        bddSynthesizer.done();

    }

}
