package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;
import org.supremica.automata.Automata;
import org.supremica.automata.IO.EncodingHelper;
import org.supremica.gui.AutomataHierarchyViewer;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;
import org.supremica.properties.Config;

/**
 * A new action
 */
public class AnalyzerViewModularStructureAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public AnalyzerViewModularStructureAction(List<IDEAction> actionList)
    {
        super(actionList);

        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);

        putValue(Action.NAME, "Modular Structure");
        putValue(Action.SHORT_DESCRIPTION, "View Modular Structure");
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

        // Sanity check
        if (!selectedAutomata.sanityCheck(ide.getIDE(), 2, false, false, true, false))
        {
            return;
        }

        // Warn if there are too many "states" i.e. automata
        int maxNbrOfStates = Config.DOT_MAX_NBR_OF_STATES.get();
        if (maxNbrOfStates < selectedAutomata.size())
        {
            String msg = "You have selected " + selectedAutomata.size() + " automata. It is not " +
                "recommended to display the modular structure for more than " + maxNbrOfStates +
                " automata.";
            msg = EncodingHelper.linebreakAdjust(msg);

            Object[] options = { "Continue", "Abort" };
            int response = JOptionPane.showOptionDialog(ide.getFrame(), msg, "Warning",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE,
                null, options, options[1]);
            if(response == JOptionPane.NO_OPTION)
            {
                return;
            }
        }

        // View
        try
        {
            AutomataHierarchyViewer viewer = new AutomataHierarchyViewer(selectedAutomata);

            viewer.setVisible(true);

            //viewer.setState(Frame.NORMAL);
        }
        catch (Exception ex)
        {
            logger.error("Exception in AutomataHierarchyViewer.", ex);
            logger.debug(ex.getStackTrace());

            return;
        }
    }
}
