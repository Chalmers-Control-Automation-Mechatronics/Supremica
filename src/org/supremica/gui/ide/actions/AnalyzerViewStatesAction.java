package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import org.supremica.automata.Automata;
import org.supremica.gui.AutomataViewer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

/**
 * A new action
 */
public class AnalyzerViewStatesAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerViewStatesAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "States");
        putValue(Action.SHORT_DESCRIPTION, "View states");
        //        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/states16.gif")));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/States16.gif")));
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
        Automata selectedAutomata = ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, false))
        {
            return;
        }

        try
        {
            AutomataViewer statesViewer = new AutomataViewer(selectedAutomata, false, true);

            statesViewer.setVisible(true);
        }
        catch (Exception ex)
        {
            // logger.error("Exception in AlphabetViewer", ex);
            logger.error("Exception in AutomataViewer: " + ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }
}
