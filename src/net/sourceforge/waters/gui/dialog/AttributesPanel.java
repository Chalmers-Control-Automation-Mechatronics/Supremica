//# -*- indent-tabs-mode: nil  c-basic-offset: 2 -*-
//###########################################################################
//# Copyright (C) 2004-2019 Robi Malik
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
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
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
import javax.swing.JPanel;
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

import net.sourceforge.waters.analysis.diagnosis.DiagnosabilityAttributeFactory;
import net.sourceforge.waters.analysis.hisc.HISCAttributeFactory;
import net.sourceforge.waters.gui.EditorColor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.gui.util.NonTypingTable;
import net.sourceforge.waters.model.base.AttributeFactory;
import net.sourceforge.waters.model.base.Proxy;

import org.supremica.automata.BDD.EFA.ForcibleEventAttributeFactory;
import org.supremica.automata.BDD.EFA.TimeInvariantAttributeFactory;
import org.supremica.gui.ide.DefaultAttributeFactory;
import org.supremica.properties.Config;


/**
 * A panel for editing an attribute map.
 * This panel consists of a label &quot;Attributes:&quot;, an editable table
 * with columns for keys and values, and two buttons to add and remove entries
 * from the table. The panel can be added to dialogs.
 *
 * @author Carly Hona, Robi Malik
 */

