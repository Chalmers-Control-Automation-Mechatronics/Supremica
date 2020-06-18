//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

package net.sourceforge.waters.gui.dialog;

import java.awt.AWTKeyStroke;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.PropositionsTree;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


/**
 * A panel for displaying and editing propositions
 *
 * @author Carly Hona
 */

class PropositionsPanel extends JPanel
{

  //#########################################################################
  //# Constructors
  PropositionsPanel(final ModuleContainer rootWindow,
                    final NodeSubject root)
  {
    final IDE ide = rootWindow.getIDE();
    final WatersPopupActionManager manager = ide.getPopupActionManager();
    mPropositionsTree = new PropositionsTree(rootWindow, manager, root, null);
    mPropositionsTree.setAutoscrolls(true);
    mPropositionsTree.setBackground(EditorColor.BACKGROUNDCOLOR);
    final Dimension minsize = new Dimension(0, 0);
    mPropositionsTree.setMinimumSize(minsize);
    mPropositionsTree.setVisibleRowCount(4);

    // Layout components ...
    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = INSETS;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.weighty = 1.0;/*
    // Label
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.gridheight = 2;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    layout.setConstraints(label, constraints);
    add(label);
    // Tree
    constraints.gridx++;*/
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    final JScrollPane scrolled = new JScrollPane(mPropositionsTree);
    final Border border = BorderFactory.createLoweredBevelBorder();
    scrolled.setBorder(border);
    layout.setConstraints(scrolled, constraints);
    add(scrolled);
  }

  public PlainEventListSubject getPropositions(){
    final NodeSubject node = (NodeSubject)mPropositionsTree.getRoot();
    return node.getPropositions();
  }

  //#########################################################################
  //# Configuration
  void setFocusTraversalKeys(final Container container)
  {
    final Set<AWTKeyStroke> forward = container.getFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    final Set<AWTKeyStroke> backward = container.getFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    setFocusTraversalKeys(forward, backward);
  }

  void setFocusTraversalKeys(final Set<AWTKeyStroke> forward,
                             final Set<AWTKeyStroke> backward)
  {
    mPropositionsTree.setFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
    mPropositionsTree.setFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
  }


  //#########################################################################
  //# Data Members
  private final PropositionsTree mPropositionsTree;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private static final Insets INSETS = new Insets(0,0,0,0);

}
