package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class OpenAction
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public OpenAction()
    {
        super();
        
        putValue(NAME, "Open...");
        putValue(SHORT_DESCRIPTION, "Open a new project");
        putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_O));
        putValue(SMALL_ICON,
            new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Open16.gif")));
    }
    
    public void actionPerformed(ActionEvent e)
    {
        ActionMan.fileOpen(ActionMan.getGui());
    }
}
