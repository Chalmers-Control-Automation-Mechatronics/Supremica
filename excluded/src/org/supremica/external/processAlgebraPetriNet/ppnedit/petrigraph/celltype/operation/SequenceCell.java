package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	class SequenceCell is a cell that order all
*	its internal cell in sequence.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *
 */

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.*;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;






//SequenceCell
//  |___ OpePetriCell
//              |__ GraphCell
//                      |__ JPanel
public class SequenceCell
					extends OpePetriCell {
    
	private BaseCell[] places = null;
	
	private boolean targetLines = false;
 	private boolean sourceLines = false;
	
    //constructor
    public SequenceCell(){
        String[] n = {"a"}; 
        set(n,new Point(0,0));
    }
    
    public SequenceCell(String[] names) {	
        set(names, new Point(0,0));
        verticalLine();
    }
    
    public SequenceCell(BaseCell[] cells) {	
        relation.setType(RelationType.SEQUENCE);
		
		this.cells = cells;
		
		makeSequence();
        updateExp();        
        pack();
        
        verticalLine();
        setCompressed(true);
    }
    
    private void set(String[] names, Point pos){
		relation.setType(RelationType.fromValue("Sequence"));
		
        makeSequence(names);
        pack();
        setCompressed(true);
        super.setPos(pos);
    }
    
	/**
	*	make a sequence from array of String
	*	every string will give name to transition
	*	in sequence.
    */
    private void makeSequence(String[] names){
        
        cells = new Transition[names.length];
        
        //make transitions
        for(int i = 0; i < cells.length; i++){
            cells[i] = new Transition();
            cells[i].setExp(names[i]);
        }
        makeSequence();
    }
    
	/**
	*	make this Sequence. Be sure
	*	to initalize cells before
	*	invoking this function.
	*
    */
    private void makeSequence(){
        
        if(cells == null || cells.length == 0){
            return;
        }
        
		cleanGraph();
		
        int antPs = cells.length-1;
        places = new BaseCell[antPs];
        
        //insert first cell
        insert(cells[0]);
        cells[0].setPos(new Point(-1,-1));
		
        //make the rest
        for(int i = 1; i < cells.length; i++){
			
			/*
			if(cells[i] instanceof RopCell){
				places[i-1] = cells[i];
			}else{
		    	places[i-1] = new Place();
			}*/
			
			places[i-1] = new Place();
			
			//connect place to cell
            places[i-1].addSourceCell(cells[i-1]);
            places[i-1].addTargetCell(cells[i]);
                        
            insert(places[i-1]);
            insert(cells[i]);
			
			if(places[i-1] instanceof Place){
				((EditableCell)places[i-1]).removeEditableCellListener();
			}
        }
		
        addTarget(cells[0]);  //first cell is target
        addSource(cells[cells.length-1]); //last cell is source
		
		listenToCells();
    }
	protected void rebuildOpePetricell(){
		makeSequence();
		verticalLine();
	}
	
	
	//ovveride
	public void drawSourceLines(boolean draw){
		sourceLines = draw;
		super.drawSourceLines(draw);
	}
	
	public void drawTargetLines(boolean draw){
		targetLines = draw;
		super.drawTargetLines(draw);
	}
	
	/**
	*	Add popupmeny items
	*
	*/
	protected void makePopupCellMenu(){
		super.makePopupCellMenu();
		
		JMenuItem menuSeqLine = new JMenuItem( "Line" );
		menuSeqLine.addActionListener( this );
		popupMenu.add( menuSeqLine );
	}
	public void actionPerformed( ActionEvent event ){
		if(event.getActionCommand().equals("Line")){
			verticalLine();
		}else{
			super.actionPerformed(event);
		}
	}
   	
	/**
	*	Indata: int anchor are defined in
	*	GraphCell. Give where UPP/DOWN/CENTER ...
	*	we whant to conect.
	* 
    *	return array of Point where every
	*	Point is a conection to this cell.
	*
    */
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
        
		InternalGraph graph = getGraph();
		
		//I use that in a SequenceCell only one target and one source exist
        Point[] tmp = null;
        if(anchor == GraphCell.UPPER_CENTER){
            BaseCell[] target = graph.getTargetCells();
			if(target != null){
            	tmp = target[0].getPoints(anchor);
			}
        }else if(anchor == GraphCell.LOWER_CENTER){
            BaseCell[] source = graph.getSourceCells();
			if(source != null){
            	tmp = source[0].getPoints(anchor);
			}
        }
        
        if(tmp != null){
            for(int i = 0; i < tmp.length; i++){
                tmp[i].translate(myPos.x,myPos.y);
            }
        }
        return tmp;
    }
    
	/**
	*	
    *	update this cells expression
	*	shoud be invoked every time
	*	cells in this cell have been
	*	modified
	*
    */
    private void updateExp(){   
        
        if(cells == null || cells.length == 0){
			return;
		}
		
		if(cells[0] instanceof OpePetriCell){
			exp = "(" + cells[0].getExp()+ ")";
		}else{
			exp = cells[0].getExp();
		}
		
		for(int i=1; i < cells.length; i++){
			exp = exp + PetriPro.getOp(PetriPro.SEQUENCE);
			
			if(cells[i] instanceof OpePetriCell){
				exp = exp + "(" + cells[i].getExp()+ ")";
			}else{
				exp = exp + cells[i].getExp();
			}
		}
		setCompressedExp(exp);
    }
	
	/**
	*	removes first event in sequence
    */
    public void removeFirstEvent(){
		if(cells[0] instanceof SequenceCell){
			((SequenceCell)cells[0]).removeFirstEvent();
			verticalLine();
			updateExp();
			super.modified();
			return;	
		}
		
		if(compressed){
			setCompressed(false);
			delete(cells[0]);
			setCompressed(true);
		}else{
        	delete(cells[0]);
		}
		
		verticalLine();
		updateExp();
		super.modified();
    }
	
    /**
	*	removes last event in sequence
    */
	public void removeLastEvent(){
		if(cells[cells.length-1] instanceof SequenceCell){
			((SequenceCell)cells[cells.length-1]).removeLastEvent();
			verticalLine();
			updateExp();
			super.modified();
			return;	
		}
		
		if(compressed){
			setCompressed(false);
			delete(cells[cells.length-1]);
			setCompressed(true);
		}else{
			delete(cells[cells.length-1]);
		}
		
		verticalLine();
		updateExp();
		super.modified();
    }
	
	/**
	*	return this cells
    *	expression.
    */
    public String getExp(){
        updateExp();
        return super.getExp();
    }
	
	/**
	*	get first cell/event in sequence	
    */
    public BaseCell getFirstCell(){
        return cells[0];
    }
	
	/**
	*	get last cell/event in sequence
    */
    public BaseCell getLastCell(){
        return cells[cells.length-1];
    }
	
	
	/**
	*	setCompressed(boolean set)	
    *	switch wiev betwen compressed
	*	and full wiev.
    */
    public void setCompressed(boolean set) {	
        updateExp();
        super.setCompressed(set);
    }
    
	//----- Override EditableCell -----------
	/**
	*
    *	copy all cells in sequence
    *	and return new SequenceCell
	*	
    *	makes copy() on this cell to work.
	*
    */
	protected BaseCell clone(){
		BaseCell[] newCells = new EditableCell[cells.length];
		for(int i = 0; i < newCells.length; i++){
			newCells[i] = (BaseCell)((EditableCell)cells[i]).copy();
		} 
		return new SequenceCell(newCells);
	}
	
	//
    //----------------- Listeners --------------------
	//
	
	//----- override cellClicked in OpPetriCell -------
    public void cellClicked(InternalGraph g, CellEvent cEvent){
        if(l != null){
            l.cellClicked(g,cEvent);
        }
    }
	
	//--------------overide EditableCellListener ----
	/**
	*
    *	Override delete in OpPetriCell
    *	delete cell and its places
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
			cells[0].drawSourceLines(sourceLines);
        	cells[0].drawTargetLines(targetLines);
			super.replace(cells[0]);
		}else{
		
			cells[0].drawSourceLines(sourceLines);
        	cells[cells.length-1].drawTargetLines(targetLines);
		
			makeSequence();
        	verticalLine();
			updateExp();
			super.modified();
		}
	}
	
	/**
	*
    *	Override paste in OpPetriCell
    *	add one cell last in sequence
    *	
    */
	public void paste(BaseCell cell){
	
		if(cell == null){
			return;
		}
		
		cells = BaseCellArray.add(cell, cells);
		
		cell.drawSourceLines(sourceLines);
        cell.drawTargetLines(targetLines);
		
		makeSequence();
        verticalLine();
		updateExp();
		super.modified();
	}
	
	/**
	*
    *	replace one cell in sequence
	*
    */
	public void replace(BaseCell cell, BaseCell newCell){
		
		newCell.drawSourceLines(sourceLines);
        newCell.drawTargetLines(targetLines);
		
		super.replace(cell, newCell);
	}
	//--------------End override EditableCellListener ----
}
