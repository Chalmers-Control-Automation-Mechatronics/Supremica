//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorEvents
//###########################################################################
//# $Id: EditorEvents.java,v 1.9 2005-02-22 22:38:42 robi Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;



/**
 * <p>The Events window which sits to the right of the editor window.</p>
 *
 * <p>This is used to view the module events which have been selected for
 * use with this particular component, and selecting other events from the
 * module for use with this component.</p>
 *
 * @author Gian Perrone, Robi Malik
 */

public class EditorEvents
	extends JTable
{

	//#######################################################################
	//# Constructors
	public EditorEvents(final ModuleProxy module,
						final SimpleComponentProxy comp)
	{
		this(module, comp.getGraph());
	}

	public EditorEvents(final ModuleProxy module,
						final GraphProxy graph)
	{
		final TableModel model = new EventTableModel(graph, module);
		final Dimension ispacing = new Dimension(0, 0);

		setModel(model);
		setTableHeader(null);
		setRowHeight(22);
		setShowGrid(false);
		setIntercellSpacing(ispacing);
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		
		final TableCellRenderer iconrenderer0 =
			getDefaultRenderer(ImageIcon.class);
		final TableCellRenderer iconrenderer1 =
			new RendererNoFocus(iconrenderer0, false);
		setDefaultRenderer(ImageIcon.class, iconrenderer1);
		final TableCellRenderer textrenderer0 =
			getDefaultRenderer(Object.class);
		final TableCellRenderer textrenderer1 =
			new RendererNoFocus(textrenderer0, true);
		setDefaultRenderer(Object.class, textrenderer1);
		final TableCellEditor editor = new IdentifierEditor();
		setDefaultEditor(Object.class, editor);

		setPreferredSizes();

		final ListSelectionModel selmodel = getSelectionModel();
		final ListSelectionListener listener = new SelectionListener();
		selmodel.addListSelectionListener(listener);
	}


	//#######################################################################
	//# Overrides for Base Class javax.swing.JTable
	public void tableChanged(final TableModelEvent event)
	{
		super.tableChanged(event);

		final int row0 = event.getFirstRow();
		final int row1 = event.getLastRow();
		if (row0 >= 0 && row1 >= 0) {
			switch (event.getType()) {
			case TableModelEvent.INSERT :
			case TableModelEvent.DELETE :
				final Dimension prefsize = getPreferredSize();
				prefsize.height = calculateHeight();
				setPreferredSize(prefsize);
				setPreferredScrollableViewportSize(prefsize);
				revalidate();
				break;
			default :
				break;
			}
		}
	}


	public String getToolTipText(final MouseEvent event)
	{
        final Point point = event.getPoint();
        final int row = rowAtPoint(point);
		final EventTableModel model = (EventTableModel) getModel();
		return model.getToolTipText(row);
	}

   

	//#######################################################################
	//# Editing
	void createEvent()
	{
		if (isEditing()) {
			final SimpleExpressionCell comp =
				(SimpleExpressionCell) getEditorComponent();
			try {
				comp.commitEdit();
				if (comp.getValue() == null) {
					return;
				}
			} catch (final java.text.ParseException exception) {
				return;
			}
		}
		final EventTableModel model = (EventTableModel) getModel();
		final int row = model.createEvent();
		if (editCellAt(row, 1)) {
			final Component comp = getEditorComponent();
			final Rectangle bounds = comp.getBounds();
			scrollRectToVisible(bounds);
			comp.requestFocus();
		}
	}


	public void setBuffer(final IdentifierProxy ident)
	{
		mBuffer = ident;
	}


	public IdentifierProxy getBuffer()
	{
		return mBuffer;
	}



	//#######################################################################
	//# Calculating Column Widths
	/**
	 * Set the table's preferred and minimum size by checking the space
	 * needed for its contents.
	 */
	private void setPreferredSizes()
	{
		final int height = calculateHeight();
		final int width1 = calculateWidth1();

		final TableColumn column0 = getColumnModel().getColumn(0);
		column0.setMinWidth(MINCOLUMNWIDTH0);
		column0.setPreferredWidth(COLUMNWIDTH0);
		column0.setMaxWidth(COLUMNWIDTH0);
		column0.setResizable(false);

		final TableColumn column1 = getColumnModel().getColumn(1);
		column1.setMinWidth(MINCOLUMNWIDTH1);
		column1.setPreferredWidth(width1);

		final int totalwidth = COLUMNWIDTH0 + width1;
		final int minwidth = MINCOLUMNWIDTH0 + MINCOLUMNWIDTH1;
		final Dimension prefsize = new Dimension(totalwidth, height);
		final Dimension minsize = new Dimension(minwidth, 3 * getRowHeight());
		setPreferredSize(prefsize);
		setPreferredScrollableViewportSize(prefsize);
		setMinimumSize(minsize);
	}


	private int calculateHeight()
	{
		return getRowCount() * getRowHeight();
	}


	private int calculateWidth1()
	{
		final TableModel model = getModel();
		final TableCellRenderer renderer = getDefaultRenderer(Object.class);
		final int rows = getRowCount();
		int maxwidth = COLUMNWIDTH1;
		for (int row = 0; row < rows; row++) {
			final Object value = model.getValueAt(row, 1);
			final Component comp =
				renderer.getTableCellRendererComponent
				(this, value, false, false, row, 1);
			final Dimension size = comp.getPreferredSize();
			final int width = size.width;
			if (width > maxwidth) {
				maxwidth = width;
			}
		}
		return maxwidth;
	}



	//#######################################################################
	//# Local Class RendererNoFocus
	private static class RendererNoFocus
		implements TableCellRenderer
	{

		//###################################################################
		//# Constructors
		private RendererNoFocus(final TableCellRenderer renderer,
								final boolean focusable)
		{
			mRenderer = renderer;
			mFocusable = focusable;
		}



		//###################################################################
		//# Interface javax.swing.table.TableCellRenderer
		public Component getTableCellRendererComponent
			(final JTable table, final Object value, final boolean isSelected,
			 final boolean hasFocus, final int row, final int column)
		{
			final Component comp =
				mRenderer.getTableCellRendererComponent
				    (table, value, isSelected, false, row, column);
			comp.setFocusable(mFocusable);
			return comp;
		}



		//###################################################################
		//# Data Members
		private final TableCellRenderer mRenderer;
		private final boolean mFocusable;

	}



	//#######################################################################
	//# Local Class IdentifierEditor
	private static class IdentifierEditor
		extends DefaultCellEditor
	{

		//###################################################################
		//# Constructors
		private IdentifierEditor()
		{
			super(new SimpleExpressionCell(SimpleExpressionProxy.TYPE_NAME));
		}



		//###################################################################
		//# Overrides for base class javax.swing.DefaultCellEditor
		public Component getTableCellEditorComponent
			(final JTable table, final Object value, final boolean isSelected,
			 final int row, final int column)
		{
			final SimpleExpressionCell textfield =
				(SimpleExpressionCell) super.getTableCellEditorComponent
				    (table, value, isSelected, row, column);
			textfield.setValue(value);
			return textfield;
		}


		public Object getCellEditorValue()
		{
			final SimpleExpressionCell textfield =
				(SimpleExpressionCell) getComponent();

			return textfield.getValue();
		}


		public boolean stopCellEditing()
		{
			final SimpleExpressionCell textfield =
				(SimpleExpressionCell) getComponent();
			return textfield.verify() && super.stopCellEditing();
		}

	}



	//#######################################################################
	//# Local Class SelectionListener
	private class SelectionListener
		implements ListSelectionListener
	{

		//###################################################################
		//# Interface javax.swing.ListSelectionListener
		public void valueChanged(final ListSelectionEvent event)
		{
			final ListSelectionModel selmodel =
				(ListSelectionModel) event.getSource();
			if (isEditing()) {
				// When we are editing a cell with invalid contents,
				// and the user clicks into another row,
				// we sometimes get spurious selection events.
				// The following code undoes their effects.
				final int row = getEditingRow();
				if (row < getRowCount() &&
					(row != selmodel.getMinSelectionIndex() ||
					 row != selmodel.getMaxSelectionIndex())) {
					setRowSelectionInterval(row, row);
					getEditorComponent().requestFocus();
				}
			} else if (event.getValueIsAdjusting()) {
				// Ignore extra messages ...
			} else if (selmodel.isSelectionEmpty()) {
				mBuffer = null;
			} else {
				final EventTableModel model = (EventTableModel) getModel();
				final int row = selmodel.getMinSelectionIndex();
				mBuffer = model.getEvent(row);
			}
		}

	}



	//#######################################################################
	//# Data Members
	private IdentifierProxy mBuffer;



	//#######################################################################
	//# Class Constants
	private static final int COLUMNWIDTH0 = 24;
	private static final int MINCOLUMNWIDTH0 = 20;
	private static final int COLUMNWIDTH1 = 96;
	private static final int MINCOLUMNWIDTH1 = 24;

}
