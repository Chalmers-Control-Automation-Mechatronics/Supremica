//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2021 Robi Malik
//###########################################################################
//# This file is part of Waters.
//# Waters is free software: you can redistribute it and/or modify it under
//# the terms of the GNU General Public License as published by the Free
//# Software Foundation, either version 2 of the License, or (at your option)
//# any later version.
//# Waters is distributed in the hope that it will be useful, but WITHOUT ANY
//# WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
//# FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
//# details.
//# You should have received a copy of the GNU General Public License along
//# with Waters. If not, see <http://www.gnu.org/licenses/>.
//#
//# Linking Waters statically or dynamically with other modules is making a
//# combined work based on Waters. Thus, the terms and conditions of the GNU
//# General Public License cover the whole combination.
//# In addition, as a special exception, the copyright holders of Waters give
//# you permission to combine Waters with code included in the standard
//# release of Supremica under the Supremica Software License Agreement (or
//# modified versions of such code, with unchanged license). You may copy and
//# distribute such a system following the terms of the GNU GPL for Waters and
//# the licenses of the other code concerned.
//# Note that people who make modified versions of Waters are not obligated to
//# grant this special exception for their modified versions; it is their
//# choice whether to do so. The GNU General Public License gives permission
//# to release a modified version without this exception; this exception also
//# makes it possible to release a modified version which carries forward this
//# exception.
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
 * @see ConstantAliasesTree
 * @see EventAliasesTree
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
