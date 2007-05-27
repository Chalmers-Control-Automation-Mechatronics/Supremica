package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.supremica.automata.Automata;
import org.supremica.automata.Automaton;
import org.supremica.gui.automataExplorer.AutomataExplorer;
import org.supremica.gui.AutomatonExplorer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;


/**
 * A new action
 */
public class AnalyzerExploreStatesAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerExploreStatesAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Explore States");
        putValue(Action.SHORT_DESCRIPTION, "Explore States");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/media/Play16.gif")));
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
       // Retrieve the selected automata and make a sanity check
        Automata selectedAutomata = ide.getSelectedAutomata();

        // Sanitycheck
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, true, false, false, true))
        {
            return;
        }

        // How many selected?
        if (selectedAutomata.size() == 1)
        {
            // One automaton selected

            // Get automaton
            Automaton theAutomaton = selectedAutomata.getFirstAutomaton();
            String currAutomatonName = theAutomaton.getName();

            // Get AutomatonExplorer
            try
            {
                AutomatonExplorer explorer = ide.getActiveModuleContainer().getVisualProject().getAutomatonExplorer(currAutomatonName);
            }
            catch (Exception ex)
            {
                logger.error("Exception in AutomatonExplorer. Automaton: " + theAutomaton.getName(), ex);
                logger.debug(ex.getStackTrace());
            }
        }
        else
        {
            // Many automata selected

            // The AutomataExplorer can not take care of nondeterministic processes...
            if (!selectedAutomata.isDeterministic())
            {
                logger.error("The current project is nondeterministic. " +
                    "Exploration of nondeterministic automata " +
                    "is currently not supported.");
            }

            // Get AutomataExplorer
            try
            {
                JOptionPane.showMessageDialog(ide.getFrame(), "The automata explorer only works in the \"forward\" direction!", "Alert", JOptionPane.INFORMATION_MESSAGE);

                AutomataExplorer explorer = new AutomataExplorer(selectedAutomata);

                explorer.setVisible(true);
                explorer.initialize();
            }
            catch (Exception ex)
            {
                logger.error("Exception in AutomataExplorer.", ex);
                logger.debug(ex.getStackTrace());
            }
        }

    }
}
