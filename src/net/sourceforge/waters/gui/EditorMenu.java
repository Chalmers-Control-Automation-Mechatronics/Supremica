//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorMenu
//###########################################################################
//# $Id: EditorMenu.java,v 1.17 2005-12-01 00:29:58 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;

// Printing
import java.awt.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import java.util.Locale;

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
	public final JMenuItem FileNewMenu;
	public final JMenuItem PrintMenu;
	public final JMenuItem FileExitMenu;
	public final JMenuItem mEditUndo;
	public final JMenuItem mEditRedo;
	public final JMenuItem mToolsCreateEvent;
	public final JMenuItem ToolsOptionsMenu;
	public final JMenuItem editDeleteMenu;
	public final JMenuItem editCopyAsWMFMenu;
	public final JMenuItem editCreatePDFMenu;
	EditorWindowInterface root;
	ControlledSurface surface;
	JFileChooser fileChooser;

	public EditorMenu(ControlledSurface c, EditorWindowInterface r)
	{
		root = r;
		surface = c;

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
		menuItem.addActionListener(this);
		PrintMenu = menuItem;
		//menuItem.setEnabled(false);
		//menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();

		menuItem = new JMenuItem("Close Window", KeyEvent.VK_X);
		menu.add(menuItem);
		menuItem.addActionListener(this);

		FileExitMenu = menuItem;
		menu = new JMenu("Edit");

		this.add(menu);

		menuItem = new JMenuItem("Undo");
		menuItem.setEnabled(root.getUndoInterface().canUndo());
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menuItem.addActionListener(this);

		mEditUndo = menuItem;

		menuItem = new JMenuItem("Redo");
		menuItem.setEnabled(root.getUndoInterface().canRedo());
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
		menu.addSeparator();
		menuItem.addActionListener(this);

		mEditRedo = menuItem;

		menu.addSeparator();

	/*
		menuItem = new JMenuItem("Copy");
		menuItem.setEnabled(false);
		menuItem.setToolTipText("Not implemented yet");
		menu.add(menuItem);
	*/

		menuItem = new JMenuItem("Copy");
		menu.add(menuItem);
		menuItem.addActionListener(this);

		editCopyAsWMFMenu = menuItem;

		menuItem = new JMenuItem("Create PDF");
		menu.add(menuItem);
		menuItem.addActionListener(this);

		editCreatePDFMenu = menuItem;

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
			surface.clearAll();
		}

		if (e.getSource() == FileExitMenu)
		{
			// root.dispose();
		}

		if (e.getSource() == PrintMenu)
		{
			ControlledSurface surface = root.getControlledSurface();

			PrinterJob printJob = PrinterJob.getPrinterJob();
			if (printJob.getPrintService() == null)
			{
				System.err.println("No default printer set.");
				return;
			}
			printJob.setPrintable((EditorSurface) surface);

			// Printing attributes
			PrintRequestAttribute name = new JobName("Waters Printing", Locale.ENGLISH);
			PrintRequestAttributeSet attributes = new HashPrintRequestAttributeSet();
			attributes.add(name);

			// Show printing dialog
			if (printJob.printDialog(attributes))
			{
				try
				{
					// Print!
					printJob.print(attributes);
				}
				catch (Exception ex)
				{
					System.err.println(ex.getStackTrace());
				}
			}
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

		if (e.getSource() == editCreatePDFMenu)
		{
			/*
			File f = new File("C:/Temp/test.pdf");
			root.createPDF(f);
			*/

			JFileChooser chooser = new JFileChooser();
			int returnVal = chooser.showSaveDialog(surface);

			if (returnVal == JFileChooser.APPROVE_OPTION) 
			{
				File file = chooser.getSelectedFile();
				root.createPDF(file);
			} 
			else 
			{
			}
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
