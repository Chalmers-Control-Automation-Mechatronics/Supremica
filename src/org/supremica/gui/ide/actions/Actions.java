
package org.supremica.gui.ide.actions;

import org.supremica.gui.ide.IDE;

public class Actions
{
	private IDE ide;

	public OpenAction openAction;
	public SaveAction saveAction;

	public Actions(IDE ide)
	{
		this.ide = ide;

		openAction = new OpenAction(ide);
		saveAction = new SaveAction(ide);

	}
}
