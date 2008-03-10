//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   SimpleComponentEditorDialog
//###########################################################################
//# $Id: SimpleComponentEditorDialog.java,v 1.4 2008-03-10 23:55:03 robi Exp $
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
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.renderer.GeometryAbsentException;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;

import net.sourceforge.waters.xsd.base.ComponentKind;


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
    final SimpleComponentSubject template =
      mComponent == null ? COMPONENT_TEMPLATE : mComponent;
    final ModuleContext context = mRoot.getModuleContext();
    final ExpressionParser parser = mRoot.getExpressionParser();
    final ActionListener commithandler = new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          commitDialog();
        }
      };
    final KeyListener keyhandler = new KeyAdapter() {
        public void keyTyped(final KeyEvent event)
        {
          if (event.getKeyChar() == '\n' && event.getModifiers() == 0) {
            commitDialog();
          }
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
    mNameInput.addKeyListener(keyhandler);
    mNameInput.setToolTipText("Enter automaton name, e.g., x or v[i]");
    mKindLabel = new JLabel("Kind:");
    mKindGroup = new ButtonGroup();
    mPlantButton =
      new IconRadioButton("Plant", IconLoader.ICON_PLANT, mKindGroup);
    mPropertyButton =
      new IconRadioButton("Property", IconLoader.ICON_PROPERTY, mKindGroup);
    mSpecButton =
      new IconRadioButton("Specification", IconLoader.ICON_SPEC, mKindGroup);
    mSupervisorButton =
      new IconRadioButton("Supervisor", IconLoader.ICON_SUPERVISOR,
                          mKindGroup);
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

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);

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
      if (mComponent == null) {
        final GraphSubject graph =
          new GraphSubject(deterministic, null, null, null);
        final SimpleComponentSubject template =
          new SimpleComponentSubject(ident, kind, graph);
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final Command command = new InsertCommand(template, panel);
        mComponent = template;
        mRoot.getUndoInterface().executeCommand(command);
        try {
          mRoot.showEditor(mComponent);
        } catch (final GeometryAbsentException exception) {
          throw new WatersRuntimeException(exception);
        }
      } else {
        final GraphSubject graph = mComponent.getGraph().clone();
        graph.setDeterministic(deterministic);
        final SimpleComponentSubject template =
          new SimpleComponentSubject(ident, kind, graph);
        if (!mComponent.equalsWithGeometry(template)) {
          final SelectionOwner panel = mRoot.getComponentsPanel();
          final Command command = new EditCommand(mComponent, template, panel);
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
      mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus();
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

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>The Waters component subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited.  If
   * a new component is being created, it is <CODE>null</CODE>
   * until the dialog is commited and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private SimpleComponentSubject mComponent;


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final GraphSubject GRAPH_TEMPLATE = new GraphSubject();
  private static final SimpleComponentSubject COMPONENT_TEMPLATE =
    new SimpleComponentSubject(new SimpleIdentifierSubject(""),
                               ComponentKind.SPEC,
                               GRAPH_TEMPLATE);

}
