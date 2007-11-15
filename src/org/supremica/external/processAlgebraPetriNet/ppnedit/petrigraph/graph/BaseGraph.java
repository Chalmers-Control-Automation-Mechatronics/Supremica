package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;

/*
*	Graph is writhen by Mikael and Magnus
*	BaseGraph extends Graph in order to
*	manipulate the behaviour without
*	editing in Graph.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:
 *
 */

import javax.swing.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.util.BaseCellArray;
import org.supremica.external.processeditor.xgraph.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;

import java.awt.*;
import java.awt.event.*;



public class BaseGraph extends Graph
                               implements EditableCellListener,
							              ActionListener {
	
	protected BaseCell[] targetCells = null;
    protected BaseCell[] sourceCells = null;
	
	protected JPopupMenu popupMenu;
	
	private final JMenuItem menuCellCopy = new JMenuItem( "Copy" );
	private final JMenuItem menuCellCut = new JMenuItem( "Cut" );
	private final JMenuItem menuCellPaste = new JMenuItem( "Paste" );
	private final JMenuItem menuCellDelete = new JMenuItem( "Delete" );
	
	private static BaseCell[] copiedCells = null;
	
	protected Point lastClick = new Point();
	
    //constructor
    public BaseGraph(){
		super();
		selection = new BaseSelection();
		
		popupMenu = new JPopupMenu("Menu");
		makePopupMenu();
		add( popupMenu );
    }
	
	protected void makePopupMenu(){

		popupMenu.add( menuCellCopy );
		popupMenu.add( menuCellCut );
		popupMenu.add( menuCellPaste );
		popupMenu.add( menuCellDelete );
		
		menuCellCopy.addActionListener(this);
		menuCellCut.addActionListener(this);
		menuCellPaste.addActionListener(this);
		menuCellDelete.addActionListener(this);
	}
    
    /**
    *	Indata: BaseCell
    *
    *	insert cell to graph. 
    *	
    */
    public void insert(BaseCell cell) {
        if(cell instanceof EditableCell){ 
            ((EditableCell)cell).addEditableCellListener(this);
        }
		
		final Point pos = cell.getPos();
		
		if(pos.x < 0){
			pos.x = 0;
			cell.setPos(pos);
		}
		
		if(pos.y < 0){
			pos.y = 0;
			cell.setPos(pos);
		}
		
        super.insert(cell);
    }
	
	@Override
	public BaseSelection getSelection() {
		return (BaseSelection)selection;
    }
	@Override
	public void setSelection(Selection s) {
		if(s instanceof BaseSelection){
			selection = (BaseSelection)s;
		}
    }
	
    /**
    *	Indata: BaseCell
    *
    *	insert cell to graph. 
    *	
    */
    public void insert(BaseCell cell, int index) {
        if(cell instanceof EditableCell){
            ((EditableCell)cell).addEditableCellListener(this);
        }
        super.insert(cell,index);
    }
    
    /**
    *	Indata: BaseCell
    *
    *	Remove cell from graph. 
    *	
    */
    public void remove(final BaseCell cell) {
        
        //Remove old cell from it's conections
        final BaseCell[] oldTargetCells = cell.getTargetCells();
		
        //remove oldCell from it's sourcecells and add newCell to
        //it's Sourcecell
        if(oldTargetCells != null){
            for (BaseCell element : oldTargetCells) {            
                element.removeSourceCell(cell);
            }
        }
        
        final BaseCell[] oldSourceCells = cell.getSourceCells();
        if(oldSourceCells != null){
            for (BaseCell element : oldSourceCells) {
                element.removeTargetCell(cell);
            }
        }
        
        removeSourceCell(cell);
        removeTargetCell(cell);
        
		super.remove(cell);
    }
	
    /**
    *	Indata: BaseCell
    *
    *	tell the graph that this cell is target cell.
    */
    public void addTargetCell(final BaseCell cell){
        targetCells = BaseCellArray.add(cell,targetCells);
    }
    
    /**
    *	Indata: BaseCell
    *
    *	remove cell from this graph
    */
    public void removeTargetCell(final BaseCell cell){
    	targetCells = BaseCellArray.remove(cell,targetCells);
    }
    
    /**
    *	Return: array of BaseCell
    *
    *	returns a pointer to all target cells in this
    *	graph.
    *
    */
    public BaseCell[] getTargetCells(){
        return targetCells;
    }
    
    /**
    *	Indata: BaseCell
    *	Return: boolean
    *
    *	return true or false depending on if cell
    *	is target cell in this graph.
    *
    */
    public boolean isTargetCell(final BaseCell cell){
        if(targetCells == null){
            return false;
        }
        
        for (BaseCell element : targetCells) {
            if(cell.equals(element)){
                return true;
            }
        }
        return false;
    }
    
    /**
    *	Indata: BaseCell
    *
    *	add cell as source cell in this graph.
    *
    */
    public void addSourceCell(final BaseCell cell){
        sourceCells = BaseCellArray.add(cell,sourceCells);
    }
    
    /**
    *	Indata: BaseCell
    *
    *	Remove cell as source cell in this graph.
    *	The cell is still in the graph
    */
    public void removeSourceCell(final BaseCell cell){
        sourceCells = BaseCellArray.remove(cell,sourceCells);
    }
	
	/**
    *
    *	Remove all cells in graph
	*
    */
    @Override
	public void removeAll(){
        sourceCells = null;
		targetCells = null;
		super.removeAll();
    }
	
    /**
    *	Returns: array of BaseCell
    *
    *	Gives a pointer to all source cells in this graph
    */
    public BaseCell[] getSourceCells(){
        return sourceCells;
    }
	
    /**
    *	Indata: BaseCells 
    *	Returns: boolean
    *
    *	Returns true or false depending on if the
    *	cell is SourceCell in this graph
    */
    public boolean isSourceCell(final BaseCell cell){
        if(sourceCells == null){
            return false;
        }
        
        for (BaseCell element : sourceCells) {
            if(cell.equals(element)){
                return true;
            }
        }
        return false;
    }
    
    /**
    *	Indata: BaseCells 
    *	Returns: boolean
    *
    *	Returns true or false depending on if the
    *	cell exist in this graph
    */
    public boolean exist(final BaseCell cell){
        if((cells == null) || !(cell instanceof GraphCell)){
            return false;
        }
        
        final GraphCell gcell = cell;
        
        for (GraphCell element : cells) {
            if(gcell.equals(element)){
                return true;
            }
        }
        return false;
    }
    
    public void selectionUpdate() {
    	if(selection != null) {
    	    selection.update();
    	}
    }
    
    /**
    *	Indata: BaseCells 
    *	Returns: int 
    *
    *	Returns the index of a cell in the graph
    *   retrurns -1 if not exist
    */
    public int getIndex(final BaseCell cell){
        if((cells == null) || !(cell instanceof GraphCell)){
            return -1;
        }
        
        final GraphCell gcell = cell;
        
        for(int i = 0; i < cells.length; i++) {
            if(gcell.equals(cells[i])){
                return i;
            }
        }
        return -1;
    }
    
    /**
    *	Indata: two BaseCells 
    *
    *	replace a cell in the graph whit another.
    *	 
    */
    public void replaceCell(final BaseCell oldCell, final BaseCell newCell) {
        
        //check indata
        if((oldCell == null) || (newCell == null)){
            return;
        }else if(!exist(oldCell)){
            return;
        }
       
        //add old target/source cells to newCell
        final BaseCell[] oldTargetCells = oldCell.getTargetCells();
        if(oldTargetCells != null){
            for (BaseCell element : oldTargetCells) {            
                element.addSourceCell(newCell);
            }
			newCell.drawTargetLines(true);
        }
        
        final BaseCell[] oldSourceCells = oldCell.getSourceCells();
        if(oldSourceCells != null){
            for (BaseCell element : oldSourceCells) {
                element.addTargetCell(newCell);
            }
			newCell.drawSourceLines(true);
        }
        
        //add as target if target
        if(isTargetCell(oldCell)){
            addTargetCell(newCell);
        }
		
        if(isSourceCell(oldCell)){
            addSourceCell(newCell);
        }
		
        insert(newCell,getIndex(oldCell));
        newCell.setPos(oldCell.getPos());
        remove(oldCell);
    }
    
    @Override
    public void mouseClicked(MouseEvent e) {
		lastClick = e.getPoint();
		selection.removeAll();
		
		if(!e.isPopupTrigger()){
			super.mouseClicked(e);
		}	
    }
    
	@Override
	public void mousePressed(final MouseEvent e) {
		
		lastClick = e.getPoint();
		
		if( MouseEvent.BUTTON2 == e.getButton()){
			selection.removeAll();
			selectionAreaStart = new Point(e.getX(), e.getY());
			selectionAreaStop = new Point(e.getX(), e.getY());
		}
		
		if( MouseEvent.BUTTON1 == e.getButton()){
			selectionAreaStart = new Point(e.getX(), e.getY());
			selectionAreaStop = new Point(e.getX(), e.getY());
		}
		
		repaint();
	}
	
	@Override
	public void mouseReleased(final MouseEvent e) {
		
		int x1, y1;
		int x2, y2;
		
		setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
		
		if(e.isPopupTrigger()){
			lastClick = e.getPoint();
			showPopupMenu(e.getPoint());
			return;
		}
		
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
		
		for(int i = 0; i < cells.length; i++) {
		    Point pos1 = cells[i].getPos();	    	    
		    Point pos2 = new Point(pos1.x+cells[i].getSize().width,
					 pos1.y+cells[i].getSize().height);
		    if(((pos1.x > x1)&&(pos1.x < x2)&&(pos1.y > y1)&&(pos1.y < y2))&&
		       	(pos2.x > x1)&&(pos2.x < x2)&&(pos2.y > y1)&&(pos2.y < y2)){
					((BaseSelection)selection).add(cells[i]);		
		    }
		}
		
		selectionAreaStart = new Point();
		selectionAreaStop = new Point();
			
		repaint();
	}
    
	protected void showPopupMenu(final Point pos){
		if(selection.hasSelected()){
			menuCellCopy.setEnabled(true);
			menuCellCut.setEnabled(true);
			menuCellDelete.setEnabled(true);
		}else{
			menuCellCopy.setEnabled(false);
			menuCellCut.setEnabled(false);
			menuCellDelete.setEnabled(false);
		}
		
		menuCellPaste.setEnabled(copiedCells != null);
		
		popupMenu.show(this, pos.x, pos.y);
	}
	
	public static void storeCells(final BaseCell[] cells){
		copiedCells = cells;
	}
	
	public static BaseCell[] getStoredCells(){
		return copiedCells;
	}
	
	public void storeSelectedCells(){
		final GraphCell[] cells = ((BaseSelection)selection).getSelected();
		
		if((cells == null) || (cells.length == 0)){
			return;
		}
		
		copiedCells  = new BaseCell[cells.length];
		for(int i = 0; i < cells.length; i ++){
			if(cells[i] instanceof BaseCell){
				copiedCells[i] = (BaseCell) cells[i];
			}else{
				copiedCells = null;
				break;
			}
		}
	}
	
	public void pasteStoredCells(){
		if((copiedCells == null) ||
		   (copiedCells.length == 0)){
			   return;
		}
		
		if(((BaseSelection)selection).getSelected() == null){
			for (BaseCell element : copiedCells) {
				if(element instanceof EditableCell){
					paste(((EditableCell)element).copy());
				}
			}
		}else{
			
			final GraphCell[] selectedCells = ((BaseSelection)selection).getSelected();
			
			for (BaseCell element : copiedCells) {
				if(element instanceof EditableCell){
					for (GraphCell element0 : selectedCells) {
						if(element0 instanceof EditableCell){
							((EditableCell)element0).paste(((EditableCell)element).copy());
						}
					}
				}
			}
		}
		repaint();
		
		storeCells(null);
		validate();
	}
	
	
	public void deleteSelectedCells(){
		final GraphCell[] cells = ((BaseSelection)selection).getSelected();
		if((cells !=null) && (cells.length > 0)){
			for (GraphCell element : cells) {
				if(element instanceof EditableCell){
					((EditableCell)element).delete();
				}
			}
		}
		selection.removeAll();
	}
	
    /*----------- EditableCellListener --------------------*/
    public void copy(final BaseCell cell){}

    public void delete(final BaseCell cell){
        remove(cell);
    }

    public void paste(final BaseCell cell){
		cell.setPos(lastClick);
        insert(cell);
    }
	public void replace(final BaseCell oldCell, final BaseCell newCell){
		replaceCell(oldCell, newCell);
	}
	public void modified(final BaseCell cell){}
	/*----------- End EditableCellListener---------------*/
	
	/* ------------ Action Listener -------------*/
	public void actionPerformed( final ActionEvent event ){
		
		if(event.getActionCommand().equals("Copy")){
			storeSelectedCells();
		}else if(event.getActionCommand().equals("Cut")){
			storeSelectedCells();
			deleteSelectedCells();
		}else if(event.getActionCommand().equals("Paste")){
			pasteStoredCells();
		}else if(event.getActionCommand().equals("Delete")){
			deleteSelectedCells();
		}else{
			System.out.println( "Unknown event: " + event );
		}
	}
	/* ------------ End Action Listener -------------*/
	
	
	//------------------- CellListener override ------------------------
    public void cellPressed(CellEvent cEvent) {
		requestFocus(true);
		if((key == null)&&(((BaseSelection)selection).isSelected(cEvent.getSource()) == -1)) {
	    	selection.removeAll();
	    	((BaseSelection)selection).add(cEvent.getSource());	    
		}
		((BaseSelection)selection).setOrigo(cEvent.getSource());
		repaint();
    }
    
    public void cellReleased(CellEvent cEvent) {
		if(selectionMoves) {
	    	((BaseSelection)selection).moveSelected(cEvent.getSource());
	    	selectionMoves = false;	    
	    	selection.hide(false);
	    	((BaseSelection)selection).add(cEvent.getSource());	
	    	updateSmallerPreferredSize();
		}else {
	    	if((key != null)&&(key.isControlDown())) {
				int index = ((BaseSelection)selection).isSelected(cEvent.getSource());
				if(index == -1) {
					((BaseSelection)selection).add(cEvent.getSource());		    
				}else {
					((BaseSelection)selection).remove(cEvent.getSource());
				}		
	    	}else {
				selection.removeAll();
				((BaseSelection)selection).add(cEvent.getSource());		
	    	}
		}
		repaint();
    }
    
    public void cellDragged(CellEvent cEvent) {
		if(!selectionMoves) {	    
	    	selectionMoves = true;	
	    	selection.hide(true);
		}	
		((BaseSelection)selection).moveSelected(cEvent.getSource());	
		updateLargePreferredSize();
		
		if(scrollPane != null) {
	    	scrollPane.setScrollFocus(cEvent.getMouseEvent().getX()+
					      cEvent.getPos().x,
					      cEvent.getMouseEvent().getY()+
					      cEvent.getPos().y);
		}	    
		((BaseSelection)selection).moveSelected(cEvent.getSource());
    }
}
