//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   Languages
//###########################################################################
//# $Id: LanguagesEN.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.Serializable;

/** <p>Waters GUI English Language data.</p>
 *
 * <p>This class fills up Languages structures with English labels.</p>
 *
 * @author Gian Perrone
 */
public class LanguagesEN extends Languages {
    public void LanguagesEN() { } 

    public static void createLanguage(Languages l) {
	l.FileMenu = "File";
	l.EditMenu = "Edit";
	l.ToolsMenu = "Tools";
	l.HelpMenu = "Help";
	
	// Menu Items
	l.FileNewMenu = "New...";
	l.FileOpenMenu = "Open...";
	l.FileSaveMenu = "Save";
	l.FileSaveAsMenu = "Save As...";
	l.FilePageSetupMenu = "Page Setup...";
	l.FilePrintMenu = "Print...";
	l.FileExitMenu = "Exit";
	l.EditUndoMenu = "Undo";
	l.EditCopyMenu = "Copy";
	l.EditCutMenu = "Cut";
	l.EditPasteMenu = "Paste";
	l.EditDelete = "Delete";
	l.ToolsSelectMenu = "Select Tool";
	l.ToolsNodeMenu = "Node Tool";
	l.ToolsEdgeMenu = "Edge Tool";
	l.ToolsInitialMenu = "Initial Node Tool";
	l.ToolsColorMenu = "Coloring Tool";
	l.ToolsOptionsMenu = "Options...";
	l.HelpAboutMenu = "About...";
	
	// Labels
	l.LabelEvents = "Events";
	l.LabelComponents = "Components";
	
	l.languageName = "EN";
    }
}
