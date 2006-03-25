package org.supremica.gui.ide.actions;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import org.supremica.gui.ide.ModuleContainer;
import org.supremica.gui.ide.IDE;

/**
 * A new action
 */
public class ActionTemplate
	extends IDEAction
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ActionTemplate(IDEActionInterface ide)
	{
		super(ide);

		putValue(Action.NAME, "Action name");
		putValue(Action.SHORT_DESCRIPTION, "Action description");	
		putValue(Action.MNEMONIC_KEY, new Integer(KeyEvent.VK_A));
		putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));
		putValue(Action.SMALL_ICON, new ImageIcon(IDE.class.getResource("/toolbarButtonGraphics/general/Icon.gif")));
	}

	public void actionPerformed(ActionEvent e)
	{
		doAction();
	}

	/**
	 * The code that is run when the action is invoked.
	 */
	public void doAction()
	{
	}
}
