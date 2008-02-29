package org.supremica.external.processeditor.processgraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.math.*;
import java.util.*;

import org.supremica.external.processeditor.processgraph.opcell.OperationCell;
import org.supremica.external.processeditor.processgraph.opcell.*;
import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.xml.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;

/**
 * Allows a nested behaviour for cell components.
 * <p>
 * The <code>NestedCell</code> class <code>extends</code> the 
 * <code>org.xgraph.GraphCell</code> class and overrides methods to allow
 * a nested behaviour. Nested behaviour means that a cell can both be
 * considered and treated as a cell and/or a graph.
 * This behaviour is achieved by letting 
 * the contents of a cell, i.e. the cell body which is the part
 * seen on the screen, be a nested graph component.
 * The graph component can consecutively enclose other cells 
 * that can be considered as graph. i.e. nested cells.
 * 
 */
public class NestedCell
					extends 
						GraphCell 
					implements 
						MouseListener,
						MouseMotionListener,
						CellListener,
                        NestedCellListener
{     
    public int cellStepX = 0;    
    public int cellStepY = 0;
    public int conditionWidth = 30;
    public int conditionThickness = 2; 

    public JPanel cellItemContainer = new JPanel();
    public NestedGraph cell = new NestedGraph();          
    public NestedGraph cells = new NestedGraph();
    public OperationCell operationCell = null; 
    public NestedCellListener nestedCellListener = null;       
    
    protected Object complexFunction = null;
    public java.util.List list = null;    

    public static boolean multiStructure;
    public boolean multiMode = true;
    public boolean compressed = true;

    private ObjectFactory objectFactory = new ObjectFactory();
   
    /**
     * Creates a new instance of the class
     */
    public NestedCell() {
    }   
    /**
     * Creates a new instance of the class with specified name.
     *
     * @param s the cell name
     */
    public NestedCell(String s) {
	//DEBUG
	//System.out.println("NestedCell(): "+s);
	//END DEBUG					
	try {	    
	    setLayout(null);
	    setFunction(objectFactory.createActivity());	
	    getActivity().setOperation(s);	          
	    build();
	    setCompressed(!compressed);
	    cell.addGraphListener(this);
	    cells.addGraphListener(this);
	    downPack();	       	
	}catch(Exception ex) {}
    }   
    /**
     * Creates a new instance of the class that will be associated with 
     * the specified object.     
     *
     * @param o the object this cell will be associated with
     */
    public NestedCell(Object o) {
    	//DEBUG
    	//System.out.println("NestedCell()");
    	//END DEBUG
    	setLayout(null);
    	if(o instanceof Relation) {		       	    	   
    		try {
    			setFunction(o);	  
     	
    			try {
    				compressed = !getRelation().getAlgebraic().isCompressed();
    			}catch(Exception ex) {}		
	    	
    			build();		
	    	
    		}catch(Exception ex) {
    			if(ex instanceof NullPointerException) {
    				System.out.println("ERROR! in NestedCell() " +
    					"NO OPERATION!");
    				System.out.println(ex);
    			}else {
    				System.out.println("ERROR! while building relation "+
    					"in NestedCell()");
    			}
    		}		    	     		    
	
    	}else if(o instanceof Activity) {		    
    		try {
    			setFunction(o);	       
    			build();
    		}catch(Exception ex) {
    			if(ex instanceof NullPointerException) {
    				System.out.println("ERROR! in NestedCell() "+
    					"EMPTY OPERATION!");
    			}else {
    				System.out.println("ERROR! while building activity "+
    					"in NestedCell()");
    			}	    
    		}
    	}	
    	setCompressed(!compressed);	
    	cell.addGraphListener(this);
    	cells.addGraphListener(this);	
    	downPack();		
    }
    /**
     * This method has intentionally been left empty. 
     */
    public void remove(GraphCell c) {	
    }
    /**
     * Adds the specified nested cell listener to recieve nested cell events
     * from this cell.
     *
     * @param l the nested cell listener
     */
    public void addNestedCellListener(NestedCellListener l) {
    	nestedCellListener = l;
    }    
    /**
     * Creates and returns a copy of this cell.
     *
     * @return the copy
     */
    protected NestedCell clone() {                     
    	return new NestedCell(Converter.clone(getFunction()));
    }    
    /**
     * Returns a copy of this cell.
     * 
     * @return the copy
     */
    public NestedCell copy() {
    	return clone();
    }
    /**
     * Removes this cell from its graph container.
     */
    public void delete() {
    	if(nestedCellListener != null) {
    		nestedCellListener.elementDelete(getFunction());	    
    	}else if(cellListener != null) {
    		cellListener.remove(this);
    	}
    }   
    /**
     * Pastes the object to this cell's grah container.
     *
     * @param o the object to paste
     */
    public void paste(Object o) {
	if(nestedCellListener != null) {
	    if(o instanceof NestedCell) {		
		nestedCellListener.elementAdd(getFunction(), 
					      setUniqueNames(((NestedCell)o).
							     getFunction()));  
	    }
	}
    }
    /**
     * Creates a outer relation around this cell.
     */    
    public void createOuterRelation() {
    	//DEBUG
    	//System.out.println("NestedCell.createOuterRelation()");
    	//END DEBUG
    	try {
    		Relation newRelation = objectFactory.createRelation();
    		newRelation.setType(RelationType.SEQUENCE);
    		newRelation.getActivityRelationGroup().add(getFunction());
    		elementReplace(newRelation);
    	}catch(Exception ex) {
    		;
    	}
    }
    /**
     * If there is any, removes the outer relation around this cell.
     */
    public void removeOuterRelation() {
    	//DEBUG
    	//System.out.println("NestedCell.removeOuterRelation()");	
    	//END DEBUG
    	if(nestedCellListener != null) {
    		nestedCellListener.removeOuterRelation(getFunction());
    	}
    }
    /**
     * If this cell is a outer relation, it is removed and replaced by
     * the specified <code>element</code>.
     *
     * @param element object that will replace this outer relation
     */
    public void removeOuterRelation(Object element) {	
    	if(getActivityRelationGroup() != null &&
    	   getActivityRelationGroup().size() == 1)
    	{
    		elementReplace(element); 
    	}
    }
    /**
     * Packs this nested cell.
     * <p>
     * Packs this nested cell, by calling the <code>this.pack()</code> 
     * method and afterwards recursively go down in the hierarchy by calling
     * the <code>downPack()</code> method to its body which is
     * a nested graph.
     */
    public void downPack() {
    	pack();
    	cells.downPack();
    	cell.downPack();
    }
    /**
     * Packs this nested graph.
     * <p>
     * Packs this nested graph, by calling the <code>this.pack()</code>
     * mehtod and aftwerwards recursively go down in the hierarchy by calling
     * the <code>upPack()</code> method to its cell listener.
     */
    public void upPack() {
    	pack();
    	if(cellListener != null) {
    		cellListener.upPack();
    	}       
    }    
    /**
     * Removes selection from this cell's graph container.
     */
    public void removeSelection() {
    	//DEBUG
    	//System.out.println("NestedCell.removeSelection()");
    	//END DEBUG
    	if(cellListener != null) {
    		cellListener.removeSelection();
    	}
    }
    
    /**
     * Auto scale and positioning this nested cell.
     */
    public void pack() {
 	Point thisCellPos = getPos();
 	NestedGraph nestedCell = null;
 	if(compressed) {	
	    if(cell != null) {		
		nestedCell = cell;		
	    }	    
	}else {
	    if(cells != null) {		
		nestedCell = cells;
	    }
	}
	if(nestedCell != null) {	   	    
	    Point nestedCellPos = nestedCell.getLocation();
	    Dimension nestedCellDim = nestedCell.getSize();	    	
	    thisCellPos.translate(nestedCellPos.x, nestedCellPos.y); 
	    nestedCell.setLocation(0, 0);
	    setSize(nestedCellDim.getSize().width, 
		    nestedCellDim.getSize().height);
	    super.setPos(thisCellPos);   
	}    	
    }        
    /**
     * Sets the list.
     *
     * @param list the concerned list
     */
    public void setList(JList list) {
    	if(nestedCellListener != null) {
    		nestedCellListener.setList(list);
    	}
    }   
    /**
     * Sets the color of the attribute types.
     */
    public void setAttributeTypeColor() {
	//DEBUG
	//System.out.println("NestedCell.setAttributeTypeColor()");
	//END DEBUG
	if(nestedCellListener != null) {
	    nestedCellListener.setAttributeTypeColor();
	}
    }  
    /**
     * Sets unique operation name for the specified object.
     * <p>
     * Sets unique operation name for the specified object if it is of an 
     * <code>instance</code> of the <code>org.xml.rop.Activity</code> class.   
     * 
     * @param o the object to give unique operation name
     * @return the object with unique operation name
     */  
    public Object setUniqueNames(Object o) {
	//DEBUG
	//System.out.println("NestedCell.setUniqueNames()");
	//END DEBUG
	if(nestedCellListener != null) {
	    return nestedCellListener.setUniqueNames(o);
	}else {
	    return Converter.setUniqueNames(getFunction(), o);
	}
    }
    /**
     * Sets this nested cell compressed or decompressed.
     * <p>
     * Compressed means that a nested cell is graphically visualized as 
     * a cell.
     * If the nested cell is not compressed its graphically visualized as
     * a graph.
     *
     * @param set if <code>true</code> the nested cell is compressed, otherwise
     * <code>false</code>
     */
    public void setCompressed(boolean set) {	
	if(set && !compressed) {
	    //DEBUG
	    //System.out.println(this+".OperationCell.setCompressed(): set");
	    //END DEBUG	    
	    remove(cells);	    
	    add(cell);		    	    
	    this.translatePos((cells.getSize().width-cell.getSize().width)/2, 
			      (cells.getSize().height-cell.getSize().height)/2);	    
	    compressed = true;	    	   	    
	}else if(!set && compressed) {
	    //DEBUG
	    //System.out.println(this+".OperationCell.setCompressed(): unset");
	    //END DEBUG
	    remove(cell);
	    add(cells);	   	    
	    this.translatePos(-(cells.getSize().width-cell.getSize().width)/2, 
			      -(cells.getSize().height-cell.getSize().height)/2);	   	    
	    compressed = false;	    	    	    	    
	}	
	setAttributeTypeColor();
     } 
    /**
     * Turns the multi mode view of this cell either on or off.
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
 	//System.out.println("NestedCell.setMultiModeView()");
 	//END DEBUG
 	NestedGraph graph = null;
 	if(set&&!multiMode) {
 	    this.addMouseListener(this);
 	    this.addMouseMotionListener(this);
 	    multiMode = true;	  
 	    graph = cells;	    
 	}else if(!set&&multiMode) {
 	    this.removeMouseListener(this);
 	    this.removeMouseMotionListener(this);
 	    multiMode = false;	    
 	    graph = cells;
 	}
 	if(graph != null) {
 	    graph.setMultiModeView(set);
 	}
    }    
    /**
     * Translates this cell position.
     * <p>
     * Translates this cell position, at position (<i>x</i>, <i>y</i>), by
     * <code>dx</code> along the <i>x</i> axis and <code>dy</code> along the
     * <i>y</i> axis so that the new position will be 
     * (<code>x+dx</code), <code>y+dy</code>).
     *
     * @param dx the distance to move this cell along the <i>x</i> axis
     * @param dy the distnace to move this cell along the <i>y</i> axis
     */     
    public void translatePos(int dx, int dy) {
	//DEBUG
	//System.out.println(this+".NestedCell.translatePos()");
	//END DEBUG
	super.translatePos(dx, dy);
	downPack();
	upPack();
    }          
    /**    
     * Deletes the <code>element</code> object from 
     * this nested cell's complex function.
     *
     * @param element the object to be deleted
     */
    public void elementDelete(Object element) {
	if(getActivityRelationGroup() != null) {
	    getActivityRelationGroup().remove(element);
	}
	rebuild();
    }
    /**    
     * Adds the <code>newElement</code> object into
     * this nested cell's complex function
     * <p>
     * The <code>newElement</code> is inserted next to 
     * the <code>oldElement</code>.
     *     
     * @param oldElement the object where to insert the added object next to
     * @param newElement the object to be added
     */
    public void elementAdd(Object oldElement, Object newElement) {
    	if(getActivityRelationGroup() != null) {
    		int index = getActivityRelationGroup().indexOf(oldElement);
    		getActivityRelationGroup().add(index+1,newElement);
    		rebuild();
    	}
    }
    /**
     * Replaces the <code>oldElement</code> object with 
     * the <code>newElement</code> object in this nested cell's
     * complex function.
     *
     * @param oldElement the object that is to be replaced
     * @param newElement the object that is to replace the old object.
     */
    public void elementReplace(Object oldElement, Object newElement) {
    	//DEBUG
    	//System.out.print("NestedCell.elementReplace(Object old, Object new): ");
    	//END DEBUG
    	if(getActivityRelationGroup() != null) {
    		int index = getActivityRelationGroup().indexOf(oldElement);	    
    		if(index != -1) {
    			getActivityRelationGroup().set(index, newElement);	       
    		}
    	}
    	rebuild();
    }
    /**
     * Replaces this nested cell's complex function with
     * the <code>newElement</code> object.
     *
     * @param newElement the object that is to replace the complex function
     */
    public void elementReplace(Object newElement) {	
	//DEBUG
	//System.out.println("NestedCell.elementReplace(Object new): ");   
	//END DEBUG
	if(nestedCellListener != null) {
	    nestedCellListener.elementReplace(getFunction(), newElement);
	}	
    }
     /**    
     * Pastes the <code>newElement</code> object next to
     * this nested cell's complex function.
     * <p>
     * An outer relation is created for this cell and 
     * the <code>newElement</code> is inserted next to 
     * the this nested cell's complex function.
     *     
     * @param newElement the object to pasted
     */
    public void elementPaste(Object newElement) {
	/**
	try {
	    Object newComplexFunction = objectFactory.createRelation();       
	    if(complexFunction instanceof Activity) {
		((Relation)newComplexFunction).setType("Sequence");
	    }else if(complexFunction instanceof Relation) {
		((Relation)newComplexFunction).setType(((Relation)complexFunction).getType());	    
	    }
	    ((Relation)newComplexFunction).getActivityRelationGroup().add(complexFunction);
	    ((Relation)newComplexFunction).getActivityRelationGroup().add(newElement);	
	    if(nestedCellListener != null) {
		nestedCellListener.elementReplace(complexFunction, newComplexFunction);	    
	    }		
	}catch(Exception ex) {}
	**/
	try {
	    Relation newRelation = objectFactory.createRelation();
	    if(getActivity() != null) {
		newRelation.setType(RelationType.SEQUENCE);
	    }else if(getRelation() != null) {
		newRelation.setType(getRelation().getType());
	    }
	    newRelation.getActivityRelationGroup().add(getFunction());
	    newRelation.getActivityRelationGroup().add(newElement);
	    if(nestedCellListener != null) {
		nestedCellListener.elementReplace(getFunction(), newRelation);
	    }
	}catch(Exception ex) {}
    }            
    /**
     * Rebuilds the contents of this nested cell, based on this nested cell's
     * complex function.
     */
    public void rebuild() {
	//DEBUG
	//System.out.println("NestedCell.rebuild()");
	//END DEBUG	
	if(nestedCellListener != null) {	
	    nestedCellListener.rebuild();
	}else {	   
	    Point tmpPos = getPos();
	    removeAll();
	    cell = new NestedGraph();
	    cells = new NestedGraph();	  	   	    	    
	    if(getRelation() != null) {		
		try {		    		    
		    if(getRelation().getType().equals("Sequence")) {	       
		    }else if(getRelation().getType().equals("Alternative")) {
		    }else if(getRelation().getType().equals("Parallel")) { 
		    }else if(getRelation().getType().equals("Arbitrary")) {    
		    }			    
		    build();
		}catch(Exception ex) {
		    if(ex instanceof NullPointerException) {
			//DEBUG
			//System.out.println("ERROR! in NestedCell.rebuild():"+
			//		   "NO OPERAITION!");
			//END DEBUG
		    }else {
			//DEBUG
			//System.out.println("ERROR! while building relation "+
			//		   "in NestedCell()");
			//END DEBUG
		    }		    
		}	   		    
	    }else if(getActivity() != null) {	    
		try {		    		   
		    build();
		}catch(Exception ex) {
		    if(ex instanceof NullPointerException) {
			//DEBUG
			//System.out.println("ERROR! in NestedCell.rebuild() "+
			//		   "EMPTY OPERATION!");
			//END DEBUG
		    }else {
			//DEBUG
			//System.out.println("ERROR! while building activity "+
			//		   "in NestedCell()");
			//END DEBUG
		    }	    		    
		}
	    }	   	   
	    compressed = !compressed;
	    setCompressed(!compressed);
	    cell.addGraphListener(this);
	    cells.addGraphListener(this);
	    downPack();		
	    setPos(tmpPos);
	    upPack();	       	   
	}
    }
    /**
     * Builds the contents of this nested cell, based on this nested cell's
     * complex function.
     */
    public void build() {
	//DEBUG
	//System.out.println(this+".NestedCell.build()");
	//END DEBUG       	
	    buildCompressedCell();			

	//----- FILL CELLS ------
	if(getFunction() != null) {
	    try {				
	    	if(getRelation() != null) {
	    		if(RelationType.SEQUENCE.equals(getRelation().getType())) {
	    			buildSequence(getActivityRelationGroup().
	    					iterator());
	    			adjusteHorizontalPos();
	    		}else if(RelationType.ALTERNATIVE.equals(getRelation().getType())) {
	    			buildAlternative(getActivityRelationGroup().
	    					iterator());
	    			adjusteVerticalPos();
	    		}else if(RelationType.PARALLEL.equals(getRelation().getType())) {
	    			buildParallel(getActivityRelationGroup().
	    					iterator());
	    			adjusteVerticalPos();
	    		}else if(RelationType.ARBITRARY.equals(getRelation().getType())) {
	    			buildArbitrary(getActivityRelationGroup().
	    					iterator());
	    			adjusteVerticalPos();
	    		}else{
	    			System.err.println("Unknown RelationType: " + getRelation().getType());
	    		}
	    		
	    	}else if(getActivity() != null) {
	    		cells = cell;
	    	}
		
	    }catch(Exception ex) {
	    	if(ex instanceof NullPointerException) {
	    		//DEBUG
	    		//System.out.println("ERROR! in NestedCell.build(): "+
	    		//		       "NO OPERATION!");
	    		//DEBUG
	    	}else {
	    		//DEBUG
	    		//System.out.println("ERROR! while building relation "+
	    		//		       "in NestedCell.build()");
	    		//END DEBUG
	    	}
	    }
	}       		    
    } 
    /**
     * Builds the compressed cell layout. 
     */     
    public void buildCompressedCell() {		
	operationCell = null;	
	if(getFunction() != null) {	    	    	   
	    operationCell = new OperationCell(getFunction());    	       
	}else {	   
	    operationCell = new OperationCell("");	
	}	      
	((OperationCell)operationCell).addNestedCellListener(this);
	cell.insert(operationCell);		
	operationCell.setPos(new Point(cell.marginX, cell.marginY));	
	OperationEdge upperEdge = new OperationEdge(this, 
						    operationCell);	    
	upperEdge.setAnchor(UPPER_CENTER, UPPER_CENTER);
	upperEdge.setSourceAnchorRelativePos(false);
	upperEdge.setMode(OperationEdge.ALTERNATIVE_START);
	cell.insert(upperEdge);	    	    
	OperationEdge lowerEdge = new OperationEdge(operationCell,
						    this);
	lowerEdge.setTargetAnchorRelativePos(false);
	lowerEdge.setAnchor(LOWER_CENTER, LOWER_CENTER);	    
	lowerEdge.setMode(OperationEdge.ALTERNATIVE_END);
	cell.insert(lowerEdge);	    	
    }   
    /**
     * Arranges this nested cell's graph contents to represents 
     * the "Sequence" relation type.
     * <p>
     * If this nested cell's complex function is an <code>instance</code>
     * of the <code>org.xml.rop.Relation</code> class and its relation type 
     * is equal to "Sequence" this method is use.
     *
     * @param func iterates over the graph contents 
     */ 
    protected void buildSequence(Iterator func) {
	try {
	    Object o = func.next();
	    GraphCell newCell = null;
	    if(o instanceof Activity) {
		newCell = new NestedCell((Activity)o);
	    }else if(o instanceof Relation) {			       
		newCell = new NestedCell((Relation)o);		
	    }	
	    if(newCell != null) {
		((NestedCell)newCell).addNestedCellListener(this);	       
		buildSequence(newCell, !func.hasNext());		
	    }
	    if(func.hasNext()) {
		buildSequence(func);		
	    }
	}catch(Exception ex) {
	     if(ex instanceof NullPointerException) {
		 //DEBUG
		 //System.out.println("ERROR! in NestedCell.buildSequence(): "+
		 //		    "NO OPERATIONS!");
		 //END DEBUG
	    }else {
		//DEBUG
		//System.out.println("ERROR! while building relation "+
		//		   "in NestedCell.buildSequence()");
		//END DEBUG
	    }
	}
    }
    /**
     * Adds the <code>newCell</code> to the graph contents of this nested cell
     * which represents the "Sequence" relation type.
     * <p>
     * The cell is added last to the graph contents. 
     * If the <code>newCell</code> is the last cell to be added this has to
     * be specified in order to connect the edges correctly.
     *
     * @param newCell the cell to be added
     * @param lastCell if <code>true</code> it is the last cell to be added, 
     * otherwise <code>false</code>.
     */
    protected void buildSequence(GraphCell newCell, boolean lastCell) {
	if(cells.getCellCount() != 0) {	       		
	    GraphCell cellBefore = cells.getCellAt(cells.getCellCount());
	    cells.insert(newCell);			    
	    newCell.setPos(new Point(cells.marginX,
				     cellBefore.getPos().y+
				     cellBefore.getSize().height+
				     cells.marginY+
				     cellStepY));			       
	    cells.insert(new OperationEdge(cellBefore, newCell));     	    
	}else {
	    cells.insert(newCell);	    
	    newCell.setPos(new Point(cells.marginX, cells.marginY));	    
	    OperationEdge startEdge = new OperationEdge(this, newCell);
	    startEdge.setAnchor(UPPER_CENTER, UPPER_CENTER);
	    startEdge.setSourceAnchorRelativePos(false);
	    startEdge.setMode(OperationEdge.ALTERNATIVE_END);
	    cells.insert(startEdge);
	}	           
	if(lastCell) {	    	
	    OperationEdge endEdge = new OperationEdge(newCell, this);
	    endEdge.setAnchor(LOWER_CENTER, LOWER_CENTER);
	    endEdge.setTargetAnchorRelativePos(false);
	    endEdge.setMode(OperationEdge.ALTERNATIVE_START);
	    cells.insert(endEdge);	    
	}	
    }       
    /**
     * Arranges this nested cell's graph contents to represents
     * the "Alternative" relation type.
     * <p>
     * If this nested cell's complex function is an <code>instance</code>
     * of the <code>org.xml.rop.Relation</code> class and its relation type is
     * equal to "Alternative" this method is use.
     *
     * @param func iterates over the graph contents 
     */ 
    protected void buildAlternative(Iterator func) {
	try {
	    Object o = func.next();
	    GraphCell newCell = null;
	    if(o instanceof Activity) {
		newCell = new NestedCell((Activity)o);
	    }else if(o instanceof Relation) {
		newCell = new NestedCell((Relation)o);
	    }	
	    if(newCell != null) {
		((NestedCell)newCell).addNestedCellListener(this);
		buildAlternative(newCell);
	    }
	    if(func.hasNext()) {
		buildAlternative(func);
	    }
	}catch(Exception ex) {
	     if(ex instanceof NullPointerException) {
		 //DEBUG
		 //System.out.println("ERROR! in NestedCell.buildAlternative(): "+
		 //		   "NO OPERATIONS!");
		 //END DEBUG
	    }else {
		//END DEBUG
		//System.out.println("ERROR! while building relation "+
		//		   "in NestedCell.buildAlternative()");
		//END DEBUG
	    }
	}
    }
    /**
     * Adds the <code>newCell</code> to the graph contents of this nested cell
     * which represents the "Alternative" relation type.
     * <p>
     * The cell is added last to the graph contents.     
     *
     * @param newCell the cell to be added
     */
    protected void buildAlternative(GraphCell newCell) {		
	if(cells.getCellCount() != 0) {	       		
	    GraphCell cellBefore = cells.getCellAt(cells.getCellCount());
	    cells.insert(newCell);
	    newCell.setPos(new Point (cellBefore.getPos().x+
				      cellBefore.getSize().width+
				      cells.marginX+
				      cellStepX,
				      cells.marginY));	 
	}else {
	    cells.insert(newCell);
	    newCell.setPos(new Point(cells.marginX, cells.marginY));
	}	    	 
	OperationEdge upperEdge = new OperationEdge(this, 
						    newCell);	
	upperEdge.setAnchor(UPPER_CENTER, UPPER_CENTER);
	upperEdge.setSourceAnchorRelativePos(false);
	upperEdge.setMode(OperationEdge.ALTERNATIVE_START);
	cells.insert(upperEdge);	    	    
	OperationEdge lowerEdge = new OperationEdge(newCell,
						    this);
	lowerEdge.setTargetAnchorRelativePos(false);
	lowerEdge.setAnchor(LOWER_CENTER, LOWER_CENTER);	    
	lowerEdge.setMode(OperationEdge.ALTERNATIVE_END);
	cells.insert(lowerEdge);	    	   
    }       
    /**
     * Arranges this nested cell's graph contents to represents 
     * the "Parallel" relation type.
     * <p>
     * If this nested cell's complex function is an <code>instance</code>
     * of the <code>org.xml.rop.Relation</code> class and its relation type 
     * is equal to "Parallel" this method is use.
     *
     * @param func iterates over the graph contents 
     */
    protected void buildParallel(Iterator func) {
	try {
	    Object o = func.next();
	    GraphCell newCell = null;
	    if(o instanceof Activity) {
		newCell = new NestedCell((Activity)o);
	    }else if(o instanceof Relation) {
		newCell = new NestedCell((Relation)o);
	    }	
	    if(newCell != null) {
		((NestedCell)newCell).addNestedCellListener(this);
		buildParallel(newCell);
	    }
	    if(func.hasNext()) {
		buildParallel(func);
	    }
	}catch(Exception ex) {
	    if(ex instanceof NullPointerException) {
		//DEBUG
		//System.out.println("ERROR! in NestedCell.buildParallel()" +
		//		   "NO OPERATIONS!");
		//END DEBUG
	    }else {
		//DEBUG
		//System.out.println("ERROR! while building relation "+
		//		   "in NestedCell.buildParallel()");
		//END DEBUG
	    }
	}    
    }
    /**    
     * Adds the <code>newCell</code> to the graph contents of this nested cell
     * which represents the "Parallel" relation type.
     * <p>
     * The cell is added last to the graph contents.         
     *
     * @param newCell the cell to be added
     */
    protected void buildParallel(GraphCell newCell) {	
	if(cells.getCellCount() != 0) {		
	    GraphCell cellBefore = cells.getCellAt(cells.getCellCount());
	    cells.insert(newCell);
	    newCell.setPos(new Point (cellBefore.getPos().x+
				      cellBefore.getSize().width+
				      cells.marginX+
				      cellStepX,
				      cells.marginY));	    	  
	}else {
	    cells.insert(newCell);
	    newCell.setPos(new Point(cells.marginX, cells.marginY));       
	}
	OperationEdge upperEdge = new OperationEdge(this, 
						    newCell);	
	upperEdge.setAnchor(UPPER_CENTER, UPPER_CENTER);
	upperEdge.setSourceAnchorRelativePos(false);
	upperEdge.setMode(OperationEdge.PARALLEL_START);
	cells.insert(upperEdge);	    	    
	OperationEdge lowerEdge = new OperationEdge(newCell,
						    this);
	lowerEdge.setTargetAnchorRelativePos(false);
	lowerEdge.setAnchor(LOWER_CENTER, LOWER_CENTER);	    
	lowerEdge.setMode(OperationEdge.PARALLEL_END);
	cells.insert(lowerEdge);	    	    
    }
     /**
     * Arranges this nested cell's graph contents to represents
     * the "Arbitrary" relation type.
     * <p>
     * If this nested cell's complex function is an <code>instance</code>
     * of the <code>org.xml.rop.Relation</code> class and its relation type is
     * equal to "Arbitrary" this method is use.
     *
     * @param func iterates over the graph contents 
     */ 
    protected void buildArbitrary(Iterator func) {
	//DEBUG
	//System.out.println("NestedCell.buildArbitrary()");
	//END DEBUG
	try {
	    Object o = func.next();
	    GraphCell newCell = null;
	    if(o instanceof Activity) {
		newCell = new NestedCell((Activity)o);
	    }else if(o instanceof Relation) {
		newCell = new NestedCell((Relation)o);
	    }	
	    if(newCell != null) {
		((NestedCell)newCell).addNestedCellListener(this);
		buildArbitrary(newCell);
	    }
	    if(func.hasNext()) {
		buildArbitrary(func);
	    }
	}catch(Exception ex) {
	     if(ex instanceof NullPointerException) {
		 //END DEBUG
		 //System.out.println("ERROR! in NestedCell.buildArbitrary(): "+
		 //		   "NO OPERATIONS!");
		 //END DEBUG
	    }else {
		//DEBUG
		//System.out.println("ERROR! while building relation "+
		//		   "in NestedCell.buildArbitrary()");
		//END DEBUG
	    }
	}
    }
    /**
     * Adds the <code>newCell</code> to the graph contents of this nested cell
     * which represents the "Arbitrary" relation type.
     * <p>
     * The cell is added last to the graph contents.     
     *
     * @param newCell the cell to be added
     */
    protected void buildArbitrary(GraphCell newCell) {		
	if(cells.getCellCount() != 0) {	       		
	    GraphCell cellBefore = cells.getCellAt(cells.getCellCount());
	    cells.insert(newCell);
	    newCell.setPos(new Point (cellBefore.getPos().x+
				      cellBefore.getSize().width+
				      cells.marginX+
				      cellStepX,
				      cells.marginY));	  	   	    
	}else {
	    cells.insert(newCell);
	    newCell.setPos(new Point(cells.marginX, cells.marginY));
	}
       
	OperationEdge upperEdge = new OperationEdge(this, this);
	upperEdge.setSourceAnchorRelativePos(false);
	upperEdge.setTargetAnchorRelativePos(false);	
	upperEdge.setAnchor(GraphCell.UPPER_LEFT, GraphCell.LOWER_RIGHT);
	upperEdge.setMode(OperationEdge.ARBITRARY);
	cells.insert(upperEdge);		
    }
    /**
     * Adjustes the horizontal position of this nested cell's graph contents
     * so it will bee horizontally centred.
     */
    protected void adjusteHorizontalPos() {
	//DEBUG
	//System.out.println("NestedCell.adjusteHorizontalPos()");
	//END DEBUG
	int xPos = 0;						
	for(int i = 1; i <= cells.getCellCount(); i++) {		    
	    xPos = Math.max(cells.getCellAt(i).getSize().width,
			    xPos);
	}			
	for(int i = 1; i <= cells.getCellCount(); i++) {
	    GraphCell tmpCell = cells.getCellAt(i);	        
	    Point tmpCellPos = new Point((xPos-tmpCell.getSize().width)/2, tmpCell.getPos().y);	   
	    tmpCell.setPos(tmpCellPos);		    	      
	} 	
    }
    /**
     * Adjustes the vertical position of this nested cell's graph contents
     * so it will be vertically centred.
     */
    protected void adjusteVerticalPos() {	
	int yPos = 0;
	for(int i = 1; i <= cells.getCellCount(); i++) {	   	    
	    yPos = Math.max(cell.getCellAt(i).getSize().height,
			    yPos);
	}
	for(int i = 1; i <= cells.getCellCount(); i++) {
	    GraphCell tmpCell = cells.getCellAt(i);
	    Point tmpCellPos = new Point(tmpCell.getPos().x, 
					 (yPos-tmpCell.getSize().height)/2);
	    tmpCell.setPos(tmpCellPos);
	}	
    }
    /**
     * Returns all the cells in this nested cell's graph as an array
     * 
     * @return the array including all cells     
     */
    public CellEvent[] getCells() {	
	CellEvent[] mySubcells;
	if(compressed) {
	    mySubcells = cell.getCells();	    
	}else {
	    mySubcells = cells.getCells();
	}
	for(int i = 0; i < mySubcells.length; i++) {
	    mySubcells[i].translatePos(getLocation().x, getLocation().y);
	}
	return mySubcells;
    }
    /**
     * Invoked when the mouse button has been clicked on this cell.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mouseClicked(MouseEvent e) {}
    /**
     * Invoked when the mouse enters this cell.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mouseEntered(MouseEvent e) {}
    /**
     * Invoked when the mouse exits this cell.
     * <p>
     * This method has intentionally left empty and
     * overrides its parent's method.
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Invoked when a mouse button has been pressed on this cell.        
     */
    public void mousePressed(MouseEvent e) {
	//DEBUG
	//System.out.println("NestedCell.mousePressed(): this.getSize(): "+this.getSize());
	//END DEBUG
	super.mousePressed(e);
    }
    /**
     * Invoked when mouse button has been released on this cell.
     * <p>
     * If the mouse button has clicked twice this nested cell will be 
     * compressed or decompressed.
     */
    public void mouseReleased(MouseEvent e) {		
	//DEBUG
	//System.out.println(this+".NestedCell.mouseReleased()");
	//END DEBUG
	if(e.getClickCount() > 1) {	    
	    if(getRelation() != null) {		
		if(compressed) {
		    try {
			getRelation().getAlgebraic().setCompressed(false);
		    }catch(Exception ex) {
			//((Relation)complexFunction).setAlgebraic(new AlgebraicImpl());
			try {
			    getRelation().setAlgebraic(objectFactory.createAlgebraic());		       
			    getRelation().getAlgebraic().setCompressed(false);
			}catch(Exception ex2) {}
		    }
		    removeSelection();
		    rebuild();
		}else {
		    try {
			getRelation().getAlgebraic().setCompressed(true);
		    }catch(Exception ex) {
			try {
			    getRelation().
				setAlgebraic(objectFactory.createAlgebraic());
			    getRelation().
				getAlgebraic().setCompressed(true);
			}catch(Exception ex2) {}			
		    }					    	    	       
		removeSelection();
		rebuild();
		}
	    }else {
		upPack();
		cellListener.cellReleased(new CellEvent(this, e));
	    }	      	    	    
	}else {
	    upPack();
	    cellListener.cellReleased(new CellEvent(this, e));	
	}
    }   
    /**
     * Invoked when a cell is pressed.
     * <p>
     * Forward this event to its cell listener.
     */
    public void cellPressed(CellEvent cEvent) {
	if(cellListener != null) {
	    cEvent.translatePos(getLocation().x, getLocation().y);
	    cellListener.cellPressed(cEvent);
	}
    }
    /**
     * Invoked when a cell is released.
     * <p>
     * Forward this event to its cell listener.
     */
    public void cellReleased(CellEvent cEvent) {
	//DEBUG
	//System.out.println(this+".NestedCell.cellReleased()");
	//END DEBUG	
	if(cellListener != null) {
	    cEvent.translatePos(getLocation().x, getLocation().y);   	    
	    cellListener.cellReleased(cEvent);
	}
    }
    /**
     * Invoked when a cell is dragged.
     * <p>
     * Forward this event to its cell listener.
     */
    public void cellDragged(CellEvent cEvent) {
	if(cellListener != null) {
	    cEvent.translatePos(getLocation().x, getLocation().y);
	    cellListener.cellDragged(cEvent);
	}
    }
    /**
     * Invoked when a cell is moved.
     * <p>
     * Forward this event to its cell listener.
     */
    public void cellMove(CellEvent cEvent) {
	//DEBUG
	//System.out.println("NestedCell.cellMove()");
	//END DEBUG
	super.translatePos(cEvent.movement.x, cEvent.movement.y);
	upPack();
    }
    /**
     * Returns the complex function of this nested cell.
     * <p>
     * The complex function is an xml-object which this
     * nested cell represents.
     *
     * @return the complex function
     */
    public Object getFunction() {
    	return complexFunction;
    }
    /**
     * Sets the complex function of this nested cell.
     * <p>
     * The complex function is an xml-object which this
     * nested cell represents.
     *
     * @param o the new complex function
     */
    public void setFunction(Object o) {
	complexFunction = o;
    }
    /**
     * Returns the complex function of this nested cell as an instance of 
     * the <code>ROP</code> class. 
     * <p>
     * If the complex function of this nested cell
     * is not an instance of the <code>ROP</code> class this 
     * method will return <code>null</code>. 
     * 
     * @return the <code>ROP</code> instance
     */
    public ROP getROP() {	
	if(getFunction() instanceof ROP) {
	    return (ROP)getFunction();
	}else {
	    return null;
	}
    }
    /**
     * Returns the complex function of this nested cell as an instance of 
     * the <code>Relation</code> class.
     * <p>
     * If the complex function of this nested cell
     * is not an instance of the <code>Relation</code> class this 
     * method will return <code>null</code>.
     *
     * @return the relation
     */
    public Relation getRelation() {
	if(getFunction() instanceof Relation) {
	    return (Relation)getFunction();
	}else {
	    return null;
	}
    }
    /**
     * Returns the complex function of this nested cell as an instance of 
     * the <code>Activity</code> class.
     * <p>
     * If the complex function of this nested cell
     * is not an instance of the <code>Activity</code> class this
     * method will return <code>null</code>.
     * 
     * @return the activity
     */
    public Activity getActivity() {
	if(getFunction() instanceof Activity) {
	    return (Activity)getFunction();
	}else {
	    return null;
	}
    }
    /**
     * Returns the activity/relation group.
     * <p>
     * Returns the activity/relations group of this nested cell if the 
     * complex function is an instance of the <code>Relation</code> or 
     * <code>ROP</code> class,
     * otherwise <code>null</code>
     *
     * @return the activity/relation group
     */
    public java.util.List getActivityRelationGroup() {
	if(getRelation() != null) {
	    return getRelation().getActivityRelationGroup();
	}else if(getROP() != null) {
	    return getROP().getRelation().getActivityRelationGroup();
	}else {
	    return null;
	}
    }
}
