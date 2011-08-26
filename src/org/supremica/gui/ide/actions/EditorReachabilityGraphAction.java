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
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.EFAMonlithicReachability;
import org.supremica.automata.algorithms.EditorSynthesizerOptions;
import org.supremica.automata.algorithms.Guard.BDDExtendedGuardGenerator;
import org.supremica.gui.EditorSynthesizerDialog;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.properties.Config;


public class EditorReachabilityGraphAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    public EditorReachabilityGraphAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(true);

        putValue(Action.NAME, "Compute reachabilty graph");
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SHORT_DESCRIPTION, "Compute the reachability graph for the specified EFA");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/synthesize16.gif")));
    }

    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    public static int max(final int[] t) {
        int maximum = t[0];
        for (int i=1; i<t.length; i++) {
            if (t[i] > maximum) {
                maximum = t[i];
            }
        }
        return maximum;
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


            final ExtendedAutomata exAutomata = new ExtendedAutomata(module);

    //        ReduceBDDvars rBDDv = new ReduceBDDvars(exAutomata.getExtendedAutomataList().get(0));
    //        rBDDv.computeOptimalPaths();

            ExtendedAutomaton efa = exAutomata.iterator().next();
            EFAMonlithicReachability efaMR = new EFAMonlithicReachability(efa.getComponent(), exAutomata.getVars(),efa.getAlphabet());
            exAutomata.addAutomaton(new ExtendedAutomaton(exAutomata, efaMR.createEFA()));

            logger.info("Reachability graph computed.");

    }

}
