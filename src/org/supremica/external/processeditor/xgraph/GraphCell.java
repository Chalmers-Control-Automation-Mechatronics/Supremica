package org.supremica.external.processeditor.xgraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Represents the cells that can added and manipulated to the graphs.
 */
public class GraphCell extends JPanel implements MouseListener,
						 MouseMotionListener {
    protected int sizeX = 50;
    protected int sizeY = 20; 
    protected int locX = 0;
    protected int locY = 0;
    protected Point mousePressedPoint = new Point();
    public Point refPoint = new Point();
    public String name;    
    public static final int CENTER = 0;
    public static final int UPPER_LEFT = 1;
    public static final int UPPER_CENTER = 2;
    public static final int UPPER_RIGHT = 3;
    public static final int RIGHT_CENTER = 4;
    public static final int LOWER_RIGHT = 5;
    public static final int LOWER_CENTER = 6;
    public static final int LOWER_LEFT = 7;
    public static final int LEFT_CENTER = 8;
    protected CellListener cellListener;   

    /**
     * Creates a new instance of the class.
     */ 
    public GraphCell() {	
	//DEBUG
	//System.out.println("GraphCell()");
	//END DEBUG
	name = "";	
	setBackground(Color.ORANGE);	

	this.addMouseListener(this);
	this.addMouseMotionListener(this);	
    }       
    /**
     * Creates a new instance of the class with specified name.
     * <p>
     * The name is labelled on the cell.
     *
     * @param s the cell name
     */
    public GraphCell(String s) {
	this();
	name = s;	
    }  
    /**
     * Paints this cell.
     *
     * @param g the graphic context
     */
    public void paintComponent(Graphics g) {	       
	Graphics2D g2 = (Graphics2D) g;
	g2.setColor(new Color(0,0,100,20));
	g2.setPaint(new GradientPaint(0, 0, new Color(255, 255, 255), getSize().width, 0, new Color(230, 220, 250)));		
    } 
    /**
     * Returns the position of this cell.
     * <p>
     * The position is by default related to this cell upper left corner.
     * 
     * @return the location point of this cell
     */    
    public Point getPos() {
	//DEBUG
	//System.out.println("GraphCell.getPos(): "+getLocation());
	//END 
	return getLocation();
    }
    /**
     * Sets the position of this cell.
     * 
     * @param p the location point to set
     */
    public void setPos(Point p) {	
	//DEBUG
	//System.out.println("GraphCell.setPos("+p+")");
	//END DEBUG
	locX = p.x; locY = p.y;
	setBounds(locX, locY, sizeX, sizeY);	
    }
    
    
    /**
     * Sets a border on the cell to mark the cell as selected.
     * 
     * @param selsected boolean variable
     */
    public void setSelected(boolean selected){
    	//This function was added by David Millares
		if(selected){
			setBorder(BorderFactory.createLineBorder(Color.black));
		}else{
			setBorder(BorderFactory.createEmptyBorder());
		}
	}
    
    /**
     * Translates this cell position
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
	//System.out.println(this+".GraphCell.translatePos()");
	//END DEBUG
	Point pos = getPos();
	pos.translate(dx, dy);
	setPos(pos);
    }    
    /**
     * Resize this cell so that it has width <code>x</code> 
     * and height <code>y</code>.
     */
    public void setSize(int x, int y) {
	sizeX = x; sizeY = y;
	super.setSize(x, y);
    }
    /**
     * Adds the specified cell listener to recieve cell events from this cell.
     * <p>
     * If listener <code>cl</code> is <code>null</code> previous cell listener
     * will be removed.
     *
     * @param cl the cell listener
     */
    public void addCellListener(CellListener cl) {
	//DEBUG
	//System.out.println("GraphCell.addCellListener()");
	//END DEBUG
	cellListener = cl;
    }    
    /**
     * Returns the absolute position of this cell.
     * <p>
     * The index <code>i</code> specifies the choice of anchor position. 
     * It is recommended to use one of the following predefined finals:
     * <code>CENTER</code>
     * , <code>UPPER_LEFT</code>, <code>UPPER_CENTER</code>
     * , <code>UPPER_RIGHT</code>, <code>RIGHT_CENTER</code>
     * , <code>LOWER_RIGHT</code>, <code>LOWER_CENTER</code>
     * , <code>LOWER_LEFT</code> or <code>LEFT_CENTER</code>.
     *
     * @param i the anchor position
     * @return the absolute position
     */
    public Point getPos(int i) {
	Point move = getAnchorPos(i);
	Point pos = getPos();
	pos.translate(move.x, move.y);
	return pos;
    }
      /**
     * Returns the relative position of this cell.
     * <p>
     * The index <code>i</code> indicate choice of anchor position. 
     * It is recommended to use one of following the predefine finals 
     * <code>CENTER</code>
     * , <code>UPPER_LEFT</code>, <code>UPPER_CENTER</code>
     * , <code>UPPER_RIGHT</code>, <code>RIGHT_CENTER</code>
     * , <code>LOWER_RIGHT</code>, <code>LOWER_CENTER</code>
     * , <code>LOWER_LEFT</code>, <code>LEFT_CENTER</code> 
     * to specify anchor position. 
     *
     * @param i the anchor position
     * @return the relative position
     */
    public Point getAnchorPos(int i) {	
	int moveX = 0;
	int moveY = 0;
	switch(i) {
	case CENTER: 	    
	    moveX = sizeX/2;
	    moveY = sizeY/2;
	    break;
	case UPPER_LEFT: 	    
	    break;
	case UPPER_CENTER: 	    
	    moveX = sizeX/2;
	    break;
	case UPPER_RIGHT:	    
	    moveX = sizeX;
	    break;
	case RIGHT_CENTER:	    
	    moveX = sizeX;
	    moveY = sizeY/2;	    
	    break;
	case LOWER_RIGHT:	    
	    moveX = sizeX;
	    moveY = sizeY;
	    break;
	case LOWER_CENTER:	    
	    moveX = sizeX/2;
	    moveY = sizeY;
	    break;
	case LOWER_LEFT:	    
	    moveY = sizeY;
	    break;
	default:
	    //DEBUG
	    //System.out.println("ERROR! in method GraphCell.getPort"); 
	    //END DEBUG
	}
	return new Point(moveX, moveY);			
    }        
    /**
     * Return the name labelled at this cell.
     *
     * @return the cell name 
     */
    public String toString() {
	return name;
    }
    /**
     * Returns the cell.
     *
     * @return the cell inclosed in an array
     */
    public CellEvent[] getCells() {
	CellEvent[] cell = new CellEvent[1];
	cell[0] = new CellEvent(this);
	return cell;
    }    
    /**
     * Repaints this cell.
     */
    public void repaint() {		
	//DEBUG
	//System.out.println("GraphCell.repaint()");
	//END DEBUG
	if(cellListener != null) {
	    cellListener.repaint();
	}	
    }
    /**
     * Invoked when the mouse button has been cliked on this cell.
     * <p>
     * <i>The method is not in use.</i>
     */
    public void mouseClicked(MouseEvent e) {
	//DEBUG 
	//System.out.println(this+".GraphCell.mouseClicked()");
	//END DEBUG	
    }
    /**
     * Invoked when the mouse enters this cell.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseEntered(MouseEvent e) {}    
    /**
     * Invoked when the mouse exits this cell.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseExited(MouseEvent e) {}
    /**
     * Invoked when a mouse button has been pressed on this cell.
     * <p>
     * Sends a cell pressed event to registered cell listener.
     */
    public void mousePressed(MouseEvent e) {
	//DEBUG
	//System.out.println(this+".GraphCell.mousePressed()");
	//END DEBUG
	cellListener.cellPressed(new CellEvent(this, e));		
	mousePressedPoint = new Point(e.getX(),e.getY());

    }
    /**
     * Invoked when a mouse button has been released on this cell.
     * <p>
     * Sends a cell released event to registered cell listener.
     */
    public void mouseReleased(MouseEvent e) {	
	//DEBUG
	//System.out.println(this+".GraphCell.mouseReleased()");
	//END DEBUG
	cellListener.cellReleased(new CellEvent(this, e));
    }
    /**
     * Invoked when a mouse button is pressed on this cell and then dragged.
     * <p>
     * Changes the cell position related to the mouse dragg motion. <br>
     * Sends a cell dragged event to registered cell listener.
     */
    public void mouseDragged(MouseEvent e) {
	//DEBUG
	//System.out.println("GraphCell.mouseDragged()");
	//END DEBUG	
	Point pos = getPos();
	refPoint = new Point(e.getX()-mousePressedPoint.x,
			     e.getY()-mousePressedPoint.y);
	pos.translate(refPoint.x, refPoint.y);
	setPos(pos);			
	cellListener.cellDragged(new CellEvent(this, e));
	
    }
    /**
     * Invoked when the mouse cursor has been moved onto this cell
     * but no buttons have been pushed.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseMoved(MouseEvent e) {}    
}
