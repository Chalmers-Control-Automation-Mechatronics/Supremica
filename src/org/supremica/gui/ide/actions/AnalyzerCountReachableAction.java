package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * A new action
 */
public class AnalyzerCountReachableAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerCountReachableAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);
         
        putValue(Action.NAME, "Count reachable states");
        putValue(Action.SHORT_DESCRIPTION, "Count reachable states using BDD methods");
        //putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        //putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
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
        org.supremica.util.BDD.test.DeveloperTest.DoReachability(ide.getActiveDocumentContainer().getAnalyzerPanel().getSelectedAutomata());
    }
}
