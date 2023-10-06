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
import javax.swing.event.DocumentEvent;

import net.sourceforge.waters.gui.analyzer.AutomataTableModel;
import net.sourceforge.waters.gui.analyzer.WatersAnalyzerPanel;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.OperatorTable;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleProxyFactory;
import net.sourceforge.waters.model.module.SimpleComponentProxy;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;
import net.sourceforge.waters.plain.module.ModuleElementFactory;

import org.supremica.gui.ide.IDE;


/**
 * The dialog to edit the properties of an automaton ({@link AutomatonProxy})
 * in the Waters Analyser. The user can change the name, kind, and attributes
 * of an existing automaton.
 *
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
    final ModuleProxyFactory factory = ModuleElementFactory.getInstance();
    final OperatorTable optable = CompilerOperatorTable.getInstance();
    final ExpressionParser parser = new ExpressionParser(factory, optable);
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
    IdentifierProxy oldName = null;
    try {
      oldName = parser.parseIdentifier(mAutomaton.getName());
    } catch (final ParseException exception) {
      oldName = factory.createSimpleIdentifierProxy(mAutomaton.getName());
    }
    final FormattedInputHandler<IdentifierProxy> handler =
      new AutomatonNameInputHandler(oldName, mAnalyzerPanel, parser, true, true);
    mNameInput = new SimpleExpressionInputCell(oldName, handler);
    mNameInput.addActionListener(commitHandler);
    mNameInput.addSimpleDocumentListener(okEnablement);
    mNameInput.setToolTipText("Enter automaton name.");
    mKindLabel = new JLabel("Kind:");
    final ButtonGroup group = new ButtonGroup();
    mPlantButton = new IconRadioButton<>
      (ComponentKind.PLANT, "Plant",
       IconAndFontLoader.ICON_PLANT, group, 'p');
    mPropertyButton = new IconRadioButton<>
      (ComponentKind.PROPERTY, "Property",
       IconAndFontLoader.ICON_PROPERTY, group, 'o');
    mSpecButton = new IconRadioButton<>
      (ComponentKind.SPEC, "Specification",
       IconAndFontLoader.ICON_SPEC, group, 's');
    mSupervisorButton = new IconRadioButton<>
      (ComponentKind.SUPERVISOR, "Supervisor",
       IconAndFontLoader.ICON_SUPERVISOR, group, 'u');
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

    // Attributes panel ...
    mAttributesPanel =
      new AutomatonAttributesPanel(mAutomaton.getAttributes());

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);

    // Buttons panel ...
    mButtonsPanel = new JPanel();
    mOkButton = new JButton("OK");
    mOkButton.setRequestFocusEnabled(false);
    mOkButton.addActionListener(commitHandler);
    mButtonsPanel.add(mOkButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    mButtonsPanel.add(cancelButton);
    updateOkButtonStatus();

    final JRootPane root = getRootPane();
    root.setDefaultButton(mOkButton);
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
  private void updateOkButtonStatus()
  {
    final boolean enabled = mNameInput.getText().length() > 0;
    mOkButton.setEnabled(enabled);
  }

  /**
   * Commits the contents of this dialog to the model. This method is attached
   * to the action listener of the 'OK' button of the event editor dialog.
   */
  private void commitDialog()
  {
    final ProductDESProxyFactory factory =
      ProductDESElementFactory.getInstance();
    if (isInputLocked()) {
      // There is invalid input and an error message has been displayed.
      // Do not try to commit.
    } else if (mNameInput.getValue() == null) {
      mNameInput.requestFocusWithErrorMessage("Please enter an automaton name.");
    } else {
      // Read the data from the dialog ...
      final String name = mNameInput.getText();
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
      if (!attribs.equals(autAttribs) ||
          !kind.equals(mAutomaton.getKind()) ||
          !name.equals(mAutomaton.getName())) {
        final AutomatonProxy newAut =
          factory.createAutomatonProxy(name, kind,
                                       mAutomaton.getEvents(),
                                       mAutomaton.getStates(),
                                       mAutomaton.getTransitions(),
                                       attribs);
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
    final IDE ide = mAnalyzerPanel.getModuleContainer().getIDE();
    final FocusTracker tracker = ide.getFocusTracker();
    return !tracker.shouldYieldFocus(this);
  }


  //#########################################################################
  //# Inner Class SimpleComponentAttributesPanel
  private class AutomatonAttributesPanel extends AttributesPanel
  {
    //#######################################################################
    //# Constructor
    private AutomatonAttributesPanel(final Map<String,String> attribs)
    {
      super(SimpleComponentProxy.class, attribs);
    }

    //#######################################################################
    //# Overrides for net.sourceforge.waters.gui.AttributesPanel
    @Override
    boolean isInputLocked()
    {
      return AutomatonPropertiesDialog.this.isInputLocked();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3597766255951309897L;
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final WatersAnalyzerPanel mAnalyzerPanel;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionInputCell mNameInput;
  private JLabel mKindLabel;
  private IconRadioButton<ComponentKind> mPlantButton;
  private IconRadioButton<ComponentKind> mPropertyButton;
  private IconRadioButton<ComponentKind> mSpecButton;
  private IconRadioButton<ComponentKind> mSupervisorButton;
  private AttributesPanel mAttributesPanel;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private JButton mOkButton;

  // Created Item
  /**
   * <P>The Waters Automaton edited by this dialog.</P>
   *
   * <P>The edited automaton is stored only in the dialog. Changes are only
   * committed to the original automaton when the OK button is pressed.</P>
   */
  private final AutomatonProxy mAutomaton;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 6159733639861131531L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
