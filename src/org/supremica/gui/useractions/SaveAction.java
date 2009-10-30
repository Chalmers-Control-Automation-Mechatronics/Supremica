package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class SaveAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public SaveAction()
	{
		super();

		putValue(NAME, "Save...");
		putValue(SHORT_DESCRIPTION, "Save this project");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_S));
		putValue(SMALL_ICON, 
				 new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Save16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSave(ActionMan.getGui());
	}
}
