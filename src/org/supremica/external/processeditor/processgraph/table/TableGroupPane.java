package org.supremica.external.processeditor.processgraph.table;


import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.*;
import java.awt.event.*;

import org.jdesktop.swingx.*;
import org.jdesktop.swingx.MultiSplitLayout.*;

import java.util.*;
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
	private Object[] rowTableCopy = null;
	
	protected JPopupMenu popupMenu;
	
	protected int[] selectedRows = null;
	
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
		
		table.addtableListener(this);
		table.addKeyListener(this);
		table.addMouseListener(this);
		
		if(comps.length == 0){
			children.add(new Leaf("0"));
			children.add(new Divider());
			children.add(new Leaf("1"));
			modelRoot.setChildren(children);
			getMultiSplitLayout().setModel(modelRoot);
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
		getMultiSplitLayout().setModel(modelRoot);
		
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
	
	public void copyRow(int index){
		
		BasicTablePane table = null;
		
		rowTableCopy = new Object[getComponentCount()];
		
		for(int i = 0; i < rowTableCopy.length; i++){
			
			Object o = getComponent(i);
			
			if(o instanceof BasicTablePane){
				table = (BasicTablePane) o;
				rowTableCopy[i] = table.getTable().getRow(index);
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
	}
	
	public void actionPerformed( ActionEvent event ){
		// Add action handling code here
	}
	
	public void mousePressed(MouseEvent e) {
		;
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.isPopupTrigger()){
			showPopupMenu(e);
		}
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	
	private void showPopupMenu(MouseEvent e){
		
		if(e.getSource() instanceof BasicTable){
			BasicTable table = (BasicTable)e.getSource();
			if(null != table.getComponentAt(e.getPoint())){
				
				 
				int col = table.columnAtPoint(e.getPoint());
				int row = table.rowAtPoint(e.getPoint());
				
				System.out.println("Column: " + col);
				System.out.println("Row: " + row);
			}else{
				System.out.println("Null");
			}
		}else{
			System.out.println("No BasicTable");
		}
		
		popupMenu.setLocation(e.getLocationOnScreen());
		popupMenu.setVisible(true);
	}
	/* End Handel popup menu */
}
