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
import org.supremica.gui.AutomataViewer;
import org.supremica.gui.VisualProject;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

/**
 * A new action
 */
public class AnalyzerViewAlphabetAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerViewAlphabetAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Alphabet");
        putValue(Action.SHORT_DESCRIPTION, "View Alphabet");
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
       //logger.debug("ActionMan::automatonAlphabet_actionPerformed(gui)");
        Automata selectedAutomata = ide.getSelectedAutomata();

        if (!selectedAutomata.sanityCheck(ide.getIDE(), 1, false, false, true, false))
        {
            return;
        }

        // Why not simpy instantiate an AlphabetViewer with the given
        // automata object?? Use AutomataViewer instead!
        try
        {
            // AlphabetViewer alphabetviewer = new AlphabetViewer(selectedAutomata);
            AutomataViewer alphabetViewer = new AutomataViewer(selectedAutomata, true, false);

            alphabetViewer.setVisible(true);
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
