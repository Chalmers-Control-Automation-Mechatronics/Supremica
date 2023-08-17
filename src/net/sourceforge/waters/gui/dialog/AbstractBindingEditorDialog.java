//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2023 Robi Malik
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

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.event.DocumentEvent;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.ProxyTransferable;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.base.ProxySubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;

import org.supremica.gui.ide.IDE;


/**
 * A common superclass to implement the similar dialogs for event aliases
 * ({@link EventAliasEditorDialog}) and parameter bindings ({@link
 * ParameterBindingEditorDialog}).
 *
 * @author Carly Hona
 */

public abstract class AbstractBindingEditorDialog<I extends IdentifierProxy>
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public AbstractBindingEditorDialog(final ModuleWindowInterface root)
  {
    super(root.getRootWindow());
    mRoot = root;
  }

  // Have to use this method otherwise the abstract methods would be used
  // before they are available.
  void initialize()
  {
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }


  //#########################################################################
  //# Simple Access
  ModuleWindowInterface getRoot()
  {
    return mRoot;
  }


  //#########################################################################
  //# Abstract Methods
  abstract SelectionOwner getSelectionOwner();
  abstract ProxySubject getProxySubject();
  abstract void setProxySubject(ProxySubject template);
  abstract ProxySubject createNewProxySubject(IdentifierSubject ident,
                                              ExpressionSubject exp);
  abstract ExpressionSubject getExpression();
  abstract ExpressionSubject getExpression(ProxySubject template);
  abstract FormattedInputHandler<I>
    createInputParser(IdentifierProxy ident, ExpressionParser parser);
  abstract IdentifierSubject getProxyIdentifier();
  abstract IdentifierSubject getProxyIdentifier(ProxySubject template);
  abstract int getOperatorMask();
  abstract ProxyTransferable createTemplateTransferable();
  abstract void setIdentifier(ProxySubject template, IdentifierSubject ident);
  abstract void setExpression(ProxySubject template, ExpressionSubject exp);


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    ProxySubject template;
    if (getProxySubject() == null) {
      try {
        final SelectionOwner panel = getSelectionOwner();
        final ProxyTransferable transferable = createTemplateTransferable();
        final List<InsertInfo> inserts = panel.getInsertInfo(transferable);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
        final ModuleProxyCloner cloner =
          ModuleSubjectFactory.getCloningInstance();
        final Proxy raw = transferable.getRawData().get(0);
        template = (ProxySubject) cloner.getClone(raw);
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = getProxySubject();
    }
    final ExpressionParser parser = mRoot.getExpressionParser();
    final SimpleDocumentListener okEnablement = new SimpleDocumentListener() {
      @Override
      public void documentChanged(final DocumentEvent event)
      {
        updateOkButtonStatus();
      }
    };
    final ActionListener commitHandler = new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };

    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mNameLabel = new JLabel("Name:");
    final IdentifierSubject oldIdent = getProxyIdentifier(template);
    final FormattedInputHandler<I>
      handler = createInputParser(oldIdent, parser);
    mNameInput = new SimpleExpressionInputCell(oldIdent, handler);
    mNameInput.addActionListener(commitHandler);
    mNameInput.addSimpleDocumentListener(okEnablement);
    mNameInput.setToolTipText("Enter the name.");
    mExpressionLabel = new JLabel("Expression:");
    SimpleExpressionProxy oldExp = null;
    if (getProxySubject() != null &&
        getExpression() instanceof SimpleExpressionProxy) {
      oldExp = (SimpleExpressionProxy) getExpression();
    }
    mExpressionInput =
      new SimpleExpressionInputCell(oldExp, getOperatorMask(), parser, true);
    mExpressionInput.addActionListener(commitHandler);
    mExpressionInput.addSimpleDocumentListener(okEnablement);
    mExpressionInput.setToolTipText("Enter the expression.");

    final ExpressionProxy exp = getExpression(template);
    mIsSimpleExpCheckBox = new JCheckBox("Use Simple Expression");
    mIsSimpleExpCheckBox.setToolTipText
      ("Uncheck this to create an event list expression after closing this dialog.");
    mIsSimpleExpCheckBox.setRequestFocusEnabled(false);
    mIsSimpleExpCheckBox.setSelected(exp instanceof SimpleExpressionProxy);
    mIsSimpleExpCheckBox.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        updateExpressionEnabled();
      }
    });

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);
    mExpressionInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    mOkButton = new JButton("OK");
    mOkButton.setRequestFocusEnabled(false);
    mOkButton.addActionListener(commitHandler);
    mButtonsPanel.add(mOkButton);
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(final ActionEvent event)
      {
        dispose();
      }
    });
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(mOkButton);
    DialogCancelAction.register(this);
    updateExpressionEnabled();
  }

  /**
   * Fill the panels and layout all buttons and components. It is assumed that
   * all needed components have been created by a call to
   * {@link #createComponents()} before.
   */
  private void layoutComponents()
  {
    mNameInput.setColumns(30);
    mExpressionInput.setColumns(30);

    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = new Insets(2, 4, 2, 4);

    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mNameLabel, constraints);
    mMainPanel.add(mNameLabel);

    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);

    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mIsSimpleExpCheckBox, constraints);
    mMainPanel.add(mIsSimpleExpCheckBox);

    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    mainlayout.setConstraints(mExpressionLabel, constraints);
    mMainPanel.add(mExpressionLabel);

    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mExpressionInput, constraints);
    mMainPanel.add(mExpressionInput);

    // Error and buttons panel do not need layouting.

    // Finally, build the full dialog ...
    final Container contents = getContentPane();
    final GridBagLayout layout = new GridBagLayout();
    contents.setLayout(layout);
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    layout.setConstraints(mMainPanel, constraints);
    contents.add(mMainPanel);
    layout.setConstraints(mErrorPanel, constraints);
    contents.add(mErrorPanel);
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mButtonsPanel, constraints);
    contents.add(mButtonsPanel);
    pack();
    final Dimension size = getSize();
    setMinimumSize(size);
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Commits the contents of this dialog to the model. This method is attached
   * to action listener of the 'OK' button of the event editor dialog.
   */
  private void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mNameInput.getValue() == null) {
      mNameInput.requestFocusWithErrorMessage("Please enter a name.");
    } else if (mIsSimpleExpCheckBox.isSelected() &&
               mExpressionInput.getValue() == null) {
      mExpressionInput.requestFocusWithErrorMessage
        ("Please enter an expression.");
    } else {
      ExpressionSubject exp = null;
      if (mIsSimpleExpCheckBox.isSelected()) {
        final SimpleExpressionSubject exp0 =
          (SimpleExpressionSubject) mExpressionInput.getValue();
        exp = makeUnique(exp0);
      }
      final IdentifierSubject ident =
        (IdentifierSubject) mNameInput.getValue();
      final SelectionOwner panel = getSelectionOwner();
      final ProxySubject subject = getProxySubject();
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      ProxySubject template = (ProxySubject) cloner.getClone(subject);
      if (subject == null) {
        template = createNewProxySubject(ident, exp);
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, panel, mRoot);
        setProxySubject(template);
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
        final IdentifierSubject oldIdent = getProxyIdentifier();
        final boolean namechange = !eq.equals(ident, oldIdent);
        final ExpressionSubject oldExp = getExpression();
        boolean expchange = false;
        if (exp == null) {
          if (oldExp instanceof SimpleExpressionProxy) {
            exp = new PlainEventListSubject();
            expchange = true;
          }
        } else {
          expchange = !eq.equals(exp, oldExp);
        }
        if (namechange || expchange) {
          if (namechange) {
            setIdentifier(template, ident);
          }
          if (expchange) {
            setExpression(template, exp);
          }
          final Command command = new EditCommand(subject, template, panel);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      dispose();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether it is unsafe to commit the currently edited text field. If
   * this method returns <CODE>true</CODE>, it is unsafe to commit the current
   * dialog contents, and shifting the focus is to be avoided.
   * @return <CODE>true</CODE> if the component currently owning the focus is
   *         to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    final IDE ide = mRoot.getRootWindow();
    final FocusTracker tracker = ide.getFocusTracker();
    return !tracker.shouldYieldFocus(this);
  }

  private SimpleExpressionSubject makeUnique(final SimpleExpressionSubject subject)
  {
    if (subject == null || subject.getParent() == null) {
      return subject;
    } else {
      return subject.clone();
    }
  }

  private void updateExpressionEnabled()
  {
    final boolean enable = mIsSimpleExpCheckBox.isSelected();
    mExpressionInput.setEnabled(enable);
    mExpressionLabel.setEnabled(enable);
    if (!enable) {
      mErrorLabel.clearDisplay();
    } else {
      mExpressionInput.requestFocusInWindow();
    }
    updateOkButtonStatus();
  }

  private void updateOkButtonStatus()
  {
    if (mNameInput.getText().length() == 0) {
      mOkButton.setEnabled(false);
    } else if (mIsSimpleExpCheckBox.isSelected()) {
      mOkButton.setEnabled(mExpressionInput.getText().length() > 0);
    } else {
      mOkButton.setEnabled(true);
    }
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionInputCell mNameInput;
  private JLabel mExpressionLabel;
  private SimpleExpressionInputCell mExpressionInput;
  private JCheckBox mIsSimpleExpCheckBox;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private JButton mOkButton;

  private Object mInsertPosition;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2379273742747986895L;

}
