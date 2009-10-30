package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.ide.AboutDialog;
import org.supremica.log.*;
import java.util.List;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

public class HelpAboutAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;
    
    @SuppressWarnings("unused")
	private static Logger logger = LoggerFactory.createLogger(HelpAboutAction.class);
    
    public HelpAboutAction(List<IDEAction> actionList)
    {
        super(actionList);
        
        putValue(Action.NAME, "About Supremica...");
        putValue(Action.SHORT_DESCRIPTION, "About Supremica");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/About16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }
    
    public void doAction()
    {
        AboutDialog aboutDialog = new AboutDialog(ide.getFrame());
        aboutDialog.setVisible(true);
    }
}

