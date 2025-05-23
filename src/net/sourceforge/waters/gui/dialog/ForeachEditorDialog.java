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
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.ForeachSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import org.supremica.gui.ide.ComponentEditorPanel;
import org.supremica.gui.ide.IDE;


/**
 * <P>A dialog to enter and edit foreach blocks.</P>
 *
 * <P>This dialog consists of three text fields to enter the name of the
 * guard variable, the index range, and the optional guard expression.
 * Input validation is performed using an {@link ExpressionParser}.</P>
 *
 * @see net.sourceforge.waters.model.module.ForeachProxy ForeachProxy
 * @author Robi Malik
 */

public class ForeachEditorDialog
  extends JDialog
{
  //#########################################################################
  //# Static Invocation
  public static void showDialog(final ForeachSubject foreach,
                                final SelectionOwner panel,
                                final ComponentEditorPanel root)
  {
    final ModuleWindowInterface rroot = root.getModuleWindowInterface();
    new ForeachEditorDialog(rroot, panel, foreach);
  }



  //#########################################################################
  //# Constructors
  public ForeachEditorDialog(final ModuleWindowInterface root,
                             final SelectionOwner panel)
  {
    this(root, panel, null);
  }

  public ForeachEditorDialog(final ModuleWindowInterface root,
                             final SelectionOwner panel,
                             final ForeachSubject foreach)
  {
    super(root.getRootWindow());
    if (foreach == null) {
      setTitle("Creating new foreach block");
    } else {
      setTitle("Editing foreach block");
    }
    mRoot = root;
    mPanel = panel;
    mForeach = foreach;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mVariableInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());

    //set max size does not work!!!
    setMaximumSize(new Dimension(getWidth(), 182));
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the foreach block being edited by this dialog.
   */
  public ForeachSubject getEditedItem()
  {
    return mForeach;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final ForeachSubject template;
    if (mForeach == null) {
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
      template = mForeach;
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
    mVariableLabel = new JLabel("Variable:");
    final String oldName = template.getName();
    final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(oldName);
    final SimpleIdentifierInputHandler handler =
      new SimpleIdentifierInputHandler(ident, parser, true);
    mVariableInput = new SimpleExpressionInputCell(ident, handler);
    mVariableInput.addActionListener(commitHandler);
    mVariableInput.addSimpleDocumentListener(okEnablement);
    mVariableInput.setToolTipText("Enter the name of the index variable.");
    mRangeLabel = new JLabel("Range:");
    final SimpleExpressionProxy oldRange =
      mForeach == null ? null : template.getRange();
    mRangeInput =
      new SimpleExpressionInputCell(oldRange, Operator.TYPE_RANGE, parser, true);
    mRangeInput.addActionListener(commitHandler);
    mRangeInput.addSimpleDocumentListener(okEnablement);
    mRangeInput.setToolTipText
      ("Enter the index range, e.g., 1..10 or [a,b,c].");

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mVariableInput.setErrorDisplay(mErrorLabel);
    mRangeInput.setErrorDisplay(mErrorLabel);

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
    // mVariableLabel
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mVariableLabel, constraints);
    mMainPanel.add(mVariableLabel);
    // mVariableInput
    mVariableInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mVariableInput, constraints);
    mMainPanel.add(mVariableInput);
    // mRangeLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mRangeLabel, constraints);
    mMainPanel.add(mRangeLabel);
    // mRangeInput
    mRangeInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mRangeInput, constraints);
    mMainPanel.add(mRangeInput);

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
    final boolean enabled =
      mVariableInput.getText().length() > 0 &&
      mRangeInput.getText().length() > 0;
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
    } else if (mVariableInput.getValue() == null) {
      mVariableInput.requestFocusWithErrorMessage
        ("Please enter the name of the index variable.");
    } else if (mRangeInput.getValue() == null) {
      mRangeInput.requestFocusWithErrorMessage
        ("Please enter an expression for the range.");
    } else {
      final String name = mVariableInput.getText();
      final SimpleExpressionSubject range0 =
        (SimpleExpressionSubject) mRangeInput.getValue();
      final SimpleExpressionSubject range = makeUnique(range0);
      if (mForeach == null) {
        final ForeachSubject template = TEMPLATE.clone();
        template.setName(name);
        template.setRange(range);
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, mPanel, mRoot);
        mForeach = template;
        executeCommand(command);
      } else {
        final String oldname = mForeach.getName();
        final boolean namechange = !name.equals(oldname);
        final SimpleExpressionSubject oldrange = mForeach.getRange();
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
        final boolean rangechange = !eq.equals(range, oldrange);
        if (namechange || rangechange) {
          final ForeachSubject template = mForeach.clone();
          if (namechange) {
            template.setName(name);
          }
          if (rangechange) {
            template.setRange(range);
          }
          final Command command = new EditCommand(mForeach, template, mPanel);
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
  private JLabel mVariableLabel;
  private SimpleExpressionInputCell mVariableInput;
  private JLabel mRangeLabel;
  private SimpleExpressionInputCell mRangeInput;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private JButton mOkButton;

  // Created Item
  /**
   * <P>The foreach block subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited. If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is committed and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private ForeachSubject mForeach;
  private Object mInsertPosition;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final ForeachSubject TEMPLATE =
    new ForeachSubject("", new SimpleIdentifierSubject(""));
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);

}
