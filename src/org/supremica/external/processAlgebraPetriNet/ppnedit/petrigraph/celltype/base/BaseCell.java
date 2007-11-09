package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.util.*;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.event.*;
import java.awt.*;


/*
*	GraphCell is made by Mikael and Magnus
*
*	I extend GraphCell whit BaseCell in order to 
*	Manipulate the apperance and functions to
*	build petrinets whitout change their code.
*
*	David Millares 
*/

//class to extends functions to GraphCell
//BaseCell
//      |__GraphCell
public class BaseCell 
				extends 
					GraphCell 
						implements ActionListener{
    
    private BaseCell[] targetCells = null;
    private BaseCell[] sourceCells = null;
	
    private static int instanceOfBaseCell = 0;
    protected int id;
    
    protected String exp = "";
	
	protected JPopupMenu popupMenu;
    
    public BaseCell() {
        super();
        instanceOfBaseCell = instanceOfBaseCell + 1;
        id = instanceOfBaseCell;
		
		// Create a popup menu
		popupMenu = new JPopupMenu( "Menu" );
		makePopupCellMenu();
		add( popupMenu );
    }
	
    public void setExp(String exp){
        this.exp = exp;
    }
	
    public String getExp(){
        return exp;
    }
    
    public int getId(){
        return id;
    }
	
    public Object getRelation(){
        return null;
    }
	
    public void addTargetCell(BaseCell cell){
        if(targetCells == null){
            targetCells = new BaseCell[] {cell};
        }else{
            targetCells = BaseCellArray.add(cell,targetCells);
        }
		drawTargetLines(true);
    }
    
    public Point[] getPoints(int anchor){
        Point[] tmp = new Point[1];
        tmp[0] = getPos(anchor);
        return tmp;
    }
    
    public void addSourceCell(BaseCell cell){
        if(sourceCells == null){
            sourceCells = new BaseCell[] {cell};
        } else {
            sourceCells = BaseCellArray.add(cell,sourceCells);
        }
		drawSourceLines(true);
    }
    
    public void removeTargetCell(BaseCell cell){
        if(targetCells != null){
            targetCells = BaseCellArray.remove(cell,targetCells);
        }
		drawTargetLines(haveTargetCells());
    }
    
    public Point[] getTargetPoints(){
        Point[] tmp = new Point[1];
        tmp[0] = getPos(UPPER_CENTER);
        return tmp;
    }
    
    public Point[] getSourcePoints(){
        Point[] tmp = new Point[1];
        tmp[0] = getPos(LOWER_CENTER);
        return tmp;
    }
    
    public void removeSourceCell(BaseCell cell){
        if(sourceCells != null){
        	sourceCells = BaseCellArray.remove(cell,sourceCells);
        }
		drawSourceLines(haveSourceCells());
    }
	
	/*functions to override*/
	public void drawSourceLines(boolean draw){};
	public void drawTargetLines(boolean draw){};
	/*end functions to override*/
    
	public boolean haveTargetCells(){
		return targetCells != null;
	}
	public boolean haveSourceCells(){
		return sourceCells != null;
	}
	
    public BaseCell[] getTargetCells(){
        return targetCells;
    }
    public BaseCell[] getSourceCells(){
        return sourceCells;
    }
	
    public boolean isTargetCell(BaseCell cell){
        if(targetCells == null){
            return false;
        }
        for(int i = 0; i < targetCells.length; i++) {
            if(cell.equals(targetCells[i])){
                return true;
            }
        }
        return false;
    }
 
    public boolean isSourceCell(BaseCell cell){
        if(sourceCells == null){
            return false;
        }
        for(int i = 0; i < sourceCells.length; i++) {
            if(cell.equals(sourceCells[i])){
                return true;
            }
        }
        return false;
    }
	
	/* Handel popup menu */
	protected void makePopupCellMenu(){
		// Create some menu items for the popup
		JMenuItem menuEdit = new JMenuItem( "Edit" );
	
		popupMenu.add( menuEdit );
		
		menuEdit.addActionListener( this );
		
		// Action and mouse listener support
		enableEvents( AWTEvent.MOUSE_EVENT_MASK );
	}
	
	public void actionPerformed( ActionEvent event ){
		// Add action handling code here
		if(event.getActionCommand().equals("Edit")){
			;
		}else{
			System.out.println( "Unknown event: " + event );
		}
	}
	
	public void mousePressed(MouseEvent e) {
		if(e.isPopupTrigger()){
			showPopupMenu(e.getPoint());
		}else{
			super.mousePressed(e);
		}
	}
	
	public void mouseReleased(MouseEvent e) {
		if(e.isPopupTrigger()){
			showPopupMenu(e.getPoint());
		}else{
			super.mouseReleased(e);	
		}
	}
	
	private void showPopupMenu(Point pos){
		popupMenu.show(this, pos.x, pos.y);
	}
	/* End Handel popup menu */
}
