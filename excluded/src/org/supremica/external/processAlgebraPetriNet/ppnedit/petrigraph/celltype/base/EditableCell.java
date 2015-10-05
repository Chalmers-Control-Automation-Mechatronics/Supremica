package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base;

/*
*
*	
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *
 */

import javax.swing.border.*;
import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;


//EditableCell
//      |__BaseCell
//              |__ GraphCell
//                      |__ JPanel
public class EditableCell extends BaseCell {
    
    protected EditableCellListener listener = null;
    
    public EditableCell(){
        super();
    }
    
    public void addEditableCellListener(EditableCellListener listener){
        this.listener = listener;
    }
	
	public void removeEditableCellListener(){
        this.listener = null;
    }
    
    public BaseCell copy() {
		return clone();
    }
	public void modified(){
		if(listener != null){
            listener.modified(this);
        }
    }
    
	/**
	*	Functions to override
	*
	*/
    public void delete(){
		if(listener != null){
            listener.delete(this);
        }
		repaint();
    }
    public void paste(BaseCell cell) {}
	
	public void replace(BaseCell newCell) {
		if(listener != null){
            listener.replace(this,newCell);
        }
	}
    
    protected BaseCell clone() {
        return null;
    }
	
	/**
	*
	*	Extend popupmeny items
	*
	*/
	protected void makePopupCellMenu(){
		super.makePopupCellMenu();
		
		// Create some menu items for the popup
		JMenuItem menuCellDelete = new JMenuItem( "Delete" );

		menuCellDelete.addActionListener(this);
		
		popupMenu.addSeparator();
		popupMenu.add( menuCellDelete );
	}
	
	/**
	*
	*	Tace care of action or send it to super.
	*
	*/
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Delete")){
			delete();
		}else{
			//not here, send to super
			super.actionPerformed(event);
		}
	}
}
