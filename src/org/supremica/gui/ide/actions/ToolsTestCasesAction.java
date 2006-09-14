package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import net.sourceforge.waters.model.marshaller.ProductDESImporter;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
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
        TestCasesDialog testCasesDialog = new TestCasesDialog(ide.getIDE(), ide.getIDE());
        testCasesDialog.setVisible(true);
    }
}
