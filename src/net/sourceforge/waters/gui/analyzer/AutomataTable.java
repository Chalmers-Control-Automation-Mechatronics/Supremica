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

package net.sourceforge.waters.gui.analyzer;

import gnu.trove.list.array.TIntArrayList;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.sourceforge.waters.analysis.tr.TRAutomatonProxy;
import net.sourceforge.waters.gui.ModuleContext;
import net.sourceforge.waters.gui.actions.WatersPopupActionManager;
import net.sourceforge.waters.gui.command.UndoInterface;
import net.sourceforge.waters.gui.observer.EditorChangedEvent;
import net.sourceforge.waters.gui.observer.Observer;
import net.sourceforge.waters.gui.observer.SelectionChangedEvent;
import net.sourceforge.waters.gui.transfer.InsertInfo;
import net.sourceforge.waters.gui.transfer.SelectionOwner;
import net.sourceforge.waters.gui.transfer.WatersDataFlavor;
import net.sourceforge.waters.gui.util.IconAndFontLoader;
import net.sourceforge.waters.model.base.ComponentKind;
import net.sourceforge.waters.model.base.Proxy;
import net.sourceforge.waters.model.des.AutomatonProxy;
import net.sourceforge.waters.model.des.EventProxy;
import net.sourceforge.waters.model.des.ProductDESProxyFactory;
import net.sourceforge.waters.plain.des.ProductDESElementFactory;

import org.supremica.gui.ide.IDE;
import org.supremica.gui.ide.ModuleContainer;


public class AutomataTable extends JTable implements SelectionOwner
{

