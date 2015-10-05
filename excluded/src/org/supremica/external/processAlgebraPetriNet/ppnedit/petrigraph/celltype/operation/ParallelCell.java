package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	ParallelCell is a cell that make
*	all its internal cells execute in parallel
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:       add sync for equal event	
 *              add coments
 *		getRelation ++
 *
 */

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.BaseCellArray;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;






//Linecell
//  |___ OpePetriCell
//              |__ GraphCell
//                      |__ JPanel
public class ParallelCell 
					extends OpePetriCell {
    
    //constructor
    public ParallelCell(BaseCell[] cells) {	
        super();
        
		relation.setType(RelationType.PARALLEL);
		
        this.cells = cells;
        
        makeParallel();
        updateExp();
        pack();
        setCompressed(true);
    }
    
    protected void makeParallel(){
	
        final int MARGIN = 20;
        Point pos;
        int move_x, move_y;
        
		//check 
        if(cells == null || cells.length == 0){
            return;
        }
        
        for(int i = 0; i < cells.length; i++){
            insert(cells[i]);
        }
        horizontalLine();
       
        //make start event
        BaseCell start_event = new Transition();
        start_event.setExp("sta");
        addTarget(start_event);

        //make last event
        BaseCell last_event = new Transition();
        last_event.setExp("sto");
        addSource(last_event);
        
        // add start place
        for(int i = 0; i < cells.length; i++){
            BaseCell place = new Place();
            
            place.addSourceCell(start_event);
            place.addTargetCell(cells[i]);
            
            pos = cells[i].getPos();
            
            move_x = 0;
            move_y = place.getSize().height + MARGIN;
            
            pos.translate(move_x,-move_y);
            place.setPos(pos);
            
            insert(place);
        }
        
        // add end place
		for(int i = 0; i < cells.length; i++){
            BaseCell place = new Place();
            
            place.addSourceCell(cells[i]);
            place.addTargetCell(last_event);
            
            pos = cells[i].getPos();
            
            move_x = 0;
            move_y = place.getSize().height + cells[i].getSize().height + MARGIN;
            
            pos.translate(move_x,move_y);
            place.setPos(pos);
            
            insert(place);
        }
        
        move_x = getSize().width/2;
        move_y = MARGIN;
        
        pos = new Point(move_x,-move_y);
        start_event.setPos(pos);
        
        move_x = getSize().width/2;
        move_y = getSize().height+MARGIN;
        
        pos = new Point(getSize().width/2,getSize().height+MARGIN);
        last_event.setPos(pos);
        
        //insert
        insert(start_event);
        insert(last_event);
        
    }
	
	protected void rebuildOpePetricell(){
		makeParallel();
		updateExp();
	}
    
    public Point[] getPoints(int anchor){
        Point myPos = super.getPos();
        
        if(compressed){
            Point[] tmp = new Point[1];
            tmp[0] = getCompressedPos(anchor);
            
            if(anchor == GraphCell.UPPER_CENTER){
                tmp[0].setLocation(tmp[0].x,0);
            } else if(anchor == GraphCell.LOWER_CENTER){
                tmp[0].setLocation(tmp[0].x,this.getHeight());
            }
            tmp[0].translate(myPos.x, myPos.y);
            return tmp;
        }
        
        //I use that in a ParallelCell only one target and one source exist
		InternalGraph graph = getGraph();
		
        Point[] tmp = null;
        if(anchor == GraphCell.UPPER_CENTER){
            BaseCell[] target = graph.getTargetCells();
            tmp = target[0].getPoints(anchor);
        }else if(anchor == GraphCell.LOWER_CENTER){
            BaseCell[] source = graph.getSourceCells();
            tmp = source[0].getPoints(anchor);
        }
        
        if(tmp != null){
            for(int i = 0; i < tmp.length; i++){
                tmp[i].translate(myPos.x,myPos.y);
            }
        }
        return tmp;
    }
    
    protected void updateExp(){ 
        if(cells != null){
            
            exp = PetriPro.getOp(PetriPro.PARALLEL) + "{";
                
            for(int i=0; i < (cells.length-1); i++){
                exp = exp + cells[i].getExp() + ", ";
            }
      
            exp = exp + cells[cells.length-1].getExp() + "}";
        }
        setCompressedExp(exp);
    }
    
    public String getExp(){
        updateExp();
        return super.getExp();
    }
    public void setCompressed(boolean set) {	
        updateExp();
        super.setCompressed(set);
    }
    
    //---------- Listeners override ------------------
    public void cellClicked(InternalGraph g, CellEvent cEvent){
        if(l != null){
            l.cellClicked(g,cEvent);
        }
    }
	
	
	//--------------override EditableCellListener ----
	public void copy(BaseCell cell){}
	
	/**
	*	
    *	delete one cell this cell
	*	if no cell left delete this cell
	*
    */
	public void delete(BaseCell cell){
		if(cell == null){
			return;
		}
		
		cells = BaseCellArray.remove(cell, cells);
		
		if(compressed || cells == null || cells.length == 0){
			super.delete();	//delete this cell
		}else if(cells.length == 1){
			super.replace(cells[0]);
		}
	}
	
	/**
	*	
    *	paste one cell to alternative,
	*	add one cell to alternative
	*
    */
	public void paste(BaseCell cell){
		if(cell == null){
			return;
		}
		
		cells = BaseCellArray.add(cell, cells);
		rebuildOpePetricell();
		
		super.paste(cell);
	}
	
	/**
	*
    *	replace one cell in parallel
	*
    */
	public void replace(BaseCell cell, BaseCell newCell){
		super.replace(cell, newCell);
	}
	
	/**
	*	
    *	one cell have been modified
	*
    */
	public void modified(BaseCell cell){
		super.modified(cell);
		updateExp();
	}
}
