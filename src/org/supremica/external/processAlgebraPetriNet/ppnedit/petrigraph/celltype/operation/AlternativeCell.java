package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	class AlternativeCell is a cell that order all
*	its internal cell in alternative.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *			getRelation
 *
 */


import javax.swing.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.PetriPro;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.event.*;
import java.awt.*;







//AlternativeCell
//  |___ OpePetriCell
//              |__ GraphCell
//                      |__ JPanel
public class AlternativeCell
					extends OpePetriCell {

	private boolean targetLines = false;
 	private boolean sourceLines = false;
	
    //constructor
    public AlternativeCell(){
		super();
		relation.setType(RelationType.ALTERNATIVE);
		
        String[] n = {"a"};
        set(n);
    }
    
    public AlternativeCell(String[] names) {
		super();
		relation.setType(RelationType.ALTERNATIVE);
        set(names);
    }
    
    public AlternativeCell(BaseCell[] cells) {
		super();
		
		relation.setType(RelationType.ALTERNATIVE);
		
        makeAlternative(cells);
        setCompressed(true);
        horizontalLine();
		pack();
		super.setPos(new Point(0,0)); //default pos
    }
	
    /**
    *	Indata: String[]
    *
    *	make alternative from array of string. 
    *	
    */
    private void set(String[] names){	
        makeAlternative(names);
        setCompressed(true);
        horizontalLine();
		pack();
		super.setPos(new Point(0,0)); //default pos
    }
    
	/**
    *	Indata: String[]
    *
    *	make alternative from array of string. 
    *	
    */
    private void makeAlternative(String[] names){
        
        int antTs = names.length;
        
        cells = new Transition[antTs];
        
        //make the transitions
        for(int i=0; i < cells.length; i++){
            cells[i] = new Transition();
            cells[i].setExp(names[i]);
        }
        makeAlternative();
    }
    
	/**
    *	Indata: BaseCell[]
    *
    *	make alternative from array of BaseCell. 
    *	
    */
	protected void makeAlternative(BaseCell[] cells){
        this.cells = cells;
		makeAlternative();
    }
	
	/**
    *	Indata: in cells
    *
    *	make alternative from BaseCells in cells 
    *	
    */
    protected void makeAlternative(){
        
        if(cells == null || cells.length == 0){
            return;
        }
	
		cleanGraph();
		cells[0].setPos(new Point(-1,-1));
        for(int i=0; i < cells.length; i++){
            
            insert(cells[i]);
            
            addTarget(cells[i]);
            addSource(cells[i]);
        }
        updateExp();
		listenToCells();
    }
	
	protected void rebuildOpePetricell(){
		makeAlternative();
		horizontalLine();
	}
	
	/**
    *	Indata: int anchor
    *
    *	anchor deside point to connect to
    *	return conections point Point[]
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
		
        Point[] tmp = null;
        if(anchor == GraphCell.UPPER_CENTER){
            tmp = graph.getTargetPoints();
        }else if(anchor == GraphCell.LOWER_CENTER){
            tmp = graph.getSourcePoints();
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
    *	return expression in human form
    *	
    */
	public String getExp(){
        updateExp();
        return super.getExp();
    }
	
	//ovveride
	public void drawSourceLines(boolean draw){
		sourceLines = draw;
		getGraph().drawSourceLines(draw);
		super.drawSourceLines(draw);
	}
	
	public void drawTargetLines(boolean draw){
		targetLines = draw;
		getGraph().drawTargetLines(draw);
		super.drawTargetLines(draw);
	}
	
    private void updateExp(){
        exp = "";
        if(cells == null || cells.length == 0){
			return;
		}
		
		if(cells[0] instanceof OpePetriCell &&
		   !(cells[0] instanceof SequenceCell)){
			exp = "(" + cells[0].getExp()+ ")";
		}else{
			exp = cells[0].getExp();
		}
			
		for(int i=1; i < cells.length; i++){
			exp = exp + PetriPro.getOp(PetriPro.ALTERNATIVE);
			if(cells[i] instanceof OpePetriCell &&
			   !(cells[i] instanceof SequenceCell)){
				exp = exp + "(" + cells[i].getExp()+ ")";
			}else{
				exp = exp + cells[i].getExp();
			}
		}
        
        setCompressedExp(exp);
    }
	
	public void upPack(){
		//horizontalLine();
		super.upPack();
	}
    
	//overide clone in EditableCell
	/**
	*	
    *	makes copy() on this cell to work.
	*
    */
	protected BaseCell clone(){
		BaseCell[] newCells = new EditableCell[cells.length];
		for(int i = 0; i < newCells.length; i++){
			newCells[i] = (BaseCell)((EditableCell)cells[i]).copy();
		} 
		return new AlternativeCell(newCells);
	}
	
	
	/**
	*	Add popupmeny items
	*
	*/
	protected void makePopupCellMenu(){
		super.makePopupCellMenu();
		
		// Create some menu items for the popup
		JMenuItem menuAltLine = new JMenuItem( "Line" );
		
		menuAltLine.addActionListener( this );
		
		popupMenu.add( menuAltLine );
	}
	public void actionPerformed( ActionEvent event ){
		// Add action handling code here
		if(event.getActionCommand().equals("Line")){
			horizontalLine();
		}else{
			super.actionPerformed(event);
		}
	}
	
	
	//--------------------Listener override --------
	public void cellClicked(InternalGraph g, CellEvent cEvent){
        if(l != null){
            l.cellClicked(g,cEvent);
        }
    }
	
	//--------------overide EditableCellListener ----
	public void copy(BaseCell cell){}
	
	/**
	*	
    *	delet one cell from alternative
	*	if no cell left delete this cell
	*
    */
	public void delete(BaseCell cell){
		removeCell(cell);
		
		updateExp();
		super.modified();
		
		if(compressed || cells == null || cells.length == 0){
			super.delete();	//delete this cell
		}else if(cells.length == 1){
			super.replace(cells[0]); //replace this cell to cells[0]
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
		
		addCell(cell);
		
		addTarget(cell);
        addSource(cell);
		
		cell.drawSourceLines(sourceLines);
        cell.drawTargetLines(targetLines);
		
		updateExp();
		super.paste(cell);
	}
	
	/**
	*
    *	replace one cell in alternative
	*
    */
	public void replace(BaseCell cell, BaseCell newCell){
		
		newCell.drawSourceLines(sourceLines);
        newCell.drawTargetLines(targetLines);
		
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
