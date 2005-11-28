
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
 * Knarrhogsgatan 10
 * SE-431 60 MOLNDAL
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

// import java.awt.event.*;
import org.supremica.gui.editor.useractions.*;

public class EditorActions
{
	private AutomataEditor theEditor = null;
	private FileAddAction fileAddAction = null;
	private FilePrintAction filePrintAction = null;
	private FileCloseAction fileCloseAction = null;
	private DeleteSelectionAction deleteSelectionAction = null;
	private DeleteStateAction deleteStateAction = null;

	public EditorActions(AutomataEditor theEditor)
	{
		this.theEditor = theEditor;

		if (theEditor == null)
		{
			System.err.println("theEditor is null");
		}

		fileAddAction = new FileAddAction(theEditor);
		filePrintAction = new FilePrintAction(theEditor);
		fileCloseAction = new FileCloseAction(theEditor);
		deleteStateAction = new DeleteStateAction(theEditor);
		deleteSelectionAction = new DeleteSelectionAction(theEditor);
	}

	public FileAddAction getFileAddAction()
	{
		return fileAddAction;
	}

	public FilePrintAction getFilePrintAction()
	{
		return filePrintAction;
	}

	public FileCloseAction getFileCloseAction()
	{
		return fileCloseAction;
	}

	public DeleteSelectionAction getDeleteSelectionAction()
	{
		return deleteSelectionAction;
	}

	public DeleteStateAction getDeleteStateAction()
	{
		return deleteStateAction;
	}
}
