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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JButton;
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
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.ConditionalProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.ConditionalSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.gui.ide.IDE;


/**
 * <P>A dialog to enter and edit conditional blocks.</P>
 *
 * <P>This dialog consists of only one text fields to enter the guard
 * expression. Input validation is performed using an
 * {@link ExpressionParser}.</P>
 *
 * @see ConditionalProxy
 * @author Robi Malik
 */

public class ConditionalEditorDialog
  extends JDialog
{
  //#########################################################################
  //# Constructors
  public ConditionalEditorDialog(final ModuleWindowInterface root,
                                 final SelectionOwner panel)
  {
    this(root, panel, null);
  }

  public ConditionalEditorDialog(final ModuleWindowInterface root,
                                 final SelectionOwner panel,
                                 final ConditionalSubject cond)
  {
    super(root.getRootWindow());
    if (cond == null) {
      setTitle("Creating new conditional block");
    } else {
      setTitle("Editing conditional block");
    }
    mRoot = root;
    mPanel = panel;
    mConditional = cond;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mGuardInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the conditional block being edited by this dialog.
   */
  public ConditionalSubject getEditedItem()
  {
    return mConditional;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final ConditionalSubject template;
    if (mConditional == null) {
      try {
        template = TEMPLATE;
        final List<InsertInfo> inserts = mPanel.getInsertInfo(TRANSFERABLE);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = mConditional;
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
    mGuardLabel = new JLabel("Condition:");
    final SimpleExpressionProxy oldGuard = template.getGuard();
    mGuardInput =
      new SimpleExpressionInputCell(oldGuard, Operator.TYPE_BOOLEAN,
                                    parser, true);
    mGuardInput.addActionListener(commitHandler);
    mGuardInput.addSimpleDocumentListener(okEnablement);
    mGuardInput.setToolTipText("The Boolean expression for the IF-statement.");

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mGuardInput.setErrorDisplay(mErrorLabel);

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
    updateOkButtonStatus();

    final JRootPane root = getRootPane();
    root.setDefaultButton(mOkButton);
    DialogCancelAction.register(this);
  }

  /**
   * Fill the panels and layout all buttons and components.
   * It is assumed that all needed components have been
   * created by a call to {@link #createComponents()} before.
   */
  private void layoutComponents()
  {
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;

    // First, layout the main panel ...
    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);
    // mGuardLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mGuardLabel, constraints);
    mMainPanel.add(mGuardLabel);
    // mGuardInput
    mGuardInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mGuardInput, constraints);
    mMainPanel.add(mGuardInput);

    // Error and buttons panel do not need layouting.

    // Finally, build the full dialog ...
    final Container contents = getContentPane();
    final GridBagLayout layout = new GridBagLayout();
    contents.setLayout(layout);
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.weightx = 1.0;
    constraints.weighty = 0.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.insets = new Insets(0, 0, 0, 0);
    layout.setConstraints(mMainPanel, constraints);
    contents.add(mMainPanel);
    layout.setConstraints(mErrorPanel, constraints);
    contents.add(mErrorPanel);
    layout.setConstraints(mButtonsPanel, constraints);
    contents.add(mButtonsPanel);
    pack();
  }


  //#########################################################################
  //# Action Listeners
  private void updateOkButtonStatus()
  {
    final boolean enabled = mGuardInput.getText().length() > 0;
    mOkButton.setEnabled(enabled);
  }

  /**
   * Commits the contents of this dialog to the model.
   * This method is attached to action listener of the 'OK' button
   * of the event editor dialog.
   */
  private void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mGuardInput.getValue() == null) {
      mGuardInput.requestFocusWithErrorMessage
        ("Please enter a Boolean expression for the IF-statement.");
    } else {
      final SimpleExpressionSubject guard0 =
        (SimpleExpressionSubject) mGuardInput.getValue();
      final SimpleExpressionSubject guard = makeUnique(guard0);
      if (mConditional == null) {
        final ConditionalSubject template = TEMPLATE.clone();
        template.setGuard(guard);
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, mPanel, mRoot);
        mConditional = template;
        executeCommand(command);
      } else {
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
        final SimpleExpressionSubject oldGuard = mConditional.getGuard();
        final boolean guardChange = !eq.equals(guard, oldGuard);
        if (guardChange) {
          final ConditionalSubject template = mConditional.clone();
          if (guardChange) {
            template.setGuard(guard);
          }
          final Command command = new EditCommand(mConditional, template, mPanel);
          executeCommand(command);
        }
      }
      dispose();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether it is unsafe to commit the currently
   * edited text field. If this method returns <CODE>true</CODE>, it is
   * unsafe to commit the current dialog contents, and shifting the focus
   * is to be avoided.
   * @return <CODE>true</CODE> if the component currently owning the focus
   *         is to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    final IDE ide = mRoot.getRootWindow();
    final FocusTracker tracker = ide.getFocusTracker();
    return !tracker.shouldYieldFocus(this);
  }

  private SimpleExpressionSubject makeUnique
    (final SimpleExpressionSubject subject)
  {
    if (subject == null || subject.getParent() == null) {
      return subject;
    } else {
      return subject.clone();
    }
  }

  private void executeCommand(final Command command){
    if(mPanel.getUndoInterface(null) == null){
      command.execute();
    }
    else{
      mPanel.getUndoInterface(null).executeCommand(command);
    }
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;
  private final SelectionOwner mPanel;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mGuardLabel;
  private SimpleExpressionInputCell mGuardInput;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private JButton mOkButton;

  // Created Item
  /**
   * <P>The conditional block subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited. If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is committed and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private ConditionalSubject mConditional;
  private Object mInsertPosition;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 4432878882695422505L;
  private static final ConditionalSubject TEMPLATE =
    new ConditionalSubject(new SimpleIdentifierSubject(""));
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);

}
