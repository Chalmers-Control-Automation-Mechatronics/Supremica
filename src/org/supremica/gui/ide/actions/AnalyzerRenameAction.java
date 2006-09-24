package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.automataExplorer.AutomataExplorer;
import org.supremica.gui.AutomatonExplorer;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;


/**
 * A new action
 */
public class AnalyzerRenameAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerRenameAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Rename");
        putValue(Action.SHORT_DESCRIPTION, "Rename");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
//        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
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
        Automata selectedAutomata = ide.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1))
        {
            return;
        }


        for(Automaton currAutomaton : selectedAutomata)
        {
            String currAutomatonName = currAutomaton.getName();

            try
            {
                String newName = ide.getIDE().getNewAutomatonName("Enter a new name for " + currAutomatonName, currAutomatonName);

                if (newName != null)
                {
                    ide.getActiveModuleContainer().getVisualProject().renameAutomaton(currAutomaton, newName);
                }
            }
            catch (Exception ex)
            {
                logger.error("Exception while renaming the automaton " + currAutomatonName, ex);
                logger.debug(ex.getStackTrace());
            }
        }
    }
}
