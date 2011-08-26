//# -*- tab-width: 4  indent-tabs-mode: nil  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Supremica/Waters IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   EditorReachabilityGraphAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import net.sourceforge.waters.subject.module.ModuleSubject;

import org.supremica.automata.ExtendedAutomata;
import org.supremica.automata.ExtendedAutomaton;
import org.supremica.automata.algorithms.EFAMonlithicReachability;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


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

            final ExtendedAutomaton efa = exAutomata.iterator().next();
            final EFAMonlithicReachability efaMR = new EFAMonlithicReachability(efa.getComponent(), exAutomata.getVars(),efa.getAlphabet());
            exAutomata.addAutomaton(new ExtendedAutomaton(exAutomata, efaMR.createEFA()));

            logger.info("Reachability graph computed.");

    }

}
