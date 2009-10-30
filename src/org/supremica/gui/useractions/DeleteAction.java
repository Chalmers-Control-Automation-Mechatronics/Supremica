package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class DeleteAction
    extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public DeleteAction()
    {
        super("Delete...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Delete16.gif")));
        
        putValue(SHORT_DESCRIPTION, "Delete selected automata");
    }
    
    public void actionPerformed(ActionEvent e)
    {
        ActionMan.automataDelete_actionPerformed(ActionMan.getGui());
    }
}
