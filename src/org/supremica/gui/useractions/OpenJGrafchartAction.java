package org.supremica.gui.useractions;

import java.awt.event.*;
import javax.swing.*;
import org.supremica.gui.Supremica;
import org.supremica.gui.ActionMan;

public class OpenJGrafchartAction
	extends AbstractAction
{
	public OpenJGrafchartAction()
	{
		super("Open JGrafchart", new ImageIcon(Supremica.class.getResource("/toolbarButtonGraphics/general/Edit16.gif")));

		putValue(SHORT_DESCRIPTION, "Open the JGrafchart Editor");
	}

	public void actionPerformed(ActionEvent e)
	{
		ActionMan.openJGrafchartEditor(ActionMan.getGui());
	}
}
