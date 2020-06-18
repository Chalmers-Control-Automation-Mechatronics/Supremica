//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2020 Robi Malik
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

import java.awt.AWTKeyStroke;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.ModuleWindowInterface;
import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.FocusTracker;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.ModuleEqualityVisitor;
import net.sourceforge.waters.model.module.ModuleProxyCloner;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.VariableMarkingProxy;
import net.sourceforge.waters.plain.module.IntConstantElement;
import net.sourceforge.waters.plain.module.SimpleIdentifierElement;
import net.sourceforge.waters.plain.module.VariableMarkingElement;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.ModuleSubjectFactory;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.subject.module.VariableMarkingSubject;

import org.supremica.gui.ide.IDE;


/**
 * @author Robi Malik
 */

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
    final VariableComponentSubject template;
    if (mVariable == null) {
      try {
        template = VARIABLE_TEMPLATE;
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
      template = mVariable;
    }
    final ModuleContext context = mRoot.getModuleContext();
    final ExpressionParser parser = mRoot.getExpressionParser();
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
    final IdentifierProxy oldIdent =
      mVariable == null ? null : mVariable.getIdentifier();
    final ComponentNameInputHandler handler =
      new ComponentNameInputHandler(oldIdent, context, parser, true);
    mNameInput = new SimpleExpressionInputCell(oldIdent, handler);
    mNameInput.addActionListener(commitHandler);
    mNameInput.addSimpleDocumentListener(okEnablement);
    mNameInput.setToolTipText("Enter variable name, e.g., x or v[i].");
    // Standard verification does not support clocks, do not suggest it here.
    //                    + "A variable name started by 'clock:' will be "
    //                    + "treated as clock, i.e., the value of the variable "
    //                    + "will implicitly be increased at locations.");
    mTypeLabel = new JLabel("Type:");
    final SimpleExpressionProxy type = mVariable == null ? null : mVariable.getType();
    mTypeInput =
      new SimpleExpressionInputCell(type, Operator.TYPE_RANGE, parser, true);
    mTypeInput.addActionListener(commitHandler);
    mTypeInput.addSimpleDocumentListener(okEnablement);
    mTypeInput.setToolTipText("Enter type expression, e.g., 0..8 or [on,off]");
    mInitialLabel = new JLabel("Initial:");
    mInitialInput = new InitialStatePredicateCell();
    mInitialInput.addActionListener(commitHandler);
    mInitialInput.addSimpleDocumentListener(okEnablement);

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
    mMarkingsModel = new VariableMarkingTableModel(copy, context);
    mMarkingsTable = new VariableMarkingTable(mMarkingsModel);
    mMarkingsTable.setTableHeader(null);
    mMarkingsTable.setShowGrid(false);
    mMarkingsTable.setSurrendersFocusOnKeystroke(true);
    mMarkingsTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    mMarkingsTable.setFillsViewportHeight(true);
    mMarkingsTable.setBackground(EditorColor.BACKGROUNDCOLOR);
    final Dimension minsize = new Dimension(0, 0);
    mMarkingsTable.setMinimumSize(minsize);
    final Dimension prefsize = new Dimension(300, 0);
    mMarkingsTable.setPreferredScrollableViewportSize(prefsize);
    mMarkingsTable.setRowSelectionAllowed(true);
    final TableColumnModel colmodel = mMarkingsTable.getColumnModel();
    final TableColumn column0 = colmodel.getColumn(0);
    column0.setPreferredWidth(32);
    column0.setMaxWidth(32);
    final TableColumn column1 = colmodel.getColumn(1);
    column1.setPreferredWidth(100);
    final TableColumn column2 = colmodel.getColumn(2);
    column2.setPreferredWidth(160);
    final Set<AWTKeyStroke> forward = mNameInput.getFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    mMarkingsTable.setFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
    final Set<AWTKeyStroke> backward = mNameInput.getFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    mMarkingsTable.setFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
    mMarkingsTable.addEscapeAction();
    final SimpleExpressionEditor propeditor =
      new SimpleExpressionEditor(Operator.TYPE_NAME, parser, true, mErrorLabel);
    mMarkingsTable.setDefaultEditor(IdentifierSubject.class, propeditor);
    propeditor.setToolTipText("Enter the name of a proposition event");
    propeditor.addCellEditorListener(mMarkingsModel);
    final TableCellEditor prededitor = new PredicateExpressionEditor(parser);
    mMarkingsTable.setDefaultEditor(SimpleExpressionSubject.class, prededitor);
    prededitor.addCellEditorListener(mMarkingsModel);
    final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
    selmodel.addListSelectionListener(new ListSelectionListener() {
        @Override
        public void valueChanged(final ListSelectionEvent event)
        {
          updateListControlEnabled();
        }
      });
    mMarkingsTable.addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(final MouseEvent event)
        {
          handleMarkingsTableClick(event);
        }
      });
    mAddAction = new AddMarkingAction();
    final JRootPane root = getRootPane();
    final String name = (String) mAddAction.getValue(Action.NAME);
    final KeyStroke key =
      (KeyStroke) mAddAction.getValue(Action.ACCELERATOR_KEY);
    final InputMap imap = root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
    final ActionMap amap = root.getActionMap();
    imap.put(key, name);
    amap.put(name, mAddAction);
    mAddButton = new JButton(mAddAction);
    mAddButton.setRequestFocusEnabled(false);
    mRemoveAction = new RemoveMarkingsAction();
    mMarkingsTable.addKeyboardAction(mRemoveAction);
    mRemoveButton = new JButton(mRemoveAction);
    mRemoveButton.setRequestFocusEnabled(false);
    mUpAction = new MoveMarkingsUpAction();
    mMarkingsTable.addKeyboardAction(mUpAction);
    mUpButton = new JButton(mUpAction);
    mUpButton.setRequestFocusEnabled(false);
    mDownAction = new MoveMarkingsDownAction();
    mMarkingsTable.addKeyboardAction(mDownAction);
    mDownButton = new JButton(mDownAction);
    mDownButton.setRequestFocusEnabled(false);

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

    root.setDefaultButton(mOkButton);
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
      mRemoveAction.setEnabled(true);
      final ListSelectionModel selmodel = mMarkingsTable.getSelectionModel();
      final int maxindex = selmodel.getMaxSelectionIndex();
      final int minindex = selmodel.getMinSelectionIndex();
      final int lastrow = mMarkingsTable.getRowCount() - 1;
      mUpAction.setEnabled(minindex > 0 ||
                           minindex + selcount - 1 < maxindex);
      mDownAction.setEnabled(maxindex < lastrow ||
                             maxindex - selcount + 1 > minindex);
    } else {
      mRemoveAction.setEnabled(false);
      mUpAction.setEnabled(false);
      mDownAction.setEnabled(false);
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
            @Override
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
        final VariableMarkingSubject template = getMarkingTemplate();
        row = mMarkingsModel.createEditedItemAtEnd(template);
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
  private void removeMarkings()
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
  private void moveMarkingsUp()
  {
    if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
              moveMarkingsUp();
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
  private void moveMarkingsDown()
  {
    if (mMarkingsTable.isEditing()) {
      final TableCellEditor editor = mMarkingsTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run()
            {
              moveMarkingsDown();
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

  private void updateOkButtonStatus()
  {
    final boolean enabled =
      mNameInput.getText().length() > 0 &&
      mTypeInput.getText().length() > 0 &&
      mInitialInput.getText().length() > 0;
    mOkButton.setEnabled(enabled);
  }

  /**
   * Commits the contents of this dialog to the model.
   * This method is attached to action listener of the 'OK' button
   * of the event editor dialog.
   */
  private void commitDialog()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mNameInput.getValue() == null) {
      mNameInput.requestFocusWithErrorMessage("Please enter a variable name.");
    } else if (mTypeInput.getValue() == null) {
      mTypeInput.requestFocusWithErrorMessage
        ("Please enter an expression for the variable range.");
    } else if (mInitialInput.getValue() == null) {
      mInitialInput.requestFocusWithErrorMessage
        ("Please enter an initial state condition.");
    } else {
      final IdentifierSubject ident =
        (IdentifierSubject) mNameInput.getValue();
      final SimpleExpressionSubject type =
        (SimpleExpressionSubject) mTypeInput.getValue();
      final SimpleExpressionSubject initial =
        mInitialInput.getInitialStatePredicate();
      if (mMarkingsTable.isEditing()) {
        mMarkingsTable.getCellEditor().stopCellEditing();
      }
      final List<VariableMarkingSubject> origMarkings =
        mMarkingsModel.getList();
      final List<VariableMarkingSubject> markings =
        new ArrayList<VariableMarkingSubject>(origMarkings.size());
      for (final VariableMarkingSubject marking : origMarkings) {
        if (marking.getParent() == null) {
          markings.add(marking);
        } else {
          markings.add(marking.clone());
        }
      }
      final IdentifierSubject tIdent =
        ident.getParent() == null ? ident : ident.clone();
      final SimpleExpressionSubject tType =
        type.getParent() == null ? type : type.clone();
      final SimpleExpressionSubject tInitial =
        initial.getParent() == null ? initial : initial.clone();
      final VariableComponentSubject template =
        new VariableComponentSubject(tIdent, tType, tInitial, markings);
      final ModuleEqualityVisitor eq = new ModuleEqualityVisitor(true);
      if (mVariable == null) {
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final InsertInfo insert = new InsertInfo(template, mInsertPosition);
        final List<InsertInfo> list = Collections.singletonList(insert);
        final Command command = new InsertCommand(list, panel, mRoot);
        mVariable = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else if (!eq.equals(mVariable, template)) {
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final Command command = new EditCommand(mVariable, template, panel);
        mRoot.getUndoInterface().executeCommand(command);
      }
      dispose();
    }
  }


  //#########################################################################
  //# Auxiliary Methods
  /**
   * Checks whether it is unsafe to commit the currently edited text field.
   * If this method returns <CODE>true</CODE>, it is unsafe to commit the
   * current dialog contents, and shifting the focus is to be avoided.
   * @return <CODE>true</CODE> if the component currently owning the focus
   *         is to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  private boolean isInputLocked()
  {
    final IDE ide = mRoot.getRootWindow();
    final FocusTracker tracker = ide.getFocusTracker();
    return !tracker.shouldYieldFocus(this);
  }


  //#########################################################################
  //# Auxiliary Static Methods
  @SuppressWarnings("unused")
  private static VariableComponentSubject getVariableTemplate()
  {
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    return (VariableComponentSubject) cloner.getClone(VARIABLE_TEMPLATE);
  }

  private static VariableMarkingSubject getMarkingTemplate()
  {
    final ModuleProxyCloner cloner =
      ModuleSubjectFactory.getCloningInstance();
    return (VariableMarkingSubject) cloner.getClone(MARKING_TEMPLATE);
  }


  //#########################################################################
  //# Inner Class InitialStatePredicateCell
  /**
   * <P>The initial state editor.</P>
   *
   * <P>This cell supports smart editing of the initial state predicate for
   * deterministic variables. For a deterministic variable <CODE>x</CODE>,
   * the user can enter an initial <I>value</I> such
   * as&nbsp;<CODE>0</CODE>, and the GUI automatically generates the
   * predicate <CODE>x==0</CODE>. For nondeterministic variables, the
   * values is entered directly.</P>
   *
   * <P>The behaviour and text fields contents change, when the checkbox
   * for the <I>deterministic</I> attribute changes. The changes are
   * reflected in a smart tooltip.</P>
   */
  private class InitialStatePredicateCell
    extends SimpleExpressionInputCell
  {
    //#######################################################################
    //# Constructor
    private InitialStatePredicateCell()
    {
      super(mVariable == null ? null : mVariable.getInitialStatePredicate(),
            new InitialStateInputHandler());
      setToolTipText("");
    }

    //#######################################################################
    //# Overrides for Base Class javax.swing.JComponent
    @Override
    public String getToolTipText(final MouseEvent event)
    {
      final String text = mNameInput.getText();
      final String name = text.length() == 0 ? "x" : text;
      final StringBuilder buffer = new StringBuilder(160);
      buffer.append("Enter initial state predicate, e.g., ");
      buffer.append(name);
      buffer.append(" == 0 | ");
      buffer.append(name);
      buffer.append(" >= 4.");
      return buffer.toString();
    }

    //#######################################################################
    //# Simple Access
    private SimpleExpressionSubject getInitialStatePredicate()
    {
      final SimpleExpressionSubject value =
        (SimpleExpressionSubject) getValue();
      if (value == null) {
        return null;
      } else {
        return value;
      }
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -1703295424347060568L;
  }


  //#########################################################################
  //# Local Class InitialStateInputParser
  private class InitialStateInputHandler
    extends AbstractSimpleExpressionInputHandler<SimpleExpressionProxy>
  {
    //#######################################################################
    //# Constructors
    private InitialStateInputHandler()
    {
      super(Operator.TYPE_BOOLEAN, mRoot.getExpressionParser(), true);
    }

    //#######################################################################
    //# Interface
    //# net.sourceforge.waters.gui.FormattedInputParser<SimpleExpressionProxy>
    @Override
    public SimpleExpressionProxy parse(final String text)
      throws ParseException
    {
      return callParser(text);
    }
  }


  //#########################################################################
  //# Inner Class PredicateExpressionEditor
  /**
   * This extension of {@link SimpleExpressionEditor} is only done to
   * add a smart tooltip.
   */
  private class PredicateExpressionEditor
    extends SimpleExpressionEditor
  {
    //#######################################################################
    //# Constructor
    private PredicateExpressionEditor(final ExpressionParser parser)
    {
      super(Operator.TYPE_BOOLEAN, parser, false, mErrorLabel);
    }

    //#######################################################################
    //# Overrides for Base Class
    //# net.sourceforge.waters.gui.SimpleExpressionEditor
    @Override
    public SimpleExpressionInputCell getTableCellEditorComponent
      (final JTable table, final Object value, final boolean isSelected,
       final int row, final int column)
    {
      final SimpleExpressionInputCell cell = super.getTableCellEditorComponent
        (table, value, isSelected, row, column);
      final String text = mNameInput.getText();
      final String name = text.length() == 0 ? "x" : text;
      final StringBuilder buffer = new StringBuilder(160);
      buffer.append("Enter marking condition, e.g., ");
      buffer.append(name);
      buffer.append(" == 0 | ");
      buffer.append(name);
      buffer.append(" >= 4. ");
      buffer.append("0 stands for false, and 1 stands for true.");
      final String tooltip = buffer.toString();
      cell.setToolTipText(tooltip);
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 6964682623458579505L;
  }


  //#########################################################################
  //# Inner Class VariableMarkingTable
  /**
   * This extension of {@link JTable} is only done to add tooltips to the
   * columns.
   */
  private static class VariableMarkingTable
    extends NonTypingTable
  {
    //#######################################################################
    //# Constructor
    private VariableMarkingTable(final VariableMarkingTableModel model)
    {
      super(model);
    }

    //#######################################################################
    //# Overrides for javax.swing.JTable
    @Override
    public String getToolTipText(final MouseEvent event)
    {
      final Point point = event.getPoint();
      final int column = columnAtPoint(point);
      if (column <= 1) {
        return "Propositions";
      } else {
        return "Marking Predicates";
      }
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3655210236422935964L;
  }


  //#########################################################################
  //# Inner Class AddMarkingAction
  private class AddMarkingAction extends AbstractAction
  {
    //#######################################################################
    //# Constructor
    private AddMarkingAction()
    {
      putValue(Action.NAME, "Add");
      putValue(Action.SHORT_DESCRIPTION, "Create a new marking");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_INSERT);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
      setEnabled(true);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      addMarking();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -4680369986902735099L;
  }


  //#########################################################################
  //# Inner Class RemoveMarkingsAction
  private class RemoveMarkingsAction extends AbstractAction
  {
    //#######################################################################
    //# Constructor
    private RemoveMarkingsAction()
    {
      putValue(Action.NAME, "Remove");
      putValue(Action.SHORT_DESCRIPTION, "Delete all selected markings");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      removeMarkings();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 5595797508063606092L;
  }


  //#########################################################################
  //# Inner Class MoveMarkingsUpAction
  private class MoveMarkingsUpAction extends AbstractAction
  {
    //#######################################################################
    //# Constructor
    private MoveMarkingsUpAction()
    {
      putValue(Action.NAME, "Up");
      putValue(Action.SHORT_DESCRIPTION, "Move selected markings up");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_UP);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_UP,
                                      InputEvent.CTRL_DOWN_MASK));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      moveMarkingsUp();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = -3528168004852617521L;
  }


  //#########################################################################
  //# Inner Class MoveMarkingsDownAction
  private class MoveMarkingsDownAction extends AbstractAction
  {
    //#######################################################################
    //# Constructor
    private MoveMarkingsDownAction()
    {
      putValue(Action.NAME, "Down");
      putValue(Action.SHORT_DESCRIPTION, "Move selected markings down");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DOWN);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
                                      InputEvent.CTRL_DOWN_MASK));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      moveMarkingsDown();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 4153451970893897487L;
  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;

  // Swing components
  private JPanel mMainPanel;
  private JLabel mNameLabel;
  private SimpleExpressionInputCell mNameInput;
  private JLabel mTypeLabel;
  private SimpleExpressionInputCell mTypeInput;
  private JLabel mInitialLabel;
  private InitialStatePredicateCell mInitialInput;

  private JPanel mMarkingsPanel;
  private VariableMarkingTableModel mMarkingsModel;
  private VariableMarkingTable mMarkingsTable;
  private Action mAddAction;
  private Action mRemoveAction;
  private Action mUpAction;
  private Action mDownAction;
  private JButton mAddButton;
  private JButton mRemoveButton;
  private JButton mUpButton;
  private JButton mDownButton;

  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;
  private JButton mOkButton;

  // Created Item
  /**
   * <P>The Waters variable subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited.  If
   * a new variable is being created, it is <CODE>null</CODE>
   * until the dialog is committed and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private VariableComponentSubject mVariable;
  private Object mInsertPosition;



  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = -2853266631960752351L;
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final VariableComponentSubject VARIABLE_TEMPLATE =
    new VariableComponentSubject(new SimpleIdentifierSubject(""),
                                 new SimpleIdentifierSubject(""),
                                 new SimpleIdentifierSubject(""));
  private static final VariableMarkingProxy MARKING_TEMPLATE =
    new VariableMarkingElement
      (new SimpleIdentifierElement(EventDeclProxy.DEFAULT_MARKING_NAME),
       new IntConstantElement(1));
  private static final Transferable TRANSFERABLE =
    WatersDataFlavor.createTransferable(VARIABLE_TEMPLATE);
}
