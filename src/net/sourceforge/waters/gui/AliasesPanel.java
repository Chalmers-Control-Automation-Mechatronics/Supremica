//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   AliasesPanel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

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
    setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();

    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.anchor = GridBagConstraints.NORTHWEST;

    add(mConstantAliasesPanel, constraints);

    add(mEventsAliasesPanel, constraints);
    constraints.weighty = 1.0;
    final JPanel invisible1 = new JPanel();
    invisible1.setPreferredSize(new Dimension(0,0));
    add(invisible1, constraints);
    constraints.gridx = 1;
    constraints.gridy = 0;
    constraints.weightx = 1;
    constraints.weighty = 0;
    final JPanel invisible2 = new JPanel();
    invisible2.setPreferredSize(new Dimension(0,0));
    add(invisible2, constraints);
    }

  public ConstantAliasesTree getConstantAliasesPanel(){
    return mConstantAliasesPanel;
  }

  public EventAliasesTree getEventAliasesPanel(){
    return mEventsAliasesPanel;
  }

  private static final long serialVersionUID = 1L;
  private final ConstantAliasesTree mConstantAliasesPanel;
  private final  EventAliasesTree mEventsAliasesPanel;
}
