
package org.supremica.gui.ide;

import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import java.awt.event.KeyEvent;

public class IDEMenuBar
    extends JMenuBar
{
	private IDE ide;

    public IDEMenuBar(IDE ide)
    {
		this.ide = ide;

		initMenubar();
    }

    private void initMenubar()
    {
		JMenu menuFile = new JMenu("File");
		menuFile.setMnemonic(KeyEvent.VK_F);
		add(menuFile);

		menuFile.add(new JMenuItem(ide.getActions().openAction));
		menuFile.add(new JMenuItem(ide.getActions().saveAction));

	}
}
