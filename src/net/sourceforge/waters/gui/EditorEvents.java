
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: waters.gui
//# CLASS:   EditorEvents
//###########################################################################
//# $Id: EditorEvents.java,v 1.3 2005-02-18 03:09:06 knut Exp $
//###########################################################################
package net.sourceforge.waters.gui;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.Iterator;
import java.util.TreeSet;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JFormattedTextField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import net.sourceforge.waters.model.base.UnexpectedWatersException;
import net.sourceforge.waters.model.expr.IdentifierProxy;
import net.sourceforge.waters.model.expr.SimpleExpressionProxy;
import net.sourceforge.waters.model.module.EdgeProxy;
import net.sourceforge.waters.model.module.GraphProxy;
import net.sourceforge.waters.model.module.ModuleProxy;
import net.sourceforge.waters.model.module.NodeProxy;
import net.sourceforge.waters.model.module.SimpleComponentProxy;

/**
 * <p>The Events window which sits to the right of the editor window.</p>
 *
 * <p>This is used to view the module events which have been selected for
 * use with this particular component, and selecting other events from the
 * module for use with this component.</p>
 *
 * @author Gian Perrone
 */
class EditorEvents
	extends JTable
{

	//#########################################################################
	//# Constructors
	EditorEvents(final ModuleProxy module, final SimpleComponentProxy comp, final EditorWindow root)
	{
		this(module, comp.getGraph(), root);
	}

	EditorEvents(final ModuleProxy module, final GraphProxy graph, final EditorWindow root)
	{
		final Collection events = collectEvents(graph);

		mModel = new EventTableModel(events, module);

		final Dimension ispacing = new Dimension(0, 0);

		setModel(mModel);
		setTableHeader(null);
		setRowHeight(22);
		setShowGrid(false);
		setIntercellSpacing(ispacing);

		final TableColumn column0 = getColumnModel().getColumn(0);

		column0.setMinWidth(20);
		column0.setPreferredWidth(24);
		column0.setMaxWidth(24);
		column0.setResizable(false);

		final TableColumn column1 = getColumnModel().getColumn(1);

		column1.setMinWidth(100);
		column1.setResizable(false);

		final TableCellRenderer iconrender0 = getDefaultRenderer(ImageIcon.class);
		final TableCellRenderer iconrender1 = new RendererNoFocus(iconrender0);

		setDefaultRenderer(ImageIcon.class, iconrender1);

		final TableCellRenderer textrender0 = getDefaultRenderer(Object.class);
		final TableCellRenderer textrender1 = new RendererNoFocus(textrender0);

		setDefaultRenderer(Object.class, textrender1);

		final TableCellEditor editor = new IdentifierEditor();

		setDefaultEditor(Object.class, editor);

		final Dimension minsize = getMinimumSize();

		minsize.width = 128;

		setPreferredScrollableViewportSize(minsize);

		ListSelectionModel rowSM = getSelectionModel();

		rowSM.addListSelectionListener(new ListSelectionListener()
		{
			public void valueChanged(ListSelectionEvent e)
			{

				//Ignore extra messages.
				if (e.getValueIsAdjusting())
				{
					return;
				}

				ListSelectionModel lsm = (ListSelectionModel) e.getSource();

				if (lsm.isSelectionEmpty())
				{
					mBuffer = null;
				}
				else
				{
					final int row = lsm.getMinSelectionIndex();

					mBuffer = mModel.getEvent(row);
				}
			}
		});
	}

	//#########################################################################
	//# Editing
	public void setBuffer(final IdentifierProxy ident)
	{
		mBuffer = ident;
	}

	public IdentifierProxy getBuffer()
	{
		return mBuffer;
	}

	//#########################################################################
	//# Auxiliary Methods
	private static Collection collectEvents(final GraphProxy graph)
	{
		final Collection result = new TreeSet();

		if (graph != null)
		{
			final Collection blocked = graph.getBlockedEvents();

			result.addAll(blocked);

			final Collection nodes = graph.getNodes();
			final Iterator nodeiter = nodes.iterator();

			while (nodeiter.hasNext())
			{
				final NodeProxy node = (NodeProxy) nodeiter.next();
				final Collection props = node.getPropositions();

				result.addAll(props);
			}

			final Collection edges = graph.getEdges();
			final Iterator edgeiter = edges.iterator();

			while (edgeiter.hasNext())
			{
				final EdgeProxy edge = (EdgeProxy) edgeiter.next();
				final Collection labels = edge.getLabelBlock();

				result.addAll(labels);
			}
		}

		return result;
	}

	//#########################################################################
	//# Local Class RendererNoFocus
	private static class RendererNoFocus
		implements TableCellRenderer
	{

		//#######################################################################
		//# Constructors
		private RendererNoFocus(final TableCellRenderer renderer)
		{
			mRenderer = renderer;
		}

		//#######################################################################
		//# Interface javax.swing.table.TableCellRenderer
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column)
		{
			return mRenderer.getTableCellRendererComponent(table, value, isSelected, false, row, column);
		}

		//#######################################################################
		//# Data Members
		private final TableCellRenderer mRenderer;
	}

	//#########################################################################
	//# Local Class IdentifierEditor
	private static class IdentifierEditor
		extends DefaultCellEditor
	{

		//#######################################################################
		//# Constructors
		private IdentifierEditor()
		{
			super(new SimpleExpressionCell(SimpleExpressionProxy.TYPE_NAME));
		}

		//#######################################################################
		//# Overrides for base class javax.swing.DefaultCellEditor
		public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column)
		{
			final JFormattedTextField textfield = (JFormattedTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);

			textfield.setValue(value);

			return textfield;
		}

		public Object getCellEditorValue()
		{
			final JFormattedTextField textfield = (JFormattedTextField) getComponent();

			return textfield.getValue();
		}

		public boolean stopCellEditing()
		{
			final JFormattedTextField textfield = (JFormattedTextField) getComponent();

			if (textfield.isEditValid())
			{
				try
				{
					textfield.commitEdit();
				}
				catch (final java.text.ParseException exception)
				{

					// Can't happen if input is valid.
					throw new UnexpectedWatersException(exception);
				}
			}
			else
			{    // text is invalid ...
				return false;    // don't let the editor go away ...
			}

			return super.stopCellEditing();
		}
	}

	//#########################################################################
	//# Data Members
	private final EventTableModel mModel;
	private IdentifierProxy mBuffer;
}
