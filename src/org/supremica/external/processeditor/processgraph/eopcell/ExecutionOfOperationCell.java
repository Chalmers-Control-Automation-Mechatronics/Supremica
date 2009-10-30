package org.supremica.external.processeditor.processgraph.eopcell;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.processgraph.resrccell.*;

import org.supremica.manufacturingTables.xsd.eop.*;

import java.io.File;

import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;


public class ExecutionOfOperationCell 
									extends 
										ResourceCell
									implements
										ActionListener,
										FocusListener
{
    private static final long serialVersionUID = 1L;
	private static final Color bgColor = new Color(0,0,100,50);
	private static final Color fgColor = Color.BLACK;
	
	private static final String TEXT = "EOP";
	
	private EOPInfoWindow eopInfoWin = null;
	
	private JPopupMenu popupMenu = null;
	
	
	public ExecutionOfOperationCell(EOP eop){
		this();
		if(null != eop){
			setEOP(eop);
		}
	}
	
	public ExecutionOfOperationCell(){
		super();
		
		popupMenu = new JPopupMenu( "Menu" );
		makePopupMenu();
		
		setOperation((new ObjectFactory()).createOperation());
		setSize(30,30);
		
		this.addFocusListener(this);
	}
	
	public void paintComponent(Graphics g) {
		int diff = 4;
		int x,y;
	
		Graphics2D g2 = (Graphics2D) g;
		
		g2.setColor(bgColor);
		g2.fillOval(-diff, -diff, getHeight()+diff*2, getWidth()+diff*2);
		
		g2.setColor(fgColor);
		
		x = (getWidth() - g2.getFontMetrics().stringWidth(TEXT))/2;
		y = getHeight()/2 + g2.getFontMetrics().getHeight()/4;
		
		g2.drawString(TEXT,x,y);
		
	} 
	
	public void setEOP(EOP eop){
		Object o = getFunction();
		if(o instanceof Operation){
			((Operation)o).setEOP(eop);
		}
		setFunction(o);
	}
	
    public void setOperation(Operation op){
    	if(null == op.getEOP()){
    		op.setEOP((new ObjectFactory()).createEOP());
    	}
		setFunction(op);
	}
	
	public EOP getEOP(){
		Object o = getFunction();
		if(o instanceof Operation){
			return ((Operation)o).getEOP();
		}
		return null;
	}
	
	public Operation getOperation(){
		Object o = getFunction();
		
		if(o instanceof Operation){
			return (Operation)o;
		}
		
		return null;
	}
	
	public void setFile(File file) {
		super.setFile(file);
		setToolTipText(file.getAbsolutePath());
	}
	
	public void mouseClicked(MouseEvent e) {
		;
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		
		if(e.isPopupTrigger()){
			showPopupMenu(e);
		}else if(e.getClickCount() > 1){
			
			if(null == eopInfoWin){
				eopInfoWin = new EOPInfoWindow(this);
			}else{
				eopInfoWin.setEOP(getEOP());
			}
			
			eopInfoWin.setVisible(true);
			if(null != getFile()){
	    		eopInfoWin.setFile(getFile());
	    	}
			
		}else{
			super.mouseReleased(e);
		}
	}
	
	/* Handel popup menu */
	protected void makePopupMenu(){
		
		//override this to make pop-up-menu
		// Action and mouse listener support
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );
		
		// Create some menu items for the popup
		JMenuItem menuItem = new JMenuItem( "Set Operation" );
		menuItem.addActionListener( this );
		popupMenu.add( menuItem );
		
		menuItem = new JMenuItem( "Set Machine" );
		menuItem.addActionListener( this );
		popupMenu.add( menuItem );
	}
	
	private void showPopupMenu( MouseEvent e ){
		popupMenu.setLocation( e.getLocationOnScreen() );
		popupMenu.setVisible( true );
	}
	
	
	public void actionPerformed( ActionEvent event ){
		String tmp = "";
		
		// Add action handling code here
		if( event.getActionCommand().equals( "Set Operation" ) ){
			
			//hide popupmenu
			popupMenu.setVisible( false );
			
			//show text input dialog
			tmp = (String)JOptionPane.showInputDialog(
                    null,
                    "Operation",
                    "Set Operation",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    getOperation().getOpID());
			
			//set text to operation id
			if( "" != tmp ){
				getOperation().setOpID( tmp );
			}
			
		}else if( event.getActionCommand().equals( "Set Machine" ) ){
			
			//hide popoup menu
			popupMenu.setVisible( false );
			
			//show text input dialog
			tmp = (String)JOptionPane.showInputDialog(
                    null,
                    "Machine",
                    "Set Machine",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    getOperation().getMachine());
			
			//set text to machine
			if( "" != tmp ){
				getOperation().setMachine( tmp );
			}
		}
	}
	
	public void focusGained(FocusEvent e){
		
	}
	
    public void focusLost(FocusEvent e){
    	//hide popupmenu
		popupMenu.setVisible( false );
    }
	
	@Override
	public void pack(){};
	
	@Override
	public boolean isEmpty() {
    	return false;
    }
	
	@Override
	protected NestedCell clone() {
		ObjectFactory factory = new ObjectFactory();
		
		EOP originalEOP = getEOP();
		EOP cloneEOP = factory.createEOP();
		
		cloneEOP.setExternalComponents(originalEOP.getExternalComponents());
		cloneEOP.setInitialState(originalEOP.getInitialState());
		cloneEOP.setInternalComponents(originalEOP.getInternalComponents());
		cloneEOP.setZones(originalEOP.getZones());
		
		cloneEOP.getAction().addAll(originalEOP.getAction());
		
		ExecutionOfOperationCell eopCell = new ExecutionOfOperationCell();
		eopCell.setEOP(cloneEOP);
		
    	return eopCell;
    }
}
