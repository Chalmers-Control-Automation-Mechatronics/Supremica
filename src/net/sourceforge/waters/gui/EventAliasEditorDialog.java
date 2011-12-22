//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   ForeachComponentEditorDialog
//###########################################################################
//# $Id$
//###########################################################################

package net.sourceforge.waters.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.EventAliasSubject;
import net.sourceforge.waters.subject.module.ExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.PlainEventListSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;


public class EventAliasEditorDialog extends JDialog
{

  //#########################################################################
  //# Constructors
  public EventAliasEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public EventAliasEditorDialog(final ModuleWindowInterface root,
                                final EventAliasSubject alias)
  {
    super(root.getRootWindow());
    if (alias == null) {
      setTitle("Creating new Event Alias");
    } else {
      setTitle("Editing Event Alias");
    }
    mRoot = root;
    mAlias = alias;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
  }

  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   *
   * @return A reference to the foreach component being edited by this dialog.
   */
  public EventAliasSubject getEditedItem()
  {
    return mAlias;
  }

  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final EventAliasSubject template = mAlias == null ? TEMPLATE : mAlias;
    final ExpressionParser parser = mRoot.getExpressionParser();
    final ActionListener commithandler = new ActionListener() {
      public void actionPerformed(final ActionEvent event)
      {
        commitDialog();
      }
    };

    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mNameLabel = new JLabel("Name:");
    final String oldname = template.getName();
    final SimpleIdentifierSubject ident =
      new SimpleIdentifierSubject(oldname);
    final SimpleIdentifierInputParser nameparser =
      new SimpleIdentifierInputParser(ident, parser);
    mNameInput = new SimpleExpressionCell(ident, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter the name");
    mExpressionLabel = new JLabel("Expression:");
    SimpleExpressionProxy oldexp = null;
    if (mAlias != null
        && mAlias.getExpression() instanceof SimpleExpressionProxy) {
      oldexp = (SimpleExpressionProxy) mAlias.getExpression();
    }
    mExpressionInput =
      new SimpleExpressionCell(oldexp, Operator.TYPE_NAME, parser);
    mExpressionInput.addActionListener(commithandler);
    mExpressionInput.setToolTipText("Enter the expression");
    mExpressionInput.setAllowNull(false);

    final ExpressionProxy exp = template.getExpression();
    mIsSimpleExpCheckBox = new JCheckBox("Use Simple Expression");
    mIsSimpleExpCheckBox.setRequestFocusEnabled(false);
    mIsSimpleExpCheckBox.setSelected(exp instanceof SimpleExpressionProxy);
    mIsSimpleExpCheckBox.addActionListener(new ActionListener() {
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

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
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
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;

    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);

    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mNameLabel, constraints);
    mMainPanel.add(mNameLabel);

    mNameInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);
    mNameInput.setColumns(20);

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
  public void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mIsSimpleExpCheckBox.isSelected()) {
      final IdentifierSubject name =
        (IdentifierSubject) mNameInput.getValue();
      final SimpleExpressionSubject exp0 =
        (SimpleExpressionSubject) mExpressionInput.getValue();
      final SimpleExpressionSubject exp = makeUnique(exp0);
      final SelectionOwner panel = mRoot.getEventAliasesPanel();
      if (mAlias == null) {
        final EventAliasSubject template = new EventAliasSubject(name, exp);
        final Command command = new InsertCommand(template, panel, mRoot);
        mAlias = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        final String oldname = mAlias.getName();
        final boolean namechange = !name.equals(oldname);
        final ExpressionProxy oldExp = mAlias.getExpression();

        final ModuleEqualityVisitor eq =
          ModuleEqualityVisitor.getInstance(true);
        final boolean expchange = !eq.equals(exp, oldExp);
        if (namechange || expchange) {
          final EventAliasSubject template = mAlias.clone();
          if (namechange) {
            template.setIdentifier(name);
          }
          if (expchange) {
            template.setExpression(exp);
          }
          final Command command = new EditCommand(mAlias, template, panel);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      dispose();
    } else if (!mIsSimpleExpCheckBox.isSelected()) {
      final IdentifierSubject name =
        (IdentifierSubject) mNameInput.getValue();
      final SelectionOwner panel = mRoot.getEventAliasesPanel();
      if (mAlias == null) {
        final EventAliasSubject template =
          new EventAliasSubject(name, new PlainEventListSubject());
        final Command command = new InsertCommand(template, panel, mRoot);
        mAlias = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        final String oldname = mAlias.getName();
        final boolean namechange = !name.equals(oldname);
        boolean expchange = false;
        ExpressionSubject exp = mAlias.getExpression();
        if (exp instanceof SimpleExpressionProxy) {
          exp = new PlainEventListSubject();
          expchange = true;
        }
        if (namechange || expchange) {
          final EventAliasSubject template = mAlias.clone();
          if (namechange) {
            template.setIdentifier(name);
          }
          if (expchange) {
            template.setExpression(exp);
          }
          final Command command = new EditCommand(mAlias, template, panel);
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
   *
   * @return <CODE>true</CODE> if the component currently owning the focus is
   *         to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus()
           || mExpressionInput.isFocusOwner()
           && !mExpressionInput.shouldYieldFocus();
  }

  private SimpleExpressionSubject makeUnique(final SimpleExpressionSubject subject)
  {
    if (subject == null || subject.getParent() == null) {
      return subject;
    } else {
      return subject.clone();
    }
  }

  /**
   * Enables or disables the 'required' checkbox. This method is attached to
   * action listeners in response to the selection or deselection of the
   * 'parameter' checkbox.
   */
  private void updateExpressionEnabled()
  {
    final boolean enable = mIsSimpleExpCheckBox.isSelected();
    mExpressionInput.setEnabled(enable);
    mExpressionLabel.setEnabled(enable);
    if(!enable){
      mErrorLabel.clearDisplay();
    }
  }

  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mExpressionLabel;
  private SimpleExpressionCell mExpressionInput;
  private JCheckBox mIsSimpleExpCheckBox;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  private EventAliasSubject mAlias;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final EventAliasSubject TEMPLATE =
    new EventAliasSubject(new SimpleIdentifierSubject(""),
                          new SimpleIdentifierSubject(""));

}
