//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorMenu
//###########################################################################
//# $Id: EditorMenu.java,v 1.20 2006-03-23 12:07:14 flordal Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

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
	implements ActionListener,
			   Observer
{
	public final JMenuItem fileNewMenu;
	public final JMenuItem printMenu;
	public final JMenuItem fileExitMenu;
	public final JMenuItem mEditUndo;
	public final JMenuItem mEditRedo;
	public final JMenuItem mToolsCreateEvent;
	public final JMenuItem toolsOptionsMenu;
	public final JMenuItem editDeleteMenu;
	public final JMenuItem editCopyAsWMFMenu;
	public final JMenuItem editExportPostscriptMenu;
	public final JMenuItem editExportPDFMenu;
	public final JMenuItem mEmbedder;
	EditorWindowInterface root;
	ControlledSurface surface;
	JFileChooser fileChooser;

	public EditorMenu(ControlledSurface c, EditorWindowInterface r)
	{
		root = r;
		surface = c;

		// New menu
		JMenu menu = new JMenu("File");
		menu.setMnemonic(KeyEvent.VK_F);
		menu.getAccessibleContext().setAccessibleDescription("The File menu");
		this.add(menu);

		JMenuItem menuItem = new JMenuItem("Clear all", KeyEvent.VK_O);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		fileNewMenu = menuItem;

		menu.addSeparator();

		menuItem = new JMenuItem("Export to Postscript");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		editExportPostscriptMenu = menuItem;

		menuItem = new JMenuItem("Export to PDF");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		editExportPDFMenu = menuItem;

		menu.addSeparator();

		menuItem = new JMenuItem("Print...", KeyEvent.VK_P);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		printMenu = menuItem;

		menuItem = new JMenuItem("Page Setup...", KeyEvent.VK_G);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Close Window", KeyEvent.VK_X);
		menuItem.addActionListener(this);
		menu.add(menuItem);
		fileExitMenu = menuItem;

		// Next menu
		menu = new JMenu("Edit");
		this.add(menu);

		menuItem = new JMenuItem("Undo");
		menuItem.setEnabled(root.getUndoInterface().canUndo());
		menuItem.setToolTipText("Not implemented yet");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		mEditUndo = menuItem;

		menuItem = new JMenuItem("Redo");
		menuItem.setEnabled(root.getUndoInterface().canRedo());
		menuItem.setToolTipText("Not implemented yet");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		mEditRedo = menuItem;

		menu.addSeparator();

		menuItem = new JMenuItem("Copy as WMF");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		editCopyAsWMFMenu = menuItem;

		menuItem = new JMenuItem("Copy");
		menuItem.setMnemonic(KeyEvent.VK_COPY);
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

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

		// Next menu
		menu = new JMenu("Tools");
		this.add(menu);

		menuItem = new JMenuItem("Select Tool");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");		
		menu.add(menuItem);

		menuItem = new JMenuItem("Node Tool");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menuItem = new JMenuItem("Edge Tool");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menuItem = new JMenuItem("Initial Node Tool");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Create Event");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		mToolsCreateEvent = menuItem;
		
		menuItem = new JMenuItem("Run Embedder");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		mEmbedder = menuItem;
		
		menu.addSeparator();

		menuItem = new JMenuItem("Options...");
		menuItem.addActionListener(this);
		menu.add(menuItem);
		toolsOptionsMenu = menuItem;

		// Next menu
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
		if (e.getSource() == fileNewMenu)
		{
			surface.clearAll();
		}

		if (e.getSource() == fileExitMenu)
		{
			// root.dispose();
		}

		if (e.getSource() == printMenu)
		{
			root.printFigure();
		}

		if (e.getSource() == editDeleteMenu)
		{
			root.getControlledSurface().deleteSelected();
		}

		if (e.getSource() == mToolsCreateEvent)
		{
			// Is there a point in having a special way to add events
			// here? Why not simply use the EventEditorDialog?
			//root.getEventPane().createEvent();
			root.createEvent();
		}

		if (e.getSource() == toolsOptionsMenu)
		{
			root.getControlledSurface().setOptionsVisible(true);
		}

		if (e.getSource() == editCopyAsWMFMenu)
		{
			root.copyAsWMFToClipboard();
		}

		if (e.getSource() == mEditUndo)
		{
			if (root.getUndoInterface().canUndo()) {
				root.getUndoInterface().undo();
			}
			mEditUndo.setEnabled(root.getUndoInterface().canUndo());
		}

		if (e.getSource() == mEditRedo)
		{
			if (root.getUndoInterface().canRedo()) {
				root.getUndoInterface().redo();
			}
			mEditRedo.setEnabled(root.getUndoInterface().canRedo());
		}
		
		if (e.getSource() == mEmbedder)
		{
			try {
				int iterations = Integer.parseInt(JOptionPane.showInputDialog(this, "Input A number of Iterations to Run the Embedder",
												  new Integer(100)));
				SpringEmbedder.run(surface, surface.getGraph() , iterations);
			}
			catch(Throwable t)
			{
				JOptionPane.showMessageDialog(this, "Input must be an Integer");
			}												  
		}

		if (e.getSource() == editExportPostscriptMenu)
		{
			root.exportPostscript();
		}

		if (e.getSource() == editExportPDFMenu)
		{
			root.exportPDF();
		}
	}
	public void update(EditorChangedEvent e)
	{
		if (e.getType() == EditorChangedEvent.UNDOREDO)
		{
			mEditRedo.setEnabled(root.getUndoInterface().canRedo());
			mEditUndo.setEnabled(root.getUndoInterface().canUndo());
			mEditRedo.setText(root.getUndoInterface().getRedoPresentationName());
			mEditUndo.setText(root.getUndoInterface().getUndoPresentationName());
		}
	}
}
