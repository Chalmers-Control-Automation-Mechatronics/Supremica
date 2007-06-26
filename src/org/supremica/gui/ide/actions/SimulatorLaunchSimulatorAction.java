package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.VisualProject;
import org.supremica.gui.simulator.SimulatorExecuter;

/**
 * Launch simulator action.
 */
public class SimulatorLaunchSimulatorAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    /**
     * Constructor.
     */
    public SimulatorLaunchSimulatorAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        setEditorActiveRequired(false);
        setAnalyzerActiveRequired(true);
        
        putValue(Action.NAME, "Launch Simulator");
        putValue(Action.SHORT_DESCRIPTION, "Opens Simulator Window");
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
        VisualProject project = ide.getIDE().getActiveDocumentContainer().getAnalyzerPanel().getVisualProject();
        project.clearSimulationData();

        // We can not simulate nondeterministic processes properly just yet...
        if (!project.isDeterministic())
        {
            ide.error("The current project is nondeterministic. Simulation of nondeterminism is currently not supported.");
        }

        try
        {
            if (!project.hasAnimation())
            {
                ide.info("No simulation present");

                return;
            }

            SimulatorExecuter simulator = project.getSimulator();

            if (simulator != null)
            {
                simulator.setVisible(true);
                simulator.initialize();
            }
        }
        catch (Exception ex)
        {
            ide.error("Exception in Simulator", ex);
            ide.debug(ex.getStackTrace().toString());
        }
    }
}
