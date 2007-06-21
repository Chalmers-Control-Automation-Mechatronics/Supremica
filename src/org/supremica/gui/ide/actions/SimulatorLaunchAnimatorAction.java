package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.VisualProject;
import org.supremica.gui.animators.scenebeans.Animator;

/**
 * Launch animator action.
 */
public class SimulatorLaunchAnimatorAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public SimulatorLaunchAnimatorAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Launch Animator");
        putValue(Action.SHORT_DESCRIPTION, "Opens Animator Window");
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
        try
        {
            VisualProject currProject = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getVisualProject();

            if (!currProject.hasAnimation())
            {
                ide.info("No animation present.");

                return;
            }

            Animator animator = currProject.getAnimator();
        }
        catch (Exception ex)
        {
            ide.error("Exception while getting Animator.", ex);
        }
    }
}
