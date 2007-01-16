package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.automata.Alphabet;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class AnalyzerStatisticsAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerStatisticsAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Statistics");
        putValue(Action.SHORT_DESCRIPTION, "Statistics");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Information16.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    public void doAction()
    {
        int nbrOfAutomata = ide.getActiveModuleContainer().getVisualProject().nbrOfAutomata();
        //gui.info("Number of automata: " + nbrOfAutomata);

        Automata selectedAutomata = ide.getSelectedAutomata();
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, true))
        {
            return;
        }

        ide.getIDE().info("Number of selected automata: " + selectedAutomata.size() + " (" + nbrOfAutomata + ")");
        ide.getIDE().info("Size of union alphabet: " + selectedAutomata.getUnionAlphabet().size());

        for (Automaton currAutomaton : selectedAutomata)
        {
            StringBuffer statusStr = new StringBuffer();

            statusStr.append("Status for automaton: " + currAutomaton.getName());

            statusStr.append("\n\tnumber of states: " + currAutomaton.nbrOfStates());
            statusStr.append("\n\tnumber of events: " + currAutomaton.nbrOfEvents());
            statusStr.append("\n\tnumber of transitions: " + currAutomaton.nbrOfTransitions());
            statusStr.append("\n\tnumber of accepting states: " + currAutomaton.nbrOfAcceptingStates());
            //statusStr.append("\n\tNumber of mutually accepting states: " + currAutomaton.nbrOfMutuallyAcceptingStates());
            statusStr.append("\n\tnumber of forbidden states: " + currAutomaton.nbrOfForbiddenStates());

            int acceptingAndForbiddenStates = currAutomaton.nbrOfAcceptingAndForbiddenStates();
            if (acceptingAndForbiddenStates > 0)
            {
                statusStr.append("\n\tnumber of accepting AND forbidden states: " + acceptingAndForbiddenStates);
            }

            if (currAutomaton.isDeterministic())
            {
                Alphabet redundantEvents = currAutomaton.getRedundantEvents();
                if (redundantEvents.nbrOfEvents() > 0)
                    statusStr.append("\n\talphabet of redundant events: " + redundantEvents);
                statusStr.append("\n\tthe automaton is deterministic");
            }

            if ((currAutomaton.getComment() != null) && !currAutomaton.getComment().equals(""))
            {
                statusStr.append("\n\tcomment: \"" + currAutomaton.getComment() + "\"");
            }

            // logger.info(statusStr.toString());
            ide.getIDE().info(statusStr.toString());
        }

        if (selectedAutomata.size() > 1)
        {
            double potentialNumberOfStates = 1.0;

            for (Automaton currAutomaton : selectedAutomata)
            {
                potentialNumberOfStates = potentialNumberOfStates * currAutomaton.nbrOfStates();
            }

            ide.getIDE().info("Number of potential states: " + new Double(potentialNumberOfStates).longValue());
        }
    }
}
