//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2018 Robi Malik
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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.subject.base.Subject;
import net.sourceforge.waters.subject.base.UndoInfo;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.LabelBlockSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * @author Robi Malik
 */

public class SimpleComponentEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public SimpleComponentEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public SimpleComponentEditorDialog(final ModuleWindowInterface root,
                                     final SimpleComponentSubject comp)
  {
    super(root.getRootWindow());
    if (comp == null) {
      setTitle("Creating new automaton");
    } else {
      final IdentifierSubject ident = comp.getIdentifier();
      setTitle("Editing automaton '" + ident.toString() + "'");
    }
    mRoot = root;
    mComponent = comp;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the component being edited by this dialog.
   */
  public SimpleComponentSubject getEditedItem()
  {
    return mComponent;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final SimpleComponentSubject template;
    if (mComponent == null) {
      try {
        template = COMPONENT_TEMPLATE;
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final List<InsertInfo> inserts = panel.getInsertInfo(TRANSFERABLE);
        final InsertInfo insert = inserts.get(0);
        mInsertPosition = insert.getInsertPosition();
      } catch (final IOException exception) {
        throw new WatersRuntimeException(exception);
      } catch (final UnsupportedFlavorException exception) {
        throw new WatersRuntimeException(exception);
      }
    } else {
      template = mComponent;
    }
    final ModuleContext context = mRoot.getModuleContext();
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
    final IdentifierProxy oldname = template.getIdentifier();
    final FormattedInputParser nameparser =
      new ComponentNameInputParser(oldname, context, parser);
    mNameInput = new SimpleExpressionCell(oldname, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter automaton name, e.g., x or v[i]");
    mKindLabel = new JLabel("Kind:");
    mKindGroup = new ButtonGroup();
    mPlantButton =
      new IconRadioButton("Plant", IconAndFontLoader.ICON_PLANT,
                          mKindGroup, 'p');
    mPropertyButton =
      new IconRadioButton("Property", IconAndFontLoader.ICON_PROPERTY,
                          mKindGroup, 'o');
    mSpecButton =
      new IconRadioButton("Specification", IconAndFontLoader.ICON_SPEC,
                          mKindGroup, 's');
    mSupervisorButton =
      new IconRadioButton("Supervisor", IconAndFontLoader.ICON_SUPERVISOR,
                          mKindGroup, 'u');
    switch (template.getKind()) {
    case PLANT:
      mPlantButton.setSelected(true);
      break;
    case PROPERTY:
      mPropertyButton.setSelected(true);
      break;
    case SPEC:
      mSpecButton.setSelected(true);
      break;
    case SUPERVISOR:
      mSupervisorButton.setSelected(true);
      break;
    }
    mDeterministicLabel = new JLabel("Deterministic:");
    final GraphSubject graph = template.getGraph();
    final boolean deterministic = graph.isDeterministic();
    mDeterministicButton = new JCheckBox((String) null, deterministic);
    mDeterministicButton.setRequestFocusEnabled(false);

    // Attributes panel ...
    mAttributesPanel =
      new SimpleComponentAttributesPanel(template.getAttributes());

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
    constraints.weighty = 0.0;
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
    // mKindLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mKindLabel, constraints);
    mMainPanel.add(mKindLabel);
    // mPlantButton
    constraints.gridx++;
    constraints.weightx = 1.0;
    mainlayout.setConstraints(mPlantButton, constraints);
    mMainPanel.add(mPlantButton);
    // mSpecButton
    constraints.gridx++;
    mainlayout.setConstraints(mSpecButton, constraints);
    mMainPanel.add(mSpecButton);
    // mPropertyButton
    constraints.gridx--;
    constraints.gridy++;
    mainlayout.setConstraints(mPropertyButton, constraints);
    mMainPanel.add(mPropertyButton);
    // mSupervisorButton
    constraints.gridx++;
    mainlayout.setConstraints(mSupervisorButton, constraints);
    mMainPanel.add(mSupervisorButton);
    // mDeterministicLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mDeterministicLabel, constraints);
    mMainPanel.add(mDeterministicLabel);
    // mDeterministicButton
    constraints.gridx++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mDeterministicButton, constraints);
    mMainPanel.add(mDeterministicButton);

    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.NONE;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    final JLabel attributesLabel = new JLabel(AttributesPanel.LABEL_NAME);
    mainlayout.setConstraints(attributesLabel, constraints);
    mMainPanel.add(attributesLabel);

    constraints.gridx++;
    constraints.gridwidth = 2;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.BOTH;
    mainlayout.setConstraints(mAttributesPanel, constraints);
    mMainPanel.add(mAttributesPanel);
    // Attributes, error, and buttons panel do not need layouting.

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
   * This method is attached to the action listener of the 'OK' button
   * of the event editor dialog.
   */
  public void commitDialog()
  {
    if (isInputLocked()) {
      // There is invalid input and an error message has been displayed.
      // Do not try to commit.
    } else {
      // Read the data from the dialog ...
      final IdentifierSubject ident0 =
        (IdentifierSubject) mNameInput.getValue();
      final IdentifierSubject ident =
        ident0.getParent() == null ? ident0 : ident0.clone();
      final ComponentKind kind;
      if (mPlantButton.isSelected()) {
        kind = ComponentKind.PLANT;
      } else if (mPropertyButton.isSelected()) {
        kind = ComponentKind.PROPERTY;
      } else if (mSpecButton.isSelected()) {
        kind = ComponentKind.SPEC;
      } else if (mSupervisorButton.isSelected()) {
        kind = ComponentKind.SUPERVISOR;
      } else {
        throw new IllegalStateException("Component kind not selected!");
      }
      final boolean deterministic = mDeterministicButton.isSelected();
      final Map<String,String> attribs = mAttributesPanel.getTableData();
      final GraphSubject graph =
        new GraphSubject(deterministic, null, null, null);
      if (mComponent == null) {
        // Create new component and insert
        final SimpleComponentSubject template =
          new SimpleComponentSubject(ident, kind, graph, attribs);
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, panel, mRoot);
        mComponent = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else {
        // Modify existing component using data from dialog
        graph.setDeterministic(deterministic);
        final SimpleComponentSubject template =
          new SimpleComponentSubject(ident, kind, graph, attribs);
        final Set<Subject> boundary = new THashSet<Subject>(3);
        final LabelBlockSubject blocked =
          mComponent.getGraph().getBlockedEvents();
        if (blocked != null) {
          boundary.add(blocked);
        }
        boundary.add(mComponent.getGraph().getNodesModifiable());
        boundary.add(mComponent.getGraph().getEdgesModifiable());
        final UndoInfo info = mComponent.createUndoInfo(template, boundary);
        // Modify only if something has changed
        if (info != null) {
          final SelectionOwner panel = mRoot.getComponentsPanel();
          final Command command =
            new EditCommand(mComponent, info, panel, null);
          mRoot.getUndoInterface().executeCommand(command);
        }
      }
      // Close the dialog
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
   *         has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    return
      mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
  }


  //#########################################################################
  //# Inner Class SimpleComponentAttributesPanel
  private class SimpleComponentAttributesPanel extends AttributesPanel
  {

    //#######################################################################
    //# Constructor
    private SimpleComponentAttributesPanel(final Map<String,String> attribs)
    {
      super(SimpleComponentProxy.class, attribs);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.AttributesPanel
    @Override
    boolean isInputLocked()
    {
      return SimpleComponentEditorDialog.this.isInputLocked();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3597766255951309896L;

  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mDeterministicLabel;
  private JLabel mKindLabel;
  private ButtonGroup mKindGroup;
  private IconRadioButton mPlantButton;
  private IconRadioButton mPropertyButton;
  private IconRadioButton mSpecButton;
  private IconRadioButton mSupervisorButton;
  private JCheckBox mDeterministicButton;
  private AttributesPanel mAttributesPanel;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>The Waters component subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited.  If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is committed and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private SimpleComponentSubject mComponent;
  private Object mInsertPosition;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 5606299929823517586L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final GraphSubject GRAPH_TEMPLATE = new GraphSubject();
  private static final SimpleComponentSubject COMPONENT_TEMPLATE =
    new SimpleComponentSubject(new SimpleIdentifierSubject(""),
                               ComponentKind.PLANT,
                               GRAPH_TEMPLATE);
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(COMPONENT_TEMPLATE);

}
