//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   Languages
//###########################################################################
//# $Id: Languages.java,v 1.1 2005-02-17 01:43:35 knut Exp $
//###########################################################################

package net.sourceforge.waters.gui;

import java.io.Serializable;

/** <p>Manages internationalization and multi-language support.</p>
 *
 * <p>This class consists primarily of public fields which are
 * used in place of strings which would otherwise be hardcoded.</p>
 *
 * @author Gian Perrone
 */
public class Languages implements Serializable {
    public Languages() { }
    public String languageName = "default";
    
    // Menubar Labels
    public String FileMenu;
    public String EditMenu;
    public String ToolsMenu;
    public String HelpMenu;
    
    // Menu Items
    public String FileNewMenu;
    public String FileOpenMenu;
    public String FileSaveMenu;
    public String FileSaveAsMenu;
    public String FilePageSetupMenu;
    public String FilePrintMenu;
    public String FileExitMenu;
    public String EditUndoMenu;
    public String EditCopyMenu;
    public String EditCutMenu;
    public String EditPasteMenu;
    public String EditDelete;
    public String ToolsSelectMenu;
    public String ToolsNodeMenu;
    public String ToolsEdgeMenu;
    public String ToolsInitialMenu;
    public String ToolsColorMenu;
    public String ToolsOptionsMenu;
    public String HelpAboutMenu;

    // Labels
    public String LabelEvents;
    public String LabelComponents;
}
