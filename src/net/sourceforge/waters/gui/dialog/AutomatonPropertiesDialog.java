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

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.xsd.base.ComponentKind;


/**
 * @author George Hewlett, Carly Hona
 */
public class AutomatonPropertiesDialog extends JDialog
{

  //#######################################################################
  //# Constructor
  public AutomatonPropertiesDialog(final WatersAnalyzerPanel panel,
                                   final AutomatonProxy aut)
  {
    mAutomaton = aut;
    mAnalyzerPanel = panel;
    setTitle("Editing automaton '" + aut.getName() + "'");
    createComponents();
    layoutComponents();
    setLocationRelativeTo(panel.getTopLevelAncestor());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    setMinimumSize(getSize());
  }

  //#########################################################################
  //# Access to Edited Item
  /**
   * Gets the automaton edited by this dialog.
   *
   * @return A reference to the automaton being edited by this dialog.
   */
  public AutomatonProxy getEditedItem()
  {
    return mAutomaton;
  }

  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    // TODO Make parser
    final ExpressionParser parser = null;
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
    /*
    IdentifierProxy oldname = null;
    try {
      oldname = parser.parseIdentifier(mAutomaton.getName());
    } catch (final ParseException exception) {
      exception.printStackTrace();
    }
    mNameInput = new SimpleExpressionCell(oldname, Operator.TYPE_NAME, parser);
    */
    mNameInput = new SimpleExpressionCell(Operator.TYPE_NAME, parser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter automaton name, e.g., x or v[i]");
    mKindLabel = new JLabel("Kind:");
    mKindGroup = new ButtonGroup();
    mPlantButton = new IconRadioButton("Plant", IconAndFontLoader.ICON_PLANT,
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
    switch (mAutomaton.getKind()) {
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
   * Fill the panels and layout all buttons and components. It is assumed that
   * all needed components have been created by a call to
   * {@link #createComponents()} before.
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
   * Commits the contents of this dialog to the model. This method is attached
   * to the action listener of the 'OK' button of the event editor dialog.
   */
  public void commitDialog()
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
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
      final Map<String,String> attribs = mAttributesPanel.getTableData();
      final Map<String,String> autAttribs = mAutomaton.getAttributes();
      if (!attribs.equals(autAttribs) || !kind.equals(mAutomaton.getKind())
          || !ident.getPlainText().equals(mAutomaton.getName())) {
        final AutomatonProxy newAut =
          factory.createAutomatonProxy(ident.getPlainText(), kind,
                                       mAutomaton.getEvents(),
                                       mAutomaton.getStates(),
                                       mAutomaton.getTransitions(),
                                       mAutomaton.getAttributes());

        final AutomataTableModel model =
          mAnalyzerPanel.getAutomataTableModel();
        model.replaceAutomaton(mAutomaton, newAut);
      }
      // Close the dialog
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
   * @return <CODE>true</CODE> if the component currently owning the focus has
   *         been found to contain invalid information, <CODE>false</CODE>
   *         otherwise.
   */
  private boolean isInputLocked()
  {
    return mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
  }

  //#########################################################################
  //# Data Members
  // Dialog state
  private final WatersAnalyzerPanel mAnalyzerPanel;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mKindLabel;
  private ButtonGroup mKindGroup;
  private IconRadioButton mPlantButton;
  private IconRadioButton mPropertyButton;
  private IconRadioButton mSpecButton;
  private IconRadioButton mSupervisorButton;
  private AttributesPanel mAttributesPanel;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>
   * The Waters Automaton edited by this dialog.
   * </P>
   *
   * <P>
   * The edited automaton is stored only in the dialog. Changes are only
   * committed to the original automaton when the OK button is pressed.
   * </P>
   */
  private final AutomatonProxy mAutomaton;

  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
