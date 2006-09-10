package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.IDE;

// Action related
import org.supremica.workbench.Workbench;
import org.supremica.gui.VisualProject;
import org.supremica.automata.Automata;
import java.util.List;

/**
 * A new action
 */
public class WorkbenchAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public WorkbenchAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "Workbench...");
        putValue(Action.SHORT_DESCRIPTION, "Launch workbench");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_W));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/waters/workbench16.gif")));
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
        try
        {
            VisualProject visualProject = ide.getIDE().getActiveModuleContainer().getVisualProject();
            Automata automata = ide.getIDE().getActiveModuleContainer().getAnalyzerPanel().getSelectedAutomata();
            
            Workbench workbench = new Workbench(visualProject, automata);
            workbench.setVisible(true);
        }
        catch (Exception ex)
        {
            System.err.println("Error starting Workbench.");
        }
    }
}
