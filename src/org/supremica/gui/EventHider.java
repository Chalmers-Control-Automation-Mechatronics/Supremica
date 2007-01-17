
/*********************** LanguageRestrictor.java ******************/

// This is the GUI interface for the restriction facility
// Collects the alphabet to consider as epsilons and calls Determinizer
// Does not alter the original automata, but generates new automata
// named as "restr(automaton)"
// Note, this is the first command to implement the Swing.Action interface
// Makes things so much simpler!
// TODO:
// Need a TreeSelectionListener that selects the event and all its children
// when it or one of its children is selected (or only allow level 1 nodes to be selected
package org.supremica.gui;

import java.awt.MenuBar;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.gui.ide.IDEReportInterface;

/**
 * Lets the user choose events to be hidden.
 */
class EventHiderDialog
    extends LanguageRestrictorDialog
{
    // Gaaaah! LanguageRestrictorDialog has lots of stuff in it that should be somewhere else.
    // There should be a "EventSelectorDialog" or something that should be used for the selection.
    // Other classes may want to do this, you know. I didn't have the energy to do all that so this
    // is an ugly fix using the LanguageRestrictorDialog.

    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.createLogger(EventHiderDialog.class);
    
    private IDEReportInterface ide;
    
    private boolean preserveControllability = false;
    
    public EventHiderDialog(IDEReportInterface ide, Automata automata, Alphabet globalAlphabet)
    {
        super(automata, globalAlphabet);
        super.setTitle("Event hider");
        
        super.okButton = new OkButton();
        
        // Add menu for choosing whether to preserve controllabilty or not.
        JMenuBar menuBar = super.getJMenuBar();
                
        // Restrict
        JMenu controllabilityMenu = new JMenu("Controllability");        
        controllabilityMenu.setMnemonic(KeyEvent.VK_C);        
        // Ignore controllability
        JRadioButtonMenuItem preserveMenuIgnore = new JRadioButtonMenuItem("Ignore controllability", !preserveControllability);        
        preserveMenuIgnore.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                preserveControllability = false;
            }
        });
        controllabilityMenu.add(preserveMenuIgnore);
        // Preserve controllability
        JRadioButtonMenuItem preserveMenuPreserve = new JRadioButtonMenuItem("Preserve controllability", preserveControllability);        
        preserveMenuPreserve.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                preserveControllability = true;
            }
        });
        controllabilityMenu.add(preserveMenuPreserve);
        // Group the radio buttons
        ButtonGroup preserveGroup = new ButtonGroup();
        preserveGroup.add(preserveMenuIgnore);
        preserveGroup.add(preserveMenuPreserve);
        // Add to menu
        menuBar.add(controllabilityMenu);
        
        super.pack();
        this.ide = ide;
    }
    
    private class OkButton
        extends JButton
    {
        private static final long serialVersionUID = 1L;
        
        public OkButton()
        {
            super("Ok");
            
            setToolTipText("Do the hiding");
            addActionListener(new ActionListener()
            {
                public void actionPerformed(ActionEvent e)
                {
                    System.err.println("Tryck inte s� h�rt!");
                    doRestrict();
                }
            });
        }
    }
    
    protected void doRestrict()
    {
        // The set of new automata, based on the selected automata
        Automata newAutomata = new Automata();
        
        // Get the events selected by the user (may be for keeping or for hiding)
        Alphabet alpha = restrictEvents.getAlphabet();
        
        // Loop over the selected automata
        Iterator autit = automata.iterator();
        while (autit.hasNext())
        {
            Automaton automaton = (Automaton) autit.next();
            Automaton newAutomaton = new Automaton(automaton);
            newAutomaton.setName(null);
            
            // Find out which events should be hidden
            if (!restrictEvents.toErase())
            {
                alpha = AlphabetHelpers.minus(automaton.getAlphabet(), alpha);
            }
            
            // Do the hiding (preserve controllability!)
            newAutomaton.hide(alpha, preserveControllability);
            
            // Set appropriate comment
            newAutomaton.setComment(automaton.getName() + "//" +
                AlphabetHelpers.intersect(automaton.getAlphabet(), alpha));
            
            // Add automaton
            newAutomata.addAutomaton(newAutomaton);
        }
        
        // Shut the window!!
        shutWindow();
        
        try
        {
            ide.addAutomata(newAutomata);
        }
        catch (Exception ex)
        {
            logger.debug("EventHiderDialog::doRestrict() -- ", ex);
            logger.debug(ex.getStackTrace());
        }
    }
}

public class EventHider
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;
    
    private static Logger logger = LoggerFactory.createLogger(EventHider.class);
    private IDEReportInterface ide;
    
    public EventHider(IDEReportInterface ide)
    {
        putValue(NAME, "Event hider");
        putValue(SHORT_DESCRIPTION, "Stop observing selected events");
        this.ide = ide;
    }
    
    public void actionPerformed(ActionEvent event)
    {
        // Get the selected automata
        Automata automata = ActionMan.getGui().getSelectedAutomata();
        
        // Throw up the dialog, let the user select the alphabet
        EventHiderDialog dlg = new EventHiderDialog(ide, automata, ActionMan.getGui().getUnselectedAutomata().getUnionAlphabet());
    }
    
    public void doAction(Automata theAutomata, Alphabet othersAlphabet)
    {
        EventHiderDialog dlg = new EventHiderDialog(ide, theAutomata, othersAlphabet);        
    }
}
