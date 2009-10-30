package org.supremica.external.processeditor.processgraph.table;


import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.Component;
import java.awt.AWTEvent;

import java.awt.event.ActionListener;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;

import org.jdesktop.swingx.MultiSplitPane;
import org.jdesktop.swingx.MultiSplitLayout;
import org.jdesktop.swingx.MultiSplitLayout.Split;
import org.jdesktop.swingx.MultiSplitLayout.Leaf;
import org.jdesktop.swingx.MultiSplitLayout.Divider;

import java.util.LinkedList;
import java.util.List;

public class TableGroupPane 
						extends 
							MultiSplitPane
						implements
							TableListener,
							ActionListener,
							MouseListener,
							KeyListener
{
    private static final long serialVersionUID = 1L;

	protected JPopupMenu popupMenu;
	
	protected int[] selectedRows = null;
	
	private BasicTable table = null;
	private int col = -1;
	private int row = -1;
	
	public TableGroupPane(){
		super();
		popupMenu = new JPopupMenu( "Menu" );
		makePopupMenu();
		addMouseListener(this);
	}
	
	public void addTable(BasicTablePane table){
		
		Component[] comps = getComponents();
		List<MultiSplitLayout.Node> children = new LinkedList<MultiSplitLayout.Node>();
		Split modelRoot = new Split();
		
		removeAll();
		validate();
		
		table.addtableListener(this);
		table.addKeyListener(this);
		table.addMouseListener(this);
		
		if(comps.length == 0){
			
			children.add(new Leaf("0"));
			children.add(new Divider());
			children.add(new Leaf("1"));
			
			modelRoot.setChildren(children);
			setModel(modelRoot);
			
			add(table,"0");
			add(new JPanel(),"1");
			
			return;
		}
		
		//make split layout
		for(int i = 0; i < comps.length; i++){
			if(comps[i] instanceof BasicTablePane){
				children.add(new Leaf(Integer.toString(i)));
				children.add(new Divider());
			}
		}
		children.add(new Leaf(Integer.toString(comps.length)));
		
		modelRoot.setChildren(children);
		setModel(modelRoot);
		setDividerSize(3);
		
		//add tables
		for(int i = 0; i < comps.length; i++){
			if(comps[i] instanceof BasicTablePane){
				add(comps[i], Integer.toString(i));
			}
		}
		add(table, Integer.toString(comps.length));
		
		validate();
	}
	
	/*
	 * TODO: Allow user to select multiple rows
	 */
	public void setSelectedRows(){
		
		if(selectedRows == null || selectedRows.length == 0){
			return;
		}
		
		BasicTablePane table = null;
		 
		for(int i = 0; i < getComponentCount(); i++){
			
			Object o = getComponent(i);
			
			if(o instanceof BasicTablePane){
				table = (BasicTablePane) o;
				table.removeTableListener();
				table.setRowSelectionIntervall(selectedRows[0], selectedRows[0]);
				table.addtableListener(this);
			}
		}
	}
	
	// ---- TableListener ---- //
	public void tableSelectionChanged(TableEvent e){
		selectedRows = e.getSource().getSelectedRows();
		setSelectedRows();
	}
	
	public void rowAdded(TableEvent e){}
	public void columnAdded(TableEvent e){};
	public void columnRemoved(TableEvent e){};
	public void rowRemoved(TableEvent e){};
	//--- End TableListener ---- //

	// --- KeyListener ---
	public void keyPressed(KeyEvent e){} 
    public void keyReleased(KeyEvent e){}
    public void keyTyped(KeyEvent e){}
	
	/* Handel popup menu */
	protected void makePopupMenu(){
		//override this to make pop-up-menu
		// Action and mouse listener support
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );
		
		// Create some menu items for the popup
		JMenuItem menuItem = new JMenuItem( "Fill down" );
		popupMenu.add( menuItem );
		menuItem.addActionListener( this );
	}
	
	public void actionPerformed( ActionEvent event ){
		// Add action handling code here
		if(event.getActionCommand().equals("Fill down")){
			if(null != table){
				table.fillColumn(table.getValueAt(row, col), col, row, table.getRowCount());
			}
			hidePopupMenu();
		}
	}
	
	public void mousePressed(MouseEvent e) {
		;
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.isPopupTrigger()){
			showPopupMenu(e);
		}else{
			hidePopupMenu();
		}
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	
	private void showPopupMenu(MouseEvent e){
		
		if(e.getSource() instanceof BasicTable){
			table = (BasicTable)e.getSource();
			
			col = table.columnAtPoint(e.getPoint());
			row = table.rowAtPoint(e.getPoint());
			
			if(-1 != row){
				table.getSelectionModel().setSelectionInterval(row, row);
			}
				
		}
		
		popupMenu.setLocation(e.getLocationOnScreen());
		popupMenu.setVisible(true);
	}
	
	protected void hidePopupMenu(){
		popupMenu.setVisible(false);
	}
	
	/* End Handel popup menu */
}
