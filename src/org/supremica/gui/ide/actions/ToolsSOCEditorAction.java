package org.supremica.gui.ide.actions;

import java.util.List;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;
import org.supremica.log.*;
import org.supremica.external.processeditor.SOCFrame;

/**
 * A new action
 */
public class ToolsSOCEditorAction
    extends IDEAction
{
    private Logger logger = LoggerFactory.createLogger(IDE.class);

    private static final long serialVersionUID = 1L;

    /**
     * Constructor.
     */
    public ToolsSOCEditorAction(List<IDEAction> actionList)
    {
        super(actionList);

        putValue(Action.NAME, "SOC Editor");
        putValue(Action.SHORT_DESCRIPTION, "SOC Editor");
//        putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
//        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/icons/processeditor/icon.ico")));
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
    	SOCFrame soc = new SOCFrame();
    }
}
