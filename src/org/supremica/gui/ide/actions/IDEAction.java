
package org.supremica.gui.ide.actions;

import org.supremica.gui.ide.IDE;
import javax.swing.AbstractButton;
import javax.swing.AbstractAction;

public abstract class IDEAction
	extends AbstractAction
{
	private AbstractButton theButton = null;

	public abstract void doAction();

	public void setButton(AbstractButton theButton)
	{
		this.theButton = theButton;
	}

	public AbstractButton getButton()
	{
		return theButton;
	}
}
