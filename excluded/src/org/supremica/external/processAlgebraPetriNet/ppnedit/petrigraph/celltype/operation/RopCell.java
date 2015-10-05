package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	
*/

/*
 * To Do:	Everything
 *
 */

import java.awt.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.Converter;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.BaseCellArray;
import org.supremica.external.processeditor.xgraph.*;



public class RopCell
				extends OpePetriCell{
	
	private Place start;
	private Place end;
	
	private boolean comp = true;
	private boolean have_places = true;
		
	private boolean targetLines = false;
 	private boolean sourceLines = false;
 	
    private ROP rop = null;
    
	//constructor
    public RopCell(ROP rop){
		super();
		
		/* fix compressed */
		super.setCompressed(false);
		setCompressed(true);
		
		this.rop = rop;
		buildCell();
		
		setCompressed(comp);
		verticalLine();
		
		pack();
		super.setPos(new Point(10,10)); //default pos
    }
	
	private void buildCell(){
		if(rop == null){
			System.out.println("null in buildCell");
			return;
		}
		
		cleanGraph();
		
		start = new Place();
		end = new Place();
		
		BaseCell tmp = new Transition();
		tmp.setExp(rop.getMachine());
		cells = BaseCellArray.add(tmp,cells);
		
		tmp = Converter.createBaseCell(rop.getRelation());
		cells = BaseCellArray.add(tmp,cells);
	
		start.setText(rop.getMachine());
		start.setInfoAtHorizont(false);
    	start.setInfoRightUpp(true);
    	start.setToken(true);
		
		insert(start);
		insert(cells[0]);
		insert(end);
		
		start.addTargetCell(cells[0]);
		end.addSourceCell(cells[0]);
		
		//to get all delete comands
		listenToCell(start);
		listenToCell(end);
		
		listenToCells();
	}
	
	private void updateROP(){
		if(rop == null){
			System.out.println("null in updateROP");
			return;
		}
		
		rop.setMachine(cells[0].getExp());
		
		Object o = cells[1].getRelation();
		
		if(o instanceof Relation){
			rop.setRelation((Relation)o);
		}else if(o instanceof Activity){
			Relation r = rop.getRelation();
			r.setType(RelationType.SEQUENCE);
			r.getActivityRelationGroup().clear();
			r.getActivityRelationGroup().add(o);
		}else{
			System.err.println("Unknown object " + o.toString() + " in RopCell");
		}
	}
	
	public ROP getROP(){
		updateROP();
		return rop;
	}
	
	public Relation getRelation(){
		updateROP();
		return rop.getRelation();
	}
	
	public void removePlaces(){
		remove(start);
		remove(end);
		have_places = false;
	}
	
	//ovveride
	@Override
	public void drawSourceLines(final boolean draw){
		if((cells == null) || (cells.length != 2)){
			return;
		}
		cells[0].drawSourceLines(draw);
		cells[1].drawSourceLines(draw);
 		sourceLines = draw;
	}
	
	@Override
	public void drawTargetLines(final boolean draw){
		if((cells == null) || (cells.length != 2)){
			return;
		}
		cells[0].drawTargetLines(draw);
		cells[1].drawTargetLines(draw);
		targetLines = draw;
	}
	
	@Override
	protected void rebuildOpePetricell(){
		updateROP();
		buildCell();
	}
    
    @Override
	public Point[] getPoints(final int anchor){
		final Point myPos = super.getPos();
		
        Point[] tmp = null;
		
		if(have_places){
			if(anchor == GraphCell.UPPER_CENTER){
				tmp = start.getPoints(anchor);
			}else if(anchor == GraphCell.LOWER_CENTER){
				tmp = end.getPoints(anchor);	
			}
		}else{
			if(comp){
				tmp = cells[0].getPoints(anchor);
			}else{
        		tmp = cells[1].getPoints(anchor);
        	}
		}
		
        if(tmp != null){
            for (Point element : tmp) {
                element.translate(myPos.x,myPos.y);
            }
        }
        return tmp;
    }
	
	@Override
	public boolean isCompressed(){
		return comp;
	}
	
	@Override
	public void setCompressed(boolean set) {
		//find center
		final Point pos = getPos();
		pos.translate(getWidth()/2,getHeight()/2);
		
		final InternalGraph graph = getGraph();
        if(set && !comp) {
            graph.replace(cells[1],cells[0]);
			comp = true;
			start.showInfo(false);
		}else if(!set && comp) {
	    	graph.replace(cells[0],cells[1]);
			comp = false;
			start.setText(cells[0].getExp());
			start.showInfo(true);
		}
		
		if(!have_places){
			if(comp){
				cells[0].drawSourceLines(sourceLines);
				cells[0].drawTargetLines(targetLines);
			}else{
				cells[1].drawSourceLines(sourceLines);
				cells[1].drawTargetLines(targetLines);
			}
		}
		listenToCells();
		
		//center cell
		setPos(pos);
		translatePos(-getWidth()/2,-getHeight()/2);
    }
	
	/**
    *
    *	return expression in human form
    *	
    */
	@Override
	public String getExp(){
        return cells[0].getExp();
    }

	//override in OpePetriCell
	@Override
	public void upPack(){
		super.upPack();
		verticalLine();
	}
	@Override
	public void downPack(){
		super.downPack();
		verticalLine();
	}
	
	//makes copy() work
	@Override
	protected BaseCell clone(){
        	return new RopCell(rop);
    }
	
	//--------------overide EditableCellListener ----
	
	/**
	*
    *	Override delete in OpPetriCell
    *	
    *	
    */
	@Override
	public void delete(final BaseCell cell){
		//do nothing
	}
	
	@Override
	public void replace(final BaseCell oldCell, final BaseCell newCell){
		if((oldCell == null) || (newCell == null)){
			return;
		}
		
		InternalGraph graph = getGraph();
		
		if(oldCell.equals(cells[0])){
			cells = BaseCellArray.replace(cells[1], newCell, cells);
			graph.replace(cells[1], newCell);
			cells[0].setExp(rop.getMachine());
			modified();
		}else{
			cells = BaseCellArray.replace(oldCell, newCell, cells);
			graph.replace(oldCell, newCell);
			updateROP();
			modified();
		}
	}
}
