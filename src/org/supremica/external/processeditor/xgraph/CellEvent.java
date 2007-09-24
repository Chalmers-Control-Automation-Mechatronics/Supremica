package org.supremica.external.processeditor.xgraph;

import java.awt.*;
import java.awt.event.*;

/**
 * An event which indicates that a cell action occured in a graph.
 */
public class CellEvent {  

    public GraphCell source;
    public Point pos;
    public Dimension dim;   
    public Point movement;
    public String comment = null;
    public MouseEvent mEvent = null;
 
    /**
     * Constructs a <code>CellEvent</code> object with 
     * the specified source cell.
     *
     * @param c the source cell
     */
    public CellEvent(GraphCell c) {
	source = c;
	pos = source.getPos();	
	dim = source.getSize();	
	movement = new Point();
    }
    /**
     * Constructs a <code>CellEvent</code> object with
     * the specified source cell and mouse event.
     *
     * @param c the soruce cell
     * @param me the soruce mouse event
     */
    public CellEvent(GraphCell c, MouseEvent me) {
	this(c);
	mEvent = me;
    }
    /**
     * Returns the source mouse event
     *
     * @return the source mouse event
     */
    public MouseEvent getMouseEvent() { 
	return mEvent;	
    }
    /**
     * Sets the source mouse event.
     * 
     * @param me the source mouse event
     */
    public void setMouseEvent(MouseEvent me) {
	mEvent = me;
    }
    /**
     * Returns the source cell.
     * 
     * @return the source cell
     */
    public GraphCell getSource() {
	return source;
    }
    /**
     * Returns the position of the cell event
     * <p>
     * Returns the position of the cell event, which in the constructor is
     * set to the source cell position.
     *
     * @return the location point
     */
    public Point getPos() {
	return pos;
    }
    /**
     * Returns the size of the cell event.
     * <p>
     * Returns the size of the cell event, which in the constructor is 
     * set to the source cell size.
     * 
     * @return the dimension of the cell event size
     */
    public Dimension getSize() {
	return dim;
    }
    /**
     * Translate the position of the cell event. 
     * <p>
     * Translates this cell event position,
     * at position (<i>x</i>, <i>y</i>), by
     * <code>dx</code> along the <i>x</i> axis and <code>dy</code> along the
     * <i>y</i> axis so that the new position will be 
     * (<code>x+dx</code), <code>y+dy</code>). <br>
     * Since the cell event position doesn't directly point at the 
     * source cell position the cell event position can be modified without
     * any change to the cell it self is made.
     * 
     * @param dx the distance to move this cell along the <i>x</i> axis
     * @param dy the distance to move this cell along the <i>y</i> axis
     */
    public void translatePos(int dx, int dy) {
	Point p = (Point)pos.clone();
	p.translate(dx, dy);
	pos = p;	
    }
    /**
     * Resize the size of the cell event.
     * <p>
     * Resize this cell event size,
     * with dimension (<i>x</i>, <i>y</i>), with
     * <code>dx</code> along the <i>x</i> axis and <code>dy</code> along the
     * <i>y</i> axis so that the new size will be
     * (<code>x+dx</code>, <code>y+dy</code>). <br>
     * Since the cell event size doesn't directly point at the 
     * source cell size the cell event size can be modified without
     * any change to the cell it self is made. 
     * 
     * @param dx the distance to resize this cell along the <i>x</i> axis with
     * @param dy the distance to resize this cell along the <i>y</i> axis with
     */
    public void resize(int dx, int dy) {
	dim = new Dimension(dim.width+dx, dim.height+dy);
    }
    /**
     * Sets the displacement of the source cell.
     * 
     * @param dx the displacement along the <i>x</i> axis
     * @param dy the displacement along the <i>y</i> axis
     */
    public void setMovement(int dx, int dy) {
	movement = new Point(dx, dy);
    }
    /**
     * Sets the cell event position.
     * <p>
     * Since the cell event position doesn't directly point at the
     * source cell position the cell event position can be modified without
     * any change to the cell it self is made.
     * 
     * @param p the location point to set
     */
    public void setPos(Point p) {
	pos = p;
    }   
    /**
     * Sets the cell event size.
     * <p>
     * Since the cell event size doesn't directly point at the
     * source cell size the cell event size can be modified without
     * any change to the cell it self is made.
     *
     * @param d the dimension to set
     */
    public void setSize(Dimension d) {
	dim = d;
    }
    /**
     * Returns a comment of this cell event.
     * 
     * @return cell event comment
     */
    public String toString() {
	if(comment != null) {
	    return comment;
	}else {
	    return source.toString();
	}
    }
    /**
     * Sets a comment to this cell event.
     *
     * @param s comment
     */
    public void setString(String s) {
	comment = s;
    }
}
