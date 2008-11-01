//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EventEditorDialog
//###########################################################################
//# $Id$
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.AWTKeyStroke;
import java.awt.Color;
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
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
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
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;

import net.sourceforge.waters.gui.command.Command;
import net.sourceforge.waters.gui.command.InsertCommand;
import net.sourceforge.waters.gui.command.EditCommand;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.util.DialogCancelAction;
import net.sourceforge.waters.gui.util.IconRadioButton;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.model.expr.ParseException;
import net.sourceforge.waters.model.module.SimpleIdentifierProxy;
import net.sourceforge.waters.subject.module.ColorGeometrySubject;
import net.sourceforge.waters.subject.module.EventDeclSubject;
import net.sourceforge.waters.subject.module.ModuleSubject;
import net.sourceforge.waters.subject.module.SimpleExpressionSubject;
import net.sourceforge.waters.subject.module.SimpleIdentifierSubject;
import net.sourceforge.waters.xsd.base.EventKind;
import net.sourceforge.waters.xsd.module.ScopeKind;

import org.supremica.properties.Config;


public class EventEditorDialog
  extends JDialog
{

  //#########################################################################
  //# Constructor
  public EventEditorDialog(final ModuleWindowInterface root)
  {
    this(root, false);
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final EventDeclSubject decl)
  {
    this(root, usesMoreOptions(decl), decl);
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final boolean moreoptions)
  {
    this(root, moreoptions, null);
  }

  public EventEditorDialog(final ModuleWindowInterface root,
                           final boolean moreoptions,
                           final EventDeclSubject decl)
  {
    super(root.getRootWindow());
    if (decl == null) {
      setTitle("Creating new event declation");
    } else {
      setTitle("Editing event declation '" + decl.getName() + "'");
    }
    mRoot = root;
    mDisplayingMoreOptions = moreoptions;
    mEventDecl = decl;
    createComponents();
    layoutComponents();
    setLocationRelativeTo(mRoot.getRootWindow());
    mNameInput.requestFocusInWindow();
    setVisible(true);
    mActionListeners = new LinkedList<ActionListener>();
  }


  //#########################################################################
  //# Access to Created Item
  /**
   * Gets the Waters subject edited by this dialog.
   * @return A reference to the event declaration being edited by this
   *         dialog.
   */
  public EventDeclSubject getEditedItem()
  {
    return mEventDecl;
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Adds an action listener to this dialog. The action listeners of an
   * event editor dialog are triggered when the user commits the dialog,
   * after the event declaration has been created and added
   * to the module. Therefore, they can query the value of {@link
   * #getEventDecl()} to determine which subject was created. The
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
    final EventDeclSubject template =
      mEventDecl == null ? TEMPLATE : mEventDecl;
    ActionListener commithandler = null;
    if (mNamePanel == null) {
      // Initialising for the first time. Everything needs to be done.
      // Name panel, basic part ...
      final boolean advanced = Config.INCLUDE_INSTANTION.isTrue();
      mNamePanel = new RaisedDialogPanel();
      mNameLabel = new JLabel("Name:");
      final FormattedInputParser parser = new EventNameInputParser();
      mNameInput = new SimpleExpressionCell(template.getIdentifier(), parser);
      commithandler = new ActionListener() {
          public void actionPerformed(final ActionEvent event)
          {
            commitDialog();
            fireActionPerformed(event);
          }
        };
      mNameInput.addActionListener(commithandler);
      mKindLabel = new JLabel("Kind:");
      mKindGroup = new ButtonGroup();
      mControllableButton =
        new IconRadioButton("Controllable", IconLoader.ICON_CONTROLLABLE,
                            mKindGroup);
      mUncontrollableButton =
        new IconRadioButton("Uncontrollable", IconLoader.ICON_UNCONTROLLABLE,
                            mKindGroup);
      mPropositionButton =
        new IconRadioButton("Proposition", PropositionIcon.getDefaultIcon(),
                            mKindGroup);
      mPropositionButton.setEnabled(advanced);
      switch (template.getKind()) {
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
      mMoreOptionsButton.setEnabled(advanced);
      mMoreOptionsButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event)
          {
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

      // And record the colour ...
      final ColorGeometrySubject geo = template.getColorGeometry();
      if (geo == null || geo.getColorSet().isEmpty()) {
        mChosenColor = EditorColor.DEFAULTMARKINGCOLOR;
      } else {
        mChosenColor = geo.getColorSet().iterator().next();
      }
    }
    if (mDisplayingMoreOptions && mIndexPanel == null) {
      // Initialising with more options, and index panel not used before.
      // Need to create extra components for name panel ...
      mObservableButton = new JCheckBox("Observable");
      mObservableButton.setRequestFocusEnabled(false);
      mObservableButton.setSelected(template.isObservable());
      final ScopeKind scope = template.getScope();
      mParameterButton = new JCheckBox("Parameter");
      mParameterButton.setRequestFocusEnabled(false);
      mParameterButton.setSelected(scope != ScopeKind.LOCAL);
      mParameterButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event)
          {
            updateRequiredEnabled();
          }
        });

      mRequiredButton = new JCheckBox("Required");
      mRequiredButton.setRequestFocusEnabled(false);
      mRequiredButton.setSelected(scope != ScopeKind.OPTIONAL_PARAMETER);
      updateRequiredEnabled();
      mColorDisplay = new JPanel();
      mColorDisplay.setBackground(mChosenColor);
      mColorDisplay.addMouseListener(new MouseAdapter() {
          public void mouseClicked(final MouseEvent event)
          {
            if (event.getClickCount() == 2) {
              chooseColor();
            }
          }
        });
      mColorButton = new JButton("Color ...");
      mColorButton.setRequestFocusEnabled(false);
      mColorButton.addActionListener(new ActionListener() {
          public void actionPerformed(final ActionEvent event)
          {
            chooseColor();
          }
        });
      // ... add listeners to enable/disable the colour button ...
      final ActionListener kindlistener = new ActionListener() {
          public void actionPerformed(final ActionEvent event)
          {
            updateColorEnabled();
          }
        };
      mControllableButton.addActionListener(kindlistener);
      mUncontrollableButton.addActionListener(kindlistener);
      mPropositionButton.addActionListener(kindlistener);
      updateColorEnabled();
      // ... and create index panel.
      mIndexPanel = new RaisedDialogPanel();
      final List<SimpleExpressionSubject> ranges =
        template.getRangesModifiable();
      final List<SimpleExpressionSubject> copy =
        new ArrayList<SimpleExpressionSubject>(ranges);
      mIndexModel = new ListTableModel<SimpleExpressionSubject>
        (copy, SimpleExpressionSubject.class);
      mIndexTable = new NonTypingTable(mIndexModel);
      mIndexTable.setTableHeader(null);
      mIndexTable.setShowGrid(false);
      mIndexTable.setSurrendersFocusOnKeystroke(true);
      mIndexTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
      mIndexTable.setFillsViewportHeight(true);
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
      mIndexTable.addEscapeAction();
      final ExpressionParser parser = getExpressionParser();
      final SimpleExpressionEditor editor =
        new SimpleExpressionEditor(Operator.TYPE_RANGE, parser, mErrorLabel);
      editor.setAllowNull(true);
      editor.addCellEditorListener(mIndexModel);
      mIndexTable.setDefaultEditor(Object.class, editor);
      final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
      selmodel.addListSelectionListener(new ListSelectionListener() {
          public void valueChanged(final ListSelectionEvent event)
          {
            updateListControlEnabled();
          }
        });
      mIndexTable.addMouseListener(new MouseAdapter() {
          public void mouseClicked(final MouseEvent event)
          {
            handleIndexTableClick(event);
          }
        });
      mAddAction = new AddIndexRangeAction();
      final JRootPane root = getRootPane();
      final String name = (String) mAddAction.getValue(Action.NAME);
      final KeyStroke key =
        (KeyStroke) mAddAction.getValue(Action.ACCELERATOR_KEY);
      final InputMap imap =
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
      final ActionMap amap = root.getActionMap();
      imap.put(key, name);
      amap.put(name, mAddAction);
      mAddButton = new JButton(mAddAction);
      mAddButton.setRequestFocusEnabled(false);
      mRemoveAction = new RemoveIndexRangesAction();
      mIndexTable.addKeyboardAction(mRemoveAction);
      mRemoveButton = new JButton(mRemoveAction);
      mRemoveButton.setRequestFocusEnabled(false);
      mUpAction = new MoveIndexRangesUpAction();
      mIndexTable.addKeyboardAction(mUpAction);
      mUpButton = new JButton(mUpAction);
      mUpButton.setRequestFocusEnabled(false);
      mDownAction = new MoveIndexRangesDownAction();
      mIndexTable.addKeyboardAction(mDownAction);
      mDownButton = new JButton(mDownAction);
      mDownButton.setRequestFocusEnabled(false);
      updateListControlEnabled();
      layoutIndexPanel();
    }
  }


  /**
   * Fill the panels and layout all buttons and components.
   * This method uses the {@link #mDisplayingMoreOptions} member to
   * determine whether the full dialog or only the reduced version is to
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
    // mColorButton
    constraints.gridx += 2;
    constraints.gridy -= 2;
    constraints.gridwidth = 1;
    constraints.weightx = 0.0;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.anchor = GridBagConstraints.EAST;
    layout.setConstraints(mColorButton, constraints);
    mNamePanel.add(mColorButton);
    // mColorDisplay
    constraints.gridy += 1;
    layout.setConstraints(mColorDisplay, constraints);
    mNamePanel.add(mColorDisplay);
    // mMoreOptionsButton
    constraints.gridy += 1;
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
          public void run()
          {
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
   * Pops up a colour selection dialog to choose a colour for
   * a proposition event.
   */
  private void chooseColor()
  {
    final Color newcolor = JColorChooser.showDialog
      (this, "Choose Colour for Proposition", mChosenColor);
    if (newcolor != null) {
      mChosenColor = newcolor;
      mColorDisplay.setBackground(mChosenColor);
    }
  }

  /**
   * Enables or disables the colour button.
   * This method is attached to action listeners in response to the
   * selection or deselection of the proposition radio button.
   */
  private void updateColorEnabled()
  {
    if (mColorButton != null) {
      final boolean enable = mPropositionButton.isSelected();
      mColorButton.setEnabled(enable);
      mColorDisplay.setOpaque(enable);
      final Border border = enable ?
        BorderFactory.createLoweredBevelBorder() :
        BorderFactory.createEmptyBorder();
      mColorDisplay.setBorder(border);
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
      mRemoveAction.setEnabled(true);
      final ListSelectionModel selmodel = mIndexTable.getSelectionModel();
      final int maxindex = selmodel.getMaxSelectionIndex();
      final int minindex = selmodel.getMinSelectionIndex();
      final int lastrow = mIndexTable.getRowCount() - 1;
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
   * This method is attached as an action listener of the 'add' button
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
            public void run()
            {
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
   * This method is attached as an action listener of the 'remove' button
   * of the index list control.
   */
  private void removeIndexRanges()
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
    mErrorLabel.clearDisplay();
  }


  /**
   * Moves all selected indexes up by one step.
   * This method is attached as an action listener of the 'up' button
   * of the index list control.
   */
  private void moveIndexRangesUp()
  {
    if (mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              moveIndexRangesUp();
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
  private void moveIndexRangesDown()
  {
    if (mIndexTable.isEditing()) {
      final TableCellEditor editor = mIndexTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              moveIndexRangesDown();
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
   * This method is attached as an action listener of the 'OK' button
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
            public void run()
            {
              commitDialog();
            }
          });
      }
    } else {
      final SimpleIdentifierSubject ident = 
        (SimpleIdentifierSubject) mNameInput.getValue();
      final EventKind kind;
      if (mControllableButton.isSelected()) {
        kind = EventKind.CONTROLLABLE;
      } else if (mUncontrollableButton.isSelected()) {
        kind = EventKind.UNCONTROLLABLE;
      } else if (mPropositionButton.isSelected()) {
        kind = EventKind.PROPOSITION;
      } else {
        throw new IllegalStateException("Event kind not selected!");
      }
      final boolean observable;
      final ScopeKind scope;
      final List<SimpleExpressionSubject> ranges;
      if (mIndexPanel == null) {
        observable = true;
        scope = ScopeKind.LOCAL;
        ranges = null;
      } else {
        observable = mObservableButton.isSelected();
        if (!mParameterButton.isSelected()) {
          scope = ScopeKind.LOCAL;
        } else if (mRequiredButton.isSelected()) {
          scope = ScopeKind.REQUIRED_PARAMETER;
        } else {
          scope = ScopeKind.OPTIONAL_PARAMETER;
        }
        // *** BUG ***
        // This copying would not be necessary if assignFrom() could accept
        // proxies.
        final List<SimpleExpressionSubject> origranges = mIndexModel.getList();
        ranges = new ArrayList<SimpleExpressionSubject>(origranges.size());
        for (final SimpleExpressionSubject range : origranges) {
          if (range.getParent() == null) {
            ranges.add(range);
          } else {
            ranges.add(range.clone());
          }
        }
        // ***
      }
      final ColorGeometrySubject geo;
      if (kind != EventKind.PROPOSITION ||
          mChosenColor.equals(EditorColor.DEFAULTMARKINGCOLOR)) {
        geo = null;
      } else {
        final Set<Color> set = Collections.singleton(mChosenColor);
        geo = new ColorGeometrySubject(set);
      }
      final SelectionOwner panel = mRoot.getEventsPanel();
      final EventDeclSubject template =
        new EventDeclSubject(ident, kind, observable, scope, ranges, geo);
      if (mEventDecl == null) {
        final Command command = new InsertCommand(template, panel);
        mEventDecl = template;
        mRoot.getUndoInterface().executeCommand(command);
      } else if (!mEventDecl.equalsWithGeometry(template)) {
        final Command command = new EditCommand(mEventDecl, template, panel);
        mRoot.getUndoInterface().executeCommand(command);
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
  private ExpressionParser getExpressionParser()
  {
    return mRoot.getExpressionParser();
  }


  //#########################################################################
  //# Auxiliary Static Methods
  private static boolean usesMoreOptions(final EventDeclSubject decl)
  {
    return
      !decl.isObservable() ||
      decl.getScope() != ScopeKind.LOCAL ||
      !decl.getRanges().isEmpty();
  }


  //#########################################################################
  //# Local Class EventNameInputParser
  private class EventNameInputParser
    extends DocumentFilter
    implements FormattedInputParser
  {

    //#######################################################################
    //# Interface net.sourceforge.waters.gui.FormattedInputParser
    public SimpleIdentifierProxy parse(final String text)
      throws ParseException
    {
      final ExpressionParser parser = getExpressionParser();
      final SimpleIdentifierProxy ident = parser.parseSimpleIdentifier(text);
      final String newname = ident.getName();
      final String oldname = mEventDecl == null ? "" : mEventDecl.getName();
      if (!newname.equals(oldname)) {
        mModuleContext.checkNewEventName(newname);
      }
      return ident;
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

    //#######################################################################
    //# Data Members
    private final ModuleContext mModuleContext = mRoot.getModuleContext();

  }


  //#########################################################################
  //# Inner Class AddIndexAction
  private class AddIndexRangeAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private AddIndexRangeAction()
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
      addIndexRange();
    }

  }


  //#########################################################################
  //# Inner Class RemoveIndexRangesAction
  private class RemoveIndexRangesAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private RemoveIndexRangesAction()
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
      removeIndexRanges();
    }

  }


  //#########################################################################
  //# Inner Class MoveIndexRangesUpAction
  private class MoveIndexRangesUpAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private MoveIndexRangesUpAction()
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
      moveIndexRangesUp();
    }

  }


  //#########################################################################
  //# Inner Class MoveIndexRangesDownAction
  private class MoveIndexRangesDownAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private MoveIndexRangesDownAction()
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
      moveIndexRangesDown();
    }

  }


  //#########################################################################
  //# Data Members
  // Dialog state
  private final ModuleWindowInterface mRoot;
  private boolean mDisplayingMoreOptions;

  // Swing components
  private JPanel mNamePanel;
  private JLabel mNameLabel;
  private SimpleExpressionCell mNameInput;
  private JLabel mKindLabel;
  private ButtonGroup mKindGroup;
  private IconRadioButton mControllableButton;
  private IconRadioButton mUncontrollableButton;
  private IconRadioButton mPropositionButton;
  private JCheckBox mObservableButton;
  private JCheckBox mParameterButton;
  private JCheckBox mRequiredButton;
  private JButton mColorButton;
  private JPanel mColorDisplay;
  private JButton mMoreOptionsButton;
  private JPanel mIndexPanel;
  private ListTableModel<SimpleExpressionSubject> mIndexModel;
  private NonTypingTable mIndexTable;
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

  // Action Listeners
  private final List<ActionListener> mActionListeners;

  // Created Item
  /**
   * <P>The Waters event declaration subject edited by this dialog.</P>
   *
   * <P>This is a reference to the actual object that is being edited.  If
   * a new event declaration is being created, it is <CODE>null</CODE>
   * until the dialog is commited and the actually created subject is
   * assigned.</P>
   *
   * <P>The edited state is stored only in the dialog. Changes are only
   * committed to the model when the OK button is pressed.</P>
   */
  private EventDeclSubject mEventDecl;
  /**
   * The colour chosen by the user, or <CODE>null</CODE>.
   * This variable is initialised to the colour of the edited event
   * declaration, and changed whenever the colour selection dialog closes.
   */
  private Color mChosenColor;


  //#########################################################################
  //# Class Constants
  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final SimpleIdentifierSubject TEMPLATE_IDENT =
    new SimpleIdentifierSubject("");
  private static final EventDeclSubject TEMPLATE =
    new EventDeclSubject(TEMPLATE_IDENT, EventKind.CONTROLLABLE);

}
