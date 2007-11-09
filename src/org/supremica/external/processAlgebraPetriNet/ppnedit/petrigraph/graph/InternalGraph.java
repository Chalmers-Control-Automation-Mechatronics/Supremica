package org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph;

/*
*	InternalGraph is used inside a BaseCell.
*
*	David Millares 2007-02-16 
*/

/*
 * To Do:	add comments
 *			see horizontalLine
 *
 */

import java.awt.*;

import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.celltype.base.*;
import org.supremica.external.processAlgebraPetriNet.ppnedit.petrigraph.graph.*;
import org.supremica.external.processeditor.xgraph.*;


public class InternalGraph 
					extends BaseGraph
								implements CellListener{
    
	private CellListener graphListener;
    private InternalGraphListener internalListener;
    
    private int marginX = 10;
    private int marginY = 10;
    
    private final Color backgroundColor = new Color(0,0,100,20);
    
    private boolean drawMargin = true;
	
    private boolean drawSourceLines = true;
    private boolean drawTargetLines = true;
	
    public InternalGraph() {
		
		setBackground(backgroundColor);
		
        //remove listener from Graph
        this.removeMouseListener(this);
        this.removeMouseMotionListener(this);
        removeKeyListener(this);
	
        //remove selection
        selection = null;
        
        setFocusable(false);
        pack();
    }
    
    public void drawGraphLines(final boolean draw){
        drawSourceLines(draw);
		drawTargetLines(draw);
    }
	public void drawSourceLines(final boolean draw){
        drawSourceLines = draw;
    }
	public void drawTargetLines(final boolean draw){
		drawTargetLines = draw;
    }
	
	//ovverride from BaseGraph
	@Override
	public void replaceCell(final BaseCell oldCell, final BaseCell newCell) {
		newCell.drawSourceLines(drawSourceLines);
		newCell.drawTargetLines(drawTargetLines);
		super.replaceCell(oldCell,newCell);
	}
	
	
	public void setBgColor(final Color color){
        setBackground(color);
    }
	
    public void drawMargin(final boolean margin){
        drawMargin = margin;
    }
    
    @Override
	public void insert(final BaseCell cell) {
		super.insert(cell);
		pack();
		upPack();  
    }
    @Override
	public void insert(final BaseCell cell, final int index) {
		super.insert(cell, index);
		pack();
		upPack();   
    }
    @Override
	public void remove(final BaseCell cell) {
		super.remove(cell);
		cellRemoved();
		
		pack();
		downPack();  
    }
	
	@Override
	public void removeAll() {
		super.removeAll();
		cellRemoved();
		
		pack();
		downPack();
    }
    
    @Override
	public void upPack() {
        if(graphListener != null) {
            graphListener.upPack();
        }
    }
	
	@Override
	public void downPack() {
        if(graphListener != null) {
            graphListener.downPack();
        }
    }
	
	public void cellRemoved() {
		if(internalListener != null) {
            internalListener.cellRemoved();
        }
	}
  
    @Override
	public void removeSelection() {
		if(graphListener != null) {
            graphListener.removeSelection();
		}
    }
	
	public void setMargin(final int margin) {
		marginX = margin;
		marginY = margin;
    }
	
    /**
    *
    *	Rezise the graph to its cells.
    *	
    */
    public void pack() {
		
		final int tmpX = this.marginX;
        final int tmpY = this.marginY;
        
        if(!drawMargin){
            marginX = 0;
            marginY = 0;
        }
        //check if we have any cells in this graph
        if((cells != null) && (cells.length > 0)) {
            
	    	int north = cells[0].getPos().y; 
	    	int east = cells[0].getPos().x; 
	    	int south = cells[0].getPos().y; 
	    	int west = cells[0].getPos().x;
	    
        	for (final GraphCell element : cells) {
		
                //check if this is the most distant cell
                if(element.getPos().y < north) {
		    		north = element.getPos().y;
				}
				if((element.getPos().x+element.getSize().width) > east) {
		    		east = element.getPos().x+element.getSize().width;
				}
				if((element.getPos().y+element.getSize().height) > south) {
		    		south = element.getPos().y+element.getSize().height;
				}
				if(element.getPos().x < west) {
		     		west = element.getPos().x;
				}
	    	}
	    	if((north !=0)||(west != 0)) {
				final Point graphPos = getLocation();
				graphPos.translate(west-marginX, north-marginY);
				setLocation(graphPos);
					for (final GraphCell element : cells) {
		    			final Point pos = element.getPos();
		    			pos.translate(-west+marginX, -north+marginY);
		    			element.setPos(pos);
					}
	    	}
	    	setSize(east-west+marginX*2, south-north+marginY*2);    
		}else {
	    	setSize(0,0);
		}

        //reset variables
        marginX = tmpX;
        marginY = tmpY;
		
        repaint(); 	
    }
    
    @Override
	public void repaint() {
		super.repaint();
    }
   
    @Override
	public void paintComponent(final Graphics g) {
	
        //konvert to Graphics2D
		final Graphics2D g2 = (Graphics2D) g;
		
        //add Lines to the graph
        if(drawSourceLines){
            if(targetCells != null){
                final Point[] pos = getInternalPoints(targetCells,GraphCell.UPPER_CENTER);
                if(pos != null){
                    for (final Point element : pos) {
                        Arc.drawLine(g2,element.x,0,element.x,element.y);
                    }
                }
            }
		}
		if(drawTargetLines){
            if(sourceCells != null){
                final Point[] pos = getInternalPoints(sourceCells,GraphCell.LOWER_CENTER);
                if(pos != null){
                    for (final Point element : pos) {
                        Arc.drawLine(g2,element.x,element.y,element.x,this.getHeight());
                    }
                }
            }
        }
        
        for (final GraphCell element : cells) {
            if(element instanceof Place){
                Arc.drawCellLines(g2,(Place)element);
            }
        }
       
        //do the basic
        super.paintComponent(g);
    }
    
    public Point[] getTargetPoints(){
        if(targetCells != null){
            Point[] internalTargetPoints = null;
            internalTargetPoints = getInternalPoints(targetCells,
                                                     GraphCell.UPPER_CENTER);
            for (final Point element : internalTargetPoints) {
                element.setLocation(element.x,0);
            }
            return internalTargetPoints;
        }
        return null;
    }
    public Point[] getSourcePoints(){
        if(sourceCells != null){
            Point[] internalTargetPoints = null;
            internalTargetPoints = getInternalPoints(sourceCells, 
                                                     GraphCell.LOWER_CENTER);
            for (final Point element : internalTargetPoints) {
                element.setLocation(element.x,
                                                    this.getHeight());
            }
            return internalTargetPoints;
        }
        return null;
    }
     
    private Point[] getInternalPoints(final BaseCell[] cells, final int anchor){
         if(cells != null){
            final Point[][] pMatrix = new Point[cells.length][]; 
            
            for(int i = 0; i < cells.length; i++){
                    pMatrix[i] = cells[i].getPoints(anchor);
            }
            return matrixToArray(pMatrix);
         }
         return null;
    }
    
    //ex indata {{1},{2 3 4 5},{6},{7 8}}
    //out {1,2,3,4,5,6,7,8}
    private Point[] matrixToArray(final Point[][] pMatrix){
        int length = 0;
        
        for (final Point[] element : pMatrix) {
            length = length + element.length;
        }
        
        final Point[] pArray = new Point[length];
        
        int index = 0;
        for (final Point[] element : pMatrix) {
            for(int ii = 0; ii < element.length; ii++){
                pArray[index] = element[ii];
                index = index + 1;
            }   
        }
        return pArray;
    }
    
    //
    //arrange cells
    //
    public void verticalLine(){
        Point pos = new Point(marginX,marginY);
        
        final int MARGIN = 20;
		 
        int translateX = 0;
        int translateY = 0;
        
        if((cells == null) || (cells.length < 2)){
			return;
		}
		
		//place first cell in middle
		for(int i = 1; i < cells.length; i++){
			//find max
			if(translateX < cells[i].getPos().x){
				translateX =  cells[i].getPos().x;
			}
			
			//find min
			if(translateY > cells[i].getPos().x){
				translateY =  cells[i].getPos().x;
			}
		}
		
		pos = new Point((translateX+translateY)/2,cells[0].getPos().y);
		
		cells[0].setPos(pos);
		
		//place the rest
		Point[] source = ((BaseCell)cells[0]).getSourcePoints();
		Point[] target;
		
		for(int i = 1; i < cells.length; i++){
			target = ((BaseCell)cells[i]).getTargetPoints();
			if(target[0].x > source[0].x){
				translateX = -(target[0].x - source[0].x);
			}else{
				translateX = (source[0].x - target[0].x);
			}
			
			if(target[0].y > source[0].y){
				translateY = (source[0].y - target[0].y);
			}else{
				translateY = -(target[0].y - source[0].y);
			}
			
			pos = cells[i].getPos();
			pos.translate(translateX,translateY);
			pos.translate(0,MARGIN);
			cells[i].setPos(pos);
			source = ((BaseCell)cells[i]).getSourcePoints();
		}
		pack();
	}
    
    
    /* Att göra:
     *En sorteringsfunktion som der till så att cellerna inte ändrar 
     *ordning då de radas upp
     *sortera efter placering
     */
    public void horizontalLine(){
        Point pos = new Point(marginX,marginY);
        
        final int MARGIN = 20;
		
        int translateX = 0;
        int translateY = 0;
        
        if((cells == null) || (cells.length < 2)){
			return;
		}
		
		//place first cell in middle heigth
		for(int i = 1; i < cells.length; i++){
			//find max
			if(translateX < cells[i].getPos().y){
				translateX =  cells[i].getPos().y;
			}
			
			//find min
			if(translateY > cells[i].getPos().y){
				translateY =  cells[i].getPos().y;
			}
		}
		
		pos = new Point(marginX,(translateX+translateY)/2);
		cells[0].setPos(pos);
		
		//place the rest
		for(int i = 0; i < (cells.length - 1); i++){
			translateX = cells[i].getWidth() + MARGIN;
			
			if(cells[i+1].getHeight()  == cells[i].getHeight()){
				translateY = 0;
			}else{
				translateY = cells[i].getHeight()/2 - cells[i+1].getHeight()/2;
			}
			
			pos = cells[i].getPos();
			pos.translate(translateX,translateY);
			cells[i+1].setPos(pos);
			
		}
		pack();
    }
    
    
    //------------------  Listeners -----------------------------
    public void addGraphListener(final CellListener l) {
		graphListener = l;
    }
    public void addInternalGraphListener(final InternalGraphListener l) {
		internalListener = l;
    }
    
    @Override
	public void setSize(final int w, final int h){
        final Dimension tmp = getSize();
        super.setSize(w,h);
        
        if((internalListener != null) && (tmp != getSize())){
            internalListener.sizeChanged(this);
        }
    }
    
    //------------------- CellListener ------------------------
    @Override
	public void cellPressed(final CellEvent cEvent) {
		if(graphListener != null) {	    
	    	graphListener.cellPressed(cEvent);
		}	
    }
    @Override
	public void cellReleased(final CellEvent cEvent) {
		if(graphListener != null) {	    	    	    
	    	graphListener.cellReleased(cEvent);
		}
        if(internalListener != null) {	    	    	
	    	internalListener.cellClicked(this,cEvent);
		}
    } 
    @Override
	public void cellDragged(final CellEvent cEvent) {
		pack();
		repaint();
		if(graphListener != null) {	    
	    	graphListener.cellDragged(cEvent);
		}
    }
    @Override
	public void cellMove(final CellEvent cEvent) {
		pack();
		repaint();
		if(graphListener != null) {
	    	graphListener.cellMove(cEvent);
		}
    }
}
