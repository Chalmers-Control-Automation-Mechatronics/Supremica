
package org.supremica.gui.ide.actions;

import javax.swing.AbstractButton;
import javax.swing.AbstractAction;

public abstract class IDEAction
	extends AbstractAction
{
	private AbstractButton theButton = null;
	protected IDEActionInterface ide;

	public IDEAction(IDEActionInterface ide)
	{
		this.ide = ide;
	}

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
