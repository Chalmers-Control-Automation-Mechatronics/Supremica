/******************* SaveAsAction.java *********************/
package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;

import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class SaveAsAction
	extends AbstractAction
{
	public SaveAsAction()
	{
		super("Save As...", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/SaveAs16.gif")));
		putValue(SHORT_DESCRIPTION, "Save this project");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.fileSaveAs(ActionMan.getGui());
	}

}
	