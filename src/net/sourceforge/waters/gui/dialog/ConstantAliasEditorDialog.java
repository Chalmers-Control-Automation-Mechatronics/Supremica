//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui.dialog
//# CLASS:   ConstantAliasEditorDialog
//###########################################################################
//# $Id$
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
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.ExpressionProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.ConstantAliasSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.module.ScopeKind;


/**
 * @author Carly Hona
 */

public class ConstantAliasEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public ConstantAliasEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public ConstantAliasEditorDialog(final ModuleWindowInterface root,
                                   final ConstantAliasSubject alias)
  {
    super(root.getRootWindow());
    if (alias == null) {
      setTitle("Creating new named constant");
    } else {
      setTitle("Editing named constant");
    }
    mRoot = root;
    mAlias = alias;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(MIN_SIZE);
    setMaximumSize(MAX_SIZE);
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the foreach component being edited by this dialog.
   */
  public ConstantAliasSubject getEditedItem()
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
    final ConstantAliasSubject template;
    if (mAlias == null) {
      try {
        template = TEMPLATE;
        final SelectionOwner panel = mRoot.getConstantAliasesPanel();
        final List<InsertInfo> inserts = panel.getInsertInfo(TRANSFERABLE);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = mAlias;
    }
    final ExpressionParser parser = mRoot.getExpressionParser();
    final ActionListener commithandler = new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent event)
        {
          commitDialog();
        }
      };

    // Main panel ...
    mMainPanel = new RaisedDialogPanel();
    mNameLabel = new JLabel("Name:");
    final String oldname = template.getName();
    final SimpleIdentifierSubject ident = new SimpleIdentifierSubject(oldname);
    final SimpleIdentifierInputParser nameparser =
      new SimpleIdentifierInputParser(ident, parser);
    mNameInput = new SimpleExpressionCell(ident, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter the name");
    mExpressionLabel = new JLabel("Expression:");
    final SimpleExpressionProxy oldexp =
      mAlias == null ? null : (SimpleExpressionProxy)template.getExpression();
    mExpressionInput =
      new SimpleExpressionCell(oldexp, Operator.TYPE_ANY, parser);
    mExpressionInput.addActionListener(commithandler);
    mExpressionInput.setToolTipText("Enter the expression");

    final ScopeKind scope = template.getScope();
    mHasParameterCheckBox = new JCheckBox("Parameter");
    mHasParameterCheckBox.setRequestFocusEnabled(false);
    mHasParameterCheckBox.setSelected(scope != ScopeKind.LOCAL);
    mHasParameterCheckBox.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(final ActionEvent event)
        {
          updateRequiredEnabled();
        }
      });

    mIsRequiredCheckBox = new JCheckBox("Required");
    mIsRequiredCheckBox.setRequestFocusEnabled(false);
    mIsRequiredCheckBox.setSelected(scope != ScopeKind.OPTIONAL_PARAMETER);
    updateRequiredEnabled();

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
        @Override
        public void actionPerformed(final ActionEvent event)
        {
          dispose();
        }
      });
    mButtonsPanel.add(cancelButton);

    final JRootPane root = getRootPane();
    root.setDefaultButton(okButton);
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
    mainlayout.setConstraints(mNameLabel, constraints);
    mMainPanel.add(mNameLabel);
    // mVariableInput
    mNameInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);
    // mRangeLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mExpressionLabel, constraints);
    mMainPanel.add(mExpressionLabel);
    // mRangeInput
    mNameInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 1;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mExpressionInput, constraints);
    mMainPanel.add(mExpressionInput);

    constraints.gridy++;
    constraints.weightx = 0.0;
    mainlayout.setConstraints(mHasParameterCheckBox, constraints);
    mMainPanel.add(mHasParameterCheckBox);

    constraints.gridy++;
    mainlayout.setConstraints(mIsRequiredCheckBox, constraints);
    mMainPanel.add(mIsRequiredCheckBox);
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
    } else if (!mExpressionInput.shouldYieldFocus()) {
      mExpressionInput.requestFocusInWindow();
    } else {
      final SimpleExpressionSubject exp0 =
        (SimpleExpressionSubject) mExpressionInput.getValue();
      final SimpleExpressionSubject exp = makeUnique(exp0);
      final SelectionOwner panel = mRoot.getConstantAliasesPanel();

      final ScopeKind scope;
      if (!mHasParameterCheckBox.isSelected()) {
        scope = ScopeKind.LOCAL;
      } else if (mIsRequiredCheckBox.isSelected()) {
        scope = ScopeKind.REQUIRED_PARAMETER;
      } else {
        scope = ScopeKind.OPTIONAL_PARAMETER;
      }

      if (mAlias == null) {
        final IdentifierSubject ident =
          (IdentifierSubject) mNameInput.getValue();
        final ConstantAliasSubject template =
          new ConstantAliasSubject(ident, exp);
        template.setScope(scope);
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, panel, mRoot);
        mAlias = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        final String name = mNameInput.getText();
        final String oldname = mAlias.getName();
        final boolean nameChange = !name.equals(oldname);
        final ExpressionProxy oldexp = mAlias.getExpression();
        final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
        final boolean expChange = !eq.equals(exp, oldexp);
        final boolean scopeChange = scope != mAlias.getScope();
        if (nameChange || expChange || scopeChange) {
          final ConstantAliasSubject template = mAlias.clone();
          if (nameChange) {
            final IdentifierSubject ident =
              (IdentifierSubject) mNameInput.getValue();
            template.setIdentifier(ident);
          }
          if (expChange) {
            template.setExpression(exp);
          }
          template.setScope(scope);
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
    return
      mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus() ||
      mExpressionInput.isFocusOwner() && !mExpressionInput.shouldYieldFocus();
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

  /**
   * Enables or disables the 'required' checkbox.
   * This method is attached to action listeners in response to the
   * selection or deselection of the 'parameter' checkbox.
   */
  private void updateRequiredEnabled()
  {
    final boolean enable = mHasParameterCheckBox.isSelected();
    mIsRequiredCheckBox.setEnabled(enable);
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
  private JCheckBox mHasParameterCheckBox;
  private JCheckBox mIsRequiredCheckBox;
  private SimpleExpressionCell mExpressionInput;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>The Waters component subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited. If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is committed and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private ConstantAliasSubject mAlias;
  private Object mInsertPosition;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final ConstantAliasSubject TEMPLATE =
    new ConstantAliasSubject(new SimpleIdentifierSubject(""), new SimpleIdentifierSubject(""));
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(TEMPLATE);
  private static Dimension MIN_SIZE = new Dimension(333, 213);
  private static Dimension MAX_SIZE = new Dimension(433, 213);
}
