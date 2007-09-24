package org.supremica.external.processeditor.processgraph;

import java.awt.*;

import org.supremica.external.processeditor.xgraph.*;

/**
 * Allows customized graph edges.
 * <p>
 * The <code>OperationEdge</code> class <code>extends</code> the 
 * <code>org.xgraph.GraphEdge</code> class and overrides methods to allow
 * customized edge layout.
 */
public class OperationEdge extends GraphEdge {    

    public static final int MANUAL = 0;
    public static final int SEQUENCE = 1;    
    public static final int ALTERNATIVE_START = 2;
    public static final int ALTERNATIVE_END = 3;
    public static final int PARALLEL_START = 4;
    public static final int PARALLEL_END = 5;
    public static final int ARBITRARY = 6;
    private int mode = SEQUENCE;    
    
    public static final int DEFAULT_VERTICAL_LENGTH = 0;
    public static final int DEFAULT_HORIZONTAL_LENGTH = 0;

    public boolean sourceAnchorRelativePosition = true;
    public boolean targetAnchorRelativePosition = true;
    public OperationEdge() {		

    }
    /**
     * Creates a new instance of the class.
     * 
     * @param source the source cell of this edge
     * @param target the target cell of this edge
     */ 
    public OperationEdge(GraphCell source, GraphCell target) {
	setEdge(source, target);
    }
    /**
     * Creates a new instance which sets the operation edge type of this edge.
     * 
     * The index <code>i</code> specifies the choise of type.
     * It is recommended to use one of the following predefined finals:
     * <ul>
     * <li>OperationEdge.MANUAL</li>
     * <li>OperationEdge.SEQUENCE</li>
     * <li>OperationEdge.ALTERNATIVE_START</li>
     * <li>OperationEdge.ALTERNATIVE_END</li>
     * <li>OperationEdge.PARALLEL_START</li>
     * <li>OperationEdge.PARALLEL_END</li>
     * <li>OperationEdge.ARBITRARY</li>
     * </ul>
     *
     * @param i specifies the choise of operation edge type
     */
    public OperationEdge(int i) {	
	setMode(i);
    }    
    /**
     * Sets the operation edge type of this edge.
     * <p>
     * The index <code>i</code> specifies the choise of type.
     * It is recommended to use one of the following predefined finals:
     * <ul>
     * <li>OperationEdge.MANUAL</li>
     * <li>OperationEdge.SEQUENCE</li>
     * <li>OperationEdge.ALTERNATIVE_START</li>
     * <li>OperationEdge.ALTERNATIVE_END</li>
     * <li>OperationEdge.PARALLEL_START</li>
     * <li>OperationEdge.PARALLEL_END</li>
     * <li>OperationEdge.ARBITRARY</li>
     * </ul>
     * If there is no match for the index <code>i</code> the edge type
     * will be set identically to <code>SEQUENCE</code>.
     */
    protected void setMode(int i) {	
	if((i > 0)&&(i <= ARBITRARY)) {
	    mode = i;
	}else {
	    mode = SEQUENCE;
	}
    }	
    /**
     * Returns whether the source anchor position is relative or not.
     *
     * @return <code>true</code> if the source anchor position is relative, 
     * <code>false</code> otherwise.
     */    
    public boolean getSourceAnchorRelativePos() {
	return sourceAnchorRelativePosition;
    }
    /**
     * Returns wheter the target anchor position is relative or not.
     *
     * @return <code>true</code> if the target anchor position is relative,
     * <code>false</code> otherwise.
     */
    public boolean getTargetAnchorRelativePos() {
	return targetAnchorRelativePosition;
    }
    /**
     * Sets the source anchor position relative or not.
     * 
     * @param b if <code>true</code> the source anchor position will be set to
     * relative, otherwise <code>false</code>
     */
    public void setSourceAnchorRelativePos(boolean b) {
	sourceAnchorRelativePosition = b;
    }
    /**
     * Sets the target anchor position relative or not
     *
     * @param b if <code>true</code> the target anchor position will be set to
     * relative, otherwise <code>false</code>
     */
    public void setTargetAnchorRelativePos(boolean b) {
	targetAnchorRelativePosition = b;
    }    
    /**
     * Draws this edge.
     * 
     * @param g the graphic context
     */
    public void draw(Graphics g) {
	//DEBUG
	//System.out.println("OperationEdge.draw()");	
	//END DEBUG
	Point sourcePoint; Point targetPoint;
	if(sourceAnchorRelativePosition) {
	    sourcePoint = source.getPos(sourceAnchor);	    
	}else {	   
	    sourcePoint = source.getAnchorPos(sourceAnchor);	    
	}
	if(targetAnchorRelativePosition) {
	    targetPoint = target.getPos(targetAnchor);	    
	}else {	    	    
	    targetPoint = target.getAnchorPos(targetAnchor);	    
	}			
	int x1 = sourcePoint.x;
	int y1 = sourcePoint.y;
	int x2 = targetPoint.x;
	int y2 = targetPoint.y;
	if(mode == SEQUENCE) {
	    if((y2-y1) >= 0) {		
		EdgePainter.drawStraightEdge(g, x1, y1, x2, y2,
					     EdgePainter.VERTICAL_FIRST_BREAK_MID);	 
	    }else {	     		
		EdgePainter.drawStraightEdge(g, x1, y1, x2, y2,
					     EdgePainter.HORIZONTAL_FIRST_BREAK_MID);
	    }
	}else if(mode == ALTERNATIVE_START) {
	    EdgePainter.drawVerticalLine(g,
					 x1,
					 y1,					
					 y1+EdgePainter.DEFAULT_LINESPACE);
	    EdgePainter.drawStraightEdge(g, 
					 x1, 
					 y1+EdgePainter.DEFAULT_LINESPACE, 
					 x2, 
					 y2, 
					 EdgePainter.HORIZONTAL_FIRST);
	}else if(mode == ALTERNATIVE_END){
	    EdgePainter.drawStraightEdge(g, 
					 x1, 
					 y1, 
					 x2, 
					 y2-EdgePainter.DEFAULT_THICKNESS-1+
					 -EdgePainter.DEFAULT_LINESPACE,
					 EdgePainter.VERTICAL_FIRST);
	    EdgePainter.drawVerticalLine(g,
					 x2,
					 y2-EdgePainter.DEFAULT_THICKNESS-1+
					 -EdgePainter.DEFAULT_LINESPACE, 
					 y2);
	}else if(mode == PARALLEL_START) {
	    EdgePainter.drawVerticalLine(g,
					 x1,
					 y1,					
					 y1+EdgePainter.DEFAULT_LINESPACE);
	    EdgePainter.drawStraightEdge(g, 
					 x1,
					 y1+EdgePainter.DEFAULT_LINESPACE, 
					 x2, 
					 y2, 
					 EdgePainter.HORIZONTAL_FIRST_DUBBLE);
	}else if(mode == PARALLEL_END) {
	    EdgePainter.drawStraightEdge(g, 
					 x1, 
					 y1, 
					 x2, 
					 y2-EdgePainter.DEFAULT_THICKNESS+
					 -EdgePainter.DEFAULT_LINESPACE+
					 -EdgePainter.DEFAULT_LINESPACE, 
					 EdgePainter.VERTICAL_FIRST_DUBBLE);
	    EdgePainter.drawVerticalLine(g,
					x2,
					y2-EdgePainter.DEFAULT_THICKNESS+
					-EdgePainter.DEFAULT_LINESPACE,	       
					y2);
	}else if(mode == ARBITRARY) {	   
	    EdgePainter.drawHorizontalLine(g,
					   x1,
					   x2,
					   y1+EdgePainter.DEFAULT_THICKNESS);
	    EdgePainter.drawVerticalLine(g,
					 x2-EdgePainter.DEFAULT_THICKNESS-1,
					 y1,
					 y2);
	    EdgePainter.drawHorizontalLine(g,
					   x2,
					   x1,
					   y2-EdgePainter.DEFAULT_THICKNESS-1);
	    EdgePainter.drawVerticalLine(g,
					 x1+EdgePainter.DEFAULT_THICKNESS,
					 y2,
					 y1);
	}else {	    
	    super.draw(g);
	}
    }       
}

