package org.supremica.external.processeditor.processgraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import org.supremica.external.processeditor.processgraph.opcell.*;
import org.supremica.external.processeditor.xgraph.*;

/**
 * Allows a nested behaviour for graph components.
 * <p>
 * The <code>NestedGraph</code> class <code>extends</code> the 
 * <code>org.xgraph.Graph</code> class and overrides methods to allow
 * a nested behaviour. Nested behaviour means that a cell can both be 
 * considered and treated as a cell and/or a graph.
 * This behaviour is achieved by letting
 * the contents of a cell, i.e. the cell body which is the part 
 * seen on the screen, be a nested graph component.
 * The graph component can consecutively enclose other cells
 * that can be considered as graphs, i.e. nested cells.
 */
public class NestedGraph extends Graph implements MouseListener,
						  MouseMotionListener,
						  KeyListener,
                                                  CellListener {
    
    public CellListener graphListener; 
    public static int marginX = 10;
    public static int marginY = 10;
    public static Color backgroundColor = new Color(0,0,100,20);   
    private boolean multiMode;    
   
    /**
     * Creates a new instance of the class.
     */
    public NestedGraph() {
	//DEBUG
	//System.out.println("NestedGraph()");
	//END DEBUG
	multiMode = true;
	setBackground(backgroundColor);				     
	this.removeMouseListener(this);
	this.removeMouseMotionListener(this);
	removeKeyListener(this);
	selection = null;
	setFocusable(false);		
	pack();
    }
    /**
     * Adds a cell to this graph.
     * 
     * @param c the cell to be added
     */
    public void insert(GraphCell c) {
	//DEBUG
	//System.out.println("NestedGraph.insert()");       
	//END DEBUG
	super.insert(c);       
	upPack();       
    }                       
    /**
     * Packs this nested graph.
     * <p>
     * Packs this nested graph, by calling the <code>this.pack()</code>
     * mehtod and afterwards recursively go up in the hierarchy by calling 
     * the <code>upPack()</code> method to its graph listener.
     */
    public void upPack() {
	pack();
	if(graphListener != null) {
	    graphListener.upPack();
	}
    }
    /**
     * Packs this nested graph.
     * <p>
     * Packs this nested graph, by calling the <code>this.pack()</code>
     * method and aftewards recursively go down in the hierarchy by calling the
     * <code>downPack()</code> method to all the nested cells added to 
     * this graph.
     */
    public void downPack() {
	pack();
	if(cells != null) {
	    for(int i = 0; i < cells.length; i++) {
		if(cells[i] instanceof NestedCell) {
		    ((NestedCell)cells[i]).downPack();
		}
	    }
	}
    }
    /**
     * Removes the selection from this graph.
     */
    public void removeSelection() {
	//DEBUG
	//System.out.println("NestedGraph.removeSelection()");
	//END DEBUG
	if(graphListener != null) {
	    graphListener.removeSelection();
	}
    }
    /**
     * Auto scale and positioning this nested graph.
     */
    public void pack() {	
	if(cells != null && cells.length > 0) {		    
	    int north = cells[0].getPos().y; 
	    int east = cells[0].getPos().x; 
	    int south = cells[0].getPos().y; 
	    int west = cells[0].getPos().x;
	    for(int i = 0; i < cells.length; i++) {
		if(cells[i].getPos().y < north) {
		    north = cells[i].getPos().y;
		}
		if((cells[i].getPos().x+cells[i].getSize().width) > east) {
		    east = cells[i].getPos().x+cells[i].getSize().width;
		}
		if((cells[i].getPos().y+cells[i].getSize().height) > south) {
		    south = cells[i].getPos().y+cells[i].getSize().height;
		}
		if(cells[i].getPos().x < west) {
		    west = cells[i].getPos().x;
		}
	    }
	    if((north !=0)||(west != 0)) {
		Point graphPos = getLocation();
		graphPos.translate(west-marginX, north-marginY);
		setLocation(graphPos);
		for(int i = 0; i < cells.length; i++) {
		    Point pos = cells[i].getPos();
		    pos.translate(-west+marginX, -north+marginY);
		    cells[i].setPos(pos);
		}
	    }
	    setSize(east-west+marginX*2, south-north+marginY*2);	    
	}else {
	    setSize(0,0);
	}	
    }
    /**
     * Repaints this graph.
     */
    public void repaint() {
	//DEBUG
	//System.out.println(name+".NestedGraph.repaint()");
	//END DEBUG
	super.repaint();
    }           
    /**
     * Turns the multi mode view of this graph either on or off. 
     * <p>
     * If the graphical representation of the relations, i.e. the nested cells
     * enclosing one or more operation and/or other relation 
     * components, is unwanted it can be removed by turning off the multi mode.
     *
     * @param set <code>true</code> sets the multi mode on, 
     * otherwise <code>false</code>     
     */
    public void setMultiModeView(boolean set) {
	//DEBUG
	//System.out.println("NestedGraph.setMultiModeView()");
	//END DEBUG
	GraphCell[] subCells = new GraphCell[0];
	if(set&&!multiMode) {
	    setBackground(backgroundColor);	    
	    multiMode = true;	    
	    subCells = super.cells;
	}else if(!set&&multiMode) {
	    multiMode = false;
	    setBackground(new Color(0, 0, 0, 0));
	    subCells = super.cells;
	}
	for(int i = 0; i < cells.length; i++) {
	    if(subCells[i] instanceof NestedCell) {
		((NestedCell)subCells[i]).setMultiModeView(set);
	    }
	}
    }      
    /**
     * Adds the specified graph listener to recieve cell events from 
     * this graph.
     *
     * @param l the graph listener
     */
    public void addGraphListener(CellListener l) {
	graphListener = l;
    }
    /**
     * Invoked when the mouse button has been clicked on this graph.
     * <p>
     * This method has intentionally left emtpy and
     * overrides its parent's method.    
     */
    public void mouseClicked(MouseEvent e) {       
    }
    /**
     * Invoked when the mouse enters this graph.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * Invoked when the mouse exits this graph.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mouseExited(MouseEvent e) {
    }
    /**
     * Invoked when a mouse button has been pressed on this graph.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mousePressed(MouseEvent e) {	       
    }
    /**
     * Invoked when a mouse button has been pressed on this graph.
     * <p>
     * This method has intentionally been left empty and
     * overrides its parent's method.
     */
    public void mouseReleased(MouseEvent e) {       
    }        
    /**
     * Invoked when a cell is pressed.
     * <p>
     * Forward this event to its graph listener.
     */
    public void cellPressed(CellEvent cEvent) {
	//DEBUG
	//System.out.println("NestedGraph.cellPressed()");
	//END	
	if(graphListener != null) {	    
	    graphListener.cellPressed(cEvent);
	}	
    }
    /**
     * Invoked when a cell is released.
     * <p>
     * Forward this event to its graph listener.
     */
    public void cellReleased(CellEvent cEvent) {
	//DEBUG
	//System.out.println("NestedGraph.cellReleased()");
	//END DEBUG
	if(graphListener != null) {	    	    	    
	    graphListener.cellReleased(cEvent);
	}
    }       
    /**
     * Invoked when a cell is dragged.
     * <p>
     * Forward this event to its graph listener and then 
     * repack this nested graph.
     */
    public void cellDragged(CellEvent cEvent) {
	//DEBUG
	//System.out.println("NestedGraph.cellDragged()");
	//END DEBUG       	
	if(graphListener != null) {	    
	    graphListener.cellDragged(cEvent);
	}		
	upPack();		
    }
    /**
     * Ivoked when a cell is moved.
     * <p>
     * Forward this event to its graph listener.
     */
    public void cellMove(CellEvent cEvent) {
	if(graphListener != null) {
	    graphListener.cellMove(cEvent);
	}
    }
}
