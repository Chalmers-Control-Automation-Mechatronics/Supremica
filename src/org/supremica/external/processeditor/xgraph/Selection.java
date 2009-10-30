package org.supremica.external.processeditor.xgraph;

import javax.swing.*;
import java.awt.*;

/**
 * Handles the user selection of grahical objects, 
 * such as resources, relations and operations.
 * Draws the selection frame around selected objects.
 */
public class Selection {

    protected JPanel graph;
    protected CellSelection[] selections = new CellSelection[0];
    protected Point[] refPos = new Point[0];
    protected Point origo = new Point(0,0);    
    protected SelectionListener myListener = null;

    /**
     * Creates a new instance of the class.
     */
    public Selection() {}

    /**
     * Creates a new instance of the class and sets its graph. 
     * <p>
     * The specified graph is where the <code>Selection</code> object draws 
     * the seletion frame.
     * specified graph.
     *
     * @param graph 
     */
    Selection(JPanel graph) {
	setGraph(graph);
    }
    
    /**
     * Sets the graph of this <code>Selection</code> object.
     * <p>
     * The specified graph is where the <code>Selection</code> object draws 
     * the selection frame.
     * specified graph.
     *
     * @param graph
     */
    public void setGraph(JPanel graph) {
	this.graph = graph;
    }
    /**
     * Adds a new cell as selected.
     *
     * @param cEvent the <code>cEvent.getSource()</code> is the new cell 
     * to select.
     */
    public void add(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Selection.add()");
	//END DEBUG
	GraphCell c = cEvent.getSource();
	if(isSelected(new CellEvent(c)) == -1) {
	    CellSelection[] tmpSelections = selections;
	    selections = new CellSelection[tmpSelections.length+1];
	    for(int i = 0; i < tmpSelections.length; i++) {
		selections[i] = tmpSelections[i];
	    }
	    selections[tmpSelections.length] = new CellSelection(cEvent);
	}
	selectionChanged();
    }
    /**
     * Adds the specified selection listener to receive selection events from
     * this <code>Selection</code>.
     * 
     * @param l the selection listener
     */
    public void addSelectionListener(SelectionListener l) {
	myListener = l;
    }
    /**
     * Removes the selected cell from this <code>Selection</code> object, 
     * specified by the <code>cEvent.getSource()</code>.
     *
     *
     */
    public void remove(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Selection.remove()");
	//END DEBUG
	GraphCell c = cEvent.getSource();
	int index = isSelected(new CellEvent(c));
	if(index != -1) {
	    CellSelection[] tmpSelections = selections;
	    selections = new CellSelection[tmpSelections.length-1];
	    int j = 0;
	    for(int i = 0; i < tmpSelections.length; i++) {
		if(i != index) {
		    selections[j++] = tmpSelections[i];
		}else {
		    tmpSelections[i].removeSelection();
		}
	    }
	}
	selectionChanged();
    }
    /**
     * Removes all selected cell from this <code>Selection</code> object.
     */
    public void removeAll() {
	//DEBUG
	//System.out.println("Selection.removeAll()");
	//END DEBUG
	for(int i = 0; i < selections.length; i++) {
	    selections[i].removeSelection();
	}
	selections = new CellSelection[0];
	selectionChanged();
    }
    /**
     * Checks if the specified <code>cEvent.getSource()</code> cell 
     * is one of the 
     * selected cells in this <code>Selection</code> object. 
     * <p>
     * If the cell is not selected <code>-1</code> will be returned, otherwise
     * the selection index of the cell.
     *
     * @param cEvent the <code>cEvent.getSource()</code> is the cell to check
     * @return <code>-1</code> if the cell is not selected, 
     * otherwise the selection index of the cell
     */
    public int isSelected(CellEvent cEvent) {	
	GraphCell c = cEvent.getSource();
	int index = -1;
	for(int i = 0; i < selections.length; i++) {
	    if(selections[i].isSelected(new CellEvent(c))) {
		index = i;
		break;
	    }
	}
	return index;
    }
    /**
     * Redraws all the selection frames.
     */
    public void update() {
	//DEBUG
	//System.out.println("Selection.update()");
	//END DEBUG
	for(int i = 0; i < selections.length; i++) {
	    selections[i].setSelection();
	}
    }
    /**
     * Sets all the selection frames invisible or not.
     *
     * @param b if <code>true</code> the selection frames sets invisible, 
     * <code>false</code> otherwise.
     */
    public void hide(boolean b) {
	//DEBUG
	//System.out.println("Selection.hide()");
	//END DEBUG
	for(int i = 0; i < selections.length; i++) {
	    selections[i].setVisible(!b);
	}
    }
    /**
     * Moves all selected cell when one of the selected cells are dragged.
     *
     * @param cEvent the <code>cEvent.getSource()</code> is the
     * selected cell that is dragged
     */
    public void moveSelected(CellEvent cEvent) {
	//DEBUG
	//System.out.println("Selection.moveSelected()");
	//END DEBUG
	Point newOrigo = cEvent.getPos();
	for(int i = 0; i < selections.length; i++) {
	    if(!selections[i].selection.getSource().equals(cEvent.getSource())) {
	    selections[i].translateSelectedCellPos(newOrigo.x-origo.x,
						   newOrigo.y-origo.y);
	    selections[i].selection.translatePos(newOrigo.x-origo.x,
						 newOrigo.y-origo.y);
	    }else {
		selections[i].selection = cEvent;
	    }
	}
	origo = newOrigo;
    }
    /**
     * Sets the origo of this <code>Selection</code> object.
     *
     * @param cEvent the <code>cEvent.getPos()</code> specifies the origo
     */
    public void setOrigo(CellEvent cEvent) {
	origo = cEvent.getPos();
    }   
    /**
     * Returns the cell specified by the selection index.
     *
     * @param index the selection index
     * @return the cell thats corresponds to the selection index.
     * Returns <code>null</code> if the index doesn't exists.
     */    
    public GraphCell getSelectedAt(int index) {
	if((selections.length > 0)&&(index < selections.length)) {
	    if(index >= 0) {
		return selections[index].selection.getSource();
	    }else if((index == -1)&&(selections.length > 0)) {
		return selections[selections.length-1].selection.getSource();
	    }
	}
	return null;	
    }
    /**
     * Notifies this <code>Selection</code> object's selection listener that
     * changes has been made.
     */           
    public void selectionChanged() {
	if(myListener != null) {
	    myListener.selectionChanged(this);
	}
    }
    /**
     * Returns the number of selected cells.
     *
     * @return number of selected cells.
     */
    public int getSelectedCount() {
	return selections.length;
    }
    /**
     * Returns a <code>boolean</code> whether or not 
     * this <code>Selection</code> object has one or more selected cells.
     *
     * @return <code>true</code> if the there is one or more selected cells,
     * <code>false</code> otherwise.
     */
    public boolean hasSelected() {
	if(getSelectedCount() == 0) {
	    return false;
	}else {
	    return true;
	}
    }        
    /**
     * Draws selection frames.
     */  
    class CellSelection {	
	private CellEvent selection = null;    	
	private final SelectItem upperLeft = new SelectItem();
	private final SelectItem upperMid = new SelectItem();
	private final SelectItem upperRight = new SelectItem();
	private final SelectItem midRight = new SelectItem();
	private final SelectItem lowerRight = new SelectItem();
	private final SelectItem lowerMid = new SelectItem();
	private final SelectItem lowerLeft = new SelectItem();
	private final SelectItem midLeft = new SelectItem();
	
