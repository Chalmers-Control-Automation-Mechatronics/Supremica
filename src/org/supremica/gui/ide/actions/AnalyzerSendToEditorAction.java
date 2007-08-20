//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters/Supremica IDE
//# PACKAGE: org.supremica.gui.ide.actions
//# CLASS:   AnalyzerSendToEditorAction
//###########################################################################
//# $Id: AnalyzerSendToEditorAction.java,v 1.11 2007-08-20 11:36:57 flordal Exp $
//###########################################################################

package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.ImageIcon;

import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.xsd.module.ScopeKind;
import net.sourceforge.waters.subject.base.AbstractSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.LabeledEvent;
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
    public AnalyzerSendToEditorAction(List<IDEAction> actionList)
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
    public void actionPerformed(ActionEvent e)
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
            Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();
            
            // Compile into Waters module
            ProductDESImporter importer = new ProductDESImporter(ModuleSubjectFactory.getInstance());
            for (Automaton aut : selectedAutomata)
            {
                SimpleComponentProxy component = importer.importComponent(aut);
                if (ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().componentNameAvailable(component.getName()))
                {
                    // Add to current module
                    try
                    {
                        ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().addComponent((AbstractSubject) component);
                        
                        // Add all (new) events to the module
                        ModuleSubject module = ide.getActiveDocumentContainer().getEditorPanel().getEditorPanelInterface().getModuleSubject();
                        boolean problem = false;
                        for (EventProxy event: aut.getEvents())
                        {
                            if (event.getName().contains("["))
                            {
                                problem = true;
                            }
                            else
                            {
                                if (!module.getEventDeclListModifiable().containsName(event.getName()))
                                {
                                    final EventProxy proxy = (EventProxy) event;
                                    final EventDeclSubject decl =
                                        new EventDeclSubject(proxy.getName(),
                                        proxy.getKind(),
                                        proxy.isObservable(),
                                        ScopeKind.LOCAL,
                                        null, null);
                                    module.getEventDeclListModifiable().add(decl);
                                }
                            }                        
                        }
                        /*
                        for (LabeledEvent event: aut.getAlphabet())
                        {
                            if (!event.getName().contains("["))
                            {
                                if (!module.getEventDeclListModifiable().containsName(event.getName()))
                                {
                                    final EventProxy proxy = (EventProxy) event;
                                    final EventDeclSubject decl =
                                        new EventDeclSubject(proxy.getName(),
                                        proxy.getKind(),
                                        proxy.isObservable(),
                                        ScopeKind.LOCAL,
                                        null, null);
                                    module.getEventDeclListModifiable().add(decl);
                                }
                            }
                            else
                            {
                                problem = true;
                            }
                        }
                         */
                        if (problem)
                            JOptionPane.showMessageDialog(ide.getFrame(), "There is a problem in the back-translation of parametrised events.", "Alert", JOptionPane.WARNING_MESSAGE);
                    }
                    catch (Exception ex)
                    {
                        ide.getIDE().error("Could not add " + aut + " to editor." + ex);
                    }
                }
                else
                {
                    JOptionPane.showMessageDialog(ide.getFrame(), "Component: " + component.getName() + " already exists in editor", "Duplicate Name", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
}