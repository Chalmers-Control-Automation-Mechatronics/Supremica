//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   VariableEditorDialog
//###########################################################################
//# $Id: VariableEditorDialog.java,v 1.4 2007-11-21 23:42:26 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.CreateVariableComponentCommand;
import net.sourceforge.waters.gui.command.EditVariableComponentCommand;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.subject.module.VariableMarkingSubject;

import org.supremica.properties.Config;


public class VariableEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructors
  public VariableEditorDialog(final ModuleWindowInterface root)
  {
    this(root, null);
  }

  public VariableEditorDialog(final ModuleWindowInterface root,
                              final VariableComponentSubject var)
  {
    super(root.getRootWindow());
    if (var == null) {
      setTitle("Creating new variable");
    } else {
      final IdentifierSubject ident = var.getIdentifier();
      setTitle("Editing variable '" + ident.toString() + "'");
    }
    mRoot = root;
    mVariable = var;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    updateListControlEnabled();
    mNameInput.requestFocusInWindow();
    setVisible(true);
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the variable being edited by this dialog.
   */
  public VariableComponentSubject getEditedItem()
  {
    return mVariable;
  }


  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components.
   */
  private void createComponents()
  {
    final VariableComponentSubject template =
      mVariable == null ? VARIABLE_TEMPLATE : mVariable;
    final ExpressionParser parser = getExpressionParser();
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
    mNameInput = new SimpleExpressionCell
      (template.getIdentifier(), Operator.TYPE_NAME, parser);
    mNameInput.addActionListener(commithandler);
    mNameInput.addKeyListener(keyhandler);
    mNameInput.setToolTipText("Enter variable name, e.g., x or v[i]");
    mTypeLabel = new JLabel("Type:");
    mTypeInput = new SimpleExpressionCell
      (template.getType(), Operator.TYPE_RANGE, parser);
    mTypeInput.addActionListener(commithandler);
    mTypeInput.addKeyListener(keyhandler);
    mTypeInput.setToolTipText("Enter type expression, e.g., 0..8 or {on,off}");
    mDeterministicLabel = new JLabel("Deterministic:");
    mDeterministicButton =
      new JCheckBox((String) null, template.isDeterministic());
    mDeterministicButton.setRequestFocusEnabled(false);
    mInitialLabel = new JLabel("Initial:");
    mInitialInput = new SimpleExpressionCell
      (template.getInitialStatePredicate(), Operator.TYPE_ARITHMETIC, parser);
    mInitialInput.addActionListener(commithandler);
    mInitialInput.addKeyListener(keyhandler);
    mInitialInput.setToolTipText("Enter initial state, e.g., 0 or on");

    // Error panel ...
    mErrorPanel = new RaisedDialogPanel();
    mErrorLabel = new ErrorLabel();
    mErrorPanel.add(mErrorLabel);
    mNameInput.setErrorDisplay(mErrorLabel);
    mTypeInput.setErrorDisplay(mErrorLabel);
    mInitialInput.setErrorDisplay(mErrorLabel);

    // Markings panel ...
    mMarkingsPanel = new RaisedDialogPanel();
    final List<VariableMarkingSubject> markings =
      template.getVariableMarkingsModifiable();
    final List<VariableMarkingSubject> copy =
      new ArrayList<VariableMarkingSubject>(markings);
    mMarkingsModel = new VariableMarkingTableModel(copy);
    mMarkingsTable = new JTable(mMarkingsModel);
    mMarkingsTable.setTableHeader(null);
    mMarkingsTable.setShowGrid(false);
    mMarkingsTable.setSurrendersFocusOnKeystroke(true);
    mMarkingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    mMarkingsTable.setFillsViewportHeight(true);
    final Dimension minsize = new Dimension(0, 0);
    mMarkingsTable.setPreferredScrollableViewportSize(minsize);
    mMarkingsTable.setMinimumSize(minsize);
    mMarkingsTable.setRowSelectionAllowed(true);
    final TableColumnModel model = mMarkingsTable.getColumnModel();
    model.getColumn(0).setPreferredWidth(32);
    model.getColumn(0).setMaxWidth(32);
    model.getColumn(1).setPreferredWidth(160);
    model.getColumn(2).setPreferredWidth(160);
    final Set<AWTKeyStroke> forward = mNameInput.getFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    mMarkingsTable.setFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
    final Set<AWTKeyStroke> backward = mNameInput.getFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    mMarkingsTable.setFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
    final TableCellEditor propeditor =
      new SimpleExpressionEditor(Operator.TYPE_NAME, parser, mErrorLabel);
    mMarkingsTable.setDefaultEditor(IdentifierSubject.class, propeditor);
    propeditor.addCellEditorListener(mMarkingsModel);
    final TableCellEditor prededitor =
      new SimpleExpressionEditor(Operator.TYPE_BOOLEAN, parser, mErrorLabel);
    mMarkingsTable.setDefaultEditor(SimpleExpressionSubject.class, prededitor);
    prededitor.addCellEditorListener(mMarkingsModel);
    final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
    selmodel.addListSelectionListener(new ListSelectionListener() {
        public void valueChanged(final ListSelectionEvent event)
        {
          updateListControlEnabled();
        }
      });
    mMarkingsTable.addMouseListener(new MouseAdapter() {
        public void mouseClicked(final MouseEvent event)
        {
          handleMarkingsTableClick(event);
        }
      });
    mAddButton = new JButton("Add");
    mAddButton.setRequestFocusEnabled(false);
    mAddButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          addMarking();
        }
      });
    mRemoveButton = new JButton("Remove");
    mRemoveButton.setRequestFocusEnabled(false);
    mRemoveButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          removeMarking();
        }
      });
    mUpButton = new JButton("Up");
    mUpButton.setRequestFocusEnabled(false);
    mUpButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          moveMarkingUp();
        }
      });
    mDownButton = new JButton("Down");
    mDownButton.setRequestFocusEnabled(false);
    mDownButton.addActionListener(new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          moveMarkingDown();
        }
      });

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
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mNameInput, constraints);
    mMainPanel.add(mNameInput);
    // mTypeLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mTypeLabel, constraints);
    mMainPanel.add(mTypeLabel);
    // mTypeInput
    constraints.gridx++;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mTypeInput, constraints);
    mMainPanel.add(mTypeInput);
    // mDeterministicLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mDeterministicLabel, constraints);
    mMainPanel.add(mDeterministicLabel);
    // mDeterministicButton
    constraints.gridx++;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mDeterministicButton, constraints);
    mMainPanel.add(mDeterministicButton);
    // mInitialLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    mainlayout.setConstraints(mInitialLabel, constraints);
    mMainPanel.add(mInitialLabel);
    // mInitialInput
    constraints.gridx++;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    mainlayout.setConstraints(mInitialInput, constraints);
    mMainPanel.add(mInitialInput);

    // Second, the markings panel ...
    final GridBagLayout markingslayout = new GridBagLayout();
    mMarkingsPanel.setLayout(markingslayout);
    // mMarkingsTable
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.weighty = 1.0;
    // Label
    final JLabel label = new JLabel("Markings:");
    constraints.weightx = 0.0;
    constraints.gridheight = 4;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    markingslayout.setConstraints(label, constraints);
    mMarkingsPanel.add(label);
    // List
    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    final JScrollPane scrolled = new JScrollPane(mMarkingsTable);
    final Border border = BorderFactory.createLoweredBevelBorder();
    scrolled.setBorder(border);
    markingslayout.setConstraints(scrolled, constraints);
    mMarkingsPanel.add(scrolled);
    // List control buttons
    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.gridheight = 1;
    markingslayout.setConstraints(mAddButton, constraints);
    mMarkingsPanel.add(mAddButton);
    constraints.gridy++;
    markingslayout.setConstraints(mRemoveButton, constraints);
    mMarkingsPanel.add(mRemoveButton);
    constraints.gridy++;
    markingslayout.setConstraints(mUpButton, constraints);
    mMarkingsPanel.add(mUpButton);
    constraints.gridy++;
    markingslayout.setConstraints(mDownButton, constraints);
    mMarkingsPanel.add(mDownButton);

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
    constraints.weighty = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    layout.setConstraints(mMarkingsPanel, constraints);
    contents.add(mMarkingsPanel);
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
   * Enables or disables the list control buttons.
   * This method is attached to a selection listener on the markings
   * table. It makes sure that the 'remove', 'up', and 'down' buttons
   * are enabled only when something is selected.
   */
  private void updateListControlEnabled()
  {
    final int selcount = mMarkingsTable.getSelectedRowCount();
    if (selcount > 0) {
      mRemoveButton.setEnabled(true);
      final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
      final int maxindex = selmodel.getMaxSelectionIndex();
      final int minindex = selmodel.getMinSelectionIndex();
      final int lastrow = mMarkingsTable.getRowCount() - 1;
      mUpButton.setEnabled(minindex > 0 ||
                           minindex + selcount - 1 < maxindex);
      mDownButton.setEnabled(maxindex < lastrow ||
                             maxindex - selcount + 1 > minindex);
    } else {
      mRemoveButton.setEnabled(false);
      mUpButton.setEnabled(false);
      mDownButton.setEnabled(false);
    }
  }

  /**
   * Activates the markings table.
   * This method is attached to a mouse listener and called when the
   * user clicks the markings table. It checks if the click was in the unused
   * area at the bottom of the viewport. If so, it gives focus to the table
   * and, in case of a double-click, it also starts editing.
   */
  private void handleMarkingsTableClick(final MouseEvent event)
  {
    if (event.getButton() == MouseEvent.BUTTON1) {
      final Point point = event.getPoint();
      final int row = mMarkingsTable.rowAtPoint(point);
      if (row < 0) {
        switch (event.getClickCount()) {
        case 1:
          if (!mMarkingsTable.isEditing() && !mMarkingsTable.isFocusOwner()) {
            mMarkingsTable.requestFocusInWindow();
          }
          break;
        case 2:
          addMarking();
          break;
        default:
          break;
        }
      }
    }
  }


  /**
   * Creates a marking entry.
   * This method is attached to action listener of the 'add' button
   * of the markings list control.
   */
  private void addMarking()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              addMarking();
            }
          });
      }
    } else {
      final int row;
      final int column;
      final List<VariableMarkingSubject> markings = mMarkingsModel.getList();
      if (markings.isEmpty()) {
        row = mMarkingsModel.createEditedItemAtEnd(MARKING_TEMPLATE);
        column = 2;
      } else {
        row = mMarkingsModel.createEditedItemAtEnd();
        column = 1;
      }
      if (mMarkingsTable.editCellAt(row, column)) {
        final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
        selmodel.setSelectionInterval(row, row);
        final Component comp = mMarkingsTable.getEditorComponent();
        final Rectangle bounds = comp.getBounds();
        mMarkingsTable.scrollRectToVisible(bounds);
        comp.requestFocusInWindow();
      }
    }
  }

  /**
   * Removes all selected markings.
   * This method is attached to action listener of the 'remove' button
   * of the markings list control.
   */
  private void removeMarking()
  {
    final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
    if (mMarkingsTable.isEditing()) {
      final int row = mMarkingsTable.getEditingRow();
      if (selmodel.isSelectedIndex(row)) {
        final TableCellEditor editor = mMarkingsTable.getCellEditor();
        if (!editor.stopCellEditing()) {
          editor.cancelCellEditing();
        }
      }
    }
    final int maxindex = selmodel.getMaxSelectionIndex();
    if (maxindex >= 0) {
      final int minindex = selmodel.getMinSelectionIndex();
      for (int index = maxindex; index >= minindex; index--) {
        if (selmodel.isSelectedIndex(index)) {
          mMarkingsModel.removeRow(index);
        }
      }
    }
    mErrorLabel.clearDisplay();
  }


  /**
   * Moves all selected markings up by one step.
   * This method is attached to action listener of the 'up' button
   * of the markings list control.
   */
  private void moveMarkingUp()
  {
    if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              moveMarkingUp();
            }
          });
      }
    } else {
      final int[] rows = mMarkingsTable.getSelectedRows();
      final int newfirst = mMarkingsModel.moveUp(rows);
      if (newfirst >= 0) {
        final int newlast = newfirst + rows.length - 1;
        final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
        selmodel.setSelectionInterval(newfirst, newlast);
      }
    }
  }

  /**
   * Moves all selected markings down by one step.
   * This method is attached to action listener of the 'down' button
   * of the markings list control.
   */
  private void moveMarkingDown()
  {
    if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              moveMarkingDown();
            }
          });
      }
    } else {
      final int[] rows = mMarkingsTable.getSelectedRows();
      final int newfirst = mMarkingsModel.moveDown(rows);
      if (newfirst >= 0) {
        final int newlast = newfirst + rows.length - 1;
        final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
        selmodel.setSelectionInterval(newfirst, newlast);
      }
    }
  }

  /**
   * Commits the contents of this dialog to the model.
   * This method is attached to action listener of the 'OK' button
   * of the event editor dialog.
   */
  public void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              commitDialog();
            }
          });
      }
    } else if (!mTypeInput.shouldYieldFocus()) {
      mTypeInput.requestFocusInWindow();
    } else if (!mInitialInput.shouldYieldFocus()) {
      mInitialInput.requestFocusInWindow();
    } else {
      final IdentifierSubject ident =
        (IdentifierSubject) mNameInput.getValue();
      final SimpleExpressionSubject type =
        (SimpleExpressionSubject) mTypeInput.getValue();
      final boolean deterministic = mDeterministicButton.isSelected();
      final SimpleExpressionSubject initial = 
        (SimpleExpressionSubject) mInitialInput.getValue();
      final List<VariableMarkingSubject> origmarkings =
        mMarkingsModel.getList();
      final List<VariableMarkingSubject> markings =
        new ArrayList<VariableMarkingSubject>(origmarkings.size());
      for (final VariableMarkingSubject marking : origmarkings) {
        if (marking.getParent() == null) {
          markings.add(marking);
        } else {
          markings.add(marking.clone());
        }
      }
      final VariableComponentSubject template =
        new VariableComponentSubject(ident, type, deterministic,
                                     initial, markings);
      if (mVariable == null) {
        final ModuleSubject module = getModule();
        final Command command =
          new CreateVariableComponentCommand(template, module);
        mVariable = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else if (!mVariable.equalsWithGeometry(template)) {
        final Command command =
          new EditVariableComponentCommand(mVariable, template);
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
      mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus() ||
      mTypeInput.isFocusOwner() && !mTypeInput.shouldYieldFocus() ||
      mInitialInput.isFocusOwner() && !mInitialInput.shouldYieldFocus();
  }


  //#########################################################################
  //# Auxiliary Access
  private ModuleSubject getModule()
  {
    return mRoot.getModuleSubject();
  }

  private ExpressionParser getExpressionParser()
  {
    return mRoot.getExpressionParser();
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mTypeLabel;
  private SimpleExpressionCell mTypeInput;
  private JLabel mDeterministicLabel;
  private JCheckBox mDeterministicButton;
  private JLabel mInitialLabel;
  private SimpleExpressionCell mInitialInput;

  private JPanel mMarkingsPanel;
  private VariableMarkingTableModel mMarkingsModel;
  private JTable mMarkingsTable;
  private JButton mAddButton;
  private JButton mRemoveButton;
  private JButton mUpButton;
  private JButton mDownButton;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Created Item
  /**
   * <P>The Waters variable subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited.  If
   * a new variable is being created, it is <CODE>null</CODE>
   * until the dialog is commited and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private VariableComponentSubject mVariable;


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final VariableComponentSubject VARIABLE_TEMPLATE =
    new VariableComponentSubject(new SimpleIdentifierSubject(""),
                                 new SimpleIdentifierSubject(""),
                                 true,
                                 new SimpleIdentifierSubject(""));
  private static final VariableMarkingSubject MARKING_TEMPLATE =
    new VariableMarkingSubject
      (new SimpleIdentifierSubject(EventDeclProxy.DEFAULT_MARKING_NAME),
       new IntConstantSubject(1));

}
