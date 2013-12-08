//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   AliasesPanel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;

import org.supremica.gui.ide.ModuleContainer;


/**
 * The aliases panel inside the definitions tab of the module editor.
 * This panel contains two tree views sitting on top of each other,
 * for editing the constant aliases and the event aliases of a module,
 * respectively.
 *
 * @see ConstantAliasTree
 * @see EventAliasTree
 * @author Carly Hona
 */

public class AliasesPanel extends JPanel
{

  //#########################################################################
  //# Constructors
  public AliasesPanel(final ModuleContainer root,
                      final WatersPopupActionManager manager)
  {
    mConstantAliasesPanel = new ConstantAliasesTree(root, manager);
    mEventsAliasesPanel = new EventAliasesTree(root, manager);
    setLayout(new GridBagLayout());
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    add(mConstantAliasesPanel, constraints);
    constraints.fill = GridBagConstraints.BOTH;
    constraints.weighty = 1.0;
    add(mEventsAliasesPanel, constraints);
  }


  //#########################################################################
  //# Simple Access
  public ConstantAliasesTree getConstantAliasesPanel()
  {
    return mConstantAliasesPanel;
  }

  public EventAliasesTree getEventAliasesPanel()
  {
    return mEventsAliasesPanel;
  }


  //#########################################################################
  //# Data Members
  private final ConstantAliasesTree mConstantAliasesPanel;
  private final EventAliasesTree mEventsAliasesPanel;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

}
