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

package org.supremica.gui.editor.useractions;

import java.util.*;
import javax.swing.*;

import com.nwoods.jgo.JGoSelection;
import com.nwoods.jgo.JGoObject;
import com.nwoods.jgo.JGoLink;

import org.supremica.gui.editor.AutomataEditor;
import org.supremica.gui.editor.AutomatonView;
import org.supremica.gui.editor.EditorView;
import org.supremica.gui.editor.StateNode;
import org.supremica.gui.editor.NailNode;
import org.supremica.gui.VisualProject;

public abstract class EditorAction
	extends AbstractAction
{
	private static Collection theActions = new ArrayList();	

	private EditorView editorView;
	private int mnemonic = Integer.MIN_VALUE; 
	
	public EditorAction(EditorView editorView)
	{
		super();
		init(editorView);
	}

	public EditorAction(String name, EditorView editorView)
	{
		super(name);
		init(editorView);
	}
	
	public EditorAction(String name, Icon icon, EditorView editorView)
	{
		super(name, icon);
		init(editorView);
	}

	public int getMnemonic()
	{
		return mnemonic;
	}
	
	private final void init(EditorView editorView)
	{
		this.editorView = editorView;
		theActions.add(this);
	}
	
	public EditorView getEditorView()
	{
		return editorView;
	}
	
	public VisualProject getVisualProject()
	{
		return editorView.getVisualProject();
	}	
	
	public StateNode getSelectedState()
	{
		JGoSelection currSelection = getSelection();
		if (currSelection == null) return null;
		JGoObject primarySelection = currSelection.getPrimarySelection();
		if (primarySelection instanceof StateNode)
		{
			return (StateNode)primarySelection;
		}
		return null;
	}

	public NailNode getSelectedNail()
	{
		JGoSelection currSelection = getSelection();
		if (currSelection == null) return null;
		JGoObject primarySelection = currSelection.getPrimarySelection();
		if (primarySelection instanceof NailNode)
		{
			return (NailNode)primarySelection;
		}
		return null;
	}	

	public JGoLink getSelectedLink()
	{
		JGoSelection currSelection = getSelection();
		if (currSelection == null) return null;		
		JGoObject primarySelection = currSelection.getPrimarySelection();
		if (primarySelection instanceof JGoLink)
		{
			return (JGoLink)primarySelection;
		}
		return null;
	}
	
	private JGoSelection getSelection()
	{
		AutomatonView currView = editorView.getCurrentAutomatonView();
		if (currView == null)
		{
			return null;
		}
		else return currView.getSelection();
	}

	public AutomataEditor getApp()
	{
		return editorView.getAutomataEditor();
	}

	public AutomatonView getView()
	{
		return editorView.getCurrentAutomatonView();
	}

	public String toString()
	{
		return (String) getValue(NAME);
	}

	// by default each AppAction is disabled if there's no current view
	public boolean canAct()
	{
		return (getView() != null);
	}

	public void updateEnabled()
	{
		setEnabled(canAct());
	}

	public void free()
	{
		theActions.remove(this);

		editorView = null;
	}

	// keep track of all instances of AppAction
	public static void updateAllActions()
	{
		for (Iterator actIt = theActions.iterator(); actIt.hasNext(); )
		{
			EditorAction currAction = (EditorAction) actIt.next();
			currAction.updateEnabled();
		}
	}

	public static Iterator actionIterator()
	{
		return theActions.iterator();
	}
}