	/**
	 * Creates new instance of the class with 
	 * <code>cEvent.getSource()</code> as the selected cell.
	 *
	 * @param cEvent the <code>cEvent.getSource()</code> is the new cell 
	 * to select.
	 */
	public CellSelection(CellEvent cEvent) {
	    //DEBUG
	    //System.out.println("Selection()");	    
	    //END DEBUG
	    selection = cEvent;
	    addAll();
	    setSelection();	    	    
	    setVisible(true);
	}
	/**
	 * Set the selection frame position around the selected cell of this
	 * <code>CellSelection</code> object.
	 */
	public void setSelection() {
	    //DEBUG
	    //System.out.println("Selection.setSelection("+selection+")");
	    //END DEBUG		
	    int x1 = selection.getPos().x;
	    int y1 = selection.getPos().y;
	    int x2 = x1 + selection.getSize().width;
	    int y2 = y1 + selection.getSize().height;
	    upperLeft.setLocation(x1-SelectItem.SIZE_X/2, 
				  y1-SelectItem.SIZE_Y/2);
	    upperMid.setLocation(x1+(x2-x1)/2-SelectItem.SIZE_X/2,
				 y1+-SelectItem.SIZE_Y/2);
	    upperRight.setLocation(x2-SelectItem.SIZE_X/2, 
				   y1-SelectItem.SIZE_Y/2);
	    midRight.setLocation(x2-SelectItem.SIZE_X/2,
				 y1+(y2-y1)/2-SelectItem.SIZE_Y/2);
	    lowerRight.setLocation(x2-SelectItem.SIZE_X/2, 
				   y2-SelectItem.SIZE_Y/2);
	    lowerMid.setLocation(x1+(x2-x1)/2-SelectItem.SIZE_X/2,
				 y2-SelectItem.SIZE_Y/2);
	    lowerLeft.setLocation(x1-SelectItem.SIZE_X/2, 
				  y2-SelectItem.SIZE_Y/2);	
	    midLeft.setLocation(x1-SelectItem.SIZE_X/2, 
				y1+(y2-y1)/2-SelectItem.SIZE_Y/2);     	
	}
	/**
	 * Sets the selected cell reference position.
	 * <p>
	 * This method doesn't change the position of the selected cell
	 * it self, only its reference position to 
	 * this <code>CellSelection</code> object. 
	 *
	 * @param p the location point to set
	 */
	public void setSelectedCellPos(Point p) {
	    selection.getSource().setPos(p);	
	}
	/**
	 * Returns the selected cell reference position.
	 */
	public Point getSelectedCellPos() {
	    return selection.getSource().getPos();
	}
	/**
	 * Translates the reference position of the selected cell.
	 * <p>
	 * Translates the reference position of the selected cell,
	 * at position (<i>x</i>, <i>y</i>), by
	 * <code>dx</code> along the <i>x</i> axis and <code>dy</code> along the
	 * <i>y</i> axis so that the new position will be 
	 * (<code>x+dx</code), <code>y+dy</code>).<br>
	 * This method doesn't change the position of the selected cell it self,
	 * only its reference position.
	 *
	 * @param dx the distance to move this cell along the <i>x</i> axis
	 * @param dy the distance to move this cell along the <i>y</i> axis
	 */
	public void translateSelectedCellPos(int dx, int dy) {
	    selection.getSource().translatePos(dx, dy);
	}
	/**
	 * Returns the selected cell size of 
	 * this <code>CellSelection</code> object.
	 * 
	 * @return the dimesion of the selected cell size
	 */
	public Dimension getSelectedCellSize() {
	    return selection.getSource().getSize();
	}
	/**
	 * Removes selection.
	 */
	public void removeSelection() {
	    //DEBUG
	    //System.out.println("Selection.removeSelection()");
	    //END DEBUG
	    selection = null;
	    removeAll();			
	}    	
	/**
	 * Add the selection frame to the graph panel.
	 */
	private void addAll() {
	    //DEBUG
	    //System.out.println("Selection.CellSelection.addAll()");
	    //END DEBUG
	    graph.add(upperLeft,0);
	    graph.add(upperMid,0);
	    graph.add(upperRight,0);
	    graph.add(midRight,0);
	    graph.add(lowerRight,0);
	    graph.add(lowerMid,0);
	    graph.add(lowerLeft,0);
	    graph.add(midLeft,0);
	}
	/**
	 * Removes the selection frame from the graph panel.
	 */ 
	private void removeAll() {
	    graph.remove(upperLeft);
	    graph.remove(upperMid);
	    graph.remove(upperRight);
	    graph.remove(midRight);
	    graph.remove(lowerRight);
	    graph.remove(lowerMid);
	    graph.remove(lowerLeft);
	    graph.remove(midLeft);
	}
	/**
	 * Sets the selection frame invisible or not.
	 * 
	 * @param b if <code>true</code> the selection frame sets invisible,
	 * false otherwise.
	 */
	private void setVisible(boolean b) {
	    upperLeft.setVisible(b);
	    upperMid.setVisible(b);
	    upperRight.setVisible(b);
	    midRight.setVisible(b);
	    lowerRight.setVisible(b);
	    lowerMid.setVisible(b);
	    lowerLeft.setVisible(b);
	    midLeft.setVisible(b);
	}
	/**
	 * Checks whether this <code>CellSelection</code> object 
	 * has a selected cell or not.
	 *
	 * @param return <code>true</code> if it has selection, 
	 * <code>false</code> otherwise.
	 */
	public boolean isSelected() {
	    if(selection == null) {
		return false;
	    }else {
		return true;
	    }
	}
	/**
	 * Checks if the specified <code>cEvent.getSource()</code> cell
	 * is the selected cell in this <code>CellSelection</code> object.
	 *
	 * @param cEvent the <code>cEvent.getSource()</code> is the cell to check
	 * @return <code>true</code> if it is the selected cell,
	 * <code>false</code> otherwise.
	 */
	public boolean isSelected(CellEvent cEvent) {
	    if(isSelected() && 
	       selection.getSource().equals(cEvent.getSource())) {
		return true;
	    }else {
		return false;
	    }
	}
	/**
	 * The selection frame it self.
	 */
	class SelectItem extends JPanel {
	    private static final long serialVersionUID = 1L;

	    public static final int SIZE_X = 6;
	    public static final int SIZE_Y = 6;
	    /**
	     * Creates a new instance of this class.
	     */
	    public SelectItem() {
		setVisible(false);
		setSize(SIZE_X, SIZE_Y);
		setBackground(Color.lightGray);
		setLayout(null);
		JPanel upper = new JPanel();
		upper.setBounds(0, 0, 5, 1);
		upper.setBackground(Color.white);
		JPanel right = new JPanel();
		right.setBounds(5, 0, 1, 5);
		right.setBackground(Color.gray);
		JPanel lower = new JPanel();
		lower.setBounds(1, 5, 5, 1);
		lower.setBackground(Color.gray);
		JPanel left = new JPanel();
		left.setBounds(0, 1, 1, 5);
		left.setBackground(Color.white);
		add(upper); add(right); add(lower); add(left);  
	    }	
	}
    }
}
