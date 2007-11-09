package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	class ArbitaryOrderCell is a cell that order all
*	its internal cell in arbitaryorder.
*
*	all cell will be inserted as PetriProCells
*
*	this cell extends AlternativeCell
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *
 */

import java.awt.*;

import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.*;



//ArbitaryOrderCell
// |__ AlternativeCell
//        |___ OpePetriCell
//               |__ BaseCell
//                    |__ GraphCell
//                            |__ JPanel
public class ArbitaryOrderCell
					extends ParallelCell {
    
	//constructor 
    public ArbitaryOrderCell(BaseCell[] cells) {
        super(cells);  //build ParallelCell
        
        //set relation type
		relation.setType(RelationType.ARBITRARY);
		
        makeArbitaryOrder();
        pack();
        
        this.updateExp();
        
        setPos(new Point(0,0));
        
        //grafical
        setCompressed(true);
    }
    
    private void makeArbitaryOrder(){
        if(cells == null){
            return;
        }
        
        cleanGraph();
        
        makeParallel();
        
        Place place = new Place();
        
        for(int i = 0; i < cells.length; i++){
            place.addSourceCell(cells[i]);
            place.addTargetCell(cells[i]);
        }
        
        place.setToken(true);
        insert(place);
		
    }
    
	@Override
	protected void rebuildOpePetricell(){
		makeArbitaryOrder();
		updateExp();
	}
    
	@Override
	public String getExp(){
        this.updateExp();
        return exp;
    }
	
    protected void updateExp(){
    	if(cells != null){
            
            exp = PetriPro.getOp(PetriPro.ARBITARY_ORDER) + "{";
                
            for(int i=0; i < (cells.length-1); i++){
                exp = exp + cells[i].getExp() + ", ";
            }
      
            exp = exp + cells[cells.length-1].getExp() + "}";
        }
        setCompressedExp(exp);
    }
	
	//overide clone in AlternativeCell
	@Override
	protected BaseCell clone(){
		final BaseCell[] newCells = new EditableCell[cells.length];
		for(int i = 0; i < newCells.length; i++){
			newCells[i] = ((EditableCell)cells[i]).copy();
		} 
		return new ArbitaryOrderCell(newCells);
	}
	
	//--------------overide EditableCellListener ----
	/**
	*
    *	Override delete in ParallelCell
    *	
    */
	@Override
	public void delete(BaseCell cell){
		super.delete(cell);
	}
	
	/**
	*
    *	Override paste in ParallelCell
    *	add one cell and redo the cell
    *	
    */
	@Override
	public void paste(BaseCell cell){
		if(cell == null){
			return;
		}
		final Point pos = super.getPos();
		cells = BaseCellArray.add(cell, cells);
		makeArbitaryOrder();
        horizontalLine();
		updateExp();
		setPos(pos);
	}
	//--------------End override EditableCellListener ----
}
