package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.automata.Automata;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class AnalyzerDeleteAllAutomataAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public AnalyzerDeleteAllAutomataAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Delete all");
        putValue(Action.SHORT_DESCRIPTION, "Delete all automata");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
        //putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Delete16.gif")));
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
        ide.getActiveDocumentContainer().getAnalyzerPanel().getAllAutomata().removeAutomata(new Automata(ide.getActiveDocumentContainer().getAnalyzerPanel().getAllAutomata()));
    }
}
