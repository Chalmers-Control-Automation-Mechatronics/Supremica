//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorMenu
//###########################################################################
//# $Id: EditorMenu.java,v 1.6 2005-02-22 04:12:36 knut Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

/**
 * <p>The EditorWindow menu.</p>
 *
 * <p>Creates a menu bar for use with the editor.</p>
 *
 * @author Gian Perrone
 */

public class EditorMenu
	extends JMenuBar
	implements ActionListener
{
	public final JMenuItem FileNewMenu;
	public final JMenuItem FileExitMenu;
	public final JMenuItem mToolsCreateEvent;
	public final JMenuItem ToolsOptionsMenu;
	public final JMenuItem editDeleteMenu;
	public final JMenuItem editCopyAsWMFMenu;
	EditorWindowInterface root;
	ControlledSurface C;
	JFileChooser fileChooser;

	public EditorMenu(ControlledSurface c, EditorWindowInterface r)
	{
		root = r;
		C = c;

		JMenu menu = new JMenu("File");

		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The File menu");
		this.add(menu);

		JMenuItem menuItem = new JMenuItem("Clear all", KeyEvent.VK_O);
		menuItem.addActionListener(this);
		FileNewMenu = menuItem;
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Page Setup...", KeyEvent.VK_G);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menuItem = new JMenuItem("Print...", KeyEvent.VK_P);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Close Window", KeyEvent.VK_X);
		menu.add(menuItem);
		menuItem.addActionListener(this);

		FileExitMenu = menuItem;
		menu = new JMenu("Edit");

		this.add(menu);

		menuItem = new JMenuItem("Undo");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Copy");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

/*
		menuItem = new JMenuItem("Copy as WMF");
		menu.add(menuItem);
		menuItem.addActionListener(this);
*/
		editCopyAsWMFMenu = menuItem;

		menuItem = new JMenuItem("Cut");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menuItem = new JMenuItem("Paste");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Delete");
		menu.add(menuItem);
		menuItem.addActionListener(this);

		editDeleteMenu = menuItem;
		menu = new JMenu("Tools");

		this.add(menu);

		menuItem = new JMenuItem("Select Tool");

		menu.add(menuItem);

		menuItem = new JMenuItem("Node Tool");
		menu.add(menuItem);

		menuItem = new JMenuItem("Edge Tool");
		menu.add(menuItem);

		menuItem = new JMenuItem("Initial Node Tool");
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Create Event");
		mToolsCreateEvent = menuItem;
		menu.add(menuItem);
		menuItem.addActionListener(this);
		menu.addSeparator();

		menuItem = new JMenuItem("Options...");
		menu.add(menuItem);

		ToolsOptionsMenu = menuItem;

		menuItem.addActionListener(this);

		menu = new JMenu("Help");
		this.add(menu);

		menuItem = new JMenuItem("About...");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		fileChooser = new JFileChooser();

		fileChooser.addChoosableFileFilter(new WmodFileFilter());
	}

	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == FileNewMenu)
		{
			C.clearAll();
		}

		if (e.getSource() == FileExitMenu)
		{
//			root.dispose();
		}

		if (e.getSource() == editDeleteMenu)
		{
			root.getControlledSurface().deleteSelected();
		}

		if (e.getSource() == mToolsCreateEvent)
		{
			root.getEventPane().createEvent();
		}

		if (e.getSource() == ToolsOptionsMenu)
		{
			root.getControlledSurface().setOptionsVisible(true);
		}
/*
		if (e.getSource() == editCopyAsWMFMenu)
		{
			root.copyAsWMFToClipboard();
		}
*/
	}
}