  //#########################################################################
  //# Constructor
  AutomataTable(final ModuleContainer moduleContainer,
                final WatersAnalyzerPanel parent)
  {
    super(new AutomataTableModel(moduleContainer));
    mModuleContainer = moduleContainer;
    mParent = parent;
    final TextCellRenderer textRenderer = new TextCellRenderer();
    setDefaultRenderer(String.class, textRenderer);
    final TextCellRenderer numberRenderer = new TextCellRenderer(RIGHT_PADDING);
    setDefaultRenderer(Integer.class, numberRenderer);
    final TableCellRenderer iconRenderer = new IconCellRenderer();
    setDefaultRenderer(ComponentKind.class, iconRenderer);
    setShowGrid(false);
    setIntercellSpacing(new Dimension(0, 0));
    setFillsViewportHeight(true);
    final int rowHeight = IconAndFontLoader.getPreferredTableRowHeight();
    setRowHeight(rowHeight);
    getTableHeader().setReorderingAllowed(false);
    resetColumnWidths();

    final IDE ide = mModuleContainer.getIDE();
    final WatersPopupActionManager manager = ide.getPopupActionManager();
    manager.installCutCopyPasteActions(this);
    addMouseListener(new TableMouseListener());
    getModel().addTableModelListener(new TableChangeListener());

    final ListSelectionModel listModel = getSelectionModel();
    listModel.setSelectionMode
      (ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
    listModel.addListSelectionListener(new ListSelectionListener() {
      @Override
      public void valueChanged(final ListSelectionEvent event)
      {
        if (!event.getValueIsAdjusting()) {
          fireEditorChangedEvent(new SelectionChangedEvent(this));
        }
      }
    });
  }


  //#########################################################################
  //# Overrides for javax.swing.JTable
  @Override
  public AutomataTableModel getModel()
  {
    return (AutomataTableModel) super.getModel();
  }

  @Override
  public String getToolTipText(final MouseEvent event)
  {
    final AutomatonProxy aut = getAutomaton(event);
    if (aut != null) {
      final ComponentKind kind = aut.getKind();
      return ModuleContext.getComponentKindToolTip(kind) + " " + aut.getName();
    } else {
      return null;
    }
  }

  @Override
  public Component prepareRenderer(final TableCellRenderer renderer,
                                   final int row,
                                   final int column)
  {
    final Component component = super.prepareRenderer(renderer, row, column);
    if (column > NAME_COLUMN) {
      final int rendererWidth = component.getPreferredSize().width;
      final int columnGap = getIntercellSpacing().width;
      final int width = rendererWidth + columnGap;
      final TableColumn tableColumn = getColumnModel().getColumn(column);
      if (width > tableColumn.getPreferredWidth()) {
        tableColumn.setMinWidth(width);
        tableColumn.setPreferredWidth(width);
        tableColumn.setMaxWidth(width);
      }
    }
    return component;
  }


  //#########################################################################
  //# Auxiliary Methods
  private void resetColumnWidths()
  {
    final TableColumnModel columnModel = getColumnModel();
    final int columnCount = columnModel.getColumnCount();
    if (columnCount != 0) {
      final TableColumn column0 = columnModel.getColumn(ICON_COLUMN);
      final int rowHeight = getRowHeight();
      column0.setMinWidth(rowHeight);
      column0.setMaxWidth(rowHeight);
      setColumnWidthFromTitle(NAME_COLUMN);
      for (int i = NAME_COLUMN + 1; i < columnCount; i++) {
        setColumnWidthFromTitle(i);
      }
    }
  }

  private void setColumnWidthFromTitle(final int column)
  {
    final TableColumnModel columnModel = getColumnModel();
    final TableColumn tableColumn = columnModel.getColumn(column);
    TableCellRenderer renderer = tableColumn.getHeaderRenderer();
    if (renderer == null) {
      renderer = getTableHeader().getDefaultRenderer();
    }
    final Object value = tableColumn.getHeaderValue();
    final Component comp =
      renderer.getTableCellRendererComponent(this, value, false,
                                             false, -1, column);
    final int rendererWidth = comp.getPreferredSize().width;
    final int columnGap = getIntercellSpacing().width;
    final int width = rendererWidth + columnGap;
    tableColumn.setMinWidth(width);
    if (column == NAME_COLUMN) {
      tableColumn.setPreferredWidth(width + rendererWidth);
    } else {
      tableColumn.setPreferredWidth(width);
      tableColumn.setMaxWidth(width);
    }
  }

  private AutomatonProxy getAutomaton(final MouseEvent event)
  {
    final int row = rowAtPoint(event.getPoint());
    if (row >= 0) {
      final AutomataTableModel model = getModel();
      return model.getAutomaton(row);
    } else {
      return null;
    }
  }

  private void maybeShowPopup(final MouseEvent event)
  {
    final AutomatonProxy clicked = getAutomaton(event);
    if ((clicked == null)) {
      clearSelection();
    }
    final IDE ide = mModuleContainer.getIDE();
    final WatersPopupActionManager manager = ide.getPopupActionManager();
    final AnalyzerPopupFactory pop = new AnalyzerPopupFactory(manager);
    pop.maybeShowPopup(this, event, clicked);
  }


  //#########################################################################
  //# Interface net.sourcefore.waters.gui.observer.subject
  @Override
  public void attach(final Observer observer)
  {
    if (mObservers == null) {
      mObservers = new LinkedList<>();
    }
    mObservers.add(observer);
  }

  @Override
  public void detach(final Observer observer)
  {
    if (mObservers != null) {
      mObservers.remove(observer);
      if (mObservers.isEmpty()) {
        mObservers = null;
      }
    }
  }

  @Override
  public void fireEditorChangedEvent(final EditorChangedEvent event)
  {
    if (mObservers != null) {
      final List<Observer> copy = new ArrayList<>(mObservers);
      for (final Observer observer : copy) {
        observer.update(event);
      }
    }
  }


  //#########################################################################
  //# Interface net.sourcefore.waters.gui.transfer.SelectionOwner
  @Override
  public UndoInterface getUndoInterface(final Action action)
  {
    // NOT using editor's undo queue. May include separate queue for
    // analyser later, or simply do not support undo in analyser.
    return null;
  }

  @Override
  public boolean hasNonEmptySelection()
  {
    return getSelectedRow() >= 0;
  }

  @Override
  public boolean canSelectMore()
  {
    return getSelectedRowCount() < getRowCount();
  }

  @Override
  public boolean isSelected(final Proxy proxy)
  {
    if (proxy instanceof AutomatonProxy) {
      final AutomatonProxy aut = (AutomatonProxy) proxy;
      final AutomataTableModel model = getModel();
      return this.isRowSelected(model.getIndex(aut));
    }
    return false;
  }

  @Override
  public List<AutomatonProxy> getCurrentSelection()
  {
    final AutomataTableModel model = getModel();
    final List<AutomatonProxy> output = new ArrayList<>();
    for (final int i : this.getSelectedRows()) {
      output.add(model.getAutomaton(i));
    }
    return output;
  }

  @Override
  public List<AutomatonProxy> getAllSelectableItems()
  {
    final AutomataTableModel model = getModel();
    return model.getAutomataList();
  }

  @Override
  public AutomatonProxy getSelectionAnchor()
  {
    final AutomataTableModel model = getModel();
    if (this.getSelectedRow() != -1)
      return model.getAutomaton(this.getSelectedRow());
    else
      return null;
  }

  @Override
  public Proxy getSelectableAncestor(final Proxy item)
  {
    return item;
  }

  @Override
  public void clearSelection(final boolean propagate)
  {
    clearSelection();
  }

  @Override
  public void replaceSelection(final List<? extends Proxy> items)
  {
    clearSelection();
    addToSelection(items);
  }

  @Override
  public void addToSelection(final List<? extends Proxy> items)
  {
    final AutomataTableModel model = getModel();
    final TIntArrayList indexList = new TIntArrayList(items.size());
    for (final Proxy proxy : items) {
      if (proxy instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) proxy;
        indexList.add(model.getIndex(aut));
      }
    }
    indexList.sort();
    int start = -1;
    int end = -1;
    for (int i = 0; i < indexList.size(); i++) {
      final int position = indexList.get(i);
      if (start == -1) {
        start = end = position;
      } else if (position > end + 1) {
        addRowSelectionInterval(start, end);
        start = end = position;
      } else {
        end = position;
      }
    }
    addRowSelectionInterval(start, end);
  }

