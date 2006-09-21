//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventEditorDialog
//###########################################################################
//# $Id: EventEditorDialog.java,v 1.10 2006-09-21 14:03:12 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.AWTKeyStroke;
import java.awt.BorderLayout;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.model.base.WatersRuntimeException;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.subject.base.IndexedListSubject;
import net.sourceforge.waters.subject.base.NamedSubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.EventParameterSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.ParameterSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.xsd.base.EventKind;


public class EventEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructor
  public EventEditorDialog(final ModuleWindowInterface root)
  {
    this(root, false, false);
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final boolean moreoptions,
                           final boolean isparam)
  {
    this(root, true, moreoptions, createDefaultItem(isparam));
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final boolean moreoptions,
                           final EventDeclSubject decl)
  {
    this(root, false, moreoptions, decl);
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final boolean moreoptions,
                           final EventParameterSubject param)
  {
    this(root, false, moreoptions, param);
  }

  private EventEditorDialog(final ModuleWindowInterface root,
                            final boolean createnew,
                            final boolean moreoptions,
                            final NamedSubject item)
  {
    super(root.getRootWindow());
    if (createnew) {
      setTitle("Creating new event declation");
    } else {
      setTitle("Editing event declation '" + item.getName() + "'");
    }
    mRoot = root;
    mCreating = createnew;
    mDisplayingMoreOptions = moreoptions;
    mEditedItem = item;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    mActionListeners = new LinkedList<ActionListener>();
  }

  private static NamedSubject createDefaultItem(final boolean isparam)
  {
    final EventDeclSubject decl =
      new EventDeclSubject("", EventKind.CONTROLLABLE);
    if (isparam) {
      return new EventParameterSubject("", decl);
    } else {
      return decl;
    }
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * It may be of type {@link EventDeclSubject} or
   * {@link EventParameterSubject}.
   * @return A reference to the object being edited by this dialog.
   */
  public NamedSubject getEditedItem()
  {
    return mEditedItem;
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Adds an action listener to this dialog. The action listeners of an
   * event editor dialog are triggered when the user commits the dialog,
   * after the event or parameter declaration has been created and added
   * to the module. Therefore, they can query the value of {@link
   * #getEditedItem()} to determine which subject was created. The
   * {@link ActionEvent} passed to the listener is the event that caused
   * the dialog to be comitted.
   */
  public void addActionListener(final ActionListener listener)
  {
    mActionListeners.add(listener);
  }

  /**
   * Removes an action listener from this dialog.
   * @see #addActionListener(ActionListener)
   */
  public void removeActionListener(final ActionListener listener)
  {
    mActionListeners.remove(listener);
  }

	
  //#########################################################################
  //# Initialisation and Layout of Components
  /**
   * Initialise buttons and components that have not yet been initialised.
   * If {@link #mDisplayingMoreOptions} is <CODE>true</CODE> all components
   * of the full dialog are initialised, otherwise only those needed by the
   * reduced version.
   */
  private void createComponents()
  {
    EventParameterSubject param = null;
    EventDeclSubject decl = null;
    ActionListener commithandler = null;
    if (mEditedItem instanceof EventDeclSubject) {
      param = null;
      decl = (EventDeclSubject) mEditedItem;
    } else {
      param = (EventParameterSubject) mEditedItem;
      decl = param.getEventDecl();
    }
    if (mNamePanel == null) {
      // Initialising for the first time. Everything needs to be done.
      // Name panel, basic part ...
      mNamePanel = new RaisedDialogPanel();
      mNameLabel = new JLabel("Name:");
      final FormattedInputParser parser = new EventNameInputParser();
      mNameInput = new SimpleExpressionCell(decl.getName(), parser);
      commithandler = new ActionListener() {
        public void actionPerformed(final ActionEvent event) {
          commitDialog();
          fireActionPerformed(event);
        }
      };
      mNameInput.addActionListener(commithandler);
      mNameInput.addKeyListener(new KeyAdapter() {
          public void keyTyped(final KeyEvent event) {
            if (event.getKeyChar() == '\n' && event.getModifiers() == 0) {
              commitDialog();
            }
          }
        });
      mKindLabel = new JLabel("Kind:");
      mKindGroup = new ButtonGroup();
      mControllableButton = new JRadioButton("Controllable");
      mControllableButton.setRequestFocusEnabled(false);
      mKindGroup.add(mControllableButton);
      mUncontrollableButton = new JRadioButton("Uncontrollable");
      mUncontrollableButton.setRequestFocusEnabled(false);
      mKindGroup.add(mUncontrollableButton);
      mPropositionButton = new JRadioButton("Proposition");
      mPropositionButton.setRequestFocusEnabled(false);
      mPropositionButton.setEnabled(!ModuleWindow.DES_COURSE_VERSION);
      mKindGroup.add(mPropositionButton);
      switch (decl.getKind()) {
      case CONTROLLABLE:
        mControllableButton.setSelected(true);
        break;
      case UNCONTROLLABLE:
        mUncontrollableButton.setSelected(true);
        break;
      case PROPOSITION:
        mPropositionButton.setSelected(true);
        break;
      }
      mMoreOptionsButton = new JButton();
      mMoreOptionsButton.setRequestFocusEnabled(false);
      mMoreOptionsButton.setEnabled(!ModuleWindow.DES_COURSE_VERSION);
      mMoreOptionsButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            toggleMoreOptions();
          }
        });
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
          public void actionPerformed(final ActionEvent event) {
            dispose();
          }
        });
      mButtonsPanel.add(cancelButton);
    }
    if (mDisplayingMoreOptions && mIndexPanel == null) {
      // Initialising with more options, and index panel not used before.
      // Need to create extra components for name panel ...
      mObservableButton = new JCheckBox("Observable");
      mObservableButton.setRequestFocusEnabled(false);
      mObservableButton.setSelected(decl.isObservable());
      mParameterButton = new JCheckBox("Parameter");
      mParameterButton.setRequestFocusEnabled(false);
      mParameterButton.setSelected(param != null);
      mParameterButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            updateRequiredEnabled();
          }
        });
      mRequiredButton = new JCheckBox("Required");
      mRequiredButton.setRequestFocusEnabled(false);
      mRequiredButton.setSelected(param == null || param.isRequired());
      updateRequiredEnabled();
      mColourButton = new JButton("Colour ...");
      mColourButton.setRequestFocusEnabled(false);
      // ... add listeners to enable/disable the colour button ...
      final ActionListener kindlistener = new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            updateColourEnabled();
          }
        };
      mControllableButton.addActionListener(kindlistener);
      mUncontrollableButton.addActionListener(kindlistener);
      mPropositionButton.addActionListener(kindlistener);
      updateColourEnabled();
      // ... and create index panel.
      mIndexPanel = new RaisedDialogPanel();
      final List<SimpleExpressionSubject> ranges = decl.getRangesModifiable();
      final List<SimpleExpressionSubject> copy =
        new ArrayList<SimpleExpressionSubject>(ranges);
      mIndexModel = new ListTableModel<SimpleExpressionSubject>
        (copy, SimpleExpressionSubject.class);
      mIndexTable = new JTable(mIndexModel) {
          public boolean getScrollableTracksViewportHeight() {
            final Container viewport = getParent();
            return getPreferredSize().height < viewport.getHeight();
          }
        };
      mIndexTable.setTableHeader(null);
      mIndexTable.setShowGrid(false);
      mIndexTable.setSurrendersFocusOnKeystroke(true);
      mIndexTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      //mIndexTable.setFillsViewportHeight(true); // works in Java 6
      final Dimension minsize = new Dimension(0, 0);
      mIndexTable.setPreferredScrollableViewportSize(minsize);
      mIndexTable.setMinimumSize(minsize);
      mIndexTable.setRowSelectionAllowed(true);
      final Set<AWTKeyStroke> forward = mNameInput.getFocusTraversalKeys
        (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
      mIndexTable.setFocusTraversalKeys
        (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
      final Set<AWTKeyStroke> backward = mNameInput.getFocusTraversalKeys
        (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
      mIndexTable.setFocusTraversalKeys
        (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
      final ExpressionParser parser = getExpressionParser();
      final TableCellEditor editor =
        new SimpleExpressionEditor(Operator.TYPE_RANGE, parser, mErrorLabel);
      mIndexTable.setDefaultEditor(Object.class, editor);
      editor.addCellEditorListener(mIndexModel);
      final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
      selmodel.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(final ListSelectionEvent event) {
            updateListControlEnabled();
          }
        });
      mIndexTable.addMouseListener(new MouseAdapter() {
          public void mouseClicked(final MouseEvent event) {
            handleIndexTableClick(event);
          }
        });
      mAddButton = new JButton("Add");
      mAddButton.setRequestFocusEnabled(false);
      mAddButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            addIndexRange();
          }
        });
      mRemoveButton = new JButton("Remove");
      mRemoveButton.setRequestFocusEnabled(false);
      mRemoveButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            removeIndexRange();
          }
        });
      mUpButton = new JButton("Up");
      mUpButton.setRequestFocusEnabled(false);
      mUpButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            moveIndexRangeUp();
          }
        });
      mDownButton = new JButton("Down");
      mDownButton.setRequestFocusEnabled(false);
      mDownButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event) {
            moveIndexRangeDown();
          }
        });
      updateListControlEnabled();
      layoutIndexPanel();
    }
  }


  /**
   * Fill the panels and layout all buttons and components.  
   * This method uses the {@link #mDisplayingMoreOptions} member to
   * determine whether the full dialog ore only the reduced version is to
   * be shown. It is assumed that all needed components have been
   * created by a call to {@link #createComponents()} before.
   */
  private void layoutComponents()
  {
    final Container contents = getContentPane();
    final GridBagLayout layout = new GridBagLayout();
    contents.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    if (mDisplayingMoreOptions) {
      constraints.weighty = 0.0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
      layoutExtendedNamePanel();
    } else {
      constraints.weighty = 1.0;
      constraints.fill = GridBagConstraints.BOTH;
      layoutSimpleNamePanel();
    }
    layout.setConstraints(mNamePanel, constraints);
    contents.add(mNamePanel);
    if (mDisplayingMoreOptions) {
      constraints.weighty = 1.0;
      constraints.fill = GridBagConstraints.BOTH;
      layout.setConstraints(mIndexPanel, constraints);
      contents.add(mIndexPanel);
      constraints.weighty = 0.0;
      constraints.fill = GridBagConstraints.HORIZONTAL;
    }
    layout.setConstraints(mErrorPanel, constraints);
    contents.add(mErrorPanel);
    layout.setConstraints(mButtonsPanel, constraints);
    contents.add(mButtonsPanel);
    pack();
  }


  /**
   * Fill and layout the name panel with components for the simple
   * version of the dialog.
   */ 
  private void layoutSimpleNamePanel()
  {
    final GridBagLayout nameLayout = new GridBagLayout();
    mNamePanel.setLayout(nameLayout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;
    // mNameLabel
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    nameLayout.setConstraints(mNameLabel, constraints);
    mNamePanel.add(mNameLabel);
    // mNameInput
    constraints.gridwidth = 3;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    nameLayout.setConstraints(mNameInput, constraints);
    mNamePanel.add(mNameInput);
    // mKindLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    nameLayout.setConstraints(mKindLabel, constraints);
    mNamePanel.add(mKindLabel);
    // mControllableButton
    constraints.gridx++;
    constraints.weightx = 1.0;
    nameLayout.setConstraints(mControllableButton, constraints);
    mNamePanel.add(mControllableButton);
    // mUncontrollableButton
    constraints.gridx++;
    nameLayout.setConstraints(mUncontrollableButton, constraints);
    mNamePanel.add(mUncontrollableButton);
    // mPropositionButton
    constraints.gridx++;
    nameLayout.setConstraints(mPropositionButton, constraints);
    mNamePanel.add(mPropositionButton);
    // mMoreOptionsButton
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = GridBagConstraints.REMAINDER;
    constraints.anchor = GridBagConstraints.EAST;
    mMoreOptionsButton.setText("More Options >>");
    nameLayout.setConstraints(mMoreOptionsButton, constraints);
    mNamePanel.add(mMoreOptionsButton);
  }


  /**
   * Fill and layout the name panel with components for the extended
   * version of the dialog.
   */ 
  private void layoutExtendedNamePanel()
  {
    final GridBagLayout layout = new GridBagLayout();
    mNamePanel.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;
    // mNameLabel
    constraints.gridy = 0;
    constraints.weightx = 0.0;
    constraints.anchor = GridBagConstraints.WEST;
    layout.setConstraints(mNameLabel, constraints);
    mNamePanel.add(mNameLabel);
    // mNameInput
    constraints.gridwidth = 3;
    constraints.weightx = 3.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mNameInput, constraints);
    mNamePanel.add(mNameInput);
    // mKindLabel
    constraints.gridx = 0;
    constraints.gridy++;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.NONE;
    layout.setConstraints(mKindLabel, constraints);
    mNamePanel.add(mKindLabel);
    // mControllableButton
    constraints.gridx++;
    constraints.weightx = 1.0;
    layout.setConstraints(mControllableButton, constraints);
    mNamePanel.add(mControllableButton);
    // mUncontrollableButton
    constraints.gridx++;
    layout.setConstraints(mUncontrollableButton, constraints);
    mNamePanel.add(mUncontrollableButton);
    // mPropositionButton
    constraints.gridx++;
    layout.setConstraints(mPropositionButton, constraints);
    mNamePanel.add(mPropositionButton);
    // mObservableButton
    constraints.gridx = 1;
    constraints.gridy++;
    constraints.gridwidth = 2;
    layout.setConstraints(mObservableButton, constraints);
    mNamePanel.add(mObservableButton);
    // mParameterButton
    constraints.gridy++;
    layout.setConstraints(mParameterButton, constraints);
    mNamePanel.add(mParameterButton);
    // mRequiredButton
    constraints.gridy++;
    layout.setConstraints(mRequiredButton, constraints);
    mNamePanel.add(mRequiredButton);
    // mColourButton
    constraints.gridx += 2;
    constraints.gridy -= 2;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.EAST;
    layout.setConstraints(mColourButton, constraints);
    mNamePanel.add(mColourButton);
    // mMoreOptionsButton
    constraints.gridy += 2;
    mMoreOptionsButton.setText("<< Less Options");
    layout.setConstraints(mMoreOptionsButton, constraints);
    mNamePanel.add(mMoreOptionsButton);
  }


  /**
   * Fill and layout the index panel for the extended version of the
   * dialog. This method is called only once when the index panel is
   * first created, and sets up the components and their action listeners
   * add the same time.
   */
  private void layoutIndexPanel()
  {
    final GridBagLayout layout = new GridBagLayout();
    mIndexPanel.setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.weighty = 1.0;
    constraints.insets = INSETS;
    // Label
    final JLabel label = new JLabel("Array ranges:");
    constraints.weightx = 0.0;
    constraints.gridheight = 4;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    layout.setConstraints(label, constraints);
    mIndexPanel.add(label);
    // List
    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    final JScrollPane scrolled = new JScrollPane(mIndexTable);
    final Border border = BorderFactory.createLoweredBevelBorder();
    scrolled.setBorder(border);
    layout.setConstraints(scrolled, constraints);
    mIndexPanel.add(scrolled);
    // List control buttons
    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.gridheight = 1;
    layout.setConstraints(mAddButton, constraints);
    mIndexPanel.add(mAddButton);
    constraints.gridy++;
    layout.setConstraints(mRemoveButton, constraints);
    mIndexPanel.add(mRemoveButton);
    constraints.gridy++;
    layout.setConstraints(mUpButton, constraints);
    mIndexPanel.add(mUpButton);
    constraints.gridy++;
    layout.setConstraints(mDownButton, constraints);
    mIndexPanel.add(mDownButton);
  }


  /**
   * Removes all contents from the window's content pane and from the
   * name pane, so they can be redefined when more or less options
   * have been chosen.
   */
  private void resetPanels()
  {
    final Container contents = getContentPane();
    contents.removeAll();
    mNamePanel.removeAll();
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Changes the amount of options shown by the dialog.
   * If showing the reduced dialog, it will switch to the full version, and
   * vice versa. This method used and flips the {@link
   * #mDisplayingMoreOptions} member, and recalculates and redisplays the
   * entire dialog window accordingly. It is attached to the action
   * listener of the 'more options' button.
   */
  private void toggleMoreOptions()
  {
    Runnable restorer = null;
    if (mNameInput.isFocusOwner()) {
      final String name = mNameInput.getText();
      final int pos = mNameInput.getCaretPosition();
      restorer = new Runnable() {
          public void run() {
            mNameInput.setText(name);
            mNameInput.setCaretPosition(pos);
          }
        };
    }
    resetPanels();
    mDisplayingMoreOptions = !mDisplayingMoreOptions;
    createComponents();
    layoutComponents();
    pack();
    mNameInput.requestFocusInWindow();
    if (restorer != null) {
      SwingUtilities.invokeLater(restorer);
    }
  }


  /**
   * Enables or disables the 'required' checkbox.
   * This method is attached to action listeners in response to the
   * selection or deselection of the 'parameter' checkbox.
   */
  private void updateRequiredEnabled()
  {
    final boolean enable = mParameterButton.isSelected();
    mRequiredButton.setEnabled(enable);
  }


  /**
   * Enables or disables the colour button.
   * This method is attached to action listeners in response to the
   * selection or deselection of the proposition radio button.
   */
  private void updateColourEnabled()
  {
    if (mColourButton != null) {
      final boolean enable = mPropositionButton.isSelected();
      mColourButton.setEnabled(enable);
    }
  }

  /**
   * Enables or disables the list control buttons.
   * This method is attached to a selection listener on the indexes
   * table. It makes sure that the 'remove', 'up', and 'down' buttons
   * are enabled only when something is selected.
   */
  private void updateListControlEnabled()
  {
    final int selcount = mIndexTable.getSelectedRowCount();
    if (selcount > 0) {
      mRemoveButton.setEnabled(true);
      final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
      final int maxindex = selmodel.getMaxSelectionIndex();
      final int minindex = selmodel.getMinSelectionIndex();
      final int lastrow = mIndexTable.getRowCount() - 1;
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
   * Activates the index table.
   * This method is attached to a mouse listener and called when the
   * user clicks the index table. It checks if the click was in the unused
   * area at the bottom of the viewport. If so, it gives focus to the table
   * and, in case of a double-click, it also starts editing.
   */
  private void handleIndexTableClick(final MouseEvent event)
  {
    if (event.getButton() == MouseEvent.BUTTON1) {
      final Point point = event.getPoint();
      final int row = mIndexTable.rowAtPoint(point);
      if (row < 0) {
        switch (event.getClickCount()) {
        case 1:
          if (!mIndexTable.isEditing() && !mIndexTable.isFocusOwner()) {
            mIndexTable.requestFocusInWindow();
          }
          break;
        case 2:
          addIndexRange();
          break;
        default:
          break;
        }
      }
    }
  }


  /**
   * Creates a new index range.
   * This method is attached to action listener of the 'add' button
   * of the index list control.
   */
  private void addIndexRange()
  {
    if (mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus()) {
      // nothing
    } else if (mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              addIndexRange();
            }
          });
      }
    } else {
      final int row = mIndexModel.createEditedItemAtEnd();
      if (mIndexTable.editCellAt(row, 0)) {
        final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
        selmodel.setSelectionInterval(row, row);
        final Component comp = mIndexTable.getEditorComponent();
        final Rectangle bounds = comp.getBounds();
        mIndexTable.scrollRectToVisible(bounds);
        comp.requestFocusInWindow();
      }
    }
  }

  /**
   * Removes all selected indexes.
   * This method is attached to action listener of the 'remove' button
   * of the index list control.
   */
  private void removeIndexRange()
  {
    final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
    if (mIndexTable.isEditing()) {
      final int row = mIndexTable.getEditingRow();
      if (selmodel.isSelectedIndex(row)) {
        final TableCellEditor editor = mIndexTable.getCellEditor();
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
          mIndexModel.removeRow(index);
        }
      }
    }
  }


  /**
   * Moves all selected indexes up by one step.
   * This method is attached to action listener of the 'up' button
   * of the index list control.
   */
  private void moveIndexRangeUp()
  {
    if (mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              moveIndexRangeUp();
            }
          });
      }
    } else {
      final int[] rows = mIndexTable.getSelectedRows();
      final int newfirst = mIndexModel.moveUp(rows);
      if (newfirst >= 0) {
        final int newlast = newfirst + rows.length - 1;
        final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
        selmodel.setSelectionInterval(newfirst, newlast);
      }
    }
  }

  /**
   * Moves all selected indexes down by one step.
   * This method is attached to action listener of the 'down' button
   * of the index list control.
   */
  private void moveIndexRangeDown()
  {
    if (mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              moveIndexRangeDown();
            }
          });
      }
    } else {
      final int[] rows = mIndexTable.getSelectedRows();
      final int newfirst = mIndexModel.moveDown(rows);
      if (newfirst >= 0) {
        final int newlast = newfirst + rows.length - 1;
        final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
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
    if (mNameInput.isFocusOwner() && !mNameInput.shouldYieldFocus()) {
      // nothing
    } else if (mIndexTable != null && mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
              commitDialog();
            }
          });
      }
    } else {
      EventDeclSubject decl = null;
      EventParameterSubject param = null;
      final boolean wasparam = mEditedItem instanceof EventParameterSubject;
      final boolean isparam =
        mIndexPanel == null ? wasparam : mParameterButton.isSelected();
      final String name = mNameInput.getText();
      if (wasparam) {
        param = (EventParameterSubject) mEditedItem;
        decl = param.getEventDecl();
        if (isparam) {
          param.setName(name);
        } else {
          decl = decl.clone();
        }
      } else {
        decl = (EventDeclSubject) mEditedItem;
        if (isparam) {
          decl = decl.clone();
          param = new EventParameterSubject(name, decl);
        }
      }
      decl.setName(name);
      if (mControllableButton.isSelected()) {
        decl.setKind(EventKind.CONTROLLABLE);
      } else if (mUncontrollableButton.isSelected()) {
        decl.setKind(EventKind.UNCONTROLLABLE);
      } else if (mPropositionButton.isSelected()) {
        decl.setKind(EventKind.PROPOSITION);
      } else {
        throw new IllegalStateException("Event kind not selected!");
      }
      if (mIndexPanel != null) {
        final boolean observable = mObservableButton.isSelected();
        decl.setObservable(observable);
        final List<SimpleExpressionSubject> ranges =
          decl.getRangesModifiable();
        final List<SimpleExpressionSubject> newranges = mIndexModel.getList();
        ranges.clear();
        ranges.addAll(newranges);
        if (isparam) {
          final boolean required = mRequiredButton.isSelected();
          param.setRequired(required);
        }
      }
      boolean add = false;
      ModuleSubject module = null;
      if (mCreating) {
        module = getModule();
        add = true;
      } else if (isparam != wasparam) {
        module = getModule();
        if (wasparam) {
          final EventParameterSubject old =
            (EventParameterSubject) mEditedItem;
          final IndexedListSubject<ParameterSubject> parameters =
            module.getParameterListModifiable();
          parameters.remove(old);
        } else {
          final EventDeclSubject old = (EventDeclSubject) mEditedItem;
          final IndexedListSubject<EventDeclSubject> events =
            module.getEventDeclListModifiable();
          events.add(old);
        }
        add = true;
      }
      if (add) {
        if (isparam) {
          final IndexedListSubject<ParameterSubject> parameters =
            module.getParameterListModifiable();
          parameters.add(param);
        } else {
          final IndexedListSubject<EventDeclSubject> events =
            module.getEventDeclListModifiable();
          events.add(decl);
        }
      }
      dispose();
    }
  }

  private void fireActionPerformed(final ActionEvent event)
  {
    for (final ActionListener listener : mActionListeners) {
      listener.actionPerformed(event);
    }
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
  //# Local Class EventNameInputParser
  private class EventNameInputParser
    extends DocumentFilter
    implements FormattedInputParser
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    public String parse(final String text)
      throws ParseException
    {
      final ExpressionParser parser = getExpressionParser();
      parser.parseSimpleIdentifier(text);
      final String oldname = mEditedItem.getName();
      if (!text.equals(oldname)) {
        final ModuleSubject module = getModule();
        final IndexedListSubject<EventDeclSubject> eventlist =
          module.getEventDeclListModifiable();
        if (eventlist.containsName(text)) {
          throw new ParseException
            ("Name '" + text + "' is already taken by an event!", 0);
        }
        final IndexedListSubject<ParameterSubject> paramlist =
          module.getParameterListModifiable();
        if (paramlist.containsName(text)) {
          throw new ParseException
            ("Name '" + text + "' is already taken by a parameter!", 0);
        }
      }
      return text;
    }

    public DocumentFilter getDocumentFilter()
    {
      return this;
    }


    //#######################################################################
    //# Overrides for class javax.swing.DocumentFilter
    public void insertString(final DocumentFilter.FilterBypass bypass,
                             final int offset,
                             final String text,
                             final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text, offset);
      if (filtered != null) {
        super.insertString(bypass, offset, filtered, attribs);
      }
    }

    public void replace(final DocumentFilter.FilterBypass bypass,
                        final int offset,
                        final int length,
                        final String text,
                        final AttributeSet attribs)
      throws BadLocationException
    {
      final String filtered = filter(text, offset);
      if (filtered != null) {
        super.replace(bypass, offset, length, filtered, attribs);
      }
    }

    public void remove(final DocumentFilter.FilterBypass bypass,
                       final int offset,
                       final int length)
      throws BadLocationException
    {
      boolean ok = true;
      if (offset == 0) {
        final String text = mNameInput.getText();
        if (length < text.length()) {
          final ExpressionParser parser = getExpressionParser();
          final char ch = text.charAt(length);
          ok = parser.isIdentifierStart(ch);
        }
      }
      if (ok) {
        super.remove(bypass, offset, length);
      }
    }


    //#######################################################################
    //# Auxiliary Methods
    private String filter(final String text, int offset)
    {
      if (text == null) {
        return null;
      } else {
        final ExpressionParser parser = getExpressionParser();
        final int len = text.length();
        final StringBuffer buffer = new StringBuffer(len);
        for (int i = 0; i < len; i++) {
          final char ch = text.charAt(i);
          if (offset == 0 ?
              parser.isIdentifierStart(ch) :
              parser.isIdentifierCharacter(ch)) {
            buffer.append(ch);
            offset++;
          }
        }
        if (buffer.length() == 0) {
          return null;
        } else {
          return buffer.toString();
        }
      }
    }

  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;
  private boolean mCreating;
  private boolean mDisplayingMoreOptions;

  // Swing components
  private JPanel mNamePanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JRadioButton mControllableButton;
  private JLabel mKindLabel;
  private ButtonGroup mKindGroup;
  private JRadioButton mUncontrollableButton;
  private JRadioButton mPropositionButton;
  private JCheckBox mObservableButton;
  private JCheckBox mParameterButton;
  private JCheckBox mRequiredButton;
  private JButton mColourButton;
  private JButton mMoreOptionsButton;
  private JPanel mIndexPanel;
  private ListTableModel<SimpleExpressionSubject> mIndexModel;
  private JTable mIndexTable;
  private JButton mAddButton;
  private JButton mRemoveButton;
  private JButton mUpButton;
  private JButton mDownButton;
  private JPanel mErrorPanel;
  private ErrorLabel mErrorLabel;
  private JPanel mButtonsPanel;

  // Action Listeners
  private final List<ActionListener> mActionListeners;

  // Created Item
 /**
   * The Waters subject edited by this dialog.
   * It may be of type {@link EventDeclSubject} or
   * {@link EventParameterSubject}.
   * This is a reference to the actual object that is being edited,
   * or, if a new item is being created, it is a blank object filled
   * with default values.
   * The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.
   */ 
  private NamedSubject mEditedItem;


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);

}
