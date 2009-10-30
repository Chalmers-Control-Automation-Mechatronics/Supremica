
/******************* SaveAsAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class SaveAsAction
	extends AbstractAction
{
    private static final long serialVersionUID = 1L;

    public SaveAsAction()
	{
		super();

		putValue(NAME, "Save As...");
		putValue(SHORT_DESCRIPTION, "Save this project under a new name");
		putValue(MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		putValue(SMALL_ICON, 
				 new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSaveAs(ActionMan.getGui());
	}
}
