package org.supremica.external.processeditor.xgraph;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
/**
 * The class draws and handles the cell and edge components. 
 */
public class Graph 
				extends 
					JPanel 
				implements 
					MouseListener,
					MouseMotionListener,
					KeyListener,
					CellListener
{
    public GraphEdge[] edges = new GraphEdge[0];
    public GraphCell[] cells = new GraphCell[0];    
    protected Selection selection = new Selection(this);
    public boolean selectionMoves = false;
    public Point selectionAreaStart = new Point();
    public Point selectionAreaStop = new Point();
    public KeyEvent key;
    public Dimension minimumDimension = new Dimension(650, 500); 
    protected GraphScrollPane scrollPane = null;
    public int tmpCount = 0;                
    
    /**
     * Creates a new instance of the class.
     */
    public Graph() {
	//DEBUG
	//System.out.println("Graph()");
	//END DEBUG
	setPreferredSize(minimumDimension);
	setLayout(null);
	setBackground(Color.WHITE);			
	addMouseListener(this);
	addMouseMotionListener(this);
	addKeyListener(this);
	setFocusable(true);	
    }
    /**
     * Registers a <code>GraphScrollPane</code> as a <code>listener</code> 
     * so that it will be notified when this graph's size has changed.
     *
     * @param sp the <code>GraphScrollPane</code> to registrar as a listener
     */
    public void addScrollListener(GraphScrollPane sp) {
	scrollPane = sp;
    }    
    /**
     * Adds a cell to this graph.
     *
     * @param c the cell to be added
     */
    public void insert(GraphCell c) {
	//DEBUG
	//System.out.println("Graph.insert()");
	//END DEBUG
	GraphCell[] tmpCells = cells;
	cells = new GraphCell[tmpCells.length+1];
	for(int i = 0; i < tmpCells.length; i++) {
	    cells[i] = tmpCells[i];
	}
	cells[tmpCells.length] = c;
	c.addCellListener(this);
	add(c);   		
	validate();
    }
    /**
     * Adds a cell to this graph a the given position.
     *
     * @param c the cell to be added
     * @param index the position at wich to insert the cell, or <code>-1</code>
     * to append the cell to the end
     */
    public void insert(GraphCell c, int index) {
	if((index < cells.length)&&(index >= 0)) { 
	    GraphCell[] tmpCells = cells;
	    cells = new GraphCell[tmpCells.length+1];
	    int j = 0;
	    for(int i = 0; i < cells.length; i++) {
		if(i != index) {
		    cells[i] = tmpCells[j++];
		}else {
		    cells[i] = c;
		}
	    }
	    c.addCellListener(this);
	    add(c, index);	    
	    validate();
	} else {
	    insert(c);
	}	
    }   
    /**
     * Removes the specified cell from this graph.
     *
     * @param c the cell to be removed
     */
    public void remove(GraphCell c) {
	//DEBUG
	//System.out.println("Graph.remove()");
	//END DEBUG
	int exist = -1;
	for(int i = 0; i < cells.length; i++) {
	    if(cells[i].equals(c)) {
		exist = i;
		break;
	    }
	}	
	if(exist != -1) {	
	    GraphCell[] tmpCells = cells;
	    cells = new GraphCell[tmpCells.length-1];	    
	    int j = 0;
	    for(int i = 0; i < tmpCells.length; i++) {
		if(i != exist) {
		    cells[j++] = tmpCells[i];
		}		
	    }	
	    super.remove(c);
	    repaint();
	}
    }
    /**
     * Removes all the cells from this graph.
     */
    public void removeAll() {
	//DEBUG
	//System.out.println("Graph.removeAll()");
	//END DEBUG
	cells = new GraphCell[0];
	super.removeAll();
	repaint();
    }
    /**
     * Removes all the edges from this graph.
     */
    public void removeAllEdges() {
	//DEBUG
	//System.out.println("Graph.removeAllEdges()");
	//END DEBUG
	edges = new GraphEdge[0];
	repaint();
    } 
    /**
     * Adds a edge to this graph.
     *
     * @param e the edge to be added
     */
    public void insert(GraphEdge e) {
	//DEBUG
	//System.out.println("Graph.insert()");
	//END DEBUG
	GraphEdge[] tmpEdges = edges;
	edges = new GraphEdge[tmpEdges.length+1];
	for(int i = 0; i < tmpEdges.length; i++) {
	    edges[i] = tmpEdges[i];
	}
	edges[tmpEdges.length] = e;
	repaint();
    }   
    /**
     * Counts the number of cells in this graph.
     *
     * @return the number of cells
     */
    public int getCellCount() {
	return cells.length;
   }
    /**
     * Returns the i<sup>th</sup> cell in this graph.
     *
     * @param i the index of the cell to return
     * @return the i<sup>th</sup> cell in this graph
     **/
    public GraphCell getCellAt(int i) {
	if((i > 0) && (i <= cells.length)) {
	    return cells[i-1];
	}else {
	    return null;
	}
    }  
    /**
     * Returns all the cells in this graph as an array.
     *
     * @return the array including all cells
     */  
    public CellEvent[] getCells() {
	CellEvent[] mergeCell = new CellEvent[0];	
	for(int i = 0; i < cells.length; i++) {
	    mergeCell = mergeCellArray(mergeCell, cells[i].getCells());
	}
	return mergeCell;
    }
    /**
     * Merges two arrays with cell events together.
     *
     * @param m the first array that is to be merge togehter with 
     * the second array.
     * @param n the second array that is to be merge togheter with
     * the first array.
     * @return the merged array
     */
    private CellEvent[] mergeCellArray(CellEvent[] m, CellEvent[] n) {
	CellEvent[] mergeCell = new CellEvent[m.length+n.length];
	for(int i = 0; i < m.length; i++) {
	    mergeCell[i] = m[i];
	}
	for(int i = 0; i < n.length; i++) {
	    mergeCell[m.length+i] = n[i];
	}
	return mergeCell;
    }
    /**
     * Sets the selection for this graph.
     *
     * @param s the selection to be set for this graph
     */
    public void setSelection(Selection s) {	
	selection = s;
	s.setGraph((JPanel)this);
    }
    /**
     * Returns the selection for this graph
     *
     * @return the selection to be returned
     */
    public Selection getSelection() {
	return selection;
    }
    /**
     * Paints each of the cells and edges in this graph.
     *
     * @param g the graphic context
     */
    public void paintComponent(Graphics g) {
    	//DEBUG
    	//System.out.println("Graph.paintComponent");
    	//END DEBUG              

    	super.paintComponent(g);
    	
    	//----- DRAW EDGE -----
    	for(int i = 0; i < edges.length; i++) {	
    		edges[i].draw(g);
    	}	   	   
    	
    	//----- DRAW SELECTION AREA -----
    	g.setColor(Color.gray);
    	int x; int y; int width; int height;
    	if(selectionAreaStart.x < selectionAreaStop.x) {
    		x = selectionAreaStart.x;
    		width = selectionAreaStop.x-selectionAreaStart.x;
    	}else {
    		x = selectionAreaStop.x;
    		width = selectionAreaStart.x-selectionAreaStop.x;
    	}
    	if(selectionAreaStart.y < selectionAreaStop.y) {
    		y = selectionAreaStart.y;
    		height = selectionAreaStop.y-selectionAreaStart.y;
    	}else {
    		y = selectionAreaStop.y;
    		height = selectionAreaStart.y-selectionAreaStop.y;
    	}	
    	g.drawRect(x, y, width, height);	
    }
    /**
     * Repaints this graph.
     */
    public void repaint() {	
	//DEBUG
	//System.out.println("Graph.repaint()");
	//END DEBUG
	if(scrollPane != null) {
	    scrollPane.repaint();
	}       		
	setCells();
	selectionUpdate();		
	super.repaint();		
    }    
    /**
     * Update the location for each cell in this graph.          
     */
    public void setCells() {
	if(cells != null) {
	    for(int i = 0; i < cells.length; i++) {	       
		cells[i].setPos(cells[i].getPos());	
	    }
	}
    }
    /**
     * Update the selection for this graph.
     */
    public void selectionUpdate() {
	if(selection != null) {
	    selection.update();
	}
    }
    /**
     * Updates the preferred size of this graph.
     *
     * @return the preferred size of this graph
     */
    public Dimension updatePreferredSize() {
	//----- SET PREFERRED SIZE -----
	int prefX = 0; int prefY = 0;
	int marginX = 50; int marginY = 50;
	for(int i = 0; i < cells.length; i++) {
	    if(cells[i].getPos().x+cells[i].getSize().width > prefX) {
		prefX = cells[i].getPos().x+cells[i].getSize().width;
	    }
	    if(cells[i].getPos().y+cells[i].getSize().height > prefY) {
		prefY = cells[i].getPos().y+cells[i].getSize().height;
	    }
	}	
	return new Dimension(prefX + marginX, prefY + marginY);
    }
    /**
     * Updates the large preferred size.
     * <p>
     * This method is used when a object is dragged out of 
     * this graph. The method automatically enlarges this graph's size.
     */
    public  void updateLargePreferredSize() {
	Dimension newPreferredSize = updatePreferredSize();
	int prefX = newPreferredSize.width;
	int prefY = newPreferredSize.height;
	if(prefX > getPreferredSize().width) { 
	    setPreferredSize(new Dimension(prefX, getPreferredSize().height)); 
	}
	if(prefY > getPreferredSize().height) {
	    setPreferredSize(new Dimension(getPreferredSize().width, prefY));
	}
	revalidate();
    }
    /**
     * Updates the smaller preferred size.
     * <p>
     * This method is used to autoscale this graph's size 
     * to remove superflous space.
     */
    public  void updateSmallerPreferredSize() {
	Dimension newPreferredSize = updatePreferredSize();		
	int prefX = newPreferredSize.width;
	int prefY = newPreferredSize.height;
	if((prefX > minimumDimension.width)&& 
	   (prefX < getPreferredSize().width)) { 
	    setPreferredSize(new Dimension(prefX, getPreferredSize().height)); 
	}
	if((prefY > minimumDimension.height)&&
	   (prefY < getPreferredSize().height)){
	    setPreferredSize(new Dimension(getPreferredSize().width, prefY));
	}
	revalidate();    
    }    
    /**
     * Repaints this graph. 
     */
    public void upPack() {
	repaint();
    }
    /**
     * <i>This method is not in use</i>.
     */
    public void downPack() {	
    }
    /**
     * Removes the selection from this graph.
     */
    public void removeSelection() {
	//DEBUG
	//System.out.println("Graph.removeSelection()");
	//END DEBUG
	selection.removeAll();
	repaint();
    }
    /**
     * Invoked when the mouse button has been clicked on this graph.
     * <p>
     * Requests keyboard focus.
     */
    public void mouseClicked(MouseEvent e) {	
	//DEUB
	//System.out.println("Graph.mouseClicked()");
	//END DEBUG
	requestFocus(true);
    }
    /**
     * Invoked when the mouse enters this graph.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void mouseEntered(MouseEvent e) {
    }
    /**
     * Invoked when the mouse exits this graph.
     * <p>
     * <i>This method is not in use.</i>
     **/
    public void mouseExited(MouseEvent e) {
    }
    /**
     * Invoked when a mouse button has been pressed on this graph.
     * <p>
     * Removes previous selection and sets the start location
     * for the selection area.
     */
    public void mousePressed(MouseEvent e) {
	//DEBUG
	//System.out.println("Graph.mousePressed()");
	//END DEBUG		
	
	selection.removeAll();		
	repaint();       	
	setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));	
	selectionAreaStart = new Point(e.getX(), e.getY());
	selectionAreaStop = new Point(e.getX(), e.getY());		
	
    }
    /**
     * Invoked when a mouse button has been pressed on this graph.
     * <p>
     * Sets the end location for the selection area and select 
     * all cells inside.
     */
    public void mouseReleased(MouseEvent e) {
	//DEBUG
	//System.out.println("Graph.mouseReleased");
	//END DEBUG
	setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
	int x1; int y1; int x2; int y2;
	if(selectionAreaStart.x < selectionAreaStop.x) {
	    x1 = selectionAreaStart.x;
	    x2 = selectionAreaStop.x;
	}else {
	    x1 = selectionAreaStop.x;
	    x2 = selectionAreaStart.x;
	}
	if(selectionAreaStart.y < selectionAreaStop.y) {
	    y1 = selectionAreaStart.y;
	    y2 = selectionAreaStop.y;
	}else {
	    y1 = selectionAreaStop.y;
	    y2 = selectionAreaStart.y;
	}
	CellEvent[] tmpCells = getCells();
	for(int i = 0; i < tmpCells.length; i++) {
	    Point pos1 = tmpCells[i].getPos();	    	    
	    Point pos2 = new Point(pos1.x+tmpCells[i].getSize().width,
				   pos1.y+tmpCells[i].getSize().height);
	    if(((pos1.x > x1)&&(pos1.x < x2)&&(pos1.y > y1)&&(pos1.y < y2))&&
	       (pos2.x > x1)&&(pos2.x < x2)&&(pos2.y > y1)&&(pos2.y < y2)){
		selection.add(tmpCells[i]);		
	    }
	}
	selectionAreaStart = new Point();
	selectionAreaStop = new Point();
	repaint();
    }
    /**
     * Invoked when a mouse button is pressed on this graph and then
     * dragged.
     * <p>
     * Update the end location for the selection area and draw the 
     * selection area border. Also update the scrollpane scroll focus
     * if necessary. 
     */
    public void mouseDragged(MouseEvent e) {
	//DEBUG
	//System.out.println("Graph.mouseDragged()");
	//END DEBUG		
	selectionAreaStop = new Point(e.getX(), e.getY());
	repaint();
	
	if(scrollPane != null) {
	    scrollPane.setScrollFocus(e.getX(), e.getY());
	}
    }
    /**
     * Invoked when the mouse cursor has been moved onto this graph
     * but no buttons have been pushed.
     * <p>
     * <i>This metod is not in use.</i>
     */
    public void mouseMoved(MouseEvent e) {}    
    /**
     * Invoked when a key has been pressed.
     * <p>
     * If the <code>Ctrl</code> key is pressed it is
     * possible to select several cells.
     */
    public void keyPressed(KeyEvent e) {
	//DEBUG
	//System.out.println("Graph.keyPressed(): "+e);
	//END DEBUG
	key = e;
    }
    /**
     * Invoked when a key as been released.
     */
    public void keyReleased(KeyEvent e) {
	//DEBUG
	//System.out.println("Graph.keyReleased(): "+e);
	//END DEBUG
	key = null;
    }
    /**
     * Invoked when a key has been typed.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void keyTyped(KeyEvent e) {
	//DEBUG
	//System.out.println("Graph.keyTyped(): "+e);
	//END DEBUG
    }    
    /**
     * Invoked when a cell is pressed.
     * <p>
     * If the <code>Ctrl</code> key is not pressed previous
     * selection is removed and the pressed cell is selected.
     */
    public void cellPressed(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Graph.cellPressed()");	
	//END 		
	requestFocus(true);
	//DEBUG
	//%%%%%%%%%%%%%%% COMMENT %%%%%%%%%%%%%%%%%%%%%%%	
	// I) CTR NOT DOWN AND CELL NOT SELECTED    
	//        --> REMOVE ALL SELECTIONS AND ADD CELL TO SELECTED
	//     ELSE
	//        --> DO NOTHING
	//%%%%%%%%%%%%% END COMMENT %%%%%%%%%%%%%%%%%%	
	if((key == null)&&(selection.isSelected(cEvent) == -1)) {
	    //DEBUG
	    //System.out.println("I) CTR NOT DOWN AND CELL NOT SELECTED");
	    //END DEBUG
	    selection.removeAll();
	    selection.add(cEvent);	    
	}	
	selection.setOrigo(cEvent);
	repaint();
    }
    /**
     * Invoked when a cell is released.
     * <p>
     * If the <code>Ctrl</code> key is not pressed previous
     * selection is removed and the pressed cell is selected. <br>
     * If the <code>Ctrl</code> key is pressed the cell is
     * unselected or selcted depending on if it was previous 
     * selected or not. 
     */
    public void cellReleased(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Graph.cellReleased()");
	//END DEBUG	
	//%%%%%%%%%%%%%%%% COMMENT %%%%%%%%%%%%%%%%%%%%%
	// i) SELECTION MOVES
	//          --> SELECTION STOPPED MOVING
	//              IF CELL NOT SELECTED
	//                --> ADD CELL TO SELECTED
	//              SET SELECTION
	//    ELSE 
	//          II) CTR IS DOWN
	//                1) CELL IS SELECTED
	//                    --> ADD CELL TO SELECTED
	//                2) CELL IS NOT SELECTED
	//                    --> REMOVE CELL FROM SELECTED
	//         III) CTR IS NOT DOWN
	//              --> REMOVE ALL SELECTIONS AND ADD CELL TO 
	//%%%%%%%%%%%%%%%%%% END COMMENT %%%%%%%%%%%%%%%
	if(selectionMoves) {
	    //DEBUG
	    //System.out.println("selectionMoves = true");
	    //END DEBUG
	    selection.moveSelected(cEvent);
	    selectionMoves = false;	    
	    selection.hide(false);
	    selection.add(cEvent);	    	
	    updateSmallerPreferredSize();
	}else {
	    if((key != null)&&(key.isControlDown())) {	       
		int index = selection.isSelected(cEvent);
		if(index == -1) {		    
		    selection.add(cEvent);		    
		}else {
		    selection.remove(cEvent);
		}		
	    }else {
		selection.removeAll();
		selection.add(cEvent);		
	    }
	}	
	repaint();	
    }       
    /**
     * Invoked when a cell is dragged.
     * <p>
     * Updates the large preferred size and the scrollpane scroll
     * focus is set.  
     */
    public void cellDragged(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Graph.cellDragged()");
	//END DEBUG	
	if(!selectionMoves) {	    
	    selectionMoves = true;	
	    selection.hide(true);
	}	
	selection.moveSelected(cEvent);		
	updateLargePreferredSize();		
       
	if(scrollPane != null) {	    
	    scrollPane.setScrollFocus(cEvent.getMouseEvent().getX()+
					      cEvent.getPos().x,
					      cEvent.getMouseEvent().getY()+
					      cEvent.getPos().y);	    
	}		
	selection.moveSelected(cEvent);
    }
    /**
     * Invoked when a cell is moved.
     * <p>
     * <i>This method is not in use.</i>
     */
    public void cellMove(CellEvent cEvent) {
    }
}
