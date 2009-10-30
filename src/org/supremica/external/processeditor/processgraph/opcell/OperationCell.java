package org.supremica.external.processeditor.processgraph.opcell;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;

import org.supremica.external.processeditor.processgraph.*;
import org.supremica.external.processeditor.xgraph.*;
import org.supremica.external.processeditor.xml.*;
import org.supremica.manufacturingTables.xsd.processeditor.*;
import org.supremica.external.processeditor.processgraph.opcell.AttributeListener;
import org.supremica.external.processeditor.processgraph.opcell.OperationCellInfoWindow;
import org.supremica.external.processeditor.processgraph.opcell.OperationCellItem;
/**
 * Graphically representation of objects of the 
 * <code>org.xml.rop.Activity</code> class, which includes 
 * operation and predecessors.
 */
public class OperationCell 
						extends 
							GraphCell 
						implements 
							NestedCellListener,
                            AttributeListener
{
    private static final long serialVersionUID = 1L;

    public JPanel predecessorBar = new JPanel();
    public int predecessorSpaceY = 1;
    public int predecessorSpaceX = 0;
    public static int predecessorLinePosY = 0;
    public static int predecessorLineWidth = 30;
    public static int predecessorLineHeight = 6;
    public OperationCellItem body = null;

    public String[] uniqueAttributes = new String[0];
    public Color[] uniqueAttributesColor = new Color[0];    

    private NestedCellListener nestedCellListener = null;

    private Object complexFunction = null;
    
    /**
     * Creates a new instance of the class.
     */
    public OperationCell(String s) {	
    	super(s);
	
    	JLabel tag = new JLabel(s);
    	setLayout(null);
    	tag.setBounds(0, 0,tag.getPreferredSize().width, tag.getPreferredSize().height);
    	add(tag);
    }  
    /**
     * Creates a new instance of the class that will be associated with the 
     * specified object.
     * 
     * @param o the object this cell will be associated with
     */      
    public OperationCell(Object o) {
    	super("");			
    	//DEBUG
    	//System.out.println("OperationCell(Object o)");		
    	//END DEBUG      	
    	complexFunction = o;	
    	setBackground(new Color(0,0,0,0));	
    	setLayout(null);		       	
    	body = new OperationCellItem(o,this);		
    	add(body);				
    	predecessorBar.setBackground(Color.gray);
    	predecessorBar.setBorder(new BevelBorder(BevelBorder.RAISED));	
    	add(predecessorBar);
    	attributeChanged();

    	this.removeMouseListener(this);
    	this.removeMouseMotionListener(this);
    	body.addMouseListener(this);
    	body.addMouseMotionListener(this);
		
    }
    /**
     * Recalculates the size of this cell needed to visualize its contents.   
     */
    public void attributeChanged() {
	//DEBUG
	//System.out.println("OperationCell.attributeChanged()");	
	//END DEBUG	       
	boolean hasPredecessor = false;
	try {		   
	    if(complexFunction instanceof Activity) {	    	    
		
		try {
		    Iterator predIterator = ((Activity)complexFunction).
			getPrecondition().getPredecessor().iterator();
		    int sumHeight = 0;
		    if(predIterator.hasNext()) {
			hasPredecessor = true;
		    }
		    while(predIterator.hasNext()) {
			Object tmp = predIterator.next();
			if(tmp instanceof OperationReferenceType) {	 
			    String predecessor = ((OperationReferenceType)tmp).getOperation()+"@"+((OperationReferenceType)tmp).getMachine();		       
			    sumHeight += getFontMetrics(getFont()).getHeight();
			    predecessorSpaceX = Math.max(predecessorSpaceX, 
							 getFontMetrics(getFont()).stringWidth(predecessor));			
			}
		    }
		    predecessorSpaceY = Math.max(sumHeight,predecessorSpaceY);
		}catch(Exception ex) {
		    //DEBUG
		    //System.out.println("ERROR! while setting predecessor in OperationCell.attributeChanged()");
		    //END DEBUG
		}			
	    }if(complexFunction instanceof Relation) {	       	
		try {
		    String[] pred = Converter.getPredecessors(complexFunction);
		    int sumHeight = 0;
		    if(pred.length > 0) {
			hasPredecessor = true;
		    }
		    for(int i = 0; i < pred.length; i++) {		       
			sumHeight += getFontMetrics(getFont()).getHeight();
			predecessorSpaceX = Math.max(predecessorSpaceX, 
						     getFontMetrics(getFont()).stringWidth(pred[i]));
		    }		    
		    predecessorSpaceY = Math.max(sumHeight, predecessorSpaceY);
		}catch(Exception ex) {
		    //DEBUG
		    //System.out.println("ERROR! while drawing predecessors Parallel Relation");
		    //END DEBUG
		    
		}				
	    } 	 		    	  
	    setSize(Math.max(body.getSize().width,predecessorSpaceX*2+predecessorLineWidth), predecessorLinePosY+predecessorSpaceY+body.getSize().height);
	    body.setLocation((getSize().width-body.getSize().width)/2,predecessorLinePosY+predecessorSpaceY);	    	   	    	    
	}catch(Exception ex) {}	   	
       
	if(!hasPredecessor) {
	    remove(predecessorBar);
	}else {
	    Point upperCenter = super.getPos(GraphCell.UPPER_CENTER);       
	    upperCenter.translate(-NestedGraph.marginX, -NestedGraph.marginY);
	    //-------- Handle first time constructor situation ----------
	    if(upperCenter.y < 0) {
		upperCenter.translate(NestedGraph.marginX, NestedGraph.marginY);
	    }	
	    //------------------------end-------------------------------	
	    predecessorBar.setBounds(upperCenter.x-predecessorLineWidth/2,
				     upperCenter.y+predecessorLinePosY,
				     predecessorLineWidth,
				     predecessorLineHeight); 	
	}	       
	upPack();
    }
    /**
     * Removes selection from this cell's graph container.
     */
    public void removeSelection() {
	//DEBUG
	//System.out.println("OperationCell.removeSelection()");
	//END DEBUG
	if(cellListener != null) {
	    cellListener.removeSelection();
	}
    }
    /**
     * Packs this cell's graph container by repainting this cell 
     * and afterwards call the <code>uPack()</code> method
     * to its cell listener.
     */
    public void upPack() {
	//DEBUG
	//System.out.println("OperationCell.upPack()");
	//END DEBUG
	super.repaint();
	if(cellListener != null) {
	    cellListener.upPack();
	}
    }
    /**
     * This method has intentionally left empty.
     */
    public void downPack() {	
    }    
    /**
     * Sets the color for the attribute types of this cell.
     * 
     * @param uAtt the unique attribute types
     * @param uAttC the attribute type's corresponding color
     */
    public void setAttributeTypeColor(String[] uAtt, 
				    Color[] uAttC) {
	uniqueAttributes = uAtt;
	uniqueAttributesColor = uAttC;       
	body.setAttributeTypeColor(uAtt, uAttC);
    }
    /**
     * Sets the attribute type color.
     */
    public void setAttributeTypeColor() {
	if(nestedCellListener != null) {
	    nestedCellListener.setAttributeTypeColor();
	}else {
	    uniqueAttributes = new String[0];
	    uniqueAttributesColor = new Color[0];	    
	}	
    }
    /**
     * Sets attributes of the specified type 
     * visible or invisible of this cell.
     *
     * @param type specifies concerned attribute type
     * @param visible if <code>true</code> the attribute is set visible, 
     * otherwise <code>false</code>
     */
    public void setAttributeTypeVisible(String type, boolean visible) {
	//DEBUG
	//System.out.println("OperationCell.setAttributeTypeVisible()");    
	//END DEBUG
	body.setAttributeTypeVisible(type, visible);
    }
    /**
     * Sets unique operation name for this cell.
     *
     * @param o the object associated with this cell
     * @return o the object with unique operation name
     */
    public Object setUniqueNames(Object o) {
	//DEBUG
	//System.out.println("OperationCell.setUniqueNames()");
	//END DEBUG
	if(nestedCellListener != null) {
	    return nestedCellListener.setUniqueNames(o);
	}else {
	    return Converter.setUniqueNames(complexFunction, o);
	}
    }
    /**
     * Paints this cell.
     *
     * @param g the graphic context
     */
    public void paintComponent(Graphics g) {	
	//DEBUG
	//System.out.println("OperationCell.paintComponent()");
	//END DEBUG		
	Dimension tmpBodySize = body.getSize();
	body.update();	
	if(!body.getSize().equals(tmpBodySize)) {	       
	    attributeChanged();
	}		
	Point upperCenter = super.getPos(GraphCell.UPPER_CENTER);       
	upperCenter = super.getPos(GraphCell.UPPER_CENTER);
	upperCenter.translate(-NestedGraph.marginX, -NestedGraph.marginY);     
	EdgePainter.drawVerticalLine(g, 
				     upperCenter.x,
				     upperCenter.y,
				     upperCenter.y+predecessorLinePosY+
				     +predecessorSpaceY);   
      	
	if(complexFunction instanceof Activity) {	    	    	   
	    try {
		Iterator predIterator = ((Activity)complexFunction).getPrecondition().getPredecessor().iterator();
		int sumHeight = 0;
		while(predIterator.hasNext()) {
		    Object tmp = predIterator.next();
		    if(tmp instanceof OperationReferenceType) {		       
			String predecessor = ((OperationReferenceType)tmp).getOperation()+"@"+((OperationReferenceType)tmp).getMachine();		    
			sumHeight += g.getFontMetrics().getHeight();
			g.drawString(predecessor,
				     upperCenter.x+predecessorLineWidth/2,
				     upperCenter.y+predecessorLinePosY+
				     -NestedGraph.marginX/2+sumHeight);	       
		    }
		}	   
	    }catch(Exception ex) {}	   
	}else if(complexFunction instanceof Relation) {	    	   
	    try {
		String[] pred = Converter.getPredecessors(complexFunction);
		int sumHeight = 0;
		for(int i = 0; i < pred.length; i++) {		    
		    sumHeight += g.getFontMetrics().getHeight();
		    if(pred[i].endsWith(":")) {
			g.setFont(g.getFont().deriveFont(Font.ITALIC));
		    }else {
			g.setFont(g.getFont().deriveFont(Font.PLAIN));
		    }
		    g.drawString(pred[i],
				 upperCenter.x+predecessorLineWidth/2,
				 upperCenter.y+predecessorLinePosY+
				 -NestedGraph.marginX/2+sumHeight);
		}
	    }catch(Exception ex) {}	    	    
	}	
    }         
    /**
     * Adds the specified nested cell listener to recieve cell events from
     * this cell.
     * <p>
     * If listener <code>l</code> is <code>null</code> previous cell listener
     * will be removed.
     *
     * @param l the nested cell listener
     */
    public void addNestedCellListener(NestedCellListener l) {
	nestedCellListener = l;
    }
    /**
     * Returns a copy of this cell.
     *
     * @return the copy
     */
    public NestedCell copy() {
	if(nestedCellListener != null) {
	    return nestedCellListener.copy();
	}else {
	    return null;
	}
    }
    /**
     * Removes this cell form its graph container.
     */
    public void delete() {
	if(nestedCellListener != null) {
	    nestedCellListener.delete();
	}
    }
    /**
     * Pastes the object to this cell's graph container.
     */
    public void paste(Object o) {
	if(nestedCellListener != null) {
	    if(o instanceof NestedCell) {
		nestedCellListener.
		    elementPaste(setUniqueNames(((NestedCell)o).
						getFunction()));
	    }
	}
    }
    /**
     * Creates a outer relation around this cell.
     */
    public void createOuterRelation() {
	if(nestedCellListener != null) {
	    nestedCellListener.createOuterRelation();
	}
    }
    /**
     * If there is any, removes the outer relation around this cell.
     */
    public void removeOuterRelation() {
	if(nestedCellListener != null) {
	    nestedCellListener.removeOuterRelation();
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
     * Rebuilds the contents of this cell's graph container.
     */
    public void rebuild() {
	//DEBUG
	//System.out.println("OperationCell.rebuild()");
	//END DEBUG
	if(nestedCellListener != null) {
	    nestedCellListener.rebuild();
	}
    }        
    /**
     * This method has inentationally been left empty.
     */
    public void removeOuterRelation(Object element) {}
    /**
     * This method has inentationally been left empty.
     */
    public void elementDelete(Object oldElement) {}
    /**
     * This method has inentationally been left empty.
     */
    public void elementAdd(Object oldElement, Object newElement) {}
    /**
     * This method has inentationally been left empty.
     */
    public void elementReplace(Object oldElement, Object newElement) {}
    /**
     * This method has inentationally been left empty.
     */
    public void elementReplace(Object newElement) {}
    /**
     * This method has inentationally been left empty.
     */
    public void elementPaste(Object newElement) {}            
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
	//System.out.println("OperationCell.translatePos()");
	//END DEBG		
	if(cellListener != null) {
	    CellEvent cEvent = new CellEvent(this);
	    cEvent.setMovement(dx, dy);
	    cellListener.cellMove(cEvent);
	    
	}	
    } 
    /**
     * Returns this cell.
     * 
     * @return an array including this cell
     */    
    public CellEvent[] getCells() {
	CellEvent[] cell = new CellEvent[1];
	cell[0] = new CellEvent(this);
	Point bodyPos = body.getLocation();
	bodyPos.translate(NestedGraph.marginX, NestedGraph.marginY);
	cell[0].setPos(bodyPos);
	cell[0].setSize(body.getSize());
	return cell;
    }    
    /**
     * Invoked when a mouse button has been pressed on this cell.      
     */
    public void mousePressed(MouseEvent e) {
	//DEBUG
	//System.out.println("OperationCell.mousePressed()");
	//END DEBUG
	CellEvent cEvent = new CellEvent(this, e);
	Point bodyPos = body.getLocation();
	bodyPos.translate(NestedGraph.marginX, NestedGraph.marginY);
	cEvent.setPos(bodyPos);
	cEvent.setSize(body.getSize());
	cellListener.cellPressed(cEvent);
	mousePressedPoint = new Point(e.getX()+NestedGraph.marginX*0, 
				      e.getY()+NestedGraph.marginY*0);	
    }
    /**
     * Invoked when a mouse button has been released on this cell.
     */
    public void mouseReleased(MouseEvent e) {
	//DEBUG
	//System.out.println("OperationCell.mouseReleased()");
	//END DEBUG
	CellEvent cEvent = new CellEvent(this, e);
	Point bodyPos = body.getLocation();
	bodyPos.translate(NestedGraph.marginX, NestedGraph.marginY);
	cEvent.setPos(bodyPos);
	cEvent.setSize(body.getSize());
	cellListener.cellReleased(cEvent);	
	if(e.getClickCount() > 1) {	    
	    if(complexFunction instanceof Activity) {
		operationInfo();
	    }
	}
    }    
    /**
     * Displays the operation info window, which allow the user to edit
     * the operation, predecessor and attribute information. 
     */
    public void operationInfo() {
	OperationCellInfoWindow operationInfo = new OperationCellInfoWindow((Activity)complexFunction, this);	       
	int result = operationInfo.showDialog();
	removeSelection();
	if(result == OperationCellInfoWindow.APPROVE_OPTION) {	    
	    rebuild();		   
	}else if(result == OperationCellInfoWindow.CANCEL_OPTION) {    
	}else if(result == OperationCellInfoWindow.DELETE_OPTION) {   
	    delete();
	}else if(result == OperationCellInfoWindow.ERROR_OPTION) {}
    }
    /**
     * Invoked when a mouse button is pressed on this cell and then dragged.
     * <p>
     * Changes the cell position related to the mouse dragg motion.
     */
    public void mouseDragged(MouseEvent e) {
	Point pos = getPos();
	refPoint = new Point(e.getX()-mousePressedPoint.x,
			     e.getY()-mousePressedPoint.y);
	pos.translate(refPoint.x, refPoint.y);
	setPos(pos);
	CellEvent cEvent = new CellEvent(this, e);       
	Point bodyPos = body.getLocation();
	bodyPos.translate(NestedGraph.marginX, NestedGraph.marginY);
	cEvent.setPos(bodyPos);       
	cellListener.cellDragged(cEvent);
    }            
}
