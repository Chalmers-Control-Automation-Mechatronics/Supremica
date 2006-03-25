package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;
import java.util.List;

public class NewAction
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	public NewAction(List<IDEAction> actionList)
	{
		super(actionList);

		putValue(Action.NAME, "New");
		putValue(Action.SHORT_DESCRIPTION, "New module");
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_N));
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/New16.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	public void doAction()
	{
		ModuleContainer moduleContainer = ide.createNewModuleContainer();
		ide.add(moduleContainer);
		ide.setActive(moduleContainer);
	}
}
