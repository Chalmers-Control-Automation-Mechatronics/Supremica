//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# PROJECT: Waters/Supremica GUI
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   AttributesPanel
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
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxEditor;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import net.sourceforge.waters.despot.HISCAttributes;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.gui.util.RaisedDialogPanel;
import net.sourceforge.waters.model.base.Proxy;


/**
 * A panel for editing an attribute map.
 * This panel consists of a label &quot;Attributes:&quot;, an editable table
 * with columns for keys and values, and two buttons to add and remove entries
 * from the table. The panel can be added to dialogs.
 *
 * @author Robi Malik
 */

class AttributesPanel extends RaisedDialogPanel
{

  //#########################################################################
  //# Constructors
  AttributesPanel(final Class<? extends Proxy> clazz,
                  final Map<String,String> attribs)
  {
    this(clazz);
    setTableData(attribs);
  }

  AttributesPanel(final Class<? extends Proxy> clazz)
  {
    mAttributeValues = new TreeMap<String,List<String>>();
    for (final String attrib : HISCAttributes.getApplicableKeys(clazz)) {
      final List<String> values = HISCAttributes.getApplicableValues(attrib);
      mAttributeValues.put(attrib, values);
    }

    final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0);
    mTable = new NonTypingTable(model);
    mTable.setTableHeader(null);
    mTable.setShowGrid(false);
    mTable.setSurrendersFocusOnKeystroke(true);
    mTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
    mTable.setFillsViewportHeight(true);
    mTable.setBackground(EditorColor.BACKGROUNDCOLOR);
    final Dimension minsize = new Dimension(0, 0);
    mTable.setMinimumSize(minsize);
    final Dimension prefsize = new Dimension(240, 0);
    mTable.setPreferredScrollableViewportSize(prefsize);
    mTable.setRowSelectionAllowed(true);
    mTable.addEscapeAction();
    final TableCellEditor editor = new AttributeEditor();
    mTable.setDefaultEditor(Object.class, editor);
    mTable.setDefaultEditor(String.class, editor);
    final ListSelectionModel selmodel = mTable.getSelectionModel();
    selmodel.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(final ListSelectionEvent event)
      {
        updateListControlEnabled();
      }
    });
    mTable.addMouseListener(new MouseAdapter() {
      public void mouseClicked(final MouseEvent event)
      {
        handleAttributesTableClick(event);
      }
    });
    mAddAction = new AddAttributeAction();
    mTable.addKeyboardAction(mAddAction);
    mAddButton = new JButton(mAddAction);
    mAddButton.setRequestFocusEnabled(false);
    mRemoveAction = new RemoveAttributesAction();
    mTable.addKeyboardAction(mRemoveAction);
    mRemoveButton = new JButton(mRemoveAction);
    mRemoveButton.setRequestFocusEnabled(false);

    // Layout components ...
    final GridBagLayout layout = new GridBagLayout();
    setLayout(layout);
    final GridBagConstraints constraints = new GridBagConstraints();
    constraints.insets = INSETS;
    constraints.gridx = 0;
    constraints.gridy = 0;
    constraints.gridwidth = 1;
    constraints.weighty = 1.0;
    // Label
    constraints.weightx = 0.0;
    constraints.gridheight = 2;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    final JLabel label = new JLabel("Attributes:");
    layout.setConstraints(label, constraints);
    add(label);
    // Table
    constraints.gridx++;
    constraints.weightx = 1.0;
    constraints.fill = GridBagConstraints.BOTH;
    final JScrollPane scrolled = new JScrollPane(mTable);
    final Border border = BorderFactory.createLoweredBevelBorder();
    scrolled.setBorder(border);
    layout.setConstraints(scrolled, constraints);
    add(scrolled);
    // List control buttons
    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.gridheight = 1;
    layout.setConstraints(mAddButton, constraints);
    add(mAddButton);
    constraints.gridy++;
    layout.setConstraints(mRemoveButton, constraints);
    add(mRemoveButton);
  }


  //#########################################################################
  //# Simple Access
  Map<String,String> getTableData()
  {
    final Map<String,String> map = new TreeMap<String,String>();
    final TableModel model = mTable.getModel();
    final int rows = model.getRowCount();
    for (int row = 0; row < rows; row++) {
      final String attrib = (String) model.getValueAt(row, 0);
      final String value = (String) model.getValueAt(row, 1);
      map.put(attrib, value);
    }
    return map;
  }

  void setTableData(final Map<String,String> attribs)
  {
    final int size = attribs.size();
    final String[][] data = new String[size][2];
    final int row = 0;
    for (final Map.Entry<String,String> entry : attribs.entrySet()) {
      data[row][0] = entry.getKey();
      data[row][1] = entry.getValue();
    }
    final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
    model.setDataVector(data, COLUMNS);
  }


  //#########################################################################
  //# Configuration
  void setFocusTraversalKeys(final Container container)
  {
    final Set<AWTKeyStroke> forward = container.getFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS);
    final Set<AWTKeyStroke> backward = container.getFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS);
    setFocusTraversalKeys(forward, backward);
  }

  void setFocusTraversalKeys(final Set<AWTKeyStroke> forward,
                             final Set<AWTKeyStroke> backward)
  {
    mTable.setFocusTraversalKeys
      (KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, forward);
    mTable.setFocusTraversalKeys
      (KeyboardFocusManager.BACKWARD_TRAVERSAL_KEYS, backward);
  }

  /**
   * Checks whether it is unsafe to commit the currently
   * edited text field. If this method returns <CODE>true</CODE>,
   * shifting the focus is to be avoided. This method can be
   * overridden by subclasses implementing complex dialogs. The
   * default implementation always returns <CODE>false</CODE>.
   * @return <CODE>true</CODE> if the component currently owning the focus
   *         is to be parsed and has been found to contain invalid information,
   *         <CODE>false</CODE> otherwise.
   */
  boolean isInputLocked()
  {
    return false;
  }


  //#########################################################################
  //# Action Listeners
  /**
   * Enables or disables the list control buttons.
   * This method is attached to a selection listener on the attributes
   * table. It makes sure that the 'remove' button only is enabled
   * when something is selected.
   */
  private void updateListControlEnabled()
  {
    final int selcount = mTable.getSelectedRowCount();
    mRemoveAction.setEnabled(selcount > 0);
  }

  /**
   * Activates the attributes table.
   * This method is attached to a mouse listener and called when the user
   * clicks the attributes table. It checks if the click was in the unused
   * area at the bottom of the viewport. If so, it gives focus to the table
   * and, in case of a double-click, it also starts editing.
   */
  private void handleAttributesTableClick(final MouseEvent event)
  {
    if (event.getButton() == MouseEvent.BUTTON1) {
      final Point point = event.getPoint();
      final int row = mTable.rowAtPoint(point);
      if (row < 0) {
        switch (event.getClickCount()) {
        case 1:
          if (!mTable.isEditing() && !mTable.isFocusOwner()) {
            mTable.requestFocusInWindow();
          }
          break;
        case 2:
          addAttribute();
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
   * of the attributes list control.
   */
  private void addAttribute()
  {
    if (isInputLocked()) {
      // nothing
    } else if (mTable.isEditing()) {
      final TableCellEditor editor = mTable.getCellEditor();
      if (editor.stopCellEditing()) {
        // Must wait for focus change events to be processed ...
        SwingUtilities.invokeLater(new Runnable() {
            public void run()
            {
              addAttribute();
            }
          });
      }
    } else {
      final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
      final int row = model.getRowCount();
      model.addRow(EMPTY_ROW);
      if (mTable.editCellAt(row, 0)) {
        final ListSelectionModel selmodel = mTable.getSelectionModel();
        selmodel.setSelectionInterval(row, row);
        final Component comp = mTable.getEditorComponent();
        final Rectangle bounds = comp.getBounds();
        mTable.scrollRectToVisible(bounds);
        comp.requestFocusInWindow();
      }
    }
  }

  /**
   * Removes all selected attributes.
   * This method is attached to action listener of the 'remove' button
   * of the attributes list control.
   */
  private void removeAttributes()
  {
    final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
    final ListSelectionModel selmodel = mTable.getSelectionModel();
    if (mTable.isEditing()) {
      final int row = mTable.getEditingRow();
      if (selmodel.isSelectedIndex(row)) {
        final TableCellEditor editor = mTable.getCellEditor();
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
          model.removeRow(index);
        }
      }
    }
  }


  //#########################################################################
  //# Inner Class AddAttributeAction
  private class AddAttributeAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private AddAttributeAction()
    {
      putValue(Action.NAME, "Add");
      putValue(Action.SHORT_DESCRIPTION, "Create a new attribute");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_INSERT);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
      setEnabled(true);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      addAttribute();
    }

    //#########################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class RemoveAttributesAction
  private class RemoveAttributesAction extends AbstractAction
  {

    //#######################################################################
    //# Constructor
    private RemoveAttributesAction()
    {
      putValue(Action.NAME, "Remove");
      putValue(Action.SHORT_DESCRIPTION, "Delete all selected attributes");
      putValue(Action.MNEMONIC_KEY, KeyEvent.VK_DELETE);
      putValue(Action.ACCELERATOR_KEY,
               KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
      setEnabled(false);
    }

    //#######################################################################
    //# Interface java.awt.event.ActionListener
    public void actionPerformed(final ActionEvent event)
    {
      removeAttributes();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Inner Class AttributeEditor
  private class AttributeEditor
    extends DefaultCellEditor
    implements FocusListener
  {

    //#######################################################################
    //# Constructors
    private AttributeEditor()
    {
      super(new JComboBox());
      final JComboBox combo = getComboBox();
      combo.setEditable(true);
      final JTextField textfield = getTextField();
      textfield.addFocusListener(this);
    }

    //#######################################################################
    //# Overrides for base class javax.swing.DefaultCellEditor
    public JComboBox getTableCellEditorComponent
      (final JTable table, final Object value, final boolean isSelected,
       final int row, final int column)
    {
      final JComboBox combo = (JComboBox) super.getTableCellEditorComponent
                                      (table, value, isSelected, row, column);
      final Collection<String> completions;
      switch (column) {
      case 0:
        completions = mAttributeValues.keySet();
        break;
      case 1:
        final TableModel model = mTable.getModel();
        final String attrib = (String) model.getValueAt(row, 0);
        completions = mAttributeValues.get(attrib);
        break;
      default:
        throw new IllegalArgumentException
          ("Unknown column " + column + " in attribute table!");
      }
      final String text = value.toString();
      final DefaultComboBoxModel model =
        completions == null ?
        new DefaultComboBoxModel() :
        new DefaultComboBoxModel(completions.toArray());
      model.setSelectedItem(text);
      combo.setModel(model);
      return combo;
    }

    public Object getCellEditorValue()
    {
      final JTextField textfield = getTextField();
      return textfield.getText();
    }

    //#########################################################################
    //# Interface java.awt.event.FocusListener
    /**
     * Does nothing.
     */
    public void focusGained(final FocusEvent event)
    {
    }

    /**
     * Fixes a bug in Swing.
     * Called when the editor component loses focus,
     * this handler makes sure that every non-temporary loss of focus
     * causes editing to stop.
     */
    public void focusLost(final FocusEvent event)
    {
      if (!event.isTemporary()) {
        // ???
        // When the focus is transferred within the table, the opposite
        // component is mAddButton, which does not have request focus enabled.
        // When the focus is transferred outside the table, the opposite
        // component is some editable component with request focus enabled.
        // Only in the latter case, we need to stop editing in the table.
        final Component opposite = event.getOppositeComponent();
        if (opposite instanceof JComponent) {
          final JComponent jopposite = (JComponent) opposite;
          if (jopposite.isRequestFocusEnabled()); {
            stopCellEditing();
          }
        }
      }
    }

    //#######################################################################
    //# Auxiliary Methods
    private JComboBox getComboBox()
    {
      return (JComboBox) getComponent();
    }

    private JTextField getTextField()
    {
      final JComboBox combo = getComboBox();
      final ComboBoxEditor editor = combo.getEditor();
      return (JTextField) editor.getEditorComponent();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Data Members
  private final Map<String,List<String>> mAttributeValues;

  private final NonTypingTable mTable;
  private final JButton mAddButton;
  private final JButton mRemoveButton;

  private final Action mAddAction;
  private final Action mRemoveAction;


  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  private static final String[] COLUMNS = {"Key", "Value"};
  private static final String[] EMPTY_ROW = {"", ""};

}