public class AttributesPanel extends JPanel
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
    mAttributeValues = getAttributeInfo(clazz);
    final DefaultTableModel model = new DefaultTableModel(COLUMNS, 0);
    mTable = new NonTypingTable(model);
    mTable.setTableHeader(null);
    mTable.setShowGrid(false);
    final int rowHeight = IconAndFontLoader.getPreferredTableRowHeight();
    mTable.setRowHeight(rowHeight);
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
    mEditor = new AttributeEditor();
    mTable.setDefaultEditor(Object.class, mEditor);
    mTable.setDefaultEditor(String.class, mEditor);
    final ListSelectionModel selmodel = mTable.getSelectionModel();
    selmodel.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent event)
      {
        updateListControlEnabled();
      }
    });
    mTable.addMouseListener(new MouseAdapter() {
      @Override
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
    constraints.weightx = 1.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 4;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    constraints.fill = GridBagConstraints.BOTH;
    final JScrollPane scrolled = new JScrollPane(mTable);
    final Border border = BorderFactory.createLoweredBevelBorder();
    scrolled.setBorder(border);
    layout.setConstraints(scrolled, constraints);
    add(scrolled);
    // List control buttons
    constraints.gridx++;
    constraints.weightx = 0.0;
    constraints.weighty = 1.0;
    constraints.gridheight = 1;
    constraints.anchor = GridBagConstraints.SOUTHWEST;
    constraints.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(mAddButton, constraints);
    add(mAddButton);
    constraints.gridy++;
    constraints.anchor = GridBagConstraints.NORTHWEST;
    layout.setConstraints(mRemoveButton, constraints);
    add(mRemoveButton);
  }


  //#########################################################################
  //# Simple Access
  Map<String,String> getTableData()
  {
    mEditor.stopCellEditing();
    removeEmptyRows();
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
    int row = 0;
    for (final Map.Entry<String,String> entry : attribs.entrySet()) {
      data[row][0] = entry.getKey();
      data[row][1] = entry.getValue();
      row++;
    }
    final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
    model.setDataVector(data, COLUMNS);
  }

  void removeEmptyRows(){
    final DefaultTableModel model = (DefaultTableModel) mTable.getModel();
    final int rows = model.getRowCount();
    for(int r = rows-1; r >= 0; r--){
      final String value = (String) model.getValueAt(r, 0);
      if(value.equals("")){
        model.removeRow(r);;
      }
    }
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
            @Override
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
    @Override
    public void actionPerformed(final ActionEvent event)
    {
      addAttribute();
    }

    //#######################################################################
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
    @Override
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
  {

    //#######################################################################
    //# Constructors
    private AttributeEditor()
    {
      super(new JComboBox<String>());
      final JComboBox<String> combo = getComboBox();
      combo.setEditable(true);
    }

    //#######################################################################
    //# Overrides for base class javax.swing.DefaultCellEditor
    @Override
    public JComboBox<String> getTableCellEditorComponent
      (final JTable table, final Object value, final boolean isSelected,
       final int row, final int column)
    {
      @SuppressWarnings("unchecked")
      final JComboBox<String> combo =
        (JComboBox<String>) super.getTableCellEditorComponent
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
      final DefaultComboBoxModel<String> model =
        new DefaultComboBoxModel<String>();
      if (completions != null) {
        for (final String completion : completions) {
          model.addElement(completion);
        }
      }
      final String text = value.toString();
      model.setSelectedItem(text);
      combo.setModel(model);
      return combo;
    }

    @Override
    public Object getCellEditorValue()
    {
      final JTextField textfield = getTextField();
      return textfield.getText();
    }

    //#######################################################################
    //# Auxiliary Methods
    @SuppressWarnings("unchecked")
    private JComboBox<String> getComboBox()
    {
      return (JComboBox<String>) getComponent();
    }

    private JTextField getTextField()
    {
      final JComboBox<String> combo = getComboBox();
      final ComboBoxEditor editor = combo.getEditor();
      return (JTextField) editor.getEditorComponent();
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 1L;

  }


  //#########################################################################
  //# Attribute Maps
  private static class AttributeInfo extends TreeMap<String,List<String>>
  {
    private static final long serialVersionUID = 1L;
  }

  private static final List<AttributeFactory>
    ATTRIBUTE_FACTORIES = new LinkedList<AttributeFactory>();
  private static final Map<Class<? extends Proxy>,AttributeInfo>
    ATTRIBUTE_INFO_MAP = new HashMap<Class<? extends Proxy>,AttributeInfo>();

  static {
    //=======================================================================
    // Register Attribute Factories
    //=======================================================================
    // TODO Make responsive to configuration changes ...
    ATTRIBUTE_FACTORIES.add(DefaultAttributeFactory.getInstance());
    if (Config.GUI_ANALYZER_INCLUDE_DIAGNOSABILIY.isTrue()) {
      ATTRIBUTE_FACTORIES.add(DiagnosabilityAttributeFactory.getInstance());
    }
    if (Config.GUI_ANALYZER_INCLUDE_HISC.isTrue()) {
      ATTRIBUTE_FACTORIES.add(HISCAttributeFactory.getInstance());
    }
    // A condition could be added to check if the model contains any clocks
    if (Config.INCLUDE_RAS_SUPPORT.isTrue()) {
      ATTRIBUTE_FACTORIES.add(TimeInvariantAttributeFactory.getInstance());
      ATTRIBUTE_FACTORIES.add(ForcibleEventAttributeFactory.getInstance());
    }
    //=======================================================================
  }

  private static AttributeInfo getAttributeInfo
    (final Class<? extends Proxy> iface)
  {
    AttributeInfo info = ATTRIBUTE_INFO_MAP.get(iface);
    if (info == null) {
      info = new AttributeInfo();
      for (final AttributeFactory factory : ATTRIBUTE_FACTORIES) {
        for (final String attrib : factory.getApplicableKeys(iface)) {
          final List<String> values = factory.getApplicableValues(attrib);
          info.put(attrib, values);
        }
      }
      ATTRIBUTE_INFO_MAP.put(iface, info);
    }
    return info;
  }


  //#########################################################################
  //# Data Members
  private final AttributeInfo mAttributeValues;
  private final TableCellEditor mEditor;
  private final NonTypingTable mTable;
  private final JButton mAddButton;
  private final JButton mRemoveButton;

  private final Action mAddAction;
  private final Action mRemoveAction;




  //#########################################################################
  //# Class Constants
  private static final long serialVersionUID = 1L;

  private static final Insets INSETS = new Insets(2, 4, 2, 4);
  public static final String LABEL_NAME = "Attributes:";
  private static final String[] COLUMNS = {"Key", "Value"};
  private static final String[] EMPTY_ROW = {"", ""};

}
