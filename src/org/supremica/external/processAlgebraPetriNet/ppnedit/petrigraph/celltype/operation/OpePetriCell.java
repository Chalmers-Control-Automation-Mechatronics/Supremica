package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.operation;

/*
*	class OpPetriCell is a base cell who builds upp
*	common function for all operation in a petri graph.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add coments
 *
 */

import javax.swing.border.*;
import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.converter.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraph;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.InternalGraphListener;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.SelectedCellBorder;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.*;
import org.supremica.external.processeditor.xgraph.*;

import java.awt.event.*;
import java.awt.*;
import java.util.*;






public class OpePetriCell 
					extends EditableCell 
								implements InternalGraphListener,
                                           CellListener,
                                           MouseListener,
										   EditableCellListener{
	
    protected InternalGraphListener l = null;
    
	//class common variables                                                         
    protected final static int MARGIN = 5;
    
    //internal cells
    protected BaseCell[] cells = null;
	
	protected Relation relation;
    
    private InternalGraph graph = new InternalGraph(); //internal Graph
    private InternalGraph cgraph = new InternalGraph(); //compressed graph
   
    protected boolean compressed = false;
    
    private Transition ct = new Transition();

    //constructor
    public OpePetriCell(){
        String[] n = {"a"};
        set(n);
		add(graph);
    }
    
    public OpePetriCell(String[] names) {	
        set(names);
    }
    
    private void set(String[] names){
        setLayout(null);  //OBS!!
        
		ObjectFactory factory = new ObjectFactory();
		relation = factory.createRelation();
		
        //listeners
        cgraph.addInternalGraphListener(this); //listen to the compressed graph
        graph.addInternalGraphListener(this); //listen to the graph
        
        graph.addGraphListener(this); //listen to the cells in graph
        cgraph.addGraphListener(this); //listen to the cells in cgraph
        
		graph.drawGraphLines(false);
		cgraph.drawGraphLines(false);
		
        makeCompressedGraph();
    }
	
    //make the compressed form of this cell
    protected void makeCompressedGraph(){
        //add one transition
        cgraph.insert(ct);
        
        cgraph.addTargetCell(ct);
        cgraph.addSourceCell(ct);
		
		ct.addEditableCellListener(this);
        ct.setEditable(true);
		
        //to avoid misspainting 
        ct.setPos(new Point(cgraph.getLocation().x+1,cgraph.getLocation().y+1));
        cgraph.upPack();
        cgraph.repaint();
    }
    
	public Object getRelation(){
		
		ObjectFactory factory = new ObjectFactory();
		
		//add position
		Position pos = factory.createPosition();
		pos.setXCoordinate(getX());
		pos.setYCoordinate(getY());
		
		relation.setPosition(pos);
		
		//add view
		relation.setAlgebraic(factory.createAlgebraic());
		relation.getAlgebraic().setCompressed(compressed);
		
		
		//clear relationgroup
		relation.getActivityRelationGroup().clear();
		
		//add relation
		for(int i = 0; i < cells.length; i++){
			if(cells[i].getRelation() != null){
				relation.getActivityRelationGroup().add(cells[i].getRelation());
			}
		}
        return relation;
    }
	
	/**
	*	Function to call then cells are changed
	*	override then extended.
    */
	protected void rebuildOpePetricell(){};
	
    public void setCompressedExp(String exp){
		ct.removeEditableCellListener();
		ct.setExp(exp);
		ct.addEditableCellListener(this);
    }
    
    protected Point getCompressedPos(int anchor){
        return ct.getPos(anchor);    
    }
    
    public void setCompressed(boolean set) {	
        if(set && !compressed) {
            remove(graph);
            add(cgraph);
            
	    	this.translatePos((graph.getSize().width-cgraph.getSize().width)/2, 
			      			(graph.getSize().height-cgraph.getSize().height)/2);
            compressed = true;
            sizeChanged(cgraph);
			
			downPack();
		}else if(!set && compressed) {
	    	remove(cgraph);
	    	add(graph);
	    
            this.translatePos(-(graph.getSize().width-cgraph.getSize().width)/2, 
	    		      		-(graph.getSize().height-cgraph.getSize().height)/2);
	   		compressed = false;
			
			sizeChanged(graph);
			upPack();
		}
    }
	
	public boolean isCompressed(){
		return compressed;
	}
	
    protected void switchCompressed(){
         setCompressed(!isCompressed());
    }
	
    //overrides paintComponent in GraphCell
    public void paintComponent(Graphics g) {}
	
	public void addInternalGraphListener(InternalGraphListener l){
        this.l = l;
    }
	
	/**
	*	if cells instanceof Editable cell set this cell as
	*	listener.
	*/
	protected void listenToCells(){
		if(cells != null && cells.length > 0){
			for(int i = 0; i < cells.length; i++){
				if(cells[i] instanceof EditableCell){
					((EditableCell)cells[i]).addEditableCellListener(this);
				}
			} 
		}
	}
	
	protected void listenToCell(BaseCell cell){
		if(cell instanceof EditableCell){
			((EditableCell)cell).addEditableCellListener(this);
		}
	}
	
	/**
	*	Functions to give axess to graph
	*/
	protected InternalGraph getGraph(){
	 	return graph;
	}
	protected void insert(BaseCell cell){
	 	graph.insert(cell);
	}
	protected void remove(BaseCell cell){
		graph.remove(cell);
	}
	protected void addTarget(BaseCell cell){
		graph.addTargetCell(cell);
	}
    protected void addSource(BaseCell cell){
		graph.addSourceCell(cell);
	}
	protected void removeTarget(BaseCell cell){
		graph.removeTargetCell(cell);
	}
	protected void removeSource(BaseCell cell){
		graph.removeSourceCell(cell);
	}
	protected void pack(){
		Point thisCellPos = getPos();
		graph.pack();
		setPos(thisCellPos);
		repaint();
	}
	protected void horizontalLine(){
		Point pos = getPos();
		pos.translate(getWidth()/2,getHeight()/2);
		
		graph.horizontalLine();
		
		//center cell
		setPos(pos);
		translatePos(-getWidth()/2,-getHeight()/2);
	}
	protected void verticalLine(){
		Point pos = getPos();
		pos.translate(getWidth()/2,getHeight()/2);
		
		graph.verticalLine();
		
		//center cell
		setPos(pos);
		translatePos(-getWidth()/2,-getHeight()/2);
	}
	protected void cleanGraph(){
		graph.removeAll();
	}
	
	public void drawSourceLines(boolean draw){
		
		cgraph.drawSourceLines(draw);
		ct.drawSourceLines(draw);
		
		BaseCell[] cells = graph.getTargetCells();
		if(cells != null){
			for(int i = 0; i < cells.length; i++){
				cells[i].drawSourceLines(draw);
			}
		}
	}
	
	public void drawTargetLines(boolean draw){
		
		cgraph.drawTargetLines(draw);
		ct.drawTargetLines(draw);
		
		BaseCell[] cells = graph.getSourceCells();
		if(cells != null){
			for(int i = 0; i < cells.length; i++){
				cells[i].drawTargetLines(draw);
			}
		}
	}
	
	public void setMargin(int margin){
		graph.setMargin(margin);
		cgraph.setMargin(margin);
	}
	
	public void setBgColor(Color bgcolor){
		graph.setBgColor(bgcolor);
		cgraph.setBgColor(bgcolor);
	}
	
	public void setSelected(boolean selected){
		if(selected){
			if(compressed){
				cgraph.setBorder(new SelectedCellBorder());
			}else{
				graph.setBorder(new SelectedCellBorder());
			}
		}else{
			if(compressed){
				cgraph.setBorder(BorderFactory.createEmptyBorder());
			}else{
				graph.setBorder(BorderFactory.createEmptyBorder());
			}
		}
	}
	
	/**
	*	addCell to existing graph
	*/
	protected void addCell(BaseCell cell){
		cells = BaseCellArray.add(cell, cells);
		graph.insert(cell);
		
		if(cell instanceof EditableCell){
			((EditableCell)cell).addEditableCellListener(this);
		}
		repaint();
	}
	
	/**
	*	removeCell from existing graph
	*/
	protected void removeCell(BaseCell cell){
		cells = BaseCellArray.remove(cell, cells);
		graph.remove(cell);
	}
	 
    /*------------------InternalGraphListener----------------*/
    public void rebuild(){};    
    public void cellClicked(InternalGraph g, CellEvent cEvent){
        if(compressed){
            MouseEvent e = cEvent.getMouseEvent();
            if(e.getClickCount() == 2) {
                switchCompressed();
            }
        }
    }   
    public void sizeChanged(InternalGraph g){
        if(compressed && g.equals(graph) ||
		  !compressed && g.equals(cgraph)){
			return;
		}
		
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
		
		repaint();
    }
    
    public void cellRemoved(){} 
    /*--------------END InternalGraphListener ------------------------*/
    
    
    /*--------------- CellListener -------------------------------*/
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
	   		cellListener.cellReleased(cEvent);
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
		repaint();   
    }
    public void cellMove(CellEvent cEvent){
		pack();
		super.translatePos(cEvent.movement.x, cEvent.movement.y);
		repaint();
    }
    
    public void remove(GraphCell cEvent){};
    
    public void repaint(){
    	
		if( graph != null )
			graph.repaint();
		if( cgraph != null )
			cgraph.repaint();
			
		super.repaint();
    }
    
    public void downPack(){
		//whitch wiev do we have
        if(compressed){
            cgraph.pack();
        }else{
            graph.pack();
        }
		
		if(cellListener != null) {
	    	cellListener.downPack();
		}
	}
    
    public void upPack(){
        //whitch wiev do we have
        if(compressed){
            cgraph.pack();
        }else{
            graph.pack();
        }
		
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
    
    //-------------------- MouseListener ---------------------
    public void mouseClicked(MouseEvent e) {
        if(e.getClickCount() == 2) {
            switchCompressed();
		}	
    }
    public void mouseEntered(MouseEvent e) {
    	//System.out.println("Mouse enter " + exp);
    }
    public void mouseExited(MouseEvent e) {
    	//System.out.println("Mouse exit " + exp);
    }
    public void mousePressed(MouseEvent e) {
		super.mousePressed(e);
    }
    public void mouseReleased(MouseEvent e) {
		super.mouseReleased(e);
	}
    /*-------------------- END MouseListener ----------------*/
	
	/*-------------------- EditableCellListener -------------*/
    public void copy(BaseCell cell){}
    public void delete(BaseCell cell){}
	public void paste(BaseCell cell){
		upPack();
		modified();
	}
	public void modified(BaseCell cell){
		if(cell.equals(ct)){
			replace(Converter.createBaseCell(cell.getExp()));
		}
		modified();
	}
	public void replace(BaseCell oldCell, BaseCell newCell){
		cells = BaseCellArray.replace(oldCell, newCell, cells);
		rebuildOpePetricell();
		modified();
	}
	/*-------------------- END EditableCellListener ---------*/   
}
