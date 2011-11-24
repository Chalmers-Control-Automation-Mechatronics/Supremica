//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   AliasesPanel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;

/**
 * @author cph4
 */
public class AliasesPanel extends JPanel
{
  public AliasesPanel(final ModuleWindowInterface root,
                              final WatersPopupActionManager manager)
    {
    mConstantAliasesPanel = new ConstantAliasesTree(root, manager);
    mEventsAliasesPanel = new EventAliasesTree(root, manager);
    setBackground(EditorColor.BACKGROUNDCOLOR);
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    add(mConstantAliasesPanel);
    add(mEventsAliasesPanel);
    }

  public ConstantAliasesTree getConstantAliasesPanel(){
    return mConstantAliasesPanel;
  }

  public EventAliasesTree getEventAliasesPanel(){
    return mEventsAliasesPanel;
  }

  private static final long serialVersionUID = 1L;
  private final ConstantAliasesTree mConstantAliasesPanel;
  private final EventAliasesTree mEventsAliasesPanel;
}
