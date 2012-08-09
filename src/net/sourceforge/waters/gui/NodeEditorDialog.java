//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleComponentEditorDialog
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.LabelGeometryProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.PointGeometryProxy;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;


public class NodeEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public NodeEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public NodeEditorDialog(final ModuleWindowInterface root,
                                     final SimpleNodeSubject node)
  {
    super(root.getRootWindow());
    if (node == null) {
      setTitle("Creating new simple node");
    } else {
      setTitle("Editing simple node '" + node.getName() + "'");
    }
    mRoot = root;
    mNode = node;
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
   * @return A reference to the node being edited by this dialog.
   */
  public SimpleNodeSubject getEditedItem()
  {
    return mNode;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final SimpleNodeSubject template;
    if (mNode == null) {
      template = NODE_TEMPLATE;
    } else {
      template = mNode;
    }
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
    final SimpleIdentifierSubject subject = new SimpleIdentifierSubject(oldname);
    final FormattedInputParser nameparser =
      new SimpleIdentifierInputParser(oldname, parser);
    mNameInput = new SimpleExpressionCell(subject, nameparser);

    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter node name, eg. \"IDLE\" or \"WORKING\"");

    // Attributes panel ...
    mAttributesPanel =
      new SimpleNodeAttributesPanel(template.getAttributes());

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
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
    // mNameLabel
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    mainlayout.setConstraints(mNameLabel, constraints);
    mMainPanel.add(mNameLabel);
    // mNameInput
    mNameInput.setColumns(20);
    constraints.gridx++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);

    // Attributes, error, and buttons panel do not need layouting.

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
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    layout.setConstraints(mAttributesPanel, constraints);
    contents.add(mAttributesPanel);
    constraints.weighty = 0.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
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
   * of the editor dialog.
   */
  public void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else {
      final Map<String,String> attribs = mAttributesPanel.getTableData();
        final ModuleEqualityVisitor eq =
          ModuleEqualityVisitor.getInstance(true);
        final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
        final PlainEventListProxy propositions = (PlainEventListProxy) cloner.getClone(mNode.getPropositions());
        final PointGeometryProxy pointGeometry = (PointGeometryProxy) cloner.getClone(mNode.getPointGeometry());
        final PointGeometryProxy initialArrowGeometry = (PointGeometryProxy) cloner.getClone(mNode.getInitialArrowGeometry());
        final LabelGeometryProxy labelGeometry = (LabelGeometryProxy) cloner.getClone(mNode.getLabelGeometry());

        final SimpleNodeSubject template =
          new SimpleNodeSubject(mNameInput.getText(), propositions, attribs,
                                mNode.isInitial(), pointGeometry,
                                initialArrowGeometry, labelGeometry);
        if (!eq.equals(mNode, template)) {
          final Command command = new EditCommand(mNode, template, null);
          mRoot.getUndoInterface().executeCommand(command);
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
      mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
  }


  //#########################################################################
  //# Inner Class SimpleNodeAttributesPanel
  private class SimpleNodeAttributesPanel extends AttributesPanel
  {

    //#######################################################################
    //# Constructor
    private SimpleNodeAttributesPanel(final Map<String,String> attribs)
    {
      super(NodeProxy.class, attribs);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.AttributesPanel
    boolean isInputLocked()
    {
      return NodeEditorDialog.this.isInputLocked();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private AttributesPanel mAttributesPanel;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  private final SimpleNodeSubject mNode;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final SimpleNodeSubject NODE_TEMPLATE =
                                                   new SimpleNodeSubject("");

}
