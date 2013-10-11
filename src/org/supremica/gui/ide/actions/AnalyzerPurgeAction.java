package org.supremica.gui.ide.actions;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.Action;
import javax.swing.ImageIcon;

import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.automata.algorithms.AutomatonPurge;
import org.supremica.gui.ide.IDE;
import org.supremica.log.Logger;
import org.supremica.log.LoggerFactory;


/**
 * A new action
 */
public class AnalyzerPurgeAction
    extends IDEAction
{
    private final Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerPurgeAction(final List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Purge");
        putValue(Action.SHORT_DESCRIPTION, "Remove all forbidden states");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/supremica/purge16.gif")));
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
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

        for (final Automaton currAutomaton : selectedAutomata)
        {
            final AutomatonPurge automatonPurge = new AutomatonPurge(currAutomaton);

            try
            {
                automatonPurge.execute();
            }
            catch (final Exception ex)
            {
                logger.error("Exception in AutomataPurge. Automaton: " + currAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }
}
