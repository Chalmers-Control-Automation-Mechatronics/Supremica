//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.model.compiler
//# CLASS:   ExpressionComparator
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

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

import javax.swing.*;

import net.sourceforge.waters.gui.renderer.LabelBlockProxyShape;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.compiler.TypeMismatchException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.base.ModelChangeEvent;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.EdgeSubject;
import net.sourceforge.waters.subject.module.GuardActionBlockSubject;
import net.sourceforge.waters.subject.module.LabelGeometrySubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;


public class EditorEditEdgeDialog
  extends JDialog
  implements ActionListener
{

  //#########################################################################
  //# Static Invocation
  public static void showDialog(final EdgeSubject edge,
                                final EditorWindowInterface root)
  {
    final ModuleWindowInterface rroot = root.getModuleWindowInterface();
    new EditorEditEdgeDialog(edge, rroot);
  }

  public static void showDialog(final EdgeSubject edge,
                                final ModuleWindowInterface root)
  {
    new EditorEditEdgeDialog(edge, root);
  }


  //#########################################################################
  //# Constructors
  public EditorEditEdgeDialog(final EdgeSubject edge,
                              final ModuleWindowInterface root)
  {
    mEdge = edge;
    mRoot = root;

    GridBagConstraints con = new GridBagConstraints();

    setModal(true);
    setLocationRelativeTo(mRoot.getRootWindow());
    setTitle("Edit Edge");

    //setup layout manager
    layout = new GridBagLayout();
    setLayout(layout);

    //Button panel
    FlowLayout buttonLayout = new FlowLayout(FlowLayout.RIGHT, 5, 5);
    JPanel buttonPanel = new JPanel();
    buttonPanel.setLayout(buttonLayout);

    con.gridx = 2;
    con.gridy = 3;
    con.weightx = 0;
    con.weighty = 0;
    con.anchor = con.EAST;
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
    JScrollPane scrollPaneG = new JScrollPane(guardField);
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
    final StringBuffer buffer = new StringBuffer();
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
    JScrollPane scrollPaneA = new JScrollPane(actionField);
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
    setVisible(true);
  }


  //#########################################################################
  //# Interface java.awt.event.ActionListener
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
      String actionText = actionField.getText();
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
      // Store parsed results ...
      final GuardActionBlockSubject block = mEdge.getGuardActionBlock();
      if (block == null) {
        if (guard != null || actions != null) {
          final List<SimpleExpressionSubject> guards =
            guard == null ? null : Collections.singletonList(guard);
          // *** BUG ***
          // Not a very good position!
          // ***
          final LabelGeometrySubject geo = new LabelGeometrySubject
            (new Point(LabelBlockProxyShape.DEFAULT_OFFSET_X,
                       LabelBlockProxyShape.DEFAULT_OFFSET_Y + 10));
          final GuardActionBlockSubject newblock =
            new GuardActionBlockSubject(guards, actions, geo);
          mEdge.setGuardActionBlock(newblock);
        }
      } else {
        if (guard == null && actions == null) {
          mEdge.setGuardActionBlock(null);
        } else {
          final List<SimpleExpressionSubject> bguards =
            block.getGuardsModifiable();
          bguards.clear();
          if (guard != null) {
            bguards.add(guard);
          }
          final List<BinaryExpressionSubject> bactions =
            block.getActionsModifiable();
          bactions.clear();
          if (actions != null) {
            bactions.addAll(actions);
          }
        }
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
  private JButton okButton, cancelButton;
  private JTextPane guardField, actionField;
  private final JLabel guardLabel = new JLabel("Guard:");
  private final JLabel actionLabel = new JLabel("Action:");
  private GridBagLayout layout;


  //#########################################################################
  //# Class Constants
  private static final int fieldHeight = 100;
  private static final int fieldWidth = 200;

}
