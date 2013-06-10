package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.Action;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * A new action
 */
public class AnalyzerViewAutomatonAction
    extends IDEAction
{
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerViewAutomatonAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Automaton");
        putValue(Action.SHORT_DESCRIPTION, "View Automaton");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/automaton16.gif")));
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        // gui.debug("ActionMan to the rescue!");
        // Retrieve the selected automata and make a sanity check
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, true, false, false, false))
        {
            return;
        }

        for (final Automaton currAutomaton : selectedAutomata)
        {
            try
            {
                ide.getActiveDocumentContainer().getAnalyzerPanel().getVisualProject().getAutomatonViewer(currAutomaton.getName());
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomatonViewer. Automaton: " + currAutomaton, ex);
                logger.debug(ex.getStackTrace());

                return;
            }
        }
    }
}
