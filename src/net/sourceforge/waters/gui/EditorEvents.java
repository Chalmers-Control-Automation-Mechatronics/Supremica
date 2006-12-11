//# -*- tab-width: 4  indent-tabs-mode: t  c-basic-offset: 4 -*-
//###########################################################################
//# PROJECT: Waters
//# PACKAGE: net.sourceforge.waters.gui
//# CLASS:   EditorEvents
//###########################################################################
//# $Id: EditorEvents.java,v 1.30 2006-12-11 02:40:44 siw4 Exp $
//###########################################################################


package net.sourceforge.waters.gui;

import java.util.ArrayList;
import java.util.Collection;
import net.sourceforge.waters.xsd.base.EventKind;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragSource;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureListener;
import java.awt.dnd.DragSourceAdapter;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.InvalidDnDOperationException;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import net.sourceforge.waters.model.expr.ExpressionParser;
import net.sourceforge.waters.model.expr.Operator;
import net.sourceforge.waters.subject.module.IdentifierSubject;
import net.sourceforge.waters.subject.module.GraphSubject;
import net.sourceforge.waters.subject.module.SimpleComponentSubject;



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
	public EditorEvents(final ModuleWindowInterface root,
						final SimpleComponentSubject comp,
						final EditorWindowInterface window)
	{
		this(root, comp.getGraph(), window);
	}

	public EditorEvents(final ModuleWindowInterface root,
						final GraphSubject graph,
						final EditorWindowInterface window)
	{
		final TableModel model = new EventTableModel(graph, root, this);
		final Dimension ispacing = new Dimension(0, 0);
		final ExpressionParser parser = root.getExpressionParser();
		mRoot = root;
    mWindow = window;

		setModel(model);
		setTableHeader(null);
		setRowHeight(22);
		setShowGrid(false);
		setIntercellSpacing(ispacing);
		setAutoResizeMode(AUTO_RESIZE_LAST_COLUMN);
		setSurrendersFocusOnKeystroke(true);
		setRowSelectionAllowed(true);

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
		final TableCellEditor editor =
			new SimpleExpressionEditor(Operator.TYPE_NAME, parser);
		setDefaultEditor(Object.class, editor);

		setPreferredSizes();

		final ListSelectionModel selmodel = getSelectionModel();
		final ListSelectionListener listener = new SelectionListener();
		selmodel.addListSelectionListener(listener);

		final InputMap inputmap = getInputMap();
		inputmap.put(STROKE_TAB, ACTNAME_DOWN);
		inputmap.put(STROKE_SHIFT_TAB, ACTNAME_UP);
		final ActionMap actionmap = getActionMap();
		actionmap.put(ACTNAME_DOWN, new NavigationAction(ACTNAME_DOWN, 1));
		actionmap.put(ACTNAME_UP, new NavigationAction(ACTNAME_UP, -1));

		mDragSource = DragSource.getDefaultDragSource();
		mDGListener = new DGListener();
		mDSListener = new DSListener();

		// component, action, listener
		mDragSource.createDefaultDragGestureRecognizer(this,
													  mDragAction,
													  mDGListener);
		mDragSource.addDragSourceListener(mDSListener);
	}



	//#######################################################################
	//# Overrides for Base Class javax.swing.JTable
	public void tableChanged(final TableModelEvent event)
	{
		super.tableChanged(event);

//		System.err.println("tableChanged");

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
		if (row >= 0) {
			final EventTableModel model = (EventTableModel) getModel();
			return model.getToolTipText(row);
		} else {
			return null;
		}
	}


	public boolean getScrollableTracksViewportHeight()
	{
		final Container viewport = getParent();
		return getPreferredSize().height < viewport.getHeight();
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
		if (editCellAt(row, 1))
		{
			final Component comp = getEditorComponent();
			final Rectangle bounds = comp.getBounds();
			scrollRectToVisible(bounds);
			comp.requestFocusInWindow();
		}
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
				// nothing ?
			} else {
				// nothing ?
			}
		}

	}



	//#######################################################################
	//# Local Class NavigationAction
	private class NavigationAction extends AbstractAction
	{

		//###################################################################
		//# Data Members
		private NavigationAction(final String name, final int offset)
		{
			super(name);
			mOffset = offset;
		}



		//###################################################################
		//# Interface java.awt.event.ActionListener
		public void actionPerformed(final ActionEvent event)
		{
			if (!isEditing()) {
				final ListSelectionModel selmodel = getSelectionModel();
				final int numrows = getRowCount();
				final int selrow = selmodel.getLeadSelectionIndex();
				final int newrow = (selrow + numrows + mOffset) % numrows;
				setRowSelectionInterval(newrow, newrow);
			}
		}



		//###################################################################
		//# Data Members
		private final int mOffset;

	}



	private class DSListener extends DragSourceAdapter
	{
		public void dragOver(DragSourceDragEvent e)
		{
			if (e.getTargetActions() == DnDConstants.ACTION_COPY) {
				e.getDragSourceContext().setCursor
					(DragSource.DefaultCopyDrop);
			} else {
				e.getDragSourceContext().setCursor
					(DragSource.DefaultCopyNoDrop);
			}
		}
	}

	private class DGListener implements DragGestureListener
	{
		public void dragGestureRecognized(final DragGestureEvent event)
		{
			final EventTableModel model = (EventTableModel) getModel();
      final int[] rows = getSelectedRows();
      if (rows.length == 0) {
        return;
      }
      final Collection<IdentifierSubject> idents = new ArrayList<IdentifierSubject>(rows.length);
      EventType e = EventType.UNKNOWN;
      for(int i = 0; i < rows.length; i++)
      {
        final IdentifierSubject ident = model.getEvent(rows[i]);
        final EventKind guess = mRoot.guessEventKind(ident);
        System.out.println("EventType: " + e);
        System.out.println("EventKind: " + guess);
        if (guess != null) {
          switch (e) {
            case UNKNOWN:
              switch (guess) {
                case PROPOSITION:
                  e = EventType.NODE_EVENTS;
                  break;
                case CONTROLLABLE:
                  e = EventType.EDGE_EVENTS;
                  break;
                case UNCONTROLLABLE:
                  e = EventType.EDGE_EVENTS;
                  break;
                default:
                  break;
              }
              break;
            case EDGE_EVENTS:
              switch (guess) {
                case PROPOSITION:
                  e = EventType.BOTH;
                  break;
                default:
                  break;
              }
              break;
            case NODE_EVENTS:
              switch (guess) {
                case CONTROLLABLE:
                  e = EventType.BOTH;
                  break;
                case UNCONTROLLABLE:
                  e = EventType.BOTH;
                  break;
                default:
                  break;
              }
              break;
            default:
              break;
          }
        }
        idents.add(ident);
      }
      System.out.println("EventType: " + e);
      final Transferable trans = new IdentifierTransfer(idents, e);
			try {
				event.startDrag(DragSource.DefaultCopyDrop, trans);
			} catch (InvalidDnDOperationException exception) {
				throw new IllegalArgumentException(exception);
			}
		}
	}

	public EditorWindowInterface getEditorInterface()
	{
		return mWindow;
	}


	//#######################################################################
	//# Data Members
	private DragSource mDragSource;
	private DragGestureListener mDGListener;
	private DragSourceListener mDSListener;
	private int mDragAction = DnDConstants.ACTION_COPY;
	private final ModuleWindowInterface mRoot;
  private final EditorWindowInterface mWindow;
	private final JPopupMenu popupMenu = new JPopupMenu();


	//#######################################################################
	//# Class Constants
	private static final int COLUMNWIDTH0 = 24;
	private static final int MINCOLUMNWIDTH0 = 20;
	private static final int COLUMNWIDTH1 = 96;
	private static final int MINCOLUMNWIDTH1 = 24;

	private static final KeyStroke STROKE_TAB =
		KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0);
	private static final KeyStroke STROKE_SHIFT_TAB =
		KeyStroke.getKeyStroke(KeyEvent.VK_TAB, InputEvent.SHIFT_MASK);

	private static final String ACTNAME_DOWN = "EditorEvents.DOWN";
	private static final String ACTNAME_UP = "EditorEvents.UP";

}
