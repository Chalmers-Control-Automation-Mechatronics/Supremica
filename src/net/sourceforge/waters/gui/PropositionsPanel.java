//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   AttributesPanel
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

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

import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.subject.module.NodeSubject;

import org.supremica.gui.ide.IDE;


/**
 * A panel for displaying and editing propositions
 *
 * @author Carly Hona
 */

class PropositionsPanel extends JPanel
{

  //#########################################################################
  //# Constructors
  PropositionsPanel(final ModuleWindowInterface rootWindow,
                    final NodeSubject root)
  {
    final IDE ide = rootWindow.getRootWindow().getIDE();
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
