package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	PetriProCell is a cell that takes a PetriPro
*	and display the process.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *			getRelation
 *			move to special
 *
 */

import javax.swing.*;
import javax.swing.border.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraphListener;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.SelectedCellBorder;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrinet.ppn.*;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;





//PetriProCell
//      |__BaseCell
//              |__ GraphCell
//                      |__ JPanel
public class PetriProCell extends EditableCell
                                  implements MouseListener,
								             InternalGraphListener,
                                             CellListener,
											 EditableCellListener,
                                             PetriProListener{
	
    private InternalGraph graph = new InternalGraph();
    
	private boolean buildnew = true; 
	
    private PetriPro pro = null;
    
	private BaseCell myCell = new Transition();
    private BaseCell nameCell = null;
	
	private boolean compressed = false;
	
    //constructor
    public PetriProCell(PetriPro pp){
		setLayout(null);
		
        graph.addInternalGraphListener(this); //listen to the graph
        graph.addGraphListener(this); //listen to the cells in graph
		
        if(pp == null){
            System.err.println("null in PetriProCell");
        }else{
            pro = pp;
        }
        
		ROP rop = pro.getROP();
		nameCell = new Transition("",pro.getName());
		((EditableCell)nameCell).addEditableCellListener(this);
		
        set(Converter.createBaseCell(rop));
		setCompressed(true);
		
        pro.addPetriProListener(this);
		if(myCell instanceof EditableCell){
			((EditableCell)myCell).addEditableCellListener(this);
		}
		
		graph.drawGraphLines(false);
        
		add(graph);
        sizeChanged(graph);
    }
    
    private void set(BaseCell cell){
        
		if(cell == null){
			return;
		}
        
		if(cell instanceof OpePetriCell){
			//remove color
			((OpePetriCell)cell).setBgColor(new Color(0,0,0,0));
        }
        
		if(myCell == null){
			myCell = cell;
        	graph.insert(myCell);
			myCell.setPos(new Point(-1,-1));
        	graph.addTargetCell(myCell);
        	graph.addSourceCell(myCell);
        
		}else{
			graph.replaceCell(myCell,cell);
			myCell = cell;
			
			myCell.drawTargetLines(haveTargetCells());
			myCell.drawSourceLines(haveSourceCells());
		}
        
		if(myCell instanceof EditableCell){
			((EditableCell)myCell).addEditableCellListener(this);
		}
		graph.pack();
    }
	
	public void setCompressed(boolean set){
		if(set && !compressed) {
			graph.replace(myCell, nameCell);
            compressed = true;
			
			nameCell.drawTargetLines(haveTargetCells());
			nameCell.drawSourceLines(haveSourceCells());
			
			if(nameCell instanceof EditableCell){
				((EditableCell)nameCell).addEditableCellListener(this);
			}
			if(myCell instanceof EditableCell){
				((EditableCell)myCell).addEditableCellListener(this);
			}
		}else if(!set && compressed) {
			graph.replace(nameCell, myCell);
	   		compressed = false;
			
			myCell.drawTargetLines(haveTargetCells());
			myCell.drawSourceLines(haveSourceCells());
			
			if(nameCell instanceof EditableCell){
				((EditableCell)nameCell).addEditableCellListener(this);
			}
			if(myCell instanceof EditableCell){
				((EditableCell)myCell).addEditableCellListener(this);
			}
		}
	}
    
    public Point[] getPoints(int anchor){
        Point myPos = getPos();
		Point[] tmp = null;
		
		if(compressed){
			tmp = nameCell.getPoints(anchor);
		}else{
			tmp = myCell.getPoints(anchor);
		}
        if(tmp != null){
            for(int i = 0; i < tmp.length; i++){
                    tmp[i].translate(myPos.x,myPos.y);
            }
        }
        return tmp;
    }
	
	public void setSelected(boolean selected){
		if(selected){
			graph.setBorder(new SelectedCellBorder());
		}else{
			graph.setBorder(BorderFactory.createEmptyBorder());
		}
		repaint();
	}
    
    public String getExp(){
        return pro.getName();
    }
	public PetriPro getPetriPro(){
        return pro;
    }
	
	//ovveride
	public void drawSourceLines(boolean draw){
		myCell.drawSourceLines(draw);
		nameCell.drawSourceLines(draw);
	}
	
	public void drawTargetLines(boolean draw){
		myCell.drawTargetLines(draw);
		nameCell.drawTargetLines(draw);
	}
	
    //overrides paintComponent in GraphCell
    public void paintComponent(Graphics g) {}
	
	//----- Override EditableCell -----------
	/**
	*	
    *	makes copy() on this cell to work.
	*
    */
	protected BaseCell clone(){
		return new PetriProCell(pro);
	}
	
	//-------------------- MouseListener ---------------------
	//send everything to myCell
    public void mouseClicked(MouseEvent e) {
		if(e.getClickCount() == 2) {
            setCompressed(!compressed);
		}
    }
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {}
    //-------------------- END -----------------------------------
    
    //------------------PetriProListener---------------------------
    public void expChanged(PetriPro pp){
		if(buildnew){
			ROP rop = pp.getROP();
			
			BaseCell newCell = Converter.createBaseCell(rop);
			
			if(newCell instanceof OpePetriCell &&
			    myCell instanceof OpePetriCell){
				   ((OpePetriCell)newCell).setCompressed(
				   ((OpePetriCell)myCell).isCompressed());
			}
			set(newCell);
			this.repaint();
		}
    }
    //------------------ END --------------------------------------
    
    //------------------InternalGraphListener---------------------------
    public void rebuild(){};    
    public void cellClicked(InternalGraph g, CellEvent cEvent){}   
    public void sizeChanged(InternalGraph g){
        
        //the internal graph has changed his size
        //get the positions
        Point thisCellPos = getPos();
        Point graphPos = g.getLocation();
        
        //place the internal graph in upper right corner
        g.setLocation(0, 0);
        //change to same size
        setSize(g.getSize().width, g.getSize().height);
        
        //move the cell
        thisCellPos.translate(graphPos.x, graphPos.y);
        super.setPos(thisCellPos);
    }
    public void cellRemoved(){}
    //------------------------ END ---------------------------------
    
    
    //--------------- CellListener -------------------------------
    //listen to the cells in the internal graph
    public void cellPressed(CellEvent cEvent){
		if(compressed){
	    	cEvent = new CellEvent(this, cEvent.getMouseEvent());
		}else{
			cEvent.translatePos(getLocation().x, getLocation().y);   
		}
        if(cellListener != null) {
	    	cellListener.cellPressed(cEvent);
		}
    }
    public void cellReleased(CellEvent cEvent){
		if(compressed){
	    	cEvent = new CellEvent(this, cEvent.getMouseEvent());
		}else{
			cEvent.translatePos(getLocation().x, getLocation().y);   
		}
		if(cellListener != null) {
	    	cellListener.cellPressed(cEvent);
		}
    }
    public void cellDragged(CellEvent cEvent){
		if(compressed){
	    	cEvent = new CellEvent(this, cEvent.getMouseEvent());
		}else{
			cEvent.translatePos(getLocation().x, getLocation().y);
		}
		
		if(cellListener != null) {
	    	cellListener.cellDragged(cEvent);
		}
    }
    public void cellMove(CellEvent cEvent){
		super.translatePos(cEvent.movement.x, cEvent.movement.y);
		upPack();
    }
    
    public void remove(GraphCell cEvent){};
    
    public void repaint(){
        super.repaint();
    }
    
    public void downPack(){};
    
    public void upPack(){
        graph.pack();
		if(cellListener != null) {
	    	cellListener.upPack();
		}
    }
    
    public void removeSelection(){
        if(cellListener != null) {
	    	cellListener.removeSelection();
		}
    }
    //------------------------- END -----------------------------------
	
	//-------------- EditableCellListener ----
	public void copy(BaseCell cell){}
	public void delete(BaseCell cell){
		super.delete();	//delete this cell
	}
	public void paste(BaseCell cell){
		if(myCell instanceof EditableCell){
			((EditableCell)myCell).paste(cell);
			
			buildnew = false;
			pro.setExp(myCell.getExp());
			buildnew = true;
		}
		super.modified();
	}
	public void modified(BaseCell cell){
		
			buildnew = false;
			pro.setExp(myCell.getExp());
			buildnew = true;
		if(nameCell.equals(cell)){
			System.out.println("nameCell modified");
		}
		super.modified();
	}
	public void replace(BaseCell oldCell, BaseCell newCell){
		set(newCell);
	}
}
