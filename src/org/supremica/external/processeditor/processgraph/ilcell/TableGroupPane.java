package org.supremica.external.processeditor.processgraph.ilcell;


import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import java.awt.*;
import java.awt.event.*;

import javax.swing.event.*;

import java.util.List;

public class TableGroupPane 
						extends 
							JPanel
						implements
							TableListener,
							ActionListener,
							MouseListener,
							KeyListener
{
	private Object[] rowTableCopy = null;
	private String rowCopyName = null;
	
	protected JPopupMenu popupMenu;
	
	protected int[] selectedRows = null;
	
	TableGroupPane(){
		super(new GridBagLayout());
		setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		//setBorder(BorderFactory.createLineBorder(Color.black));
		
		// Create a popup menu
		popupMenu = new JPopupMenu( "Menu" );
		makePopupMenu();
		add( popupMenu );
		
		addMouseListener(this);
	}
	
	public void addTable(BasicTablePane table){
		
		GridBagConstraints c = new GridBagConstraints();
		
		c.fill = GridBagConstraints.BOTH;
		
		c.weightx = 1;
		c.weighty = 1;
		
		c.gridx = getComponentCount();
		c.gridy = 0;
		
		table.addtableListener(this);
		table.addKeyListener(this);
		
		add(table, c);
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
		if(e.isPopupTrigger()){
			showPopupMenu(e.getPoint());
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.isPopupTrigger()){
			showPopupMenu(e.getPoint());
		}
	}
	
	public void mouseClicked(MouseEvent e){}
	public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
	
	private void showPopupMenu(Point pos){
		popupMenu.show(this, pos.x, pos.y);
	}
	/* End Handel popup menu */
}
