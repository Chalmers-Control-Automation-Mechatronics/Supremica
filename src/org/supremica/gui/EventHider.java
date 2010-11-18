
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

import java.util.*;
import java.awt.event.*;
import javax.swing.*;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.Alphabet;
import org.supremica.automata.AlphabetHelpers;
import org.supremica.gui.ide.actions.IDEActionInterface;

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

    // LanguageRestictorDialog is no longer used by itself! It was used from old Supremica (ActionMan) before.
    // The function is not in the AutomataMinimizer.

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.createLogger(EventHiderDialog.class);

    private final IDEActionInterface ide;

    private boolean preserveControllability = false;

    public EventHiderDialog(final IDEActionInterface ide, final Automata automata, final Alphabet globalAlphabet)
    {
        super(automata, globalAlphabet);
        super.setTitle("Event hider");
        super.okButton = new OkButton();

        // Add menu for choosing whether to preserve controllability or not.
        final JMenuBar menuBar = super.getJMenuBar();

        // Restrict
        final JMenu controllabilityMenu = new JMenu("Controllability");
        controllabilityMenu.setMnemonic(KeyEvent.VK_C);
        // Ignore controllability
        final JRadioButtonMenuItem preserveMenuIgnore = new JRadioButtonMenuItem("Ignore controllability", !preserveControllability);
        preserveMenuIgnore.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                preserveControllability = false;
            }
        });
        controllabilityMenu.add(preserveMenuIgnore);
        // Preserve controllability
        final JRadioButtonMenuItem preserveMenuPreserve = new JRadioButtonMenuItem("Preserve controllability", preserveControllability);
        preserveMenuPreserve.addActionListener(new ActionListener()
        {
            public void actionPerformed(final ActionEvent e)
            {
                preserveControllability = true;
            }
        });
        controllabilityMenu.add(preserveMenuPreserve);
        // Group the radio buttons
        final ButtonGroup preserveGroup = new ButtonGroup();
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
                public void actionPerformed(final ActionEvent e)
                {
                    System.err.println("Tryck inte så hårt!");
                    doRestrict();
                }
            });
        }
    }

    protected void doRestrict()
    {
        // The set of new automata, based on the selected automata
        final Automata newAutomata = new Automata();

        // Get the events selected by the user (may be for keeping or for hiding)
        final Alphabet alpha = restrictEvents.getAlphabet();
        Alphabet toBeHidden;

        // Loop over the selected automata
        final Iterator<Automaton> autit = automata.iterator();
        while (autit.hasNext())
        {
            final Automaton automaton = autit.next();
            final Automaton newAutomaton = new Automaton(automaton);
            newAutomaton.setName(null);

            // Find out which events should be hidden
            if (restrictEvents.toErase())
            {
                // Take the chosen ones
                toBeHidden = alpha;
            }
            else
            {
                // Invert
                toBeHidden = AlphabetHelpers.minus(automaton.getAlphabet(), alpha);
            }

            // Do the hiding (preserve controllability!)
            newAutomaton.hide(toBeHidden, preserveControllability);

            // Set appropriate comment
            newAutomaton.setComment(automaton.getName() + "//" +
                AlphabetHelpers.intersect(automaton.getAlphabet(), toBeHidden));

            // Add automaton
            newAutomata.addAutomaton(newAutomaton);
        }

        // Shut the window!!
        shutWindow();

        try
        {
            ide.getActiveDocumentContainer().getAnalyzerPanel().addAutomata(newAutomata);
        }
        catch (final Exception ex)
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

    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(EventHider.class);
    private final IDEActionInterface ide;

    public EventHider(final IDEActionInterface ide)
    {
        putValue(NAME, "Event hider");
        putValue(SHORT_DESCRIPTION, "Stop observing selected events");
        this.ide = ide;
    }

    public void actionPerformed(final ActionEvent event)
    {
        // Get the selected automata
        final Automata automata = ActionMan.getGui().getSelectedAutomata();

        // Throw up the dialog, let the user select the alphabet
        new EventHiderDialog(ide, automata, ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getUnselectedAutomata().getUnionAlphabet());
    }

    public void doAction(final Automata theAutomata, final Alphabet othersAlphabet)
    {
        new EventHiderDialog(ide, theAutomata, othersAlphabet);
    }
}
