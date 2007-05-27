package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.automata.Automata;
import org.supremica.gui.AlphabetViewer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;

/**
 * View alphabet action.
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
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_L));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/Alphabet16.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    /**
     * Opens an alphabet viewer window.
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
            AlphabetViewer alphabetViewer = new AlphabetViewer(selectedAutomata);
            //AutomataViewer alphabetViewer = new AutomataViewer(selectedAutomata, true, false);

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
