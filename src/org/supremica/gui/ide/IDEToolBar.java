
package org.supremica.gui.ide;

import java.awt.Insets;
import javax.swing.*;

public class IDEToolBar
	extends JToolBar
{
	private static final Insets theInsets = new Insets(0, 0, 0, 0);

	private IDE ide;

	public IDEToolBar(IDE ide)
	{
		this.ide = ide;
		setRollover(true);

		add(ide.getActions().openAction);
		add(ide.getActions().saveAction);

	}


	public JButton add(Action theAction)
	{
		JButton theButton = super.add(theAction);
		theButton.setMargin(theInsets);

		return theButton;
	}
}
