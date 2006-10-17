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
import org.supremica.gui.VisualProject;
import org.supremica.gui.FindStates;
import org.supremica.gui.AutomatonExplorer;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

/**
 * A new action
 */
public class AnalyzerFindStatesAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerFindStatesAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Find States");
        putValue(Action.SHORT_DESCRIPTION, "Find States");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Find16.gif")));
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
		VisualProject theProject = ide.getActiveModuleContainer().getVisualProject();
		Automata selectedAutomata = ide.getSelectedAutomata();
		// gui.info("Nbr of selected automata: " + selectedAutomata.size());
		FindStates find_states = new FindStates();

		try
		{
			find_states.execute(theProject, selectedAutomata);
		}
		catch (Exception ex)
		{
			logger.error("Exception in Find States. ", ex);
			logger.debug(ex.getStackTrace());
		}
    }
}
