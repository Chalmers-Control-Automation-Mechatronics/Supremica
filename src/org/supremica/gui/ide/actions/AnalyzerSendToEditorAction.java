//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerSendToEditorAction
//###########################################################################
//# $Id$
//###########################################################################

package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ide.IDE;


/**
 * The action used to send an automaton from the analyser to the editor.
 */
public class AnalyzerSendToEditorAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    //#######################################################################
    //# Constructor
    public AnalyzerSendToEditorAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "To editor");
        putValue(Action.SHORT_DESCRIPTION, "Send selected automata to editor");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_E));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/toEditor16.gif")));
    }

    //#######################################################################
    //# Invocation
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {
        if (ide.getActiveDocumentContainer().getEditorPanel() != null)
        {
            final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

            // Compile into Waters module
            final ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
            for (final Automaton aut : selectedAutomata) {
				try {
					final SimpleComponentProxy comp =
						importer.importComponent(aut);
					final IdentifierProxy ident = comp.getIdentifier();
					final ModuleContext context =
						ide.getActiveDocumentContainer().getEditorPanel().
						getModuleContext();
					context.checkNewComponentName(ident);
                    // Add to current module
					ide.getActiveDocumentContainer().getEditorPanel().
						addComponent((AbstractSubject) comp);
					// Add all (new) events to the module
					boolean problem = false;
					for (final EventProxy event: aut.getEvents()) {
						final String name = event.getName();
						if (name.contains(".")) {
							problem = true;
						}
					}
					if (problem) {
						JOptionPane.showMessageDialog(ide.getFrame(), "There is a problem in the back-translation of parametrised events.", "Alert", JOptionPane.WARNING_MESSAGE);
                    }
				} catch (final Exception ex) {
					ide.getIDE().error("Could not add " + aut + " to editor." + ex);
				}
			}
        }
    }

}