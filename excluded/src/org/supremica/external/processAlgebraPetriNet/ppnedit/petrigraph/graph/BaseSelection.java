package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;

import javax.swing.*;

import java.awt.*;

import org.supremica.external.processeditor.xgraph.*;

public class BaseSelection
						extends Selection{
    
	private GraphCell[] selectedCells = null;

	//constructor
    public BaseSelection() {
    	super();
    }
    
    public void add(GraphCell cell) {
		//DEBUG
		//System.out.println("Selection.add()");
		//END DEBUG
		if(cell == null){
			return;
		}
		
		if(selectedCells == null){
            selectedCells = new GraphCell[] {cell};
        } else {
		
			for(int i = 0; i < selectedCells.length; i++){
				if(cell.equals(selectedCells[i])){
					return;	//don't add cell again
				}
			}
			
			//add new cell
            GraphCell[] tmp = selectedCells;
            selectedCells = new GraphCell[tmp.length+1];
            for(int i = 0; i < tmp.length; i++) {
                selectedCells[i] = tmp[i];
            }
            selectedCells[tmp.length] = cell;
        }
		
		cell.setSelected(true);
		
		selectionChanged();
    }
    
    public void remove(GraphCell cell) {
		//DEBUG
		//System.out.println("Selection.remove()");
		//END DEBUG
		
        if(selectedCells == null || cell == null){
			return;
		}
		
		GraphCell[] tmp = null;
		int index = -1;
		
		//serch for cell
		for(int i = 0; i < selectedCells.length; i++) {
			if(selectedCells[i].equals(cell)){
				index = i;
				break;
			}
		}
		
		//if we have found one
		if(index != -1){
			tmp = selectedCells;
			selectedCells = new GraphCell[tmp.length-1];
			
			int j = 0;
			for(int i = 0; i < tmp.length; i++) {
				if(index != i){
					selectedCells[j] = tmp[i];
					j = j + 1;
				}
			}
			cell.setSelected(false);
			selectionChanged();
		}
		
    }
    
    public void removeAll() {
		//DEBUG
		//System.out.println("Selection.removeAll()");
		//END DEBUG
		
		if(selectedCells == null){
			return;
		}
		
		for(int i = 0; i < selectedCells.length; i++) {
	    	selectedCells[i].setSelected(false);
		}
		selectedCells = null;
		selectionChanged();
    }
    public int isSelected(GraphCell cell) {
	
		if(selectedCells == null){
			return -1;
		}
		
		for(int i = 0; i < selectedCells.length; i++) {
	    	if(selectedCells[i].equals(cell)) {
				return i;
	    	}
		}
		return -1;
    }
    public void update() {
		if(selectedCells == null ||
		   selectedCells.length == 0){
			return;
		}
		
		for(int i = 0; i < selectedCells.length; i++) {
	    	selectedCells[i].setSelected(true);
		}
    }
    
    public void hide(boolean b) {
		//DEBUG
		//System.out.println("Selection.hide()");
		//END DEBUG
		
		if(selectedCells == null){
			return;
		}
		
		for(int i = 0; i < selectedCells.length; i++) {
	    	selectedCells[i].setSelected(!b);
		}
    }
    public void moveSelected(GraphCell cell) {
		//DEBUG
		//System.out.println("Selection.moveSelected()");
		//END DEBUG
		
		if(selectedCells == null){
			return;
		}
		
		Point newOrigo = cell.getPos();
		for(int i = 0; i < selectedCells.length; i++) {
	    	if(!selectedCells[i].equals(cell)) {
			
				int dx = newOrigo.x-origo.x;
				int dy = newOrigo.y-origo.y;
				
	    		selectedCells[i].translatePos(dx,dy);
				
	    	}else {
				;
	    	}
		}
		origo = newOrigo;
    }
    public void setOrigo(GraphCell cell) {
		origo = cell.getPos();
    }    
    
    public GraphCell getSelectedAt(int index) {
	
		if(selectedCells == null || 
		   index > selectedCells.length ||
		   index < 0){
		   	return null;
		}
		
		return selectedCells[index];
	    
    }
	public GraphCell[] getSelected() {	
		return selectedCells;
    }
	
    public void selectionChanged(){
		if(myListener != null) {
	    	myListener.selectionChanged(this);
		}
    }
    public int getSelectedCount() {
		if(selectedCells == null){
			return 0;
		}
		
		return selectedCells.length;
    }
    public boolean hasSelected() {
		if(getSelectedCount() == 0) {
	   		return false;
		}else {
	    	return true;
		}
    }
    
    // -- override
    public void add(CellEvent cEvent) {}
    public void remove(CellEvent cEvent) {}
    public int isSelected(CellEvent cEvent) {
    	return -1;
    }
    
    public void moveSelected(CellEvent cEvent) {}
    
    
    public void setOrigo(CellEvent cEvent) {
    	origo = cEvent.getPos();
    }  
        
        
        
        
    
    
    
    
    
    
    
    
    
    
    
    
}
