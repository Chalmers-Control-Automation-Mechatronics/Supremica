//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2015 Robi Malik
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

import gnu.trove.set.hash.THashSet;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.GraphEditorPanel;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.language.ProxyNamer;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.ProxyTools;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.PlainEventListProxy;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.base.SetSubject;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.UndoInfo;
import net.sourceforge.waters.subject.module.BoxGeometrySubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.GroupNodeSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.NodeSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleNodeSubject;

import org.supremica.gui.ide.ModuleContainer;


/**
 * @author Carly Hona
 */

public class NodeEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public NodeEditorDialog(final ModuleContainer root,
                          final SelectionOwner panel,
                          final NodeSubject node)
  {
    super(root.getIDE());
    setTitle("Editing " + ProxyNamer.getItemClassName(node) + " '" +
             node.getName() + "'");
    mModuleContainer = root;
    mPanel = panel;
    mNode = node;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mModuleContainer.getIDE());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }

  //#########################################################################
  //# Static Invocation
  public static void showDialog(final ModuleContainer moduleContainer,
                                final GraphEditorPanel panel,
                                final NodeSubject node)
  {
    new NodeEditorDialog(moduleContainer, panel, node);
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the node being edited by this dialog.
   */
  public NodeSubject getEditedItem()
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
    final NodeSubject template = mNode;
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
    final SimpleIdentifierSubject subject = new SimpleIdentifierSubject(oldname);
    mNameInput = new SimpleExpressionCell(subject,  new NodeNameInputParser(subject));

    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter node name, eg. \"IDLE\" or \"WORKING\"");

    final ModuleProxyCloner cloner = ModuleSubjectFactory.getCloningInstance();
    mPropostionsPanel = new PropositionsPanel(mModuleContainer, (NodeSubject) cloner.getClone(mNode));

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
    final GridBagLayout mainlayout = new GridBagLayout();
    mMainPanel.setLayout(mainlayout);
    //constraints.weightx = 1.0;
    constraints.insets = INSETS;
    constraints.anchor = GridBagConstraints.NORTHWEST;

    //mNameLabel
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.weighty = 0.0;
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
    //mPropositionsLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.NONE;
    final JLabel propositionsLabel = new JLabel("Propositions:");
    mainlayout.setConstraints(propositionsLabel, constraints);
    mMainPanel.add(propositionsLabel);
    //mPropositionsPanel
    constraints.gridx++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.BOTH;
    mainlayout.setConstraints(mPropostionsPanel, constraints);
    mMainPanel.add(mPropostionsPanel);
    //mAttributesLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    final JLabel attributesLabel = new JLabel(AttributesPanel.LABEL_NAME);
    mainlayout.setConstraints(attributesLabel, constraints);
    mMainPanel.add(attributesLabel);
    //mAttributesPanel
    constraints.gridx++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.BOTH;
    constraints.insets = new Insets(0, 0, 0, 0);
    mainlayout.setConstraints(mAttributesPanel, constraints);
    mMainPanel.add(mAttributesPanel);

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

    //mErrorPanel
    constraints.weighty = 0.0;
    constraints.gridx = 0;
    constraints.gridy = GridBagConstraints.RELATIVE;
    constraints.gridwidth = 3;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mErrorPanel, constraints);
    contents.add(mErrorPanel);
    //mButtonsPanel
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
      try {
        mNameInput.commitEdit();
      } catch (final java.text.ParseException exception) {
        throw new WatersRuntimeException(exception);
      }
      final Map<String,String> attribs = mAttributesPanel.getTableData();
      final ModuleProxyCloner cloner =
        ModuleSubjectFactory.getCloningInstance();
      NodeSubject template = null;
      final PlainEventListProxy propositions =
        (PlainEventListProxy) cloner.getClone(mPropostionsPanel.getPropositions());
      final String name = mNameInput.getText();
      final Set<Subject> boundary = new THashSet<Subject>(5);
      if (mNode instanceof SimpleNodeSubject) {
        final SimpleNodeSubject node = (SimpleNodeSubject) mNode;
        template =
          new SimpleNodeSubject(name, propositions, attribs,
                                node.isInitial(), null, null, null);
        addToBoundary(boundary, node.getPointGeometry());
        addToBoundary(boundary, node.getInitialArrowGeometry());
        addToBoundary(boundary, node.getLabelGeometry());
      } else if (mNode instanceof GroupNodeSubject) {
        final GroupNodeSubject groupNode = (GroupNodeSubject) mNode;
        template = new GroupNodeSubject(name, propositions, attribs,
                                        null, null);
        final SetSubject<NodeSubject> immediateChildNodes =
          groupNode.getImmediateChildNodesModifiable();
        addToBoundary(boundary, immediateChildNodes);
        final BoxGeometrySubject geometry = groupNode.getGeometry();
        addToBoundary(boundary, geometry);
      } else {
        throw new IllegalStateException
          ("Unknown node class " + ProxyTools.getShortClassName(mNode));
      }
      final UndoInfo info = mNode.createUndoInfo(template, boundary);
      if (info != null) {
        final Command command = new EditCommand(mNode, info, mPanel, null);
        mModuleContainer.executeCommand(command);
      }
      dispose();
    }
  }

  private void addToBoundary(final Set<Subject> boundary, final Subject subject)
  {
    if (subject != null) {
      boundary.add(subject);
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
    @Override
    boolean isInputLocked()
    {
      return NodeEditorDialog.this.isInputLocked();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }
  //#########################################################################
  //# Inner Class StateNameInputParser
  private class NodeNameInputParser
    extends SimpleIdentifierInputParser
  {

    //#######################################################################
    //# Constructor
    private NodeNameInputParser(final SimpleIdentifierProxy oldident)
    {
      super(oldident, mModuleContainer.getExpressionParser());
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    @Override
    public SimpleIdentifierProxy parse(final String text) throws net.sourceforge.waters.model.expr.ParseException
    {
      final SimpleIdentifierProxy ident = super.parse(text);
      final String oldname = getOldName();
      if (!text.equals(oldname)) {
        final GraphSubject graph = mModuleContainer.getActiveEditorWindowInterface().getGraphEditorPanel().getGraph();
        if (graph.getNodesModifiable().containsName(text)) {
          throw new ParseException
          ("Node name '" + text + "' is already taken!", 0);
        }
      }
      return ident;
    }

  }

  //#########################################################################
  //# Data Members
  // Environment
  private final ModuleContainer mModuleContainer;
  private final SelectionOwner mPanel;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private PropositionsPanel mPropostionsPanel;
  private SimpleExpressionCell mNameInput;
  private AttributesPanel mAttributesPanel;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Dialog state
  private final NodeSubject mNode;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;
  private static final Insets INSETS = new Insets(2,4,2,4);

}
