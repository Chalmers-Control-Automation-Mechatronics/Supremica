//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ForeachComponentEditorDialog
//###########################################################################
//# $Id: ForeachComponentEditorDialog.java,v 1.3 2008-03-13 01:30:11 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ForeachComponentSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;

import net.sourceforge.waters.xsd.base.ComponentKind;


public class ForeachComponentEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public ForeachComponentEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public ForeachComponentEditorDialog(final ModuleWindowInterface root,
                                      final ForeachComponentSubject foreach)
  {
    super(root.getRootWindow());
    if (foreach == null) {
      setTitle("Creating new foreach block");
    } else {
      setTitle("Editing foreach block");
    }
    mRoot = root;
    mForeach = foreach;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mVariableInput.requestFocusInWindow();
    setVisible(true);
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the foreach component being edited by this dialog.
   */
  public ForeachComponentSubject getEditedItem()
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
    final ForeachComponentSubject template =
      mForeach == null ? TEMPLATE : mForeach;
    final ExpressionParser parser = mRoot.getExpressionParser();
    final ActionListener commithandler = new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          commitDialog();
        }
      };

    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mVariableLabel = new JLabel("Variable:");
    final String oldname = template.getName();
    final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(oldname);
    final FormattedInputParser nameparser =
      new SimpleIdentifierInputParser(ident, parser);
    mVariableInput = new SimpleExpressionCell(ident, nameparser);
    mVariableInput.addActionListener(commithandler);
    mVariableInput.setToolTipText("Enter the name of the index variable");
    mRangeLabel = new JLabel("Range:");
    final SimpleExpressionProxy oldrange = template.getRange();
    mRangeInput =
      new SimpleExpressionCell(oldrange, Operator.TYPE_RANGE, parser);
    mRangeInput.addActionListener(commithandler);
    mRangeInput.setToolTipText
      ("Enter the index range, e.g., 1..10 or {a,b,c}"); 
    mGuardLabel = new JLabel("Guard:");
    final SimpleExpressionProxy oldguard = template.getGuard();
    mGuardInput =
      new SimpleExpressionCell(oldguard, Operator.TYPE_BOOLEAN, parser);
    mGuardInput.setAllowNull(true);
    mGuardInput.addActionListener(commithandler);
    mGuardInput.setToolTipText("Optionally enter a Boolean expression"); 

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mVariableInput.setErrorDisplay(mErrorLabel);
    mRangeInput.setErrorDisplay(mErrorLabel);
    mGuardInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    final LayoutManager buttonsLayout = new GridLayout(2, 1);
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);
    final JButton cancelButton = new JButton("Cancel");
    cancelButton.setRequestFocusEnabled(false);
    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          dispose();
        }
      });
    mButtonsPanel.add(cancelButton);
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
    mVariableInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mRangeInput, constraints);
    mMainPanel.add(mRangeInput);
    // mGuardLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mGuardLabel, constraints);
    mMainPanel.add(mGuardLabel);
    // mGuardInput
    mVariableInput.setColumns(20);
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
  /**
   * Commits the contents of this dialog to the model.
   * This method is attached to action listener of the 'OK' button
   * of the event editor dialog.
   */
  public void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else {
      final String name = mVariableInput.getText();
      final SimpleExpressionSubject range =
        (SimpleExpressionSubject) mRangeInput.getValue();
      final SimpleExpressionSubject guard =
        (SimpleExpressionSubject) mGuardInput.getValue();
      if (mForeach == null) {
        final ForeachComponentSubject template =
          new ForeachComponentSubject(name, range, guard, null);
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final Command command = new InsertCommand(template, panel);
        mForeach = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        final String oldname = mForeach.getName();
        final boolean namechange = !name.equals(oldname);
        final SimpleExpressionSubject oldrange = mForeach.getRange();
        final boolean rangechange =
          !ProxyTools.equalsWithGeometry(range, oldrange);
        final SimpleExpressionSubject oldguard = mForeach.getGuard();
        final boolean guardchange =
          !ProxyTools.equalsWithGeometry(guard, oldguard);
        if (namechange || rangechange || guardchange) {
          final ForeachComponentSubject template = mForeach.clone();
          if (namechange) {
            template.setName(name);
          }
          if (rangechange) {
            template.setRange(range);
          }
          if (guardchange) {
            template.setGuard(guard);
          }
          final SelectionOwner panel = mRoot.getComponentsPanel();
          final Command command = new EditCommand(mForeach, template, panel);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      dispose();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether it is unsafe the current input to commit the currently
   * edited text field. If this method returns <CODE>true</CODE>, it is
   * unsafe to commit the current dialog contents, and shifting the focus
   * is to be avoided.
   * @return <CODE>true</CODE> if the component currently owning the focus
   *         is to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    return
      mVariableInput.isFocusOwner() && !mVariableInput.shouldYieldFocus() ||
      mRangeInput.isFocusOwner() && !mRangeInput.shouldYieldFocus() ||
      mGuardInput.isFocusOwner() && !mGuardInput.shouldYieldFocus();
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mVariableLabel;
  private SimpleExpressionCell mVariableInput;
  private JLabel mRangeLabel;
  private SimpleExpressionCell mRangeInput;
  private JLabel mGuardLabel;
  private SimpleExpressionCell mGuardInput;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>The Waters component subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited. If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is commited and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private ForeachComponentSubject mForeach;


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final ForeachComponentSubject TEMPLATE =
    new ForeachComponentSubject("", new SimpleIdentifierSubject(""));

}
