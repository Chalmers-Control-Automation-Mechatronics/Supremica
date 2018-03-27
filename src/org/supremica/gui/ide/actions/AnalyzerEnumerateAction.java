/*********************** AnalyzerEnumerateAction.java *************************
 * Action to enumerate the states, that is to name them q0, q1, q2...
 * q0 is always the initial state.
 */
package org.supremica.gui.ide.actions;

import javax.swing.Action;

import org.supremica.automata.Automata;
import org.supremica.automata.algorithms.EnumerateStates;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
/**
 *
 * @author Fabian
 */
public class AnalyzerEnumerateAction extends IDEAction
{
    private final Logger logger = LogManager.getLogger(); // LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    public AnalyzerEnumerateAction(final java.util.List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Enumerate states");
        putValue(Action.SHORT_DESCRIPTION, "Rename states as q0, q1, etc");
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/supremica/purge16.gif")));
	}
	 
    @Override
    public void actionPerformed(final java.awt.event.ActionEvent e)
    {
        doAction();
    }

    /**
     * The code that is run when the action is invoked.
     */
    @Override
    public void doAction()
    {
        final Automata selectedAutomata = ide.getActiveDocumentContainer().getSupremicaAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }

		// Hard coded prefix here, shoudl maybe have some setting for this, but not now...
		final EnumerateStates enumerateStates = new EnumerateStates(selectedAutomata, "q");
		
        try
		{
			enumerateStates.execute();
        }
        catch (final Exception ex)
        {
            logger.error("Exception in EnumerateStates", ex);
            logger.debug(ex.getStackTrace());
        }
    }	
}
