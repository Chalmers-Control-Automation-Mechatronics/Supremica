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

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.DeleteCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.expr.TypeMismatchException;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.gui.ide.ComponentEditorPanel;


/**
 * The dialog window for guard/action blocks.
 * This window is associated with edges of a graph, and opened by double-click
 * or the "Properties..." option from the popup menu. It contains text fields
 * for the guards and actions.
 *
 * @author Markus Sk&ouml;ldstam, Robi Malik
 */

public class EdgeEditorDialog
  extends JDialog
  implements ActionListener
{

  //#########################################################################
  //# Static Invocation
  public static void showDialog(final EdgeSubject edge,
                                final ComponentEditorPanel root)
  {
    final ModuleWindowInterface rroot = root.getModuleWindowInterface();
    new EdgeEditorDialog(edge, rroot);
  }

  public static void showDialog(final EdgeSubject edge,
                                final ModuleWindowInterface root)
  {
    new EdgeEditorDialog(edge, root);
  }


  //#########################################################################
  //# Constructors
  public EdgeEditorDialog(final EdgeSubject edge,
                          final ModuleWindowInterface root)
  {
    super(root.getRootWindow());
    setMinimumSize(MIN_SIZE);
    mEdge = edge;
    mRoot = root;
    mPanel = root.getActiveComponentEditorPanel().getGraphEditorPanel();

    GridBagConstraints con = new GridBagConstraints();

    setModal(true);
    setLocationRelativeTo(mRoot.getRootWindow());
    setTitle("Edit Edge");

    //setup layout manager
    layout = new GridBagLayout();
    setLayout(layout);

    //Button panel
    final FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT, 5, 5);
    final JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(buttonLayout);

    con.gridx = 2;
    con.gridy = 3;
    con.weightx = 0;
    con.weighty = 0;
    con.anchor = GridBagConstraints.EAST;
    layout.setConstraints(buttonPanel, con);
    buttonPanel.setAlignmentX(JPanel.RIGHT_ALIGNMENT);
    add(buttonPanel);

    //ok button
    okButton = new JButton();
    okButton.setText("Ok");
    buttonPanel.add(okButton);
    okButton.setActionCommand("OK");
    okButton.addActionListener(this);
    buttonPanel.add(okButton);

    getRootPane().setDefaultButton(okButton);

    //cancel button
    cancelButton = new JButton();
    cancelButton.setText("Cancel");
    add(cancelButton);
    cancelButton.setActionCommand("Cancel");
    cancelButton.addActionListener(this);
    buttonPanel.add(cancelButton);

    //guard field
    con = new GridBagConstraints();
    con.gridx = 1;
    con.gridy = 1;
    con.insets = new Insets(5,5,5,5);
    layout.setConstraints(guardLabel, con);
    add(guardLabel);

    final GuardActionBlockSubject block = mEdge.getGuardActionBlock();
    final List<SimpleExpressionSubject> guards;
    final List<BinaryExpressionSubject> actions;
    if (block == null) {
      guards = Collections.emptyList();
      actions = Collections.emptyList();
    } else {
      guards = block.getGuardsModifiable();
      actions = block.getActionsModifiable();
    }
    guardField = new JTextPane();
    if (!guards.isEmpty()) {
      final SimpleExpressionSubject guard = guards.iterator().next();
      final String guardText = guard.toString();
      guardField.setText(guardText);
    }
    guardField.setMargin(new Insets(5, 5, 5, 5));
    final JScrollPane scrollPaneG = new JScrollPane(guardField);
    scrollPaneG.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
    con.gridx = 2;
    con.gridy = 1;
    con.weightx = 1;
    con.weighty = 1;
    con.fill = GridBagConstraints.BOTH;
    con.insets = new Insets(5,5,5,5);
    layout.setConstraints(scrollPaneG, con);
    add(scrollPaneG);

    //action field
    con = new GridBagConstraints();
    con.gridx = 1;
    con.gridy = 2;
    con.insets = new Insets(5,5,5,5);
    layout.setConstraints(actionLabel, con);
    add(actionLabel);

    actionField = new JTextPane();
    final StringBuilder buffer = new StringBuilder();
    boolean first = true;
    for (final BinaryExpressionSubject action : actions) {
      if (first) {
        first = false;
      } else {
        buffer.append("; ");
      }
      final String text = action.toString();
      buffer.append(text);
    }
    final String actionText = buffer.toString();
    actionField.setText(actionText);
    actionField.setMargin(new Insets(5, 5, 5, 5));
    final JScrollPane scrollPaneA = new JScrollPane(actionField);
    scrollPaneA.setPreferredSize(new Dimension(fieldWidth, fieldHeight));
    con.gridx = 2;
    con.gridy = 2;
    con.weightx = 1;
    con.weighty = 1;
    con.fill = GridBagConstraints.BOTH;
    con.insets = new Insets(5,5,5,5);
    layout.setConstraints(scrollPaneA, con);
    add(scrollPaneA);

    pack();
    setLocationRelativeTo(root.getRootWindow());
    setVisible(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
  @Override
  public void actionPerformed(final ActionEvent event)
  {
    if (event.getActionCommand().equals("OK")) {
      final ExpressionParser parser = mRoot.getExpressionParser();
      // Get guard ...
      SimpleExpressionSubject guard;
      try {
        final String text = guardField.getText();
        guard = null;
        if (text != null && !text.trim().equals("")) {
          guard = (SimpleExpressionSubject) parser.parse
            (text, Operator.TYPE_BOOLEAN);
        }
      }	catch (final ParseException exception) {
        JOptionPane.showMessageDialog(this, exception.getMessage(),
                                      "Syntax error in guard!",
                                      JDialog.DO_NOTHING_ON_CLOSE);
        return;
      }
      // Get actions ...
      List<BinaryExpressionSubject> actions = null;
      final String actionText = actionField.getText();
      if (actionText != null && !actionText.trim().equals("")) {
        final String[] texts = actionField.getText().split(";");
        actions = new ArrayList<BinaryExpressionSubject>(texts.length);
        for (final String text : texts)	{
          if (text.length() > 0) {
            try	{
              final SimpleExpressionSubject action =
                (SimpleExpressionSubject) parser.parse(text);
              if (!(action instanceof BinaryExpressionSubject))	{
                throw new TypeMismatchException(action, "ACTION");
              }
              final BinaryExpressionSubject binaction =
                (BinaryExpressionSubject) action;
              actions.add(binaction);
            } catch (final ParseException exception) {
              JOptionPane.showMessageDialog(this, exception.getMessage(),
                                            "Syntax error in action!",
                                            JDialog.DO_NOTHING_ON_CLOSE);
              return;
            } catch (final TypeMismatchException exception) {
              JOptionPane.showMessageDialog(this, exception.getMessage(),
                                            "Syntax error in action!",
                                            JDialog.DO_NOTHING_ON_CLOSE);
              return;
            }
          }
        }
      }
      final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(false);
      // Store parsed results ...
      final GuardActionBlockSubject block = mEdge.getGuardActionBlock();
      Command command = null;
      if (block == null) {
        if (guard != null || actions != null) {
          final EdgeSubject newEdge = (EdgeSubject)cloner.getClone(mEdge);
          final List<SimpleExpressionSubject> guards =
            guard == null ? null : Collections.singletonList(guard);
          // TODO Find a better position ...
          final LabelGeometrySubject geo = new LabelGeometrySubject
            (new Point(LabelBlockProxyShape.DEFAULT_OFFSET_X,
                       LabelBlockProxyShape.DEFAULT_OFFSET_Y + 10));
          final GuardActionBlockSubject newblock =
            new GuardActionBlockSubject(guards, actions, geo);
          newEdge.setGuardActionBlock(newblock);
          if(!eq.equals(mEdge, newEdge)){
            command = new EditCommand(mEdge, newEdge, null);
          }
        }
      } else {
        if (guard == null && actions == null) {
          final List<GuardActionBlockSubject> selection =
            Collections.singletonList(block);
          final List<InsertInfo> deletes =
            mPanel.getDeletionVictims(selection);
          // The user may now have cancelled the deletion (?)
          if (deletes != null) {
            command = new DeleteCommand(deletes, mPanel);
          }
        } else {
          final GuardActionBlockSubject newblock =
            (GuardActionBlockSubject) cloner.getClone(block);
          final List<SimpleExpressionSubject> bguards =
            newblock.getGuardsModifiable();
          bguards.clear();
          if (guard != null) {
            bguards.add(guard);
          }
          final List<BinaryExpressionSubject> bactions =
            newblock.getActionsModifiable();
          bactions.clear();
          if (actions != null) {
            bactions.addAll(actions);
          }
          if(!eq.equals(block, newblock)){
            command = new EditCommand(block, newblock, null);
          }
        }
      }
      if (command != null){
        mRoot.getUndoInterface().executeCommand(command);
      }
      dispose();
    } else if (event.getActionCommand().equals("Cancel")) {
      dispose();
    }
  }


  //#########################################################################
  //# Data Members
  private final EdgeSubject mEdge;
  private final ModuleWindowInterface mRoot;
  private final SelectionOwner mPanel;
  private final JButton okButton, cancelButton;
  private final JTextPane guardField, actionField;
  private final JLabel guardLabel = new JLabel("Guard:");
  private final JLabel actionLabel = new JLabel("Action:");
  private final GridBagLayout layout;


  //#########################################################################
  //# Class Constants
  private static final int fieldHeight = 100;
  private static final int fieldWidth = 200;
  private static Dimension MIN_SIZE = new Dimension(273, 284);

  private static final long serialVersionUID = 308147283952619954L;

}