  @Override
  public void removeFromSelection(final List<? extends Proxy> items)
  {
    final AutomataTableModel model = getModel();
    final TIntArrayList indexList = new TIntArrayList(items.size());
    for (final Proxy proxy : items) {
      if (proxy instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) proxy;
        indexList.add(model.getIndex(aut));
      }
    }
    indexList.sort();
    int start = -1;
    int end = -1;
    for (int i = 0; i < indexList.size(); i++) {
      final int position = indexList.get(i);
      if (start == -1) {
        start = end = position;
      } else if (position > end + 1) {
        removeRowSelectionInterval(start, end);
        start = end = position;
      } else {
        end = position;
      }
    }
    removeRowSelectionInterval(start, end);
  }

  @Override
  public boolean canPaste(final Transferable transferable)
  {
    if (transferable.isDataFlavorSupported(WatersDataFlavor.AUTOMATON))
      return true;
    else
      return false;
  }

  @Override
  public List<InsertInfo> getInsertInfo(final Transferable transferable)
    throws IOException, UnsupportedFlavorException
  {
    final List<InsertInfo> inserts = new LinkedList<InsertInfo>();
    if (transferable.isDataFlavorSupported(WatersDataFlavor.AUTOMATON)) {
      final ProductDESProxyFactory factory =
        ProductDESElementFactory.getInstance();
      final AutomataTableModel tableModel = getModel();
      final Map<String,EventProxy> eMap = tableModel.getEventMap();
      final AutomataCloner cloner = new AutomataCloner(factory, eMap);
      @SuppressWarnings("unchecked")
      final List<Proxy> data = (List<Proxy>) transferable
        .getTransferData(WatersDataFlavor.AUTOMATON);
      for (final Proxy proxy : data) {
        final AutomatonProxy aut = (AutomatonProxy) proxy;
        final AutomataTableModel model = getModel();
        final String oldName = aut.getName();
        final String newName = model.getUniqueAutomatonName(oldName);
        final AutomatonProxy cloned;
        if (oldName == newName) {
          cloned = cloner.clone(aut);
        } else {
          cloned = cloner.clone(aut, newName);
        }
        final InsertInfo insert = new InsertInfo(cloned);
        inserts.add(insert);
      }
    } else {
      throw new UnsupportedFlavorException(null);
    }
    return inserts;
  }

  @Override
  public boolean canDelete(final List<? extends Proxy> items)
  {
    final AutomataTableModel model = getModel();
    for (final Proxy p : items) {
      if (p instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) p;
        if ((model.getIndex(aut) != -1)) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public List<InsertInfo> getDeletionVictims(final List<? extends Proxy> items)
  {
    final List<InsertInfo> infoList = new ArrayList<>(items.size());
    for (final Proxy p : items) {
      infoList.add(new InsertInfo(p));
    }
    return infoList;
  }

  @Override
  public void insertItems(final List<InsertInfo> inserts)
  {
    final List<AutomatonProxy> insertAutomatonList =
      new ArrayList<AutomatonProxy>();
    final AutomataTableModel model = getModel();
    for (final InsertInfo info : inserts) {
      final Proxy proxy = info.getProxy();
      if (proxy instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) proxy;
        insertAutomatonList.add(aut);
      }
    }
    model.insertRows(insertAutomatonList);
  }

  @Override
  public void deleteItems(final List<InsertInfo> deletes)
  {
    final TIntArrayList deleteIndexList = new TIntArrayList(deletes.size());
    final AutomataTableModel model = getModel();
    for (final InsertInfo info : deletes) {
      final Proxy proxy = info.getProxy();
      if (proxy instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) proxy;
        deleteIndexList.add(model.getIndex(aut));
      }
    }
    model.deleteRows(deleteIndexList);
  }

  @Override
  public void scrollToVisible(final List<? extends Proxy> items)
  {
    for (final Proxy p : items) {
      if (p instanceof AutomatonProxy) {
        final AutomatonProxy aut = (AutomatonProxy) p;
        final AutomataTableModel model = getModel();
        scrollRectToVisible(getCellRect(model.getIndex(aut), 0, true));
        break;
      }
    }
  }

  @Override
  public void activate()
  {
    requestFocusInWindow();
  }

  @Override
  public void close()
  {
    final AutomataTableModel model = getModel();
    model.close();
  }


  //#########################################################################
  //# Specific Access
  /**
   * Gets the currently applicable arguments for an analysis operation.
   * @return Either a list containing the automata currently selected in
   *         the table, or a list containing all automata in the table
   *         if the selection is empty.
   */
  public List<AutomatonProxy> getOperationArgument()
  {
    if (getSelectedRowCount() > 0) {
      return getCurrentSelection();
    } else {
      return getAllSelectableItems();
    }
  }

  public void insertAndSelect(final AutomatonProxy aut)
  {
    final List<AutomatonProxy> list = Collections.singletonList(aut);
    insertAndSelect(list);
  }

  public void insertAndSelect
    (final Collection<? extends AutomatonProxy> automata)
  {
    final AutomataTableModel model = getModel();
    final List<AutomatonProxy> renamedAutomata = new ArrayList<>(automata.size());
    for (final AutomatonProxy aut : automata) {
      final String name = aut.getName();
      final String uniqueName = model.getUniqueAutomatonName(name);
      final AutomatonProxy renamedAut =
        TRAutomatonProxy.renameAutomaton(aut, uniqueName);
      renamedAutomata.add(renamedAut);
    }
    model.insertRows(renamedAutomata);
    scrollToVisible(renamedAutomata);
    clearSelection();
    addToSelection(renamedAutomata);
  }


  //#########################################################################
  //# Inner Class TextCellRenderer
  /**
   * A text renderer for the analyser's automata table.
   * This renderer supports left-aligned text cells and right-aligned
   * number cells (with additional padding). It also ensures that the
   * cell are displayed without the focus rectangle.
   */
  private class TextCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Constructors
    private TextCellRenderer()
    {
      mBorder = null;
    }

    private TextCellRenderer(final int rightBorder)
    {
      final int scaledBorder =
        Math.round(rightBorder * IconAndFontLoader.GLOBAL_SCALE_FACTOR);
      mBorder = BorderFactory.createEmptyBorder(0, 0, 0, scaledBorder);
      setHorizontalAlignment(JLabel.RIGHT);
    }

    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean selected,
                                                   final boolean focused,
                                                   final int row,
                                                   final int column)
    {
      final JLabel cell = (JLabel) super.getTableCellRendererComponent
        (table, value, selected, false, row, column);
      cell.setBorder(mBorder);
      return cell;
    }

    //#######################################################################
    //# Data Members
    private final Border mBorder;

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 2739259938102695664L;
  }


  //#########################################################################
  //# Inner Class IconCellRenderer
  /**
   * An icon renderer for the analyser's automata table.
   * This renderer displays the component kind of automata as icons.
   */
 private class IconCellRenderer extends DefaultTableCellRenderer
  {
    //#######################################################################
    //# Interface javax.swing.table.TableCellRenderer
    @Override
    public Component getTableCellRendererComponent(final JTable table,
                                                   final Object value,
                                                   final boolean selected,
                                                   final boolean focused,
                                                   final int row,
                                                   final int column)
    {
      final JLabel cell =
        (JLabel) super.getTableCellRendererComponent(table, value, selected,
                                                     false, row, column);
      final ComponentKind kind = (ComponentKind) value;
      final Icon icon = ModuleContext.getComponentKindIcon(kind);
      cell.setIcon(icon);
      cell.setText(null);
      return cell;
    }

    //#######################################################################
    //# Class Constants
    private static final long serialVersionUID = 7455415810847160716L;
  }


  //#########################################################################
  //# Inner Class TableHeaderMouseListener
  private class TableMouseListener extends MouseAdapter
  {
    //#######################################################################
    //# Interface java.awt.event.MouseListener
    @Override
    public void mouseClicked(final MouseEvent event)
    {
      if (event.getButton() == MouseEvent.BUTTON1) {
        final AutomatonProxy aut = getAutomaton(event);
        if (aut == null) {
          clearSelection();
        } else if (event.getClickCount() == 2) {
          mParent.showGraph(aut);
        }
      }
    }

    @Override
    public void mousePressed(final MouseEvent event)
    {
      requestFocusInWindow();
      maybeShowPopup(event);
    }

    @Override
    public void mouseReleased(final MouseEvent event)
    {
      maybeShowPopup(event);
    }
  }


  //#########################################################################
  //# Inner Class TableChangeListener
  private class TableChangeListener implements TableModelListener, Runnable
  {
    //#######################################################################
    //# Interface javax.swing.event.TableModelListener
    @Override
    public void tableChanged(final TableModelEvent event)
    {
      switch (event.getType()) {
      case TableModelEvent.INSERT:
        SwingUtilities.invokeLater(this);
        break;
      case TableModelEvent.DELETE:
        resetColumnWidths();
        SwingUtilities.invokeLater(this);
        break;
      }
    }

    //#######################################################################
    //# Interface java.lang.Runnable
    @Override
    public void run()
    {
      // Delayed repaint to adjust column widths
      AutomataTable.this.repaint();
    }
  }


  //#########################################################################
  //# Data Members
  private final WatersAnalyzerPanel mParent;
  private List<Observer> mObservers;
  private final ModuleContainer mModuleContainer;


  //#########################################################################
  //# Class Constants
  private static final int RIGHT_PADDING = 8;
  private static final int ICON_COLUMN = 0;
  private static final int NAME_COLUMN = 1;

  private static final long serialVersionUID = -9036493474591272655L;

}
