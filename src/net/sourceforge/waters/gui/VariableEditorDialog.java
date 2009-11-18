//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   VariableEditorDialog
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.compiler.CompilerOperatorTable;
import net.sourceforge.waters.model.expr.BinaryOperator;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.EventDeclProxy;
import net.sourceforge.waters.model.module.IdentifierProxy;
import net.sourceforge.waters.model.module.SimpleExpressionProxy;
import net.sourceforge.waters.subject.module.BinaryExpressionSubject;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.IntConstantSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.subject.module.VariableComponentSubject;
import net.sourceforge.waters.subject.module.VariableMarkingSubject;


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
    final ModuleContext context = mRoot.getModuleContext();
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
    final IdentifierProxy oldname = template.getIdentifier();
    final FormattedInputParser nameparser =
      new ComponentNameInputParser(oldname, context, parser);
    mNameInput = new SimpleExpressionCell(oldname, nameparser);
    mNameInput.addActionListener(commithandler);
    mNameInput.setToolTipText("Enter variable name, e.g., x or v[i]");
    mNameInput.setAllowNull(false);
    mTypeLabel = new JLabel("Type:");
    mTypeInput = new SimpleExpressionCell
      (template.getType(), Operator.TYPE_RANGE, parser);
    mTypeInput.addActionListener(commithandler);
    mTypeInput.setToolTipText("Enter type expression, e.g., 0..8 or [on,off]");
    mTypeInput.setAllowNull(false);
    mDeterministicLabel = new JLabel("Deterministic:");
    mDeterministicButton =
      new JCheckBox((String) null, template.isDeterministic());
    mDeterministicButton.setRequestFocusEnabled(false);
    mInitialLabel = new JLabel("Initial:");
    mInitialInput = new InitialStatePredicateCell(template);
    mInitialInput.addActionListener(commithandler);
    mInitialInput.setAllowNull(false);

    final ActionListener dethandler = new ActionListener() {
        public void actionPerformed(final ActionEvent event)
        {
          mInitialInput.updateShownExpression();
        }
      };
    mDeterministicButton.addActionListener(dethandler);

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
    mMarkingsTable.setBackground(EditorColor.BACKGROUND);
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
      new SimpleExpressionEditor(Operator.TYPE_NAME, parser, mErrorLabel);
    mMarkingsTable.setDefaultEditor(IdentifierSubject.class, propeditor);
    propeditor.setAllowNull(true);
    propeditor.setToolTipText("Enter the name of a proposition event");
    propeditor.addCellEditorListener(mMarkingsModel);
    final TableCellEditor prededitor = new PredicateExpressionEditor(parser);
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
    final JButton okButton = new JButton("OK");
    okButton.setRequestFocusEnabled(false);
    okButton.addActionListener(commithandler);
    mButtonsPanel.add(okButton);
    final Action cancelAction = DialogCancelAction.getInstance();
    final JButton cancelButton = new JButton(cancelAction);
    cancelButton.setRequestFocusEnabled(false);
    mButtonsPanel.add(cancelButton);

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
        mInitialInput.getInitialStatePredicate();
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
      final IdentifierSubject iclone =
        ident.getParent() == null ? ident : ident.clone();
      final VariableComponentSubject template =
        new VariableComponentSubject(iclone, type, deterministic,
                                     initial, markings);
      if (mVariable == null) {
        final SelectionOwner panel = mRoot.getComponentsPanel();
        final Command command = new InsertCommand(template, panel);
        mVariable = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else if (!mVariable.equalsWithGeometry(template)) {
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

  private SimpleExpressionSubject getShownExpression
    (final VariableComponentSubject var)
  {
    final SimpleExpressionSubject pred = var.getInitialStatePredicate();
    if (var.isDeterministic()) {
      if (!(pred instanceof BinaryExpressionSubject)) {
        return null;
      }
      final BinaryExpressionSubject binpred = (BinaryExpressionSubject) pred;
      final CompilerOperatorTable optable =
        CompilerOperatorTable.getInstance();
      if (binpred.getOperator() != optable.getEqualsOperator()) {
        return null;
      }
      final IdentifierSubject ident = var.getIdentifier();
      final SimpleExpressionSubject lhs = binpred.getLeft();
      final SimpleExpressionSubject rhs = binpred.getRight();
      if (lhs.equalsByContents(ident)) {
        return rhs;
      } else if (rhs.equalsByContents(ident)) {
        return lhs;
      } else if (lhs instanceof SimpleIdentifierSubject) {
        return rhs;
      } else if (rhs instanceof SimpleIdentifierSubject) {
        return lhs;
      } else {
        return null;
      }
    } else {
      return pred;
    }
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
    extends SimpleExpressionCell
  {

    //#######################################################################
    //# Constructor
    private InitialStatePredicateCell(final VariableComponentSubject template)
    {
      super(getShownExpression(template), new InitialStateInputParser());
      setToolTipText("");
    }

    //#######################################################################
    //# Overrides for Base Class javax.swing.JComponent
    public String getToolTipText(final MouseEvent event)
    {
       if (mDeterministicButton.isSelected()) {
        return "Enter the initial value of this variable.";
      } else {
        final String text = mNameInput.getText();
        final String name = text.length() == 0 ? "x" : text;
        final StringBuffer buffer = new StringBuffer(160);
        buffer.append("Enter initial state predicate, e.g., ");
        buffer.append(name);
        buffer.append(" == 0 | ");
        buffer.append(name);
        buffer.append(" >= 4.");
        return buffer.toString();
      }
    }

    //#######################################################################
    //# Converting Deterministic and Nondeterministic Representations
    private void updateShownExpression()
    {
      if (shouldYieldFocus()) {
        final CompilerOperatorTable optable =
          CompilerOperatorTable.getInstance();
        final BinaryOperator eqop = optable.getEqualsOperator();
        final SimpleExpressionSubject value =
          (SimpleExpressionSubject) getValue();
        if (value == null) {
          return;
        } else if (mDeterministicButton.isSelected()) {
          if (!(value instanceof BinaryExpressionSubject)) {
            return;
          }
          final BinaryExpressionSubject binpred =
            (BinaryExpressionSubject) value;
          if (binpred.getOperator() != eqop) {
            return;
          }
          final IdentifierSubject ident =
            (IdentifierSubject) mNameInput.getValue();
          final SimpleExpressionSubject lhs = binpred.getLeft();
          final SimpleExpressionSubject rhs = binpred.getRight();
          if (lhs.equalsByContents(ident)) {
            setValue(rhs);
          } else if (rhs.equalsByContents(ident)) {
            setValue(lhs);
          } else if (lhs instanceof SimpleIdentifierSubject) {
            setValue(rhs);
          } else if (rhs instanceof SimpleIdentifierSubject) {
            setValue(lhs);
          }
        } else {
          final IdentifierSubject ident =
            (IdentifierSubject) mNameInput.getValue();
          final IdentifierSubject iclone =
            ident.getParent() == null ? ident : ident.clone();
          final SimpleExpressionSubject vclone =
            value.getParent() == null ? value : value.clone();
          final BinaryExpressionSubject pred =
            new BinaryExpressionSubject(eqop, iclone, vclone);
          setValue(pred);
        }
      }
      final InitialStateInputParser parser =
        (InitialStateInputParser) getFormattedInputParser();
      parser.updateTypeMask();
    }

    private SimpleExpressionSubject getInitialStatePredicate()
    {
      final SimpleExpressionSubject value =
        (SimpleExpressionSubject) getValue();
      if (value == null) {
        return null;
      } else if (mDeterministicButton.isSelected()) {
        final IdentifierSubject ident =
          (IdentifierSubject) mNameInput.getValue();
        final IdentifierSubject iclone =
          ident.getParent() == null ? ident : ident.clone();
        final CompilerOperatorTable optable =
          CompilerOperatorTable.getInstance();
        final BinaryOperator eqop = optable.getEqualsOperator();
        return new BinaryExpressionSubject(eqop, iclone, value);
      } else {
        return value;
      }
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Local Class InitialStateInputParser
  /**
   * This parser is needed to support the weird initial state predicate
   * cell. It changes its behaviour depending on the state of the
   * deterministic check box.
   */
  private class InitialStateInputParser
    extends DocumentFilter
    implements FormattedInputParser
  {

    //#######################################################################
    //# Constructors
    private InitialStateInputParser()
    {
      final ExpressionParser parser = mRoot.getExpressionParser();
      mDocumentFilter = new SimpleExpressionDocumentFilter(parser);
      updateTypeMask();
    }

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    public SimpleExpressionProxy parse(final String text)
      throws ParseException
    {
      final ExpressionParser parser = mRoot.getExpressionParser();
      return parser.parse(text, mTypeMask);
    }

    public DocumentFilter getDocumentFilter()
    {
      return mDocumentFilter;
    }

    //#######################################################################
    //# Auxiliary Methods
    private void updateTypeMask()
    {
      if (mDeterministicButton.isSelected()) {
        mTypeMask = Operator.TYPE_INDEX;
      } else {
        mTypeMask = Operator.TYPE_BOOLEAN;
      }
    }

    //#######################################################################
    //# Data Members
    private final DocumentFilter mDocumentFilter;
    private int mTypeMask;

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
      super(Operator.TYPE_BOOLEAN, parser, mErrorLabel);
    }

    //#######################################################################
    //# Overrides for Base Class
    //# net.sourceforge.waters.gui.SimpleExpressionEditor
    public SimpleExpressionCell getTableCellEditorComponent
      (final JTable table, final Object value, final boolean isSelected,
       final int row, final int column)
    {
      final SimpleExpressionCell cell = super.getTableCellEditorComponent
        (table, value, isSelected, row, column);
      final String text = mNameInput.getText();
      final String name = text.length() == 0 ? "x" : text;
      final StringBuffer buffer = new StringBuffer(160);
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

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

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

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class AddIndexAction
  private class AddMarkingAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private AddMarkingAction()
    {
      putValue(Action.NAME, "Add");
      putValue(Action.SHORT_DESCRIPTION, "Create a new index range");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_INSERT);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
      setEnabled(true);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      addMarking();
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

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
      putValue(Action.SHORT_DESCRIPTION, "Delete all selected index ranges");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      removeMarkings();
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

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
      putValue(Action.SHORT_DESCRIPTION, "Move selected index ranges up");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_UP);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_UP,
                                      InputEvent.CTRL_DOWN_MASK));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      moveMarkingsUp();
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

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
      putValue(Action.SHORT_DESCRIPTION, "Move selected index ranges down");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DOWN);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_DOWN,
                                      InputEvent.CTRL_DOWN_MASK));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      moveMarkingsDown();
    }

    //#########################################################################
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
  private JLabel mTypeLabel;
  private SimpleExpressionCell mTypeInput;
  private JLabel mDeterministicLabel;
  private JCheckBox mDeterministicButton;
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
  private static final long serialVersionUID = 1L;
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
