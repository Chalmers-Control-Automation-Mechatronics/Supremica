/*
 * Supremica Software License Agreement
 *
 * The Supremica software is not in the public domain
 * However, it is freely available without fee for education,
 * research, and non-profit purposes.  By obtaining copies of
 * this and other files that comprise the Supremica software,
 * you, the Licensee, agree to abide by the following
 * conditions and understandings with respect to the
 * copyrighted software:
 *
 * The software is copyrighted in the name of Supremica,
 * and ownership of the software remains with Supremica.
 *
 * Permission to use, copy, and modify this software and its
 * documentation for education, research, and non-profit
 * purposes is hereby granted to Licensee, provided that the
 * copyright notice, the original author's names and unit
 * identification, and this permission notice appear on all
 * such copies, and that no charge be made for such copies.
 * Any entity desiring permission to incorporate this software
 * into commercial products or to use it for commercial
 * purposes should contact:
 *
 * Knut Akesson (KA), knut@supremica.org
 * Supremica,
 * Haradsgatan 26A
 * 431 42 Molndal
 * SWEDEN
 *
 * to discuss license terms. No cost evaluation licenses are
 * available.
 *
 * Licensee may not use the name, logo, or any other symbol
 * of Supremica nor the names of any of its employees nor
 * any adaptation thereof in advertising or publicity
 * pertaining to the software without specific prior written
 * approval of the Supremica.
 *
 * SUPREMICA AND KA MAKES NO REPRESENTATIONS ABOUT THE
 * SUITABILITY OF THE SOFTWARE FOR ANY PURPOSE.
 * IT IS PROVIDED "AS IS" WITHOUT EXPRESS OR IMPLIED WARRANTY.
 *
 * Supremica or KA shall not be liable for any damages
 * suffered by Licensee from the use of this software.
 *
 * Supremica is owned and represented by KA.
 */

package org.supremica.gui.editor;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

import com.nwoods.jgo.*;
import com.nwoods.jgo.layout.JGoNetwork;

public class EditorActions
{
	private AutomataEditor theEditor = null;

	private AppAction fileAddAction = null;
	private AppAction fileOpenAction = null;
	private AppAction fileSaveAction = null;
	private AppAction fileSaveAsAction = null;
	private AppAction filePrintAction = null;
	private AppAction fileExitAction = null;

	private AppAction editCutAction = null;
	private AppAction editCopyAction = null;
	private AppAction editPasteAction = null;
	private AppAction editDeleteAction = null;
	private AppAction editUndoAction = null;
	private AppAction editRedoAction = null;

	private AppAction viewZoomAction = null;
	private AppAction viewZoomInAction = null;
	private AppAction viewZoomOutAction = null;

	private AppAction helpHelpTopicsAction = null;

	public EditorActions(AutomataEditor theEditor)
	{
		this.theEditor = theEditor;
	}

	public AppAction getFileAddAction()
	{
		if (fileAddAction == null)
		{
			fileAddAction = new AppAction("Add", theEditor)
			{
   				public void actionPerformed(ActionEvent e)
   				{
					theEditor.fileAdd();
				}

				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return fileAddAction;
	}

	public AppAction getFileOpenAction()
	{
		if (fileOpenAction == null)
		{
			fileOpenAction = new AppAction("Open...", theEditor)
			{
				public void actionPerformed(ActionEvent e)
				{
					theEditor.fileOpen();
				}
				
				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return fileOpenAction;
	}
	
	public AppAction getFileSaveAction()
	{
		if (fileSaveAction == null)
		{
			fileSaveAction = new AppAction("Save", theEditor)
			{
				public void actionPerformed(ActionEvent e)
				{
					theEditor.fileSave();
				}
				
				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return fileSaveAction;
	}	

	public AppAction getFileSaveAsAction()
	{
		if (fileSaveAsAction == null)
		{
			fileSaveAsAction = new AppAction("Save As...", theEditor)
			{
				public void actionPerformed(ActionEvent e)
				{
					theEditor.fileSaveAs();
				}
				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return fileSaveAsAction;
	}

	public AppAction getFilePrintAction()
	{
		if (filePrintAction == null)
		{
			filePrintAction = new AppAction("Print", theEditor)
			{
				public void actionPerformed(ActionEvent e)
				{
					theEditor.filePrint();
				}
				
				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return filePrintAction;
	}

	public AppAction getFileExitAction()
	{
		if (fileExitAction == null)
		{
			fileExitAction = new AppAction("Exit", theEditor)
			{
				public void actionPerformed(ActionEvent e)
				{
					theEditor.fileExit();
				}
				
				public boolean canAct()
				{
					return true;
				}	
			};
		}
		return fileExitAction;
	}
}


