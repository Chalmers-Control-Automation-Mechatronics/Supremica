package org.supremica.external.processeditor.xgraph;

import java.awt.Graphics;
import java.awt.Point;


/**
 * Represents the edges that can be added and manipulated to the graph.
 */
public class GraphEdge {

    public GraphCell source;
    public GraphCell target;
    public int sourceAnchor = GraphCell.LOWER_CENTER;
    public int targetAnchor = GraphCell.UPPER_CENTER;
        
    /**
     * Creates a new instance of the class.
     */
    public GraphEdge() {
	//DEBUG
	//System.out.println("GraphEdge()");
	//END DEBUG	
    }    
    /**
     * Creates a new instance of the class with the <code>s</code> cell
     * and <code>t</code> cell as source and target cell, respectively.
     *
     * @param s the source cell
     * @param t the target cell 
     */
    public GraphEdge(GraphCell s, GraphCell t) {
	setEdge(s,t);
    }
    /**
     * Sets the source and target cell.
     * 
     * @param s the source cell
     * @param t the target cell
     */
    public void setEdge(GraphCell s, GraphCell t) {	
	//DEBUG
	//System.out.println("GraphEdge.setEdge("+source+", "+target+")");
	//END DEBUG
	source = s;
	target = t;
    }
    /**
     * Sets the source cell.
     * 
     * @param s the source cell
     */
    public void setSource(GraphCell s) {	
	//DEBUG
	//System.out.println("GraphEdge.setSource("+source+")");
	//END DEBUG
	source = s;
    }
    /**
     * Sets the target cell.
     * 
     * 
     */
    public void setTarget(GraphCell t) {	
	//DEBUG
	//System.out.println("GraphEdge.setTarget("+target+")");
	//END DEBUG
	target = t;
    }
    /**
     * Sets the anchor position
     *
     * Defines where on the source and target cell this edge should 
     * "stick". 
     */
    public void setAnchor(int s, int t) {
	sourceAnchor = s;
	targetAnchor = t;
    }
    /**
     * Draws this edge.
     *
     * @param g the graphic context
     */
    public void draw(Graphics g) {			
	Point sourcePoint = source.getPos(sourceAnchor);	    
	Point targetPoint = target.getPos(targetAnchor);
	int x1 = sourcePoint.x;
	int y1 = sourcePoint.y;
	int x2 = targetPoint.x;
	int y2 = targetPoint.y;	   
	g.drawLine(x1, y1, x2, y2);
    }
}
