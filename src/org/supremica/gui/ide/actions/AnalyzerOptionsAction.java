package org.supremica.gui.ide.actions;

import javax.swing.Action;
import java.awt.event.ActionEvent;
import org.supremica.gui.PropertiesDialog;
import java.util.List;
import javax.swing.ImageIcon;
import org.supremica.gui.ide.IDE;

public class AnalyzerOptionsAction
    extends IDEAction
{
    private static final long serialVersionUID = 1L;

    /**
     * Displays dialog with all kinds of options (not only analyzer options...).
     */
    public AnalyzerOptionsAction(List<IDEAction> actionList)
    {
        super(actionList);

        putValue(Action.NAME, "Options...");
        putValue(Action.SHORT_DESCRIPTION, "Options");
        putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Preferences16.gif")));
    }

    public void actionPerformed(ActionEvent e)
    {
        doAction();
    }

    public void doAction()
    {
        PropertiesDialog dialog = new PropertiesDialog(ide.getFrame());
        dialog.setVisible(true);
    }
}
