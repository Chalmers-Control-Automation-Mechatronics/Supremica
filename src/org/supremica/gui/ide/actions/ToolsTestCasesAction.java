package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.automata.Project;
import org.supremica.gui.TestCasesDialog;

/**
 * A new action
 */
public class ToolsTestCasesAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public ToolsTestCasesAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(false);
        
        putValue(Action.NAME, "Test cases...");
        putValue(Action.SHORT_DESCRIPTION, "Generate test case");
        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_T));
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));
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
        TestCasesDialog testCasesDialog = new TestCasesDialog(ide.getIDE(), null);
        testCasesDialog.setVisible(true);
        
        // Get Supremica project
        Project project = testCasesDialog.getProject();
        
        ide.getIDE().info("Can not convert (old) Supremica project to Waters module.");
        
        // Compile into Waters module

        // Add as a new module (see OpenFileAction)
    }
}
